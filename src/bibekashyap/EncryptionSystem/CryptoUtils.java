package bibekashyap.EncryptionSystem;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.SecureRandom;
import java.security.Key;
import java.nio.file.Files;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import java.security.SecureRandom;


public class CryptoUtils {

    // AES/CBC/PKCS5Padding with 128-bit key
    private static final String CIPHER_ALGO = "AES/CBC/PKCS5Padding";
    private static final int AES_KEY_SIZE = 128; // bits
    private static final int IV_SIZE = 16; // bytes
    private static final int BUFFER_SIZE = 8192;

    // Generate a new AES key
    public static SecretKey generateKey() throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(AES_KEY_SIZE, new SecureRandom());
        return kg.generateKey();
    }

    // Save raw key bytes to a file (not encrypted). For real apps protect this!
    public static void saveKeyToFile(SecretKey key, File outFile) throws IOException {
        byte[] keyBytes = key.getEncoded();
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            fos.write(keyBytes);
        }
    }

    // Load key from file (expects raw key bytes)
    public static SecretKey loadKeyFromFile(File keyFile) throws IOException {
        byte[] keyBytes = Files.readAllBytes(keyFile.toPath());
        return new SecretKeySpec(keyBytes, "AES");
    }

    // Encrypt file: write IV as first 16 bytes, then cipher bytes
    // progressCallback receives value 0..100
    public static void encryptFile(File input, File output, SecretKey key, ProgressCallback progressCallback) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
        byte[] iv = new byte[IV_SIZE];
        SecureRandom rnd = new SecureRandom();
        rnd.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

        try (FileInputStream fis = new FileInputStream(input);
             FileOutputStream fos = new FileOutputStream(output);
             CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {

            // first write IV to output so decrypt can read it
            fos.write(iv);

            long total = input.length();
            long read = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            int n;
            while ((n = fis.read(buffer)) != -1) {
                cos.write(buffer, 0, n);
                read += n;
                if (progressCallback != null) {
                    int p = (int)((read * 100) / total);
                    progressCallback.onProgress(Math.min(p, 100));
                }
            }
            cos.flush();
        }
    }

    // Decrypt file: reads IV first
    public static void decryptFile(File input, File output, SecretKey key, ProgressCallback progressCallback) throws Exception {
        try (FileInputStream fis = new FileInputStream(input)) {
            // read IV
            byte[] iv = new byte[IV_SIZE];
            int got = fis.read(iv);
            if (got != IV_SIZE) throw new IOException("Invalid encrypted file (missing IV)");

            Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

            long total = input.length() - IV_SIZE;
            long read = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            int n;

            try (CipherInputStream cis = new CipherInputStream(fis, cipher);
                 FileOutputStream fos = new FileOutputStream(output)) {

                while ((n = cis.read(buffer)) != -1) {
                    fos.write(buffer, 0, n);
                    read += n;
                    if (progressCallback != null && total > 0) {
                        int p = (int)((read * 100) / total);
                        progressCallback.onProgress(Math.min(p, 100));
                    }
                }
                fos.flush();
            }
        }
    }

    // Small callback interface for progress updates
    public interface ProgressCallback {
        void onProgress(int percent);
    }
}
