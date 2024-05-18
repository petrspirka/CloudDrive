package indigo.clouddrive.backend.helpers;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

public class CryptoHelpers {
    /**
     * Creates an SHA-256 hash out of the given string
     * @param passwd The string to hash
     * @return Hashed representation of the string
     */
    public static String getHash(String passwd) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(passwd.getBytes());
            return Base64.getEncoder().encodeToString(md.digest());
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Generates a random hexadecimal string
     * @param length The length of the generated string
     * @return Random hexadecimal string
     */
    public static String generateRandomHexString(int length){
        StringBuilder hexString = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            hexString.append(Integer.toHexString(random.nextInt(16)).toUpperCase());
        }
        return hexString.toString();
    }

    /**
     * Returns a custom string that has 64 different characters. Keep in mind this is NOT a base64 string
     * @param length The amount of characters in this string
     * @return A random string that uses at most 64 different characters
     */
    public static String generateRandomCustomBase64String(int length){
        StringBuilder base64String = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            base64String.append(numberToBase64Character(random.nextInt(64)));
        }
        return base64String.toString();
    }

    public static String base64StringToCustomBase64String(String base64String){
        return base64String.replace('/', '-').replace("=", "");
    }

    private static String numberToBase64Character(int number){
        if(number < 0 || number > 63){
            throw new IllegalArgumentException("The specified number '" + number + "' cannot be casted to a base64 character");
        }
        if(number < 10){
            return String.valueOf(number);
        }
        number -= 10;
        if(number < 26){
            return Character.toString(number + 65);
        }
        number -= 26;
        if(number < 26){
            return Character.toString(number + 97);
        }
        number -= 26;
        if(number == 0){
            return "-";
        }
        else {
            return "+";
        }
    }

    public static String generateRandomAESSalt() {
        KeyGenerator keyGen = null;
        try {
            keyGen = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        keyGen.init(256); // for example
        SecretKey secretKey = keyGen.generateKey();
        return new String(secretKey.getEncoded(), StandardCharsets.US_ASCII);
    }
}
