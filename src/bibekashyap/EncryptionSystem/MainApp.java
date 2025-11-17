package bibekashyap.EncryptionSystem;

import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.awt.datatransfer.DataFlavor;

/**
 * MainApp: main UI + integrates existing encryption features and LAN chat
 */
public class MainApp extends JFrame {

    private JTextField txtSelected;
    private JButton btnBrowse, btnEncrypt, btnDecrypt, btnGenKey, btnSaveKey, btnLoadKey;
    private JProgressBar progressBar;
    private JFileChooser fileChooser;
    private SecretKey currentKey = null;
    private File selectedFile = null;
    private JLabel lblKeyStatus;

    public MainApp() {
        super("EncryptX - File Encrypt/Decrypt (AES)");
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // === DRAG & DROP SUPPORT (fixed) ===
        new java.awt.dnd.DropTarget(this, new java.awt.dnd.DropTargetAdapter() {
            @Override
            public void drop(java.awt.dnd.DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(dtde.getDropAction());
                    @SuppressWarnings("unchecked")
                    java.util.List<File> droppedFiles =
                            (java.util.List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (droppedFiles != null && droppedFiles.size() > 0) {
                        File f = droppedFiles.get(0);
                        // update UI on Event Dispatch Thread
                        SwingUtilities.invokeLater(() -> {
                            selectedFile = f;
                            txtSelected.setText(f.getAbsolutePath());
                            updateActionButtons();
                            JOptionPane.showMessageDialog(MainApp.this, "File selected:\n" + f.getName(), "Drag & Drop", JOptionPane.INFORMATION_MESSAGE);
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        setSize(760, 420);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(45,118,232));
        header.setBorder(new EmptyBorder(14, 16, 14, 16));
        JLabel title = new JLabel("EncryptX - File Encryption Tool");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Main Card
        JPanel main = new JPanel();
        main.setBackground(new Color(245,245,245));
        main.setBorder(new EmptyBorder(18,18,18,18));
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220,220,220),1,true),
                new EmptyBorder(18,18,18,18)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // File selection row
        JLabel lblSelect = new JLabel("1) Choose file to encrypt / decrypt:");
        lblSelect.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblSelect);
        card.add(Box.createVerticalStrut(8));

        JPanel pRow = new JPanel(new BorderLayout(8,8));
        pRow.setOpaque(false);
        txtSelected = new JTextField();
        txtSelected.setEditable(false);
        pRow.add(txtSelected, BorderLayout.CENTER);
        btnBrowse = new JButton("Browse");
        pRow.add(btnBrowse, BorderLayout.EAST);
        pRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        card.add(pRow);
        card.add(Box.createVerticalStrut(12));

        // Key controls
        JLabel lblKey = new JLabel("2) Key (Generate / Save / Load):");
        lblKey.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblKey);
        card.add(Box.createVerticalStrut(8));

        JPanel pKey = new JPanel();
        pKey.setOpaque(false);
        btnGenKey = new JButton("Generate Key");
        btnSaveKey = new JButton("Save Key");
        btnLoadKey = new JButton("Load Key");
        btnSaveKey.setEnabled(false); // no key yet
        pKey.add(btnGenKey);
        pKey.add(btnSaveKey);
        pKey.add(btnLoadKey);
        card.add(pKey);
        card.add(Box.createVerticalStrut(8));

        lblKeyStatus = new JLabel("No key loaded");
        lblKeyStatus.setForeground(Color.DARK_GRAY);
        card.add(lblKeyStatus);
        card.add(Box.createVerticalStrut(12));

        // Encrypt / Decrypt buttons
        JLabel lblAct = new JLabel("3) Action:");
        lblAct.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblAct);
        card.add(Box.createVerticalStrut(8));

        JPanel pAct = new JPanel();
        pAct.setOpaque(false);
        btnEncrypt = new JButton("Encrypt");
        btnDecrypt = new JButton("Decrypt");
        btnEncrypt.setEnabled(false);
        btnDecrypt.setEnabled(false);
        pAct.add(btnEncrypt);
        pAct.add(btnDecrypt);

        // Chat button
        JButton btnChat = new JButton("Open LAN Chat");
        pAct.add(btnChat);
        btnChat.addActionListener(e -> openChatWindow());

        card.add(pAct);
        card.add(Box.createVerticalStrut(16));

        // Progress
        progressBar = new JProgressBar(0,100);
        progressBar.setStringPainted(true);
        card.add(progressBar);

        main.add(card);
        add(main, BorderLayout.CENTER);

        // File chooser
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new AllFileFilter());

        // Listeners
        btnBrowse.addActionListener(e -> onBrowse());
        btnGenKey.addActionListener(e -> onGenerateKey());
        btnSaveKey.addActionListener(e -> onSaveKey());
        btnLoadKey.addActionListener(e -> onLoadKey());
        btnEncrypt.addActionListener(e -> onEncrypt());
        btnDecrypt.addActionListener(e -> onDecrypt());

        setVisible(true);
    }

    private void onBrowse() {
        int res = fileChooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            txtSelected.setText(selectedFile.getAbsolutePath());
            updateActionButtons();
        }
    }

    private void onGenerateKey() {
        try {
            currentKey = CryptoUtils.generateKey();
            lblKeyStatus.setText("Key generated (not saved)");
            btnSaveKey.setEnabled(true);
            updateActionButtons();
            JOptionPane.showMessageDialog(this, "New AES key generated.", "Key", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to generate key: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSaveKey() {
        if (currentKey == null) return;
        JFileChooser saver = new JFileChooser();
        saver.setDialogTitle("Save key to file (.key recommended)");
        int r = saver.showSaveDialog(this);
        if (r == JFileChooser.APPROVE_OPTION) {
            File out = saver.getSelectedFile();
            try {
                CryptoUtils.saveKeyToFile(currentKey, out);
                JOptionPane.showMessageDialog(this, "Key saved to: " + out.getAbsolutePath(), "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to save key: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onLoadKey() {
        JFileChooser loader = new JFileChooser();
        loader.setDialogTitle("Load key file");
        int r = loader.showOpenDialog(this);
        if (r == JFileChooser.APPROVE_OPTION) {
            File keyFile = loader.getSelectedFile();
            try {
                currentKey = CryptoUtils.loadKeyFromFile(keyFile);
                lblKeyStatus.setText("Key loaded: " + keyFile.getName());
                btnSaveKey.setEnabled(true);
                updateActionButtons();
                JOptionPane.showMessageDialog(this, "Key loaded.", "Loaded", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to load key: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onEncrypt() {
        if (!checkReady()) return;

        JFileChooser saver = new JFileChooser();
        saver.setDialogTitle("Save encrypted file (choose name, extension will keep same)");
        saver.setSelectedFile(new File(selectedFile.getName() + ".enc"));
        int r = saver.showSaveDialog(this);
        if (r != JFileChooser.APPROVE_OPTION) return;
        File out = saver.getSelectedFile();

        // do encryption in background
        new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {
                setUIEnabled(false);
                progressBar.setValue(0);
                CryptoUtils.encryptFile(selectedFile, out, currentKey, percent -> setProgress(percent));
                return null;
            }

            @Override
            protected void done() {
                setUIEnabled(true);
                progressBar.setValue(100);
                JOptionPane.showMessageDialog(MainApp.this, "Encryption completed:\n" + out.getAbsolutePath(), "Done", JOptionPane.INFORMATION_MESSAGE);
            }
        }.execute();
    }

    private void onDecrypt() {
        if (!checkReady()) return;

        JFileChooser saver = new JFileChooser();
        saver.setDialogTitle("Save decrypted file (choose name and proper extension)");
        saver.setSelectedFile(new File("decrypted_" + selectedFile.getName().replace(".enc","")));
        int r = saver.showSaveDialog(this);
        if (r != JFileChooser.APPROVE_OPTION) return;
        File out = saver.getSelectedFile();

        new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {
                setUIEnabled(false);
                progressBar.setValue(0);
                CryptoUtils.decryptFile(selectedFile, out, currentKey, percent -> setProgress(percent));
                return null;
            }

            @Override
            protected void done() {
                setUIEnabled(true);
                progressBar.setValue(100);
                JOptionPane.showMessageDialog(MainApp.this, "Decryption completed:\n" + out.getAbsolutePath(), "Done", JOptionPane.INFORMATION_MESSAGE);
            }
        }.execute();
    }

    private boolean checkReady() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Please choose a file first.", "No file", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (currentKey == null) {
            JOptionPane.showMessageDialog(this, "Please generate or load a key first.", "No key", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void updateActionButtons() {
        boolean hasFile = selectedFile != null;
        boolean hasKey = currentKey != null;
        btnEncrypt.setEnabled(hasFile && hasKey);
        btnDecrypt.setEnabled(hasFile && hasKey);
    }

    private void setUIEnabled(boolean enabled) {
        btnBrowse.setEnabled(enabled);
        btnGenKey.setEnabled(enabled);
        btnSaveKey.setEnabled(enabled && currentKey != null);
        btnLoadKey.setEnabled(enabled);
        btnEncrypt.setEnabled(enabled && selectedFile != null && currentKey != null);
        btnDecrypt.setEnabled(enabled && selectedFile != null && currentKey != null);
    }

    // update Swing progress safely
    public void setProgress(int nv) {
        SwingUtilities.invokeLater(() -> progressBar.setValue(nv));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainApp::new);
    }

    // ===== Chat Window implementation (text + file send/receive) =====
    private void openChatWindow() {
        JFrame chatFrame = new JFrame("LAN Chat");
        chatFrame.setSize(560, 520);
        chatFrame.setLocationRelativeTo(null);

        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);

        JTextField input = new JTextField();
        JButton sendBtn = new JButton("Send");

        JButton serverBtn = new JButton("Start Server");
        JButton clientBtn = new JButton("Connect to Server");
        JButton sendFileBtn = new JButton("Send File");

        ChatServer server = new ChatServer();
        ChatClient client = new ChatClient();

        JPanel topPanel = new JPanel();
        topPanel.add(serverBtn);
        topPanel.add(clientBtn);
        topPanel.add(sendFileBtn);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(input, BorderLayout.CENTER);
        bottomPanel.add(sendBtn, BorderLayout.EAST);

        chatFrame.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        chatFrame.add(topPanel, BorderLayout.NORTH);
        chatFrame.add(bottomPanel, BorderLayout.SOUTH);

        // Start server
        serverBtn.addActionListener(e -> {
            new Thread(() -> {
                try {
                    server.startServer(5000, msg -> SwingUtilities.invokeLater(() -> chatArea.append(msg + "\n")));
                    SwingUtilities.invokeLater(() -> chatArea.append("Client connected!\n"));
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> chatArea.append("Server error: " + ex.getMessage() + "\n"));
                }
            }).start();
        });

        // Connect as client
        clientBtn.addActionListener(e -> {
            String ip = JOptionPane.showInputDialog(chatFrame, "Enter server IP address:");
            if (ip == null || ip.trim().isEmpty()) return;
            new Thread(() -> {
                try {
                    client.connectToServer(ip.trim(), 5000, msg -> SwingUtilities.invokeLater(() -> chatArea.append(msg + "\n")));
                    SwingUtilities.invokeLater(() -> chatArea.append("Connected to server!\n"));
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> chatArea.append("Connection failed: " + ex.getMessage() + "\n"));
                }
            }).start();
        });

        // Send text
        sendBtn.addActionListener(e -> {
            String msg = input.getText().trim();
            if (msg.isEmpty()) return;
            chatArea.append("Me: " + msg + "\n");
            // send both (one may be null)
            server.sendText(msg);
            client.sendText(msg);
            input.setText("");
        });

        // Send file button
        sendFileBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int r = chooser.showOpenDialog(chatFrame);
            if (r != JFileChooser.APPROVE_OPTION) return;
            File toSend = chooser.getSelectedFile();
            // send in background
            new Thread(() -> {
                try {
                    // try both, one will throw if not connected
                    server.sendFile(toSend);
                } catch (Exception ex1) {
                    try {
                        client.sendFile(toSend);
                    } catch (Exception ex2) {
                        SwingUtilities.invokeLater(() ->
                            chatArea.append("File send failed (not connected): " + ex2.getMessage() + "\n"));
                        return;
                    }
                }
                SwingUtilities.invokeLater(() ->
                    chatArea.append("Sent file: " + toSend.getName() + "\n"));
            }).start();
        });

        chatFrame.setVisible(true);
    }
}
