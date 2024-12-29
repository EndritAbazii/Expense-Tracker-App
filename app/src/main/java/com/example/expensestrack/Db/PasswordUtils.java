package com.example.expensestrack.Db;



import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtils {
    private static final int SALT_LENGTH = 16; // bytes
    private static final int HASH_LENGTH = 64; // bytes
    private static final int ITERATIONS = 10000;

    public static byte[] generateSalt() {
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        sr.nextBytes(salt);
        return salt;
    }

    public static byte[] hashPassword(final char[] password, final byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, HASH_LENGTH * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public static boolean verifyPassword(String password, byte[] salt, byte[] expectedHash) {
        byte[] passwordHash = hashPassword(password.toCharArray(), salt);
        if (passwordHash.length != expectedHash.length) return false;
        for (int i = 0; i < passwordHash.length; i++) {
            if (passwordHash[i] != expectedHash[i]) {
                return false;
            }
        }
        return true;
    }
}
