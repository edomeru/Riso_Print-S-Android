/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * PrintSettingsFragment.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.fragment

import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.Message
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import jp.co.riso.android.dialog.DialogUtils
import jp.co.riso.android.dialog.DialogUtils.dismissDialog
import jp.co.riso.android.dialog.DialogUtils.displayDialog
import jp.co.riso.android.dialog.InfoDialogFragment
import jp.co.riso.android.dialog.InfoDialogFragment.Companion.newInstance
import jp.co.riso.android.dialog.WaitingDialogFragment
import jp.co.riso.android.dialog.WaitingDialogFragment.Companion.newInstance
import jp.co.riso.android.dialog.WaitingDialogFragment.WaitingDialogListener
import jp.co.riso.android.os.pauseablehandler.PauseableHandler
import jp.co.riso.android.os.pauseablehandler.PauseableHandlerCallback
import jp.co.riso.android.util.AppUtils
import jp.co.riso.android.util.NetUtils.isWifiAvailable
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.common.DirectPrintManager
import jp.co.riso.smartdeviceapp.common.DirectPrintManager.DirectPrintCallback
import jp.co.riso.smartdeviceapp.controller.jobs.PrintJobManager
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager.Companion.getSandboxPDFName
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.model.PrintJob.JobResult
import jp.co.riso.smartdeviceapp.model.Printer
import jp.co.riso.smartdeviceapp.model.Printer.PortSetting
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings
import jp.co.riso.smartdeviceapp.view.base.BaseFragment
import jp.co.riso.smartdeviceapp.view.printsettings.PrintSettingsView
import jp.co.riso.smartdeviceapp.view.printsettings.PrintSettingsView.PrintSettingsViewInterface
import jp.co.riso.smartprint.R
import java.util.*
import kotlin.math.min

/**
 * @class PrintSettingsFragment
 *
 * @brief Fragment which contains the Print Settings Screen
 */
class PrintSettingsFragment : BaseFragment(), PrintSettingsViewInterface, PauseableHandlerCallback,
    DirectPrintCallback, WaitingDialogListener {
    private var _directPrintManager: DirectPrintManager? = null
    private var _fragmentForPrinting = false
    private var _printerId = PrinterManager.EMPTY_ID
    private var _printSettings: PrintSettings? = null
    private var _printSettingsView: PrintSettingsView? = null
    private var _printSettingsBundle: Bundle? = null
    private var _pdfPath: String? = null
    private var _pdfIsLandscape = false
    private var _pauseableHandler: PauseableHandler? = null
    private var _waitingDialog: WaitingDialogFragment? = null
    private var _printMsg = ""

    override val viewLayout: Int
        get() = R.layout.fragment_printsettings

    override fun initializeFragment(savedInstanceState: Bundle?) {
        retainInstance = true
        if (_printSettings == null) {
            _printSettings = PrintSettings()
        }
        if (_pauseableHandler == null) {
            _pauseableHandler = PauseableHandler(Looper.myLooper(), this)
        }
        _printMsg = resources.getString(R.string.ids_info_msg_printing)
    }

    override fun initializeView(view: View, savedInstanceState: Bundle?) {
        _printSettingsView = view.findViewById(R.id.rootView)
        _printSettingsView!!.setValueChangedListener(this)
        _printSettingsView!!.setInitialValues(_printerId, _printSettings!!)
        _printSettingsView!!.setShowPrintControls(_fragmentForPrinting)
        val textView = view.findViewById<TextView>(R.id.titleTextView)
        textView!!.setText(R.string.ids_lbl_print_settings)
        if (!_fragmentForPrinting) {
            textView.setText(R.string.ids_lbl_default_print_settings)
        }
        if (_printSettingsBundle != null) {
            _printSettingsView!!.restoreState(_printSettingsBundle!!)
            _printSettingsBundle = null
        }
    }

    override fun initializeCustomActionBar(view: View, savedInstanceState: Bundle?) {}

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (_printSettingsView != null) {
            _printSettingsBundle = Bundle()
            _printSettingsView!!.saveState(_printSettingsBundle!!)
        }
    }

    override fun onPause() {
        super.onPause()
        PrinterManager.getInstance(SmartDeviceApp.appContext!!)!!.cancelUpdateStatusThread()
        _pauseableHandler!!.pause()
    }

    override fun onResume() {
        super.onResume()
        _pauseableHandler!!.resume()

        //update strings in case locale has changed         
        _printMsg = getString(R.string.ids_info_msg_printing)
        if (_waitingDialog != null) {
            _waitingDialog!!.setButtonText(getString(R.string.ids_lbl_cancel))
        }
    }
    // ================================================================================
    // Public functions
    // ================================================================================
    /**
     * @brief Sets fragment for printing
     *
     * @param fragmentForPrinting Whether the fragment is for printing or for default print settings
     */
    fun setFragmentForPrinting(fragmentForPrinting: Boolean) {
        _fragmentForPrinting = fragmentForPrinting
    }

    /**
     * @brief Sets the printer ID of the view
     *
     * @param printerId Printer ID
     */
    fun setPrinterId(printerId: Int) {
        _printerId = printerId
    }

    /**
     * @brief Creates a copy of the print settings to be applied as print settings values
     *
     * @param printSettings Print settings to be applied
     */
    fun setPrintSettings(printSettings: PrintSettings?) {
        _printSettings = PrintSettings(printSettings!!)
    }

    /**
     * @brief This method sets the value of mPdfPath
     *
     * @param pdfPath The path of the PDF to be printed
     */
    fun setPdfPath(pdfPath: String?) {
        _pdfPath = pdfPath
    }

    /**
     * @brief This method sets whether the PDF is landscape or not
     *
     * @note Will be passed in the PJL
     *
     * @param pdfIsLandscape Whether PDF is landscape or not
     */
    fun setPDFisLandscape(pdfIsLandscape: Boolean) {
        _pdfIsLandscape = pdfIsLandscape
    }

    // ================================================================================
    // INTERFACE - ValueChangedListener
    // ================================================================================
    override fun onPrinterIdSelectedChanged(printerId: Int) {
        setPrinterId(printerId)

        // set printerId to PrintPreviewFragment
        requireActivity().supportFragmentManager.setFragmentResult(
            PrintPreviewFragment.TAG_RESULT_PRINT_SETTINGS,
            bundleOf(
                RESULT_KEY to RESULT_PRINTER_ID,
                RESULT_PRINTER_ID to printerId)
        )
    }

    override fun onPrintSettingsValueChanged(printSettings: PrintSettings?) {
        setPrintSettings(printSettings)
        if (!_fragmentForPrinting) {
            printSettings!!.savePrintSettingToDB(_printerId)
        }

        // set printSettings to PrintPreviewFragment
        requireActivity().supportFragmentManager.setFragmentResult(
            PrintPreviewFragment.TAG_RESULT_PRINT_SETTINGS,
            bundleOf(
                RESULT_KEY to RESULT_PRINTER_SETTINGS,
                RESULT_PRINTER_SETTINGS to printSettings)
        )
    }

    override fun onPrint(printer: Printer?, printSettings: PrintSettings?) {
        // do not print if mPdfPath is not set
        if (_pdfPath != null && _pdfPath!!.isEmpty()) {
            return
        }
        if (printer == null || printSettings == null) {
            val strMsg = getString(R.string.ids_err_msg_no_selected_printer)
            val btnMsg = getString(R.string.ids_lbl_ok)
            val fragment = newInstance(strMsg, btnMsg)
            displayDialog(requireActivity(), TAG_MESSAGE_DIALOG, fragment)
            return
        }
        if (!isWifiAvailable) {
            val strMsg = getString(R.string.ids_err_msg_network_error)
            val btnMsg = getString(R.string.ids_lbl_ok)
            val fragment = newInstance(strMsg, btnMsg)
            displayDialog(requireActivity(), TAG_MESSAGE_DIALOG, fragment)
            return
        }
        val btnMsg: String
        val jobName = getSandboxPDFName(SmartDeviceApp.appContext)
        _directPrintManager = DirectPrintManager()
        _directPrintManager!!.setCallback(this)
        val appName = resources.getString(R.string.ids_app_name)
        val appVersion = AppUtils.getApplicationVersion(SmartDeviceApp.appContext)
        val userName = AppUtils.ownerName
        // Ver.2.0.4.2 Start
        val hostName = Build.MODEL
        // Ver.2.0.4.2 End
        val ret: Boolean
        val formattedString = printSettings.formattedString(_pdfIsLandscape)
        ret = if (printer.portSetting == PortSetting.LPR) {
            // Ver.2.0.4.2 Start
            //ret = mDirectPrintManager.executeLPRPrint(printer.getName(), appName, appVersion, userName, jobName, mPdfPath, formattedString, printer.getIpAddress());
            _directPrintManager!!.executeLPRPrint(
                printer.name,
                appName,
                appVersion,
                userName,
                jobName,
                _pdfPath,
                formattedString,
                printer.ipAddress,
                hostName
            )
            // Ver.2.0.4.2 End
        } else {
            // Ver.2.0.4.2 Start
            //ret = mDirectPrintManager.executeRAWPrint(printer.getName(), appName, appVersion, userName, jobName, mPdfPath, formattedString, printer.getIpAddress());
            _directPrintManager!!.executeRAWPrint(
                printer.name,
                appName,
                appVersion,
                userName,
                jobName,
                _pdfPath,
                formattedString,
                printer.ipAddress,
                hostName
            )
            // Ver.2.0.4.2 End
        }
        if (ret) {
            btnMsg = getString(R.string.ids_lbl_cancel)
            _waitingDialog = newInstance(null, _printMsg, true, btnMsg, TAG_MESSAGE_DIALOG)
            setResultListenerWaitingDialog(
                requireActivity().supportFragmentManager,
                this,
                PrintPreviewFragment.TAG_WAITING_DIALOG
            )
            displayDialog(requireActivity(), TAG_WAITING_DIALOG, _waitingDialog!!)
        } else {
            val strMsg = getString(R.string.ids_info_msg_print_job_failed)
            btnMsg = getString(R.string.ids_lbl_ok)
            val fragment = newInstance(strMsg, btnMsg)
            displayDialog(requireActivity(), TAG_MESSAGE_DIALOG, fragment)
        }
    }

    // ================================================================================
    // INTERFACE - PauseableHandlerCallback
    // ================================================================================
    override fun storeMessage(message: Message?): Boolean {
        return message!!.what == MSG_PRINT
    }

    override fun processMessage(message: Message?) {
        when (message!!.what) {
            MSG_PRINT -> {
                dismissDialog(requireActivity(), TAG_WAITING_DIALOG)
                val pm = PrintJobManager.getInstance(SmartDeviceApp.appContext!!)
                val filename = getSandboxPDFName(SmartDeviceApp.appContext)
                val fragment: InfoDialogFragment
                val strMsg: String = if (message.arg1 == DirectPrintManager.PRINT_STATUS_SENT) {
                    pm!!.createPrintJob(_printerId, filename, Date(), JobResult.SUCCESSFUL)
                    getString(R.string.ids_info_msg_print_job_successful)
                } else {
                    pm!!.createPrintJob(_printerId, filename, Date(), JobResult.ERROR)
                    getString(R.string.ids_info_msg_print_job_failed)
                }
                val btnMsg: String = getString(R.string.ids_lbl_ok)
                // Show dialog
                fragment = newInstance(strMsg, btnMsg)
                displayDialog(requireActivity(), TAG_MESSAGE_DIALOG, fragment)
            }
        }
    }

    // ================================================================================
    // INTERFACE - DirectPrintCallback
    // ================================================================================
    override fun onNotifyProgress(manager: DirectPrintManager?, status: Int, progress: Float) {
        if (isWifiAvailable) {
            when (status) {
                DirectPrintManager.PRINT_STATUS_ERROR_CONNECTING, DirectPrintManager.PRINT_STATUS_ERROR_SENDING, DirectPrintManager.PRINT_STATUS_ERROR_FILE, DirectPrintManager.PRINT_STATUS_ERROR, DirectPrintManager.PRINT_STATUS_SENT -> {
                    val timerTask: TimerTask = object : TimerTask() {
                        override fun run() {
                            val newMessage = Message.obtain(_pauseableHandler, MSG_PRINT)
                            newMessage.arg1 = status
                            _pauseableHandler!!.sendMessage(newMessage)
                        }
                    }
                    val timer = Timer()
                    timer.schedule(timerTask, PRINT_JOB_SENT_PROGRESS_DIALOG_DELAY.toLong())
                }
                DirectPrintManager.PRINT_STATUS_SENDING -> if (_waitingDialog != null) {
                    val msg = String.format(
                        Locale.getDefault(), "%s %.2f%%", _printMsg, min(
                            progress, 100.0f
                        )
                    )
                    _waitingDialog!!.setMessage(msg)
                }
                DirectPrintManager.PRINT_STATUS_STARTED, DirectPrintManager.PRINT_STATUS_CONNECTING, DirectPrintManager.PRINT_STATUS_CONNECTED, DirectPrintManager.PRINT_STATUS_JOB_NUM_UPDATE -> {}
            }
        } else {
            // cancel Direct Print but considered as failed job
            onCancel()
            val newMessage = Message.obtain(_pauseableHandler, MSG_PRINT)
            _pauseableHandler!!.sendMessage(newMessage)
        }
    }

    // ================================================================================
    // INTERFACE - WaitingDialogListener
    // ================================================================================
    override fun onCancel() {
        if (_directPrintManager != null) {
            _directPrintManager!!.sendCancelCommand()
            _directPrintManager = null
        }
    }

    companion object {
        private const val TAG_WAITING_DIALOG = "dialog_printing"
        private const val TAG_MESSAGE_DIALOG = "dialog_message"
        private const val PRINT_JOB_SENT_PROGRESS_DIALOG_DELAY =
            50 // To allow user to see 100% progress percentage, enforce a 50ms delay before closing the progress dialog after a successful print job
        private const val MSG_PRINT = 0

        const val RESULT_KEY = "result"
        const val RESULT_PRINTER_ID = "printer_id"
        const val RESULT_PRINTER_SETTINGS = "printer_settings"
    }
}