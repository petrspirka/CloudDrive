package indigo.clouddrive.frontend;

import indigo.clouddrive.backend.contracts.ActiveSession;
import indigo.clouddrive.backend.contracts.FileManager;
import indigo.clouddrive.backend.contracts.UserStorage;
import indigo.clouddrive.backend.exceptions.LockAlreadyAcquiredException;
import indigo.clouddrive.backend.impl.FileManagerBean;
import indigo.clouddrive.backend.models.StorageObject;
import indigo.clouddrive.frontend.helpers.PathHelpers;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;

@ViewScoped
@Named("fileController")
public class FileController implements Serializable {
    private String status;
    private String content = null;
    private String oldPath = null;
    private @Inject ActiveSession activeSession;
    private @Inject FileManager fileManager;
    @EJB
    private UserStorage userStorage;

    public boolean setFile(String path) {
        try {
            StorageObject object = userStorage.getStorageObject(activeSession.getUser(), path);
            fileManager.lockFile(object);
            content = new String(fileManager.read(), StandardCharsets.UTF_8);
            oldPath = path;
            return true;
        }
        catch(FileNotFoundException ex){
            status = "The specified file could not be found. This usually means the underlying file system has lost this file. Contact an administrator for help.";
        }
        catch(LockAlreadyAcquiredException | OverlappingFileLockException ex){
            status = "The file is already locked by another user";
        }
        catch(IOException ex){
            status = "Missing permissions to open this file for editing";
        }
        return false;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getContent() throws IOException {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String saveAndClose(String path) {
        try {
            this.fileManager.write(content.getBytes(StandardCharsets.UTF_8));
            return redirectBack(path);
        }
        catch(LockAlreadyAcquiredException ex){
            FacesContext.getCurrentInstance().addMessage("path", new FacesMessage("The file has been locked by another process. It is recommended to make a local copy of the file and replacing it once the other user has finished editing"));
        }
        catch(IOException ex){
            FacesContext.getCurrentInstance().addMessage("path", new FacesMessage("Could not save the file. It is recommended to make a local copy of the file and trying to refresh the page"));
        }
        return "";
    }

    public String cancel(String path){
        return redirectBack(path);
    }

    private String redirectBack(String path){
        return "index.xhtml?faces-redirect=true&path=" + PathHelpers.getPreviousPathName(path);
    }

    @PreDestroy
    public void onDestroy() {
        try {
            this.fileManager.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
