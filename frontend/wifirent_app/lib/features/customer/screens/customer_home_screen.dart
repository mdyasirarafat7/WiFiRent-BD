import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:wifirent_app/core/providers/auth_provider.dart';
import 'package:wifirent_app/core/providers/wallet_provider.dart';
import 'package:wifirent_app/core/api/api_client.dart';

class CustomerHomeScreen extends StatefulWidget {
  const CustomerHomeScreen({super.key});

  @override
  State<CustomerHomeScreen> createState() => _CustomerHomeScreenState();
}

class _CustomerHomeScreenState extends State<CustomerHomeScreen> {
  int _selectedIndex = 0;

  @override
  void initState() {
    super.initState();
    Future.microtask(() {
      context.read<WalletProvider>().loadBalance();
      context.read<WalletProvider>().loadTransactions();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('WiFiRent BD'),
        actions: [
          Consumer<WalletProvider>(
            builder: (context, wallet, child) {
              final bal = wallet.balance;
              final credits = bal != null ? bal['balanceMinorUnits'] ?? 0 : 0;
              return Padding(
                padding: const EdgeInsets.only(right: 16),
                child: Text(
                  '$credits demo credits',
                  style: const TextStyle(fontWeight: FontWeight.bold),
                ),
              );
            },
          ),
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: () {
              context.read<AuthProvider>().logout();
              Navigator.pushReplacementNamed(context, '/login');
            },
          ),
        ],
      ),
      body: IndexedStack(
        index: _selectedIndex,
        children: [
          _buildHotspotsTab(),
          _buildWalletTab(),
        ],
      ),
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _selectedIndex,
        onTap: (i) => setState(() => _selectedIndex = i),
        items: const [
          BottomNavigationBarItem(
            icon: Icon(Icons.wifi),
            label: 'Hotspots',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.account_balance_wallet),
            label: 'Wallet',
          ),
        ],
      ),
    );
  }

  Widget _buildHotspotsTab() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Icon(Icons.wifi, size: 64, color: Colors.grey),
          const SizedBox(height: 16),
          const Text('Discover nearby hotspots'),
          const SizedBox(height: 24),
          SizedBox(
            width: double.infinity,
            height: 50,
            child: ElevatedButton(
              onPressed: () {
                // Navigate to nearby hotspots
              },
              child: const Text('Find Hotspots Near Me'),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildWalletTab() {
    return Consumer<WalletProvider>(
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
        final credits = bal != null ? bal['balanceMinorUnits'] ?? 0 : 0;
        return Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.account_balance_wallet, size: 80, color: Colors.teal),
              const SizedBox(height: 24),
              const Text('Demo Credits Balance',
                  style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold)),
              const SizedBox(height: 8),
              Text(
                '$credits',
                style: const TextStyle(fontSize: 48, fontWeight: FontWeight.bold, color: Colors.teal),
              ),
              const SizedBox(height=24),
              const Text(
                'Demo credits have no real monetary value.\nNot withdrawable or redeemable for cash.',
                textAlign: TextAlign.center,
                style: TextStyle(fontSize: 14, color: Colors.grey),
              ),
              const SizedBox(height: 32),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  SizedBox(
                    width: 150,
                    height: 50,
                    child: ElevatedButton.icon(
                      onPressed: () => Navigator.pushNamed(context, '/wallet/topup'),
                      icon: const Icon(Icons.add),
                      label: const Text('Top Up'),
                    ),
                  ),
                  const SizedBox(width: 16),
                  SizedBox(
                    width: 150,
                    height: 50,
                    child: ElevatedButton.icon(
                      onPressed: () => Navigator.pushNamed(context, '/wallet/history'),
                      icon: const Icon(Icons.history),
                      label: const Text('History'),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 32),
              SizedBox(
                width: double.infinity,
                height: 50,
                child: ElevatedButton(
                  onPressed: () async {
                    final result = await context.read<WalletProvider>().purchasePackage(
                      hotspotId: 1,
                      packageId: 1,
                    );
                    if (result == null && mounted) {
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(
                          content: Text('Purchase failed - insufficient credits or error'),
                          backgroundColor: Colors.red,
                        ),
                      );
                    }
                  },
                  child: const Text('Buy Internet Package'),
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}
