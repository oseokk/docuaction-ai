# Privacy Policy Draft

This is a working draft for a future DocuAction AI public release. It is not legal advice and must be reviewed before production launch.

## 1. Service Summary

DocuAction AI helps users upload documents, extract key information, review AI-assisted analysis results, and create follow-up actions such as reminders.

## 2. Data We May Collect

- Account information: email, name, password hash
- Uploaded document files: PDFs and images selected by the user
- Document metadata: filename, MIME type, file size, upload time, analysis status
- Extracted text and fields: issuer, amount, dates, summary, document type
- User review changes: corrected titles, summaries, fields, and confirmation state
- Generated actions: reminder title, action date, status
- Usage and diagnostic data: analysis provider, operation status, estimated usage, error codes

## 3. How We Use Data

- Authenticate users and protect user-owned documents
- Store uploaded documents for analysis and user access
- Extract text and generate structured fields
- Let users review, correct, and confirm AI-assisted results
- Create reminders and other follow-up actions
- Monitor usage, reliability, abuse, and provider cost

## 4. AI Processing

DocuAction AI may send extracted text or document-derived content to an AI provider when AI analysis is enabled.

Production requirements before launch:

- Send only the minimum necessary content to providers
- Avoid logging full OCR text or full AI request/response bodies by default
- Store API keys only on the server
- Disclose AI provider usage in the published privacy policy

## 5. File Storage And Retention

MVP mode stores files in local private server storage. Production should use private object storage.

Planned production policy:

- Store original files privately
- Use UUID-based stored filenames
- Do not expose physical storage paths to clients
- Support user-initiated document deletion
- Define a retention period for physical deletion after logical deletion

## 6. Sharing

We do not sell user documents or extracted document data.

Data may be shared only with:

- Infrastructure providers needed to run the service
- AI/OCR providers needed to process documents
- Legal or security authorities if required by law

## 7. User Controls

Planned user controls:

- Delete uploaded documents
- Request account deletion
- Review and correct AI results before actions become active
- Opt out of optional marketing communication

## 8. Security

Planned production safeguards:

- Password hashing
- JWT-based authenticated access
- Private file storage
- HTTPS-only production traffic
- Restricted CORS origins
- Secret management through environment variables
- Sensitive log masking

## 9. Contact

Support contact must be finalized before public release.

Placeholder:

```text
support@example.com
```
