/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * LegalFragment.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.fragment

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import jp.co.riso.android.util.AppUtils.getLocalizedAssetFullPath
import jp.co.riso.android.util.Logger.logStartTime
import jp.co.riso.android.util.Logger.logStopTime
import jp.co.riso.android.util.Logger.logWarn
import jp.co.riso.smartdeviceapp.view.MainActivity
import jp.co.riso.smartdeviceapp.view.base.BaseWebFragment
import jp.co.riso.smartprint.R
import java.util.*

/**
 * @class LegalFragment
 *
 * @brief Web fragment for Legal Screen
 */
class LegalFragment : BaseWebFragment() {

    override val viewLayout: Int
        get() = R.layout.fragment_helplegal

    override fun initializeCustomActionBar(view: View, savedInstanceState: Bundle?) {
        val textView = view.findViewById<TextView>(R.id.actionBarTitle)
        textView.setText(R.string.ids_lbl_legal)
        addActionMenuButton(view)
    }

    @SuppressLint("NewApi") // Difference in injection in Kitkat and previous devices
    override fun configureWebView(webView: WebView?) {
        webView!!.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                logStartTime(activity, LegalFragment::class.java, "Legal Screen load")
            }

            override fun onPageFinished(view: WebView, url: String) {
                logStopTime(activity, LegalFragment::class.java, "Legal Screen load")
                try {
                    if (activity != null && activity is MainActivity) {
                        val packageManager = activity!!.packageManager
                        val appName = activity!!.getString(R.string.ids_app_name)
                        val versionName = packageManager.getPackageInfo(
                            activity!!.packageName, 0
                        ).versionName
                        val javascript = String.format(
                            Locale.getDefault(), JS_REPLACE_FORMAT, VERSION_HTML_ID,
                            versionName, APPNAME_HTML_ID, appName
                        )
                        view.evaluateJavascript(javascript, null)
                    }
                } catch (e: PackageManager.NameNotFoundException) {
                    logWarn(LegalFragment::class.java, "No version name found")
                }
            }
        }
    }

    override val urlString: String
        get() = getUrlString()

    @JvmName("getUrlString1")
    fun getUrlString(): String {
        val htmlFolder = getString(R.string.html_folder)
        val legalHtml = getString(R.string.legal_html)
        return getLocalizedAssetFullPath(activity, htmlFolder, legalHtml)!!
    }

    companion object {
        /// String Format for Javascript replace statement 
        const val JS_REPLACE_FORMAT =
            "javascript:document.getElementById('%s').innerHTML='%s'; javascript:document.getElementById('%s').innerHTML='%s';"

        /// HTML ID for app version name
        const val APPNAME_HTML_ID = "localize_appname"
        const val VERSION_HTML_ID = "localize_version"
    }
}