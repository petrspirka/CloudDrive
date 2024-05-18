package indigo.clouddrive.backend.exceptions;

import java.io.IOException;

public class LockAlreadyAcquiredException extends IOException {
    public LockAlreadyAcquiredException(String message){
        super(message);
    }
}
