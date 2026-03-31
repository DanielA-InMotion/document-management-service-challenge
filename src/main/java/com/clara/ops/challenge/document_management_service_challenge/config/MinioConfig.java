package com.clara.ops.challenge.document_management_service_challenge.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

  @Bean
  public MinioClient minioClient(MinioProperties properties) {
    return MinioClient.builder()
        .endpoint(properties.getEndpoint())
        .credentials(properties.getAccessKey(), properties.getSecretKey())
        .build();
  }

  @Bean
  @Qualifier("presignedMinioClient")
  public MinioClient presignedMinioClient(MinioProperties properties) {
    String endpoint =
        (properties.getPublicEndpoint() != null && !properties.getPublicEndpoint().isBlank())
            ? properties.getPublicEndpoint()
            : properties.getEndpoint();
    return MinioClient.builder()
        .endpoint(endpoint)
        .credentials(properties.getAccessKey(), properties.getSecretKey())
        .region("us-east-1")
        .build();
  }
}
