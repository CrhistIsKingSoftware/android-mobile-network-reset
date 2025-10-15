# Quick Start Guide

## For Users

### Installation

1. **Download the APK**
   - Go to the [Releases](https://github.com/CrhistIsKingSoftware/android-mobile-network-reset/releases) page
   - Download the latest APK file
   - Install on your Android device (enable "Install from Unknown Sources" if needed)

2. **Using the App**
   - Open "Mobile Network Reset" from your app drawer
   - Tap the "RESET NETWORK" button
   - Follow the on-screen instructions in the Settings app
   - Your network connection will be reset!

### When to Use

Use this app when:
- Mobile data is not connecting properly
- Network signal is weak or unstable
- You're in an area with poor tower coverage (common in some Brazilian regions)
- After entering/leaving airplane mode
- When switching between network operators

## For Developers

### Quick Build

```bash
# Clone the repository
git clone https://github.com/CrhistIsKingSoftware/android-mobile-network-reset.git
cd android-mobile-network-reset

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Or open in Android Studio and click Run
```

### Development Requirements

- **Android Studio:** Arctic Fox or newer
- **JDK:** 17 or higher
- **Android SDK:** API 26-34
- **Gradle:** 8.2 (included via wrapper)

### Project Structure

```
android-mobile-network-reset/
├── app/
│   └── src/main/
│       ├── java/com/christistking/mobilenetworkreset/
│       │   └── MainActivity.kt          # Main app logic
│       ├── res/
│       │   ├── layout/
│       │   │   └── activity_main.xml    # UI layout
│       │   └── values/                  # Strings, colors, themes
│       └── AndroidManifest.xml          # App configuration
├── .github/workflows/                   # CI/CD pipelines
├── build.gradle                         # Project configuration
└── [Documentation files]
```

### Making Changes

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Make your changes
4. Test thoroughly: `./gradlew test`
5. Run lint: `./gradlew lint`
6. Commit and push
7. Open a Pull Request

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

## For DevOps/CI

### GitHub Actions

The project includes automated workflows:

**CI Pipeline** (runs on push/PR):
```yaml
# .github/workflows/android-ci.yml
- Builds the app
- Runs tests
- Runs lint checks
- Uploads debug APK
```

**Release Pipeline** (runs on version tags):
```yaml
# .github/workflows/android-release.yml
- Builds release APK
- Creates GitHub release
- Uploads APK to release
```

### Creating a Release

```bash
# Tag the version
git tag -a v1.0.0 -m "Release version 1.0.0"

# Push the tag
git push origin v1.0.0

# GitHub Actions will automatically:
# 1. Build the release APK
# 2. Create a GitHub release
# 3. Upload the APK
```

### Environment Variables

For CI/CD, ensure:
- `GITHUB_TOKEN` is available (automatically provided by GitHub Actions)
- Android SDK is installed (handled by setup-java action)
- JDK 17 is available (configured in workflow)

## Troubleshooting

### Build Issues

**Problem:** "SDK location not found"
```bash
# Solution: Create local.properties
echo "sdk.dir=/path/to/Android/sdk" > local.properties
```

**Problem:** "Could not resolve dependencies"
```bash
# Solution: Check internet connection and try again
./gradlew --refresh-dependencies build
```

**Problem:** "Gradle version incompatible"
```bash
# Solution: Use the wrapper (recommended)
./gradlew wrapper --gradle-version 8.2
```

### Runtime Issues

**Problem:** Settings screen doesn't open
- **Solution:** Some devices restrict access to operator settings. The app will automatically fall back to general wireless settings.

**Problem:** App doesn't work
- **Cause:** Device may not have telephony capabilities (tablets without SIM)
- **Solution:** App requires a mobile network-capable device

**Problem:** Permission denied
- **Cause:** Android security restrictions
- **Solution:** This is expected. The app guides you through manual steps instead.

## FAQ

**Q: Does this require root access?**  
A: No, the app works on all Android devices without root.

**Q: Will this void my warranty?**  
A: No, this is a regular Android app with standard permissions.

**Q: Can it run in the background?**  
A: No, the app requires user interaction by design.

**Q: Does it work on tablets?**  
A: Only if the tablet has mobile network capabilities (SIM card support).

**Q: Which Android versions are supported?**  
A: Android 8.0 (API 26) and higher.

**Q: Is it safe?**  
A: Yes, the app only accesses network settings and doesn't collect any data.

## Getting Help

- **Issues:** [GitHub Issues](https://github.com/CrhistIsKingSoftware/android-mobile-network-reset/issues)
- **Discussions:** [GitHub Discussions](https://github.com/CrhistIsKingSoftware/android-mobile-network-reset/discussions)
- **Documentation:** See the various .md files in the repository

## Resources

- [README.md](README.md) - Complete documentation
- [IMPLEMENTATION.md](IMPLEMENTATION.md) - Technical details
- [CONTRIBUTING.md](CONTRIBUTING.md) - How to contribute
- [ARCHITECTURE.md](ARCHITECTURE.md) - System design
- [LICENSE](LICENSE) - MIT License

## Quick Commands Cheat Sheet

```bash
# Build
./gradlew assembleDebug              # Debug APK
./gradlew assembleRelease            # Release APK
./gradlew build                      # Full build

# Test & Check
./gradlew test                       # Run tests
./gradlew lint                       # Run lint
./gradlew check                      # Run all checks

# Install
./gradlew installDebug               # Install debug on device
./gradlew uninstallAll              # Uninstall from device

# Clean
./gradlew clean                      # Clean build files
```

---

**Ready to fix your mobile network issues? Download the app or build it yourself!**
