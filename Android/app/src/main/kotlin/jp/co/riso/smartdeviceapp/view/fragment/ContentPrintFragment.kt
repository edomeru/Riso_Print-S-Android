/*
 * Copyright (c) 2024 RISO, Inc. All rights reserved.
 *
 * ContentPrintFragment.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.fragment

import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import eu.erikw.PullToRefreshListView
import jp.co.riso.android.dialog.ConfirmDialogFragment
import jp.co.riso.android.dialog.DialogUtils
import jp.co.riso.android.dialog.InfoDialogFragment
import jp.co.riso.android.dialog.WaitingDialogFragment
import jp.co.riso.smartdeviceapp.AppConstants

import jp.co.riso.smartdeviceapp.controller.print.ContentPrintManager
import jp.co.riso.smartdeviceapp.model.ContentPrintFile
import jp.co.riso.smartdeviceapp.view.PDFHandlerActivity
import jp.co.riso.smartdeviceapp.view.base.BaseFragment
import jp.co.riso.smartdeviceapp.view.contentprint.ContentPrintFileAdapter
import jp.co.riso.smartprint.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

/**
 * @class ContentPrintFragment
 *
 * @brief Fragment which contains the Content Print Screen
 */
open class ContentPrintFragment : BaseFragment(),
        View.OnClickListener,
        ContentPrintManager.IAuthenticationCallback,
        ContentPrintManager.IContentPrintCallback,
        PullToRefreshListView.OnRefreshListener,
        ContentPrintFileAdapter.ContentPrintFileAdapterInterface,
        ConfirmDialogFragment.ConfirmDialogListener {

    enum class Authentication {
        LOGIN,
        LOGOUT
    }
    enum class Confirmation {
        LOGOUT,
        PREVIEW
    }

    private var _mainView: View? = null
    private var _listView: PullToRefreshListView? = null
    private var _loggedOutText: TextView? = null
    private var _emptyListText: TextView? = null
    private var _previousButton: Button? = null
    private var _pageLabel: TextView? = null
    private var _nextButton: Button? = null
    private var _contentPrintAdapter: ContentPrintFileAdapter? = null
    private var _contentPrintManager: ContentPrintManager? = null
    private var _downloadingDialog: WaitingDialogFragment? = null
    private var _page: Int = 1
    // to prevent double tap
    private var _lastClickTime: Long = 0

    // ================================================================================
    // INTERFACE - BaseFragment
    // ================================================================================
    override val viewLayout: Int
        get() = R.layout.fragment_contentprint

    override fun initializeFragment(savedInstanceState: Bundle?) {
        _contentPrintManager = ContentPrintManager.getInstance()
    }

    override fun initializeView(view: View, savedInstanceState: Bundle?) {
        _loggedOutText = view.findViewById(R.id.loggedOutText)
        _emptyListText = view.findViewById(R.id.emptyListText)
        _previousButton = view.findViewById(R.id.previousButton)
        _previousButton?.setOnClickListener(this)
        _pageLabel = view.findViewById(R.id.pageLabel)
        _nextButton = view.findViewById(R.id.nextButton)
        _nextButton?.setOnClickListener(this)

        _contentPrintAdapter =
            ContentPrintFileAdapter(activity, R.layout.contentprint_item, ContentPrintManager.fileList)
        _contentPrintAdapter!!.setAdapterInterface(this)
        _listView = view.findViewById(R.id.content_list)
        _listView!!.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.theme_light_3))
        _listView!!.adapter = _contentPrintAdapter
        _listView!!.setOnRefreshListener(this)
        val progressLayoutParams =
            _listView!!.findViewById<View>(R.id.ptr_id_spinner)!!.layoutParams as RelativeLayout.LayoutParams?
        progressLayoutParams!!.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
        val arrowLayoutParams =
            _listView!!.findViewById<View>(R.id.ptr_id_image)!!.layoutParams as RelativeLayout.LayoutParams?
        arrowLayoutParams!!.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)

        if (ContentPrintManager.filenameFromNotification == null) {
            refreshFileList()
        }
    }

    override fun initializeCustomActionBar(view: View, savedInstanceState: Bundle?) {
        val textView = view.findViewById<TextView>(R.id.actionBarTitle)
        textView.setText(R.string.ids_lbl_content_print)
        _mainView = view

        addMenuButton(
            view,
            R.id.leftActionLayout,
            R.id.menu_id_back_button,
            R.drawable.selector_actionbar_back,
            this
        )

        updateActionBar(view)
    }

    override fun onStart() {
        super.onStart()
        ContentPrintManager.checkAccessToken(this)
    }

    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================
    override fun onClick(v: View) {
        super.onClick(v)

        if (SystemClock.elapsedRealtime() - _lastClickTime > AppConstants.DOUBLE_TAP_TIME_ELAPSED) {
            // prevent double tap
            _lastClickTime = SystemClock.elapsedRealtime()

            val id = v.id
            when (id) {
                R.id.menu_id_back_button -> {
                    val fm = parentFragmentManager
                    val ft = fm.beginTransaction()
                    if (fm.backStackEntryCount > 0) {
                        fm.popBackStack()
                        ft.commit()
                        fm.executePendingTransactions()
                    }
                }

                R.id.menu_id_action_login_button -> {
                    Log.d(ContentPrintManager.TAG, "===== Login button clicked =====")
                    lastAuthentication = Authentication.LOGIN
                    Log.d(ContentPrintManager.TAG, "===== Attempting to login =====")
                    _contentPrintManager?.login(this.activity, this)
                }

                R.id.menu_id_action_logout_button -> {
                    lastAuthentication = Authentication.LOGOUT
                    showLogoutConfirmDialog()
                }

                R.id.menu_id_action_refresh_button -> refreshFileList()
                R.id.previousButton -> previousPage()
                R.id.nextButton -> nextPage()
            }
        }
    }

    // ================================================================================
    // INTERFACE - PullToRefreshListView.OnRefreshListener
    // ================================================================================
    override fun onRefresh() {
        refreshFileList()
    }

    override fun onHeaderAdjusted(margin: Int) {
        if (_emptyListText != null) {
            val params = _emptyListText!!.layoutParams as FrameLayout.LayoutParams?
            params!!.topMargin = margin
            _emptyListText!!.layoutParams = params
        }
    }

    override fun onBounceBackHeader(duration: Int) {
        if (_emptyListText != null) {
            val params = _emptyListText!!.layoutParams as FrameLayout.LayoutParams?
            val animation = ValueAnimator.ofInt(params!!.topMargin, 0)
            animation.addUpdateListener { valueAnimator ->
                params.topMargin = (valueAnimator.animatedValue as Int?)!!
                _emptyListText?.requestLayout()
            }
            animation.duration = duration.toLong()
            animation.start()
        }
    }

    // ================================================================================
    // INTERFACE - ContentPrintManager.IAuthenticationCallback
    // ================================================================================
    override fun onAuthenticationStarted() {
        CoroutineScope(Dispatchers.Main).launch {
            // Hide the logged out text
            _loggedOutText?.visibility = View.GONE
            // Start the loading indicator
            _listView?.setRefreshing()
        }
    }

    override fun onAuthenticationFinished() {
        Log.d(ContentPrintManager.TAG, "===== onAuthenticationFinished: logged in = ${ContentPrintManager.isLoggedIn} =====")
        // UI updates should be called on the main thread,
        // in case the authentication is refreshed from the background
        CoroutineScope(Dispatchers.Main).launch {
            // Stop the loading indicator
            _listView?.onRefreshComplete()
            // Update the action bar
            updateActionBar()
            // Display error
            if (lastAuthentication == Authentication.LOGIN && !ContentPrintManager.isLoggedIn) {
                Log.d(ContentPrintManager.TAG, "===== Login failed, showing error dialog =====")
                showLoginError()
            }
        }

        if (ContentPrintManager.isLoggedIn && ContentPrintManager.filenameFromNotification != null) {
            // Download the file from the notification
            _contentPrintManager?.downloadFile(
                ContentPrintManager.filenameFromNotification,
                this
            )
        } else {
            // Update the file list
            refreshFileList()
        }
    }

    // ================================================================================
    // INTERFACE - ContentPrintManager.IContentPrintCallback
    // ================================================================================
    override fun onFileListUpdated(success: Boolean) {
        // If there are no entries in the File List
        val isLoggedIn = ContentPrintManager.isLoggedIn
        val list = ContentPrintManager.fileList
        if (success && isLoggedIn) {
            Log.d("TEST", "===== onFileListUpdated - START =====")
            val count = if (list.count() > ITEMS_PER_PAGE) ITEMS_PER_PAGE else list.count()
            Log.d("TEST", "Downloading $count thumbnails")

            for (i in 0 until count) {
                // Download the image thumbnails
                _contentPrintManager?.downloadThumbnail(list[i], this)
            }

            //for (file in list) {
            //    Log.d("TEST", "= ${file.filename}")
            //}
        } else {
            hideLoadingPreviewDialog()
        }

        // UI updates should be called on the main thread,
        CoroutineScope(Dispatchers.Main).launch {
            _listView?.onRefreshComplete()
        }

        if (isLoggedIn) {
            if (list.isEmpty()) {
                this._emptyListText?.visibility = View.VISIBLE
            } else {
                this._emptyListText?.visibility = View.GONE
                _previousButton?.visibility = View.VISIBLE
                _pageLabel?.visibility = View.VISIBLE
                _nextButton?.visibility = View.VISIBLE
                _pageLabel?.text = "$_page / ${getTotalNumberOfPages()}"

                // Repopulate the list
                _contentPrintAdapter?.clear()
                _contentPrintAdapter?.addAll(list)
                _contentPrintAdapter?.notifyDataSetChanged()
            }
        }
        Log.d("TEST", "===== onFileListUpdated - END =====")
    }

    override fun onThumbnailDownloaded(contentPrintFile: ContentPrintFile, filePath: String, success: Boolean) {
        if (success) {
            // Set the thumbnail image path
            contentPrintFile.thumbnailImagePath = filePath

            CoroutineScope(Dispatchers.Main).launch {
                _contentPrintAdapter?.notifyDataSetChanged()
            }
        }
    }

    override fun onStartFileDownload() {
        showLoadingPreviewDialog()
    }

    override fun onFileDownloaded(contentPrintFile: ContentPrintFile, filePath: String, success: Boolean) {
        if (success) {
            ContentPrintManager.filePath = filePath
            ContentPrintManager.isFileFromContentPrint = true

            // Get the printer list before displaying the preview screen
            _contentPrintManager?.updatePrinterList(this)
        } else {
            hideLoadingPreviewDialog()

            ContentPrintManager.filePath = null
            ContentPrintManager.isFileFromContentPrint = false

            // Display download error message
            showDownloadError()
        }

        // UI updates should be called on the main thread,
        CoroutineScope(Dispatchers.Main).launch {
            _listView?.onRefreshComplete()
        }
    }

    override fun onPrinterListUpdated(success: Boolean) {
        hideLoadingPreviewDialog()

        if (success && ContentPrintManager.filePath != null) {
            val intent = Intent(activity, PDFHandlerActivity::class.java)
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.fromFile(File(ContentPrintManager.filePath!!))
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra(AppConstants.VAL_KEY_CONTENT_PRINT, ContentPrintManager.filePath)
            intent.putExtra(AppConstants.EXTRA_FILE_FROM_PICKER, HomeFragment.PDF_FROM_PICKER)
            startActivity(intent)
        } else {
            showDownloadError()
        }
    }

    // ================================================================================
    // INTERFACE - ContentPrintFileAdapterInterface
    // ================================================================================
    override fun onFileSelect(contentPrintFile: ContentPrintFile?): Int {
        ContentPrintManager.selectedFile = contentPrintFile
        if (contentPrintFile != null) {
            showContentPrintConfirmDialog()
            return 1
        }
        return -1
    }

    // ================================================================================
    // INTERFACE - ConfirmDialogFragment.ConfirmDialogListener
    // ================================================================================
    override fun onConfirm() {
        if (_lastConfirmation == Confirmation.LOGOUT) {
            _contentPrintManager?.logout(this)
        } else { // _lastConfirmation == Confirmation.PREVIEW
            if (ContentPrintManager.selectedFile != null) {
                _contentPrintManager?.downloadFile(ContentPrintManager.selectedFile!!, this)
            }
        }
    }

    override fun onCancel() {
        // Do nothing
    }

    // ================================================================================
    // Private Functions
    // ================================================================================
    private fun refreshFileList() {
        ContentPrintManager.fileList = ArrayList()
        _previousButton?.visibility = View.GONE
        _pageLabel?.visibility = View.GONE
        _nextButton?.visibility = View.GONE
        _contentPrintAdapter?.clear()

        if (!ContentPrintManager.isLoggedIn) {
            this._loggedOutText?.visibility = View.VISIBLE
            this._emptyListText?.visibility = View.GONE
        } else {
            this._loggedOutText?.visibility = View.GONE
            this._emptyListText?.visibility = View.GONE
            this._listView?.setRefreshing()

            // Get the file list
            _contentPrintManager?.updateFileList(ITEMS_PER_PAGE, _page, this)
        }
    }

    private fun addMenuButtonLabel(
        v: View,
        layoutId: Int,
        viewId: Int,
        labelId: Int,
        imageResId: Int,
        listener: View.OnClickListener?
    ): View {
        val li = LayoutInflater.from(v.context)
        val button = li.inflate(R.layout.actionbar_button_with_label, null) as LinearLayout
        button.id = viewId
        button.setOnClickListener(listener)
        val label = button.findViewById<TextView>(R.id.buttonLabelText)
        label.text = v.context.resources.getString(labelId)
        val image = button.findViewById<ImageView>(R.id.buttonImage)
        image.setImageResource(imageResId)
        val layout = v.findViewById<ViewGroup>(layoutId)
        layout.addView(button)
        return button
    }

    private fun updateActionBar(view: View) {
        val isLoggedIn = ContentPrintManager.isLoggedIn
        Log.d("TEST", "updateActionBar: isLoggedIn = $isLoggedIn")
        if (!isLoggedIn) {
            addMenuButtonLabel(
                view,
                R.id.rightActionLayout,
                R.id.menu_id_action_login_button,
                R.string.ids_lbl_login,
                R.drawable.selector_actionbar_login,
                this
            )
            this._loggedOutText?.visibility = View.VISIBLE
            this._emptyListText?.visibility = View.GONE
        } else {
            addMenuButtonLabel(
                view,
                R.id.rightActionLayout,
                R.id.menu_id_action_logout_button,
                R.string.ids_lbl_logout,
                R.drawable.selector_actionbar_logout,
                this
            )
            if (context != null) {
                addMenuButton(
                    view,
                    R.id.rightActionLayout,
                    R.id.menu_id_action_refresh_button,
                    R.drawable.selector_actionbar_refresh,
                    this
                )
            }
            this._loggedOutText?.visibility = View.GONE
        }
    }

    private fun updateActionBar() {
        Log.d("TEST", "updateActionBar (removeAllViews)")
        _mainView?.findViewById<ViewGroup>(R.id.rightActionLayout)?.removeAllViews()
        _mainView?.let { updateActionBar(it) }
    }

    private fun previousPage() {
        if (_page > 1) {
            _page--
            _pageLabel?.text = "$_page / ${getTotalNumberOfPages()}"
            refreshFileList()
        }
    }

    private fun nextPage() {
        val pages = getTotalNumberOfPages()
        if (_page < pages) {
            _page++
            _pageLabel?.text = "$_page / $pages"
            refreshFileList()
        }
    }

    private fun getTotalNumberOfPages(): Int {
        val count = ContentPrintManager.fileCount
        return ceil((count / ITEMS_PER_PAGE).toDouble()).toInt()
    }

    private fun showLogoutConfirmDialog() {
        // UI updates should be called on the main thread
        CoroutineScope(Dispatchers.Main).launch {
            val confirm = ConfirmDialogFragment.newInstance(
                resources.getString(R.string.ids_lbl_confirm_logout_title),
                resources.getString(R.string.ids_lbl_confirm_logout),
                resources.getString(R.string.ids_lbl_ok),
                resources.getString(R.string.ids_lbl_cancel),
                KEY_CONTENT_PRINT_LOGOUT_DIALOG
            )
            _lastConfirmation = Confirmation.LOGOUT
            setResultListenerConfirmDialog(
                requireActivity().supportFragmentManager,
                this@ContentPrintFragment,
                KEY_CONTENT_PRINT_LOGOUT_DIALOG
            )
            DialogUtils.displayDialog(requireActivity(), KEY_CONTENT_PRINT_LOGOUT_DIALOG, confirm)
        }
    }

    private fun showContentPrintConfirmDialog() {
        // UI updates should be called on the main thread
        CoroutineScope(Dispatchers.Main).launch {
            val confirm = ConfirmDialogFragment.newInstance(
                resources.getString(R.string.ids_info_msg_confirm_preview_title),
                resources.getString(R.string.ids_info_msg_confirm_preview),
                resources.getString(R.string.ids_lbl_ok),
                resources.getString(R.string.ids_lbl_cancel),
                KEY_CONTENT_PRINT_PREVIEW_DIALOG
            )
            _lastConfirmation = Confirmation.PREVIEW
            setResultListenerConfirmDialog(
                requireActivity().supportFragmentManager,
                this@ContentPrintFragment,
                KEY_CONTENT_PRINT_PREVIEW_DIALOG
            )
            DialogUtils.displayDialog(requireActivity(), KEY_CONTENT_PRINT_PREVIEW_DIALOG, confirm)
        }
    }

    private fun showLoadingPreviewDialog() {
        // UI updates should be called on the main thread
        CoroutineScope(Dispatchers.Main).launch {
            if (_downloadingDialog == null) {
                _downloadingDialog = WaitingDialogFragment.newInstance(
                    null,
                    resources.getString(R.string.ids_info_msg_downloading),
                    false,
                    null,
                    TAG_DOWNLOADING_DIALOG
                )
                DialogUtils.displayDialog(
                    requireActivity(),
                    TAG_DOWNLOADING_DIALOG,
                    _downloadingDialog!!
                )
            }
        }
    }

    private fun hideLoadingPreviewDialog() {
        // UI updates should be called on the main thread
        CoroutineScope(Dispatchers.Main).launch {
            if (_downloadingDialog != null) {
                // Delay 1 second to give the loading preview dialog time to be displayed
                delay(TimeUnit.SECONDS.toMillis(1))

                // Check if the Content Print Fragment is still being displayed after 1 second
                if (isAdded) {
                    DialogUtils.dismissDialog(requireActivity(), TAG_DOWNLOADING_DIALOG)
                }
                _downloadingDialog = null
            }
        }
    }

    private fun showLoginError() {
        // UI updates should be called on the main thread
        CoroutineScope(Dispatchers.Main).launch {
            DialogUtils.displayDialog(
                requireActivity(),
                KEY_CONTENT_PRINT_LOGIN_ERROR_DIALOG,
                InfoDialogFragment.newInstance(
                    resources.getString(R.string.ids_err_msg_login_failed),
                    resources.getString(R.string.ids_lbl_ok)
                )
            )
        }
    }

    private fun showDownloadError() {
        // UI updates should be called on the main thread
        CoroutineScope(Dispatchers.Main).launch {
            // Dismiss the loading preview dialog if applicable
            hideLoadingPreviewDialog()

            DialogUtils.displayDialog(
                requireActivity(),
                KEY_CONTENT_PRINT_DOWNLOAD_ERROR_DIALOG,
                InfoDialogFragment.newInstance(
                    resources.getString(R.string.ids_lbl_content_print),
                    resources.getString(R.string.ids_err_msg_download_failed),
                    resources.getString(R.string.ids_lbl_ok)
                )
            )
        }
    }

    companion object {
        /// Tag used to identify the content print fragment
        const val FRAGMENT_TAG_CONTENT_PRINT = "fragment_content_print"

        const val KEY_CONTENT_PRINT_LOGOUT_DIALOG = "content_print_logout_dialog"
        const val KEY_CONTENT_PRINT_PREVIEW_DIALOG = "content_print_preview_dialog"
        const val TAG_DOWNLOADING_DIALOG = "content_print_downloading_dialog"
        const val KEY_CONTENT_PRINT_LOGIN_ERROR_DIALOG = "content_print_login_error_dialog"
        const val KEY_CONTENT_PRINT_DOWNLOAD_ERROR_DIALOG = "content_print_download_error_dialog"
        const val ITEMS_PER_PAGE = 10

        var lastAuthentication = Authentication.LOGIN
        private var _lastConfirmation = Confirmation.LOGOUT
    }
}
