package com.stretchscreen

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {

    private lateinit var enableSwitch: Switch
    private lateinit var settingsButton: Button
    private lateinit var statusText: TextView
    private lateinit var infoCard: CardView
    private lateinit var rootRequiredText: TextView
    private lateinit var permissionRequiredCard: CardView
    private lateinit var requestPermissionButton: Button

    companion object {
        private const val OVERLAY_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        enableSwitch = findViewById(R.id.enable_switch)
        settingsButton = findViewById(R.id.settings_button)
        statusText = findViewById(R.id.status_text)
        infoCard = findViewById(R.id.info_card)
        rootRequiredText = findViewById(R.id.root_required_text)
        permissionRequiredCard = findViewById(R.id.permission_required_card)
        requestPermissionButton = findViewById(R.id.request_permission_button)

        // Check if we have necessary permissions
        if (!hasRequiredPermissions()) {
            enableSwitch.isEnabled = false
            permissionRequiredCard.visibility = View.VISIBLE
        } else {
            permissionRequiredCard.visibility = View.GONE
            setupServiceControls()
        }

        // Show root notification if the device likely isn't rooted
        if (!ScreenUtils.isDeviceRooted()) {
            rootRequiredText.visibility = View.VISIBLE
        } else {
            rootRequiredText.visibility = View.GONE
        }

        // Set up click listeners
        requestPermissionButton.setOnClickListener {
            requestOverlayPermission()
        }

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        
        // Update UI based on permissions state when returning to the activity
        if (hasRequiredPermissions()) {
            permissionRequiredCard.visibility = View.GONE
            enableSwitch.isEnabled = true
            setupServiceControls()
        }

        // Update switch state based on if service is running
        if (DisplayService.isRunning) {
            enableSwitch.isChecked = true
            statusText.text = getString(R.string.status_enabled)
        } else {
            enableSwitch.isChecked = false
            statusText.text = getString(R.string.status_disabled)
        }
    }

    private fun setupServiceControls() {
        // Set initial switch state based on service status
        enableSwitch.isChecked = DisplayService.isRunning
        
        // Update status text
        statusText.text = if (DisplayService.isRunning) 
            getString(R.string.status_enabled) else getString(R.string.status_disabled)

        // Switch listener to toggle the service
        enableSwitch.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                if (hasRequiredPermissions()) {
                    startDisplayService()
                    statusText.text = getString(R.string.status_enabled)
                } else {
                    enableSwitch.isChecked = false
                    requestOverlayPermission()
                }
            } else {
                stopDisplayService()
                statusText.text = getString(R.string.status_disabled)
            }
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        // Check for overlay permission on Android 6.0+
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true // Overlay permission not required before Android 6.0
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    permissionRequiredCard.visibility = View.GONE
                    enableSwitch.isEnabled = true
                    setupServiceControls()
                    Toast.makeText(this, R.string.permission_granted, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startDisplayService() {
        val intent = Intent(this, DisplayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopDisplayService() {
        val intent = Intent(this, DisplayService::class.java)
        stopService(intent)
    }
}
