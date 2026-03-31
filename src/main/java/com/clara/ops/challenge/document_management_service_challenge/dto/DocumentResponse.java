package com.clara.ops.challenge.document_management_service_challenge.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentResponse {

  private String id;
  private String user;
  private String name;
  private List<String> tags;
  private Long size;
  private String type;
  private String createdAt;
}
