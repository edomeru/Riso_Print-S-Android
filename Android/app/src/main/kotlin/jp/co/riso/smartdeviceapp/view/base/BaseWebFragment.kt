/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * BaseWebFragment.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.base

import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.webkit.WebView
import jp.co.riso.smartdeviceapp.view.webkit.SDAWebView
import jp.co.riso.smartprint.R

/**
 * @class BaseWebFragment
 *
 * @brief Base web fragment class
 */
abstract class BaseWebFragment : BaseFragment() {
    private var _webView: SDAWebView? = null

    override fun initializeFragment(savedInstanceState: Bundle?) {}

    override fun initializeView(view: View, savedInstanceState: Bundle?) {
        _webView = view.findViewById(R.id.contentWebView)
        if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
            _webView!!.defaultFocusHighlightEnabled = false
        }
        configureWebView(_webView)
        if (isChromeBook && savedInstanceState != null) {
            _webView!!.restoreState(savedInstanceState)
        } else {
            _webView!!.loadUrl(urlString)
        }

        _webView!!.setOnGenericMotionListener { _, event ->
            if (event.action == MotionEvent.ACTION_SCROLL && event.isCtrlPressed()) {
                val scrollDelta = event.getAxisValue(MotionEvent.AXIS_VSCROLL)

                if (scrollDelta > 0) {
                    // Scroll is upward
                    _webView!!.zoomIn()
                } else if (scrollDelta < 0) {
                    // Scroll is downward
                    _webView!!.zoomOut()
                }
                return@setOnGenericMotionListener true
            }
            false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (isChromeBook) {
            _webView!!.saveState(outState)
        }
    }

    /**
     * @brief Performs any additional configuration to the webview before URL Loading.
     *
     * @param webView WebView to be configured
     */
    abstract fun configureWebView(webView: WebView?)

    /**
     * @brief Gets the URL to be loaded in the web view.
     *
     * @return URL String
     */
    abstract val urlString: String
}