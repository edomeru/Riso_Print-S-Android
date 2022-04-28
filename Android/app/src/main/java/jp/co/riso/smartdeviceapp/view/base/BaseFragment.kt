/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * BaseFragment.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.base

import android.app.ActionBar
import android.view.View.OnLayoutChangeListener
import android.os.Bundle
import android.view.*
import jp.co.riso.smartprint.R
import android.widget.FrameLayout
import jp.co.riso.android.util.AppUtils
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.view.MainActivity
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import jp.co.riso.android.util.Logger

/**
 * @class BaseFragment
 *
 * @brief Base fragment class
 */
abstract class BaseFragment() : DialogFragment(), OnLayoutChangeListener, View.OnClickListener {
    private var mIconState = false
    private var mIconId = 0
    private var mIconIdToRestore = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.logStartTime(activity, this.javaClass, "Fragment Instance")
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            mIconState = savedInstanceState.getBoolean(KEY_ICON_STATE)
            mIconIdToRestore = savedInstanceState.getInt(KEY_ICON_ID)
        }
        initializeFragment(savedInstanceState)
        Logger.logStopTime(activity, this.javaClass, "Fragment Instance")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Logger.logStartTime(activity, this.javaClass, "Fragment View")
        if (dialog != null) {
            dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        }
        val view = inflater.inflate(viewLayout, container, false)
        if (view.findViewById<View?>(R.id.actionBarLayout) != null) {
            initializeCustomActionBar(view, savedInstanceState)

            // Add size change listener to prevent overlaps
            if (view.findViewById<View>(R.id.actionBarLayout) is FrameLayout) {
                view.findViewById<View>(R.id.actionBarLayout).addOnLayoutChangeListener(this)
            }
        }

        // Let the action bar be initialized first
        initializeView(view, savedInstanceState)

        // set width and height of dialog
        if (dialog != null) {

            /* set height of item after title bar */
            val mainView = view.findViewById<View>(R.id.rootView)
            if (mainView != null && mainView.layoutParams != null) {
                val width = resources.getDimensionPixelSize(R.dimen.dialog_width)
                val height = resources.getDimensionPixelSize(R.dimen.dialog_height)
                mainView.layoutParams.width = width
                mainView.layoutParams.height = height
            }
        }

        // AppUtils.changeChildrenFont((ViewGroup) view, SmartDeviceApp.getAppFont());
        Logger.logStopTime(activity, this.javaClass, "Fragment View")
        return view
    }

    override fun onResume() {
        super.onResume()
        // restore selected only if screen is rotated
        if (mIconIdToRestore != 0 && mIconState) {
            setIconState(mIconIdToRestore, true)
        }
        mIconIdToRestore = 0
        AppUtils.hideSoftKeyboard(activity)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_ICON_STATE, mIconState)
        outState.putInt(KEY_ICON_ID, mIconId)
        mIconIdToRestore = mIconId

        // Get selected menu item and store it in bundle
        val parentView = view
        var selectedChildId = -1
        if (parentView != null) {
            val rightActionLayout =
                parentView.findViewById<ViewGroup>(R.id.rightActionLayout) ?: return
            for (idx in 0 until rightActionLayout.childCount) {
                val child = rightActionLayout.getChildAt(idx)
                if (child.isSelected) {
                    selectedChildId = child.id
                    break
                }
            }
        }
        outState.putInt(KEY_SELECTED_MENU_ITEM, selectedChildId)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState == null) return

        // Retrieve selected menu item
        // If value retrieved is -1, there is no selected menu item
        val selectedChildId = savedInstanceState.getInt(KEY_SELECTED_MENU_ITEM)
        if (selectedChildId != -1) {
            setIconState(selectedChildId, true)
        }
    }
    // ================================================================================
    // Abstract Functions
    // ================================================================================
    /**
     * @brief Gets the view layout id associated with this fragment.
     *
     * @return Layout id of the view
     */
    abstract val viewLayout: Int

    /**
     * @brief Initialization of the fragment is performed.
     *
     * @param savedInstanceState Bundle which contains a saved state during recreation
     */
    abstract fun initializeFragment(savedInstanceState: Bundle?)

    /**
     * @brief Initialization of the fragment view is performed.
     *
     * @param view The view of the fragment
     * @param savedInstanceState Bundle which contains a saved state during recreation
     */
    abstract fun initializeView(view: View?, savedInstanceState: Bundle?)

    /**
     * @brief Initializes the custom action bar.
     *
     * @param view The view of the fragment
     * @param savedInstanceState Bundle which contains the saved state during recreation
     */
    abstract fun initializeCustomActionBar(view: View?, savedInstanceState: Bundle?)
    // ================================================================================
    // Public Functions
    // ================================================================================
    /**
     * @brief Checks whether the device is in tablet mode.
     *
     * @retval true Device is a tablet
     * @retval false Device is a phone
     */
    val isTablet: Boolean
        get() = if (activity == null) {
            false
        } else resources.getBoolean(R.bool.is_tablet)

    /**
     * @brief Checks whether the fragment is on the right drawer
     *
     * @retval true Fragment is on the right drawer
     * @retval false Fragment is not on the right drawer
     */
    val isOnRightDrawer: Boolean
        get() {
            val fm = parentFragmentManager
            return this === fm.findFragmentById(R.id.rightLayout)
        }

    /**
     * @brief Checks whether the device is a Chrome Book.
     *
     * @retval true Device is a chrome book
     * @retval false Device is a tablet or phone
     */
    val isChromeBook: Boolean
        get() = if (activity == null) {
            false
        } else requireActivity().packageManager.hasSystemFeature(AppConstants.CHROME_BOOK)

    /**
     * @brief Adds an action menu button which by defaults draws the left drawer.
     *
     * @param v Action bar view
     */
    fun addActionMenuButton(v: View) {
        addMenuButton(
            v,
            R.id.leftActionLayout,
            R.id.menu_id_action_button,
            R.drawable.selector_actionbar_mainmenu,
            this
        )
    }

    /**
     * @brief Adds a menu button with the specified parameters.
     *
     * @param v Action bar view
     * @param layoutId Layout id of the view
     * @param viewId View id to be associated with the menu button
     * @param imageResId Image resource id of the menu button
     * @param listener OnClickListener of the menu button
     *
     * @return menu button added
     */
    fun addMenuButton(
        v: View,
        layoutId: Int,
        viewId: Int,
        imageResId: Int,
        listener: View.OnClickListener?
    ): View {
        val li = LayoutInflater.from(v.context)
        val button = li.inflate(R.layout.actionbar_button, null) as ImageView
        button.id = viewId
        button.setImageResource(imageResId)
        button.setOnClickListener(listener)
        val width = (activity as BaseActivity?)!!.actionBarHeight
        val layout = v.findViewById<ViewGroup>(layoutId)
        layout.addView(button, width, ActionBar.LayoutParams.MATCH_PARENT)
        return button
    }

    /**
     * @brief Sets icon's selected state.
     *
     * @param id Icon id
     * @param state Icon selected state
     */
    fun setIconState(id: Int, state: Boolean) {
        if (view != null && view?.findViewById<View?>(id) != null) {
            view?.findViewById<View>(id)?.isSelected = state
            mIconState = state
            mIconId = id
        }
    }

    /**
     * @brief Resets the icon states to not selected.
     */
    open fun clearIconStates() {
        if (view != null) {
            val menuButton = view?.findViewById<View>(R.id.menu_id_action_button)
            if (menuButton != null) {
                setIconState(R.id.menu_id_action_button, false)
            }
        }
        mIconState = false
        mIconId = 0
    }

    /**
     * @brief Translates key press from activity to fragment
     *
     * @param keyCode Key that was pressed
     *
     * @return If key press was handled
     */
    open fun onKeyUp(keyCode: Int): Boolean {
        return false
    }

    // ================================================================================
    // INTERFACE - View.OnLayoutChangeListener
    // ================================================================================
    override fun onLayoutChange(
        v: View,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int
    ) {
        if (v.id == R.id.actionBarLayout) {
            val leftWidth = v.findViewById<View>(R.id.leftActionLayout).width
            val rightWidth = v.findViewById<View>(R.id.rightActionLayout).width
            v.findViewById<View>(R.id.actionBarTitle).layoutParams.width =
                right - left - leftWidth.coerceAtLeast(rightWidth) * 2
        }
    }

    // ================================================================================
    // INTERFACE - View.OnLayoutChangeListener
    // ================================================================================
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.menu_id_action_button) {
            if (activity != null && activity is MainActivity) {
                val activity = activity as MainActivity?
                activity!!.openDrawer(Gravity.LEFT)
            }
        }
    }

    /**
     * Called when the fragment is closed/hidden as a right drawer
     */
    open fun onRightFragmentDrawerClosed() {
        onPause()
    }

    companion object {
        private const val KEY_ICON_STATE = "icon_state"
        private const val KEY_ICON_ID = "icon_id"
        private const val KEY_SELECTED_MENU_ITEM = "selected_menu_item"
    }
}