/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * PDFHandlerActivity.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.SmartDeviceApp.Companion.appContext
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager.Companion.clearSandboxPDFName
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager.Companion.createTemporaryPdfFromContentUri
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager.Companion.setHasNewPDFData

class PDFHandlerActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var intent = intent
        if (intent != null) {
            // From Home Screen file picker
            if (intent.getIntExtra(AppConstants.EXTRA_FILE_FROM_PICKER, 1) < 1) {
                val hasNewData = intent.data != null || intent.clipData != null
                clearSandboxPDFName(appContext)
                setHasNewPDFData(appContext, hasNewData)
                intent.setClass(this, MainActivity::class.java)
                handleViewIntentData(intent)
            } else {
                intent.setClass(this, SplashActivity::class.java)
                val action = intent.action
                if (action != null && (action == Intent.ACTION_VIEW || action == Intent.ACTION_SEND_MULTIPLE)) {
                    handleViewIntentData(intent)
                }
            }
        } else {
            intent = Intent(this, SplashActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun handleViewIntentData(intent: Intent) {
        val intentData = intent.data
        val intentClipData = intent.clipData
        if (intentData != null) {
            if (intentData.scheme == FILE_SCHEME || intentData.scheme == CONTENT_SCHEME) {
                intent.data = intentData
            } else { // load the PDF input stream from this activity only in order to handle special "content" URIs that cannot be opened by other activities (such as Gmail attachment URI)
                intent.data = createTemporaryPdfFromContentUri(this, intent.data)
            }
        } else if (intentClipData != null) {
            intent.clipData = intentClipData
        }
    }

    companion object {
        const val FILE_SCHEME = "file"
        const val CONTENT_SCHEME = "content"
    }
}