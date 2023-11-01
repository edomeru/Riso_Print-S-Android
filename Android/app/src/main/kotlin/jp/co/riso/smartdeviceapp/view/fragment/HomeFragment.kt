/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * HomeFragment.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.fragment

import android.Manifest
import android.Manifest.permission.*
import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import jp.co.riso.android.dialog.ConfirmDialogFragment
import jp.co.riso.android.dialog.ConfirmDialogFragment.ConfirmDialogListener
import jp.co.riso.android.dialog.DialogUtils.displayDialog
import jp.co.riso.android.dialog.InfoDialogFragment.Companion.newInstance
import jp.co.riso.android.util.FileUtils
import jp.co.riso.android.util.ImageUtils
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.view.PDFHandlerActivity
import jp.co.riso.smartdeviceapp.view.base.BaseFragment
import jp.co.riso.smartprint.R

/**
 * @class HomeFragment
 *
 * @brief Fragment which contains the Home Screen
 */
open class HomeFragment : BaseFragment(), View.OnClickListener, ConfirmDialogListener {

    private var _homeButtons: LinearLayout? = null
    private var _fileButton: LinearLayout? = null
    private var _photosButton: LinearLayout? = null
    // private var _cameraButton: LinearLayout? = null aLINK edit: HIDE_NEW_FEATURES: Capture Photo function is hidden. Hide camera permission declaration
    private var _confirmDialogFragment: ConfirmDialogFragment? = null
    private var _buttonTapped: LinearLayout? = null

    private val _permissionsStorage = arrayOf(
        WRITE_EXTERNAL_STORAGE)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val _permissionsStorageAndroid13 = arrayOf(
        READ_MEDIA_IMAGES
    )

    private val _permissionsCameraStorage = arrayOf(
        CAMERA,
        WRITE_EXTERNAL_STORAGE)

    // to prevent double tap
    private var _lastClickTime: Long = 0
    private var _checkPermission = false

    override val viewLayout: Int
        get() = R.layout.fragment_home

    override fun initializeFragment(savedInstanceState: Bundle?) {
        val intent = requireActivity().intent
        if (intent != null) {
            val extras = intent.extras
            if (extras != null) {
                val text = extras.getString(Intent.EXTRA_TEXT)
                if (text != null && text == AppConstants.ERR_KEY_INVALID_INTENT) {
                    intent.removeExtra(Intent.EXTRA_TEXT)
                    // Display error message that an invalid intent was sent by a third-party app
                    val message = resources.getString(R.string.ids_err_msg_invalid_contents_android)
                    val button = resources.getString(R.string.ids_lbl_ok)
                    displayDialog(requireActivity(), FRAGMENT_TAG_DIALOG, newInstance(message, button))
                }
            }
        }
    }

    override fun initializeView(view: View, savedInstanceState: Bundle?) {
        _homeButtons = view.findViewById(R.id.homeButtons)
        setOnClickListeners(view)
    }

    override fun initializeCustomActionBar(view: View, savedInstanceState: Bundle?) {
        val textView = view.findViewById<TextView>(R.id.actionBarTitle)
        textView.setText(R.string.ids_lbl_home)
        addActionMenuButton(view)
    }

    override fun onClick(v: View) {
        super.onClick(v)
        val id = v.id
        if (id == R.id.fileButton) {
            /* 20231020 - Permission is not needed anymore for PDF/TXT files.
             *  Permission will only be applied for images
             */
            _buttonTapped = _fileButton
            if (SystemClock.elapsedRealtime() - _lastClickTime > 1000) {
                // prevent double tap
                _lastClickTime = SystemClock.elapsedRealtime()
                val filePickerIntent = Intent(Intent.ACTION_GET_CONTENT)
                filePickerIntent.type = "*/*"
                if (!isChromeBook) {
                    filePickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, AppConstants.DOC_TYPES)
                }
                _resultLauncherFile.launch(
                    Intent.createChooser(
                        filePickerIntent,
                        getString(R.string.ids_lbl_select_document)
                    )
                )
            }
        } else if (id == R.id.photosButton) {
            _buttonTapped = _photosButton
            _checkPermission = checkPermission(true)
            if (_checkPermission && SystemClock.elapsedRealtime() - _lastClickTime > 1000) {
                // prevent double tap
                _lastClickTime = SystemClock.elapsedRealtime()
                val photosPickerIntent = Intent(Intent.ACTION_GET_CONTENT)
                photosPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                photosPickerIntent.type = "*/*"
                if (!isChromeBook) {
                    if (SDK_INT >= Build.VERSION_CODES.S) {
                        photosPickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, AppConstants.IMAGE_TYPES_ANDROID_12)
                    } else {
                        if (SDK_INT >= Build.VERSION_CODES.P) {
                            photosPickerIntent.putExtra(
                                Intent.EXTRA_MIME_TYPES,
                                AppConstants.IMAGE_TYPES
                            )
                        } else {
                            photosPickerIntent.putExtra(
                                Intent.EXTRA_MIME_TYPES,
                                AppConstants.IMAGE_TYPES_ANDROID_8
                            )
                        }
                    }
                }
                _resultLauncherPhoto.launch(
                    Intent.createChooser(
                        photosPickerIntent,
                        getString(R.string.ids_lbl_select_photos)
                    )
                )
            }
        }

        /* aLINK edit: HIDE_NEW_FEATURES: Capture Photo function is hidden. Hide camera permission declaration
        else if (id == R.id.cameraButton) {
            _buttonTapped = _cameraButton
            _checkPermission = checkPermission(false)
            if (_checkPermission && SystemClock.elapsedRealtime() - _lastClickTime > 1000) {
                // prevent double tap
                _lastClickTime = SystemClock.elapsedRealtime()

                // RM 789 Fix: Add checking of available internal storage before opening External Camera Application
                if (_availableStorageInBytes > AppConstants.CONST_FREE_SPACE_BUFFER) {
                    val intent = Intent(activity, ScanActivity::class.java)
                    intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, ScanConstants.OPEN_CAMERA)
                    _resultLauncherCamera.launch(intent)
                } else {
                    // RM 789 Fix - Start
                    // Display Error Message if internal storage is less than Free Space Buffer
                    val message = resources.getString(R.string.ids_err_msg_not_enough_space)
                    val button = resources.getString(R.string.ids_lbl_ok)
                    displayDialog(requireActivity(), FRAGMENT_TAG_DIALOG, newInstance(message, button))
                    // RM 789 Fix - End
                }
            }
        }*/
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val mainView = requireView().findViewById<LinearLayout>(R.id.contentView)
        mainView.removeView(_homeButtons)
        val newView = View.inflate(activity, R.layout.home_buttons, null) as LinearLayout
        newView.layoutParams = _homeButtons!!.layoutParams
        mainView.addView(newView)
        _homeButtons = newView
        setOnClickListeners(_homeButtons!!)
    }

    private fun setOnClickListeners(view: View) {
        _fileButton = view.findViewById(R.id.fileButton)
        _photosButton = view.findViewById(R.id.photosButton)
        // _cameraButton = view.findViewById(R.id.cameraButton) aLINK edit: HIDE_NEW_FEATURES: Capture Photo function is hidden. Hide camera permission declaration
        _fileButton!!.setOnClickListener(this)
        _photosButton!!.setOnClickListener(this)
        // _cameraButton!!.setOnClickListener(this) aLINK edit: HIDE_NEW_FEATURES: Capture Photo function is hidden. Hide camera permission declaration
    }

    private fun checkPermission(isStorageOnly: Boolean): Boolean {
        // WRITE_EXTERNAL_STORAGE for Android 12 and older versions
        var permissionType = WRITE_EXTERNAL_STORAGE

        if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // READ_MEDIA_IMAGES for Android 13 and later versions
            permissionType = READ_MEDIA_IMAGES
        }

        if (isStorageOnly && ContextCompat.checkSelfPermission(
                requireActivity(),
                permissionType
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (shouldShowRequestPermissionRationale(permissionType)) {
                if (_confirmDialogFragment == null) {
                    val message =
                        requireActivity().getString(R.string.ids_err_msg_storage_permission_not_allowed)
                    val positiveButton = requireActivity().getString(R.string.ids_lbl_ok)
                    _confirmDialogFragment =
                        ConfirmDialogFragment.newInstance(message, positiveButton, null, TAG_PERMISSION_STORAGE_DIALOG)
                    setResultListenerConfirmDialog(
                        requireActivity().supportFragmentManager,
                        this,
                        TAG_PERMISSION_STORAGE_DIALOG
                    )
                    displayDialog(requireActivity(), TAG_PERMISSION_STORAGE_DIALOG, _confirmDialogFragment!!)
                }
            } else {
                // Check target devices OS version
                if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    _resultLauncherPermissionStorage.launch(_permissionsStorageAndroid13)
                } else {
                    _resultLauncherPermissionStorage.launch(_permissionsStorage)
                }
            }
            return false
        } else if (!isStorageOnly && (ContextCompat.checkSelfPermission(
                requireActivity(), CAMERA
            ) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        requireActivity(),
                        permissionType
                    ) != PackageManager.PERMISSION_GRANTED)
        ) {
            if (shouldShowRequestPermissionRationale(CAMERA) || shouldShowRequestPermissionRationale(
                    permissionType
                )
            ) {
                if (_confirmDialogFragment == null) {
                    val message =
                        requireActivity().getString(R.string.ids_err_msg_camera_permission_not_allowed)
                    val positiveButton = requireActivity().getString(R.string.ids_lbl_ok)
                    _confirmDialogFragment =
                        ConfirmDialogFragment.newInstance(message, positiveButton, null, TAG_PERMISSION_CAMERA_STORAGE_DIALOG)
                    setResultListenerConfirmDialog(
                        requireActivity().supportFragmentManager,
                        this,
                        TAG_PERMISSION_CAMERA_STORAGE_DIALOG
                    )
                    displayDialog(requireActivity(), TAG_PERMISSION_CAMERA_STORAGE_DIALOG, _confirmDialogFragment!!)
                }
            } else {
                _resultLauncherPermissionCameraStorage.launch(_permissionsCameraStorage)
            }
            return false
        }
        return true
    }

    /**
     * @brief Opens file in app
     *
     * @param data File uri
     * @param clipData Clip data for multiple selected files
     * @param fileType File type
     */
    private fun openFile(data: Uri?, clipData: ClipData?, fileType: Int) {
        val intent = Intent(activity, PDFHandlerActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(AppConstants.EXTRA_FILE_FROM_PICKER, fileType)
        if (fileType == IMAGES_FROM_PICKER) {
            intent.clipData = clipData
        } else {
            intent.data = data
        }
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivity(intent)
    }

    // ================================================================================
    // INTERFACE - ConfirmDialogListener
    // ================================================================================
    override fun onConfirm() {
        _confirmDialogFragment = null
        /* aLINK edit: HIDE_NEW_FEATURES: Capture Photo function is hidden. Hide camera permission declaration
        if (_buttonTapped === _cameraButton) {
            _resultLauncherPermissionCameraStorage.launch(_permissionsCameraStorage)
        } else {
            _resultLauncherPermissionStorage.launch(_permissionsStorage)
        } */
        // Check target devices OS version
        if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            _resultLauncherPermissionStorage.launch(_permissionsStorageAndroid13)
        } else {
            _resultLauncherPermissionStorage.launch(_permissionsStorage)
        }
    }

    override fun onCancel() {
        _confirmDialogFragment = null
    }

    /* aLINK edit: HIDE_NEW_FEATURES: Capture Photo function is hidden. Hide camera permission declaration
    // ================================================================================
    // Internal Methods
    // ================================================================================
    // RM 789 Fix - Start
    /**
     * @brief Gets Available Internal Storage Size
     *
     * @return Remaining Internal Storage Size (in Bytes)
     */
    private val _availableStorageInBytes: Long
        get() {
            val stat = StatFs(SmartDeviceApp.appContext!!.filesDir!!.path)
            val bytesAvailable = stat.blockSizeLong * stat.availableBlocksLong
            Log.d(HomeFragment::class.java.simpleName, "bytesAvailable:$bytesAvailable")
            return bytesAvailable
        }
    // RM 789 Fix - End*/

    // ================================================================================
    // Register for Activity Result
    // ================================================================================
    private val _resultLauncherFile = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data: Intent? = result.data
        if (result.resultCode == Activity.RESULT_OK && data != null) {
            val contentType = FileUtils.getMimeType(activity, data.data)
            if (!listOf(*AppConstants.DOC_TYPES).contains(contentType)) {
                val message = resources.getString(R.string.ids_err_msg_open_failed)
                val button = resources.getString(R.string.ids_lbl_ok)
                displayDialog(requireActivity(), FRAGMENT_TAG_DIALOG, newInstance(message, button))
            } else {
                val fileType =
                    if (contentType == AppConstants.DOC_TYPES[0]) PDF_FROM_PICKER else TEXT_FROM_PICKER
                openFile(data.data, null, fileType)
            }
        }
    }

    private val _resultLauncherPhoto = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data: Intent? = result.data
        if (result.resultCode == Activity.RESULT_OK && data != null) {
            // There are no request codes
            if (data.clipData != null) { // multiple image files
                if (!ImageUtils.isImageFileSupported(activity, data.clipData)) {
                    val message = resources.getString(R.string.ids_err_msg_invalid_file_selection)
                    val button = resources.getString(R.string.ids_lbl_ok)
                    displayDialog(requireActivity(), FRAGMENT_TAG_DIALOG, newInstance(message, button))
                } else {
                    openFile(null, data.clipData, IMAGES_FROM_PICKER)
                }
            } else {    // single image file
                if (!ImageUtils.isImageFileSupported(activity, data.data)) {
                    val message = resources.getString(R.string.ids_err_msg_invalid_file_selection)
                    val button = resources.getString(R.string.ids_lbl_ok)
                    displayDialog(requireActivity(), FRAGMENT_TAG_DIALOG, newInstance(message, button))
                } else {
                    openFile(data.data, null, IMAGE_FROM_PICKER)
                }
            }
        }
    }

    /* aLINK edit: HIDE_NEW_FEATURES: Capture Photo function is hidden. Hide camera permission declaration
    private inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
        SDK_INT >= 33 -> getParcelable(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelable(key) as? T
    }

    private val _resultLauncherCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data: Intent? = result.data
        if (result.resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.extras!!.getParcelable<Uri>(ScanConstants.SCANNED_RESULT)
            val previewIntent = Intent(activity, PDFHandlerActivity::class.java)
            previewIntent.action = Intent.ACTION_VIEW
            previewIntent.putExtra(AppConstants.EXTRA_FILE_FROM_PICKER, IMAGE_FROM_CAMERA)
            previewIntent.data = imageUri
            previewIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(previewIntent)
        }
    } */

    private val _resultLauncherPermissionStorage = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        // WRITE_EXTERNAL_STORAGE for Android 12 and older versions
        var permissionType = WRITE_EXTERNAL_STORAGE

        if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // READ_MEDIA_IMAGES for Android 13 and later versions
            permissionType = READ_MEDIA_IMAGES
        }

        // ignore if result is empty which is due to repeated permission request because of quick taps
        if (permissions.isNotEmpty()) {
            if (permissions[permissionType] == true) {
                // permission was granted, start picker intent
                if (_buttonTapped != null) {
                    _buttonTapped!!.performClick()
                }
            }
            if (!_checkPermission && !shouldShowRequestPermissionRationale(permissionType)) {
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
    }

    private val _resultLauncherPermissionCameraStorage = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        // ignore if result is empty which is due to repeated permission request because of quick taps
        if (permissions.isNotEmpty()) {
            if (permissions[CAMERA] == true && permissions[WRITE_EXTERNAL_STORAGE] == true) {
                // permission was granted, start picker intent
                if (_buttonTapped != null) {
                    _buttonTapped!!.performClick()
                }
            }
            if (!_checkPermission && !shouldShowRequestPermissionRationale(CAMERA) &&
                !shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)
            ) {
                val message =
                    resources.getString(R.string.ids_err_msg_camera_permission_not_granted)
                val button = resources.getString(R.string.ids_lbl_ok)
                displayDialog(
                    requireActivity(),
                    FRAGMENT_TAG_DIALOG,
                    newInstance(message, button)
                )
            }
        }
    }

    companion object {
        // flags for file types picked
        const val PDF_FROM_PICKER = 0
        const val TEXT_FROM_PICKER = -1
        const val IMAGE_FROM_PICKER = -2
        const val IMAGES_FROM_PICKER = -3
        const val IMAGE_FROM_CAMERA = -4
        const val FRAGMENT_TAG_DIALOG = "file_error_dialog"
        private const val TAG_PERMISSION_STORAGE_DIALOG = "storage_tag"
        private const val TAG_PERMISSION_CAMERA_STORAGE_DIALOG = "camera_storage_tag"
    }
}