/*
 * Copyright (c) 2023 RISO, Inc. All rights reserved.
 *
 * SplashActivity.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.graphics.Bitmap
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.AndroidRuntimeException
import android.view.ContextThemeWrapper
import android.view.KeyEvent
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.PreferenceManager
import jp.co.riso.android.os.pauseablehandler.PauseableHandler
import jp.co.riso.android.os.pauseablehandler.PauseableHandlerCallback
import jp.co.riso.android.util.AppUtils
import jp.co.riso.android.util.FileUtils
import jp.co.riso.android.util.Logger
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.common.BaseTask
import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager
import jp.co.riso.smartdeviceapp.view.base.BaseActivity
// Azure Notification Hub - START
import jp.co.riso.smartdeviceapp.controller.print.ContentPrintManager
import jp.co.riso.smartdeviceapp.view.notification.NotificationHubListener
// Azure Notification Hub - END
import jp.co.riso.smartdeviceapp.view.webkit.SDAWebView
import jp.co.riso.smartprint.R
import java.io.File
import java.io.IOException

/**
 * @class SplashActivity
 *
 * @brief Splash activity class.
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity(), PauseableHandlerCallback, View.OnClickListener {

    private var _handler: PauseableHandler? = null
    private var _initTask: DBInitTask? = null
    private var _databaseInitialized = false
    private var _webView: SDAWebView? = null

    override fun onCreateContent(savedInstanceState: Bundle?) {
        // Azure Notification Hub - START
        ContentPrintManager.isFromPushNotification = true
        val filename = ContentPrintManager.getFilename(intent)
        if (filename != null) {
            if (intent.data == null) {
                ContentPrintManager.filenameFromNotification = filename
            }
            runMainActivity()
            return
        }
        // Azure Notification Hub - END

        setContentView(R.layout.activity_splash)
        if (_handler == null) {
            _handler = PauseableHandler(Looper.myLooper(), this)
        }
        _databaseInitialized = false
        if (savedInstanceState != null) {
            _databaseInitialized = savedInstanceState.getBoolean(KEY_DB_INITIALIZED, false)
        }
        val prefs = PreferenceManager.getDefaultSharedPreferences(this@SplashActivity)
        val dbIsOK = prefs.contains(AppConstants.PREF_KEY_DB_VERSION)
        if (!_databaseInitialized) {
            if (!dbIsOK) {
                if (_initTask == null) {
                    _initTask = DBInitTask()
                    _initTask!!.execute()
                }
            } else {
                _databaseInitialized = true
            }
        }
        if (!_handler!!.hasMessages(MESSAGE_RUN_MAIN_ACTIVITY)) {
            if (!AppConstants.APP_SHOW_SPLASH && dbIsOK) {
                runMainActivity()
            } else {
                _handler!!.sendEmptyMessageDelayed(
                    MESSAGE_RUN_MAIN_ACTIVITY,
                    AppConstants.APP_SPLASH_DURATION
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        _handler!!.resume()
    }

    override fun onPause() {
        super.onPause()
        _handler!!.pause()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // On a new intent, set the current intent so that URL Scheme works during Splash Screen display
        setIntent(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_DB_INITIALIZED, _databaseInitialized)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            // Shortcut key : Quit CTRL + Q or ALT + Q
            KeyEvent.KEYCODE_Q -> {
                if (event.isCtrlPressed || event.isAltPressed) {
                    finishAndRemoveTask()
                }
                super.onKeyUp(keyCode, event)
            }
            // Shortcut key : Quit ALT + F4
            KeyEvent.KEYCODE_F4 -> {
                if (event.isAltPressed) {
                    finishAndRemoveTask()
                }
                super.onKeyUp(keyCode, event)
            }

            else -> super.onKeyUp(keyCode, event)
        }
    }

    private inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelable(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelable(key) as? T
    }

    // ================================================================================
    // Public Functions
    // ================================================================================
    // ================================================================================
    // Private Functions
    // ================================================================================
    /**
     * @brief Run the main activity.
     */
    private fun runMainActivity() {
        val launchIntent: Intent?
        val preferences = getSharedPreferences("licenseAgreementPrefs", MODE_PRIVATE)
        if (preferences.getBoolean("licenseAgreementDone", false)) {
            //if user has already agreed to the license agreement
            launchIntent = AppUtils.createActivityIntent(this, MainActivity::class.java)
        } else {
            //if user has not yet agreed to the license agreement
            val textView = findViewById<TextView>(R.id.actionBarTitle)
            textView.setText(R.string.ids_lbl_license)
            textView.setPadding(18, 0, 0, 0)
            val context: Context = this
            _webView = findViewById(R.id.contentWebView)

            _webView?.apply {
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        Logger.logStartTime(
                            context,
                            SplashActivity::class.java,
                            "License Activity load"
                        )
                    }

                    override fun onPageFinished(view: WebView, url: String) {
                        Logger.logStopTime(
                            context,
                            SplashActivity::class.java,
                            "License Activity load"
                        )
                    }
                }
                loadUrl(_urlString)
            }

            val buttonLayout = findViewById<LinearLayout>(R.id.LicenseButtonLayout)
            buttonLayout.visibility = View.VISIBLE
            val agreeButton = buttonLayout.findViewById<Button>(R.id.licenseAgreeButton)
            agreeButton.setText(R.string.ids_lbl_agree)
            agreeButton.setOnClickListener(this)
            val disagreeButton = buttonLayout.findViewById<Button>(R.id.licenseDisagreeButton)
            disagreeButton.setText(R.string.ids_lbl_disagree)
            disagreeButton.setOnClickListener(this)
            val vf = findViewById<ViewFlipper>(R.id.viewFlipper)
            vf.showNext()
            return
        }

        //reset secure print values
        val prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.appContext!!)
        val editor = prefs.edit()
        editor.putBoolean(
            AppConstants.PREF_KEY_AUTH_SECURE_PRINT,
            AppConstants.PREF_DEFAULT_AUTH_SECURE_PRINT
        )
        editor.putString(
            AppConstants.PREF_KEY_AUTH_PIN_CODE,
            AppConstants.PREF_DEFAULT_AUTH_PIN_CODE
        )
        editor.commit()
        if (launchIntent == null) {
            Logger.logError(SplashActivity::class.java, "Cannot create Intent")
            throw NullPointerException("Cannot create Intent")
        }
        var data: Uri? = null
        var clipData: ClipData? = null
        val intent = intent
        if (intent != null) {
            val action = intent.action
            if (Intent.ACTION_VIEW == action) {
                data = intent.data
            } else if (Intent.ACTION_SEND == action) {
                if (intent.extras!!.parcelable<Parcelable>(Intent.EXTRA_STREAM) != null) {
                    data = Uri.parse(
                        intent.extras!!.parcelable<Parcelable>(Intent.EXTRA_STREAM).toString()
                    )
                } else {
                    // An invalid intent was sent by the third party app
                    launchIntent.putExtra(Intent.EXTRA_TEXT, AppConstants.ERR_KEY_INVALID_INTENT)
                }
            } else if (Intent.ACTION_SEND_MULTIPLE == action) {
                clipData = intent.clipData
            }
        }

        // Notify PDF File Data that there is a new PDF
        PDFFileManager.clearSandboxPDFName(SmartDeviceApp.appContext)
        PDFFileManager.setHasNewPDFData(
            SmartDeviceApp.appContext,
            data != null || clipData != null
        )
        when {
            data != null -> {
                launchIntent.data = data
            }

            clipData != null -> {
                launchIntent.clipData = clipData
                launchIntent.action = Intent.ACTION_SEND_MULTIPLE
            }

            else -> {
                // delete PDF cache
                val file = File(PDFFileManager.sandboxPath)
                try {
                    FileUtils.delete(file)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        var flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        flags = flags or Intent.FLAG_ACTIVITY_NEW_TASK
        if (isTaskRoot) {
            flags = flags or Intent.FLAG_ACTIVITY_NO_ANIMATION
        }

        // https://stackoverflow.com/questions/41743978/permission-denial-media-documents-provider
        // Propagate permission to MainActivity so that when data/clipData gets Allowed from
        // SplashActivity (when user selects file from Open In), URI can be accessed
        flags = flags or Intent.FLAG_GRANT_READ_URI_PERMISSION
        launchIntent.flags = flags
        try {
            startActivity(launchIntent)
        } catch (e: ActivityNotFoundException) {
            Logger.logError(
                SplashActivity::class.java,
                "Fatal Error: Intent MainActivity Not Found is not defined"
            )
            throw e
        } catch (e: AndroidRuntimeException) {
            Logger.logError(SplashActivity::class.java, "Fatal Error: Android runtime")
            throw e
        }

        finish()
    }

    private val _urlString: String
        get() {
            val htmlFolder = getString(R.string.html_folder)
            val helpHtml = getString(R.string.license_html)
            return AppUtils.getLocalizedAssetFullPath(this, htmlFolder, helpHtml)!!
        }

    // ================================================================================
    // INTERFACE - PauseableHandlerCallback
    // ================================================================================
    override fun storeMessage(message: Message?): Boolean {
        return message!!.what == MESSAGE_RUN_MAIN_ACTIVITY
    }

    override fun processMessage(message: Message?) {
        if (message!!.what == MESSAGE_RUN_MAIN_ACTIVITY) {
            if (_databaseInitialized) {
                runMainActivity()
            }
        }
    }
    // ================================================================================
    // Internal Classes
    // ================================================================================
    /**
     * @class DBInitTask
     *
     * @brief Async task for initializing database.
     */
    private inner class DBInitTask : BaseTask<Void?, Void?>() {
        override fun doInBackground(vararg params: Void?): Void? {
            val manager = DatabaseManager(this@SplashActivity)
            manager.writableDatabase
            manager.close()
            saveToPrefs()
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            if (!this@SplashActivity.isFinishing) {
                if (_handler!!.hasStoredMessage(MESSAGE_RUN_MAIN_ACTIVITY)) {
                    _databaseInitialized = true
                } else {
                    val activity: Activity = SmartDeviceApp.activity!!
                    activity.runOnUiThread { runMainActivity() }
                }
            }
        }

        /**
         * @brief Save database version to shared preference.
         */
        private fun saveToPrefs() {
            val prefs = PreferenceManager.getDefaultSharedPreferences(this@SplashActivity)
            val editor = prefs.edit()
            editor.putInt(AppConstants.PREF_KEY_DB_VERSION, DatabaseManager.DATABASE_VERSION)
            editor.apply()
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.licenseAgreeButton -> {
                // save to shared preferences
                val preferences = getSharedPreferences("licenseAgreementPrefs", MODE_PRIVATE)
                val edit = preferences.edit()
                edit.putBoolean("licenseAgreementDone", true)
                //edit.putBoolean("licenseAgreementDone",false);
                edit.apply()

                // show permission onboarding screens
                findViewById<View>(R.id.settingsButton).setOnClickListener(this)
                findViewById<View>(R.id.startButton).setOnClickListener(this)
                val infoText = findViewById<View>(R.id.txtPermissionInfo) as TextView
                infoText.text =
                    getString(
                        R.string.ids_lbl_permission_information,
                        getString(R.string.ids_app_name)
                    )
                (findViewById<View>(R.id.viewFlipper) as ViewFlipper).showNext()
            }

            R.id.licenseDisagreeButton -> {
                // alert box
                val title = getString(R.string.ids_lbl_license)
                val message = getString(R.string.ids_err_msg_disagree_to_license)
                val buttonTitle = getString(R.string.ids_lbl_ok)
                val newContext =
                    ContextThemeWrapper(this, android.R.style.TextAppearance_Holo_DialogWindowTitle)
                val builder = AlertDialog.Builder(newContext)
                builder.apply {
                    setTitle(title)
                    setMessage(message)
                    setNegativeButton(buttonTitle, null)
                }
                    .create()
                    .show()
            }

            R.id.startButton -> {
                // start Home Screen
                runMainActivity()
            }

            R.id.settingsButton -> {
                // Go to Settings screen screen
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                _resultLauncher.launch(intent)
            }

            else -> {
                v.performClick()
            }
        }
    }

    private var _resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            runMainActivity()
        }

    companion object {
        /// Message ID for running main activity
        const val MESSAGE_RUN_MAIN_ACTIVITY = 0x10001
        const val KEY_DB_INITIALIZED = "database_initialized"
    }
}
