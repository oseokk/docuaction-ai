import 'package:flutter/material.dart';

import '../../core/network/api_client.dart';
import 'auth_repository.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({
    required this.authRepository,
    required this.onAuthenticated,
    super.key,
  });

  final AuthRepository authRepository;
  final VoidCallback onAuthenticated;

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _emailController = TextEditingController(text: 'user@example.com');
  final _passwordController = TextEditingController(text: 'password1234');
  final _nameController = TextEditingController(text: '김영석');
  bool _signupMode = false;
  bool _loading = false;
  String? _error;

  @override
  void dispose() {
    _emailController.dispose();
    _passwordController.dispose();
    _nameController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: Center(
          child: ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 420),
            child: ListView(
              shrinkWrap: true,
              padding: const EdgeInsets.all(24),
              children: [
                Text(
                  'DocuAction AI',
                  style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                        fontWeight: FontWeight.w700,
                      ),
                ),
                const SizedBox(height: 8),
                Text(
                  '문서를 분석하고 해야 할 일을 자동으로 정리합니다.',
                  style: Theme.of(context).textTheme.bodyMedium,
                ),
                const SizedBox(height: 32),
                if (_signupMode)
                  TextField(
                    controller: _nameController,
                    decoration: const InputDecoration(labelText: '이름'),
                  ),
                TextField(
                  controller: _emailController,
                  decoration: const InputDecoration(labelText: '이메일'),
                  keyboardType: TextInputType.emailAddress,
                ),
                TextField(
                  controller: _passwordController,
                  decoration: const InputDecoration(labelText: '비밀번호'),
                  obscureText: true,
                ),
                if (_error != null) ...[
                  const SizedBox(height: 16),
                  Text(_error!, style: const TextStyle(color: Colors.red)),
                ],
                const SizedBox(height: 24),
                FilledButton(
                  onPressed: _loading ? null : _submit,
                  child: Text(_signupMode ? '회원가입' : '로그인'),
                ),
                TextButton(
                  onPressed: _loading
                      ? null
                      : () => setState(() => _signupMode = !_signupMode),
                  child: Text(_signupMode ? '로그인으로 돌아가기' : '회원가입'),
                ),
                const SizedBox(height: 16),
                const _ReleaseNotice(),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Future<void> _submit() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      if (_signupMode) {
        await widget.authRepository.signup(
          _emailController.text.trim(),
          _passwordController.text,
          _nameController.text.trim(),
        );
      } else {
        await widget.authRepository.login(
          _emailController.text.trim(),
          _passwordController.text,
        );
      }
      widget.onAuthenticated();
    } on ApiException catch (exception) {
      setState(() => _error = exception.message);
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }
}

class _ReleaseNotice extends StatelessWidget {
  const _ReleaseNotice();

  @override
  Widget build(BuildContext context) {
    final textStyle = Theme.of(context).textTheme.bodySmall;
    final color = Theme.of(context).colorScheme.primary;
    return DecoratedBox(
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.08),
        border: Border.all(color: color.withValues(alpha: 0.2)),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(Icons.verified_user_outlined, size: 18, color: color),
                const SizedBox(width: 6),
                Text(
                  '출시 준비 고지',
                  style: textStyle?.copyWith(
                    color: color,
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            Text(
              '문서에는 개인정보가 포함될 수 있습니다. AI 분석 결과는 참고용이며, 액션 생성 전 사용자가 반드시 검수해야 합니다.',
              style: textStyle,
            ),
            const SizedBox(height: 6),
            Text(
              '개인정보 처리방침과 이용약관 초안은 docs 폴더에서 관리됩니다.',
              style: textStyle,
            ),
          ],
        ),
      ),
    );
  }
}
