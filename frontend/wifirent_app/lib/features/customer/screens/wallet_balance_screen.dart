import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:wifirent_app/core/providers/wallet_provider.dart';

class WalletBalanceScreen extends StatelessWidget {
  const WalletBalanceScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Wallet Balance')),
      body: Consumer<WalletProvider>(
        builder: (context, wallet, child) {
          if (wallet.isLoading) {
            return const Center(child: CircularProgressIndicator());
          }
          if (wallet.error != null) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(wallet.error!, style: const TextStyle(color: Colors.red)),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: () => wallet.loadBalance(),
                    child: const Text('Retry'),
                  ),
                ],
              ),
            );
          }
          final bal = wallet.balance;
          if (bal == null) {
            return const Center(child: Text('No balance data'));
          }
          return Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Icon(Icons.account_balance_wallet, size: 80, color: Colors.teal),
                const SizedBox(height: 24),
                const Text('Demo Credits', style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold)),
                const SizedBox(height: 8),
                Text(
                  wallet.formatDemoCredits(bal['balanceMinorUnits'] ?? 0),
                  style: const TextStyle(fontSize: 32, fontWeight: FontWeight.bold, color: Colors.teal),
                ),
                const SizedBox(height: 16),
                const Text(
                  'Demo credits have no real monetary value.\nNot withdrawable or redeemable for cash.',
                  textAlign: TextAlign.center,
                  style: TextStyle(fontSize: 14, color: Colors.grey),
                ),
                const SizedBox(height: 24),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    ElevatedButton.icon(
                      onPressed: () => Navigator.pushNamed(context, '/wallet/topup'),
                      icon: const Icon(Icons.add),
                      label: const Text('Top Up'),
                    ),
                    ElevatedButton.icon(
                      onPressed: () => Navigator.pushNamed(context, '/wallet/history'),
                      icon: const Icon(Icons.history),
                      label: const Text('History'),
                    ),
                  ],
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}
