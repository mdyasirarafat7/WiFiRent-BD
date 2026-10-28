import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';

class ApiClient {
  static final ApiClient _instance = ApiClient._internal();
  factory ApiClient() => _instance;

  ApiClient._internal();

  String get baseUrl {
    return dotenv.env['API_BASE_URL'] ?? 'http://10.0.2.2:8080/api';
  }

  Future<String?> get _token async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString('access_token');
  }

  Future<void> _saveToken(String token) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('access_token', token);
  }

  Future<void> _saveRefreshToken(String token) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('refresh_token', token);
  }

  Future<String?> get _refreshToken async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString('refresh_token');
  }

  Future<void> clearTokens() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('access_token');
    await prefs.remove('refresh_token');
  }

  Future<http.Response> get(String path, {Map<String, String>? headers}) async {
    final token = await _token;
    final allHeaders = <String, String>{
      'Content-Type': 'application/json',
      if (token != null) 'Authorization': 'Bearer $token',
      if (headers != null) ...headers,
    };
    return http.get(Uri.parse('$baseUrl$path'), headers: allHeaders);
  }

  Future<http.Response> post(String path,
      {dynamic body, Map<String, String>? headers}) async {
    final token = await _token;
    final allHeaders = <String, String>{
      'Content-Type': 'application/json',
      if (token != null) 'Authorization': 'Bearer $token',
      if (headers != null) ...headers,
    };
    return http.post(
      Uri.parse('$baseUrl$path'),
      headers: allHeaders,
      body: body is String ? body : jsonEncode(body),
    );
  }

  Future<http.Response> put(String path,
      {dynamic body, Map<String, String>? headers}) async {
    final token = await _token;
    final allHeaders = <String, String>{
      'Content-Type': 'application/json',
      if (token != null) 'Authorization': 'Bearer $token',
      if (headers != null) ...headers,
    };
    return http.put(
      Uri.parse('$baseUrl$path'),
      headers: allHeaders,
      body: body is String ? body : jsonEncode(body),
    );
  }

  Future<http.Response> patch(String path,
      {dynamic body, Map<String, String>? headers}) async {
    final token = await _token;
    final allHeaders = <String, String>{
      'Content-Type': 'application/json',
      if (token != null) 'Authorization': 'Bearer $token',
      if (headers != null) ...headers,
    };
    return http.patch(
      Uri.parse('$baseUrl$path'),
      headers: allHeaders,
      body: body is String ? body : jsonEncode(body),
    );
  }

  Future<http.Response> delete(String path,
      {Map<String, String>? headers}) async {
    final token = await _token;
    final allHeaders = <String, String>{
      'Content-Type': 'application/json',
      if (token != null) 'Authorization': 'Bearer $token',
      if (headers != null) ...headers,
    };
    return http.delete(Uri.parse('$baseUrl$path'), headers: allHeaders);
  }
}
