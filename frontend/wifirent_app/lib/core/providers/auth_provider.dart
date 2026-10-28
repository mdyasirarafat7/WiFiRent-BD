import 'package:flutter/foundation.dart';
import 'package:wifirent_app/core/api/api_client.dart';

class AuthProvider extends ChangeNotifier {
  final ApiClient _api = ApiClient();

  bool _isAuthenticated = false;
  bool _isLoading = false;
  String? _error;
  String? _userName;
  String? _userEmail;
  String? _userRole;

  bool get isAuthenticated => _isAuthenticated;
  bool get isLoading => _isLoading;
  String? get error => _error;
  String? get userName => _userName;
  String? get userEmail => _userEmail;
  String? get userRole => _userRole;

  Future<bool> login(String email, String password) async {
    _setLoading(true);
    _clearError();
    try {
      final response = await _api.post('/auth/login', body: {
        'email': email,
        'password': password,
      });
      if (response.statusCode == 200) {
        final data = _decode(response.body);
        await _saveTokens(data['accessToken'], data['refreshToken']);
        _setUser(data['name'], data['email'], data['role']);
        _isAuthenticated = true;
        notifyListeners();
        return true;
      } else {
        _setError('Login failed: ${response.statusCode}');
        return false;
      }
    } catch (e) {
      _setError('Network error: $e');
      return false;
    } finally {
      _setLoading(false);
    }
  }

  Future<bool> register(String name, String email, String password) async {
    _setLoading(true);
    _clearError();
    try {
      final response = await _api.post('/auth/register', body: {
        'name': name,
        'email': email,
        'password': password,
      });
      if (response.statusCode == 200 || response.statusCode == 201) {
        final data = _decode(response.body);
        await _saveTokens(data['accessToken'], data['refreshToken']);
        _setUser(data['name'], data['email'], data['role']);
        _isAuthenticated = true;
        notifyListeners();
        return true;
      } else {
        _setError('Registration failed: ${response.statusCode}');
        return false;
      }
    } catch (e) {
      _setError('Network error: $e');
      return false;
    } finally {
      _setLoading(false);
    }
  }

  Future<void> logout() async {
    await _api.post('/auth/logout');
    await _api.clearTokens();
    _isAuthenticated = false;
    _userName = null;
    _userEmail = null;
    _userRole = null;
    notifyListeners();
  }

  Future<void> loadSession() async {
    final token = await _api._token;
    if (token != null) {
      _isAuthenticated = true;
      notifyListeners();
    }
  }

  void _saveTokens(String accessToken, String refreshToken) async {
    await _api._saveToken(accessToken);
    await _api._saveRefreshToken(refreshToken);
  }

  void _setUser(String? name, String? email, String? role) {
    _userName = name;
    _userEmail = email;
    _userRole = role;
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
}
