package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentDownloadUrl;
import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentSearchFilters;
import com.clara.ops.challenge.document_management_service_challenge.dto.PaginatedDocumentSearch;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentService {

  void upload(String user, String name, List<String> tags, MultipartFile file);

  PaginatedDocumentSearch search(DocumentSearchFilters filters, int page, int size);

  DocumentDownloadUrl download(String documentId);
}
