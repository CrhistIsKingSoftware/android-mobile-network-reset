package com.christistking.mobilenetworkreset

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import android.widget.TextView
import android.os.Handler
import android.os.Looper

/**
 * MainActivity for the Mobile Network Reset application.
 * 
 * This activity provides a simple interface to reset mobile network connections
 * by automating the process of toggling network operator selection.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var resetButton: MaterialButton
    private lateinit var settingsButton: MaterialButton
    private lateinit var statusText: TextView
    private lateinit var telephonyManager: TelephonyManager
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        resetButton = findViewById(R.id.resetButton)
        settingsButton = findViewById(R.id.settingsButton)
        statusText = findViewById(R.id.statusText)

        // Initialize telephony manager
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        // Set up button click listeners
        resetButton.setOnClickListener {
            performNetworkReset()
        }

        settingsButton.setOnClickListener {
            openNetworkSettings()
        }
    }

    /**
     * Performs the network reset operation.
     * 
     * This method attempts to reset the mobile network by:
     * 1. Opening the network operator settings
     * 
     * Note: Due to Android security restrictions, automatic toggling of network
     * selection requires system-level permissions that are not available to regular apps.
     * This implementation opens the settings screen for the user to perform the reset manually
     * with guidance.
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

            // Provide user guidance and open network settings
            Toast.makeText(
                this,
                "Opening network settings. Please:\n" +
                        "1. Disable 'Select automatically'\n" +
                        "2. Select a network\n" +
                        "3. Go back\n" +
                        "4. Re-enable 'Select automatically'",
                Toast.LENGTH_LONG
            ).show()

            // Delay opening settings to allow user to read the toast
            handler.postDelayed({
                openNetworkOperatorSettings()
                // Reset button and status after a delay
                handler.postDelayed({
                    updateStatus(getString(R.string.status_idle))
                    resetButton.isEnabled = true
                }, 2000)
            }, 3000)

        } catch (e: Exception) {
            updateStatus(getString(R.string.status_error))
            Toast.makeText(
                this,
                "Error: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            resetButton.isEnabled = true
        }
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
}
