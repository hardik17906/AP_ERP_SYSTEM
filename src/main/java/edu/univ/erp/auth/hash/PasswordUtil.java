package edu.univ.erp.auth.hash;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class for handling password hashing and verification using jBCrypt.
 *
 * This class provides static methods to securely hash a plaintext password
 * and to check a plaintext password against a stored hash.
 */
public class PasswordUtil {

    private static final Logger log = LoggerFactory.getLogger(PasswordUtil.class);

    // The "work factor" for bcrypt. 
    // 12 is a good, standard balance between security and performance
    // Higher numbers are more secure but slower.
    private static final int WORK_FACTOR = 12;

    /**
     * Hashes a plaintext password using bcrypt with a generated salt.
     *
     * @param plainTextPassword The password to hash.
     * @return A string containing the bcrypt hash (which includes the salt).
     */
    public static String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.isEmpty()) {
            log.warn("Attempted to hash an empty or null password.");
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        
        // BCrypt.gensalt() automatically generates a random salt.
        // BCrypt.hashpw() combines the password and salt and hashes them.
        // The resulting string contains the hash, the salt, and the work factor.
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    /**
     * Checks a plaintext password against a stored bcrypt hash.
     *
     * @param plainTextPassword The password from the user's login attempt.
     * @param storedHash The hash retrieved from the 'users_auth' database.
     * @return true if the password matches the hash, false otherwise.
     */
    public static boolean checkPassword(String plainTextPassword, String storedHash) {
        if (plainTextPassword == null || storedHash == null || 
            plainTextPassword.isEmpty() || storedHash.isEmpty()) {
            
            log.warn("Password or hash is null/empty during check.");
            return false;
        }

        try {
            // BCrypt.checkpw handles extracting the salt from the storedHash
            // and performs the comparison.
            return BCrypt.checkpw(plainTextPassword, storedHash);
        } catch (Exception e) {
            // This can happen if the storedHash is malformed
            log.error("Error during password check. Is the hash valid? Hash: {}", storedHash, e);
            return false;
        }
    }

    public static void main(String[] args) {
        String passwordToHash = "password123";
        String generatedHash = hashPassword(passwordToHash);

        System.out.println("--- Password Hash Generator ---");
        System.out.println("Password: " + passwordToHash);
        System.out.println("Hashed:   " + generatedHash);
        System.out.println("\nPaste this hash into your 3_seed_data.sql file.");

        // Test the checkPassword method
        boolean match = checkPassword(passwordToHash, generatedHash);
        System.out.println("\nVerification check (should be true): " + match);
        
        boolean noMatch = checkPassword("wrongpassword", generatedHash);
        System.out.println("Verification check (should be false): " + noMatch);
    }
}