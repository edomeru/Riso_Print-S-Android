/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * MemoryUtils.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.android.util

import android.app.Activity
import android.app.ActivityManager
import android.content.Context

/**
 * @class MemoryUtils
 *
 * @brief Utility class for memory operations
 */
object MemoryUtils {
    /**
     * @brief Get cache size.
     *
     * @param activity Activity
     *
     * @return Cache size
     */
    @JvmStatic
    fun getCacheSizeBasedOnMemoryClass(activity: Activity?): Int {
        if (activity == null) {
            return 0
        }
        val manager = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        // Get memory class of this device, exceeding this amount will throw an OutOfMemory exception.
        val memClass = manager.memoryClass
        return 1024 * 1024 * memClass
    }

    /**
     * @brief Get available memory.
     *
     * @param context Application context
     *
     * @return Available memory
     */
    @JvmStatic
    fun getAvailableMemory(context: Context?): Float {
        if (context == null) {
            return Float.NaN
        }
        val mi = ActivityManager.MemoryInfo()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.getMemoryInfo(mi)
        return mi.availMem / 1048576.0f
    }
}