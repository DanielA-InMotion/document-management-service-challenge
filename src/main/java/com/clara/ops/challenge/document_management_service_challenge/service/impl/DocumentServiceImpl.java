package com.clara.ops.challenge.document_management_service_challenge.service.impl;

import com.clara.ops.challenge.document_management_service_challenge.config.MinioProperties;
import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentDownloadUrl;
import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentResponse;
import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentSearchFilters;
import com.clara.ops.challenge.document_management_service_challenge.dto.Metadata;
import com.clara.ops.challenge.document_management_service_challenge.dto.PaginatedDocumentSearch;
import com.clara.ops.challenge.document_management_service_challenge.entity.Document;
import com.clara.ops.challenge.document_management_service_challenge.exception.DocumentNotFoundException;
import com.clara.ops.challenge.document_management_service_challenge.exception.StorageException;
import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentRepository;
import com.clara.ops.challenge.document_management_service_challenge.service.DocumentService;
import com.clara.ops.challenge.document_management_service_challenge.service.StorageService;
import com.clara.ops.challenge.document_management_service_challenge.specification.DocumentSpecification;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

  private static final String PDF_CONTENT_TYPE = "application/pdf";

  private final DocumentRepository documentRepository;
  private final StorageService storageService;
  private final MinioProperties minioProperties;

  @Override
  @Transactional
  public void upload(String user, String name, List<String> tags, MultipartFile file) {
    String contentType =
        file.getContentType() != null ? file.getContentType() : PDF_CONTENT_TYPE;
    String objectPath = user + "/" + name;

    try {
      storageService.uploadFile(
          minioProperties.getBucketName(),
          objectPath,
          file.getInputStream(),
          file.getSize(),
          contentType);
    } catch (IOException ex) {
      throw new StorageException("Failed to read uploaded file", ex);
    }

    Document document = new Document();
    document.setUserName(user);
    document.setDocumentName(name);
    document.setMinioPath(minioProperties.getBucketName() + "/" + objectPath);
    document.setFileSize(file.getSize());
    document.setFileType(contentType);
    if (tags != null) {
      tags.forEach(document::addTag);
    }

    documentRepository.save(document);
    log.info("Document uploaded: user={}, name={}", user, name);
  }

  @Override
  @Transactional(readOnly = true)
  public PaginatedDocumentSearch search(DocumentSearchFilters filters, int page, int size) {
    Pageable pageable =
        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

    Page<Document> documentPage =
        documentRepository.findAll(DocumentSpecification.withFilters(filters), pageable);

    List<DocumentResponse> responses =
        documentPage.getContent().stream().map(this::toResponse).collect(Collectors.toList());

    Metadata metadata =
        Metadata.builder()
            .currentPage(documentPage.getNumber())
            .itemsPerPage(documentPage.getSize())
            .currentItems(documentPage.getNumberOfElements())
            .totalPages(documentPage.getTotalPages())
            .totalItems(documentPage.getTotalElements())
            .build();

    return PaginatedDocumentSearch.builder().metadata(metadata).documents(responses).build();
  }

  @Override
  @Transactional(readOnly = true)
  public DocumentDownloadUrl download(String documentId) {
    UUID id;
    try {
      id = UUID.fromString(documentId);
    } catch (IllegalArgumentException ex) {
      throw new DocumentNotFoundException(documentId);
    }

    Document document =
        documentRepository.findById(id).orElseThrow(() -> new DocumentNotFoundException(documentId));

    String objectPath = document.getUserName() + "/" + document.getDocumentName();
    String url = storageService.generatePresignedUrl(minioProperties.getBucketName(), objectPath);
    return new DocumentDownloadUrl(url);
  }

  private DocumentResponse toResponse(Document document) {
    List<String> tagValues =
        document.getTags().stream()
            .map(tag -> tag.getTag())
            .collect(Collectors.toList());

    return DocumentResponse.builder()
        .id(document.getId().toString())
        .user(document.getUserName())
        .name(document.getDocumentName())
        .tags(tagValues)
        .size(document.getFileSize())
        .type(document.getFileType())
        .createdAt(document.getCreatedAt().toString())
        .build();
  }
}
