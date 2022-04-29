/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * BaseWebFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.base

import jp.co.riso.smartdeviceapp.view.base.BaseFragment
import jp.co.riso.smartdeviceapp.view.webkit.SDAWebView
import android.os.Bundle
import jp.co.riso.smartprint.R
import android.os.Build
import android.view.View
import android.webkit.WebView

/**
 * @class BaseWebFragment
 *
 * @brief Base web fragment class
 */
abstract class BaseWebFragment : BaseFragment() {
    protected var mWebView: SDAWebView? = null

    override fun initializeFragment(savedInstanceState: Bundle?) {}

    override fun initializeView(view: View, savedInstanceState: Bundle?) {
        mWebView = view.findViewById(R.id.contentWebView)
        if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
            mWebView!!.setDefaultFocusHighlightEnabled(false)
        }
        configureWebView(mWebView)
        if (isChromeBook && savedInstanceState != null) {
            mWebView!!.restoreState(savedInstanceState)
        } else {
            mWebView!!.loadUrl(urlString!!)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (isChromeBook) {
            mWebView!!.saveState(outState)
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
    abstract val urlString: String?
}