package indigo.clouddrive.backend.contracts;

import indigo.clouddrive.backend.exceptions.EncryptionException;
import jakarta.ejb.Local;

/**
 * Interface representing service used for encrypting and decrypting data when storing
 */
@Local
public interface ContentEncrypter {
    /**
     * Encrypts data using an encryption algorithm
     * @param data Data that should be encrypted
     * @param key Key that should be used to decrypt the data
     * @return Encrypted data
     * @throws EncryptionException When encryption cannot be executed properly
     */
    byte[] encrypt(byte[] data, String key) throws EncryptionException;
    /**
     * Decrypts data that was encrypted using an encryption algorithm
     * @param data Data that should be decrypted
     * @param key Key that should be used to decrypt the data
     * @return Decrypted data
     * @throws EncryptionException When decryption cannot be executed properly
     */
    byte[] decrypt(byte[] data, String key) throws EncryptionException;

    /**
     * Gets the unique identifier of the implementation
     * <br>
     * Can be used to create multiple paths based on which algorithm is used
     * @return String identifier of the content encrypter
     */
    @SuppressWarnings("unused")
    String getEncryptionPathKey();
}
