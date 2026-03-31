package com.clara.ops.challenge.document_management_service_challenge.service.impl;

import com.clara.ops.challenge.document_management_service_challenge.config.MinioProperties;
import com.clara.ops.challenge.document_management_service_challenge.exception.StorageException;
import com.clara.ops.challenge.document_management_service_challenge.service.StorageService;
import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MinioStorageServiceImpl implements StorageService {

  private final MinioClient minioClient;
  private final MinioClient presignedMinioClient;
  private final MinioProperties minioProperties;

  public MinioStorageServiceImpl(
      MinioClient minioClient,
      @Qualifier("presignedMinioClient") MinioClient presignedMinioClient,
      MinioProperties minioProperties) {
    this.minioClient = minioClient;
    this.presignedMinioClient = presignedMinioClient;
    this.minioProperties = minioProperties;
  }

  @PostConstruct
  public void ensureBucketExists() {
    String bucketName = minioProperties.getBucketName();
    try {
      boolean exists =
          minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
      if (!exists) {
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        log.info("Created MinIO bucket: {}", bucketName);
      }
    } catch (Exception ex) {
      throw new StorageException("Failed to initialize MinIO bucket: " + bucketName, ex);
    }
  }

  @Override
  public void uploadFile(
      String bucketName,
      String objectPath,
      InputStream inputStream,
      long fileSize,
      String contentType) {
    try {
      // Stream directly to MinIO — never loads the full file into JVM heap.
      // When fileSize is known (disk-spooled multipart), MinIO uses simple PUT or
      // server-side multipart automatically; partSize -1 means auto-calculate.
      // When fileSize is unknown (-1), we fall back to a 10MB part size so MinIO
      // can still use its internal multipart upload.
      long partSize = fileSize > 0 ? -1 : 10 * 1024 * 1024L;
      long objectSize = fileSize > 0 ? fileSize : -1;

      minioClient.putObject(
          PutObjectArgs.builder()
              .bucket(bucketName)
              .object(objectPath)
              .stream(inputStream, objectSize, partSize)
              .contentType(contentType)
              .build());
    } catch (Exception ex) {
      throw new StorageException("Failed to upload file to MinIO: " + objectPath, ex);
    }
  }

  @Override
  public String generatePresignedUrl(String bucketName, String objectPath) {
    try {
      return presignedMinioClient.getPresignedObjectUrl(
          GetPresignedObjectUrlArgs.builder()
              .method(Method.GET)
              .bucket(bucketName)
              .object(objectPath)
              .expiry(minioProperties.getPresignedUrlExpiryMinutes(), TimeUnit.MINUTES)
              .build());
    } catch (Exception ex) {
      throw new StorageException("Failed to generate presigned URL for: " + objectPath, ex);
    }
  }
}
