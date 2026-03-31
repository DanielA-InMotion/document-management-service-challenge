package com.clara.ops.challenge.document_management_service_challenge.specification;

import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentSearchFilters;
import com.clara.ops.challenge.document_management_service_challenge.entity.Document;
import com.clara.ops.challenge.document_management_service_challenge.entity.DocumentTag;
import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class DocumentSpecification {

  private DocumentSpecification() {}

  public static Specification<Document> withFilters(DocumentSearchFilters filters) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (StringUtils.hasText(filters.getUser())) {
        predicates.add(cb.equal(cb.lower(root.get("userName")), filters.getUser().toLowerCase()));
      }

      if (StringUtils.hasText(filters.getName())) {
        predicates.add(
            cb.like(
                cb.lower(root.get("documentName")),
                "%" + filters.getName().toLowerCase() + "%"));
      }

      if (filters.getTags() != null && !filters.getTags().isEmpty()) {
        Subquery<UUID> tagSubquery = query.subquery(UUID.class);
        Root<DocumentTag> tagRoot = tagSubquery.from(DocumentTag.class);
        tagSubquery
            .select(tagRoot.get("document").get("id"))
            .where(
                cb.and(
                    tagRoot.get("tag").in(filters.getTags()),
                    cb.equal(tagRoot.get("document"), root)));
        predicates.add(cb.exists(tagSubquery));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
