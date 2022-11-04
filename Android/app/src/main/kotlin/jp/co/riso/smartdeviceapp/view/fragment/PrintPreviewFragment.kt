/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * PrintPreviewFragment.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.fragment

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Point
import android.net.Uri
import android.os.*
import android.os.Build.VERSION.SDK_INT
import android.util.LruCache
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import jp.co.riso.android.dialog.ConfirmDialogFragment
import jp.co.riso.android.dialog.ConfirmDialogFragment.ConfirmDialogListener
import jp.co.riso.android.dialog.DialogUtils.dismissDialog
import jp.co.riso.android.dialog.DialogUtils.displayDialog
import jp.co.riso.android.dialog.InfoDialogFragment.Companion.newInstance
import jp.co.riso.android.dialog.WaitingDialogFragment
import jp.co.riso.android.dialog.WaitingDialogFragment.Companion.newInstance
import jp.co.riso.android.dialog.WaitingDialogFragment.WaitingDialogListener
import jp.co.riso.android.os.pauseablehandler.PauseableHandler
import jp.co.riso.android.os.pauseablehandler.PauseableHandlerCallback
import jp.co.riso.android.util.AppUtils
import jp.co.riso.android.util.FileUtils
import jp.co.riso.android.util.ImageUtils
import jp.co.riso.android.util.MemoryUtils.getCacheSizeBasedOnMemoryClass
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.SmartDeviceApp.Companion.appContext
import jp.co.riso.smartdeviceapp.controller.pdf.PDFConverterManager
import jp.co.riso.smartdeviceapp.controller.pdf.PDFConverterManagerInterface
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager.Companion.cleanupCachedPdf
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager.Companion.clearSandboxPDFName
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager.Companion.getSandboxPDFName
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManagerInterface
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings
import jp.co.riso.smartdeviceapp.view.MainActivity
import jp.co.riso.smartdeviceapp.view.base.BaseFragment
import jp.co.riso.smartdeviceapp.view.preview.PreviewControlsListener
import jp.co.riso.smartdeviceapp.view.preview.PrintPreviewView
import jp.co.riso.smartdeviceapp.viewmodel.PrintSettingsViewModel
import jp.co.riso.smartprint.R
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.*
import kotlin.math.min

/**
 * @class PrintPreviewFragment
 *
 * @brief Fragment which contains the Print Preview Screen
 */
class PrintPreviewFragment : BaseFragment(), Handler.Callback, PDFFileManagerInterface,
    PDFConverterManagerInterface, PreviewControlsListener, OnSeekBarChangeListener,
    PauseableHandlerCallback, ConfirmDialogListener, WaitingDialogListener, View.OnKeyListener,
    OnFocusChangeListener {

    private var _pdfManager: PDFFileManager? = null
    private var _pdfConverterManager: PDFConverterManager? = null
    private var _printerId = PrinterManager.EMPTY_ID
    private var _printSettings: PrintSettings? = null
    private var _printPreviewView: PrintPreviewView? = null
    private var _pageControls: View? = null
    private var _seekBar: SeekBar? = null
    private var _pageLabel: TextView? = null
    private var _progressBar: ProgressBar? = null
    private var _openInView: View? = null
    private var _currentPage = 0
    private var _zoomLevel = 1.0f
    private var _panX = 0.0f
    private var _panY = 0.0f
    private var _bmpCache: LruCache<String, Bitmap>? = null
    private var _handler: Handler? = null
    private var _pauseableHandler: PauseableHandler? = null
    private var _confirmDialogFragment: ConfirmDialogFragment? = null
    private var _isPermissionDialogOpen = false
    private var _isPdfInitialized = false
    private var _isConverterInitialized = false
    private var _shouldDisplayExplanation = false
    private var _intentData: Uri? = null
    private var _inputStream: InputStream? = null
    private var _filenameFromContent: String? = null

    // BTS ID#20039: This flag controls if page index should reset to 1 on PDF
    // initialization. Currently only reset after allowing storage permission to
    // prevent setting to page zero when there is already a PDF file
    private var _shouldResetToFirstPageOnInitialize = false
    private var _hasConversionError = false
    private var _waitingDialog: WaitingDialogFragment? = null
    override val viewLayout: Int
        get() = R.layout.fragment_printpreview

    private val _printSettingsViewModel: PrintSettingsViewModel by activityViewModels()
    private var _permissionType: String = WRITE_EXTERNAL_STORAGE

    override fun initializeFragment(savedInstanceState: Bundle?) {
        // dismiss permission alert dialog if showing
        dismissDialog(requireActivity(), TAG_PERMISSION_DIALOG)
        if (savedInstanceState != null) {
            _currentPage = savedInstanceState.getInt(KEY_CURRENT_PAGE, 0)
            _isPermissionDialogOpen =
                savedInstanceState.getBoolean(KEY_EXTERNAL_STORAGE_DIALOG_OPEN, false)
        }
        val intent = requireActivity().intent
        if (intent != null) {
            _intentData = intent.data
            val fileFromPickerFlag = intent.getIntExtra(AppConstants.EXTRA_FILE_FROM_PICKER, 1)

            // Check if from Home Screen OR opened-in file is not PDF
            if (_intentData != null &&
                FileUtils.getMimeType(activity, _intentData) != AppConstants.DOC_TYPES[0] ||
                fileFromPickerFlag < 0 ||
                intent.clipData != null
            ) {
                val clipData = intent.clipData
                if (_pdfConverterManager == null) {
                    _pdfConverterManager =
                        PDFConverterManager(appContext!!, this)
                }
                // Check if multiple images
                if (clipData != null) {
                    _pdfConverterManager!!.setImageFile(clipData)
                    if (clipData.itemCount > 1) {
                        _filenameFromContent = AppConstants.MULTI_IMAGE_PDF_FILENAME
                    } else {
                        try {
                            _filenameFromContent = FileUtils.getFileName(
                                appContext,
                                clipData.getItemAt(0).uri,
                                true
                            )
                        } catch (e: SecurityException) {
                            e.printStackTrace()
                        }
                    }
                } // Check if single image
                else if (_intentData != null && ImageUtils.isImageFileSupported(
                        activity,
                        _intentData
                    )
                ) {
                    _pdfConverterManager!!.setImageFile(_intentData)
                    // check if captured image from camera
                    if (fileFromPickerFlag == HomeFragment.IMAGE_FROM_CAMERA) {
                        _filenameFromContent = AppConstants.CONST_IMAGE_CAPTURED_FILENAME
                    } else {
                        try {
                            _filenameFromContent =
                                FileUtils.getFileName(appContext, _intentData, true)
                        } catch (e: SecurityException) {
                            e.printStackTrace()
                        }
                    }
                } // Check text file
                else if (_intentData != null && FileUtils.getMimeType(
                        activity, _intentData
                    ) == AppConstants.DOC_TYPES[1]
                ) {
                    _pdfConverterManager!!.setTextFile(_intentData)
                    try {
                        _filenameFromContent = FileUtils.getFileName(appContext, _intentData, true)
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                    }
                } else {    // Invalid format
                    _pdfConverterManager = null
                }
                if (_pdfConverterManager != null) {
                    // mIntentData should not be null and will make uri scheme 'file'
                    _intentData = Uri.fromFile(
                        File(
                            requireActivity().cacheDir,
                            AppConstants.CONST_TEMP_PDF_PATH
                        )
                    )
                }
            } else if (getSandboxPDFName(appContext) == null && _intentData != null && _intentData!!.scheme != "file"
            ) {
                //resolve content if not from Sandbox or File
                try {
                    val c = requireActivity().contentResolver
                    _inputStream = c.openInputStream(_intentData!!)
                    _filenameFromContent = FileUtils.getFileName(appContext, _intentData, false)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
        }
        _handler = Handler(Looper.myLooper()!!, this)
        if (_pauseableHandler == null) {
            _pauseableHandler = PauseableHandler(Looper.myLooper(), this)
        }

        // Check if device is Android 13 for permissions
        if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            _permissionType = READ_MEDIA_IMAGES
        }

        // Initialize PDF File Manager if it has not been previously initialized yet
        if (_pdfManager == null) {
            _pdfManager = PDFFileManager(this)

            // if has PDF to open and the Android permission dialog is not yet opened, check permission
            if (hasPdfFile() && !_isPermissionDialogOpen) {
                if (ContextCompat.checkSelfPermission(
                        requireActivity(),
                        _permissionType
                    )
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    if (_pdfConverterManager != null) {
                        initializePdfConverterAndRunAsync()
                    }
                    initializePdfManagerAndRunAsync()
                } else {
                    if (shouldShowRequestPermissionRationale(_permissionType)) {
                        _shouldDisplayExplanation = true
                    } else {
                        _isPermissionDialogOpen = true
                        // Request the permission, no explanation needed
                        _resultLauncherPermissionStorage.launch(
                            arrayOf(_permissionType))
                    }
                }
            }
        }
        if (_printerId == PrinterManager.EMPTY_ID) {
            _printerId = PrinterManager.getInstance(appContext!!)!!.defaultPrinter
            if (_printerId != -1) {
                val printerType =
                    PrinterManager.getInstance(appContext!!)!!.getPrinterType(_printerId)
                _printSettings = PrintSettings(_printerId, printerType!!)
            }
        }

        // Initialize Print Settings if it has not been previously initialized yet
        if (_printSettings == null) {
            var printerType = AppConstants.PRINTER_MODEL_IS
            if (_printerId != -1) {
                printerType = PrinterManager.getInstance(appContext!!)!!.getPrinterType(_printerId)!!
            }
            _printSettings = PrintSettings(printerType)
        }
        if (_bmpCache == null) {
            var cacheSize = getCacheSizeBasedOnMemoryClass(requireActivity())
            cacheSize = cacheSize shr AppConstants.APP_BMP_CACHE_PART // 1/8
            _bmpCache = object : LruCache<String, Bitmap>(cacheSize) {
                override fun sizeOf(key: String?, value: Bitmap): Int {
                    return value.byteCount
                }
            }
            _bmpCache?.evictAll()
        }
    }

    /**
     * @brief Checks if there is a PDF file for display
     *
     * @return true if there is a PDF in sandbox or from open-in
     * false if app is opened without PDF to be displayed
     */
    private fun hasPdfFile(): Boolean {
        val intent = requireActivity().intent
        val isOpenIn = intent != null && (intent.data != null || intent.clipData != null)
        val isInSandbox = getSandboxPDFName(appContext) != null
        return isInSandbox || isOpenIn
    }

    /**
     * @brief Initializes the PDF Converter and runs the PDF task asynchronously
     */
    private fun initializePdfConverterAndRunAsync() {
        _isConverterInitialized = true
        _pdfConverterManager!!.initializeAsync()
    }

    /**
     * @brief Initializes the PDF Manager and runs the PDF task asynchronously
     */
    private fun initializePdfManagerAndRunAsync() {
        _isPdfInitialized = true
        (requireActivity() as MainActivity).initializeRadaee()
        val pdfInSandbox = getSandboxPDFName(appContext)
        if (pdfInSandbox != null) {
            _pdfManager!!.setSandboxPDF()
            // Automatically open asynchronously
            _pdfManager!!.initializeAsync(null)
        } else if (_intentData != null) {
            if (_intentData!!.scheme == "file") {
                _pdfManager!!.setPDF(_intentData!!.path)
                // Automatically open asynchronously
                _pdfManager!!.initializeAsync(null)
            } else {
                _pdfManager!!.setPDF(null)
                // Automatically open asynchronously
                _pdfManager!!.initializeAsync(_inputStream)
            }
        }
    }

    private inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
        SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelable(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelable(key) as? T
    }

    override fun initializeView(view: View, savedInstanceState: Bundle?) {
        _printPreviewView = view.findViewById(R.id.printPreviewView)
        _printPreviewView!!.setPdfManager(_pdfManager)
        //mPrintPreviewView.setShow3Punch(isPrinterJapanese());
        _printPreviewView!!.setPrintSettings(_printSettings)

        // Clear mBmpCache when Display Size is changed dynamically (Screen Zoom in Android 7)
        if (savedInstanceState != null) {
            val oldScreen = savedInstanceState.parcelable<Point>(KEY_SCREEN_SIZE)
            if (AppUtils.getScreenDimensions(requireActivity()) != oldScreen
                && _bmpCache != null
            ) {
                _bmpCache!!.evictAll()
            }
        }
        _printPreviewView!!.setBmpCache(_bmpCache)
        _printPreviewView!!.setListener(this)
        _printPreviewView!!.setPans(_panX, _panY)
        _printPreviewView!!.setZoomLevel(_zoomLevel)
        _printPreviewView!!.visibility = View.GONE
        _pageControls = view.findViewById(R.id.previewControls)
        _pageLabel = _pageControls!!.findViewById(R.id.pageDisplayTextView)
        _seekBar = _pageControls!!.findViewById(R.id.pageSlider)
        _seekBar!!.setOnSeekBarChangeListener(this)
        _seekBar!!.setOnKeyListener(this)
        _seekBar!!.onFocusChangeListener = this
        if (_currentPage != 0) {
            _printPreviewView!!.currentPage = _currentPage
        }
        if (_pdfManager!!.isInitialized) {
            _printPreviewView!!.refreshView()
            setTitle(view, _pdfManager!!.fileName)
        }

        _openInView = view.findViewById(R.id.openInView)
        _progressBar = view.findViewById(R.id.pdfLoadIndicator)
        _progressBar!!.visibility = View.GONE

        setPrintPreviewViewDisplayed(view, _pdfManager!!.fileName != null)
        val newMessage = Message.obtain(_handler, 0)
        newMessage.arg1 = _printPreviewView!!.visibility
        _printPreviewView!!.visibility = View.GONE
        _handler!!.sendMessage(newMessage)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (_printPreviewView != null) {
            _currentPage = _printPreviewView!!.currentPage
            _printPreviewView!!.freeResources()
            _printPreviewView = null
        }

        // When there is an ongoing PDF conversion and user selects file from Open-In,
        // onDestroyView() gets called. Stop ongoing conversion.
        if (_waitingDialog != null && _pdfConverterManager != null) {
            _pdfConverterManager!!.cancel()
            _pdfConverterManager = null
            _waitingDialog = null
        }
    }

    override fun initializeCustomActionBar(view: View, savedInstanceState: Bundle?) {
        setDefaultTitle(view)
        addActionMenuButton(view)
        addPrintButton(view)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (_printPreviewView != null) {
            _currentPage = _printPreviewView!!.currentPage
            outState.putInt(KEY_CURRENT_PAGE, _currentPage)
            outState.putParcelable(KEY_SCREEN_SIZE, AppUtils.getScreenDimensions(requireActivity()))
        }
        outState.putBoolean(KEY_EXTERNAL_STORAGE_DIALOG_OPEN, _isPermissionDialogOpen)
    }

    override fun clearIconStates() {
        super.clearIconStates()
        setIconState(R.id.view_id_print_button, false)
    }

    override fun onResume() {
        super.onResume()
        if (hasPdfFile() && !_isPermissionDialogOpen) {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    _permissionType
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                // to enable PDF display when Storage permission is switched from disabled to enabled
                if (_confirmDialogFragment != null) {
                    dismissDialog(requireActivity(), TAG_PERMISSION_DIALOG)
                }
                if (_pdfConverterManager != null && !_isConverterInitialized) {
                    initializePdfConverterAndRunAsync()
                }
                if (!_isPdfInitialized) {
                    initializePdfManagerAndRunAsync()
                }
            } else if (_shouldDisplayExplanation) {
                val mainActivity = requireActivity() as MainActivity
                val handler = Handler(Looper.getMainLooper())
                val printSettingsDisposer = Runnable {
                    showPrintSettingsButton(view, false)
                    if (mainActivity.isDrawerOpen(Gravity.RIGHT)) {
                        mainActivity.closeDrawers()
                    }
                }
                // Call with a no-delay handler since not calling inside
                // a main looper handler does not close the drawer
                handler.postDelayed(printSettingsDisposer, 0)
                clearSandboxPDFName(requireActivity())

                // Display an explanation to the user that the permission is needed
                if (_confirmDialogFragment == null) {
                    val message =
                        requireActivity().getString(R.string.ids_err_msg_storage_permission_not_allowed)
                    val positiveButton = requireActivity().getString(R.string.ids_lbl_ok)
                    _confirmDialogFragment =
                        ConfirmDialogFragment.newInstance(message, positiveButton, null, TAG_PERMISSION_DIALOG)
                    setResultListenerConfirmDialog(
                        requireActivity().supportFragmentManager,
                        this,
                        TAG_PERMISSION_DIALOG
                    )
                    displayDialog(
                        requireActivity(),
                        TAG_PERMISSION_DIALOG,
                        _confirmDialogFragment!!
                    )
                }
            }
        }
        _shouldDisplayExplanation = false
        val printerManager = PrinterManager.getInstance(appContext!!)
        val defaultPrinterId = printerManager!!.defaultPrinter
        val isFirstPrinterAdded =
            _printerId == PrinterManager.EMPTY_ID && defaultPrinterId != PrinterManager.EMPTY_ID
        val isCurrentPrinterRemoved =
            _printerId != PrinterManager.EMPTY_ID && !printerManager.isExists(_printerId)
        // OnResume, update printer settings to default printer settings (or IS printer settings when 
        // printer list is empty) only when the 1st printer is added or the selected printer is removed
        // this is to prevent unnecessary reload/refresh of the page preview
        if (isFirstPrinterAdded || isCurrentPrinterRemoved) {
            setPrintId(defaultPrinterId)
            val printerType = PrinterManager.getInstance(appContext!!)!!.getPrinterType(_printerId)

            //use fallthrough printer type IS, if printer does not yet exist
            setPrintSettings(
                PrintSettings(
                    _printerId,
                    printerType ?: AppConstants.PRINTER_MODEL_IS
                )
            )
        }
        if (_pauseableHandler != null) {
            _pauseableHandler!!.resume()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (_pauseableHandler != null) {
            _pauseableHandler!!.resume()
        }
        if (_pageControls != null) {
            val mainView = requireView().findViewById<LinearLayout>(R.id.previewView)
            mainView!!.removeView(_pageControls)
            val newView = View.inflate(requireActivity(), R.layout.preview_controls, null)
            newView.layoutParams = _pageControls!!.layoutParams
            newView.visibility = _pageControls!!.visibility

            // AppUtils.changeChildrenFont((ViewGroup) newView, SmartDeviceApp.getAppFont());
            mainView.addView(newView)
            _pageControls = newView
            _pageLabel = _pageControls!!.findViewById(R.id.pageDisplayTextView)
            _seekBar = _pageControls!!.findViewById(R.id.pageSlider)
            _seekBar!!.setOnSeekBarChangeListener(this)
            _seekBar!!.setOnKeyListener(this)
            _seekBar!!.onFocusChangeListener = this
            if (_pageControls!!.visibility == View.VISIBLE) {
                updateSeekBar()
                updatePageLabel()
            }
        }
    }
    // ================================================================================
    // Public functions
    // ================================================================================
    /**
     * @brief Add a print button in the action bar
     *
     * @param v Root view which contains the action bar view
     */
    private fun addPrintButton(v: View?) {
        addMenuButton(
            v!!,
            R.id.rightActionLayout,
            R.id.view_id_print_button,
            R.drawable.selector_actionbar_printsettings,
            this
        )
    }

    /**
     * @brief Sets printer id
     *
     * @param printerId ID of the printer to be set
     */
    private fun setPrintId(printerId: Int) {
        _printerId = printerId
        if (_printPreviewView != null) {
            _printPreviewView!!.refreshView()
        }
    }

    /**
     * @brief Creates a copy of the print settings and applies it in the view
     *
     * @param printSettings Print settings to be applied
     */
    private fun setPrintSettings(printSettings: PrintSettings?) {
        _printSettings = printSettings?.let { PrintSettings(it) }
        if (_printPreviewView != null) {
            _printPreviewView!!.setPrintSettings(_printSettings)
            _printPreviewView!!.refreshView()
            updateSeekBar()
            updatePageLabel()
        }
    }

    /**
     * @brief Checks if the printer set is Japanese. Uses the punch 3 capability
     *
     * @return true 3-punch in the printer is available
     * @return false 3-punch in the printer is not available available or no printer is set
     */
    /* -- unused method
    open fun isPrinterJapanese(): Boolean {
        if (_printerId === PrinterManager.EMPTY_ID) {
            return false
        }
        val mList: MutableList<Printer>? = PrinterManager.getInstance(activity).savedPrintersList
        for (printer in mList!!) {
            if (printer.id === _printerId) {
                return printer.config.isPunch3Available()
            }
        }
        return false
    }*/

    /**
     * @brief Shows/Hide the Print Preview View.
     *
     * @param v View which contains the Print Preview components
     * @param show Show or hide the Preview
     */
    private fun setPrintPreviewViewDisplayed(v: View?, show: Boolean) {
        _progressBar!!.visibility = View.GONE
        setDefaultTitle(v)
        if (show) {
            _openInView!!.visibility = View.GONE
            if (!_pdfManager!!.isInitialized) {
                _printPreviewView!!.visibility = View.GONE
                _pageControls!!.visibility = View.INVISIBLE
                _progressBar!!.visibility = View.VISIBLE
                showPrintSettingsButton(v, false)
            } else {
                _printPreviewView!!.visibility = View.VISIBLE
                _pageControls!!.visibility = View.VISIBLE
                _printPreviewView!!.reconfigureCurlView()
                _printPreviewView!!.refreshView()
                updateSeekBar()
                updatePageLabel()
                showPrintSettingsButton(v, true)
                setTitle(v, _pdfManager!!.fileName)
            }
        } else {
            _openInView!!.visibility = View.VISIBLE
            _printPreviewView!!.visibility = View.GONE
            _pageControls!!.visibility = View.INVISIBLE
            showPrintSettingsButton(v, false)
        }
    }

    /**
     * @brief Sets the default tile of the view (Preview)
     *
     * @param v Root view which contains the action bar view
     */
    private fun setDefaultTitle(v: View?) {
        setTitle(v, resources.getString(R.string.ids_lbl_print_preview))
    }

    /**
     * @brief Sets the default tile of the view
     *
     * @param v Root view which contains the action bar view
     * @param title String to be displayed in the title bar
     */
    fun setTitle(v: View?, title: String?) {
        val textView = v!!.findViewById<TextView>(R.id.actionBarTitle)
        textView.text = title
    }

    /**
     * @brief Shows/Hide the Print Settings button
     *
     * @param v View which contains the Print Settings button
     * @param show Show or hide the button
     */
    private fun showPrintSettingsButton(v: View?, show: Boolean) {
        if (v!!.findViewById<View?>(R.id.view_id_print_button) != null) {
            if (show) {
                v.findViewById<View>(R.id.view_id_print_button).visibility =
                    View.VISIBLE
            } else {
                v.findViewById<View>(R.id.view_id_print_button).visibility =
                    View.GONE
            }
        }
    }

    /**
     * @brief Indicate if conversion is ongoing
     *
     * @return true If text or image conversion is ongoing
     * @return false If text or image conversion is finished or not ongoing
     */
    val isConversionOngoing: Boolean
        get() = _pdfConverterManager != null
    // ================================================================================
    // Private functions
    // ================================================================================
    /**
     * @brief Get the PDF error message associated with the status
     *
     * @param status PDF open status
     *
     * @retval ids_err_msg_pdf_encrypted Encrypted status
     * @retval ids_err_msg_pdf_printing_not_allowed Print restricted status
     * @retval ids_err_msg_open_failed Open failed status
     * @retval Empty No error status
     */
    private fun getPdfErrorMessage(status: Int): String {
        return when (status) {
            PDFFileManager.PDF_ENCRYPTED -> resources.getString(R.string.ids_err_msg_pdf_encrypted)
            PDFFileManager.PDF_PRINT_RESTRICTED -> resources.getString(R.string.ids_err_msg_pdf_printing_not_allowed)
            PDFFileManager.PDF_OPEN_FAILED -> resources.getString(R.string.ids_err_msg_open_failed)
            PDFFileManager.PDF_NOT_ENOUGH_FREE_SPACE -> resources.getString(R.string.ids_err_msg_not_enough_space)
            else -> ""
        }
    }

    /**
     * @brief Get the PDF Conversion error message associated with the status
     *
     * @param status Conversion status
     *
     * @retval ids_err_msg_conversion_failed Conversion failed
     * @retval ids_err_msg_invalid_file_selection Unsupported file format included in multiple selection
     * @retval Empty No error status
     */
    private fun getConversionErrorMessage(status: Int): String {
        return when (status) {
            PDFConverterManager.CONVERSION_FAILED -> resources.getString(R.string.ids_err_msg_conversion_failed)
            PDFConverterManager.CONVERSION_UNSUPPORTED -> resources.getString(R.string.ids_err_msg_invalid_file_selection)
            PDFConverterManager.CONVERSION_EXCEED_TEXT_FILE_SIZE_LIMIT -> resources.getString(
                R.string.ids_err_msg_txt_size_limit
            )
            PDFConverterManager.CONVERSION_SECURITY_ERROR -> resources.getString(R.string.ids_err_msg_permission)
            PDFConverterManager.CONVERSION_FILE_NOT_FOUND -> resources.getString(R.string.ids_err_msg_open_failed)
            else -> ""
        }
    }

    /**
     * @brief Transitions Screen to Home
     */
    private fun transitionToHomeScreen() {
        val activity = requireActivity() as MainActivity
        var menuFragment = activity.menuFragment
        if (menuFragment == null) {
            val ft = activity.supportFragmentManager.beginTransaction()
            menuFragment = MenuFragment()
            // When MenuFragment gets destroyed due to config change such as changing permissions
            // from Settings, the state gets reset to default as well.
            // Since we know we are in PrintPreview, set the current state to PrintPreview
            menuFragment.mState = MenuFragment.STATE_PRINTPREVIEW
            ft.replace(R.id.leftLayout, menuFragment, "fragment_menu")
            ft.commit()
        }
        menuFragment.hasPdfFile = false
        menuFragment.setCurrentState(MenuFragment.STATE_HOME)
    }
    // ================================================================================
    // Page Control functions
    // ================================================================================
    /**
     * @brief Update the Seek Bar to match the Preview displayed
     */
    private fun updateSeekBar() {
        val currentPage = _printPreviewView!!.currentPage
        var pageCount = _printPreviewView!!.pageCount
        if (!_printPreviewView!!.allowLastPageCurl) {
            pageCount--
        }
        _seekBar!!.max = 0
        _seekBar!!.max = pageCount
        // keyboard increment is updated based on range so this must be reset when range changes
        _seekBar!!.keyProgressIncrement = 1
        updateSeekBarProgress(currentPage)
    }

    /**
     * @brief Sets the current seek bar progress to the specified value
     *
     * @param index Value to be set on the seek bar
     */
    private fun updateSeekBarProgress(index: Int) {
        _seekBar!!.progress = index
    }

    /**
     * @brief Updates the displayed page label to match the Preview displayed
     */
    private fun updatePageLabel() {
        _pageLabel!!.text = _printPreviewView!!.pageString
    }

    // ================================================================================
    // INTERFACE - View.OnKeyListener
    // ================================================================================
    override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
        if (v.id == R.id.pageSlider && event.action == KeyEvent.ACTION_UP &&
            (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
        ) {
            _printPreviewView!!.currentPage = _seekBar!!.progress
        }
        return false
    }

    // ================================================================================
    // INTERFACE - View.OnFocusChangeListener
    // ================================================================================
    override fun onFocusChange(v: View, hasFocus: Boolean) {
        // when limits are reached, arrow keys will move the focus to other buttons instead of calling onKey
        // if focus moves out of page slider, update page to 1st or last page
        if (v.id == R.id.pageSlider && !hasFocus) {
            val progress = _seekBar!!.progress
            if (progress == 0 || progress == _seekBar!!.max) {
                _printPreviewView!!.currentPage = progress
            }
        }
    }

    // ================================================================================
    // INTERFACE - View.OnLayoutChangeListener
    // ================================================================================
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.view_id_print_button) {
            val msg = Message.obtain(_pauseableHandler, R.id.view_id_print_button)
            msg.obj = v
            _pauseableHandler!!.sendMessage(msg)
        } else if (id == R.id.menu_id_action_button) {
            _pauseableHandler!!.sendEmptyMessage(R.id.menu_id_action_button)
        }
    }

    // ================================================================================
    // INTERFACE - PDFFileManagerInterface
    // ================================================================================
    override fun onFileInitialized(status: Int) {
        if (!isDetached && view != null && !_hasConversionError) {
            setPrintPreviewViewDisplayed(view, false)
            when (status) {
                PDFFileManager.PDF_OK -> {
                    if (_filenameFromContent != null) {
                        _pdfManager!!.fileName = _filenameFromContent
                    }
                    setPrintPreviewViewDisplayed(view, true)
                    if (_shouldResetToFirstPageOnInitialize) {
                        _shouldResetToFirstPageOnInitialize = false
                        _printPreviewView!!.currentPage = 0
                        onIndexChanged(0)
                    }
                }
                else -> {
                    val message = getPdfErrorMessage(status)
                    val button = resources.getString(R.string.ids_lbl_ok)
                    displayDialog(
                        requireActivity(),
                        FRAGMENT_TAG_DIALOG,
                        newInstance(message, button)
                    )

                    // If there are any initialization/ conversion errors, ensure that any data gets deleted as this is checked in MenuFragment (sidebar)
                    val intent = requireActivity().intent
                    intent.action = null
                    intent.clipData = null
                    intent.data = null
                    transitionToHomeScreen()
                }
            }
            cleanupCachedPdf(requireActivity())
        }
    }

    // ================================================================================
    // INTERFACE - PDFConverterManagerInterface
    // ================================================================================
    override fun onFileConverted(status: Int) {
        val intent = requireActivity().intent
        if (!isDetached && view != null) {
            when (status) {
                PDFConverterManager.CONVERSION_OK -> {
                    dismissDialog(requireActivity(), TAG_WAITING_DIALOG)
                    _intentData = Uri.fromFile(_pdfConverterManager!!.destFile)
                    // prevent conversion repeatedly on app reopen
                    intent.action = null
                    intent.clipData = null
                    intent.data = _intentData
                    intent.putExtra(AppConstants.EXTRA_FILE_FROM_PICKER, 1)
                    _pdfConverterManager = null
                }
                else -> {
                    dismissDialog(requireActivity(), TAG_WAITING_DIALOG)
                    _hasConversionError = true
                    val message = getConversionErrorMessage(status)
                    val button = resources.getString(R.string.ids_lbl_ok)
                    displayDialog(
                        requireActivity(),
                        FRAGMENT_TAG_DIALOG,
                        newInstance(message, button)
                    )

                    // If there are any initialization/ conversion errors, ensure that any data gets deleted as this is checked in MenuFragment (sidebar)
                    intent.action = null
                    intent.clipData = null
                    intent.data = null
                    transitionToHomeScreen()
                }
            }
        }
    }

    override fun onNotifyProgress(current: Int, total: Int, isPercentage: Boolean) {
        val msg: String = if (isPercentage) {
            val progress = current.toFloat() / total.toFloat() * 100.0f
            String.format(
                Locale.getDefault(),
                "%s %.2f%%",
                resources.getString(R.string.ids_info_msg_converting),
                min(progress, 100f)
            )
        } else {
            String.format(
                Locale.getDefault(),
                "%s %d/%d %s",
                resources.getString(R.string.ids_info_msg_converting),
                current,
                total,
                resources.getString(R.string.ids_info_msg_image)
            )
        }
        onNotifyProgress(msg)
    }

    override fun onNotifyProgress(message: String?) {
        if (_waitingDialog != null) {
            _waitingDialog!!.setMessage(message)
        } else {
            _waitingDialog = newInstance(null, message, true, getString(R.string.ids_lbl_cancel), TAG_WAITING_DIALOG)
            requireActivity().runOnUiThread { setResultListenerWaitingDialog(
                requireActivity().supportFragmentManager,
                this,
                TAG_WAITING_DIALOG
            )}
            displayDialog(requireActivity(), TAG_WAITING_DIALOG, _waitingDialog!!)
        }
    }

    // ================================================================================
    // INTERFACE - PreviewControlsListener
    // ================================================================================
    override fun onIndexChanged(index: Int) {
        updateSeekBarProgress(index)
        requireActivity().runOnUiThread { updatePageLabel() }
    }

    override val controlsHeight: Int
        get() = 0

    override fun panChanged(panX: Float, panY: Float) {
        _panX = panX
        _panY = panY
    }

    override fun zoomLevelChanged(zoomLevel: Float) {
        _zoomLevel = zoomLevel
    }

    override fun setControlsEnabled(enabled: Boolean) {}

    // ================================================================================
    // INTERFACE - OnSeekBarChangeListener
    // ================================================================================
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            _pageLabel!!.text = _printPreviewView!!.getPageString(progress)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}
    override fun onStopTrackingTouch(seekBar: SeekBar) {
        val progress = seekBar.progress
        _printPreviewView!!.currentPage = progress
        updatePageLabel()
    }

    // ================================================================================
    // INTERFACE - Callback
    // ================================================================================
    override fun handleMessage(msg: Message): Boolean {
        _printPreviewView!!.reconfigureCurlView()
        _printPreviewView!!.visibility = msg.arg1
        return true
    }

    // ================================================================================
    // INTERFACE - PauseableHandlerCallback
    // ================================================================================
    override fun storeMessage(message: Message?): Boolean {
        return false
    }

    override fun processMessage(message: Message?) {
        val id = message!!.what
        if (id == R.id.view_id_print_button) {
            _pauseableHandler!!.pause()
            val printerManager = PrinterManager.getInstance(appContext!!)
            if (printerManager!!.defaultPrinter == PrinterManager.EMPTY_ID) {
                val titleMsg = resources.getString(R.string.ids_lbl_print_settings)
                val strMsg = getString(R.string.ids_err_msg_no_selected_printer)
                val btnMsg = getString(R.string.ids_lbl_ok)
                val fragment = newInstance(titleMsg, strMsg, btnMsg)
                displayDialog(requireActivity(), TAG_MESSAGE_DIALOG, fragment)
                _pauseableHandler!!.resume()
                return
            }
            if (requireActivity() is MainActivity) {
                val activity = requireActivity() as MainActivity
                val v = message.obj as View
                if (!activity.isDrawerOpen(Gravity.RIGHT)) {
                    val fm = activity.supportFragmentManager
                    setIconState(v.id, true)

                    // Always make new
                    val fragment: PrintSettingsFragment? // (PrintSettingsFragment)
                    // fm.findFragmentByTag(FRAGMENT_TAG_PRINT_SETTINGS);
                    val ft = fm.beginTransaction()
                    fragment = PrintSettingsFragment()
                    ft.replace(R.id.rightLayout, fragment, FRAGMENT_TAG_PRINT_SETTINGS)
                    ft.commit()
                    fragment.setPrinterId(_printerId)
                    fragment.setPdfPath(_pdfManager!!.path)
                    fragment.setPDFisLandscape(_pdfManager!!.isPDFLandscape)
                    fragment.setPrintSettings(_printSettings)
                    fragment.setFragmentForPrinting(true)
                    fragment.setTargetFragmentPrintPreview()
                    setResultListenerForPrintSettings()
                    activity.openDrawer(Gravity.RIGHT, isTablet)
                } else {
                    activity.closeDrawers()
                }
            }
        } else if (id == R.id.menu_id_action_button) {
            _pauseableHandler!!.pause()
            val activity = requireActivity() as MainActivity
            activity.openDrawer(Gravity.LEFT)
        }
    }

    // ================================================================================
    // INTERFACE - FragmentResultListener
    // ================================================================================
    private fun setResultListenerForPrintSettings() {
        requireActivity().supportFragmentManager.setFragmentResultListener(
            TAG_RESULT_PRINT_PREVIEW,
            this
        ) { _, bundle: Bundle ->
            val result = bundle.getString(PrintSettingsFragment.RESULT_KEY)
            if (result == PrintSettingsFragment.RESULT_PRINTER_ID) {
                setPrintId(
                    _printSettingsViewModel.printerId
                )
            } else if (result == PrintSettingsFragment.RESULT_PRINTER_SETTINGS) {
                setPrintSettings(
                    _printSettingsViewModel.printSettings
                )
            }
        }
    }

    // ================================================================================
    // INTERFACE - ConfirmDialogListener
    // ================================================================================
    override fun onConfirm() {
        _confirmDialogFragment = null
        _isPermissionDialogOpen = true
        _resultLauncherPermissionStorage.launch(
            arrayOf(_permissionType))
    }

    override fun onCancel() {
        if (_waitingDialog != null) {
            _pdfConverterManager!!.cancel()
            val intent = requireActivity().intent
            intent.action = null
            intent.clipData = null
            intent.data = null
            intent.putExtra(AppConstants.EXTRA_FILE_FROM_PICKER, 1)
            transitionToHomeScreen()
        } else if (_confirmDialogFragment != null) {
            _confirmDialogFragment = null
            transitionToHomeScreen()
        }
    }

    // ================================================================================
    // Result Launcher
    // ================================================================================
    private val _resultLauncherPermissionStorage =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            _isPermissionDialogOpen = false // the request returned a result hence dialog is closed
            if (permissions.isNotEmpty()) {
                if (permissions[_permissionType] == true) {
                    // based on design docs - RISO_SmartDeviceApp_Design_03_Android Rev 3.0
                    // N.Print Preview 1.Display Screen
                    // Figure I 44 Print Preview – Display Screen – Activity Diagram
                    // after permission is granted and before PDF initialization,
                    // loading indicator (spinning progress bar) should be displayed
                    setPrintPreviewViewDisplayed(view, true)

                    // permission was granted, run PDF conversion and initializations
                    if (_pdfConverterManager != null) {
                        initializePdfConverterAndRunAsync()
                    }
                    initializePdfManagerAndRunAsync()
                    _shouldResetToFirstPageOnInitialize = true
                } else {
                    // permission was denied check if Print Settings is open
                    val activity = requireActivity() as MainActivity
                    if (activity.isDrawerOpen(Gravity.RIGHT)) {
                        activity.closeDrawers()
                    }

                    // Q&A188#1/RM775/RM786 Fix: Clear PDF before transition to Home Screen -- start
                    val intent = requireActivity().intent
                    intent.action = null
                    intent.clipData = null
                    intent.data = null
                    // Q&A188#1/RM775/RM786 Fix -- end
                    transitionToHomeScreen()
                }
            }
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    _permissionType
                ) != PackageManager.PERMISSION_GRANTED && !shouldShowRequestPermissionRationale(
                    _permissionType
                )
            ) {
                val message =
                    resources.getString(R.string.ids_err_msg_write_external_storage_permission_not_granted)
                val button = resources.getString(R.string.ids_lbl_ok)
                displayDialog(
                    requireActivity(),
                    FRAGMENT_TAG_DIALOG,
                    newInstance(message, button)
                )
            }
        }


    companion object {
        /// Tag used to identify the error dialog
        const val FRAGMENT_TAG_DIALOG = "pdf_error_dialog"

        /// Tag used to identify the print settings fragment
        const val FRAGMENT_TAG_PRINT_SETTINGS = "fragment_print_settings"

        /// Key used to store current page
        private const val KEY_CURRENT_PAGE = "current_page"

        /// Tag used to identify the dialog message
        const val TAG_MESSAGE_DIALOG = "dialog_message"
        private const val TAG_PERMISSION_DIALOG = "external_storage_tag"
        private const val KEY_EXTERNAL_STORAGE_DIALOG_OPEN = "key_external_storage_dialog_open"
        private const val KEY_SCREEN_SIZE = "screen_size"
        const val TAG_WAITING_DIALOG = "dialog_converting"

        /// Tag used to identify print setting update result
        const val TAG_RESULT_PRINT_PREVIEW = "result_print_preview"
    }
}