package indigo.clouddrive.backend.impl;

import indigo.clouddrive.backend.contracts.ContentEncrypter;
import indigo.clouddrive.backend.exceptions.EncryptionException;
import jakarta.annotation.Priority;
import jakarta.ejb.Stateless;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Priority(1)
@Stateless
public class AESContentEncrypter implements ContentEncrypter {
    @Override
    public byte[] encrypt(byte[] data, String key) throws EncryptionException {
        return runCipher(Cipher.ENCRYPT_MODE, data, key);
    }

    @Override
    public byte[] decrypt(byte[] data, String key) throws EncryptionException {
        return runCipher(Cipher.DECRYPT_MODE, data, key);
    }

    private byte[] runCipher(int mode, byte[] data, String key) throws EncryptionException {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.US_ASCII), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(mode, keySpec);
            return cipher.doFinal(data);
        }
        catch(IllegalBlockSizeException | BadPaddingException ex){
            throw new EncryptionException("Could not encrypt the specified data", ex);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            throw new EncryptionException("Could not get the AES encryption. Make sure your environment supports this or use another implementation of ContentEncrypter", e);
        } catch (InvalidKeyException e) {
            throw new EncryptionException("The specified encryption key is invalid", e);
        }
    }

    @Override
    public String getEncryptionPathKey() {
        return "AES";
    }
}
