import 'package:flutter/material.dart';

import '../../core/network/api_client.dart';
import 'document_models.dart';
import 'document_repository.dart';
import 'document_status.dart';

class DocumentDetailScreen extends StatefulWidget {
  const DocumentDetailScreen({
    required this.documentId,
    required this.documentRepository,
    super.key,
  });

  final int documentId;
  final DocumentRepository documentRepository;

  @override
  State<DocumentDetailScreen> createState() => _DocumentDetailScreenState();
}

class _DocumentDetailScreenState extends State<DocumentDetailScreen> {
  late Future<DocumentDetail> _detailFuture;

  @override
  void initState() {
    super.initState();
    _detailFuture = _fetchDetail();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('문서 상세')),
      body: FutureBuilder<DocumentDetail>(
        future: _detailFuture,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }
          if (snapshot.hasError) {
            return Center(child: Text('문서를 불러오지 못했습니다: ${snapshot.error}'));
          }
          final detail = snapshot.data!;
          return _DocumentDetailBody(
            detail: detail,
            documentRepository: widget.documentRepository,
            onReload: _reload,
          );
        },
      ),
    );
  }

  Future<DocumentDetail> _fetchDetail() {
    return widget.documentRepository.fetchDocument(widget.documentId);
  }

  Future<void> _reload() async {
    setState(() => _detailFuture = _fetchDetail());
    await _detailFuture;
  }
}

class _DocumentDetailBody extends StatefulWidget {
  const _DocumentDetailBody({
    required this.detail,
    required this.documentRepository,
    required this.onReload,
  });

  final DocumentDetail detail;
  final DocumentRepository documentRepository;
  final Future<void> Function() onReload;

  @override
  State<_DocumentDetailBody> createState() => _DocumentDetailBodyState();
}

class _DocumentDetailBodyState extends State<_DocumentDetailBody> {
  late final TextEditingController _titleController;
  late final TextEditingController _summaryController;
  late String _documentType;
  late List<_EditableField> _fields;
  bool _saving = false;
  String? _message;

  @override
  void initState() {
    super.initState();
    final detail = widget.detail;
    _titleController = TextEditingController(text: detail.title);
    _summaryController = TextEditingController(text: detail.summary ?? '');
    _documentType = detail.documentType;
    _fields = detail.fields.map(_EditableField.fromItem).toList();
  }

  @override
  void dispose() {
    _titleController.dispose();
    _summaryController.dispose();
    for (final field in _fields) {
      field.dispose();
    }
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final detail = widget.detail;
    return RefreshIndicator(
      onRefresh: widget.onReload,
      child: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          _Header(detail: detail),
          const SizedBox(height: 12),
          _StatusBanner(detail: detail),
          const SizedBox(height: 16),
          TextField(
            controller: _titleController,
            enabled: detail.canReview && !_saving,
            decoration: const InputDecoration(labelText: '제목'),
          ),
          const SizedBox(height: 12),
          DropdownButtonFormField<String>(
            initialValue: _documentType,
            decoration: const InputDecoration(labelText: '문서 유형'),
            items: const [
              DropdownMenuItem(value: 'BILL', child: Text('고지서')),
              DropdownMenuItem(value: 'RECEIPT', child: Text('영수증')),
              DropdownMenuItem(value: 'CONTRACT', child: Text('계약서')),
              DropdownMenuItem(value: 'CERTIFICATE', child: Text('증명서')),
              DropdownMenuItem(value: 'ETC', child: Text('기타')),
              DropdownMenuItem(value: 'UNKNOWN', child: Text('미분류')),
            ],
            onChanged: detail.canReview && !_saving
                ? (value) =>
                    setState(() => _documentType = value ?? _documentType)
                : null,
          ),
          const SizedBox(height: 12),
          TextField(
            controller: _summaryController,
            enabled: detail.canReview && !_saving,
            minLines: 3,
            maxLines: 5,
            decoration: const InputDecoration(labelText: '요약'),
          ),
          const SizedBox(height: 24),
          Row(
            children: [
              Expanded(
                child: Text(
                  '추출 필드',
                  style: Theme.of(context).textTheme.titleMedium,
                ),
              ),
              if (detail.canReview)
                TextButton.icon(
                  onPressed: _saving ? null : _addField,
                  icon: const Icon(Icons.add),
                  label: const Text('필드'),
                ),
            ],
          ),
          const SizedBox(height: 8),
          if (_fields.isEmpty)
            const Text('추출된 필드가 없습니다.')
          else
            ..._fields.map(
              (field) => _FieldEditor(
                field: field,
                enabled: detail.canReview && !_saving,
                onRemove: detail.canReview && !_saving
                    ? () => setState(() {
                          field.dispose();
                          _fields.remove(field);
                        })
                    : null,
              ),
            ),
          if (_message != null) ...[
            const SizedBox(height: 12),
            _InlineMessage(message: _message!),
          ],
          const SizedBox(height: 24),
          if (detail.canReview)
            FilledButton.icon(
              onPressed: _saving ? null : _submitReview,
              icon: _saving
                  ? const SizedBox.square(
                      dimension: 16,
                      child: CircularProgressIndicator(strokeWidth: 2),
                    )
                  : const Icon(Icons.check_circle_outline),
              label: const Text('검수 완료'),
            ),
          if (!detail.canReview && detail.analysisStatus != 'COMPLETED') ...[
            const SizedBox(height: 12),
            OutlinedButton.icon(
              onPressed: widget.onReload,
              icon: const Icon(Icons.refresh),
              label: const Text('새로고침'),
            ),
          ],
          const SizedBox(height: 28),
          Text(
            '생성된 액션',
            style: Theme.of(context).textTheme.titleMedium,
          ),
          const SizedBox(height: 8),
          if (detail.actions.isEmpty)
            const Text('아직 생성된 액션이 없습니다.')
          else
            ...detail.actions.map(_ActionTile.new),
        ],
      ),
    );
  }

  void _addField() {
    setState(() {
      _fields.add(
        _EditableField(
          keyController: TextEditingController(text: 'customField'),
          labelController: TextEditingController(text: '사용자 필드'),
          valueController: TextEditingController(),
          type: 'STRING',
        ),
      );
    });
  }

  Future<void> _submitReview() async {
    final title = _titleController.text.trim();
    if (title.isEmpty) {
      setState(() => _message = '제목을 입력해주세요.');
      return;
    }
    final hasInvalidField = _fields.any(
      (field) =>
          field.keyController.text.trim().isEmpty ||
          field.labelController.text.trim().isEmpty,
    );
    if (hasInvalidField) {
      setState(() => _message = '필드 키와 라벨은 비워둘 수 없습니다.');
      return;
    }

    setState(() {
      _saving = true;
      _message = null;
    });
    try {
      await widget.documentRepository.reviewDocument(
        documentId: widget.detail.documentId,
        documentType: _documentType,
        title: title,
        summary: _summaryController.text.trim(),
        fields: _fields.map((field) => field.toItem()).toList(),
      );
      setState(() => _message = '검수가 완료되었습니다.');
      await widget.onReload();
    } on ApiException catch (exception) {
      setState(() => _message = exception.message);
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }
}

class _Header extends StatelessWidget {
  const _Header({required this.detail});

  final DocumentDetail detail;

  @override
  Widget build(BuildContext context) {
    final confidence = detail.confidence;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          detail.originalFileName,
          style: Theme.of(context).textTheme.titleLarge,
        ),
        const SizedBox(height: 8),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            DocumentStatusChip(status: detail.analysisStatus),
            Chip(label: Text(documentTypeLabel(detail.documentType))),
            if (confidence != null)
              Chip(label: Text('신뢰도 ${(confidence * 100).round()}%')),
          ],
        ),
        const SizedBox(height: 8),
        Text('${_formatBytes(detail.fileSize)} · ${detail.mimeType}'),
      ],
    );
  }

  static String _formatBytes(int bytes) {
    if (bytes < 1024) return '$bytes B';
    if (bytes < 1024 * 1024) return '${(bytes / 1024).toStringAsFixed(1)} KB';
    return '${(bytes / 1024 / 1024).toStringAsFixed(1)} MB';
  }
}

class _StatusBanner extends StatelessWidget {
  const _StatusBanner({required this.detail});

  final DocumentDetail detail;

  @override
  Widget build(BuildContext context) {
    final info = documentStatusInfo(detail.analysisStatus);
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: info.color.withValues(alpha: 0.08),
        border: Border.all(color: info.color.withValues(alpha: 0.25)),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(info.icon, color: info.color),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  info.label,
                  style: Theme.of(context).textTheme.titleSmall?.copyWith(
                        color: info.color,
                        fontWeight: FontWeight.w700,
                      ),
                ),
                const SizedBox(height: 4),
                Text(info.description),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _InlineMessage extends StatelessWidget {
  const _InlineMessage({required this.message});

  final String message;

  @override
  Widget build(BuildContext context) {
    final color = Theme.of(context).colorScheme.primary;
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.08),
        border: Border.all(color: color.withValues(alpha: 0.24)),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Row(
        children: [
          Icon(Icons.info_outline, color: color),
          const SizedBox(width: 8),
          Expanded(child: Text(message)),
        ],
      ),
    );
  }
}

class _FieldEditor extends StatelessWidget {
  const _FieldEditor({
    required this.field,
    required this.enabled,
    required this.onRemove,
  });

  final _EditableField field;
  final bool enabled;
  final VoidCallback? onRemove;

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          children: [
            Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: field.labelController,
                    enabled: enabled,
                    decoration: const InputDecoration(labelText: '라벨'),
                  ),
                ),
                IconButton(
                  tooltip: '필드 삭제',
                  onPressed: onRemove,
                  icon: const Icon(Icons.delete_outline),
                ),
              ],
            ),
            const SizedBox(height: 8),
            TextField(
              controller: field.keyController,
              enabled: enabled,
              decoration: const InputDecoration(labelText: '키'),
            ),
            const SizedBox(height: 8),
            TextField(
              controller: field.valueController,
              enabled: enabled,
              decoration: const InputDecoration(labelText: '값'),
            ),
            const SizedBox(height: 8),
            DropdownButtonFormField<String>(
              initialValue: field.type,
              decoration: const InputDecoration(labelText: '타입'),
              items: const [
                DropdownMenuItem(value: 'STRING', child: Text('문자')),
                DropdownMenuItem(value: 'NUMBER', child: Text('숫자')),
                DropdownMenuItem(value: 'DATE', child: Text('날짜')),
              ],
              onChanged:
                  enabled ? (value) => field.type = value ?? field.type : null,
            ),
          ],
        ),
      ),
    );
  }
}

class _ActionTile extends StatelessWidget {
  const _ActionTile(this.action);

  final DocumentActionItem action;

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: ListTile(
        title: Text(action.title),
        subtitle: Text('${action.actionType} · ${action.actionDate}'),
        trailing: Text(action.status),
      ),
    );
  }
}

class _EditableField {
  _EditableField({
    required this.keyController,
    required this.labelController,
    required this.valueController,
    required this.type,
  });

  final TextEditingController keyController;
  final TextEditingController labelController;
  final TextEditingController valueController;
  String type;

  factory _EditableField.fromItem(DocumentFieldItem item) {
    return _EditableField(
      keyController: TextEditingController(text: item.key),
      labelController: TextEditingController(text: item.label),
      valueController: TextEditingController(text: item.value ?? ''),
      type: item.type,
    );
  }

  DocumentFieldItem toItem() {
    return DocumentFieldItem(
      key: keyController.text.trim(),
      label: labelController.text.trim(),
      value: valueController.text.trim(),
      type: type,
    );
  }

  void dispose() {
    keyController.dispose();
    labelController.dispose();
    valueController.dispose();
  }
}
