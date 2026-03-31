package com.clara.ops.challenge.document_management_service_challenge;

import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class DocumentManagementServiceChallengeApplicationTests {

  @MockBean MinioClient minioClient;

  @Test
  void contextLoads() {}
}
