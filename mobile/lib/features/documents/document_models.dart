class DocumentListItem {
  DocumentListItem({
    required this.documentId,
    required this.documentType,
    required this.title,
    required this.summary,
    required this.originalFileName,
    required this.analysisStatus,
    required this.createdAt,
  });

  final int documentId;
  final String documentType;
  final String title;
  final String? summary;
  final String originalFileName;
  final String analysisStatus;
  final String createdAt;

  factory DocumentListItem.fromJson(Map<String, dynamic> json) {
    return DocumentListItem(
      documentId: json['documentId'] as int,
      documentType: json['documentType'] as String,
      title: json['title'] as String,
      summary: json['summary'] as String?,
      originalFileName: json['originalFileName'] as String,
      analysisStatus: json['analysisStatus'] as String,
      createdAt: json['createdAt'] as String,
    );
  }
}

class DocumentDetail {
  DocumentDetail({
    required this.documentId,
    required this.documentType,
    required this.title,
    required this.summary,
    required this.confidence,
    required this.originalFileName,
    required this.fileSize,
    required this.mimeType,
    required this.analysisStatus,
    required this.fields,
    required this.actions,
    required this.createdAt,
    required this.updatedAt,
  });

  final int documentId;
  final String documentType;
  final String title;
  final String? summary;
  final double? confidence;
  final String originalFileName;
  final int fileSize;
  final String mimeType;
  final String analysisStatus;
  final List<DocumentFieldItem> fields;
  final List<DocumentActionItem> actions;
  final String createdAt;
  final String updatedAt;

  bool get canReview => analysisStatus == 'NEEDS_REVIEW';

  factory DocumentDetail.fromJson(Map<String, dynamic> json) {
    final fields = json['fields'] as List<dynamic>? ?? [];
    final actions = json['actions'] as List<dynamic>? ?? [];
    return DocumentDetail(
      documentId: json['documentId'] as int,
      documentType: json['documentType'] as String,
      title: json['title'] as String,
      summary: json['summary'] as String?,
      confidence: (json['confidence'] as num?)?.toDouble(),
      originalFileName: json['originalFileName'] as String,
      fileSize: json['fileSize'] as int,
      mimeType: json['mimeType'] as String,
      analysisStatus: json['analysisStatus'] as String,
      fields: fields
          .cast<Map<String, dynamic>>()
          .map(DocumentFieldItem.fromJson)
          .toList(),
      actions: actions
          .cast<Map<String, dynamic>>()
          .map(DocumentActionItem.fromJson)
          .toList(),
      createdAt: json['createdAt'] as String,
      updatedAt: json['updatedAt'] as String,
    );
  }
}

class DocumentFieldItem {
  DocumentFieldItem({
    required this.key,
    required this.label,
    required this.value,
    required this.type,
  });

  final String key;
  final String label;
  final String? value;
  final String type;

  factory DocumentFieldItem.fromJson(Map<String, dynamic> json) {
    return DocumentFieldItem(
      key: json['key'] as String,
      label: json['label'] as String,
      value: json['value'] as String?,
      type: json['type'] as String,
    );
  }

  Map<String, dynamic> toReviewJson() {
    return {
      'key': key,
      'label': label,
      'value': value,
      'type': type,
    };
  }
}

class DocumentActionItem {
  DocumentActionItem({
    required this.actionId,
    required this.actionType,
    required this.title,
    required this.description,
    required this.actionDate,
    required this.status,
  });

  final int actionId;
  final String actionType;
  final String title;
  final String? description;
  final String actionDate;
  final String status;

  factory DocumentActionItem.fromJson(Map<String, dynamic> json) {
    return DocumentActionItem(
      actionId: json['actionId'] as int,
      actionType: json['actionType'] as String,
      title: json['title'] as String,
      description: json['description'] as String?,
      actionDate: json['actionDate'] as String,
      status: json['status'] as String,
    );
  }
}
