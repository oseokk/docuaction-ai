class DocumentListItem {
  DocumentListItem({
    required this.documentId,
    required this.documentType,
    required this.title,
    required this.analysisStatus,
    required this.createdAt,
  });

  final int documentId;
  final String documentType;
  final String title;
  final String analysisStatus;
  final String createdAt;

  factory DocumentListItem.fromJson(Map<String, dynamic> json) {
    return DocumentListItem(
      documentId: json['documentId'] as int,
      documentType: json['documentType'] as String,
      title: json['title'] as String,
      analysisStatus: json['analysisStatus'] as String,
      createdAt: json['createdAt'] as String,
    );
  }
}
