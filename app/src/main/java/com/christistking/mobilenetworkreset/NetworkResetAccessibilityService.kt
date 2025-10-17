package com.christistking.mobilenetworkreset

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Accessibility Service to automate network reset on non-rooted devices.
 * 
 * This service navigates through the Settings UI to automatically perform:
 * 1. Disable "Select automatically"
 * 2. Select a network operator
 * 3. Go back
 * 4. Re-enable "Select automatically"
 */
class NetworkResetAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var isProcessing = false
    private var currentStep = Step.IDLE
    
    companion object {
        private const val TAG = "NetworkResetA11yService"
        const val ACTION_START_RESET = "com.christistking.mobilenetworkreset.START_RESET"
        const val ACTION_STOP_RESET = "com.christistking.mobilenetworkreset.STOP_RESET"
        const val BROADCAST_STATUS_UPDATE = "com.christistking.mobilenetworkreset.STATUS_UPDATE"
        const val EXTRA_STATUS = "status"
        const val EXTRA_STEP = "step"
        
        // Common text patterns to look for in Settings
        private val AUTO_SELECT_PATTERNS = arrayOf(
            "select automatically",
            "automatically select",
            "choose automatically",
            "automatic selection",
            "select network automatically"
        )
        
        private val NETWORK_OPERATOR_PATTERNS = arrayOf(
            "network operators",
            "mobile networks",
            "carrier",
            "operators"
        )
    }
    
    enum class Step {
        IDLE,
        WAITING_FOR_SETTINGS,
        WAITING_FOR_NETWORK_OPERATORS,
        DISABLING_AUTO_SELECT,
        SELECTING_NETWORK,
        GOING_BACK,
        ENABLING_AUTO_SELECT,
        COMPLETE,
        ERROR
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Accessibility service connected")
        
        val info = AccessibilityServiceInfo().apply {
            // We need to observe all events from Settings app
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_CLICKED
            
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            
            // We need to interact with Settings UI
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            
            notificationTimeout = 100
        }
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!isProcessing || event == null) return
        
        Log.d(TAG, "Event: ${event.eventType}, Package: ${event.packageName}, Step: $currentStep")
        
        // Handle different steps based on current state
        when (currentStep) {
            Step.WAITING_FOR_SETTINGS -> handleSettingsScreen(event)
            Step.WAITING_FOR_NETWORK_OPERATORS -> handleNetworkOperatorsScreen(event)
            Step.DISABLING_AUTO_SELECT -> handleDisableAutoSelect(event)
            Step.SELECTING_NETWORK -> handleSelectNetwork(event)
            Step.ENABLING_AUTO_SELECT -> handleEnableAutoSelect(event)
            else -> {}
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
        stopProcessing()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_RESET -> {
                Log.d(TAG, "Starting network reset process")
                startProcessing()
            }
            ACTION_STOP_RESET -> {
                Log.d(TAG, "Stopping network reset process")
                stopProcessing()
            }
        }
        return START_STICKY
    }

    private fun startProcessing() {
        isProcessing = true
        currentStep = Step.WAITING_FOR_SETTINGS
        broadcastStatus("Starting automated network reset", currentStep)
    }

    private fun stopProcessing() {
        isProcessing = false
        currentStep = Step.IDLE
        broadcastStatus("Process stopped", currentStep)
    }

    private fun handleSettingsScreen(event: AccessibilityEvent) {
        // Look for "Network operators" or similar option
        handler.postDelayed({
            val rootNode = rootInActiveWindow ?: return@postDelayed
            
            if (findAndClickNode(rootNode, NETWORK_OPERATOR_PATTERNS)) {
                currentStep = Step.WAITING_FOR_NETWORK_OPERATORS
                broadcastStatus("Found network operators option", currentStep)
            } else {
                Log.d(TAG, "Network operators option not found yet")
            }
            
            rootNode.recycle()
        }, 1000)
    }

    private fun handleNetworkOperatorsScreen(event: AccessibilityEvent) {
        // Look for "Select automatically" switch or checkbox
        handler.postDelayed({
            val rootNode = rootInActiveWindow ?: return@postDelayed
            
            val autoSelectNode = findNodeWithText(rootNode, AUTO_SELECT_PATTERNS)
            if (autoSelectNode != null) {
                // Check if it's enabled (checked)
                val isChecked = autoSelectNode.isChecked
                if (isChecked) {
                    // Disable it
                    if (autoSelectNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                        currentStep = Step.DISABLING_AUTO_SELECT
                        broadcastStatus("Disabling automatic selection", currentStep)
                        
                        // Wait and then select a network
                        handler.postDelayed({
                            currentStep = Step.SELECTING_NETWORK
                            handleSelectNetwork(event)
                        }, 2000)
                    }
                } else {
                    // Already disabled, move to selecting network
                    currentStep = Step.SELECTING_NETWORK
                    handleSelectNetwork(event)
                }
                autoSelectNode.recycle()
            }
            
            rootNode.recycle()
        }, 1000)
    }

    private fun handleDisableAutoSelect(event: AccessibilityEvent) {
        // After disabling, wait for UI to update
        handler.postDelayed({
            currentStep = Step.SELECTING_NETWORK
        }, 2000)
    }

    private fun handleSelectNetwork(event: AccessibilityEvent) {
        // Find and click on the first available network operator
        handler.postDelayed({
            val rootNode = rootInActiveWindow ?: return@postDelayed
            
            // Look for network operators in the list
            val networkNode = findFirstClickableNetwork(rootNode)
            if (networkNode != null) {
                if (networkNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    currentStep = Step.GOING_BACK
                    broadcastStatus("Selected network operator", currentStep)
                    
                    // Wait for selection, then go back
                    handler.postDelayed({
                        performGlobalAction(GLOBAL_ACTION_BACK)
                        currentStep = Step.ENABLING_AUTO_SELECT
                        
                        // Wait and re-enable auto select
                        handler.postDelayed({
                            handleEnableAutoSelect(event)
                        }, 2000)
                    }, 3000)
                }
                networkNode.recycle()
            } else {
                Log.d(TAG, "No network operators found")
            }
            
            rootNode.recycle()
        }, 1000)
    }

    private fun handleEnableAutoSelect(event: AccessibilityEvent) {
        // Re-enable "Select automatically"
        handler.postDelayed({
            val rootNode = rootInActiveWindow ?: return@postDelayed
            
            val autoSelectNode = findNodeWithText(rootNode, AUTO_SELECT_PATTERNS)
            if (autoSelectNode != null && !autoSelectNode.isChecked) {
                if (autoSelectNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    currentStep = Step.COMPLETE
                    broadcastStatus("Network reset complete!", currentStep)
                    
                    // Stop processing after completion
                    handler.postDelayed({
                        stopProcessing()
                    }, 2000)
                }
                autoSelectNode.recycle()
            }
            
            rootNode.recycle()
        }, 1000)
    }

    private fun findNodeWithText(node: AccessibilityNodeInfo, patterns: Array<String>): AccessibilityNodeInfo? {
        val text = node.text?.toString()?.lowercase() ?: ""
        val contentDesc = node.contentDescription?.toString()?.lowercase() ?: ""
        
        for (pattern in patterns) {
            if (text.contains(pattern, ignoreCase = true) || 
                contentDesc.contains(pattern, ignoreCase = true)) {
                return node
            }
        }
        
        // Search children
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findNodeWithText(child, patterns)
            if (result != null) {
                return result
            }
            child.recycle()
        }
        
        return null
    }

    private fun findAndClickNode(node: AccessibilityNodeInfo, patterns: Array<String>): Boolean {
        val foundNode = findNodeWithText(node, patterns)
        if (foundNode != null) {
            val clicked = foundNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            foundNode.recycle()
            return clicked
        }
        return false
    }

    private fun findFirstClickableNetwork(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        // Look for nodes that might be network operators
        // These are typically in a list and have network names
        if (node.isClickable && node.text != null) {
            val text = node.text.toString()
            // Skip the "Select automatically" option
            if (!AUTO_SELECT_PATTERNS.any { text.contains(it, ignoreCase = true) }) {
                return node
            }
        }
        
        // Search children
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findFirstClickableNetwork(child)
            if (result != null) {
                return result
            }
            child.recycle()
        }
        
        return null
    }

    private fun broadcastStatus(status: String, step: Step) {
        Log.d(TAG, "Status: $status, Step: $step")
        val intent = Intent(BROADCAST_STATUS_UPDATE).apply {
            putExtra(EXTRA_STATUS, status)
            putExtra(EXTRA_STEP, step.name)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }
}
