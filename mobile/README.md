# DocuAction Mobile

Flutter MVP client for the DocuAction AI backend.

## Setup

This repository currently contains the Flutter source shell. On a machine with Flutter installed:

```bash
cd mobile
flutter create --platforms android,ios .
flutter pub get
flutter run --dart-define=DOCUACTION_API_BASE_URL=http://10.0.2.2:8080
```

Use `http://10.0.2.2:8080` for Android emulator and `http://localhost:8080` for desktop/web local runs.
