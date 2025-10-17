# Mobile Network Reset - Android App

An Android application designed to help users in areas with poor mobile network connectivity (like some regions in Brazil) by automating the network operator reset process.

> 🚀 **NEW**: Now supports **non-rooted devices** using Accessibility Service! See the [Quick Setup Guide](QUICK_SETUP_GUIDE.md) to get started.

## Quick Links

- 📖 [Quick Setup Guide](QUICK_SETUP_GUIDE.md) - Get started with accessibility service
- 🔧 [Technical Documentation](ACCESSIBILITY_SERVICE.md) - How the service works
- 🛡️ [Security Summary](SECURITY_SUMMARY.md) - Security analysis
- 💡 [Implementation Details](IMPLEMENTATION.md) - Architecture and design
- 🤝 [Contributing Guide](CONTRIBUTING.md) - How to contribute

## Problem Statement

Sometimes mobile networks fail to properly connect when using data networks on Android devices. This is particularly noticeable in regions where it's difficult to find nearby mobile operator towers. A manual workaround exists that involves:

1. Opening Android mobile network connections
2. Opening network operators
3. Disabling the "Select automatically" flag
4. Choosing one of the available mobile networks
5. Going back
6. Opening network operators again
7. Re-enabling the "Select automatically" flag

This app simplifies this tedious manual process with a single button press.

## Features

- **Accessibility Service Automation**: Fully automated network reset for non-rooted devices
- **One-Tap Network Reset**: Simple button to initiate the network reset process
- **Multiple Reset Methods**: Direct API (rooted), Accessibility Service (non-rooted), or manual
- **User Guidance**: Clear instructions and step-by-step guidance
- **Direct Settings Access**: Quick access to network operator settings
- **Material Design**: Modern, intuitive user interface
- **Status Updates**: Real-time feedback during the automated reset process

## How It Works

The app provides multiple automated approaches to reset your mobile network:

### Approach 1: Direct API (Rooted Devices/System Apps)
1. **Requests location permission**: Required for network scanning
2. **Disables automatic network selection**: Toggles off auto-select mode programmatically
3. **Scans for available networks**: Identifies the current network operator
4. **Selects the network manually**: Connects to the operator manually
5. **Re-enables automatic network selection**: Returns to auto-select mode

### Approach 2: Accessibility Service (Non-Rooted Devices) - NEW!
For regular (non-rooted) devices, the app now includes an **Accessibility Service** that automates the manual process:

1. **Enable the accessibility service**: One-time setup in Android Settings
2. **Automated navigation**: The service navigates through Settings UI automatically
3. **Toggles network selection**: Disables and re-enables "Select automatically"
4. **Selects network operator**: Automatically selects an available network
5. **Completes the cycle**: Returns to automatic selection mode

**Note**: The Accessibility Service requires a one-time permission grant in Android's Accessibility Settings. Once enabled, the app can perform the full network reset automatically without root access.

## Requirements

- Android 9.0 (API level 28) or higher
- Device with telephony capabilities (mobile network support)
- Location permission (for network scanning)
- Android Studio Arctic Fox or newer (for development)
- JDK 17 or higher (for building)

**Note for CI/CD**: Building this project requires access to Google Maven Repository (dl.google.com) and Gradle repositories.

## Installation

### Option 1: Build from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/CrhistIsKingSoftware/android-mobile-network-reset.git
   cd android-mobile-network-reset
   ```

2. Open the project in Android Studio

3. Build the project:
   ```bash
   ./gradlew build
   ```

4. Install on your device:
   ```bash
   ./gradlew installDebug
   ```

### Option 2: Download APK

Download the latest APK from the [Releases](https://github.com/CrhistIsKingSoftware/android-mobile-network-reset/releases) page.

**Note**: The APK is signed with a debug certificate (suitable for open-source projects). You may need to enable "Install from Unknown Sources" on your device to install it.

## Usage

### For Non-Rooted Devices (Recommended):
1. Open the **Mobile Network Reset** app
2. In the **Accessibility Service** section, tap **"Enable Accessibility Service"**
3. Find **"Mobile Network Reset"** in the accessibility services list and enable it
4. Return to the app
5. Tap **"Automated Reset (via Accessibility)"** button
6. The app will automatically navigate through Settings and reset your network

### For Rooted Devices/System Apps:
1. Open the **Mobile Network Reset** app
2. Read the instructions on the main screen
3. Tap the **"Reset Network"** button
4. The app will automatically reset your network using system APIs

### Manual Approach:
1. Tap **"Open Network Settings"** to access network settings directly
2. Follow the on-screen guidance to complete the reset process manually

## Permissions

The app requires the following permissions:

- `ACCESS_NETWORK_STATE`: To check network connectivity status
- `CHANGE_NETWORK_STATE`: To modify network settings
- `MODIFY_PHONE_STATE`: To change network operator selection (system-level permission)
- `ACCESS_FINE_LOCATION`: To scan for available network operators
- `ACCESS_COARSE_LOCATION`: Alternative location permission

**Note**: The `MODIFY_PHONE_STATE` permission is a system-level permission. On rooted devices or when installed as a system app, the automated network reset will work fully. On regular devices, the app uses an **Accessibility Service** to automate the process through the Settings UI, or can fall back to providing guided manual instructions.

## Development

### Project Structure

```
android-mobile-network-reset/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/christistking/mobilenetworkreset/
│   │       │   └── MainActivity.kt
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   │   └── activity_main.xml
│   │       │   ├── values/
│   │       │   │   ├── colors.xml
│   │       │   │   ├── strings.xml
│   │       │   │   └── themes.xml
│   │       │   └── drawable/
│   │       └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
├── settings.gradle
└── README.md
```

### Building

Build the debug APK:
```bash
./gradlew assembleDebug
```

Build the release APK:
```bash
./gradlew assembleRelease
```

### Testing

Run unit tests:
```bash
./gradlew test
```

Run instrumented tests:
```bash
./gradlew connectedAndroidTest
```

### Linting

Run Android Lint:
```bash
./gradlew lint
```

## CI/CD

This project uses GitHub Actions for continuous integration and deployment.

### Workflows

- **Build**: Automatically builds the app on every push and pull request
- **Release**: Creates releases with APK artifacts when tags are pushed

### Setting Up CI/CD

The CI/CD pipeline is configured in `.github/workflows/` and includes:

1. **Automated Builds**: Triggered on push and PR to main branch
2. **APK Generation**: Debug APK created for every build
3. **Release Automation**: Release APK created and uploaded when version tags are pushed

To create a release:
```bash
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

## Limitations

- **Direct API approach**: Requires system-level access (rooted devices or system app installation)
- **Accessibility Service approach**: Requires one-time permission grant in Accessibility Settings
- **UI Automation**: Accessibility service may need adjustment if device manufacturer changes Settings UI
- The app may not work on all devices or Android versions
- Some device manufacturers may restrict access to network operator settings
- Requires Android 9.0 (API 28) or higher for the network selection APIs
- Accessibility service automation depends on Settings UI text patterns and may vary by device language

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is open source and available under the MIT License.

## Troubleshooting

### APK Installation Issues

If you encounter "invalid APK" or installation errors:

1. **Ensure you have the latest APK**: Download from the [Releases](https://github.com/CrhistIsKingSoftware/android-mobile-network-reset/releases) page
2. **Enable installation from unknown sources**: Required for apps not from Play Store
3. **Check Android version**: Requires Android 9.0 (API 28) or higher
4. **Verify device compatibility**: Device must have mobile network (SIM card) support
5. **Grant location permission**: Required for network scanning when the app requests it

For more details about APK signing and build configuration, see [APK_SIGNING_FIX.md](APK_SIGNING_FIX.md).

## Support

If you encounter any issues or have questions, please:
- Open an issue on GitHub
- Check existing issues for solutions
- Review the documentation

## Acknowledgments

- Developed to solve connectivity issues experienced in Brazil and other regions with challenging mobile network conditions
- Built with Android Jetpack and Material Design components
