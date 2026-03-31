package com.clara.ops.challenge.document_management_service_challenge.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.clara.ops.challenge.document_management_service_challenge.config.MinioProperties;
import com.clara.ops.challenge.document_management_service_challenge.exception.StorageException;
import io.minio.*;
import io.minio.http.Method;
import java.io.ByteArrayInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MinioStorageServiceImplTest {

  @Mock private MinioClient minioClient;
  @Mock private MinioProperties minioProperties;

  @InjectMocks private MinioStorageServiceImpl minioStorageService;

  @BeforeEach
  void setUp() {
    when(minioProperties.getBucketName()).thenReturn("document-bucket");
    when(minioProperties.getPresignedUrlExpiryMinutes()).thenReturn(60);
  }

  @Test
  void uploadFile_shouldCallPutObjectWithCorrectArgs() throws Exception {
    ByteArrayInputStream inputStream = new ByteArrayInputStream("pdf-data".getBytes());

    minioStorageService.uploadFile(
        "document-bucket", "alice/test.pdf", inputStream, 8L, "application/pdf");

    verify(minioClient).putObject(any(PutObjectArgs.class));
  }

  @Test
  void uploadFile_shouldThrowStorageException_whenMinioFails() throws Exception {
    doThrow(new RuntimeException("connection refused"))
        .when(minioClient)
        .putObject(any(PutObjectArgs.class));

    assertThatThrownBy(
            () ->
                minioStorageService.uploadFile(
                    "document-bucket",
                    "alice/test.pdf",
                    new ByteArrayInputStream(new byte[0]),
                    0L,
                    "application/pdf"))
        .isInstanceOf(StorageException.class)
        .hasMessageContaining("Failed to upload file");
  }

  @Test
  void generatePresignedUrl_shouldReturnUrl() throws Exception {
    when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
        .thenReturn("https://minio/presigned-url");

    String url = minioStorageService.generatePresignedUrl("document-bucket", "alice/test.pdf");

    assertThat(url).isEqualTo("https://minio/presigned-url");
  }

  @Test
  void generatePresignedUrl_shouldThrowStorageException_whenMinioFails() throws Exception {
    when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
        .thenThrow(new RuntimeException("MinIO unavailable"));

    assertThatThrownBy(
            () -> minioStorageService.generatePresignedUrl("document-bucket", "alice/test.pdf"))
        .isInstanceOf(StorageException.class)
        .hasMessageContaining("Failed to generate presigned URL");
  }

  @Test
  void ensureBucketExists_shouldCreateBucket_whenItDoesNotExist() throws Exception {
    when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

    minioStorageService.ensureBucketExists();

    verify(minioClient).makeBucket(any(MakeBucketArgs.class));
  }

  @Test
  void ensureBucketExists_shouldNotCreateBucket_whenItAlreadyExists() throws Exception {
    when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

    minioStorageService.ensureBucketExists();

    verify(minioClient, never()).makeBucket(any(MakeBucketArgs.class));
  }
}
