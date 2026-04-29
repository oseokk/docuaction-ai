import 'package:flutter/material.dart';

import '../../core/network/api_client.dart';

class ActionListScreen extends StatefulWidget {
  const ActionListScreen({required this.apiClient, super.key});

  final ApiClient apiClient;

  @override
  State<ActionListScreen> createState() => _ActionListScreenState();
}

class _ActionListScreenState extends State<ActionListScreen> {
  late Future<List<Map<String, dynamic>>> _actionsFuture;

  @override
  void initState() {
    super.initState();
    _actionsFuture = _fetchActions();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('예정 액션')),
      body: FutureBuilder<List<Map<String, dynamic>>>(
        future: _actionsFuture,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }
          if (snapshot.hasError) {
            return Center(child: Text('액션을 불러오지 못했습니다: ${snapshot.error}'));
          }
          final actions = snapshot.data ?? [];
          if (actions.isEmpty) {
            return const Center(child: Text('예정된 액션이 없습니다.'));
          }
          return ListView.builder(
            padding: const EdgeInsets.all(16),
            itemCount: actions.length,
            itemBuilder: (context, index) {
              final action = actions[index];
              return Card(
                child: ListTile(
                  title: Text(action['title']?.toString() ?? ''),
                  subtitle: Text(
                    '${action['actionType']} · ${action['actionDate']}',
                  ),
                  trailing: Text(action['status']?.toString() ?? ''),
                ),
              );
            },
          );
        },
      ),
    );
  }

  Future<List<Map<String, dynamic>>> _fetchActions() async {
    final response = await widget.apiClient.getJson('/api/actions/upcoming');
    final data = response['data'] as List<dynamic>;
    return data.cast<Map<String, dynamic>>();
  }
}
