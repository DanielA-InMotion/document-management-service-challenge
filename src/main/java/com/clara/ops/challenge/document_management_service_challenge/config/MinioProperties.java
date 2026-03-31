package com.clara.ops.challenge.document_management_service_challenge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

  private String endpoint;
  private String publicEndpoint;
  private String accessKey;
  private String secretKey;
  private String bucketName;
  private int presignedUrlExpiryMinutes;
}
