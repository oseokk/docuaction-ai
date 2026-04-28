# Backend Implementation Checklist

## Initial Package Structure

```text
com.docuaction
  auth
  user
  document
  analysis
  action
  file
  usage
  notification
  common
```

## First APIs

```text
GET  /api/health
POST /api/auth/signup
POST /api/auth/login
GET  /api/users/me
POST /api/documents/upload
GET  /api/documents
GET  /api/documents/{documentId}
GET  /api/actions/upcoming
```

## Early Entity Draft

Core entities:

- User
- Document
- DocumentField
- DocumentAction
- AnalysisJob
- AnalysisLog
- UsageQuota
- AiUsageLog

## Engineering Decisions To Make Before Coding

- Gradle or Maven
- JPA or MyBatis
- PostgreSQL local setup method
- JWT access token expiration
- Refresh token strategy
- Local file storage path
- OCR provider for MVP
- AI provider and model

## Recommended Defaults

- Gradle
- Spring Data JPA
- PostgreSQL
- JWT access token plus refresh token
- Local private storage under backend runtime path for MVP
- PDF text extraction first
- One paid OCR provider only after core upload works

