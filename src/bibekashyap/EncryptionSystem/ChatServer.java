package bibekashyap.EncryptionSystem;

import java.io.*;
import java.net.*;
import javax.swing.*;

public class ChatServer {

    private ServerSocket serverSocket;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public interface MessageListener {
        void onMessageReceived(String msg);
    }

    // Start server and wait for single client
    public void startServer(int port, MessageListener listener) throws Exception {
        serverSocket = new ServerSocket(port);
        String ip = InetAddress.getLocalHost().getHostAddress();
        // Inform user
        SwingUtilities.invokeLater(() ->
            JOptionPane.showMessageDialog(null,
                    "Server started!\nWaiting for client...\nIP: " + ip,
                    "Chat Server", JOptionPane.INFORMATION_MESSAGE)
        );

        socket = serverSocket.accept(); // blocking
        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

        // reader thread
        new Thread(() -> {
            try {
                while (!socket.isClosed()) {
                    int type;
                    try {
                        type = in.readInt();
                    } catch (EOFException eof) { break; }
                    if (type == 1) { // text
                        String text = in.readUTF();
                        listener.onMessageReceived("Peer: " + text);
                    } else if (type == 2) { // file
                        String filename = in.readUTF();
                        long filesize = in.readLong();
                        File recvDir = new File(System.getProperty("user.home"), "received_files");
                        if (!recvDir.exists()) recvDir.mkdirs();
                        File outFile = new File(recvDir, filename);
                        try (FileOutputStream fos = new FileOutputStream(outFile)) {
                            byte[] buffer = new byte[8192];
                            long remaining = filesize;
                            while (remaining > 0) {
                                int read = in.read(buffer, 0, (int)Math.min(buffer.length, remaining));
                                if (read == -1) throw new EOFException("Unexpected EOF while reading file");
                                fos.write(buffer, 0, read);
                                remaining -= read;
                            }
                            fos.flush();
                        }
                        listener.onMessageReceived("Received file: " + outFile.getAbsolutePath());
                    } else {
                        // unknown type — ignore
                    }
                }
            } catch (Exception ex) {
                // notify listener on error
                listener.onMessageReceived("Connection closed or error: " + ex.getMessage());
            } finally {
                closeQuietly();
            }
        }).start();
    }

    public void sendText(String msg) {
        try {
            if (out == null) return;
            out.writeInt(1);
            out.writeUTF(msg);
            out.flush();
        } catch (Exception ex) {
            // ignore or log
        }
    }

    // send a file to the connected client
    public void sendFile(File file) throws Exception {
        if (out == null) throw new IOException("Not connected");
        out.writeInt(2);
        out.writeUTF(file.getName());
        out.writeLong(file.length());
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int n;
            while ((n = fis.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
        }
        out.flush();
    }

    public void stopServer() {
        closeQuietly();
        try { if (serverSocket != null) serverSocket.close(); } catch (Exception ignored) {}
    }

    private void closeQuietly() {
        try { if (in != null) in.close(); } catch (Exception ignored) {}
        try { if (out != null) out.close(); } catch (Exception ignored) {}
        try { if (socket != null) socket.close(); } catch (Exception ignored) {}
    }
}
