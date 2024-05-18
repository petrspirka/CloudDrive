package indigo.clouddrive.backend.exceptions;

import java.security.GeneralSecurityException;

public class EncryptionException extends GeneralSecurityException {
    public EncryptionException(String message, Throwable cause){
        super(message, cause);
    }
}
