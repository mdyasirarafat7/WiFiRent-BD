import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:wifirent_app/core/providers/wallet_provider.dart';

class WalletTransactionHistoryScreen extends StatelessWidget {
  const WalletTransactionHistoryScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Transaction History')),
      body: Consumer<WalletProvider>(
        builder: (context, wallet, child) {
          if (wallet.isLoading) {
            return const Center(child: CircularProgressIndicator());
          }
          final txs = wallet.transactions;
          if (txs.isEmpty) {
            return const Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.receipt_long, size: 64, color: Colors.grey),
                  SizedBox(height: 16),
                  Text('No transactions yet'),
                ],
              ),
            );
          }
          return ListView.builder(
            itemCount: txs.length,
            itemBuilder: (context, index) {
              final tx = txs[index];
              final isCredit = (tx['amountMinorUnits'] ?? 0) > 0;
              return ListTile(
                leading: CircleAvatar(
                  backgroundColor: isCredit ? Colors.green.shade100 : Colors.red.shade100,
                  child: Icon(
                    isCredit ? Icons.arrow_downward : Icons.arrow_upward,
                    color: isCredit ? Colors.green : Colors.red,
                  ),
                ),
                title: Text(tx['transactionType'] ?? 'Unknown'),
                subtitle: Text(tx['description'] ?? ''),
                trailing: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Text(
                      '${isCredit ? '+' : ''}${tx['amountMinorUnits'] ?? 0}',
                      style: TextStyle(
                        fontWeight: FontWeight.bold,
                        color: isCredit ? Colors.green : Colors.red,
                      ),
                    ),
                    Text(
                      tx['createdAt']?.toString().substring(0, 16) ?? '',
                      style: const TextStyle(fontSize: 12, color: Colors.grey),
                    ),
                  ],
                ),
              );
            },
          );
        },
      ),
    );
  }
}
