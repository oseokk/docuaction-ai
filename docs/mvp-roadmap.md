# MVP Roadmap

## Phase 0. Project Foundation

Goal: define the product and engineering boundaries before implementation.

- Define MVP scope
- Define target user and monetization direction
- Define document types
- Define backend package structure
- Define database draft
- Define AI/OCR risk policies

Deliverables:

- README
- MVP roadmap
- product risk addendum
- backend implementation checklist

## Phase 1. Backend Foundation

Goal: create a runnable Spring Boot backend.

- Create Spring Boot project
- Add PostgreSQL configuration
- Add common response format
- Add global exception handling
- Add health check API
- Add local profile

Done when:

- Backend runs locally
- Health check API responds
- Basic project structure is stable

## Phase 2. Auth

Goal: support authenticated document ownership.

- User signup
- User login
- Password hashing
- JWT access token
- Refresh token decision
- Current user lookup

Done when:

- A user can sign up and log in
- Protected APIs can identify the user

## Phase 3. Document Upload

Goal: upload and store documents safely.

- Multipart upload
- File extension validation
- MIME type validation
- File size limit
- UUID stored filename
- Document metadata persistence
- Analysis status initialization

Done when:

- A logged-in user can upload a PDF or image
- The document appears in the database with `UPLOADED` status

## Phase 4. Analysis Pipeline

Goal: analyze uploaded documents asynchronously.

- Background analysis job
- PDF text extraction
- OCR provider abstraction
- AI analysis request
- JSON schema validation
- Field persistence
- Analysis logs
- Failure status handling

Done when:

- A document can move from `UPLOADED` to `NEEDS_REVIEW` or a failure status

## Phase 5. User Review

Goal: make AI results trustworthy through user correction.

- Analysis result API
- Editable extracted fields
- Document type correction
- Draft action preview
- User confirmation

Done when:

- A user can approve or correct AI results before actions become active

## Phase 6. Actions

Goal: turn documents into useful follow-up work.

- Reminder action generation
- Expense action generation
- Action status lifecycle
- Upcoming action API
- Action completion API

Done when:

- Confirmed analysis results create actionable records

## Phase 7. Portfolio Packaging

Goal: make the project easy to evaluate.

- Architecture diagram
- ERD
- API documentation
- Failure flow diagram
- Demo screenshots or GIF
- README polish

