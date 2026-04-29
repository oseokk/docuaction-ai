# ERD

This ERD represents the current backend MVP schema generated from the JPA entities.

```mermaid
erDiagram
    USERS ||--o{ DOCUMENTS : owns
    USERS ||--o{ DOCUMENT_ACTIONS : owns
    USERS ||--o{ REFRESH_TOKENS : owns
    DOCUMENTS ||--o{ DOCUMENT_FIELDS : has
    DOCUMENTS ||--o{ DOCUMENT_ACTIONS : creates
    DOCUMENTS ||--o{ ANALYSIS_JOBS : analyzed_by

    USERS {
        bigint user_id PK
        varchar email
        varchar password
        varchar name
        datetime created_at
        datetime updated_at
    }

    REFRESH_TOKENS {
        bigint refresh_token_id PK
        bigint user_id FK
        varchar token_hash
        datetime expires_at
        boolean revoked
        datetime created_at
        datetime updated_at
    }

    DOCUMENTS {
        bigint document_id PK
        bigint user_id FK
        varchar original_file_name
        varchar stored_file_name
        varchar file_path
        bigint file_size
        varchar mime_type
        varchar document_type
        varchar analysis_status
        varchar title
        text summary
        double confidence
        text ocr_text
        boolean deleted
        datetime created_at
        datetime updated_at
    }

    DOCUMENT_FIELDS {
        bigint field_id PK
        bigint document_id FK
        varchar field_key
        varchar field_label
        varchar field_value
        varchar field_type
        datetime created_at
    }

    ANALYSIS_JOBS {
        bigint job_id PK
        bigint document_id FK
        bigint user_id FK
        varchar status
        int retry_count
        int max_retry_count
        varchar error_code
        text error_message
        datetime started_at
        datetime completed_at
        datetime created_at
        datetime updated_at
    }

    DOCUMENT_ACTIONS {
        bigint action_id PK
        bigint document_id FK
        bigint user_id FK
        varchar action_type
        varchar title
        text description
        date action_date
        varchar status
        datetime created_at
        datetime updated_at
    }
```

## Notes

- `documents.deleted` supports logical deletion.
- `refresh_tokens.token_hash` stores a hash of the opaque refresh token, not the original token value.
- `document_fields` stores user-reviewable extracted values.
- `analysis_jobs` records the async analysis lifecycle.
- `document_actions` stores reminders and expense records generated after user review.
- Dynamic extracted values are stored as field rows rather than fixed columns to support multiple document types.

