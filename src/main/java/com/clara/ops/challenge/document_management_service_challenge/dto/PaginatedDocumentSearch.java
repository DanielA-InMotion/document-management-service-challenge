package com.clara.ops.challenge.document_management_service_challenge.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaginatedDocumentSearch {

  private Metadata metadata;
  private List<DocumentResponse> documents;
}
