import 'dart:io';

import '../../core/network/api_client.dart';
import 'document_models.dart';

class DocumentRepository {
  DocumentRepository(this._apiClient);

  final ApiClient _apiClient;

  Future<List<DocumentListItem>> fetchDocuments() async {
    final response = await _apiClient.getJson('/api/documents?page=0&size=20');
    final data = response['data'] as Map<String, dynamic>;
    final content = data['content'] as List<dynamic>;
    return content
        .cast<Map<String, dynamic>>()
        .map(DocumentListItem.fromJson)
        .toList();
  }

  Future<void> upload(File file) async {
    await _apiClient.uploadFile('/api/documents/upload', file);
  }
}
