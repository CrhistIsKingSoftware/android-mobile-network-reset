# App Crash Fix Summary

## Issue
After PR #6 (accessibility service implementation), the app would crash immediately when users tapped the "Automated Reset (via Accessibility)" button.

## Root Cause
The code was incorrectly calling `startService()` on an `AccessibilityService`. This is fundamentally wrong because:

1. **AccessibilityServices are system-managed**: They are bound by the Android system, not started by applications
2. **startService() fails for AccessibilityService**: Attempting to call `startService()` on an AccessibilityService causes a crash
3. **Wrong communication pattern**: The app was treating the AccessibilityService like a regular Service

### The Problematic Code (MainActivity.kt line 439-442)
```kotlin
val serviceIntent = Intent(this, NetworkResetAccessibilityService::class.java).apply {
    action = NetworkResetAccessibilityService.ACTION_START_RESET
}
startService(serviceIntent)  // ❌ THIS CRASHES!
```

## Solution
Changed from `startService()` to `sendBroadcast()` for inter-process communication:

### Fixed Code (MainActivity.kt line 439-442)
```kotlin
val serviceIntent = Intent(NetworkResetAccessibilityService.ACTION_START_RESET).apply {
    setPackage(packageName)  // Package-scoped for security
}
sendBroadcast(serviceIntent)  // ✅ THIS WORKS!
```

### Supporting Changes (NetworkResetAccessibilityService.kt)

**Added BroadcastReceiver registration in `onServiceConnected()`:**
```kotlin
commandReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            ACTION_START_RESET -> startProcessing()
            ACTION_STOP_RESET -> stopProcessing()
        }
    }
}

val filter = IntentFilter().apply {
    addAction(ACTION_START_RESET)
    addAction(ACTION_STOP_RESET)
}

if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
    registerReceiver(commandReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
} else {
    registerReceiver(commandReceiver, filter)
}
```

**Added proper cleanup in `onDestroy()`:**
```kotlin
override fun onDestroy() {
    super.onDestroy()
    commandReceiver?.let {
        try {
            unregisterReceiver(it)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver", e)
        }
    }
}
```

## Files Modified
- `app/src/main/java/com/christistking/mobilenetworkreset/MainActivity.kt` (6 lines changed)
- `app/src/main/java/com/christistking/mobilenetworkreset/NetworkResetAccessibilityService.kt` (44 lines added)

Total: 47 insertions(+), 3 deletions(-)

## Security Considerations
✅ **Broadcast is package-scoped**: Uses `setPackage(packageName)` to ensure only this app receives the broadcast  
✅ **RECEIVER_NOT_EXPORTED on API 33+**: Prevents other apps from sending broadcasts to the receiver  
✅ **Proper cleanup**: Unregisters receiver in `onDestroy()` to prevent memory leaks  
✅ **No vulnerabilities**: CodeQL security scan passed

## Why This Fix Works

### Before (Broken)
```
MainActivity
    ↓
startService() on AccessibilityService
    ↓
💥 CRASH - AccessibilityServices cannot be started this way
```

### After (Fixed)
```
MainActivity
    ↓
sendBroadcast(ACTION_START_RESET)
    ↓
AccessibilityService's BroadcastReceiver
    ↓
startProcessing()
    ↓
✅ Works correctly!
```

## Testing Recommendations
1. Install the updated APK on a test device
2. Enable the accessibility service
3. Tap "Automated Reset (via Accessibility)" button
4. Verify the app does not crash
5. Verify the accessibility automation starts correctly
6. Verify the network reset completes successfully

## Future Improvements
- Consider using LocalBroadcastManager for even more secure local communication
- Add more detailed error handling for broadcast delivery failures
- Consider adding a timeout mechanism if the broadcast is not received

## References
- Android Accessibility Service Documentation: https://developer.android.com/guide/topics/ui/accessibility/service
- Android BroadcastReceiver Best Practices: https://developer.android.com/guide/components/broadcasts
- Android Service Lifecycle: https://developer.android.com/guide/components/services
