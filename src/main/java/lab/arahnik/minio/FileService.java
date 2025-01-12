package lab.arahnik.minio;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
@PropertySource("classpath:application.properties")
public class FileService {

    private final MinioClient minioClient;
    @Value("${minio.bucket-name}") private String minioBucket;

    public void uploadFile(String objectName, InputStream fileStream, String contentType) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(minioBucket)
                        .object(objectName)
                        .stream(fileStream, fileStream.available(), -1)
                        .contentType(contentType)
                        .build()
        );
    }

    public InputStream downloadFile(String objectName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(minioBucket)
                        .object(objectName)
                        .build()
        );
    }

    public void deleteFile(String objectName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(minioBucket)
                        .object(objectName)
                        .build()
        );
    }

}
