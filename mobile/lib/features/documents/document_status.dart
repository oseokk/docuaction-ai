import 'package:flutter/material.dart';

class DocumentStatusInfo {
  const DocumentStatusInfo({
    required this.label,
    required this.description,
    required this.icon,
    required this.color,
  });

  final String label;
  final String description;
  final IconData icon;
  final Color color;
}

DocumentStatusInfo documentStatusInfo(String status) {
  return switch (status) {
    'UPLOADED' => const DocumentStatusInfo(
        label: '업로드됨',
        description: '문서가 저장됐고 분석을 준비하고 있습니다.',
        icon: Icons.cloud_done_outlined,
        color: Color(0xFF2563EB),
      ),
    'PROCESSING' => const DocumentStatusInfo(
        label: '분석 중',
        description: '문서 내용을 읽고 필요한 정보를 추출하고 있습니다.',
        icon: Icons.sync,
        color: Color(0xFF7C3AED),
      ),
    'NEEDS_REVIEW' => const DocumentStatusInfo(
        label: '검수 필요',
        description: 'AI가 추출한 내용을 확인하고 확정해주세요.',
        icon: Icons.rate_review_outlined,
        color: Color(0xFFD97706),
      ),
    'COMPLETED' => const DocumentStatusInfo(
        label: '완료',
        description: '검수가 끝났고 후속 액션이 생성됐습니다.',
        icon: Icons.check_circle_outline,
        color: Color(0xFF059669),
      ),
    'OCR_FAILED' => const DocumentStatusInfo(
        label: 'OCR 실패',
        description: '문서 텍스트 추출에 실패했습니다. 파일 품질을 확인해주세요.',
        icon: Icons.image_not_supported_outlined,
        color: Color(0xFFDC2626),
      ),
    'AI_FAILED' => const DocumentStatusInfo(
        label: 'AI 실패',
        description: 'AI 분석에 실패했습니다. 잠시 후 다시 시도할 수 있습니다.',
        icon: Icons.psychology_alt_outlined,
        color: Color(0xFFDC2626),
      ),
    'VALIDATION_FAILED' => const DocumentStatusInfo(
        label: '검증 실패',
        description: '분석 결과가 필요한 형식을 만족하지 못했습니다.',
        icon: Icons.rule_folder_outlined,
        color: Color(0xFFDC2626),
      ),
    'ACTION_FAILED' => const DocumentStatusInfo(
        label: '액션 생성 실패',
        description: '분석은 끝났지만 후속 액션 생성에 실패했습니다.',
        icon: Icons.event_busy_outlined,
        color: Color(0xFFDC2626),
      ),
    'FAILED' => const DocumentStatusInfo(
        label: '실패',
        description: '문서 처리 중 문제가 발생했습니다.',
        icon: Icons.error_outline,
        color: Color(0xFFDC2626),
      ),
    _ => DocumentStatusInfo(
        label: status,
        description: '현재 처리 상태를 확인하고 있습니다.',
        icon: Icons.info_outline,
        color: const Color(0xFF64748B),
      ),
  };
}

String documentTypeLabel(String type) {
  return switch (type) {
    'BILL' => '고지서',
    'RECEIPT' => '영수증',
    'CONTRACT' => '계약서',
    'CERTIFICATE' => '증명서',
    'ETC' => '기타',
    'UNKNOWN' => '미분류',
    _ => type,
  };
}

class DocumentStatusChip extends StatelessWidget {
  const DocumentStatusChip({required this.status, super.key});

  final String status;

  @override
  Widget build(BuildContext context) {
    final info = documentStatusInfo(status);
    return Chip(
      avatar: Icon(info.icon, size: 18, color: info.color),
      label: Text(info.label),
      side: BorderSide(color: info.color.withValues(alpha: 0.35)),
      backgroundColor: info.color.withValues(alpha: 0.08),
      labelStyle: TextStyle(color: info.color, fontWeight: FontWeight.w600),
    );
  }
}
