/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * HelpFragment.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import jp.co.riso.android.util.AppUtils.getLocalizedAssetFullPath
import jp.co.riso.android.util.Logger.logStartTime
import jp.co.riso.android.util.Logger.logStopTime
import jp.co.riso.smartdeviceapp.view.base.BaseWebFragment
import jp.co.riso.smartprint.R

/**
 * @class HelpFragment
 *
 * @brief Fragment class for Help Screen
 */
class HelpFragment : BaseWebFragment() {

    override val viewLayout: Int
        get() = R.layout.fragment_helplegal

    override fun initializeCustomActionBar(view: View, savedInstanceState: Bundle?) {
        val textView = view.findViewById<TextView>(R.id.actionBarTitle)
        textView.setText(R.string.ids_lbl_help)
        addActionMenuButton(view)
    }

    override fun configureWebView(webView: WebView?) {
        webView!!.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                logStartTime(activity, HelpFragment::class.java, "Help Screen load")
            }

            override fun onPageFinished(view: WebView, url: String) {
                logStopTime(activity, HelpFragment::class.java, "Help Screen load")
            }
        }
    }

    override val urlString: String
        get() = getUrlString()

    @JvmName("getUrlString1")
    fun getUrlString(): String {
        val htmlFolder = getString(R.string.html_folder)
        val helpHtml = getString(R.string.help_html)
        return getLocalizedAssetFullPath(activity, htmlFolder, helpHtml)!!
    }
}