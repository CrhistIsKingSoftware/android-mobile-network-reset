package com.christistking.mobilenetworkreset

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

/**
 * Interface for cloud log upload services
 */
interface CloudLogService {
    suspend fun uploadLog(logFile: File): Boolean
    suspend fun uploadLogContent(content: String, fileName: String): Boolean
}

/**
 * Azure Blob Storage implementation for cloud log uploads.
 * Uses Azure Storage REST API with Shared Access Signature (SAS) token.
 * 
 * Azure offers free tier with 5GB storage and 20,000 read/write operations per month.
 * 
 * Setup instructions:
 * 1. Create an Azure Storage account (free tier)
 * 2. Create a container for logs
 * 3. Generate a SAS token with write permissions
 * 4. Configure the connection string in the app
 */
class AzureBlobStorageService(
    private val context: Context,
    private val accountName: String,
    private val containerName: String,
    private val sasToken: String
) : CloudLogService {
    
    companion object {
        private const val TAG = "AzureBlobStorage"
        private const val AZURE_BLOB_ENDPOINT = "https://%s.blob.core.windows.net/%s/%s%s"
        private const val CONNECTION_TIMEOUT = 30000 // 30 seconds
        private const val READ_TIMEOUT = 30000 // 30 seconds
    }
    
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    
    /**
     * Uploads a log file to Azure Blob Storage
     */
    override suspend fun uploadLog(logFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!logFile.exists()) {
                Log.e(TAG, "Log file does not exist: ${logFile.absolutePath}")
                return@withContext false
            }
            
            val content = logFile.readText()
            return@withContext uploadLogContent(content, logFile.name)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload log file", e)
            false
        }
    }
    
    /**
     * Uploads log content as a blob to Azure Storage
     */
    override suspend fun uploadLogContent(content: String, fileName: String): Boolean = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        
        try {
            // Generate unique blob name with timestamp
            val timestamp = dateFormat.format(Date())
            val blobName = "android_${android.os.Build.MODEL}_${timestamp}_$fileName"
            
            // Construct blob URL with SAS token
            val blobUrl = String.format(
                AZURE_BLOB_ENDPOINT,
                accountName,
                containerName,
                blobName,
                if (sasToken.startsWith("?")) sasToken else "?$sasToken"
            )
            
            Log.d(TAG, "Uploading to blob: $blobName")
            
            val url = URL(blobUrl)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "PUT"
            connection.doOutput = true
            connection.connectTimeout = CONNECTION_TIMEOUT
            connection.readTimeout = READ_TIMEOUT
            
            // Set required headers for Azure Blob Storage
            connection.setRequestProperty("x-ms-blob-type", "BlockBlob")
            connection.setRequestProperty("Content-Type", "text/plain; charset=utf-8")
            connection.setRequestProperty("Content-Length", content.toByteArray(Charsets.UTF_8).size.toString())
            
            // Write content to blob
            connection.outputStream.use { outputStream: OutputStream ->
                outputStream.write(content.toByteArray(Charsets.UTF_8))
            }
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                Log.i(TAG, "Successfully uploaded log to Azure: $blobName")
                return@withContext true
            } else {
                val errorMessage = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                Log.e(TAG, "Failed to upload log. Response code: $responseCode, Error: $errorMessage")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during log upload", e)
            return@withContext false
        } finally {
            connection?.disconnect()
        }
    }
}

/**
 * Manages cloud log uploads with automatic retry and configuration
 */
class CloudLogManager(
    private val context: Context,
    private val logManager: LogManager
) {
    
    companion object {
        private const val TAG = "CloudLogManager"
        private const val PREFS_NAME = "cloud_log_prefs"
        private const val KEY_AZURE_ACCOUNT_NAME = "azure_account_name"
        private const val KEY_AZURE_CONTAINER_NAME = "azure_container_name"
        private const val KEY_AZURE_SAS_TOKEN = "azure_sas_token"
        private const val KEY_AUTO_UPLOAD_ENABLED = "auto_upload_enabled"
        private const val KEY_LAST_UPLOAD_TIME = "last_upload_time"
        
        // Auto-upload interval (24 hours)
        private const val AUTO_UPLOAD_INTERVAL_MS = 24 * 60 * 60 * 1000L
    }
    
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * Configures Azure Blob Storage for cloud uploads
     */
    fun configureAzureStorage(accountName: String, containerName: String, sasToken: String) {
        prefs.edit().apply {
            putString(KEY_AZURE_ACCOUNT_NAME, accountName)
            putString(KEY_AZURE_CONTAINER_NAME, containerName)
            putString(KEY_AZURE_SAS_TOKEN, sasToken)
            apply()
        }
        Log.i(TAG, "Azure Storage configured")
    }
    
    /**
     * Checks if cloud upload is configured
     */
    fun isConfigured(): Boolean {
        return !prefs.getString(KEY_AZURE_ACCOUNT_NAME, "").isNullOrEmpty() &&
               !prefs.getString(KEY_AZURE_CONTAINER_NAME, "").isNullOrEmpty() &&
               !prefs.getString(KEY_AZURE_SAS_TOKEN, "").isNullOrEmpty()
    }
    
    /**
     * Enables or disables auto-upload
     */
    fun setAutoUploadEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_UPLOAD_ENABLED, enabled).apply()
        Log.i(TAG, "Auto-upload ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Checks if auto-upload is enabled
     */
    fun isAutoUploadEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_UPLOAD_ENABLED, false)
    }
    
    /**
     * Checks if it's time for auto-upload
     */
    fun shouldAutoUpload(): Boolean {
        if (!isAutoUploadEnabled() || !isConfigured()) {
            return false
        }
        
        val lastUploadTime = prefs.getLong(KEY_LAST_UPLOAD_TIME, 0)
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastUploadTime) >= AUTO_UPLOAD_INTERVAL_MS
    }
    
    /**
     * Creates a cloud service instance
     */
    private fun createCloudService(): CloudLogService? {
        val accountName = prefs.getString(KEY_AZURE_ACCOUNT_NAME, "") ?: ""
        val containerName = prefs.getString(KEY_AZURE_CONTAINER_NAME, "") ?: ""
        val sasToken = prefs.getString(KEY_AZURE_SAS_TOKEN, "") ?: ""
        
        if (accountName.isEmpty() || containerName.isEmpty() || sasToken.isEmpty()) {
            Log.w(TAG, "Cloud service not configured")
            return null
        }
        
        return AzureBlobStorageService(context, accountName, containerName, sasToken)
    }
    
    /**
     * Uploads all logs to the cloud
     */
    suspend fun uploadAllLogs(): Boolean = withContext(Dispatchers.IO) {
        try {
            val cloudService = createCloudService()
            if (cloudService == null) {
                Log.w(TAG, "Cloud service not configured, cannot upload logs")
                return@withContext false
            }
            
            val allLogs = logManager.getAllLogsContent()
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "all_logs_$timestamp.txt"
            
            val success = cloudService.uploadLogContent(allLogs, fileName)
            
            if (success) {
                // Update last upload time
                prefs.edit().putLong(KEY_LAST_UPLOAD_TIME, System.currentTimeMillis()).apply()
                Log.i(TAG, "Successfully uploaded all logs to cloud")
            }
            
            return@withContext success
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload logs to cloud", e)
            return@withContext false
        }
    }
    
    /**
     * Uploads current log file to the cloud
     */
    suspend fun uploadCurrentLog(): Boolean = withContext(Dispatchers.IO) {
        try {
            val cloudService = createCloudService()
            if (cloudService == null) {
                Log.w(TAG, "Cloud service not configured, cannot upload logs")
                return@withContext false
            }
            
            val currentLog = logManager.getCurrentLogContent()
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "current_log_$timestamp.txt"
            
            val success = cloudService.uploadLogContent(currentLog, fileName)
            
            if (success) {
                // Update last upload time
                prefs.edit().putLong(KEY_LAST_UPLOAD_TIME, System.currentTimeMillis()).apply()
                Log.i(TAG, "Successfully uploaded current log to cloud")
            }
            
            return@withContext success
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload current log to cloud", e)
            return@withContext false
        }
    }
    
    /**
     * Performs auto-upload if needed
     */
    suspend fun performAutoUploadIfNeeded() {
        if (shouldAutoUpload()) {
            Log.i(TAG, "Performing automatic log upload")
            uploadAllLogs()
        }
    }
}
