# DocuAction Mobile

Flutter MVP client for the DocuAction AI backend.

## Setup

This repository contains the Flutter source shell. On a machine with Flutter installed:

```bash
cd mobile
flutter pub get
flutter run -d chrome --dart-define=DOCUACTION_API_BASE_URL=http://localhost:8080
```

Use `http://10.0.2.2:8080` for Android emulator and `http://localhost:8080` for iOS simulator, macOS, and web local runs.

## Verified Local Tooling

- Flutter 3.41.8
- Xcode 16.4
- CocoaPods 1.16.2
- Web runner
- macOS runner

Android builds still require Android Studio or a separately configured Android SDK.

## Tests

```bash
cd mobile
flutter test --no-test-assets
```
