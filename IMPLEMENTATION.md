# Implementation Notes

## Overview
This Android application was created to solve mobile network connectivity issues by automating the network operator reset process.

## Architecture

### MainActivity
The main entry point of the application. It provides:
- A simple, user-friendly interface with Material Design components
- A button to initiate the network reset process
- A button to directly access network settings
- Status messages to guide the user through the process
- Instructions on how the network reset works

### Key Features Implemented

1. **Accessibility Service Automation (NEW)**: Fully automated network reset for non-rooted devices
   - Uses Android's Accessibility Service API to navigate Settings UI
   - Automatically disables/enables "Select automatically"
   - Selects network operators programmatically
   - Provides real-time status updates via broadcasts
   - Works on non-rooted devices with one-time permission grant
   
2. **Automatic Network Reset (Direct API)**: Attempts to automate the network reset process
   - Requests location permission at startup
   - Uses TelephonyManager APIs to control network selection
   - Provides step-by-step status updates during the process
   - Falls back to manual guidance if automation fails
   
3. **Runtime Permission Handling**: Requests ACCESS_FINE_LOCATION permission
   - Checks permission status at startup
   - Requests permission if not granted
   - Validates permission before attempting network operations

4. **Settings Access**: Direct access to network operator settings (fallback)
   - Opens Android's network operator settings when automation fails
   - Falls back to general wireless settings if operator settings aren't available

5. **User Guidance**: Clear instructions throughout the UI
   - Explains the 4-step process in a card view
   - Shows current status of operations
   - Provides helpful error messages
   - Displays accessibility service status and setup instructions

### Android Permissions

The app declares the following permissions in AndroidManifest.xml:
- `ACCESS_NETWORK_STATE`: To check network connectivity status
- `CHANGE_NETWORK_STATE`: To modify network settings
- `MODIFY_PHONE_STATE`: System-level permission for network operator selection
- `ACCESS_FINE_LOCATION`: To scan for available network operators
- `ACCESS_COARSE_LOCATION`: Alternative location permission

**Important Note**: `MODIFY_PHONE_STATE` is a system-level permission that regular apps cannot obtain through normal installation. This is an Android security restriction to prevent malicious apps from interfering with phone functionality. The app will attempt to use this permission, and if denied, will fall back to the manual approach.

### Implementation Approach

The app now takes a **three-tiered approach**:

1. **Primary approach - Accessibility Service (NEW)**: 
   - Uses Android's AccessibilityService API to automate UI interaction
   - Navigates through Settings app to perform network reset
   - Finds and clicks on UI elements programmatically
   - Works on non-rooted devices with user permission
   - Provides real-time status updates via broadcasts
   - Requires one-time setup in Accessibility Settings

2. **Secondary approach - Direct API (Rooted/System)**: 
   - Uses TelephonyManager.setNetworkSelectionModeAutomatic() and setNetworkSelectionModeManual()
   - Available since Android API 28 (Android 9.0)
   - Works on rooted devices or when installed as system app
   - Provides real-time status updates through the UI

3. **Fallback approach - Guided manual**:
   - Activates when SecurityException is caught (permission denied)
   - Opens the correct settings screen (ACTION_NETWORK_OPERATOR_SETTINGS)
   - Provides clear step-by-step guidance via toast message
   - Handles edge cases and fallback scenarios

This three-tiered approach:
- Provides full automation for non-rooted devices via Accessibility Service
- Attempts direct API automation where system permissions allow
- Works on all Android devices without requiring root access
- Provides the best user experience within Android's security constraints
- Maintains security by gracefully handling permission denials

### Resources

The app includes:
- **Layout**: activity_main.xml - ScrollView-based layout with ConstraintLayout
- **Strings**: All user-facing text is externalized for internationalization
- **Colors**: Material Design color palette
- **Themes**: Material Components theme
- **Icons**: Vector drawable launcher icon

### Build Configuration

- **Minimum SDK**: API 28 (Android 9.0 Pie)
- **Target SDK**: API 34 (Android 14)
- **Compile SDK**: API 34
- **Build Tools**: Compatible with Android Gradle Plugin 8.1.4
- **Kotlin Version**: 1.9.0
- **JDK Requirement**: Java 17 or higher

**Note**: Minimum SDK was increased to 28 because the TelephonyManager network selection APIs (setNetworkSelectionModeAutomatic and setNetworkSelectionModeManual) were introduced in API 28.

### Dependencies

Key dependencies used:
- AndroidX Core KTX
- AppCompat
- Material Components
- ConstraintLayout

All dependencies use stable, production-ready versions.

## Testing Considerations

To properly test this app:

1. **Physical Device**: Must be tested on a real Android device with cellular capabilities
2. **Network Conditions**: Best tested in areas with poor network connectivity
3. **Operator Settings**: Verify that the device allows access to network operator settings

## Future Enhancements

Possible improvements for future versions:

1. **Network Scan Results**: Display available networks to let user choose which one to select
2. **Network Monitoring**: Add background service to detect network issues and notify user
3. **Auto-trigger**: Detect network failures and prompt user to run reset
4. **Statistics**: Track success rate and network quality improvements
5. **Multiple Languages**: Add internationalization support for Portuguese (Brazil) and other languages
6. **Enhanced Accessibility**: Improve accessibility service to handle more UI variations and languages
7. **Smart Network Selection**: Use signal strength and network quality to select the best operator

## Known Limitations

1. **Accessibility Service**: Requires one-time user setup in Accessibility Settings
2. **UI Variations**: Accessibility service may need adjustment for different device manufacturers or Android versions
3. **Language Support**: Text patterns in accessibility service currently target English UI
4. **Direct API**: Requires system-level permission (MODIFY_PHONE_STATE) for rooted/system apps
5. Some device manufacturers may restrict access to network operator settings
6. Effectiveness depends on the specific network conditions and operator availability
7. Requires Android 9.0 (API 28) or higher

## CI/CD Pipeline

The project includes GitHub Actions workflows:

### android-ci.yml
- Triggers on push/PR to main and develop branches
- Builds the app
- Runs lint checks
- Runs unit tests
- Uploads debug APK as artifact
- Uploads lint reports

### android-release.yml
- Triggers on version tags (v*)
- Builds release APK
- Creates GitHub release
- Uploads APK to release assets

## Development Environment

Recommended setup:
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17 (required for Gradle 8.2)
- Android SDK with API 34
- Kotlin plugin 1.9.0 or newer

## Building the Project

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew test

# Run lint
./gradlew lint
```

## Troubleshooting

### Build Issues
- Ensure ANDROID_HOME is set correctly
- Verify JDK 17 or higher is installed
- Check that Android SDK API 34 is installed
- Run `./gradlew clean` if builds fail

### Runtime Issues
- Verify device has telephony capabilities
- Check that device is running Android 9.0 (API 28) or higher
- Ensure location permission is granted for network scanning
- Check that network operator settings are accessible on the device
- On non-rooted devices, be prepared to follow manual steps if automation fails

## License

This project is open source and available under the MIT License.
