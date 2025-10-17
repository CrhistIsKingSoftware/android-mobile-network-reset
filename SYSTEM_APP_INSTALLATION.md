# System App Installation Guide

## Overview

Installing this app as a system app grants it the `MODIFY_PHONE_STATE` permission, enabling full automation of the network reset process without user interaction. This guide explains the methods, requirements, and limitations.

## Important Note: User Interaction Limitation

**There is NO way to programmatically request a user to install an app as a system app through normal Android APIs.** This is a fundamental Android security restriction. System app installation requires:
- Root access (ADB root or device root)
- Manual file system modification
- Device reboot

Regular apps cannot escalate themselves to system app status without these prerequisites.

## Methods to Install as System App

### Method 1: Using ADB with Root Access (Recommended for Development)

This method requires:
- Android device with USB debugging enabled
- ADB (Android Debug Bridge) installed on computer
- Device with unlocked bootloader and root access

**Steps:**

```bash
# 1. Build the release APK
./gradlew assembleRelease

# 2. Connect device via USB and verify ADB connection
adb devices

# 3. Restart ADB with root privileges
adb root

# 4. Remount system partition as read-write
adb remount

# 5. Push APK to system app directory
adb push app/build/outputs/apk/release/app-release.apk /system/priv-app/MobileNetworkReset/MobileNetworkReset.apk

# 6. Set proper permissions
adb shell chmod 644 /system/priv-app/MobileNetworkReset/MobileNetworkReset.apk

# 7. Reboot device
adb reboot

# 8. After reboot, verify installation
adb shell pm list packages | grep mobilenetworkreset
```

**Location Options:**
- `/system/priv-app/` - For privileged system apps (recommended for this app)
- `/system/app/` - For regular system apps

### Method 2: Using Recovery Mode (TWRP)

For devices with custom recovery like TWRP:

1. Build release APK
2. Create a flashable zip package with the APK
3. Boot into TWRP recovery
4. Flash the zip package
5. Reboot system

**Example flashable zip structure:**
```
flashable-zip/
├── META-INF/
│   └── com/google/android/
│       ├── update-binary
│       └── updater-script
└── system/
    └── priv-app/
        └── MobileNetworkReset/
            └── MobileNetworkReset.apk
```

### Method 3: Custom ROM Integration

For ROM developers or users building custom ROMs:

1. Add APK to ROM source tree:
   ```
   vendor/[manufacturer]/[device]/prebuilt/priv-app/MobileNetworkReset/
   ```

2. Add to device makefile:
   ```makefile
   PRODUCT_PACKAGES += \
       MobileNetworkReset
   ```

3. Build ROM with the app included

### Method 4: Magisk Module (Root Users)

Create a Magisk module for easy installation on rooted devices:

**Module structure:**
```
MobileNetworkReset-Module/
├── module.prop
├── system/
│   └── priv-app/
│       └── MobileNetworkReset/
│           └── MobileNetworkReset.apk
└── META-INF/
    └── com/google/android/
        └── update-binary
```

**module.prop:**
```ini
id=mobile_network_reset
name=Mobile Network Reset
version=v1.0
versionCode=1
author=CrhistIsKing
description=System app for automatic mobile network reset
```

Install via Magisk Manager app.

## Required Manifest Changes

The app already has the necessary permissions declared:

```xml
<uses-permission android:name="android.permission.MODIFY_PHONE_STATE"
    tools:ignore="ProtectedPermissions" />
```

When installed as a system app, this permission is automatically granted.

## Verification Steps

After installing as system app, verify it works:

```bash
# 1. Check app is in system partition
adb shell pm path com.christistking.mobilenetworkreset

# Expected output should show /system/priv-app/ path
# package:/system/priv-app/MobileNetworkReset/MobileNetworkReset.apk

# 2. Check permissions granted
adb shell dumpsys package com.christistking.mobilenetworkreset | grep MODIFY_PHONE_STATE

# Should show: granted=true

# 3. Test app functionality
# Open app and tap "Reset Network" button
# Should show automated steps without opening settings
```

## User Instructions for System App Installation

Since there's no programmatic way to request system app installation, you should provide clear documentation for users:

### For End Users (Include in README or User Guide):

**Requirements:**
- Rooted Android device (Android 9.0+)
- ADB tools installed on computer
- USB debugging enabled

**Warning:** 
⚠️ Installing apps as system apps requires root access and modifies system partition. This:
- Voids warranty on most devices
- Requires technical knowledge
- Can brick your device if done incorrectly
- May prevent OTA updates

**For users without root:** The app will still work but will open network settings for manual completion instead of full automation.

## Alternative: AccessibilityService (Future Enhancement)

As an alternative to system app installation, consider implementing an AccessibilityService in a future version. This would allow:
- Automation without root access
- User grants permission through Android settings
- Works on unrooted devices

However, this requires:
- User to manually enable the accessibility service
- Different implementation approach
- May be restricted by some manufacturers

## Troubleshooting

### App doesn't have MODIFY_PHONE_STATE permission after system installation

**Solution:**
```bash
# Check if app is really in system partition
adb shell pm path com.christistking.mobilenetworkreset

# If not in /system/, reinstall following the steps above

# Clear app data and reboot
adb shell pm clear com.christistking.mobilenetworkreset
adb reboot
```

### App crashes when calling TelephonyManager methods

**Possible causes:**
1. App not installed in `/system/priv-app/` (use priv-app for privileged permissions)
2. SELinux denials (check logcat)
3. Device manufacturer restrictions

**Check logs:**
```bash
adb logcat | grep -i "mobilenetworkreset\|telephony\|permission"
```

### Cannot remount system partition

**For Android 10+** (uses dynamic partitions):
```bash
adb root
adb disable-verity
adb reboot
# After reboot
adb root
adb remount
```

## Security Considerations

### For Users
- Only install from trusted sources
- Verify APK signature before installation
- Understand the risks of root access
- Keep device security updated

### For Developers
- Sign APK with proper certificates
- Don't include system app installation in Play Store version
- Clearly document security implications
- Provide alternative (non-system) version for regular users

## Conclusion

Installing as a system app enables full automation but requires:
- Root access (cannot be requested programmatically)
- Manual installation process
- Technical knowledge
- Acceptance of security risks

The current implementation correctly handles both scenarios:
- ✅ Full automation when installed as system app
- ✅ Graceful fallback for regular installation

This provides the best user experience across all installation methods.

## References

- [Android Platform Permissions](https://developer.android.com/guide/topics/permissions/overview#platform-permissions)
- [System App Installation](https://source.android.com/docs/core/architecture/bootloader/system-as-root)
- [ADB Commands Reference](https://developer.android.com/studio/command-line/adb)
