/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * PrintSettingsViewModel.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.viewmodel

import androidx.lifecycle.ViewModel
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings

class PrintSettingsViewModel : ViewModel() {

    private var _printerId = 0
    val printerId: Int
        get() = _printerId

    private var _printSettings = PrintSettings()
    val printSettings: PrintSettings
        get() = _printSettings

    private var _isTargetPrintPreviewFragment = true
    val isTargetFragmentPrintPreview: Boolean
        get() = _isTargetPrintPreviewFragment

    fun setTargetPrintPreviewFragment() {
        _isTargetPrintPreviewFragment = true
    }

    fun setTargetPrintersFragment() {
        _isTargetPrintPreviewFragment = false
    }

    fun setPrinterId(id: Int) {
        _printerId = id
    }

    fun setPrintSettings(settings: PrintSettings) {
        _printSettings = settings
    }

}