import 'dart:io';

import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';

import '../../core/network/api_client.dart';
import '../actions/action_list_screen.dart';
import '../auth/auth_repository.dart';
import '../documents/document_detail_screen.dart';
import '../documents/document_models.dart';
import '../documents/document_repository.dart';
import '../documents/document_status.dart';

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
                  return _StatePanel(
                    icon: Icons.wifi_off_outlined,
                    title: '문서 목록을 불러오지 못했습니다.',
                    message: snapshot.error.toString(),
                  );
                }
                final documents = snapshot.data ?? [];
                if (documents.isEmpty) {
                  return const _StatePanel(
                    icon: Icons.upload_file_outlined,
                    title: '아직 업로드한 문서가 없습니다.',
                    message: '고지서, 영수증, 계약서 PDF나 이미지를 업로드하면 분석이 시작됩니다.',
                  );
                }
                return Column(
                  children: documents
                      .map(
                        (document) => _DocumentTile(
                          document: document,
                          onTap: () => _openDocument(document.documentId),
                        ),
                      )
                      .toList(),
                );
              },
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _reload() async {
    setState(
        () => _documentsFuture = widget.documentRepository.fetchDocuments());
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

  Future<void> _openDocument(int documentId) async {
    await Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => DocumentDetailScreen(
          documentId: documentId,
          documentRepository: widget.documentRepository,
        ),
      ),
    );
    if (mounted) await _reload();
  }
}

class _DocumentTile extends StatelessWidget {
  const _DocumentTile({
    required this.document,
    required this.onTap,
  });

  final DocumentListItem document;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: ListTile(
        onTap: onTap,
        title: Text(
          document.title,
          maxLines: 1,
          overflow: TextOverflow.ellipsis,
        ),
        subtitle: Padding(
          padding: const EdgeInsets.only(top: 8),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Wrap(
                spacing: 6,
                runSpacing: 6,
                children: [
                  DocumentStatusChip(status: document.analysisStatus),
                  Chip(label: Text(documentTypeLabel(document.documentType))),
                ],
              ),
              const SizedBox(height: 6),
              Text(
                document.originalFileName,
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
            ],
          ),
        ),
        trailing: const Icon(Icons.chevron_right),
      ),
    );
  }
}

class _StatePanel extends StatelessWidget {
  const _StatePanel({
    required this.icon,
    required this.title,
    required this.message,
  });

  final IconData icon;
  final String title;
  final String message;

  @override
  Widget build(BuildContext context) {
    final color = Theme.of(context).colorScheme.primary;
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 28),
      child: Column(
        children: [
          Icon(icon, size: 44, color: color),
          const SizedBox(height: 12),
          Text(
            title,
            textAlign: TextAlign.center,
            style: Theme.of(context).textTheme.titleMedium,
          ),
          const SizedBox(height: 6),
          Text(
            message,
            textAlign: TextAlign.center,
            style: Theme.of(context).textTheme.bodyMedium,
          ),
        ],
      ),
    );
  }
}
