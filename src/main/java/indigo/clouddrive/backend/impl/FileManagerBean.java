package indigo.clouddrive.backend.impl;

import indigo.clouddrive.backend.contracts.ContentEncrypter;
import indigo.clouddrive.backend.contracts.FileManager;
import indigo.clouddrive.backend.contracts.Storage;
import indigo.clouddrive.backend.exceptions.EncryptionException;
import indigo.clouddrive.backend.exceptions.LockAlreadyAcquiredException;
import indigo.clouddrive.backend.models.StorageObject;
import indigo.clouddrive.frontend.helpers.PathHelpers;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SessionScoped
public class FileManagerBean implements FileManager, Serializable {
    private FileLock fileLock;
    private FileChannel channel;
    private RandomAccessFile file;
    private StorageObject storageObject;

    @EJB
    private ContentEncrypter contentEncrypter;

    @EJB
    private Storage storage;

    @Override
    public void write(byte[] content) throws IOException {
        if(!fileLock.isValid() && !reacquireLock()) {
            throw new IOException("Could not save the file");
        }
        //Empties the file before writing
        channel.truncate(0);
        try {
            channel.write(
                ByteBuffer.wrap(
                        contentEncrypter.encrypt(content, storageObject.getSalt())
                )
            );
        } catch (EncryptionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] read() throws IOException {
        if(!fileLock.isValid() && reacquireLock()){
            throw new IOException("Could not read the file");
        }
        byte[] data = new byte[(int)channel.size()];
        channel.read(ByteBuffer.wrap(data));
        try {
            return contentEncrypter.decrypt(data, storageObject.getSalt());
        } catch (EncryptionException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public byte[] getContent(StorageObject object) throws IOException {
        //TODO: DRY, potentially fix later
        if(object.isFolder()){
            throw new FileNotFoundException("The specified path is a directory");
        }
        String hashedPath = object.getHashedPath();
        try(RandomAccessFile file = storage.openFile(Path.of(hashedPath));
            FileChannel channel = file.getChannel();
            FileLock lock = channel.tryLock()){
            if(lock == null){
                return null;
            }
            byte[] data = new byte[(int)file.length()];
            channel.read(ByteBuffer.wrap(data));
            return contentEncrypter.decrypt(data, object.getSalt());
        }
        catch(EncryptionException ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean setContent(StorageObject object, byte[] content) throws IOException {
        //TODO: DRY, potentially fix later
        if(object.isFolder()){
            throw new FileNotFoundException("The specified path is a directory");
        }
        String hashedPath = object.getHashedPath();
        Path path = Path.of(hashedPath);
        if(!storage.exists(path)){
            if(content == null){
                return true;
            }
            storage.createFile(path);
        }
        else if(content == null){
            storage.deleteFile(path);
            return true;
        }
        try(RandomAccessFile file = storage.openFile(path);
            FileChannel channel = file.getChannel();
            FileLock lock = channel.tryLock()){
            if(lock == null){
                return false;
            }
            byte[] data = contentEncrypter.encrypt(content, object.getSalt());
            channel.write(ByteBuffer.wrap(data));
            return true;
        }
        catch(EncryptionException ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void lockFile(StorageObject object) throws IOException {
        releaseData();
        if(object.isFolder()){
            throw new FileNotFoundException("The specified path is a directory");
        }
        storageObject = object;
        file = storage.openFile(Path.of(object.getHashedPath()));
        if(file == null){
            throw new IOException("The specified file could not be found or created");
        }
        if(!reacquireLock()){
            file.close();
            throw new LockAlreadyAcquiredException("Could not lock the specified file");
        }
    }

    @Override
    public byte[] compressStorageObject(StorageObject object, String pathPrefixToRemove) throws IOException {
        byte[] data;
        try(ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ZipOutputStream stream = new ZipOutputStream(byteStream)) {
            packageStorageObject(stream, pathPrefixToRemove, object);
            stream.close();
            data = byteStream.toByteArray();
        }
        catch (EncryptionException e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    private void packageStorageObject(ZipOutputStream stream, String pathPrefixToRemove, StorageObject object) throws IOException, EncryptionException {
        if(!object.isFolder()){
            ZipEntry entry = new ZipEntry(PathHelpers.getTrimmedPath(PathHelpers.getSubPathWithoutStart(object.getPath(false), pathPrefixToRemove)));
            stream.putNextEntry(entry);
            stream.write(getContent(object));
            stream.closeEntry();
        }
        else {
            for(StorageObject child : object.getChildren()){
                packageStorageObject(stream, pathPrefixToRemove, child);
            }
        }
    }

    public void close() {
        releaseData();
    }

    @PreDestroy
    public void onDestroy() {
        close();
    }

    private void releaseData(){
        if(file == null){
            return;
        }
        if(fileLock != null && fileLock.isValid()) {
            try {
                fileLock.release();
                fileLock.close();
            } catch (IOException ignored) {}
        }
        if(channel != null) {
            try {
                channel.close();
            } catch (IOException ignored) {
            }
        }
        try {
            file.close();
        } catch (IOException ignored) {
        }
    }

    private boolean reacquireLock(){
        if(fileLock != null && fileLock.isValid()){
            return true;
        }
        else if(channel != null && channel.isOpen()){
            if(fileLock != null) {
                try {
                    fileLock.close();
                } catch (IOException ignored) {}
            }
            try {
                fileLock = channel.tryLock();
                return fileLock.isValid();
            }
            catch(OverlappingFileLockException | IOException ex){
                return false;
            }
        }
        else {
            if(reacquireChannel()){
                return reacquireLock();
            }
            return false;
        }
    }

    private boolean reacquireChannel(){
        try {
            if(channel != null) {
                channel.close();
            }
            channel = file.getChannel();
            return channel.isOpen();
        }
        catch(IOException ex){
            return false;
        }
    }
}
