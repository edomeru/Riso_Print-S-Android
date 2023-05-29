/* Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * SDAWebView.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.webkit

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.InputDevice
import android.view.MotionEvent
import android.webkit.WebView
import jp.co.riso.smartdeviceapp.view.base.isCtrlPressed

/**
 * @class SDAWebView
 *
 * @brief Subclass of WebView class. Sets the standard functionalities of the WebView.
 */
class SDAWebView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : WebView(
    context!!, attrs, defStyle
) {
    // Variables to track touch events
    private var isPanInProgress = false
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    /**
     * @brief Initializes the WebView with the standard settings
     */
    fun init() {
        if (!isInEditMode) {
            setSettings()
            setLook()
        }
    }

    /* ALK70 Support - Mouse: Advanced pointer support
     * Zoom (CTRL + Scroll wheel)
     */
    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        if (event!!.action == MotionEvent.ACTION_SCROLL && event.isCtrlPressed()) {
            val scrollDelta = event.getAxisValue(MotionEvent.AXIS_VSCROLL)

            if (scrollDelta > 0) {
                // Scroll is upward
                zoomIn()
            } else if (scrollDelta < 0) {
                // Scroll is downward
                zoomOut()
            }
            return true
        }
        return super.onGenericMotionEvent(event)
    }

    /* ALK70 Support - Mouse: Advanced pointer support
     * Pan and scroll using mouse and trackpad
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
         // Check if using mouse/trackpad
        if (event!!.isFromSource(InputDevice.SOURCE_MOUSE)) {
            // Detect if the user is panning the WebView.
            if (event.pointerCount == 1) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Touch down event
                        isPanInProgress = true
                        lastTouchX = event.x
                        lastTouchY = event.y
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        // Touch move event
                        if (isPanInProgress) {
                            val deltaX = event.x - lastTouchX
                            val deltaY = event.y - lastTouchY

                            // Perform panning by adjusting the scroll position
                            scrollBy(-deltaX.toInt(), -deltaY.toInt())

                            lastTouchX = event.x
                            lastTouchY = event.y
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        // Touch up or cancel event
                        isPanInProgress = false
                        return true
                    }
                    else -> {}
                }
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * @brief Sets the settings of the WebView to match the default android webview.
     */
    @SuppressLint("SetJavaScriptEnabled") // Javascript is enabled
    private fun setSettings() {
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.javaScriptEnabled = true
    }

    /**
     * @brief Sets the visible style of the WebView
     */
    private fun setLook() {
        scrollBarStyle = SCROLLBARS_OUTSIDE_OVERLAY
        isScrollbarFadingEnabled = true
        setBackgroundColor(Color.WHITE)
    }
    /**
     * @brief Constructs a new WebView with layout parameters and a default style.
     *
     * @param context A Context object used to access application assets
     * @param attrs An AttributeSet passed to our parent
     * @param defStyle The default style resource ID
     */
    /**
     * @brief Constructs a new WebView with layout parameters.
     *
     * @param context A Context object used to access application assets
     * @param attrs An AttributeSet passed to our parent
     */
    /**
     * @brief Constructs a new WebView with a Context object
     *
     * @param context A Context object used to access application assets
     */
    init {
        init()
    }
}