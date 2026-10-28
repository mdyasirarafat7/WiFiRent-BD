import 'package:flutter/material.dart';
import 'package:wifirent_app/core/api/api_client.dart';
import 'package:wifirent_app/core/providers/auth_provider.dart';
import 'package:wifirent_app/core/providers/wallet_provider.dart';
import 'package:wifirent_app/features/auth/screens/splash_screen.dart';
import 'package:wifirent_app/features/auth/screens/login_screen.dart';
import 'package:wifirent_app/features/auth/screens/register_screen.dart';
import 'package:wifirent_app/features/customer/screens/customer_home_screen.dart';
import 'package:wifirent_app/features/customer/screens/wallet_balance_screen.dart';
import 'package:wifirent_app/features/customer/screens/wallet_topup_screen.dart';
import 'package:wifirent_app/features/customer/screens/wallet_transaction_history_screen.dart';
import 'package:wifirent_app/features/host/screens/host_dashboard_screen.dart';
import 'package:wifirent_app/features/admin/screens/admin_dashboard_screen.dart';

void main() {
  runApp(const WiFiRentApp());
}

class WiFiRentApp extends StatelessWidget {
  const WiFiRentApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => AuthProvider()),
        ChangeNotifierProvider(create: (_) => WalletProvider()),
      ],
      child: MaterialApp(
        title: 'WiFiRent BD',
        theme: ThemeData(
          primarySwatch: Colors.teal,
          useMaterial3: true,
        ),
        initialRoute: '/',
        routes: {
          '/': (context) => const SplashScreen(),
          '/login': (context) => const LoginScreen(),
          '/register': (context) => const RegisterScreen(),
          '/customer/home': (context) => const CustomerHomeScreen(),
          '/wallet/balance': (context) => const WalletBalanceScreen(),
          '/wallet/topup': (context) => const WalletTopUpScreen(),
          '/wallet/history': (context) => const WalletTransactionHistoryScreen(),
          '/host/dashboard': (context) => const HostDashboardScreen(),
          '/admin/dashboard': (context) => const AdminDashboardScreen(),
        },
        onGenerateRoute: (settings) {
          return MaterialPageRoute(
            builder: (context) => const SplashScreen(),
          );
        },
      ),
    );
  }
}
