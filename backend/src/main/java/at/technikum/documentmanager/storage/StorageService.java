package at.technikum.documentmanager.storage;

import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

public interface StorageService {
    void store(InputStream in, long size, String contentType, String objectName) throws Exception;
    Optional<InputStream> load(String objectName) throws Exception;               // stream read (backend usage)
    void delete(String objectName) throws Exception;
    boolean exists(String objectName) throws Exception;
}
