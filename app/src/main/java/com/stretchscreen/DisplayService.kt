package com.stretchscreen

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat

class DisplayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var layoutParams: WindowManager.LayoutParams
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "StretchScreenChannel"
        var isRunning = false
            private set
        
        private var widthRatio = 100
        private var heightRatio = 100
        
        fun updateRatio(newWidthRatio: Int, newHeightRatio: Int) {
            widthRatio = newWidthRatio
            heightRatio = newHeightRatio
            // The service will check these values periodically
        }
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        
        // Initialize window manager
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        // Get saved settings
        sharedPreferences = getSharedPreferences(SettingsActivity.PREF_NAME, MODE_PRIVATE)
        widthRatio = sharedPreferences.getInt(SettingsActivity.KEY_WIDTH_RATIO, 100)
        heightRatio = sharedPreferences.getInt(SettingsActivity.KEY_HEIGHT_RATIO, 100)
        
        // Create and show the overlay
        createOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }

    private fun createNotification(): Notification {
        // Create notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Stretch Screen Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Create a pending intent that opens the app
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        // Build the notification
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Stretch Screen Active")
            .setContentText("Tap to open settings")
            .setSmallIcon(android.R.drawable.ic_menu_crop)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createOverlay() {
        // Create a transparent view
        overlayView = View(this)
        overlayView.setBackgroundColor(0x00000000) // Fully transparent
        
        // Create layout parameters for the overlay
        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            getOverlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        
        // Set the gravity to top-left
        layoutParams.gravity = Gravity.TOP or Gravity.START
        
        // Apply the current aspect ratio settings
        updateOverlayScale()
        
        // Add the view to the window manager
        try {
            windowManager.addView(overlayView, layoutParams)
        } catch (e: Exception) {
            // Handle potential permission issues or other exceptions
            stopSelf()
        }
        
        // Start a thread to periodically update the overlay scale
        Thread {
            while (isRunning) {
                try {
                    updateOverlayScale()
                    Thread.sleep(1000) // Check every second for changes
                } catch (e: InterruptedException) {
                    break
                } catch (e: Exception) {
                    // Handle any errors
                }
            }
        }.start()
    }
    
    private fun updateOverlayScale() {
        try {
            // Calculate the scale based on the width and height ratios
            val scaleX = widthRatio / 100f
            val scaleY = heightRatio / 100f
            
            // Apply the scale to the view
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // On Android 7.0+, we can use scaling
                overlayView.scaleX = scaleX
                overlayView.scaleY = scaleY
            } else {
                // On older versions, try to modify the layout params
                val displayMetrics = resources.displayMetrics
                layoutParams.width = (displayMetrics.widthPixels * scaleX).toInt()
                layoutParams.height = (displayMetrics.heightPixels * scaleY).toInt()
                windowManager.updateViewLayout(overlayView, layoutParams)
            }
        } catch (e: Exception) {
            // Handle any errors during scaling
        }
    }
    
    private fun getOverlayType(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        
        // Remove the overlay view
        try {
            windowManager.removeView(overlayView)
        } catch (e: Exception) {
            // Handle view removal errors
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
