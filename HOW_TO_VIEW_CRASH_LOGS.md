# Quick Guide: How to View App Crash Logs

This guide shows you how to view crash logs for the Mobile Network Reset app to diagnose any issues.

## Prerequisites

- Android device with USB debugging enabled
- ADB (Android Debug Bridge) installed on your computer
- OR Android Studio installed

## Method 1: Using ADB Command Line

### 1. Connect Your Device
```bash
# Connect your Android device via USB
# Enable USB debugging on the device (Settings → Developer options → USB debugging)

# Verify device is connected
adb devices
```

### 2. View Live Logs
```bash
# View all app logs (recommended)
adb logcat -s MainActivity:* NetworkResetA11yService:*

# View only error logs
adb logcat -s MainActivity:E NetworkResetA11yService:E

# View only debug and error logs
adb logcat -s MainActivity:D MainActivity:E NetworkResetA11yService:D NetworkResetA11yService:E
```

### 3. Save Logs to File
```bash
# Save all app logs to a file
adb logcat -s MainActivity:* NetworkResetA11yService:* > app_logs.txt

# Clear old logs and start fresh
adb logcat -c
adb logcat -s MainActivity:* NetworkResetA11yService:* > app_logs.txt
```

### 4. View Saved Logs
```bash
# On Windows
notepad app_logs.txt

# On macOS/Linux
cat app_logs.txt
# or
less app_logs.txt
```

## Method 2: Using Android Studio

### 1. Open Android Studio
- Launch Android Studio
- Open the project or just use it for viewing logs

### 2. Open Logcat
- Click **View** → **Tool Windows** → **Logcat**
- Or press **Alt+6** (Windows/Linux) or **Cmd+6** (macOS)

### 3. Filter Logs
In the Logcat window:
1. Select your device from the dropdown
2. In the search box, enter: `MainActivity|NetworkResetA11yService`
3. OR use the filter dropdown and select **Edit Filter Configuration**:
   - **Filter Name**: MobileNetworkReset
   - **Log Tag**: `MainActivity|NetworkResetA11yService`
   - **Log Level**: Verbose (or Debug/Error based on need)
   - Click **OK**

### 4. Save Logs
- Right-click in the Logcat window
- Select **Export to File**
- Choose location and save

## What to Look For

### Successful App Startup
```
D/MainActivity: onCreate: Starting MainActivity initialization
D/MainActivity: onCreate: All views initialized successfully
D/MainActivity: onCreate: TelephonyManager initialized successfully
D/MainActivity: onCreate: AccessibilityManager initialized successfully
D/MainActivity: onCreate: MainActivity initialization completed successfully
```

### App Crash
```
E/MainActivity: === UNCAUGHT EXCEPTION ===
E/MainActivity: Thread: main
E/MainActivity: Exception: java.lang.NullPointerException
E/MainActivity: Message: Attempt to invoke virtual method...
E/MainActivity: Stack trace:
E/MainActivity:   at com.christistking.mobilenetworkreset.MainActivity.onCreate(MainActivity.kt:123)
E/MainActivity:   at android.app.Activity.performCreate...
E/MainActivity: === END UNCAUGHT EXCEPTION ===
```

### Service Unavailable
```
E/MainActivity: onCreate: TelephonyManager is null - telephony service unavailable
```
This is normal on tablets or emulators without telephony support.

### Network Reset Operation
```
D/MainActivity: performNetworkReset: Starting network reset
D/MainActivity: performNetworkReset: Step 1 - Disabling automatic selection
D/MainActivity: performNetworkScanAndSelect: Starting network scan
D/MainActivity: performNetworkScanAndSelect: Current operator: 310260
D/MainActivity: reEnableAutomaticSelection: Re-enabling automatic selection
D/MainActivity: reEnableAutomaticSelection: Network reset completed successfully
```

### Permission Issues
```
W/MainActivity: performNetworkReset: SecurityException - falling back to manual
```

## Log Levels Explained

- **D (Debug)**: Normal operations, informational messages
- **I (Info)**: Important informational messages
- **W (Warning)**: Potential issues, fallback scenarios
- **E (Error)**: Errors, exceptions, failures
- **V (Verbose)**: Detailed low-level information

## Troubleshooting Common Issues

### No Logs Appearing
```bash
# Make sure device is connected
adb devices

# Restart ADB server
adb kill-server
adb start-server

# Try again
adb logcat -s MainActivity:*
```

### Too Many Logs
```bash
# Clear old logs first
adb logcat -c

# Then start app and view new logs
adb logcat -s MainActivity:* NetworkResetA11yService:*
```

### Can't Find ADB
```bash
# Add Android SDK platform-tools to PATH
# Windows: Add C:\Users\YourName\AppData\Local\Android\Sdk\platform-tools
# macOS/Linux: Add ~/Library/Android/sdk/platform-tools or ~/Android/Sdk/platform-tools

# Or use full path
/path/to/android-sdk/platform-tools/adb logcat
```

## Reporting Issues

When reporting a crash or issue, please include:

1. **Device information**:
   - Device model (e.g., Samsung Galaxy S21)
   - Android version (e.g., Android 13)
   - Device type (phone/tablet)

2. **Log output**:
   - Run: `adb logcat -s MainActivity:* NetworkResetA11yService:* > app_logs.txt`
   - Reproduce the issue
   - Share the `app_logs.txt` file

3. **Steps to reproduce**:
   - What you were doing when the crash occurred
   - Can you reproduce it consistently?

## Advanced: Filtering by Time

```bash
# Save logs with timestamp
adb logcat -v time -s MainActivity:* > app_logs.txt

# Filter logs from specific time
adb logcat -v time -t "12-30 15:45:00.000"
```

## Quick Commands Reference

```bash
# View all app logs
adb logcat -s MainActivity:* NetworkResetA11yService:*

# View only errors
adb logcat -s MainActivity:E NetworkResetA11yService:E

# Clear and start fresh
adb logcat -c && adb logcat -s MainActivity:*

# Save to file
adb logcat -s MainActivity:* > app_logs.txt

# View with timestamp
adb logcat -v time -s MainActivity:*

# View last 100 lines
adb logcat -t 100 -s MainActivity:*
```

## Need Help?

If you're still having trouble viewing logs or diagnosing issues:
1. Check that USB debugging is enabled on your device
2. Try restarting ADB: `adb kill-server && adb start-server`
3. Try using Android Studio's Logcat instead
4. Ask for help in the project's issue tracker with details about your setup
