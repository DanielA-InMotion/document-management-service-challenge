package com.clara.ops.challenge.document_management_service_challenge.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Metadata {

  private int currentPage;
  private int itemsPerPage;
  private int currentItems;
  private int totalPages;
  private long totalItems;
}
