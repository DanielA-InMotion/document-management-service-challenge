CREATE SCHEMA IF NOT EXISTS document_schema;

SET search_path TO document_schema;

CREATE TABLE IF NOT EXISTS documents (
    id          UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    user_name   VARCHAR(255)    NOT NULL,
    document_name VARCHAR(255)  NOT NULL,
    minio_path  VARCHAR(1000)   NOT NULL,
    file_size   BIGINT          NOT NULL,
    file_type   VARCHAR(100)    NOT NULL,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS document_tags (
    id          UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    document_id UUID            NOT NULL REFERENCES documents (id) ON DELETE CASCADE,
    tag         VARCHAR(255)    NOT NULL
);

-- Indices to support common query patterns
CREATE INDEX IF NOT EXISTS idx_documents_user_name     ON documents (user_name);
CREATE INDEX IF NOT EXISTS idx_documents_document_name ON documents (document_name);
CREATE INDEX IF NOT EXISTS idx_documents_created_at    ON documents (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_document_tags_document  ON document_tags (document_id);
CREATE INDEX IF NOT EXISTS idx_document_tags_tag       ON document_tags (tag);
