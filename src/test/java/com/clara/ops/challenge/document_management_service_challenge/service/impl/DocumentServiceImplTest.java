package com.clara.ops.challenge.document_management_service_challenge.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.clara.ops.challenge.document_management_service_challenge.config.MinioProperties;
import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentDownloadUrl;
import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentSearchFilters;
import com.clara.ops.challenge.document_management_service_challenge.dto.PaginatedDocumentSearch;
import com.clara.ops.challenge.document_management_service_challenge.entity.Document;
import com.clara.ops.challenge.document_management_service_challenge.exception.DocumentNotFoundException;
import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentRepository;
import com.clara.ops.challenge.document_management_service_challenge.service.StorageService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

  @Mock private DocumentRepository documentRepository;
  @Mock private StorageService storageService;
  @Mock private MinioProperties minioProperties;

  @InjectMocks private DocumentServiceImpl documentService;

  @BeforeEach
  void setUp() {
    when(minioProperties.getBucketName()).thenReturn("document-bucket");
  }

  @Test
  void upload_shouldUploadToStorageAndPersistMetadata() throws IOException {
    MockMultipartFile file =
        new MockMultipartFile(
            "file", "test.pdf", "application/pdf", "pdf-content".getBytes());
    when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));

    documentService.upload("alice", "test.pdf", List.of("tag1", "tag2"), file);

    verify(storageService)
        .uploadFile(
            eq("document-bucket"),
            eq("alice/test.pdf"),
            any(InputStream.class),
            eq(file.getSize()),
            eq("application/pdf"));
    verify(documentRepository).save(any(Document.class));
  }

  @Test
  void upload_shouldPersistCorrectMetadata() {
    MockMultipartFile file =
        new MockMultipartFile(
            "file", "report.pdf", "application/pdf", new byte[1024]);
    ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
    when(documentRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

    documentService.upload("bob", "report.pdf", List.of("finance"), file);

    Document saved = captor.getValue();
    assertThat(saved.getUserName()).isEqualTo("bob");
    assertThat(saved.getDocumentName()).isEqualTo("report.pdf");
    assertThat(saved.getFileSize()).isEqualTo(1024L);
    assertThat(saved.getTags()).hasSize(1);
    assertThat(saved.getTags().get(0).getTag()).isEqualTo("finance");
  }

  @Test
  void upload_shouldThrowStorageException_whenStorageFails() {
    MockMultipartFile file =
        new MockMultipartFile("file", "test.pdf", "application/pdf", "data".getBytes());
    doThrow(new com.clara.ops.challenge.document_management_service_challenge.exception.StorageException("upload failed", new RuntimeException()))
        .when(storageService)
        .uploadFile(any(), any(), any(), anyLong(), any());

    assertThatThrownBy(() -> documentService.upload("alice", "test.pdf", List.of(), file))
        .isInstanceOf(
            com.clara.ops.challenge.document_management_service_challenge.exception.StorageException.class);
    verify(documentRepository, never()).save(any());
  }

  @Test
  @SuppressWarnings("unchecked")
  void search_shouldReturnPaginatedResults() {
    Document doc = buildDocument("alice", "report.pdf");
    when(documentRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(doc)));

    DocumentSearchFilters filters = new DocumentSearchFilters();
    PaginatedDocumentSearch result = documentService.search(filters, 0, 20);

    assertThat(result.getDocuments()).hasSize(1);
    assertThat(result.getDocuments().get(0).getUser()).isEqualTo("alice");
    assertThat(result.getMetadata().getCurrentPage()).isZero();
    assertThat(result.getMetadata().getTotalItems()).isEqualTo(1);
  }

  @Test
  @SuppressWarnings("unchecked")
  void search_shouldReturnEmptyResults_whenNoDocumentsFound() {
    when(documentRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    PaginatedDocumentSearch result =
        documentService.search(new DocumentSearchFilters(), 0, 20);

    assertThat(result.getDocuments()).isEmpty();
    assertThat(result.getMetadata().getTotalItems()).isZero();
  }

  @Test
  void download_shouldReturnPresignedUrl() {
    Document doc = buildDocument("alice", "report.pdf");
    when(documentRepository.findById(doc.getId())).thenReturn(Optional.of(doc));
    when(storageService.generatePresignedUrl("document-bucket", "alice/report.pdf"))
        .thenReturn("https://minio/presigned-url");

    DocumentDownloadUrl result = documentService.download(doc.getId().toString());

    assertThat(result.getUrl()).isEqualTo("https://minio/presigned-url");
  }

  @Test
  void download_shouldThrowDocumentNotFoundException_whenDocumentDoesNotExist() {
    UUID id = UUID.randomUUID();
    when(documentRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> documentService.download(id.toString()))
        .isInstanceOf(DocumentNotFoundException.class);
  }

  @Test
  void download_shouldThrowDocumentNotFoundException_whenIdIsInvalidUuid() {
    assertThatThrownBy(() -> documentService.download("not-a-uuid"))
        .isInstanceOf(DocumentNotFoundException.class);
  }

  private Document buildDocument(String user, String name) {
    Document doc = new Document();
    doc.setUserName(user);
    doc.setDocumentName(name);
    doc.setMinioPath("document-bucket/" + user + "/" + name);
    doc.setFileSize(1024L);
    doc.setFileType("application/pdf");
    doc.setCreatedAt(OffsetDateTime.now());
    // Set id via reflection to avoid needing @GeneratedValue in tests
    try {
      var field = Document.class.getDeclaredField("id");
      field.setAccessible(true);
      field.set(doc, UUID.randomUUID());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return doc;
  }
}
