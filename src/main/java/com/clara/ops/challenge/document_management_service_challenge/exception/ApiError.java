package com.clara.ops.challenge.document_management_service_challenge.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

  private int status;
  private String error;
  private String message;
  private OffsetDateTime timestamp;
  private List<String> details;
}
