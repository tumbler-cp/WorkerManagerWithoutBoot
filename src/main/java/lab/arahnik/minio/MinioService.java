package lab.arahnik.minio;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioService {

  private final MinioClient minioClient;

  public void upload(String bucketName, String objectName, byte[] content, String contentType) throws Exception {
    if (!bucketExists(bucketName)) {
      minioClient.makeBucket(
              MakeBucketArgs.builder()
                      .bucket(bucketName)
                      .build()
      );
    }
    ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
    minioClient.putObject(PutObjectArgs.builder()
            .bucket(bucketName)
            .object(objectName)
            .stream(inputStream, content.length, -1)
            .contentType(contentType)
            .build());
    boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
            .bucket(bucketName)
            .build());
    System.out.println("Bucket exists: " + bucketExists);
  }

  public InputStream getFile(String bucketName, String objectName) throws Exception {
    return minioClient.getObject(
            GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
    );
  }

  public void rollbackSaveFile(String objectName, String bucketName) throws Exception {
    try {
      minioClient.removeObject(
              RemoveObjectArgs.builder()
                      .bucket(bucketName)
                      .object(objectName)
                      .build()
      );
    } catch (Exception e) {
      System.out.println("Rollback failed: " + e.getMessage());
    }
  }

  public boolean bucketExists(String bucketName) throws Exception {
    return minioClient.bucketExists(BucketExistsArgs.builder()
            .bucket(bucketName)
            .build());
  }

  public String saveFile(String bucketName, String username, MultipartFile file) throws Exception {
    String extension = FilenameUtils.getExtension(file.getOriginalFilename());
    String newFileName = username + "_" + UUID.randomUUID() + "." + (extension == null || extension.isEmpty() ? "csv" : extension);

    byte[] content = file.getBytes();
    String contentType = file.getContentType();

    upload(bucketName, newFileName, content, contentType == null ? "application/octet-stream" : contentType);
    return newFileName;
  }

}
