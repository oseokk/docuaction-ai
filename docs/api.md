# API Documentation

Base URL:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

All protected APIs require:

```text
Authorization: Bearer {accessToken}
```

## Common Response

Success:

```json
{
  "success": true,
  "data": {},
  "error": null,
  "timestamp": "2026-04-29T00:00:00Z"
}
```

Error:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "COMMON_404",
    "message": "Resource not found."
  },
  "timestamp": "2026-04-29T00:00:00Z"
}
```

## Health

### `GET /api/health`

Public endpoint.

## Auth

### `POST /api/auth/signup`

```json
{
  "email": "user@example.com",
  "password": "password1234",
  "name": "김영석"
}
```

### `POST /api/auth/login`

```json
{
  "email": "user@example.com",
  "password": "password1234"
}
```

Response data:

```json
{
  "accessToken": "jwt-token",
  "tokenType": "Bearer"
}
```

## User

### `GET /api/users/me`

Returns the authenticated user.

## Documents

### `POST /api/documents/upload`

Content type:

```text
multipart/form-data
```

Form field:

```text
file
```

Supported files:

- JPG/JPEG
- PNG
- PDF

Limit:

- 10MB

Response data:

```json
{
  "documentId": 1,
  "status": "UPLOADED",
  "message": "Document uploaded. Analysis is ready to start."
}
```

After upload, an async analysis job starts. PDF files are parsed with PDFBox. The default local mode uses a mock AI analyzer. If `DOCUACTION_AI_PROVIDER=openai` and `OPENAI_API_KEY` are configured, the OpenAI adapter calls the Responses API with structured JSON output.

### `GET /api/documents?page=0&size=20&type=BILL&status=NEEDS_REVIEW`

Returns only the authenticated user's non-deleted documents.

Optional filters:

- `type`: one of the document types listed below
- `status`: one of the document statuses listed below

### `GET /api/documents/{documentId}`

Returns document metadata, extracted fields, and generated actions.

### `POST /api/documents/{documentId}/review`

Confirms or corrects AI-generated document analysis.

Request:

```json
{
  "documentType": "BILL",
  "title": "4월 전기요금 고지서",
  "summary": "사용자가 검수한 고지서입니다.",
  "fields": [
    {
      "key": "issuer",
      "label": "기관명",
      "value": "한국전력",
      "type": "STRING"
    },
    {
      "key": "amount",
      "label": "납부금액",
      "value": "80000",
      "type": "NUMBER"
    },
    {
      "key": "dueDate",
      "label": "납부기한",
      "value": "2026-05-11",
      "type": "DATE"
    }
  ]
}
```

Review completion changes document status to `COMPLETED` and regenerates actions.

### `DELETE /api/documents/{documentId}`

Performs logical deletion.

Deleted documents are excluded from:

- document list
- document detail
- upcoming actions

Original files are retained in MVP.

## Actions

### `GET /api/actions/upcoming`

Returns authenticated user's pending actions from today onward.

Deleted documents' actions are excluded.

### `POST /api/actions/{actionId}/complete`

Marks an action as completed.

Only the action owner can complete it.

## Document Statuses

```text
UPLOADED
PROCESSING
OCR_FAILED
AI_FAILED
VALIDATION_FAILED
ACTION_FAILED
NEEDS_REVIEW
COMPLETED
FAILED
```

## Document Types

```text
BILL
RECEIPT
CONTRACT
CERTIFICATE
ETC
UNKNOWN
```

## Action Rules

Current MVP rules:

- `BILL` with `dueDate`: creates `REMINDER` 3 days before due date.
- `RECEIPT` with `amount`: creates `EXPENSE_RECORD`.
- `CONTRACT` with `endDate`: creates `REMINDER` 30 days before end date.
