import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import '../../core/network/api_client.dart';

class AuthRepository {
  AuthRepository(this._apiClient);

  static const _accessTokenKey = 'accessToken';
  static const _refreshTokenKey = 'refreshToken';

  final ApiClient _apiClient;
  final FlutterSecureStorage _storage = const FlutterSecureStorage();

  Future<bool> restoreSession() async {
    final accessToken = await _storage.read(key: _accessTokenKey);
    final refreshToken = await _storage.read(key: _refreshTokenKey);
    _apiClient.accessToken = accessToken;
    _apiClient.refreshToken = refreshToken;
    return accessToken != null && refreshToken != null;
  }

  Future<void> login(String email, String password) async {
    final response = await _apiClient.postJson(
      '/api/auth/login',
      body: {'email': email, 'password': password},
    );
    await _saveTokens(response['data'] as Map<String, dynamic>);
  }

  Future<void> signup(String email, String password, String name) async {
    await _apiClient.postJson(
      '/api/auth/signup',
      body: {'email': email, 'password': password, 'name': name},
    );
    await login(email, password);
  }

  Future<void> logout() async {
    _apiClient.accessToken = null;
    _apiClient.refreshToken = null;
    await _storage.delete(key: _accessTokenKey);
    await _storage.delete(key: _refreshTokenKey);
  }

  Future<void> _saveTokens(Map<String, dynamic> data) async {
    final accessToken = data['accessToken'] as String;
    final refreshToken = data['refreshToken'] as String;
    _apiClient.accessToken = accessToken;
    _apiClient.refreshToken = refreshToken;
    await _storage.write(key: _accessTokenKey, value: accessToken);
    await _storage.write(key: _refreshTokenKey, value: refreshToken);
  }
}
