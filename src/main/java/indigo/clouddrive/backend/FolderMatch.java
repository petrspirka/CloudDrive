package indigo.clouddrive.backend;

import indigo.clouddrive.backend.models.StorageObject;

public record FolderMatch(String oldPath, StorageObject storageObject) {
}
