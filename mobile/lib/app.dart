import 'package:flutter/material.dart';

import 'core/config/app_config.dart';
import 'core/network/api_client.dart';
import 'features/auth/auth_repository.dart';
import 'features/auth/login_screen.dart';
import 'features/documents/document_repository.dart';
import 'features/home/home_screen.dart';

class DocuActionApp extends StatefulWidget {
  const DocuActionApp({super.key});

  @override
  State<DocuActionApp> createState() => _DocuActionAppState();
}

class _DocuActionAppState extends State<DocuActionApp> {
  late final ApiClient _apiClient;
  late final AuthRepository _authRepository;
  late final DocumentRepository _documentRepository;
  bool _authenticated = false;

  @override
  void initState() {
    super.initState();
    _apiClient = ApiClient(baseUrl: AppConfig.apiBaseUrl);
    _authRepository = AuthRepository(_apiClient);
    _documentRepository = DocumentRepository(_apiClient);
    _restoreSession();
  }

  Future<void> _restoreSession() async {
    final restored = await _authRepository.restoreSession();
    if (!mounted) return;
    setState(() => _authenticated = restored);
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'DocuAction AI',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF2563EB)),
        useMaterial3: true,
      ),
      home: _authenticated
          ? HomeScreen(
              authRepository: _authRepository,
              apiClient: _apiClient,
              documentRepository: _documentRepository,
              onLogout: () => setState(() => _authenticated = false),
            )
          : LoginScreen(
              authRepository: _authRepository,
              onAuthenticated: () => setState(() => _authenticated = true),
            ),
    );
  }
}
