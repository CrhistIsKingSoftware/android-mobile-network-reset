# Quick Setup Guide - Accessibility Service

## For Non-Rooted Devices

This guide will help you set up the automated network reset feature using the Accessibility Service.

### Step 1: Install the App

1. Download the APK from the [Releases](https://github.com/CrhistIsKingSoftware/android-mobile-network-reset/releases) page
2. Install the APK on your device
3. Open the app

### Step 2: Enable Accessibility Service (One-Time Setup)

1. In the app, locate the **"Accessibility Service"** section (blue card)
2. Read the instructions carefully
3. Tap **"Enable Accessibility Service"** button
4. Your device will open the **Accessibility Settings** page
5. Scroll down and find **"Mobile Network Reset"** in the list
6. Tap on **"Mobile Network Reset"**
7. Toggle the switch to **ON**
8. Read the permission dialog and tap **"Allow"** or **"OK"**
9. Press the back button to return to the app

### Step 3: Verify Setup

After returning to the app:
- The status should show: **"✓ Enabled - Automated reset available"**
- The **"Automated Reset (via Accessibility)"** button should be enabled (not grayed out)
- The **"Enable Accessibility Service"** button should be disabled

### Step 4: Use Automated Reset

Whenever you need to reset your network:

1. Open the **Mobile Network Reset** app
2. Tap **"Automated Reset (via Accessibility)"** button
3. The app will show "Starting automated reset..."
4. Network Operator Settings will open automatically
5. Watch as the app automatically:
   - Disables "Select automatically"
   - Selects a network operator
   - Re-enables "Select automatically"
6. Wait for the completion message
7. Your network should now be reset!

## Troubleshooting

### "Please enable the accessibility service first"

**Problem**: You tapped the automated reset button but got this message.

**Solution**:
1. Check if the status shows "✗ Disabled"
2. Follow Step 2 above to enable the service
3. Make sure you toggled the switch to ON in Settings

### Service Not Working

**Problem**: The automated reset starts but doesn't complete.

**Solution**:
1. Return to the app
2. Wait 30 seconds for timeout
3. Try the automated reset again
4. If still not working, use the "Open Network Settings" button for manual reset

### Can't Find "Mobile Network Reset" in Accessibility Settings

**Problem**: Can't locate the app in the accessibility services list.

**Solution**:
1. Make sure the app is properly installed
2. Try closing and reopening the Accessibility Settings
3. Scroll through the entire list carefully
4. The app may be under "Downloaded apps" or "Installed services" section

### Automated Reset Takes Too Long

**Problem**: The process seems to be taking more than 30 seconds.

**Solution**:
- This is normal on some devices
- The service uses delays to ensure UI elements load
- Wait for the completion message
- If it doesn't complete after 1 minute, return to the app

### Service Stops Working After Android Update

**Problem**: The service worked before but stopped after updating Android.

**Solution**:
1. Disable the accessibility service in Settings
2. Re-enable it following Step 2
3. If still not working, reinstall the app
4. Report the issue on GitHub with your Android version

## Tips for Best Results

1. **Grant Location Permission**: When the app first asks for location permission, grant it. This is needed for network scanning.

2. **Stable Screen**: Keep your device steady during the automated reset. Don't switch apps or lock the screen.

3. **First Time**: The first automated reset may take longer as the service learns your device's UI.

4. **Regular Updates**: Check for app updates periodically for improvements and bug fixes.

5. **Fallback Available**: If automation doesn't work, you can always use the "Open Network Settings" button for manual reset.

## How It Works

The accessibility service automates the manual process you would normally do:

```
Normal Manual Process:          Automated Process:
1. Open Settings                1. Tap one button
2. Navigate to Mobile Networks  2. Watch it happen automatically
3. Open Network Operators       3. Done!
4. Disable "Select automatically"
5. Tap a network
6. Wait for connection
7. Go back
8. Enable "Select automatically"
9. Done!
```

## Privacy & Security

- ✅ **No data collection**: The app doesn't collect or send any data
- ✅ **Local operation**: Everything happens on your device
- ✅ **Open source**: Code is publicly available for review
- ✅ **Transparent**: All actions are visible in Settings
- ✅ **User control**: You can disable the service anytime

## Need Help?

- **Check the app**: Status messages provide helpful information
- **Read the docs**: See [ACCESSIBILITY_SERVICE.md](ACCESSIBILITY_SERVICE.md) for technical details
- **Report issues**: Open an issue on [GitHub](https://github.com/CrhistIsKingSoftware/android-mobile-network-reset/issues)
- **Ask questions**: Use GitHub Discussions for questions

## Alternative Methods

If the accessibility service doesn't work for you:

### Method 1: Direct API (Rooted Devices Only)
Use the standard "Reset Network" button if your device is rooted or the app is installed as a system app.

### Method 2: Manual Reset (All Devices)
1. Tap "Open Network Settings"
2. Follow the on-screen instructions
3. Manually complete the 4 steps

---

**Enjoy hassle-free network resets!** 🎉
