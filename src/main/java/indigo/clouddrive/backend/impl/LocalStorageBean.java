package indigo.clouddrive.backend.impl;

import indigo.clouddrive.backend.contracts.Storage;
import jakarta.ejb.Stateless;

import java.io.*;
import java.nio.file.*;

@Stateless
public class LocalStorageBean implements Storage {
    //Directory where the application should store the data
    private final String BASE_PATH = System.getProperty("user.home") + "/" + "cloud-drive";

    private Path getFullPath(Path path){
        return Path.of(BASE_PATH, path.toString());
    }

    private boolean existsFullPath(Path fullPath){
        return Files.exists(fullPath);
    }

    @Override
    public RandomAccessFile openFile(Path path) throws IOException {
        Path fullPath = getFullPath(path);
        if(!existsFullPath(fullPath)){
            throw new FileNotFoundException("The specified file could not be found");
        }
        return new RandomAccessFile(fullPath.toFile(), "rw");
    }

    @Override
    public void createFile(Path path) throws IOException {
        Path fullPath = getFullPath(path);
        if(existsFullPath(fullPath)){
            throw new FileAlreadyExistsException("The specified file already exists");
        }
        if(!existsFullPath(fullPath.getParent())){
            if(!path.toFile().mkdirs()){
                throw new IOException("Could not create directories for this file");
            }
        }
        Files.createFile(fullPath);
    }

    @Override
    public boolean exists(Path path) {
        return Files.exists(getFullPath(path));
    }

    @Override
    public void deleteFile(Path path) throws IOException {
        Path fullPath = getFullPath(path);
        if(!existsFullPath(fullPath)){
            throw new FileNotFoundException("The specified file could not be found");
        }
        Files.delete(fullPath);
    }

    @Override
    public File[] getPathContents(Path path) throws IOException {
        Path fullPath = getFullPath(path);
        if(!existsFullPath(fullPath)){
            throw new FileNotFoundException("The specified path could not be found");
        }
        return fullPath.toFile().listFiles();
    }

    @Override
    public void renameFile(Path path, Path newPath) throws IOException {
        Path fullPath = getFullPath(path);
        if(!existsFullPath(fullPath)){
            throw new FileNotFoundException("The specified file could not be found");
        }
        Files.move(fullPath, getFullPath(newPath), StandardCopyOption.ATOMIC_MOVE);
    }
}
