# DocuAction AI

DocuAction AI is a personal document automation service.

The goal is not just to store documents. The service analyzes uploaded documents, extracts important fields, and turns them into follow-up actions such as reminders, expense records, and calendar events.

## Product Direction

Initial target users are freelancers and solo business owners who repeatedly manage receipts, contracts, bills, and certificates.

Core value:

- Upload a document from mobile.
- Extract text with OCR or PDF parsing.
- Classify the document type with AI.
- Extract fields such as issuer, amount, due date, and contract end date.
- Let the user review the result.
- Create actionable reminders or records.

## MVP Scope

The first MVP focuses on a small but production-shaped flow:

1. User signup and login
2. Document upload
3. Analysis status tracking
4. PDF text extraction and one image OCR provider
5. AI document classification and field extraction
6. User review and correction
7. Draft action creation
8. Action confirmation
9. Document list/detail APIs
10. Upcoming action APIs
11. Usage quota tracking

## Initial Tech Stack

- Backend: Java 17, Spring Boot
- Database: PostgreSQL
- Mobile: Flutter, after backend MVP stabilizes
- Storage: local private storage for MVP, S3-compatible storage later
- AI: schema-based AI response for classification and extraction
- OCR: one provider for MVP, with provider abstraction

## Repository Structure

Planned structure:

```text
docuAI/
  backend/
  mobile/
  docs/
```

Current focus:

- `docs/` for product and engineering decisions
- `backend/` after project scaffolding

## Key Design Principles

- AI output is never trusted without validation.
- Sensitive document data is treated as private by default.
- Cost is controlled with quotas, caching, and usage logs.
- User review is part of the main flow, not an optional afterthought.
- Background analysis must be recoverable after failures.

