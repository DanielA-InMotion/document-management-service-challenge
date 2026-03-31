package com.clara.ops.challenge.document_management_service_challenge.controller;

import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentDownloadUrl;
import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentSearchFilters;
import com.clara.ops.challenge.document_management_service_challenge.dto.PaginatedDocumentSearch;
import com.clara.ops.challenge.document_management_service_challenge.service.DocumentService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/document-management")
@RequiredArgsConstructor
@Validated
public class DocumentController {

  private final DocumentService documentService;

  /**
   * Uploads a PDF document with its metadata. Accepts multipart/form-data so the binary file can
   * be streamed directly to MinIO without buffering it in the 50MB JVM heap.
   */
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public void uploadDocument(
      @RequestParam("user") @NotBlank String user,
      @RequestParam("name") @NotBlank String name,
      @RequestParam("tags") @NotEmpty List<String> tags,
      @RequestPart("file") MultipartFile file) {
    documentService.upload(user, name, tags, file);
  }

  @PostMapping("/search")
  public ResponseEntity<PaginatedDocumentSearch> searchDocuments(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) int size,
      @RequestBody DocumentSearchFilters filters) {
    return ResponseEntity.ok(documentService.search(filters, page, size));
  }

  @GetMapping("/download/{documentId}")
  public ResponseEntity<DocumentDownloadUrl> downloadDocument(
      @PathVariable("documentId") String documentId) {
    return ResponseEntity.ok(documentService.download(documentId));
  }
}
