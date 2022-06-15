/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * MenuFragment.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.fragment

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import jp.co.riso.smartdeviceapp.SmartDeviceApp.Companion.appContext
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager.Companion.getSandboxPDFName
import jp.co.riso.smartdeviceapp.view.MainActivity
import jp.co.riso.smartdeviceapp.view.base.BaseFragment
import jp.co.riso.smartprint.R

/**
 * @class MenuFragment
 *
 * @brief Web fragment class for Menu Screen.
 */
class MenuFragment : BaseFragment(), View.OnClickListener {
    var mState = STATE_HOME
    var hasPdfFile = false

    override val viewLayout: Int
        get() = R.layout.fragment_menu

    override fun initializeFragment(savedInstanceState: Bundle?) {
        if (activity is MainActivity) {
            val activity = activity as MainActivity?
            val isOpenIn =
                activity!!.intent != null && (activity.intent.data != null || activity.intent.clipData != null)
            val isInSandbox = getSandboxPDFName(appContext) != null
            hasPdfFile = isInSandbox || isOpenIn
        }
    }

    override fun initializeView(view: View, savedInstanceState: Bundle?) {
        view.findViewById<View>(R.id.homeButton).setOnClickListener(this)
        view.findViewById<View>(R.id.printPreviewButton).setOnClickListener(this)
        view.findViewById<View>(R.id.printersButton).setOnClickListener(this)
        view.findViewById<View>(R.id.printJobsButton).setOnClickListener(this)
        view.findViewById<View>(R.id.settingsButton).setOnClickListener(this)
        view.findViewById<View>(R.id.helpButton).setOnClickListener(this)
        view.findViewById<View>(R.id.legalButton).setOnClickListener(this)
        mState = savedInstanceState?.getInt(
            KEY_STATE,
            STATE_HOME
        )
                // No need to restore the fragment state as this is already handled
            ?: // No states were saved
                    if (hasPdfFile) STATE_PRINTPREVIEW else STATE_HOME
        setSelectedButton(view, mState)
    }

    override fun initializeCustomActionBar(view: View, savedInstanceState: Bundle?) {
        //This has no custom action bar
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_STATE, mState)
    }

    // ================================================================================
    // Private Methods
    // ================================================================================
    /**
     * @brief This method sets the state of the selected button.
     *
     * @param view Parent view
     * @param state Fragment state
     */
    private fun setSelectedButton(view: View, state: Int) {
        if (state < 0 || state >= MENU_ITEMS.size) {
            return
        }
        for (i in MENU_ITEMS.indices) {
            if (i == STATE_PRINTPREVIEW && !hasPdfFile) {
                view.findViewById<View>(MENU_ITEMS[i]).isSelected = false
                view.findViewById<View>(MENU_ITEMS[i]).isClickable =
                    false
                view.findViewById<View>(MENU_ITEMS[i]).setBackgroundColor(
                    ContextCompat.getColor(requireActivity(),R.color.theme_light_4)
                )
            } else {
                view.findViewById<View>(MENU_ITEMS[i]).isSelected = false
                view.findViewById<View>(MENU_ITEMS[i]).isClickable =
                    true
            }
        }
        view.findViewById<View>(MENU_ITEMS[state]).isSelected =
            true
        view.findViewById<View>(MENU_ITEMS[state]).isClickable =
            false
    }

    /**
     * @brief This method sets the state of the Menu Fragment.
     *
     * @param state Fragment state
     */
    fun setCurrentState(state: Int) {
        if (mState != state) {
            setSelectedButton(requireView(), state)
            switchToFragment(state)
            mState = state
        }
        if (activity is MainActivity) {
            val activity = activity as MainActivity?
            activity!!.closeDrawers()
        }
    }

    /**
     * @brief Switch to fragment.
     *
     * @param state Fragment state
     */
    private fun switchToFragment(state: Int) {
        val fm = parentFragmentManager
        fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        val ft = fm.beginTransaction()
        val container = fm.findFragmentById(R.id.mainLayout)
        if (container != null) {
            if (isChromeBook) {
                // Avoid rotation issues in Chrome
                if (container.retainInstance) {
                    ft.detach(container)
                } else {
                    ft.remove(container)
                }
            } else {
                if (container is PrintPreviewFragment || container is PrintersFragment || container is PrintJobsFragment) {
                    ft.detach(container)
                } else {
                    ft.remove(container)
                }
            }
        }
        val tag = FRAGMENT_TAGS[state]

        // Check retained fragments
        var fragment = fm.findFragmentByTag(tag) as BaseFragment?
        if (fragment == null) {
            when (state) {
                STATE_HOME -> fragment = HomeFragment()
                STATE_PRINTPREVIEW -> fragment = PrintPreviewFragment()
                STATE_PRINTERS -> fragment = PrintersFragment()
                STATE_PRINTJOBS -> fragment = PrintJobsFragment()
                STATE_SETTINGS -> fragment = SettingsFragment()
                STATE_HELP -> fragment = HelpFragment()
                STATE_LEGAL -> fragment = LegalFragment()
            }
            ft.add(R.id.mainLayout, fragment!!, tag)
        } else {
            ft.attach(fragment)
        }
        setIconState(R.id.menu_id_action_button, true)
        ft.commit()
    }

    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================
    override fun onClick(v: View) {
        when (v.id) {
            R.id.homeButton -> {
                setCurrentState(STATE_HOME)
            }
            R.id.printPreviewButton -> {
                setCurrentState(STATE_PRINTPREVIEW)
            }
            R.id.printersButton -> {
                setCurrentState(STATE_PRINTERS)
            }
            R.id.printJobsButton -> {
                setCurrentState(STATE_PRINTJOBS)
            }
            R.id.settingsButton -> {
                setCurrentState(STATE_SETTINGS)
            }
            R.id.helpButton -> {
                setCurrentState(STATE_HELP)
            }
            R.id.legalButton -> {
                setCurrentState(STATE_LEGAL)
            }
        }
    }

    companion object {
        /// Print Preview Screen
        const val STATE_HOME = 0

        /// Print Preview Screen
        const val STATE_PRINTPREVIEW = 1

        /// Printers Screen
        const val STATE_PRINTERS = 2

        /// Print Jobs Screen
        const val STATE_PRINTJOBS = 3

        /// Settings Screen
        const val STATE_SETTINGS = 4

        /// Help Screen
        const val STATE_HELP = 5

        /// Legal Screen
        const val STATE_LEGAL = 6

        /// Menu Fragment key state
        const val KEY_STATE = "MenuFragment_State"
        @JvmField
        var MENU_ITEMS = intArrayOf(
            R.id.homeButton,
            R.id.printPreviewButton,
            R.id.printersButton,
            R.id.printJobsButton,
            R.id.settingsButton,
            R.id.helpButton,
            R.id.legalButton
        )
        var FRAGMENT_TAGS = arrayOf(
            "fragment_home",
            "fragment_printpreview",
            "fragment_printers",
            "fragment_printjobs",
            "fragment_settings",
            "fragment_help",
            "fragment_legal"
        )
    }
}