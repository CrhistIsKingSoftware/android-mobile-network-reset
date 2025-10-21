package com.christistking.mobilenetworkreset

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import android.widget.TextView
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * MainActivity for the Mobile Network Reset application.
 * 
 * This activity provides a simple interface to reset mobile network connections
 * by automating the process of toggling network operator selection.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var resetButton: MaterialButton
    private lateinit var settingsButton: MaterialButton
    private lateinit var viewLogsButton: MaterialButton
    private lateinit var accessibilityResetButton: MaterialButton
    private lateinit var enableAccessibilityButton: MaterialButton
    private lateinit var statusText: TextView
    private lateinit var accessibilityStatusText: TextView
    private var telephonyManager: TelephonyManager? = null
    private var accessibilityManager: AccessibilityManager? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isReceiverRegistered = false
    private lateinit var logManager: LogManager
    private lateinit var cloudLogManager: CloudLogManager
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == NetworkResetAccessibilityService.BROADCAST_STATUS_UPDATE) {
                val status = intent.getStringExtra(NetworkResetAccessibilityService.EXTRA_STATUS)
                val step = intent.getStringExtra(NetworkResetAccessibilityService.EXTRA_STEP)
                if (status != null) {
                    updateStatus(status)
                }
            }
        }
    }
    
    companion object {
        private const val TAG = "MainActivity"
        private const val PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize log manager
        logManager = LogManager.getInstance(this)
        cloudLogManager = CloudLogManager(this, logManager)
        
        // Set up global exception handler to log crashes
        setupCrashLogging()
        
        try {
            logManager.i(TAG, "onCreate: Starting MainActivity initialization")
            Log.d(TAG, "onCreate: Starting MainActivity initialization")
            setContentView(R.layout.activity_main)

            // Initialize views with error handling
            try {
                resetButton = findViewById(R.id.resetButton)
                settingsButton = findViewById(R.id.settingsButton)
                viewLogsButton = findViewById(R.id.viewLogsButton)
                accessibilityResetButton = findViewById(R.id.accessibilityResetButton)
                enableAccessibilityButton = findViewById(R.id.enableAccessibilityButton)
                statusText = findViewById(R.id.statusText)
                accessibilityStatusText = findViewById(R.id.accessibilityStatusText)
                Log.d(TAG, "onCreate: All views initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "onCreate: Error initializing views", e)
                throw e
            }

            // Initialize managers with null safety checks
            try {
                telephonyManager = getSystemService(TelephonyManager::class.java)
                if (telephonyManager == null) {
                    Log.e(TAG, "onCreate: TelephonyManager is null - telephony service unavailable")
                    Toast.makeText(
                        this,
                        "Telephony service is unavailable on this device",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Log.d(TAG, "onCreate: TelephonyManager initialized successfully")
                }
                
                accessibilityManager = getSystemService(AccessibilityManager::class.java)
                if (accessibilityManager == null) {
                    Log.e(TAG, "onCreate: AccessibilityManager is null - accessibility service unavailable")
                } else {
                    Log.d(TAG, "onCreate: AccessibilityManager initialized successfully")
                }
            } catch (e: Exception) {
                Log.e(TAG, "onCreate: Error initializing system services", e)
                Toast.makeText(
                    this,
                    "Error initializing system services: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

            // Set up button click listeners
            resetButton.setOnClickListener {
                try {
                    performNetworkReset()
                } catch (e: Exception) {
                    Log.e(TAG, "Error in resetButton click", e)
                    handleError("Error during reset: ${e.message}", e)
                }
            }

            settingsButton.setOnClickListener {
                try {
                    openNetworkSettings()
                } catch (e: Exception) {
                    Log.e(TAG, "Error in settingsButton click", e)
                    Toast.makeText(this, "Error opening settings: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            
            viewLogsButton.setOnClickListener {
                try {
                    openLogsActivity()
                } catch (e: Exception) {
                    logManager.e(TAG, "Error in viewLogsButton click", e)
                    Log.e(TAG, "Error in viewLogsButton click", e)
                    Toast.makeText(this, "Error opening logs: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            
            accessibilityResetButton.setOnClickListener {
                try {
                    performAccessibilityReset()
                } catch (e: Exception) {
                    Log.e(TAG, "Error in accessibilityResetButton click", e)
                    handleError("Error during accessibility reset: ${e.message}", e)
                }
            }
            
            enableAccessibilityButton.setOnClickListener {
                try {
                    openAccessibilitySettings()
                } catch (e: Exception) {
                    Log.e(TAG, "Error in enableAccessibilityButton click", e)
                    Toast.makeText(this, "Error opening accessibility settings: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            
            // Check for location permission on startup
            try {
                checkLocationPermission()
            } catch (e: Exception) {
                logManager.e(TAG, "onCreate: Error checking location permission", e)
                Log.e(TAG, "onCreate: Error checking location permission", e)
            }
            
            // Update accessibility service status
            try {
                updateAccessibilityServiceStatus()
            } catch (e: Exception) {
                logManager.e(TAG, "onCreate: Error updating accessibility service status", e)
                Log.e(TAG, "onCreate: Error updating accessibility service status", e)
            }
            
            // Perform auto-upload if needed
            activityScope.launch {
                try {
                    cloudLogManager.performAutoUploadIfNeeded()
                } catch (e: Exception) {
                    logManager.e(TAG, "onCreate: Error during auto-upload", e)
                    Log.e(TAG, "onCreate: Error during auto-upload", e)
                }
            }
            
            logManager.i(TAG, "onCreate: MainActivity initialization completed successfully")
            Log.d(TAG, "onCreate: MainActivity initialization completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: Fatal error during initialization", e)
            Toast.makeText(
                this,
                "Fatal error: ${e.message}\nPlease check logcat for details",
                Toast.LENGTH_LONG
            ).show()
            throw e
        }
    }
    
    /**
     * Set up crash logging to capture and log any uncaught exceptions
     */
    private fun setupCrashLogging() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val crashMessage = buildString {
                append("=== UNCAUGHT EXCEPTION ===\n")
                append("Thread: ${thread.name}\n")
                append("Exception: ${throwable.javaClass.name}\n")
                append("Message: ${throwable.message}\n")
                append("Stack trace:\n")
                throwable.stackTrace.forEach { element ->
                    append("  at $element\n")
                }
                append("=== END UNCAUGHT EXCEPTION ===")
            }
            
            // Log to file and logcat
            logManager.e(TAG, crashMessage, throwable)
            Log.e(TAG, "=== UNCAUGHT EXCEPTION ===", throwable)
            Log.e(TAG, "Thread: ${thread.name}")
            Log.e(TAG, "Exception: ${throwable.javaClass.name}")
            Log.e(TAG, "Message: ${throwable.message}")
            Log.e(TAG, "Stack trace:")
            throwable.stackTrace.forEach { element ->
                Log.e(TAG, "  at $element")
            }
            Log.e(TAG, "=== END UNCAUGHT EXCEPTION ===")
            
            // Try to upload crash log immediately
            try {
                activityScope.launch {
                    cloudLogManager.uploadCurrentLog()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload crash log", e)
            }
            
            // Call the default handler to let the system handle the crash
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
    
    override fun onResume() {
        super.onResume()
        
        // Register broadcast receiver for accessibility service updates
        if (!isReceiverRegistered) {
            try {
                val filter = IntentFilter(NetworkResetAccessibilityService.BROADCAST_STATUS_UPDATE)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    registerReceiver(statusReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
                } else {
                    @Suppress("UnspecifiedRegisterReceiverFlag")
                    registerReceiver(statusReceiver, filter)
                }
                isReceiverRegistered = true
            } catch (e: Exception) {
                Log.e("MainActivity", "Error registering receiver", e)
            }
        }
        
        // Update accessibility service status when returning to the app
        updateAccessibilityServiceStatus()
    }
    
    override fun onPause() {
        super.onPause()
        
        // Unregister broadcast receiver
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(statusReceiver)
                isReceiverRegistered = false
            } catch (e: Exception) {
                Log.e("MainActivity", "Error unregistering receiver", e)
            }
        }
    }
    
    /**
     * Check if location permission is granted, request if not
     */
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        }
    }
    
    /**
     * Handle permission request result
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this,
                    "Location permission granted. You can now reset the network.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "Location permission is required for network scanning. Please grant it in settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Performs the network reset operation automatically.
     * 
     * This method attempts to reset the mobile network by:
     * 1. Checking permissions
     * 2. Disabling automatic network selection
     * 3. Scanning for available networks
     * 4. Selecting the first available network
     * 5. Re-enabling automatic network selection
     */
    private fun performNetworkReset() {
        try {
            Log.d(TAG, "performNetworkReset: Starting network reset")
            updateStatus(getString(R.string.status_resetting))
            resetButton.isEnabled = false

            // Check if telephonyManager is available
            if (telephonyManager == null) {
                Log.e(TAG, "performNetworkReset: TelephonyManager is null")
                updateStatus(getString(R.string.status_error))
                Toast.makeText(
                    this,
                    "Telephony service is not available on this device",
                    Toast.LENGTH_LONG
                ).show()
                resetButton.isEnabled = true
                return
            }

            // Check if we have telephony service
            if (telephonyManager?.phoneType == TelephonyManager.PHONE_TYPE_NONE) {
                Log.w(TAG, "performNetworkReset: No telephony service available")
                updateStatus(getString(R.string.status_error))
                Toast.makeText(
                    this,
                    "No telephony service available on this device",
                    Toast.LENGTH_LONG
                ).show()
                resetButton.isEnabled = true
                return
            }
            
            // Check for location permission (required for network scanning)
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w(TAG, "performNetworkReset: Location permission not granted")
                updateStatus(getString(R.string.status_error))
                Toast.makeText(
                    this,
                    "Location permission is required. Please grant it and try again.",
                    Toast.LENGTH_LONG
                ).show()
                resetButton.isEnabled = true
                checkLocationPermission()
                return
            }

            // Step 1: Disable automatic network selection
            updateStatus("Step 1/4: Disabling automatic selection...")
            Log.d(TAG, "performNetworkReset: Step 1 - Disabling automatic selection")
            try {
                // Try to set manual selection mode by toggling to automatic first
                // This ensures we start from a known state
                val result = telephonyManager?.setNetworkSelectionModeAutomatic()
                Log.d(TAG, "performNetworkReset: setNetworkSelectionModeAutomatic returned: $result")
                
                // Wait a moment for the operation to complete
                handler.postDelayed({
                    // Step 2: Scan and select network
                    performNetworkScanAndSelect()
                }, 2000)
                
            } catch (e: SecurityException) {
                // Permission denied - fall back to manual approach
                Log.w(TAG, "performNetworkReset: SecurityException - falling back to manual", e)
                fallbackToManualApproach(e)
            } catch (e: Exception) {
                Log.e(TAG, "performNetworkReset: Exception during network reset", e)
                handleError("Error during network reset: ${e.message}", e)
            }

        } catch (e: Exception) {
            Log.e(TAG, "performNetworkReset: Fatal error", e)
            handleError("Error: ${e.message}", e)
        }
    }
    
    /**
     * Performs network scan and selection
     */
    private fun performNetworkScanAndSelect() {
        try {
            Log.d(TAG, "performNetworkScanAndSelect: Starting network scan")
            updateStatus("Step 2/4: Scanning for networks...")
            
            // Get the current network operator as a fallback
            val currentOperator = telephonyManager?.networkOperator ?: ""
            Log.d(TAG, "performNetworkScanAndSelect: Current operator: $currentOperator")
            
            if (currentOperator.isNotEmpty()) {
                // Step 3: Manually select the network
                updateStatus("Step 3/4: Selecting network...")
                Log.d(TAG, "performNetworkScanAndSelect: Step 3 - Selecting network")
                
                handler.postDelayed({
                    try {
                        // Select the current network manually
                        val manualResult = telephonyManager?.setNetworkSelectionModeManual(
                            currentOperator,
                            true
                        )
                        Log.d(TAG, "performNetworkScanAndSelect: setNetworkSelectionModeManual returned: $manualResult")
                        
                        // Step 4: Re-enable automatic selection after a delay
                        handler.postDelayed({
                            reEnableAutomaticSelection()
                        }, 3000)
                        
                    } catch (e: SecurityException) {
                        // Permission denied - fall back to manual approach
                        Log.w(TAG, "performNetworkScanAndSelect: SecurityException - falling back to manual", e)
                        fallbackToManualApproach(e)
                    } catch (e: Exception) {
                        Log.e(TAG, "performNetworkScanAndSelect: Error selecting network", e)
                        handleError("Error selecting network: ${e.message}", e)
                    }
                }, 1000)
                
            } else {
                // No operator found, try to re-enable automatic
                Log.w(TAG, "performNetworkScanAndSelect: No operator found, re-enabling automatic")
                handler.postDelayed({
                    reEnableAutomaticSelection()
                }, 1000)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "performNetworkScanAndSelect: Error scanning networks", e)
            handleError("Error scanning networks: ${e.message}", e)
        }
    }
    
    /**
     * Re-enables automatic network selection
     */
    private fun reEnableAutomaticSelection() {
        try {
            Log.d(TAG, "reEnableAutomaticSelection: Re-enabling automatic selection")
            updateStatus("Step 4/4: Re-enabling automatic selection...")
            
            val result = telephonyManager?.setNetworkSelectionModeAutomatic()
            Log.d(TAG, "reEnableAutomaticSelection: setNetworkSelectionModeAutomatic returned: $result")
            
            // Wait for operation to complete
            handler.postDelayed({
                updateStatus(getString(R.string.status_success))
                Toast.makeText(
                    this,
                    "Network reset completed successfully!",
                    Toast.LENGTH_SHORT
                ).show()
                resetButton.isEnabled = true
                Log.d(TAG, "reEnableAutomaticSelection: Network reset completed successfully")
            }, 2000)
            
        } catch (e: SecurityException) {
            // Permission denied - fall back to manual approach
            Log.w(TAG, "reEnableAutomaticSelection: SecurityException - falling back to manual", e)
            fallbackToManualApproach(e)
        } catch (e: Exception) {
            Log.e(TAG, "reEnableAutomaticSelection: Error re-enabling automatic selection", e)
            handleError("Error re-enabling automatic selection: ${e.message}", e)
        }
    }
    
    /**
     * Falls back to manual approach when automated approach fails
     */
    private fun fallbackToManualApproach(exception: Exception) {
        Log.w(TAG, "fallbackToManualApproach: Falling back to manual approach", exception)
        updateStatus("Automated reset not available on this device")
        
        Toast.makeText(
            this,
            "Automatic network reset requires system permissions.\n" +
                    "Opening settings for manual reset.\n\n" +
                    "Please:\n" +
                    "1. Disable 'Select automatically'\n" +
                    "2. Select a network\n" +
                    "3. Go back\n" +
                    "4. Re-enable 'Select automatically'",
            Toast.LENGTH_LONG
        ).show()
        
        // Delay opening settings to allow user to read the toast
        handler.postDelayed({
            openNetworkOperatorSettings()
            handler.postDelayed({
                updateStatus(getString(R.string.status_idle))
                resetButton.isEnabled = true
            }, 2000)
        }, 4000)
    }
    
    /**
     * Handles errors during network reset
     */
    private fun handleError(message: String, exception: Exception) {
        Log.e(TAG, "handleError: $message", exception)
        updateStatus(getString(R.string.status_error))
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        resetButton.isEnabled = true
        exception.printStackTrace()
    }

    /**
     * Opens the network operator settings screen.
     */
    private fun openNetworkOperatorSettings() {
        try {
            val intent = Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to general network settings if operator settings not available
            openNetworkSettings()
        }
    }

    /**
     * Opens the general network settings screen.
     */
    private fun openNetworkSettings() {
        try {
            val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Unable to open network settings",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Updates the status text view.
     */
    private fun updateStatus(status: String) {
        statusText.text = status
    }
    
    /**
     * Checks if accessibility service is enabled for this app
     */
    private fun isAccessibilityServiceEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        
        val colonSplitter = enabledServices.split(":")
        val serviceName = "${packageName}/${NetworkResetAccessibilityService::class.java.name}"
        
        return colonSplitter.any { it.equals(serviceName, ignoreCase = true) }
    }
    
    /**
     * Updates the accessibility service status display
     */
    private fun updateAccessibilityServiceStatus() {
        val isEnabled = isAccessibilityServiceEnabled()
        
        if (isEnabled) {
            accessibilityStatusText.text = getString(R.string.accessibility_service_enabled)
            accessibilityResetButton.isEnabled = true
            enableAccessibilityButton.isEnabled = false
        } else {
            accessibilityStatusText.text = getString(R.string.accessibility_service_disabled)
            accessibilityResetButton.isEnabled = false
            enableAccessibilityButton.isEnabled = true
        }
    }
    
    /**
     * Performs network reset using accessibility service
     */
    private fun performAccessibilityReset() {
        try {
            Log.d(TAG, "performAccessibilityReset: Starting accessibility reset")
            if (!isAccessibilityServiceEnabled()) {
                Log.w(TAG, "performAccessibilityReset: Accessibility service not enabled")
                Toast.makeText(
                    this,
                    "Please enable the accessibility service first",
                    Toast.LENGTH_LONG
                ).show()
                openAccessibilitySettings()
                return
            }
            
            updateStatus(getString(R.string.status_starting_accessibility))
            accessibilityResetButton.isEnabled = false
            
            // First open the network operator settings
            val settingsIntent = Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS)
            startActivity(settingsIntent)
            Log.d(TAG, "performAccessibilityReset: Opened network operator settings")
            
            // Then trigger the accessibility service to start processing
            handler.postDelayed({
                val serviceIntent = Intent(NetworkResetAccessibilityService.ACTION_START_RESET).apply {
                    setPackage(packageName)
                }
                sendBroadcast(serviceIntent)
                Log.d(TAG, "performAccessibilityReset: Sent START_RESET broadcast")
                
                // Re-enable button after a delay
                handler.postDelayed({
                    accessibilityResetButton.isEnabled = true
                    Log.d(TAG, "performAccessibilityReset: Re-enabled button after timeout")
                }, 30000) // 30 seconds timeout
            }, 2000) // Wait 2 seconds for settings to open
            
        } catch (e: Exception) {
            Log.e(TAG, "performAccessibilityReset: Error starting accessibility reset", e)
            handleError("Error starting accessibility reset: ${e.message}", e)
        }
    }
    
    /**
     * Opens accessibility settings
     */
    private fun openAccessibilitySettings() {
        try {
            Log.d(TAG, "openAccessibilitySettings: Opening accessibility settings")
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            Toast.makeText(
                this,
                "Find and enable \"Mobile Network Reset\" in the list",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Log.e(TAG, "openAccessibilitySettings: Error opening accessibility settings", e)
            Toast.makeText(
                this,
                "Unable to open accessibility settings",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    /**
     * Opens the logs activity
     */
    private fun openLogsActivity() {
        val intent = Intent(this, LogsActivity::class.java)
        startActivity(intent)
    }
}
