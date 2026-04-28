# Product Risk Addendum

This document deepens the original design in five areas: cost, privacy, failure recovery, user review, and monetization.

## 1. Cost Management

### Cost Sources

- OCR API calls
- AI analysis API calls
- File storage
- OCR text and analysis result storage
- Reanalysis
- Push notification infrastructure

### Analysis Credit Policy

One uploaded document consumes one analysis credit when OCR or AI analysis starts.

Credits are not consumed when:

- Upload validation fails
- The system fails before external OCR or AI calls
- A provider outage prevents analysis from starting

Credits are consumed when:

- OCR and AI analysis are successfully requested
- The user manually requests reanalysis
- A different provider is selected for a second analysis attempt

### Cost Reduction

- Store file hashes to detect duplicate uploads.
- Reuse OCR text when only AI analysis is retried.
- Trim unnecessary OCR whitespace before AI requests.
- Use document-type-specific prompts after initial classification if needed.
- Limit OCR text length for MVP.
- Track per-user usage and provider cost.

### Suggested Tables

```text
USAGE_QUOTA
- quota_id
- user_id
- plan_type
- month
- analysis_limit
- analysis_used
- reset_at

AI_USAGE_LOG
- usage_id
- user_id
- document_id
- provider
- model
- input_tokens
- output_tokens
- estimated_cost
- status
- created_at
```

## 2. Privacy And Sensitive Data

Documents may contain names, addresses, phone numbers, account numbers, contract amounts, due dates, and certificate numbers.

### Storage Policy

- Store original files in private storage.
- Never expose the physical file path to clients.
- Use short-lived signed URLs for preview and download.
- Generate stored filenames with UUIDs.
- Encrypt files in production.

### Log Policy

Analysis logs must not store full OCR text or full AI request/response bodies by default.

Recommended log fields:

```text
ANALYSIS_LOG
- log_id
- document_id
- step
- status
- request_summary
- response_summary
- masked_sample_text
- error_code
- error_message
- created_at
```

### Deletion Policy

- Document deletion starts as logical deletion.
- Original files and OCR text are physically deleted after a retention period.
- Account deletion removes user documents, actions, analysis logs, and stored files.

## 3. Failure Recovery

### Failure Types

- Upload validation failure
- OCR provider failure
- AI provider failure
- AI JSON parsing failure
- Schema validation failure
- Required field missing
- Action generation failure
- Notification delivery failure

### Analysis Statuses

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

### Retry Policy

- Transient OCR/API errors: retry up to 2 times.
- AI JSON parsing failure: retry once with a stricter repair prompt.
- Low-quality OCR or missing required fields: move to `NEEDS_REVIEW`.
- Action generation failure: keep analysis result and mark action creation as failed.

### Recoverable Jobs

MVP may start with Spring `@Async`, but the production-shaped design should move to a database-backed job table or message queue.

```text
ANALYSIS_JOB
- job_id
- document_id
- user_id
- status
- retry_count
- max_retry_count
- next_retry_at
- locked_at
- error_code
- created_at
- updated_at
```

## 4. User Review

AI results should become useful only after the user can inspect and correct them.

### Review Required When

- AI confidence is below the threshold.
- Required fields are missing.
- Date or amount validation fails.
- OCR quality is low.
- Extracted values conflict with each other.

### Reviewable Fields

- Document type
- Title
- Summary
- Issuer or merchant
- Amount
- Date fields
- Generated action title and date

### Action Confirmation

AI-generated actions are initially saved as `DRAFT`.

After user confirmation:

- Reminder actions become `PENDING`.
- Expense records become confirmed records.
- The document analysis status becomes `COMPLETED`.

Suggested action statuses:

```text
DRAFT
PENDING
COMPLETED
CANCELED
FAILED
```

### Field History

```text
DOCUMENT_FIELD_HISTORY
- history_id
- field_id
- document_id
- old_value
- new_value
- changed_by
- created_at
```

## 5. Monetization

### Primary Target

The first commercial target is freelancers and solo business owners.

They repeatedly handle receipts, contracts, bills, and tax-related documents, and they have clearer willingness to pay than casual personal users.

### Pricing Model

Free:

- Monthly analysis limit
- Basic document storage
- Upcoming actions

Pro:

- Higher monthly analysis limit
- Expense summary
- Calendar integration
- Long-term file retention
- Export features

Business:

- Team sharing
- Admin dashboard
- Custom document templates
- Bulk upload
- API integration

### Paid Conversion Points

- Monthly analysis limit reached
- Calendar integration
- Expense statistics
- CSV/PDF export
- Long-term original file storage
- Custom reminder rules

### Positioning

Cloud drives store documents.

DocuAction AI extracts obligations, dates, and amounts from documents, then turns them into useful actions.

