/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * MainActivity.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.Message
import android.view.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.radaee.pdf.Global
import jp.co.riso.android.os.pauseablehandler.PauseableHandler
import jp.co.riso.android.os.pauseablehandler.PauseableHandlerCallback
import jp.co.riso.android.util.FileUtils
import jp.co.riso.android.util.Logger
import jp.co.riso.android.util.NetUtils
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager
import jp.co.riso.smartdeviceapp.view.base.BaseActivity
import jp.co.riso.smartdeviceapp.view.base.BaseFragment
import jp.co.riso.smartdeviceapp.view.fragment.HomeFragment
import jp.co.riso.smartdeviceapp.view.fragment.MenuFragment
import jp.co.riso.smartdeviceapp.view.fragment.PrintPreviewFragment
import jp.co.riso.smartdeviceapp.view.widget.SDADrawerLayout
import jp.co.riso.smartprint.R
import java.io.File
import java.io.IOException
import kotlin.math.abs


/**
 * @class MainActivity
 * 
 * @brief Main activity class.
 */
class MainActivity : BaseActivity(), PauseableHandlerCallback {
    private var _drawerLayout: SDADrawerLayout? = null
    private var _mainLayout: ViewGroup? = null
    private var _leftLayout: ViewGroup? = null
    private var _rightLayout: ViewGroup? = null
    private var _drawerToggle: ActionBarDrawerToggle? = null
    private var _resizeView = false
    private var _handler: PauseableHandler? = null

    var menuFragment: MenuFragment? = null
        private set

    @Volatile
    private var _isRadaeeInitialized = false
    override fun onCreateContent(savedInstanceState: Bundle?) {
        if (intent != null && intent.data != null) {
            Logger.logStartTime(this, this.javaClass, "Open-in")
        } else {
            Logger.logStartTime(this, this.javaClass, "AppLaunch")
        }
        if (intent != null && (intent.data != null || intent.clipData != null)) { // check if Open-In
            // Check first if device is Android 13
            var permissionType = Manifest.permission.WRITE_EXTERNAL_STORAGE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionType = Manifest.permission.READ_MEDIA_IMAGES
            }

            if (ContextCompat.checkSelfPermission(this, permissionType)
                == PackageManager.PERMISSION_GRANTED
            ) {
                // permission is granted, initialize Radaee (uses external storage)
                initializeRadaee()
            }
        }
        _handler = PauseableHandler(Looper.myLooper(), this)
        setContentView(R.layout.activity_main)
        _drawerLayout = findViewById(R.id.drawerLayout)
        _drawerLayout?.setScrimColor(Color.TRANSPARENT)
        _mainLayout = findViewById(R.id.mainLayout)
        _leftLayout = findViewById(R.id.leftLayout)
        _rightLayout = findViewById(R.id.rightLayout)
        _leftLayout?.layoutParams?.width = drawerWidth
        _rightLayout?.layoutParams?.width = drawerWidth

        // RM1008 workaround for text field auto focus due to OS behavior
        // https://stackoverflow.com/questions/7593887/disable-auto-focus-on-edit-text
        // exclude Chromebook because it will affect keyboard navigation
        // and text field autofocus does not occur on Chromebook
        if (!packageManager.hasSystemFeature(AppConstants.CHROME_BOOK)) {
            _rightLayout?.isFocusableInTouchMode = true
        }
        _drawerToggle = SDAActionBarDrawerToggle(
            this,
            _drawerLayout,
            R.string.default_content_description,
            R.string.default_content_description
        )

        // Set the drawer toggle as the DrawerListener
        _drawerLayout?.addDrawerListener(_drawerToggle!!)
        _drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        if (actionBar != null) {
            actionBar!!.setDisplayHomeAsUpEnabled(true)
            actionBar!!.setHomeButtonEnabled(true)
        }

        // Begin Fragments
        if (savedInstanceState == null) {
            val fm = supportFragmentManager
            val ft = fm.beginTransaction()
            val fragment = if (intent != null && (intent.data != null || intent.clipData != null)) {
                PrintPreviewFragment()
            } else {
                HomeFragment()
            }
            ft.add(R.id.mainLayout, fragment, fragment.tag)

            menuFragment = MenuFragment()
            ft.add(R.id.leftLayout, menuFragment!!)
            
            ft.commit()
        } else {
            _resizeView = savedInstanceState.getBoolean(KEY_RESIZE_VIEW, false)
            if (savedInstanceState.getBoolean(KEY_LEFT_OPEN, false)) {
                _mainLayout?.translationX = drawerWidth.toFloat()
            } else if (savedInstanceState.getBoolean(KEY_RIGHT_OPEN, false)) {
                if (!_resizeView) {
                    _mainLayout?.translationX = -drawerWidth.toFloat()
                }
            } else {
                val msg = Message.obtain(_handler, MSG_CLEAR_ICON_STATES)
                _handler!!.sendMessage(msg)
            }
        }
        NetUtils.registerNetworkCallback(this)

    }

    override fun onDestroy() {
        //Debug.waitForDebugger();
        // do not delete if from Home screen picker
        if (isFinishing && intent != null && intent.getIntExtra(
                AppConstants.EXTRA_FILE_FROM_PICKER,
                1
            ) < 0
        ) {
            // delete PDF cache
            val file = File(PDFFileManager.sandboxPath)
            try {
                FileUtils.delete(file)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            NetUtils.unregisterNetworkCallback(this)
        }

        super.onDestroy()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        _drawerToggle!!.syncState()
        if (intent != null && intent.data != null) {
            Logger.logStopTime(this, this.javaClass, "Open-in")
        } else {
            Logger.logStopTime(this, this.javaClass, "AppLaunch")
        }
    }

    @SuppressLint("RtlHardcoded")
    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_LEFT_OPEN, _drawerLayout!!.isDrawerOpen(Gravity.LEFT))
        outState.putBoolean(KEY_RIGHT_OPEN, _drawerLayout!!.isDrawerOpen(Gravity.RIGHT))
        outState.putBoolean(KEY_RESIZE_VIEW, _resizeView)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (_drawerToggle!!.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(
            item
        )
    }

    override fun onPause() {
        super.onPause()
        _handler!!.pause()
    }

    override fun onResume() {
        super.onResume()
        _handler!!.resume()
    }

    override fun onStart() {
        super.onStart()

        // RM805 workaround for app hang in Android 11
        // If following steps are done, the view will become unresponsive but fragment is still OK:
        // 1. PDF is open in preview screen
        // 2. Go to a different screen and then return to preview screen
        // 3. Minimize the app and then return to the app
        // Workaround: Re-attach the fragment in order to recreate the view
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val fm = supportFragmentManager
            val screen = fm.findFragmentById(R.id.mainLayout)
            if (screen is PrintPreviewFragment && !screen.isConversionOngoing) {
                fm.beginTransaction().detach(screen).commitNow()
                fm.beginTransaction().attach(screen).commitNow()
                // print settings button state need to be updated if print settings is open
                if (isDrawerOpen(Gravity.RIGHT)) {
                    screen.requireView().findViewById<View>(R.id.view_id_print_button).isSelected =
                        true
                }
            }
        }
    }

    @SuppressLint("RtlHardcoded")
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        _drawerToggle!!.onConfigurationChanged(newConfig)
        val fragment = supportFragmentManager.findFragmentById(R.id.mainLayout) as BaseFragment?
        if (!_drawerLayout!!.isDrawerOpen(Gravity.RIGHT) && !_drawerLayout!!.isDrawerOpen(Gravity.LEFT)) {
            fragment!!.clearIconStates()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (_drawerLayout!!.isDrawerOpen(Gravity.LEFT) || _drawerLayout!!.isDrawerOpen(Gravity.RIGHT)) {
            closeDrawers()
        } else {
            val fm = supportFragmentManager
            val screen = fm.findFragmentById(R.id.mainLayout)
            if (screen is PrintPreviewFragment) {
                moveTaskToBack(true)
            } else {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        val layoutId =
            when {
                _drawerLayout!!.isDrawerOpen(Gravity.RIGHT) -> R.id.rightLayout
                _drawerLayout!!.isDrawerOpen(Gravity.LEFT) -> R.id.leftLayout
                else -> R.id.mainLayout
            }
        val fragment = supportFragmentManager.findFragmentById(layoutId) as BaseFragment?
        val menuFragment = supportFragmentManager.findFragmentById(R.id.leftLayout) as MenuFragment?

        return when (keyCode) {
            KeyEvent.KEYCODE_ENTER -> {
                fragment!!.onKeyUp(keyCode)
            }
            // Shortcut key : Open File CTRL + O
            KeyEvent.KEYCODE_O -> {
                if (event.isCtrlPressed) {
                    menuFragment!!.setCurrentState(menuFragment.STATE_HOME)
                    fragment!!.onKeyUp(keyCode)
                } else {
                    super.onKeyUp(keyCode, event)
                }
            }
            // Shortcut key : Help F1, About SHIFT + F1
            KeyEvent.KEYCODE_F1 -> {
                if (event.isShiftPressed) {
                    menuFragment!!.setCurrentState(menuFragment.STATE_LEGAL)
                } else {
                    menuFragment!!.setCurrentState(menuFragment.STATE_HELP)
                }
                fragment!!.onKeyUp(keyCode)
            }
            // Shortcut key : Print CTRL + P
            KeyEvent.KEYCODE_P -> {
                if (event.isCtrlPressed && fragment!! is PrintPreviewFragment) {
                    (fragment as PrintPreviewFragment).openPrintSettings()
                    fragment.onKeyUp(keyCode)
                } else {
                    super.onKeyUp(keyCode, event)
                }
            }
            // Shortcut key : Quit CTRL + Q or ALT + Q
            KeyEvent.KEYCODE_Q -> {
                if (event.isCtrlPressed || event.isAltPressed) {
                    finishAndRemoveTask()
                    fragment!!.onKeyUp(keyCode)
                } else {
                    super.onKeyUp(keyCode, event)
                }
            }
            // Shortcut key : Quit ALT + F4
            KeyEvent.KEYCODE_F4 -> {
                if (event.isAltPressed) {
                    finishAndRemoveTask()
                    fragment!!.onKeyUp(keyCode)
                } else {
                    super.onKeyUp(keyCode, event)
                }
            }
            else -> super.onKeyUp(keyCode, event)
        }
    }

    // ================================================================================
    // Public Functions
    // ================================================================================
    /**
     * @brief Open Drawer.
     * 
     * @param gravity Drawer gravity
     * @param resizeView Prevent layout from touches

     */
    @JvmOverloads
    fun openDrawer(gravity: Int, resizeView: Boolean = false) {
        val msg = Message.obtain(_handler, MSG_OPEN_DRAWER)
        msg.arg1 = gravity
        msg.arg2 = 0
        if (gravity == Gravity.RIGHT) {
            msg.arg2 = if (resizeView) 1 else 0
        }
        _handler!!.sendMessage(msg)
    }

    /**
     * @brief Close drawers.
     */
    fun closeDrawers() {
        val msg = Message.obtain(_handler, MSG_CLOSE_DRAWER)
        _handler!!.sendMessage(msg)
    }

    /**
     * @brief Determines if the drawer indicated by gravity is open.
     *
     * @param gravity Drawer gravity
     *
     * @retval true The drawer is open
     * @retval false The drawer is close
     */
    fun isDrawerOpen(gravity: Int): Boolean {
        return _drawerLayout!!.isDrawerOpen(gravity)
    }

    // ================================================================================
    // INTERFACE - PauseableHandlerCallback 
    // ================================================================================
    override fun storeMessage(message: Message?): Boolean {
        return message!!.what == MSG_OPEN_DRAWER || message.what == MSG_CLOSE_DRAWER || message.what == MSG_CLEAR_ICON_STATES
    }

    @SuppressLint("RtlHardcoded")
    override fun processMessage(message: Message?) {
        val fragment = supportFragmentManager.findFragmentById(R.id.mainLayout) as BaseFragment?
        val gravityLeft = message!!.arg1 == Gravity.LEFT
        when (message.what) {
            MSG_OPEN_DRAWER -> {
                _drawerLayout!!.closeDrawers()
                fragment?.setIconState(R.id.menu_id_action_button, gravityLeft)
                _resizeView = message.arg2 == 1
                _drawerLayout!!.openDrawer(message.arg1)
            }
            MSG_CLOSE_DRAWER -> _drawerLayout!!.closeDrawers()
            MSG_CLEAR_ICON_STATES -> fragment?.clearIconStates()
        }
    }
    // ================================================================================
    // Internal Classes
    // ================================================================================
    /**
     * @class SDAActionBarDrawerToggle
     *
     * @brief Class for Action Bar Drawer toggle.
     */
    private inner class SDAActionBarDrawerToggle
    /**
     * @brief Constructor.
     *
     * @param activity Activity
     * @param drawerLayout Drawer layout
     * @param openDrawerContentDescRes Drawer content description resource
     * @param closeDrawerContentDescRes Drawer content description resource
     */
        (
        activity: Activity?, drawerLayout: DrawerLayout?, openDrawerContentDescRes: Int,
        closeDrawerContentDescRes: Int
    ) : ActionBarDrawerToggle(
        activity,
        drawerLayout,
        openDrawerContentDescRes,
        closeDrawerContentDescRes
    ) {
        override fun syncState() {
            super.syncState()
            if (_resizeView && _drawerLayout!!.isDrawerOpen(Gravity.RIGHT)) {
                _mainLayout!!.setPadding(0, 0, drawerWidth, 0)
                _mainLayout!!.requestLayout()
            }
        }

        override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            var moveFactor = _leftLayout!!.width * slideOffset
            if (drawerView.id == _rightLayout!!.id) {
                moveFactor *= -1f
            }
            if (_resizeView && drawerView.id == _rightLayout!!.id) {
                _mainLayout!!.setPadding(0, 0, abs(moveFactor).toInt(), 0)
            } else {
                _mainLayout!!.translationX = moveFactor
            }
        }

        @SuppressLint("RtlHardcoded")
        override fun onDrawerStateChanged(newState: Int) {
            super.onDrawerStateChanged(newState)

            // https://code.google.com/p/android/issues/detail?id=60671
            _drawerLayout!!.post {
                if (newState == DrawerLayout.STATE_IDLE) {
                    when {
                        _drawerLayout!!.isDrawerOpen(Gravity.LEFT) -> {
                            _drawerLayout!!.setDrawerLockMode(
                                DrawerLayout.LOCK_MODE_UNLOCKED,
                                Gravity.LEFT
                            )
                            _drawerLayout!!.setDrawerLockMode(
                                DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
                                Gravity.RIGHT
                            )
                        }
                        _drawerLayout!!.isDrawerOpen(Gravity.RIGHT) -> {
                            _drawerLayout!!.setDrawerLockMode(
                                DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
                                Gravity.LEFT
                            )
                            _drawerLayout!!.setDrawerLockMode(
                                DrawerLayout.LOCK_MODE_UNLOCKED,
                                Gravity.RIGHT
                            )
                        }
                        else -> {
                            _drawerLayout!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                        }
                    }
                }
            }
        }

        override fun onDrawerClosed(view: View) {
            super.onDrawerClosed(view)
            invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
            val fragment = supportFragmentManager.findFragmentById(R.id.mainLayout) as BaseFragment?
            fragment!!.clearIconStates()
            if (_drawerLayout!!.findViewById<View>(R.id.rightLayout) === view) {
                val rightFragment =
                    supportFragmentManager.findFragmentById(R.id.rightLayout) as BaseFragment?
                rightFragment!!.onRightFragmentDrawerClosed()
            }
            supportFragmentManager.findFragmentById(R.id.mainLayout)!!.onResume()
        }

        override fun onDrawerOpened(drawerView: View) {
            super.onDrawerOpened(drawerView)
            invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
            if (_drawerLayout!!.findViewById<View>(R.id.rightLayout) === drawerView) {
                supportFragmentManager.findFragmentById(R.id.rightLayout)!!.onResume()
            }
            if (!_resizeView) {
                supportFragmentManager.findFragmentById(R.id.mainLayout)!!.onPause()
            }
        }
    }

    /**
     * @brief Initializes 3rd party PDF library
     */
    @Synchronized
    fun initializeRadaee() {
        if (!_isRadaeeInitialized) {
            Global.Init(this)
            _isRadaeeInitialized = true
        }
    }

    companion object {
        /// Key for right drawer
        const val KEY_RIGHT_OPEN = "right_drawer_open"

        /// Key for left drawer
        const val KEY_LEFT_OPEN = "left_drawer_open"

        /// Key for view resize
        const val KEY_RESIZE_VIEW = "resize_view"

        //public static final String KEY_TRANSLATION = "translate";
        private const val MSG_OPEN_DRAWER = 0
        private const val MSG_CLOSE_DRAWER = 1
        private const val MSG_CLEAR_ICON_STATES = 2
    }
}