package com.stretchscreen

import android.os.Build
import java.io.File

/**
 * Utility class for screen-related operations
 */
object ScreenUtils {

    /**
     * Checks if the device is likely rooted
     * This is useful because some advanced screen manipulation may require root access
     */
    fun isDeviceRooted(): Boolean {
        // Check for common root management apps
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/system/xbin/su",
            "/system/bin/su",
            "/sbin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/data/local/su"
        )
        
        // Check if su binary exists
        for (path in paths) {
            if (File(path).exists()) return true
        }
        
        // Check by executing su command
        return try {
            Runtime.getRuntime().exec("su")
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Calculates an appropriate overlay scale based on the width and height ratios
     */
    fun calculateScale(widthRatio: Int, heightRatio: Int): Pair<Float, Float> {
        // Convert percentage to scale factor (100% = 1.0)
        val scaleX = widthRatio / 100f
        val scaleY = heightRatio / 100f
        return Pair(scaleX, scaleY)
    }

    /**
     * Determines if we're running on a version of Android that supports the necessary features
     */
    fun canSupportFullStretchMode(): Boolean {
        // For full system-wide effect, we generally need Android 7.0+ for proper window scaling
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    }
}
