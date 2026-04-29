# DocuAction AI

DocuAction AI is an AI-powered document automation service.

The service does not stop at storing uploaded documents. It extracts text, structures the document with AI-style analysis, lets the user review the result, and turns confirmed document data into follow-up actions such as reminders and expense records.

## Current MVP Flow

```text
Sign up / log in
-> Upload a PDF or image
-> Create an async analysis job
-> Extract PDF text
-> Classify and structure the document
-> Save extracted fields
-> Wait for user review
-> Confirm reviewed result
-> Generate document actions
-> Query or complete upcoming actions
```

## Implemented Backend Features

- Common API response and global exception handling
- Email/password signup and login
- BCrypt password hashing
- JWT access token authentication
- Current user lookup
- Multipart document upload
- File extension, MIME type, and 10MB size validation
- UUID-based local file storage
- User-owned document list and detail APIs
- Async analysis job pipeline
- PDF text extraction with Apache PDFBox
- Mock AI document classification and field extraction
- Optional OpenAI Responses API integration with Structured Outputs
- User review API for correcting AI results
- Automatic action generation after review
- Upcoming action query
- Action completion
- Logical document deletion

## Tech Stack

- Java 17
- Spring Boot 3.5
- Spring Security
- Spring Data JPA
- H2 for local MVP development
- PostgreSQL driver included for production-like migration
- Apache PDFBox
- Gradle Wrapper

## AI Provider Configuration

The default local mode uses the mock analyzer:

```bash
DOCUACTION_AI_PROVIDER=mock
```

To use OpenAI:

```bash
DOCUACTION_AI_PROVIDER=openai
OPENAI_API_KEY=your_api_key
```

The OpenAI adapter uses the Responses API with JSON schema structured output. The default model is configured in `backend/src/main/resources/application.yml`.

## Run Locally

```bash
cd backend
./gradlew bootRun
```

Health check:

```bash
curl http://localhost:8080/api/health
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Run tests:

```bash
cd backend
./gradlew test
```

## Local Storage

Uploaded files are stored under:

```text
backend/storage/documents
```

This directory is ignored by Git.

## API Documentation

See [docs/api.md](docs/api.md).

## Architecture Documentation

- [Architecture](docs/architecture.md)
- [ERD](docs/erd.md)

## Product Direction

The first target users are freelancers and solo business owners who repeatedly manage receipts, contracts, bills, and certificates.

Core value:

- Extract obligations, dates, and amounts from documents.
- Reduce manual document review and reminder setup.
- Keep user review in the loop before actions become active.

## Design Principles

- AI output is never treated as final without validation or user review.
- Sensitive document data is private by default.
- Background analysis must be recoverable and observable.
- Cost-sensitive AI/OCR work should be tracked and quota-controlled.
- The MVP should stay small but production-shaped.

## Repository Structure

```text
docuAI/
  backend/
    src/main/java/com/docuaction/
      action/
      analysis/
      auth/
      common/
      document/
      file/
      user/
  docs/
```

## Next Milestones

- Replace mock AI analysis with schema-based OpenAI integration.
- Add provider usage logging and cost estimates.
- Add image OCR provider integration.
- Add refresh token support.
- Add usage quota and AI/OCR usage logs.
- Add OpenAPI/Swagger generation.
- Start Flutter mobile MVP.
