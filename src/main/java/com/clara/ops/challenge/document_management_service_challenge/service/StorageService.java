package com.clara.ops.challenge.document_management_service_challenge.service;

import java.io.InputStream;

public interface StorageService {

  void uploadFile(
      String bucketName, String objectPath, InputStream inputStream, long fileSize, String contentType);

  String generatePresignedUrl(String bucketName, String objectPath);
}
