package com.clara.ops.challenge.document_management_service_challenge.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
public class Document {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_name", nullable = false)
  private String userName;

  @Column(name = "document_name", nullable = false)
  private String documentName;

  @Column(name = "minio_path", nullable = false)
  private String minioPath;

  @Column(name = "file_size", nullable = false)
  private Long fileSize;

  @Column(name = "file_type", nullable = false)
  private String fileType;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @OneToMany(
      mappedBy = "document",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<DocumentTag> tags = new ArrayList<>();

  @PrePersist
  protected void onCreate() {
    createdAt = OffsetDateTime.now();
  }

  public void addTag(String tag) {
    DocumentTag documentTag = new DocumentTag();
    documentTag.setDocument(this);
    documentTag.setTag(tag);
    tags.add(documentTag);
  }
}
