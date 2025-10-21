package com.christistking.mobilenetworkreset

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
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
    private lateinit var configureCloudButton: MaterialButton
    
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
        configureCloudButton = findViewById(R.id.configureCloudButton)

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

        configureCloudButton.setOnClickListener {
            showCloudConfiguration()
        }

        // Update upload button state
        updateUploadButtonState()

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
            Toast.makeText(this, "Cloud upload not configured. Please configure Azure Storage first.", Toast.LENGTH_LONG).show()
            showCloudConfiguration()
            return
        }

        uploadButton.isEnabled = false
        Toast.makeText(this, "Uploading logs to cloud...", Toast.LENGTH_SHORT).show()

        activityScope.launch {
            try {
                val success = cloudLogManager.uploadAllLogs()
                if (success) {
                    Toast.makeText(this@LogsActivity, "Logs uploaded successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@LogsActivity, "Failed to upload logs. Check configuration.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading logs", e)
                Toast.makeText(this@LogsActivity, "Error uploading logs: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                uploadButton.isEnabled = true
            }
        }
    }

    /**
     * Shows cloud configuration dialog
     */
    private fun showCloudConfiguration() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_cloud_config, null)
        val accountNameInput = dialogView.findViewById<TextInputEditText>(R.id.accountNameInput)
        val containerNameInput = dialogView.findViewById<TextInputEditText>(R.id.containerNameInput)
        val sasTokenInput = dialogView.findViewById<TextInputEditText>(R.id.sasTokenInput)

        AlertDialog.Builder(this)
            .setTitle("Configure Azure Storage")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val accountName = accountNameInput.text?.toString() ?: ""
                val containerName = containerNameInput.text?.toString() ?: ""
                val sasToken = sasTokenInput.text?.toString() ?: ""

                if (accountName.isNotEmpty() && containerName.isNotEmpty() && sasToken.isNotEmpty()) {
                    cloudLogManager.configureAzureStorage(accountName, containerName, sasToken)
                    cloudLogManager.setAutoUploadEnabled(true)
                    updateUploadButtonState()
                    Toast.makeText(this, "Cloud storage configured successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Updates upload button state based on configuration
     */
    private fun updateUploadButtonState() {
        uploadButton.isEnabled = cloudLogManager.isConfigured()
        if (cloudLogManager.isConfigured()) {
            configureCloudButton.text = "Reconfigure Cloud"
        } else {
            configureCloudButton.text = "Configure Cloud"
        }
    }
}
