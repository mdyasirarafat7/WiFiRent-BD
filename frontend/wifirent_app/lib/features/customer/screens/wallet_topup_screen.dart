import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:wifirent_app/core/providers/wallet_provider.dart';

class WalletTopUpScreen extends StatefulWidget {
  const WalletTopUpScreen({super.key});

  @override
  State<WalletTopUpScreen> createState() => _WalletTopUpScreenState();
}

class _WalletTopUpScreenState extends State<WalletTopUpScreen> {
  final _amountController = TextEditingController();
  final _refController = TextEditingController();
  final _formKey = GlobalKey<FormState>();

  @override
  void dispose() {
    _amountController.dispose();
    _refController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Demo Top-Up')),
      body: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'Demo Top-Up',
                style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 8),
              const Text(
                'Add demo credits to your wallet. These are virtual credits only - no real money is involved.',
                style: TextStyle(fontSize: 14, color: Colors.grey),
              ),
              const SizedBox(height: 24),
              TextFormField(
                controller: _amountController,
                decoration: const InputDecoration(
                  labelText: 'Amount (demo credits)',
                  border: OutlineInputBorder(),
                  prefixText: '',
                ),
                keyboardType: TextInputType.number,
                validator: (v) {
                  if (v == null || v.isEmpty) return 'Amount is required';
                  if (int.tryParse(v) == null || int.parse(v) <= 0)
                    return 'Enter a valid positive amount';
                  return null;
                },
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _refController,
                decoration: const InputDecoration(
                  labelText: 'Reference ID',
                  border: OutlineInputBorder(),
                  hintText: 'e.g. topup-001',
                ),
                validator: (v) =>
                    v == null || v.isEmpty ? 'Reference ID is required' : null,
              ),
              const SizedBox(height: 24),
              Consumer<WalletProvider>(
                builder: (context, wallet, child) {
                  if (wallet.isLoading) {
                    return const CircularProgressIndicator();
                  }
                  if (wallet.error != null) {
                    return Column(
                      children: [
                        Text(wallet.error!,
                            style: const TextStyle(color: Colors.red)),
                        const SizedBox(height: 16),
                      ],
                    );
                  }
                  return const SizedBox.shrink();
                },
              ),
              SizedBox(
                width: double.infinity,
                height: 50,
                child: ElevatedButton(
                  onPressed: () async {
                    if (_formKey.currentState!.validate()) {
                      final success = await context.read<WalletProvider>().topUp(
                        targetUserId: 1, // Current user - in real app this comes from auth
                        amount: int.parse(_amountController.text),
                        referenceId: _refController.text,
                      );
                      if (success && mounted) {
                        Navigator.pop(context);
                      }
                    }
                  },
                  child: const Text('Add Demo Credits'),
                ),
              ),
              const SizedBox(height: 16),
              const Text(
                'Note: This is a demo system. Credits cannot be purchased with real money.',
                style: TextStyle(fontSize: 12, color: Colors.grey),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
