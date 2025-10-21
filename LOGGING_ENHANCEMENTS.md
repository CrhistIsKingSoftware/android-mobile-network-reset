# Logging Enhancements Documentation

## Overview

The Mobile Network Reset app has been enhanced with comprehensive logging capabilities that allow users to:
- Save logs to files on the Android device
- View and manage logs through a dedicated interface
- Upload logs to Azure Blob Storage cloud service
- Share logs via email or other apps
- Configure automatic cloud log uploads

## Features

### 1. File-based Logging

All application logs are now saved to internal storage in addition to Android's logcat system. This provides persistent logging that survives app restarts and device reboots.

**Key Features:**
- Automatic log rotation when files exceed 5MB
- Maintains up to 5 historical log files
- Thread-safe log writing
- Logs are stored in the app's internal storage (`/data/data/com.christistking.mobilenetworkreset/files/logs/`)

### 2. Log Viewer Interface

A new "View Logs" button has been added to the main activity that opens a dedicated logs viewer where users can:
- View all application logs in a scrollable interface
- Refresh logs in real-time
- Clear all logs
- Share logs via system share dialog
- Upload logs to cloud storage
- Configure cloud storage settings

### 3. Cloud Log Upload (Azure Blob Storage)

The app supports automatic upload of logs to Azure Blob Storage, a free cloud service from Microsoft.

**Azure Free Tier Benefits:**
- 5GB storage included free
- 20,000 read/write operations per month
- Perfect for application logging needs

**Configuration Steps:**

1. **Create an Azure Storage Account:**
   - Go to [Azure Portal](https://portal.azure.com)
   - Create a free account if you don't have one
   - Navigate to "Storage accounts" and create a new storage account
   - Choose the free tier (LRS - Locally Redundant Storage)

2. **Create a Container:**
   - Open your storage account
   - Go to "Containers" under "Data storage"
   - Create a new container (e.g., "mobile-network-reset-logs")
   - Set public access level to "Private"

3. **Generate a SAS Token:**
   - In your container, click "Shared access signature"
   - Set permissions: Write, Add, Create
   - Set expiry date (e.g., 1 year from now)
   - Click "Generate SAS token and URL"
   - Copy the SAS token (starts with "?sv=...")

4. **Configure in the App:**
   - Open the Mobile Network Reset app
   - Tap "View Logs"
   - Tap "Configure Cloud"
   - Enter:
     - Storage Account Name: Your Azure storage account name
     - Container Name: The container you created (e.g., "mobile-network-reset-logs")
     - SAS Token: The token you generated (include the "?" at the beginning)
   - Tap "Save"

5. **Upload Logs:**
   - Tap "Upload" to manually upload logs immediately
   - Or enable auto-upload to automatically upload logs every 24 hours

### 4. Auto-Upload Feature

Once configured, the app can automatically upload logs to Azure Blob Storage:
- Uploads occur every 24 hours
- Triggered automatically when the app starts (if 24 hours have passed)
- Uploads include all log files combined into a single timestamped file
- Crash logs are uploaded immediately when detected

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

The `CloudLogManager` class handles cloud upload configuration and operations:

```kotlin
val cloudLogManager = CloudLogManager(context, logManager)

// Configure Azure Storage
cloudLogManager.configureAzureStorage(accountName, containerName, sasToken)

// Enable auto-upload
cloudLogManager.setAutoUploadEnabled(true)

// Manual upload
cloudLogManager.uploadAllLogs()
cloudLogManager.uploadCurrentLog()

// Auto-upload check
cloudLogManager.performAutoUploadIfNeeded()
```

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
   - SAS tokens are stored securely in SharedPreferences
   - Communication with Azure uses HTTPS
   - SAS tokens can be configured with limited permissions (write-only)
   - Set appropriate expiry dates on SAS tokens

3. **Log Sharing:**
   - Users are in control of when and how logs are shared
   - Logs shared via system share dialog use Android's FileProvider for secure file sharing

## Troubleshooting

### Cloud Upload Issues

**Error: "Failed to upload logs. Check configuration."**
- Verify your Azure storage account name is correct
- Ensure the container exists
- Check that the SAS token is valid and hasn't expired
- Verify the SAS token has write permissions
- Check your internet connection

**Error: "Response code: 403"**
- SAS token doesn't have required permissions
- SAS token has expired
- Container access policy has changed

**Error: "Response code: 404"**
- Container name is incorrect
- Container doesn't exist
- Storage account name is incorrect

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

4. **Cloud Upload Configuration:**
   - Use limited SAS token permissions (write-only)
   - Set reasonable expiry dates (6-12 months)
   - Regenerate tokens periodically
   - Consider enabling auto-upload for continuous monitoring

## Future Enhancements

Potential future improvements:
- Support for other cloud services (AWS S3, Google Cloud Storage)
- Log filtering by level or tag
- Search functionality in log viewer
- Export logs in different formats (JSON, CSV)
- Encrypted log storage
- Log compression before upload
- Configurable log retention periods
