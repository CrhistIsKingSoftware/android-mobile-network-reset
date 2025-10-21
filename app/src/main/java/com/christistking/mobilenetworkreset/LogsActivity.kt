package com.christistking.mobilenetworkreset

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import android.widget.ScrollView
import android.widget.TextView

/**
 * Activity for viewing and managing application logs
 */
class LogsActivity : AppCompatActivity() {

    private lateinit var logTextView: TextView
    private lateinit var scrollView: ScrollView
    private lateinit var refreshButton: MaterialButton
    private lateinit var clearButton: MaterialButton
    private lateinit var shareButton: MaterialButton
    private lateinit var uploadButton: MaterialButton
    
    private lateinit var logManager: LogManager
    private lateinit var cloudLogManager: CloudLogManager
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        private const val TAG = "LogsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)

        // Initialize managers
        logManager = LogManager.getInstance(this)
        cloudLogManager = CloudLogManager(this, logManager)

        // Initialize views
        logTextView = findViewById(R.id.logTextView)
        scrollView = findViewById(R.id.scrollView)
        refreshButton = findViewById(R.id.refreshButton)
        clearButton = findViewById(R.id.clearButton)
        shareButton = findViewById(R.id.shareButton)
        uploadButton = findViewById(R.id.uploadButton)

        // Set up button listeners
        refreshButton.setOnClickListener {
            refreshLogs()
        }

        clearButton.setOnClickListener {
            confirmClearLogs()
        }

        shareButton.setOnClickListener {
            shareLogs()
        }

        uploadButton.setOnClickListener {
            uploadLogs()
        }

        // Update upload button state based on build-time configuration
        uploadButton.isEnabled = cloudLogManager.isConfigured()
        if (!cloudLogManager.isConfigured()) {
            Toast.makeText(
                this,
                "Cloud upload not configured in this build. Contact the developer for a build with cloud logging enabled.",
                Toast.LENGTH_LONG
            ).show()
        }

        // Load logs
        refreshLogs()
    }

    /**
     * Refreshes the log display
     */
    private fun refreshLogs() {
        activityScope.launch {
            try {
                val logs = withContext(Dispatchers.IO) {
                    logManager.getAllLogsContent()
                }
                logTextView.text = logs
                scrollView.post {
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                }
                Toast.makeText(this@LogsActivity, "Logs refreshed", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing logs", e)
                Toast.makeText(this@LogsActivity, "Error loading logs: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Confirms and clears all logs
     */
    private fun confirmClearLogs() {
        AlertDialog.Builder(this)
            .setTitle("Clear Logs")
            .setMessage("Are you sure you want to clear all logs? This action cannot be undone.")
            .setPositiveButton("Clear") { _, _ ->
                clearLogs()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Clears all logs
     */
    private fun clearLogs() {
        activityScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    logManager.clearLogs()
                }
                logTextView.text = "Logs cleared"
                Toast.makeText(this@LogsActivity, "All logs cleared", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing logs", e)
                Toast.makeText(this@LogsActivity, "Error clearing logs: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Shares logs via system share dialog
     */
    private fun shareLogs() {
        activityScope.launch {
            try {
                val logsFile = withContext(Dispatchers.IO) {
                    val tempFile = File(cacheDir, "app_logs.txt")
                    logManager.exportLogs(tempFile)
                    tempFile
                }

                val uri = FileProvider.getUriForFile(
                    this@LogsActivity,
                    "${packageName}.fileprovider",
                    logsFile
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Mobile Network Reset Logs")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                startActivity(Intent.createChooser(shareIntent, "Share Logs"))
            } catch (e: Exception) {
                Log.e(TAG, "Error sharing logs", e)
                Toast.makeText(this@LogsActivity, "Error sharing logs: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Uploads logs to cloud
     */
    private fun uploadLogs() {
        if (!cloudLogManager.isConfigured()) {
            Toast.makeText(
                this,
                "Cloud upload not configured in this build. Please share logs manually instead.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        uploadButton.isEnabled = false
        Toast.makeText(this, "Uploading logs to cloud...", Toast.LENGTH_SHORT).show()

        activityScope.launch {
            try {
                val success = cloudLogManager.uploadAllLogs()
                if (success) {
                    Toast.makeText(this@LogsActivity, "Logs uploaded successfully to Azure", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@LogsActivity, "Failed to upload logs. Please try again.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading logs", e)
                Toast.makeText(this@LogsActivity, "Error uploading logs: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                uploadButton.isEnabled = cloudLogManager.isConfigured()
            }
        }
    }
}
