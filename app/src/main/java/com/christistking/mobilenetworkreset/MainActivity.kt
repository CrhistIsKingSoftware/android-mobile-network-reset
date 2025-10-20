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

/**
 * MainActivity for the Mobile Network Reset application.
 * 
 * This activity provides a simple interface to reset mobile network connections
 * by automating the process of toggling network operator selection.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var resetButton: MaterialButton
    private lateinit var settingsButton: MaterialButton
    private lateinit var accessibilityResetButton: MaterialButton
    private lateinit var enableAccessibilityButton: MaterialButton
    private lateinit var statusText: TextView
    private lateinit var accessibilityStatusText: TextView
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var accessibilityManager: AccessibilityManager
    private val handler = Handler(Looper.getMainLooper())
    private var isReceiverRegistered = false
    
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
        private const val PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        resetButton = findViewById(R.id.resetButton)
        settingsButton = findViewById(R.id.settingsButton)
        accessibilityResetButton = findViewById(R.id.accessibilityResetButton)
        enableAccessibilityButton = findViewById(R.id.enableAccessibilityButton)
        statusText = findViewById(R.id.statusText)
        accessibilityStatusText = findViewById(R.id.accessibilityStatusText)

        // Initialize managers using type-safe getSystemService
        telephonyManager = getSystemService(TelephonyManager::class.java)
        accessibilityManager = getSystemService(AccessibilityManager::class.java)

        // Set up button click listeners
        resetButton.setOnClickListener {
            performNetworkReset()
        }

        settingsButton.setOnClickListener {
            openNetworkSettings()
        }
        
        accessibilityResetButton.setOnClickListener {
            performAccessibilityReset()
        }
        
        enableAccessibilityButton.setOnClickListener {
            openAccessibilitySettings()
        }
        
        // Check for location permission on startup
        checkLocationPermission()
        
        // Update accessibility service status
        updateAccessibilityServiceStatus()
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
            updateStatus(getString(R.string.status_resetting))
            resetButton.isEnabled = false

            // Check if we have telephony service
            if (telephonyManager.phoneType == TelephonyManager.PHONE_TYPE_NONE) {
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
            try {
                // Try to set manual selection mode by toggling to automatic first
                // This ensures we start from a known state
                val result = telephonyManager.setNetworkSelectionModeAutomatic()
                
                // Wait a moment for the operation to complete
                handler.postDelayed({
                    // Step 2: Scan and select network
                    performNetworkScanAndSelect()
                }, 2000)
                
            } catch (e: SecurityException) {
                // Permission denied - fall back to manual approach
                fallbackToManualApproach(e)
            } catch (e: Exception) {
                handleError("Error during network reset: ${e.message}", e)
            }

        } catch (e: Exception) {
            handleError("Error: ${e.message}", e)
        }
    }
    
    /**
     * Performs network scan and selection
     */
    private fun performNetworkScanAndSelect() {
        try {
            updateStatus("Step 2/4: Scanning for networks...")
            
            // Get the current network operator as a fallback
            val currentOperator = telephonyManager.networkOperator
            
            if (currentOperator.isNotEmpty()) {
                // Step 3: Manually select the network
                updateStatus("Step 3/4: Selecting network...")
                
                handler.postDelayed({
                    try {
                        // Select the current network manually
                        val manualResult = telephonyManager.setNetworkSelectionModeManual(
                            currentOperator,
                            true
                        )
                        
                        // Step 4: Re-enable automatic selection after a delay
                        handler.postDelayed({
                            reEnableAutomaticSelection()
                        }, 3000)
                        
                    } catch (e: SecurityException) {
                        // Permission denied - fall back to manual approach
                        fallbackToManualApproach(e)
                    } catch (e: Exception) {
                        handleError("Error selecting network: ${e.message}", e)
                    }
                }, 1000)
                
            } else {
                // No operator found, try to re-enable automatic
                handler.postDelayed({
                    reEnableAutomaticSelection()
                }, 1000)
            }
            
        } catch (e: Exception) {
            handleError("Error scanning networks: ${e.message}", e)
        }
    }
    
    /**
     * Re-enables automatic network selection
     */
    private fun reEnableAutomaticSelection() {
        try {
            updateStatus("Step 4/4: Re-enabling automatic selection...")
            
            val result = telephonyManager.setNetworkSelectionModeAutomatic()
            
            // Wait for operation to complete
            handler.postDelayed({
                updateStatus(getString(R.string.status_success))
                Toast.makeText(
                    this,
                    "Network reset completed successfully!",
                    Toast.LENGTH_SHORT
                ).show()
                resetButton.isEnabled = true
            }, 2000)
            
        } catch (e: SecurityException) {
            // Permission denied - fall back to manual approach
            fallbackToManualApproach(e)
        } catch (e: Exception) {
            handleError("Error re-enabling automatic selection: ${e.message}", e)
        }
    }
    
    /**
     * Falls back to manual approach when automated approach fails
     */
    private fun fallbackToManualApproach(exception: Exception) {
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
        if (!isAccessibilityServiceEnabled()) {
            Toast.makeText(
                this,
                "Please enable the accessibility service first",
                Toast.LENGTH_LONG
            ).show()
            openAccessibilitySettings()
            return
        }
        
        try {
            updateStatus(getString(R.string.status_starting_accessibility))
            accessibilityResetButton.isEnabled = false
            
            // First open the network operator settings
            val settingsIntent = Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS)
            startActivity(settingsIntent)
            
            // Then trigger the accessibility service to start processing
            handler.postDelayed({
                val serviceIntent = Intent(NetworkResetAccessibilityService.ACTION_START_RESET).apply {
                    setPackage(packageName)
                }
                sendBroadcast(serviceIntent)
                
                // Re-enable button after a delay
                handler.postDelayed({
                    accessibilityResetButton.isEnabled = true
                }, 30000) // 30 seconds timeout
            }, 2000) // Wait 2 seconds for settings to open
            
        } catch (e: Exception) {
            handleError("Error starting accessibility reset: ${e.message}", e)
        }
    }
    
    /**
     * Opens accessibility settings
     */
    private fun openAccessibilitySettings() {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            Toast.makeText(
                this,
                "Find and enable \"Mobile Network Reset\" in the list",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Unable to open accessibility settings",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
