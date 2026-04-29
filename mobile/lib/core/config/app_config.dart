class AppConfig {
  static const apiBaseUrl = String.fromEnvironment(
    'DOCUACTION_API_BASE_URL',
    defaultValue: 'http://10.0.2.2:8080',
  );
}
