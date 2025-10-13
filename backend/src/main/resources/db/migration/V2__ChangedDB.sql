CREATE TABLE documents
(
    id                UUID                        NOT NULL,
    original_filename VARCHAR(255)                NOT NULL,
    content_type      VARCHAR(100)                NOT NULL,
    size              BIGINT                      NOT NULL,
    uploaded_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_documents PRIMARY KEY (id)
);