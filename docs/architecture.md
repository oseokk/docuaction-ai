# Architecture

This document describes the current backend MVP architecture.

## Package Overview

```text
com.docuaction
  action      - document-generated actions and action lifecycle
  analysis    - async analysis jobs, text extraction, mock AI analysis
  auth        - signup, login, JWT issuing
  common      - response, exception, security, health
  document    - upload, query, review, logical deletion
  file        - local file storage
  user        - user entity and current user API
```

## High-Level Flow

```mermaid
flowchart TD
    Client["Mobile or API Client"]
    Auth["Auth API"]
    DocumentAPI["Document API"]
    ActionAPI["Action API"]
    Storage["Local Private Storage"]
    DB[("Database")]
    Async["Async Analysis Worker"]
    PDF["PDF Text Extractor"]
    AI["AI Analyzer\nMock or OpenAI"]

    Client --> Auth
    Auth --> DB

    Client --> DocumentAPI
    DocumentAPI --> Storage
    DocumentAPI --> DB
    DocumentAPI --> Async

    Async --> PDF
    Async --> AI
    Async --> DB

    Client --> ActionAPI
    ActionAPI --> DB
```

## Upload And Analysis Sequence

```mermaid
sequenceDiagram
    participant Client
    participant DocumentAPI
    participant FileStorage
    participant DB
    participant AnalysisWorker
    participant TextExtractor
    participant AiAnalyzer

    Client->>DocumentAPI: POST /api/documents/upload
    DocumentAPI->>FileStorage: Store uploaded file
    FileStorage-->>DocumentAPI: Stored file metadata
    DocumentAPI->>DB: Save Document with UPLOADED status
    DocumentAPI->>DB: Save AnalysisJob with PENDING status
    DocumentAPI-->>Client: documentId, UPLOADED

    DocumentAPI-->>AnalysisWorker: Start after transaction commit
    AnalysisWorker->>DB: Mark job PROCESSING
    AnalysisWorker->>DB: Mark document PROCESSING
    AnalysisWorker->>TextExtractor: Extract text from PDF
    TextExtractor-->>AnalysisWorker: OCR text
    AnalysisWorker->>AiAnalyzer: Classify and extract fields
    AiAnalyzer-->>AnalysisWorker: Structured analysis result
    AnalysisWorker->>DB: Save ocrText, summary, fields
    AnalysisWorker->>DB: Mark document NEEDS_REVIEW
    AnalysisWorker->>DB: Mark job COMPLETED
```

## Review And Action Generation Sequence

```mermaid
sequenceDiagram
    participant Client
    participant DocumentAPI
    participant ReviewService
    participant ActionService
    participant DB

    Client->>DocumentAPI: POST /api/documents/{documentId}/review
    DocumentAPI->>ReviewService: Review request
    ReviewService->>DB: Load document by documentId and userId
    ReviewService->>DB: Replace document fields
    ReviewService->>DB: Mark document COMPLETED
    ReviewService->>ActionService: Recreate actions
    ActionService->>DB: Delete previous actions for document
    ActionService->>DB: Save generated PENDING actions
    DocumentAPI-->>Client: COMPLETED
```

## Action Lifecycle

```mermaid
stateDiagram-v2
    [*] --> PENDING: Created after document review
    PENDING --> COMPLETED: User completes action
    PENDING --> CANCELED: Future
    PENDING --> FAILED: Future notification failure
```

## Document Lifecycle

```mermaid
stateDiagram-v2
    [*] --> UPLOADED
    UPLOADED --> PROCESSING: Async analysis starts
    PROCESSING --> NEEDS_REVIEW: Text extraction and analysis complete
    PROCESSING --> OCR_FAILED: PDF or OCR extraction failed
    PROCESSING --> FAILED: Unexpected failure
    NEEDS_REVIEW --> COMPLETED: User review complete
```

## Current MVP Boundaries

- Text extraction uses pluggable providers.
- PDF text extraction is real and uses Apache PDFBox.
- Image OCR currently uses an unsupported-image provider that fails clearly until a real OCR provider is configured.
- AI analysis defaults to `MockAiAnalysisService`.
- OpenAI integration can be enabled with `DOCUACTION_AI_PROVIDER=openai` and `OPENAI_API_KEY`.
- OCR and AI analysis steps write usage logs for provider, status, duration, payload size, and error details.
- Local H2 is used for MVP development.
- Uploaded files are stored locally under `backend/storage/documents`.
- Document deletion is logical. Original files are retained in MVP.

## Next Architecture Changes

- Add provider-level usage logs and cost estimates.
- Add image OCR provider integration.
- Move from local storage to S3-compatible private object storage.
- Replace `@Async` with a persistent job worker or queue when needed.
