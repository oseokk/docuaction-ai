# App Release Checklist

DocuAction AI is being built as both a portfolio project and a future distributable app. This checklist tracks the non-code requirements that must be handled before a real public release.

## Accounts And Distribution

- Apple Developer account enrolled
- App Store Connect app record created
- Google Play Console account enrolled
- Android SDK and release signing configured
- Bundle identifiers and package names finalized
- App icons and launch assets replaced with branded assets

## Privacy And Trust

- Privacy policy drafted and hosted
- Terms of service drafted and hosted
- AI result disclaimer added to onboarding or review flow
- User review requirement explained before actions become active
- Contact/support email configured
- Account deletion request path defined
- Document deletion and retention policy defined

## Sensitive Document Handling

- Original files stored in private object storage
- Physical file paths never returned to clients
- Production file encryption decided
- Signed URL strategy for future preview/download
- OCR text and AI request logs masked by default
- Production logs checked for personal data leakage

## Backend Readiness

- Production PostgreSQL configured
- Local H2 limited to development profile
- JWT secret required in production
- CORS restricted to production domains
- File upload size and MIME policy reviewed
- Persistent analysis queue or recoverable job runner planned
- Rate limiting and abuse protection planned

## AI And Cost Control

- Per-document AI/OCR usage logging verified
- User quota model finalized
- Reanalysis cost policy finalized
- Provider outage fallback behavior defined
- OpenAI API key stored only in server environment
- Estimated cost fields added before paid launch

## Payments And Monetization

- Free plan limits defined
- Paid plan limits defined
- Refund and cancellation policy drafted
- Store payment rules reviewed for iOS and Android
- Server-side entitlement checks planned

## Release QA

- End-to-end demo flow tested on web
- iOS simulator flow tested
- Android emulator flow tested
- Upload, analysis, review, action creation, and deletion tested
- Failed OCR/AI states tested
- Accessibility labels and readable empty states reviewed
