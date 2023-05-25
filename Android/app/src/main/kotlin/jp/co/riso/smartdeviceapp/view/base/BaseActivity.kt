/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * BaseActivity.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.base

import android.hardware.display.DisplayManager.DisplayListener
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.os.Build
import android.os.Handler
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import android.os.Looper
import android.view.*
import androidx.fragment.app.FragmentActivity
import jp.co.riso.smartprint.R
import jp.co.riso.android.util.AppUtils

/**
 * @class BaseActivity
 *
 * @brief Base activity class
 */
abstract class BaseActivity : FragmentActivity() {

    private var _systemUIFlags // Stores initial System UI Visibility flags of device. Initialized and used only on Android 10 Phones.
            = 0
    private var _lastRotation // Stores previous rotation to isolate change in rotation events only
            = 0
    private var _displayListener: DisplayListener? = null
    private var _displayManager: DisplayManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onCreateContent(savedInstanceState)

        /* V2.2 BUG: Display bug occurs only on Android 10 Phones with 2-3 button system navigation enabled when device is rotated.
         * Fix:
         *  - Detect display rotation (landscape to reverse landscape rotation not handled in `onConfigurationChanged()`
         *  - Hide system navigation bar upon rotation (this allows the app to cover the whole screen's width)
         *  - Display system navigation bar again immediately
         */if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !isTablet) {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                // 031521 - For API Level 30 deprecation
                _systemUIFlagsForSDK29
            }
            // RM1139 fix: Use onDisplayChanged. Less delay in detecting rotation changes compared to OnOrientationChange
            if (_displayListener == null) {
                _displayListener = object : DisplayListener {
                    override fun onDisplayAdded(displayId: Int) {}
                    override fun onDisplayRemoved(displayId: Int) {}
                    override fun onDisplayChanged(displayId: Int) {
                        handleSystemUIRotation()
                    }
                }
            }
            _displayManager =
                SmartDeviceApp.appContext?.getSystemService(DISPLAY_SERVICE) as DisplayManager
            if (_displayManager != null) {
                _displayManager!!.registerDisplayListener(
                    _displayListener, Handler(
                        Looper.myLooper()!!
                    )
                )
            }
        }
    }
    // ================================================================================
    // Abstract Functions
    // ================================================================================
    /**
     * @brief Called in onCreate which will serve as the main activity initialization.
     *
     * @param savedInstanceState Bundle which contains a saved state during recreation
     */
    protected abstract fun onCreateContent(savedInstanceState: Bundle?)

    // ================================================================================
    // Private Functions
    // ================================================================================
    private fun handleSystemUIRotation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // RM1132 fix: Use onOrientationChanged to capture rotation events only
            val display = display
            val rotation = display!!.rotation
            if (rotation != _lastRotation) {
                // 031521 - For API Level 30 deprecation
                // RM1132 fix: Add checking for navigation bar
                val metrics = windowManager.currentWindowMetrics
                // Gets all excluding insets
                val windowInsets = metrics.windowInsets
                val insets = windowInsets.getInsets(WindowInsets.Type.systemBars())
                val insetsWidth = insets.right + insets.left
                val insetsHeight = insets.top + insets.bottom
                if (insetsWidth > 0 || insetsHeight > 0) {
                    if (window.insetsController != null) {
                        // Hide system navigation bar
                        window.insetsController!!.hide(WindowInsets.Type.navigationBars())
                        window.insetsController!!.systemBarsBehavior =
                            WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                        val handler = Handler(Looper.myLooper()!!)
                        handler.postDelayed({ 
						    // Show system navigation bar
                            window.insetsController!!.show(WindowInsets.Type.navigationBars())
                        }, 10)
                    }
                }
                _lastRotation = rotation
            }
        } else {
            // 031521 - For API Level 30 deprecation
            handleSystemUIRotationForSDK29()
        }
    }

    @Suppress("DEPRECATION")
    private val _systemUIFlagsForSDK29: Unit
        get() {
            val decorView = window.decorView
            _systemUIFlags = decorView.systemUiVisibility
        }

    @Suppress("DEPRECATION")
    private fun handleSystemUIRotationForSDK29() {
        // RM1132 fix: Use onOrientationChanged to capture rotation events only
        val display = windowManager.defaultDisplay
        val rotation = display.rotation
        if (rotation != _lastRotation) {
            var decorView = window.decorView
            decorView.systemUiVisibility =
                _systemUIFlags or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // Hide system navigation bar
            val handler = Handler(Looper.myLooper()!!)
            handler.postDelayed({
                decorView = window.decorView
                decorView.systemUiVisibility =
                    _systemUIFlags or View.SYSTEM_UI_FLAG_LAYOUT_STABLE // Show system navigation bar
            }, 10)
            _lastRotation = rotation
        }
    }
    // ================================================================================
    // Public Functions
    // ================================================================================
    /**
     * @brief Checks whether the device is in tablet mode.
     *
     * @retval true Device is a tablet
     * @retval false Device is a phone
     */
    val isTablet: Boolean
        get() = resources.getBoolean(R.bool.is_tablet)// Calculate ActionBar height

    /**
     * @brief Gets the action bar height from the android defaults.
     *
     * @return Action bar height in pixels
     */
    val actionBarHeight: Int
        get() =// Calculate ActionBar height
            resources.getDimensionPixelSize(R.dimen.actionbar_height)

    /**
     * @brief Gets the drawer width.
     *
     * @return Drawer width in pixels
     */
    val drawerWidth: Int
        get() {
            val screenSize = AppUtils.getScreenDimensions(this)
            val drawerWidthPercentage =
                resources.getFraction(R.fraction.drawer_width_percentage, 1, 1)
            val minDrawerWidth = resources.getDimensionPixelSize(R.dimen.drawer_width_min)
            val maxDrawerWidth = resources.getDimensionPixelSize(R.dimen.drawer_width_max)
            var drawerWidth = screenSize!!.x.coerceAtMost(screenSize.y) * drawerWidthPercentage
            drawerWidth = drawerWidth.coerceAtLeast(minDrawerWidth.toFloat())
            drawerWidth = drawerWidth.coerceAtMost(maxDrawerWidth.toFloat())
            return drawerWidth.toInt()
        }

    override fun onDestroy() {
        super.onDestroy()
        if (_displayManager != null && _displayListener != null) {
            _displayManager!!.unregisterDisplayListener(_displayListener)
        }
    }
}

// Extension function to check if CTRL key is pressed
fun MotionEvent.isCtrlPressed(): Boolean {
    return (metaState and KeyEvent.META_CTRL_ON) != 0
}