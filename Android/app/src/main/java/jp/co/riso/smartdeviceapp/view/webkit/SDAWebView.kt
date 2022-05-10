/* Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * SDAWebView.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.webkit

import kotlin.jvm.JvmOverloads
import android.webkit.WebView
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet

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
    /**
     * @brief Initializes the WebView with the standard settings
     */
    fun init() {
        if (!isInEditMode) {
            setSettings()
            setLook()
        }
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