# Accessibility Service Implementation

## Overview

The Mobile Network Reset app now includes an **Accessibility Service** that enables full automation of the network reset process on non-rooted devices. This service uses Android's Accessibility APIs to interact with the Settings UI programmatically.

## How It Works

### Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      User Interface                          │
│                     (MainActivity)                           │
└────────────┬─────────────────────────────────┬──────────────┘
             │                                  │
             │ 1. User taps button              │ 6. Receives status
             │                                  │    updates
             ▼                                  ▼
┌──────────────────────────┐          ┌──────────────────────┐
│  Open Settings Screen    │          │  Broadcast Receiver  │
│  (Network Operators)     │          │                      │
└────────────┬─────────────┘          └──────────▲───────────┘
             │                                    │
             │ 2. Settings opens                  │ 5. Broadcasts
             │                                    │    status
             ▼                                    │
┌─────────────────────────────────────────────────┴───────────┐
│           NetworkResetAccessibilityService                   │
│                                                              │
│  3. Observes UI events                                       │
│  4. Performs automated actions:                              │
│     - Finds "Select automatically" toggle                    │
│     - Disables it                                           │
│     - Selects a network operator                            │
│     - Re-enables "Select automatically"                     │
└──────────────────────────────────────────────────────────────┘
```

### Process Flow

```
1. User Setup (One-Time)
   ├─► User enables accessibility service in Android Settings
   ├─► Grants permission to "Mobile Network Reset"
   └─► Returns to app

2. Automated Reset Process
   ├─► User taps "Automated Reset" button
   ├─► App opens Network Operator Settings
   ├─► Accessibility service activates
   │   ├─► Step 1: Waits for Settings screen
   │   ├─► Step 2: Finds "Select automatically" toggle
   │   ├─► Step 3: Disables automatic selection
   │   ├─► Step 4: Selects first available network
   │   ├─► Step 5: Waits for selection to complete
   │   ├─► Step 6: Re-enables automatic selection
   │   └─► Step 7: Broadcasts completion status
   └─► User receives completion notification
```

## Implementation Details

### Key Components

#### 1. NetworkResetAccessibilityService

The main accessibility service class that:
- Observes accessibility events from the Settings app
- Navigates through the UI using node traversal
- Clicks on UI elements programmatically
- Manages the state machine for the reset process
- Broadcasts status updates to the main app

**Key Methods:**
- `onAccessibilityEvent()`: Processes UI events
- `handleSettingsScreen()`: Finds network operator option
- `handleNetworkOperatorsScreen()`: Finds and toggles auto-select
- `handleSelectNetwork()`: Selects a network operator
- `handleEnableAutoSelect()`: Re-enables automatic selection

#### 2. MainActivity Integration

Enhanced to support accessibility service:
- Checks if accessibility service is enabled
- Displays service status in UI
- Provides button to enable service
- Triggers automated reset when service is active
- Receives and displays status updates via broadcast

#### 3. Configuration

**accessibility_service_config.xml:**
```xml
- Event Types: Window state changes, content changes, clicks
- Feedback Type: Generic feedback
- Flags: Include unimportant views, report view IDs
- Can Retrieve Window Content: true
```

### Text Pattern Matching

The service uses text patterns to identify UI elements:

**Auto-Select Patterns:**
- "select automatically"
- "automatically select"
- "choose automatically"
- "automatic selection"
- "select network automatically"

**Network Operator Patterns:**
- "network operators"
- "mobile networks"
- "carrier"
- "operators"

These patterns work with English UI. For other languages, additional patterns would need to be added.

### State Machine

The service uses a state machine with the following states:

1. **IDLE**: Not processing
2. **WAITING_FOR_SETTINGS**: Waiting for Settings screen to load
3. **WAITING_FOR_NETWORK_OPERATORS**: Waiting for network operators screen
4. **DISABLING_AUTO_SELECT**: Toggling off automatic selection
5. **SELECTING_NETWORK**: Selecting a network operator
6. **GOING_BACK**: Navigating back after selection
7. **ENABLING_AUTO_SELECT**: Re-enabling automatic selection
8. **COMPLETE**: Process completed successfully
9. **ERROR**: An error occurred

## User Experience

### Setup Process

1. User opens the app for the first time
2. Sees "Accessibility Service (Non-Rooted Devices)" card
3. Reads instructions for enabling the service
4. Taps "Enable Accessibility Service" button
5. Android Settings opens to Accessibility page
6. User finds "Mobile Network Reset" in the list
7. User toggles it ON
8. User accepts the permission dialog
9. User returns to the app
10. Status updates to "✓ Enabled - Automated reset available"

### Reset Process

1. User taps "Automated Reset (via Accessibility)" button
2. App shows "Starting automated reset..." status
3. Network Operator Settings opens
4. Accessibility service begins automation
5. User sees status updates:
   - "Found network operators option"
   - "Disabling automatic selection"
   - "Selected network operator"
   - "Network reset complete!"
6. Settings automatically returns to normal state
7. App shows completion message

## Security & Privacy

### Permissions Required

- **BIND_ACCESSIBILITY_SERVICE**: System permission for accessibility services
- User must explicitly grant in Settings (cannot be granted programmatically)

### What the Service Can Access

- **Only Settings app UI**: The service only activates and interacts with Settings
- **Read UI elements**: Can read text and element properties
- **Perform clicks**: Can trigger click actions on UI elements
- **No data access**: Does not access personal data, messages, or other apps

### Privacy Considerations

- The service only processes when explicitly triggered by user
- Does not run in background continuously
- Only interacts with Network Operator settings
- All actions are transparent and visible to user
- Can be disabled at any time in Android Settings

## Limitations

### Device Variations

Different device manufacturers may:
- Use different text labels in Settings
- Have different UI structures
- Restrict accessibility service capabilities
- Use different layouts for network operator settings

**Solution**: The service uses multiple text patterns and flexible node traversal to handle variations.

### Language Support

Currently supports English UI text patterns.

**Future Enhancement**: Add text patterns for other languages (Portuguese, Spanish, etc.)

### Settings UI Changes

Android updates or manufacturer customizations may change Settings UI.

**Solution**: Service uses flexible pattern matching rather than hardcoded element IDs.

## Testing

### Test Cases

1. **Service Enable/Disable**
   - Enable service in Settings
   - Verify status updates in app
   - Disable service
   - Verify app disables automated button

2. **Automated Reset**
   - Trigger reset with service enabled
   - Verify each step completes
   - Verify status updates appear
   - Verify network selection changes

3. **Error Handling**
   - Test with service disabled
   - Test with airplane mode on
   - Test with no SIM card
   - Verify appropriate error messages

4. **UI Variations**
   - Test on different Android versions (9-14)
   - Test on different device manufacturers
   - Test in different languages (if patterns added)

### Manual Testing Steps

1. Install app on non-rooted device
2. Enable accessibility service
3. Return to app and verify status
4. Tap automated reset button
5. Observe Settings navigation
6. Verify network reset completes
7. Return to app and verify status

## Troubleshooting

### Service Not Working

**Issue**: Automated reset doesn't start
**Solution**: 
- Verify accessibility service is enabled in Settings
- Check app has proper permissions
- Restart the app

**Issue**: Service gets stuck at a step
**Solution**:
- Go back to the app
- Service will timeout after 30 seconds
- Try manual reset instead

**Issue**: Can't find UI elements
**Solution**:
- Device may use different text labels
- Try manual reset approach
- Report issue with device model for future support

### Performance Issues

**Issue**: Slow response
**Solution**:
- Service intentionally uses delays for UI to load
- Allow 10-15 seconds for full process
- Avoid interacting with device during automation

## Future Enhancements

1. **Multi-Language Support**: Add text patterns for multiple languages
2. **Smart Retry**: Automatically retry if a step fails
3. **UI Variant Detection**: Detect and adapt to different UI structures
4. **Offline Training**: Learn UI patterns from user's device
5. **Performance Optimization**: Reduce delays where possible
6. **Enhanced Logging**: Better diagnostic information for troubleshooting

## Developer Notes

### Adding New UI Patterns

To support a new text pattern or language:

1. Add pattern to `AUTO_SELECT_PATTERNS` or `NETWORK_OPERATOR_PATTERNS`
2. Test on device with that language
3. Update documentation

Example:
```kotlin
private val AUTO_SELECT_PATTERNS = arrayOf(
    "select automatically",
    "selecionar automaticamente",  // Portuguese
    "seleccionar automáticamente"  // Spanish
)
```

### Debugging

Enable verbose logging:
```kotlin
private const val TAG = "NetworkResetA11yService"
Log.d(TAG, "Event: ${event.eventType}, Step: $currentStep")
```

Use Android Studio's Logcat to view service activity:
```
adb logcat | grep NetworkResetA11yService
```

### Best Practices

1. Always recycle AccessibilityNodeInfo objects to prevent memory leaks
2. Use delays to allow UI to stabilize before actions
3. Broadcast status updates for user feedback
4. Handle edge cases gracefully
5. Test on multiple device manufacturers

## References

- [Android Accessibility Service API](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService)
- [Creating Accessibility Services](https://developer.android.com/guide/topics/ui/accessibility/service)
- [AccessibilityNodeInfo Documentation](https://developer.android.com/reference/android/view/accessibility/AccessibilityNodeInfo)
- [Accessibility Testing](https://developer.android.com/guide/topics/ui/accessibility/testing)
