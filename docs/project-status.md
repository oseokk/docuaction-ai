# Project Status

Checked against GitHub `origin/main` on 2026-04-29, then updated after merging the Flutter app shell into local `main`.

Updated on 2026-05-29 after local Flutter/iOS tooling setup.

Latest GitHub main commit observed before mobile merge:

```text
7cf5980 Add document reanalysis API
```

## Backend MVP Status

The backend MVP is functionally complete for the first product loop.

Implemented flow:

```text
Sign up / log in
-> Upload document
-> Create async analysis job
-> Extract PDF text or run configured image OCR provider
-> Analyze text with mock AI or OpenAI provider
-> Save structured fields
-> Wait for user review
-> User confirms or corrects fields
-> Generate actions
-> Query upcoming actions
-> Complete actions
-> Reanalyze or delete document
```

## Completed Areas

- Spring Boot backend scaffold
- Common API response format
- Global exception handling
- JWT access token authentication
- Refresh token rotation
- Current user API
- Document upload
- File validation and local storage
- Document list, detail, filters, and logical deletion
- Async analysis job pipeline
- PDF text extraction with Apache PDFBox
- Pluggable text extraction provider structure
- Mock image OCR provider for local demos
- Optional OpenAI Responses API provider with structured output
- Mock AI analyzer for local development
- Analysis usage logging foundation
- User review and correction API
- Action generation after review
- Upcoming action query
- Action completion
- Document reanalysis API
- Swagger/OpenAPI documentation
- Flutter mobile app shell
- Mobile signup/login screen
- Mobile token storage
- Mobile document upload
- Mobile document list
- Mobile upcoming action list
- Mobile document detail screen
- Mobile analysis result review screen
- Mobile web and macOS runner setup
- Mobile dependency lockfile
- README, API docs, architecture docs, ERD, and app release checklist

## Current Main Branch Boundaries

- Real external image OCR provider is not integrated yet.
- OpenAI integration is optional and requires `DOCUACTION_AI_PROVIDER=openai` plus `OPENAI_API_KEY`.
- The default local mode still uses mock AI analysis.
- Local storage is used instead of S3-compatible object storage.
- Async processing uses Spring `@Async`, not a persistent queue.
- Flutter app still needs stronger empty, loading, and failure states for demo polish.
- Flutter 3.41.8, Xcode 16.4, and CocoaPods 1.16.2 are configured locally.
- Local web runs require backend CORS for `localhost` and `127.0.0.1`, which is enabled for development.
- Android SDK is not configured yet, so Android emulator builds are still pending.

## Remote Branches Observed

These remote branches were present during the GitHub check:

```text
origin/feature-analysis-usage-log
origin/feature-flutter-app-shell
origin/feature-image-ocr-provider
origin/feature-mock-image-ocr
origin/feature-reanalyze-document
origin/feature-refresh-token
origin/fix-analysis-review-flow
```

Most backend feature branches have already been incorporated into `origin/main`. The Flutter app shell branch has now been merged into local `main` and is ready to push.

## Recommended Next Work

1. Polish Flutter empty, loading, and failure states for the demo loop.
2. Add Android SDK setup for Android emulator builds.
3. Add privacy policy and terms draft for future app distribution.
4. Add a real image OCR provider adapter.
5. Add provider cost estimates to `analysis_usage_logs`.
6. Replace local file storage with private object storage.
7. Add demo screenshots or GIFs for portfolio presentation.
