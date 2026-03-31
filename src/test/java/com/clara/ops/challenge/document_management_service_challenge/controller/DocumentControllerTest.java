package com.clara.ops.challenge.document_management_service_challenge.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentDownloadUrl;
import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentResponse;
import com.clara.ops.challenge.document_management_service_challenge.dto.Metadata;
import com.clara.ops.challenge.document_management_service_challenge.dto.PaginatedDocumentSearch;
import com.clara.ops.challenge.document_management_service_challenge.exception.DocumentNotFoundException;
import com.clara.ops.challenge.document_management_service_challenge.exception.GlobalExceptionHandler;
import com.clara.ops.challenge.document_management_service_challenge.service.DocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DocumentController.class)
@Import(GlobalExceptionHandler.class)
class DocumentControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockBean private DocumentService documentService;

  @Test
  void upload_shouldReturn201_whenRequestIsValid() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "test.pdf", "application/pdf", "pdf-data".getBytes());

    mockMvc
        .perform(
            multipart("/document-management/upload")
                .file(file)
                .param("user", "alice")
                .param("name", "test.pdf")
                .param("tags", "tag1", "tag2"))
        .andExpect(status().isCreated());

    verify(documentService).upload(eq("alice"), eq("test.pdf"), eq(List.of("tag1", "tag2")), any());
  }

  @Test
  void upload_shouldReturn400_whenUserIsMissing() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "test.pdf", "application/pdf", "pdf-data".getBytes());

    mockMvc
        .perform(
            multipart("/document-management/upload")
                .file(file)
                .param("name", "test.pdf")
                .param("tags", "tag1"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void upload_shouldReturn400_whenFileIsMissing() throws Exception {
    mockMvc
        .perform(
            multipart("/document-management/upload")
                .param("user", "alice")
                .param("name", "test.pdf")
                .param("tags", "tag1"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void search_shouldReturn200_withPaginatedResults() throws Exception {
    PaginatedDocumentSearch response =
        PaginatedDocumentSearch.builder()
            .metadata(
                Metadata.builder()
                    .currentPage(0)
                    .itemsPerPage(20)
                    .currentItems(1)
                    .totalPages(1)
                    .totalItems(1)
                    .build())
            .documents(
                List.of(
                    DocumentResponse.builder()
                        .id(UUID.randomUUID().toString())
                        .user("alice")
                        .name("report.pdf")
                        .tags(List.of("finance"))
                        .size(1024L)
                        .type("application/pdf")
                        .createdAt("2024-01-01T00:00:00Z")
                        .build()))
            .build();

    when(documentService.search(any(), eq(0), eq(20))).thenReturn(response);

    mockMvc
        .perform(
            post("/document-management/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.metadata.totalItems").value(1))
        .andExpect(jsonPath("$.documents[0].user").value("alice"));
  }

  @Test
  void search_shouldReturn200_withFilters() throws Exception {
    when(documentService.search(any(), anyInt(), anyInt()))
        .thenReturn(
            PaginatedDocumentSearch.builder()
                .metadata(
                    Metadata.builder()
                        .currentPage(0)
                        .itemsPerPage(20)
                        .currentItems(0)
                        .totalPages(0)
                        .totalItems(0)
                        .build())
                .documents(List.of())
                .build());

    String body =
        objectMapper.writeValueAsString(Map.of("user", "alice", "tags", List.of("finance")));

    mockMvc
        .perform(
            post("/document-management/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.documents").isArray());
  }

  @Test
  void download_shouldReturn200_withPresignedUrl() throws Exception {
    String docId = UUID.randomUUID().toString();
    when(documentService.download(docId))
        .thenReturn(new DocumentDownloadUrl("https://minio/presigned"));

    mockMvc
        .perform(get("/document-management/download/{id}", docId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.url").value("https://minio/presigned"));
  }

  @Test
  void download_shouldReturn404_whenDocumentNotFound() throws Exception {
    String docId = UUID.randomUUID().toString();
    when(documentService.download(docId)).thenThrow(new DocumentNotFoundException(docId));

    mockMvc
        .perform(get("/document-management/download/{id}", docId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }
}
