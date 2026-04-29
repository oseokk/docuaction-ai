import 'dart:io';

import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';

import '../../core/network/api_client.dart';
import '../actions/action_list_screen.dart';
import '../auth/auth_repository.dart';
import '../documents/document_models.dart';
import '../documents/document_repository.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({
    required this.authRepository,
    required this.apiClient,
    required this.documentRepository,
    required this.onLogout,
    super.key,
  });

  final AuthRepository authRepository;
  final ApiClient apiClient;
  final DocumentRepository documentRepository;
  final VoidCallback onLogout;

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  late Future<List<DocumentListItem>> _documentsFuture;
  String? _message;
  bool _uploading = false;

  @override
  void initState() {
    super.initState();
    _documentsFuture = widget.documentRepository.fetchDocuments();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('DocuAction AI'),
        actions: [
          IconButton(
            tooltip: '예정 액션',
            onPressed: _openActions,
            icon: const Icon(Icons.notifications_outlined),
          ),
          IconButton(
            tooltip: '로그아웃',
            onPressed: _logout,
            icon: const Icon(Icons.logout),
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: _reload,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            Row(
              children: [
                Expanded(
                  child: Text(
                    '최근 문서',
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                ),
                FilledButton.icon(
                  onPressed: _uploading ? null : _pickAndUpload,
                  icon: _uploading
                      ? const SizedBox.square(
                          dimension: 16,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : const Icon(Icons.upload_file),
                  label: const Text('업로드'),
                ),
              ],
            ),
            if (_message != null) ...[
              const SizedBox(height: 12),
              Text(_message!),
            ],
            const SizedBox(height: 12),
            FutureBuilder<List<DocumentListItem>>(
              future: _documentsFuture,
              builder: (context, snapshot) {
                if (snapshot.connectionState == ConnectionState.waiting) {
                  return const Center(
                    child: Padding(
                      padding: EdgeInsets.all(32),
                      child: CircularProgressIndicator(),
                    ),
                  );
                }
                if (snapshot.hasError) {
                  return Text('문서 목록을 불러오지 못했습니다: ${snapshot.error}');
                }
                final documents = snapshot.data ?? [];
                if (documents.isEmpty) {
                  return const Padding(
                    padding: EdgeInsets.symmetric(vertical: 32),
                    child: Text('아직 업로드한 문서가 없습니다.'),
                  );
                }
                return Column(
                  children: documents.map(_DocumentTile.new).toList(),
                );
              },
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _reload() async {
    setState(() => _documentsFuture = widget.documentRepository.fetchDocuments());
    await _documentsFuture;
  }

  Future<void> _pickAndUpload() async {
    final result = await FilePicker.platform.pickFiles(
      type: FileType.custom,
      allowedExtensions: ['jpg', 'jpeg', 'png', 'pdf'],
    );
    final path = result?.files.single.path;
    if (path == null) return;

    setState(() {
      _uploading = true;
      _message = null;
    });
    try {
      await widget.documentRepository.upload(File(path));
      setState(() => _message = '업로드가 완료되었습니다. 분석이 시작됩니다.');
      await _reload();
    } on ApiException catch (exception) {
      setState(() => _message = exception.message);
    } finally {
      if (mounted) setState(() => _uploading = false);
    }
  }

  Future<void> _logout() async {
    await widget.authRepository.logout();
    widget.onLogout();
  }

  void _openActions() {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => ActionListScreen(apiClient: widget.apiClient),
      ),
    );
  }
}

class _DocumentTile extends StatelessWidget {
  const _DocumentTile(this.document);

  final DocumentListItem document;

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: ListTile(
        title: Text(document.title),
        subtitle: Text('${document.documentType} · ${document.analysisStatus}'),
        trailing: const Icon(Icons.chevron_right),
      ),
    );
  }
}
