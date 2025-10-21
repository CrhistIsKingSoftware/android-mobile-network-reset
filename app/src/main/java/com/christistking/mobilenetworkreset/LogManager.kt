package com.christistking.mobilenetworkreset

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Manages logging to files on the device and cloud upload.
 * 
 * Features:
 * - Saves logs to internal storage
 * - Automatic log rotation when files get too large
 * - Thread-safe log writing
 * - Cloud upload integration
 */
class LogManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "LogManager"
        private const val LOG_DIR_NAME = "logs"
        private const val LOG_FILE_NAME = "app_log.txt"
        private const val MAX_LOG_FILE_SIZE = 5 * 1024 * 1024 // 5 MB
        private const val MAX_LOG_FILES = 5
        
        @Volatile
        private var instance: LogManager? = null
        
        fun getInstance(context: Context): LogManager {
            return instance ?: synchronized(this) {
                instance ?: LogManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    private val logDir: File = File(context.filesDir, LOG_DIR_NAME)
    private val currentLogFile: File
        get() = File(logDir, LOG_FILE_NAME)
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    
    init {
        // Ensure log directory exists
        if (!logDir.exists()) {
            logDir.mkdirs()
        }
    }
    
    /**
     * Logs a message with specified level
     */
    fun log(level: String, tag: String, message: String, throwable: Throwable? = null) {
        // Log to Android logcat as well
        when (level) {
            "DEBUG", "D" -> Log.d(tag, message, throwable)
            "INFO", "I" -> Log.i(tag, message, throwable)
            "WARN", "W" -> Log.w(tag, message, throwable)
            "ERROR", "E" -> Log.e(tag, message, throwable)
            else -> Log.v(tag, message, throwable)
        }
        
        // Write to file
        writeToFile(level, tag, message, throwable)
    }
    
    /**
     * Convenience methods for different log levels
     */
    fun d(tag: String, message: String) = log("DEBUG", tag, message)
    fun i(tag: String, message: String) = log("INFO", tag, message)
    fun w(tag: String, message: String, throwable: Throwable? = null) = log("WARN", tag, message, throwable)
    fun e(tag: String, message: String, throwable: Throwable? = null) = log("ERROR", tag, message, throwable)
    
    /**
     * Writes log entry to file
     */
    private fun writeToFile(level: String, tag: String, message: String, throwable: Throwable?) {
        try {
            synchronized(this) {
                // Check if rotation is needed
                if (currentLogFile.exists() && currentLogFile.length() > MAX_LOG_FILE_SIZE) {
                    rotateLogFiles()
                }
                
                FileWriter(currentLogFile, true).use { writer ->
                    val timestamp = dateFormat.format(Date())
                    val logEntry = buildString {
                        append("[$timestamp] [$level] [$tag] $message")
                        
                        if (throwable != null) {
                            append("\n")
                            append("Exception: ${throwable.javaClass.name}")
                            append("\nMessage: ${throwable.message}")
                            append("\nStack trace:\n")
                            throwable.stackTrace.forEach { element ->
                                append("  at $element\n")
                            }
                        }
                        append("\n")
                    }
                    writer.write(logEntry)
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to write to log file", e)
        }
    }
    
    /**
     * Rotates log files when current file gets too large
     */
    private fun rotateLogFiles() {
        try {
            // Delete oldest log file if we have too many
            val logFiles = logDir.listFiles()?.filter { it.name.startsWith("app_log") }
                ?.sortedByDescending { it.lastModified() } ?: emptyList()
            
            if (logFiles.size >= MAX_LOG_FILES) {
                logFiles.drop(MAX_LOG_FILES - 1).forEach { it.delete() }
            }
            
            // Rename current log file with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val archiveFile = File(logDir, "app_log_$timestamp.txt")
            currentLogFile.renameTo(archiveFile)
            
            Log.i(TAG, "Rotated log files. New archive: ${archiveFile.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to rotate log files", e)
        }
    }
    
    /**
     * Gets all log files
     */
    fun getLogFiles(): List<File> {
        return logDir.listFiles()?.filter { it.name.startsWith("app_log") }
            ?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
    
    /**
     * Gets the current log file content
     */
    fun getCurrentLogContent(): String {
        return try {
            if (currentLogFile.exists()) {
                currentLogFile.readText()
            } else {
                "No logs available"
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read log file", e)
            "Error reading logs: ${e.message}"
        }
    }
    
    /**
     * Gets all logs content combined
     */
    fun getAllLogsContent(): String {
        return try {
            val logFiles = getLogFiles()
            buildString {
                logFiles.forEach { file ->
                    append("=== ${file.name} ===\n")
                    append(file.readText())
                    append("\n\n")
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read log files", e)
            "Error reading logs: ${e.message}"
        }
    }
    
    /**
     * Clears all log files
     */
    fun clearLogs() {
        try {
            getLogFiles().forEach { it.delete() }
            Log.i(TAG, "All log files cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear log files", e)
        }
    }
    
    /**
     * Gets the log directory path
     */
    fun getLogDirectory(): File = logDir
    
    /**
     * Exports logs to a specific file
     */
    suspend fun exportLogs(outputFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val allLogs = getAllLogsContent()
            outputFile.writeText(allLogs)
            Log.i(TAG, "Logs exported to ${outputFile.absolutePath}")
            true
        } catch (e: IOException) {
            Log.e(TAG, "Failed to export logs", e)
            false
        }
    }
}
