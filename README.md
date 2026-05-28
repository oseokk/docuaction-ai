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
- Refresh token rotation
- Current user lookup
- Multipart document upload
- File extension, MIME type, and 10MB size validation
- UUID-based local file storage
- User-owned document list and detail APIs
- Async analysis job pipeline
- Document reanalysis request API
- AI/OCR usage logging foundation
- PDF text extraction with Apache PDFBox
- Pluggable text extraction provider structure
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
- Flutter 3.41
- Xcode 16.4 and CocoaPods for iOS/macOS Flutter development

## AI Provider Configuration

The default local mode uses the mock analyzer:

```bash
DOCUACTION_AI_PROVIDER=mock
```

Image OCR defaults to an unsupported provider so missing OCR integration fails clearly. For local demos, enable the mock image OCR provider:

```bash
DOCUACTION_OCR_IMAGE_PROVIDER=mock
```

To use OpenAI:

```bash
DOCUACTION_AI_PROVIDER=openai
OPENAI_API_KEY=your_api_key
```

The OpenAI adapter uses the Responses API with JSON schema structured output. The default model is configured through `DOCUACTION_OPENAI_MODEL`.

## Environment Profiles

- `local`: H2 database, H2 console enabled, local file storage, development JWT secret fallback.
- `prod`: PostgreSQL from environment variables, H2 console disabled, JWT secret required.

## Run Locally

The default Spring profile is `local`.

```bash
cd backend
./gradlew bootRun
```

Production-like run:

```bash
cd backend
SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun
```

Required production variables are listed in `.env.example`.

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

## Mobile MVP

Flutter source lives under `mobile/`.

```bash
cd mobile
flutter pub get
flutter run -d chrome --dart-define=DOCUACTION_API_BASE_URL=http://localhost:8080
```

Use `http://10.0.2.2:8080` for Android emulator and `http://localhost:8080` for iOS simulator, macOS, and web local runs. The repository includes Flutter web and macOS runner files.

The current mobile shell includes signup/login, token storage, document upload, document list, and upcoming action list screens.

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
- [Project Status](docs/project-status.md)
- [App Release Checklist](docs/app-release-checklist.md)
- [Privacy Policy Draft](docs/privacy-policy-draft.md)
- [Terms Of Service Draft](docs/terms-of-service-draft.md)

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
  mobile/
    lib/
      core/
      features/
  docs/
```

## Next Milestones

- Expand Flutter document detail and review screens.
- Add Android SDK setup for Android emulator builds.
- Add a real image OCR provider adapter.
- Add provider cost estimates to analysis usage logs.
- Replace local file storage with private object storage.
- Add demo screenshots or GIFs for portfolio presentation.
