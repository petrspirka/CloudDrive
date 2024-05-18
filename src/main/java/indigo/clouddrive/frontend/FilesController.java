package indigo.clouddrive.frontend;

import indigo.clouddrive.backend.contracts.*;
import indigo.clouddrive.backend.models.Share;
import indigo.clouddrive.backend.models.StorageObject;
import indigo.clouddrive.backend.models.User;
import indigo.clouddrive.frontend.helpers.PathHelpers;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ViewScoped
@Named("filesController")
public class FilesController implements Serializable {
    @EJB
    private UserStorage userStorage;

    private @Inject FileManager fileManager;

    @EJB
    private ShareManager shareManager;

    @EJB
    private UserManager userManager;

    private @Inject ActiveSession activeSession;
    private String manipulationField;
    private String error = "";
    private Part file;

    public List<StorageObject> getObjects(String path) throws IOException {
        if(activeSession.getUser() == null){
            return new ArrayList<>();
        }
        try {
            return this.userStorage.getFolderContents(activeSession.getUser(), path);
        } catch (IOException e) {
            addError("Could not fetch the specified folder's objects");
            if(PathHelpers.getTrimmedPath(path).startsWith("shares/")){
                //TODO: There has to be a better way to manage paths...
                FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml?path=" + getPreviousPathName(path));
            }
            return new ArrayList<>();
        }
    }

    public String getManipulationField() {
        return manipulationField;
    }

    public void setManipulationField(String manipulationField) {
        this.manipulationField = manipulationField;
    }

    public void addFile(String basePath) {
        String fullPath = Path.of(basePath, this.manipulationField).toString();
        try {
            this.userStorage.createFile(activeSession.getUser(), fullPath);
        } catch (IOException e) {
            addError("Could not add the specified file");
        }
    }

    public void addFolder(String basePath) {
        String fullPath = Paths.get(basePath, this.manipulationField).toString();
        try {
            this.userStorage.createFolder(activeSession.getUser(), fullPath);
        } catch (IOException e) {
            addError("Could not add the specified folder");
        }
    }

    public void deleteStorageObject(String path) {
        try {
            this.userStorage.deleteStorageObject(activeSession.getUser(), path);
            FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml?path=" + PathHelpers.getPreviousPathName(path));
        } catch (IOException e) {
            addError("Could not delete the specified file/folder");
        }
        catch(OverlappingFileLockException ex){
            addError("One or more storage objects that were supposed to be deleted were locked by another user");
        }
    }

    public void downloadStorageObject(String path){
        byte[] data;
        try {
            data = this.fileManager.compressStorageObject(userStorage.getStorageObject(activeSession.getUser(), path, true), PathHelpers.getPreviousPathName(path));
        }
        catch(IOException ex){
            addError("Could not download the specified file/folder");
            return;
        }
        catch(OverlappingFileLockException ex){
            addError("One or more storage objects that were supposed to be downloaded were locked by another user");
            return;
        }
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.setResponseContentType("application/zip");
        ec.setResponseContentLength(data.length);
        ec.setResponseHeader("Content-Disposition", "attachment; filename=\"" + PathHelpers.getPathEnding(path) + ".zip\"");
        try(OutputStream stream = ec.getResponseOutputStream()){
            stream.write(data);
        }
        catch(IOException ex){
            addError("Could not write data to output stream");
        }
    }

    private void addError(String message){
        //FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(message));
        error = message;
    }

    public String getError() {
        return error;
    }

    public void renameStorageObject() {
        String path = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("pathInput");
        if(path.isEmpty()){
            addError("Missing path parameter");
            return;
        }
        try {
            StorageObject object = userStorage.getStorageObject(activeSession.getUser(), path, false);
            userStorage.renameStorageObject(activeSession.getUser(), path, manipulationField);
        } catch (IOException e) {
            addError("Could not rename the specified file/folder");
        }
        catch(OverlappingFileLockException ex){
            addError("One or more storage objects that were supposed to be renamed were locked by another user");
        }
    }

    public void shareStorageObject(){
        Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String path = requestParameterMap.get("pathInput");
        String isReadOnly = requestParameterMap.get("isReadOnly");

        if(path == null || path.isEmpty()){
            addError("Missing path parameter");
            return;
        }
        try {
            User targetUser = userManager.getUserUnsafe(manipulationField);
            if(targetUser == null){
                addError("The specified user could not be found");
                return;
            }
            shareManager.addShare(userStorage.getStorageObject(activeSession.getUser(), path), targetUser, isReadOnly != null && isReadOnly.equals("on"));
        } catch (IOException e) {
            addError("Could not add the specified share");
        }
    }

    public void removeStorageObjectShare(){
        Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String path = requestParameterMap.get("pathInput");

        if(path == null || path.isEmpty()){
            addError("Missing path parameter");
            return;
        }

        try {
            User toRemove = userManager.getUserUnsafe(manipulationField);
            StorageObject object = userStorage.getStorageObject(activeSession.getUser(), path);
            shareManager.removeShare(object, toRemove);
        } catch (IOException e) {
            addError("Could not remove the specified share");
        }
    }

    public String getShares(String path){
        try {
            List<Share> shares = shareManager.getShares(userStorage.getStorageObject(activeSession.getUser(), path));
            StringBuilder sb = new StringBuilder();
            shares.forEach((share) -> sb.append(share.getOwner().getLoginName()).append(share.isReadOnly() ? "(R)" : "(RW)").append(", "));
            return sb.toString();
        } catch (IOException e) {
            addError("Could not get shares for the path " + path);
            return "";
        }
    }

    public boolean isShare(String path){
        return PathHelpers.getTrimmedPath(path).equals("shares");
    }

    public boolean canWrite(String path) {
        try {
            StorageObject object = userStorage.getStorageObject(activeSession.getUser(), path);
            return shareManager.canWrite(object, activeSession.getUser());
        } catch (IOException ex){
            return false;
        }
    }

    public void makeCopy(String path) {
        try {
            userStorage.copyStorageObject(activeSession.getUser(), path);
        }
        catch(IOException ex){
            addError("Could not copy the specified file: " + ex.getMessage());
        }
        catch(OverlappingFileLockException ex){
            addError("One or more storage objects that were supposed to be copied were locked by another user");
        }
    }

    public String getPath(StorageObject object){
        StringBuilder sb = new StringBuilder();
        User owner = object.getUser();
        if(!owner.equals(activeSession.getUser())){
            sb.append("shares/")
                    .append(owner.getLoginName())
                    .append("/");
        }
        sb.append(object.getPath(false));
        return sb.toString();
    }

    public String getPreviousPathName(String path) {
        return PathHelpers.getPreviousPathName(path);
    }

    public Part getFile() {
        return file;
    }

    public void setFile(Part file) {
        this.file = file;
    }

    public void uploadFile(String path) {
        if(file == null){
            addError("No file has been chosen");
            return;
        }
        String fullPath = (path.endsWith("/") ? path : path + "/") + file.getSubmittedFileName();
        try(InputStream stream = file.getInputStream()){
            this.userStorage.createFile(activeSession.getUser(), fullPath);
            StorageObject object = userStorage.getStorageObject(activeSession.getUser(), fullPath, false);
            fileManager.setContent(object, stream.readAllBytes());
        }
        catch(IOException ex){
            addError("Could not add the specified file");
        }
    }
}
