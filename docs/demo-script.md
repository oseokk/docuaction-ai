# Demo Script

This script is for portfolio demos and local product walkthroughs.

## Goal

Show that DocuAction AI can turn a document into reviewed follow-up work:

```text
Sign up or log in
-> Upload sample bill PDF
-> Wait for analysis
-> Review extracted fields
-> Confirm result
-> See generated reminder action
```

## Local Setup

Terminal 1:

```bash
cd backend
./gradlew bootRun
```

Terminal 2:

```bash
cd mobile
flutter run -d chrome --dart-define=DOCUACTION_API_BASE_URL=http://localhost:8080
```

## Demo Account

The local H2 database resets when the backend restarts, so either sign up during the demo or use the default values already filled in the login screen:

```text
email: user@example.com
password: password1234
name: 김영석
```

## Sample Documents

Use the files in `docs/demo-assets/`.

- `sample-bill.txt`: plain text source for the sample bill
- `sample-bill.pdf`: upload-ready sample bill PDF

All sample content is fake and safe for public demos.

## Walkthrough

1. Open the Flutter web app.
2. Sign up with the demo account.
3. Upload `docs/demo-assets/sample-bill.pdf`.
4. Refresh or wait until the document status becomes `검수 필요`.
5. Open the document detail screen.
6. Explain the status banner and extracted fields.
7. Correct `기관명` to `한국전력 샘플`.
8. Confirm the review.
9. Show the generated reminder action.
10. Open the upcoming action list from the notification icon.

## Talking Points

- AI results do not become final automatically.
- The user reviews extracted fields before actions are activated.
- The backend tracks analysis and action lifecycle separately.
- The app is already shaped for web, macOS, iOS, and Android distribution.
- Release readiness is tracked in `docs/app-release-checklist.md`.

## Known Demo Boundaries

- Local mode uses H2 and local file storage.
- Image OCR is mock or unsupported depending on environment configuration.
- Real AI requires `DOCUACTION_AI_PROVIDER=openai` and `OPENAI_API_KEY`.
- Android emulator setup is still pending.
