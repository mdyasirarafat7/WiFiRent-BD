import 'package:flutter/foundation.dart';
import 'package:wifirent_app/core/api/api_client.dart';

class WalletProvider extends ChangeNotifier {
  final ApiClient _api = ApiClient();

  bool _isLoading = false;
  String? _error;
  Map<String, dynamic>? _balance;
  List<Map<String, dynamic>> _transactions = [];

  bool get isLoading => _isLoading;
  String? get error => _error;
  Map<String, dynamic>? get balance => _balance;
  List<Map<String, dynamic>> get transactions => _transactions;

  Future<void> loadBalance() async {
    _setLoading(true);
    _clearError();
    try {
      final response = await _api.get('/wallet/balance');
      if (response.statusCode == 200) {
        _balance = _decode(response.body);
        notifyListeners();
      } else {
        _setError('Failed to load balance: ${response.statusCode}');
      }
    } catch (e) {
      _setError('Network error: $e');
    } finally {
      _setLoading(false);
    }
  }

  Future<bool> topUp({
    required int targetUserId,
    required int amount,
    required String referenceId,
    String? idempotencyKey,
  }) async {
    _setLoading(true);
    _clearError();
    try {
      final response = await _api.post('/wallet/topup', body: {
        'targetUserId': targetUserId,
        'amountMinorUnits': amount,
        'referenceId': referenceId,
        'idempotencyKey': idempotencyKey,
      });
      if (response.statusCode == 200) {
        await loadBalance();
        return true;
      } else {
        _setError('Top-up failed: ${response.statusCode}');
        return false;
      }
    } catch (e) {
      _setError('Network error: $e');
      return false;
    } finally {
      _setLoading(false);
    }
  }

  Future<bool> transfer({
    required int targetUserId,
    required int amount,
    required String referenceId,
    String? idempotencyKey,
  }) async {
    _setLoading(true);
    _clearError();
    try {
      final response = await _api.post('/wallet/transfer', body: {
        'targetUserId': targetUserId,
        'amountMinorUnits': amount,
        'referenceId': referenceId,
        'idempotencyKey': idempotencyKey,
      });
      if (response.statusCode == 200) {
        await loadBalance();
        await loadTransactions();
        return true;
      } else {
        _setError('Transfer failed: ${response.statusCode}');
        return false;
      }
    } catch (e) {
      _setError('Network error: $e');
      return false;
    } finally {
      _setLoading(false);
    }
  }

  Future<void> loadTransactions({int page = 0, int size = 20}) async {
    _setLoading(true);
    _clearError();
    try {
      final response = await _api.get('/wallet/transactions?page=$page&size=$size');
      if (response.statusCode == 200) {
        final data = _decode(response.body);
        _transactions = List<Map<String, dynamic>>.from(data['content'] ?? []);
        notifyListeners();
      } else {
        _setError('Failed to load transactions: ${response.statusCode}');
      }
    } catch (e) {
      _setError('Network error: $e');
    } finally {
      _setLoading(false);
    }
  }

  Future<Map<String, dynamic>?> purchasePackage({
    required int hotspotId,
    required int packageId,
    String? idempotencyKey,
  }) async {
    _setLoading(true);
    _clearError();
    try {
      final response = await _api.post('/wallet/purchase', body: {
        'hotspotId': hotspotId,
        'packageId': packageId,
        'idempotencyKey': idempotencyKey,
      });
      if (response.statusCode == 200) {
        final result = _decode(response.body);
        await loadBalance();
        await loadTransactions();
        return result;
      } else {
        _setError('Purchase failed: ${response.statusCode}');
        return null;
      }
    } catch (e) {
      _setError('Network error: $e');
      return null;
    } finally {
      _setLoading(false);
    }
  }

  void _setLoading(bool loading) {
    _isLoading = loading;
    notifyListeners();
  }

  void _clearError() {
    _error = null;
  }

  void _setError(String error) {
    _error = error;
    notifyListeners();
  }

  dynamic _decode(String body) {
    return const JsonDecoder().convert(body);
  }

  String formatDemoCredits(int minorUnits) {
    return '${minorUnits} demo credits';
  }
}
