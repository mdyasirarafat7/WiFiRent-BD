# WiFiRent BD - Flutter Mobile App

## Setup

### Prerequisites
- Flutter SDK (not available on this system - install from https://docs.flutter.dev)
- Android SDK

### Installation
1. Navigate to the frontend directory:
   ```powershell
   cd E:\wifirent-bd\frontend\wifirent_app
   ```

2. Install dependencies:
   ```powershell
   flutter pub get
   ```

3. Create a `.env` file:
   ```text
   API_BASE_URL=http://10.0.2.2:8080/api
   ```

4. Run on Android emulator/device:
   ```powershell
   flutter run
   ```

5. Build APK:
   ```powershell
   flutter build apk
   ```

## Architecture

Feature-based architecture:
- `lib/core/` - API client, providers, models, utilities
- `lib/features/auth/` - Authentication screens
- `lib/features/customer/` - Customer screens
- `lib/features/host/` - Host screens
- `lib/features/admin/` - Admin screens
- `lib/widgets/` - Shared widgets

## State Management
- Provider package for state management
- AuthProvider manages authentication state

## API Client
- Uses `http` package for REST calls
- Token stored in `SharedPreferences`
- Configurable base URL via `.env`

## Limitations
- Flutter SDK not available on this system; code is a reference implementation
- Install Flutter SDK to compile and test
