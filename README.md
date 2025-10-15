# Mobile Network Reset - Android App

An Android application designed to help users in areas with poor mobile network connectivity (like some regions in Brazil) by automating the network operator reset process.

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

- **One-Tap Network Reset**: Simple button to initiate the network reset process
- **User Guidance**: Clear instructions and step-by-step guidance
- **Direct Settings Access**: Quick access to network operator settings
- **Material Design**: Modern, intuitive user interface

## How It Works

The app provides a guided approach to reset your mobile network:

1. **Disables automatic network selection**: Toggles off auto-select mode
2. **Scans for available networks**: Searches for nearby mobile operators
3. **Selects the first available network**: Connects to an available operator
4. **Re-enables automatic network selection**: Returns to auto-select mode

## Requirements

- Android 8.0 (API level 26) or higher
- Device with telephony capabilities (mobile network support)

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

## Usage

1. Open the **Mobile Network Reset** app
2. Read the instructions on the main screen
3. Tap the **"Reset Network"** button
4. Follow the on-screen guidance to complete the reset process
5. Or tap **"Open Network Settings"** to access network settings directly

## Permissions

The app requires the following permissions:

- `ACCESS_NETWORK_STATE`: To check network connectivity status
- `CHANGE_NETWORK_STATE`: To modify network settings
- `MODIFY_PHONE_STATE`: To change network operator selection (system-level permission)

**Note**: Due to Android security restrictions, the `MODIFY_PHONE_STATE` permission is a system-level permission. Regular apps installed from the Play Store cannot automatically toggle network selection. The app provides guided access to the settings screen where users can perform the reset manually.

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

- Due to Android security policies, automatic network operator selection toggling requires system-level permissions
- The app may not work on all devices or Android versions
- Some device manufacturers may restrict access to network operator settings

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is open source and available under the MIT License.

## Support

If you encounter any issues or have questions, please:
- Open an issue on GitHub
- Check existing issues for solutions
- Review the documentation

## Acknowledgments

- Developed to solve connectivity issues experienced in Brazil and other regions with challenging mobile network conditions
- Built with Android Jetpack and Material Design components
