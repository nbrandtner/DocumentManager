package at.technikum.documentmanager.storage;

import io.minio.*;
import io.minio.errors.MinioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Optional;

@Service
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;
    private final String bucket;

    public MinioStorageService(
            MinioClient minioClient,
            @Value("${minio.bucket}") String bucket
    ) {
        this.minioClient = minioClient;
        this.bucket = bucket;
    }

    @Override
    public void store(InputStream in, long size, String contentType, String objectName) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .contentType(contentType != null ? contentType : "application/octet-stream")
                        .stream(in, size, -1)
                        .build()
        );
    }

    @Override
    public Optional<InputStream> load(String objectName) throws Exception {
        try {
            GetObjectResponse res = minioClient.getObject(
                    GetObjectArgs.builder().bucket(bucket).object(objectName).build()
            );
            return Optional.of(res);
        } catch (MinioException e) {
            return Optional.empty();
        }
    }

    @Override
    public void delete(String objectName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder().bucket(bucket).object(objectName).build()
        );
    }

    @Override
    public boolean exists(String objectName) throws Exception {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucket).object(objectName).build()
            );
            return true;
        } catch (MinioException e) {
            return false;
        }
    }
}
