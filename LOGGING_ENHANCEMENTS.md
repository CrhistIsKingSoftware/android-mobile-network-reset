# Logging Enhancements Documentation

## Overview

The Mobile Network Reset app includes comprehensive logging capabilities that:
- Save logs to files on the Android device
- Automatically upload logs to Azure Blob Storage (when configured at build time)
- Provide a user interface for viewing and sharing logs
- Enable crash detection and automatic upload

## Features

### 1. File-based Logging

All application logs are saved to internal storage in addition to Android's logcat system. This provides persistent logging that survives app restarts and device reboots.

**Key Features:**
- Automatic log rotation when files exceed 5MB
- Maintains up to 5 historical log files
- Thread-safe log writing
- Logs are stored in the app's internal storage (`/data/data/com.christistking.mobilenetworkreset/files/logs/`)

### 2. Log Viewer Interface

A "View Logs" button on the main activity opens a dedicated logs viewer where users can:
- View all application logs in a scrollable interface
- Refresh logs in real-time
- Clear all logs
- Share logs via system share dialog (email, drive, messaging apps, etc.)
- Upload logs to cloud storage (if configured at build time)

### 3. Cloud Log Upload (Azure Blob Storage)

**Important**: Cloud upload is configured at build time by the app maintainer, not by end users.

The app automatically uploads logs to Azure Blob Storage when:
- Built with proper Azure credentials (via GitHub Secrets)
- Every 24 hours (automatic)
- Immediately when a crash is detected

**Azure Free Tier Benefits:**
- 5GB storage included free
- 20,000 read/write operations per month
- Perfect for application logging needs

**For Developers/Maintainers**: See [AZURE_DEPLOYMENT_GUIDE.md](AZURE_DEPLOYMENT_GUIDE.md) for setup instructions.

**For End Users**: If the "Upload to Cloud" button is disabled, the app was built without cloud credentials. You can still use the "Share" button to export logs manually.

### 4. Auto-Upload Feature

When the app is built with Azure credentials:
- Logs automatically upload every 24 hours when the app is opened
- Crash logs are uploaded immediately when detected
- All log files are combined into a single timestamped file
- Upload happens in the background without user interaction

### 5. Log Sharing

Users can share logs with developers or support teams:
- Tap "Share" in the logs viewer
- Choose how to share (Email, Drive, Messaging apps, etc.)
- Logs are exported as a text file attachment

## Implementation Details

### LogManager Class

The `LogManager` class provides centralized log management:

```kotlin
// Initialize (singleton pattern)
val logManager = LogManager.getInstance(context)

// Log messages
logManager.d(TAG, "Debug message")
logManager.i(TAG, "Info message")
logManager.w(TAG, "Warning message")
logManager.e(TAG, "Error message", exception)

// Get logs
val currentLog = logManager.getCurrentLogContent()
val allLogs = logManager.getAllLogsContent()
val logFiles = logManager.getLogFiles()

// Clear logs
logManager.clearLogs()

// Export logs
logManager.exportLogs(outputFile)
```

### CloudLogManager Class

The `CloudLogManager` class handles cloud upload operations:

```kotlin
val cloudLogManager = CloudLogManager(context, logManager)

// Check if configured (at build time)
val isConfigured = cloudLogManager.isConfigured()

// Manual upload (if configured)
cloudLogManager.uploadAllLogs()
cloudLogManager.uploadCurrentLog()

// Auto-upload check
cloudLogManager.performAutoUploadIfNeeded()
```

Note: Configuration is done at build time via BuildConfig, not at runtime by users.

### CloudLogService Interface

The `CloudLogService` interface allows for different cloud storage implementations:

```kotlin
interface CloudLogService {
    suspend fun uploadLog(logFile: File): Boolean
    suspend fun uploadLogContent(content: String, fileName: String): Boolean
}
```

Current implementation: `AzureBlobStorageService`

## Log Format

Logs are formatted with the following structure:

```
[2024-01-15 10:30:45.123] [INFO] [MainActivity] User tapped reset button
[2024-01-15 10:30:45.456] [DEBUG] [MainActivity] Starting network reset
[2024-01-15 10:30:46.789] [ERROR] [MainActivity] Network reset failed
Exception: java.lang.SecurityException
Message: Permission denied
Stack trace:
  at com.christistking.mobilenetworkreset.MainActivity.performNetworkReset(MainActivity.kt:287)
  ...
```

Each log entry includes:
- Timestamp with millisecond precision
- Log level (DEBUG, INFO, WARN, ERROR)
- Tag (typically the class name)
- Message
- Exception details (if applicable)

## Security Considerations

1. **Log Storage:**
   - Logs are stored in the app's internal storage (not accessible by other apps)
   - No sensitive data (passwords, tokens) should be logged
   - Logs are cleared when the app is uninstalled

2. **Cloud Upload:**
   - Azure credentials are embedded at build time (not stored on device)
   - Communication with Azure uses HTTPS only
   - SAS tokens configured with limited permissions (write-only recommended)
   - Tokens set with appropriate expiry dates

3. **Log Sharing:**
   - Users are in control of when and how logs are shared
   - Logs shared via system share dialog use Android's FileProvider for secure file sharing

## Troubleshooting

### Cloud Upload Issues

**"Upload to Cloud" button is disabled:**
- The app was built without Azure credentials
- Use the "Share" button to export logs manually
- Contact the app maintainer for a build with cloud logging enabled

**Error: "Failed to upload logs. Please try again."**
- Check your internet connection
- Verify you're connected to WiFi or have mobile data enabled
- Try again later

**Upload works but logs don't appear in Azure:**
- Verify with app maintainer that Azure container exists
- Check that credentials haven't expired

### Log File Issues

**Logs not appearing:**
- Check that LogManager is initialized properly
- Verify that log methods are being called
- Check app permissions

**Logs not rotating:**
- Current log file hasn't reached 5MB limit yet
- Check available storage space

## Best Practices

1. **Log Level Usage:**
   - DEBUG: Detailed information for debugging
   - INFO: General informational messages
   - WARN: Warning messages for potentially harmful situations
   - ERROR: Error messages for failures that need attention

2. **What to Log:**
   - Application lifecycle events
   - User actions
   - Network operations
   - Errors and exceptions
   - State changes

3. **What NOT to Log:**
   - Passwords or credentials
   - Personal identifiable information (PII)
   - Financial data
   - Health information
   - Any sensitive user data

## For Developers/Maintainers

See [AZURE_DEPLOYMENT_GUIDE.md](AZURE_DEPLOYMENT_GUIDE.md) for:
- Setting up Azure Blob Storage
- Configuring GitHub Secrets
- Building with cloud logging enabled
- Managing and rotating credentials

## Future Enhancements

Potential future improvements:
- Support for other cloud services (AWS S3, Google Cloud Storage)
- Log filtering by level or tag
- Search functionality in log viewer
- Export logs in different formats (JSON, CSV)
- Encrypted log storage
- Log compression before upload
- Configurable log retention periods
