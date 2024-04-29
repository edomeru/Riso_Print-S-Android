/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * PrintSettingsView.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.printsettings

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.TranslateAnimation
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.preference.PreferenceManager
import jp.co.riso.android.util.AppUtils
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.UpdateStatusCallback
// Content Print - START
import jp.co.riso.smartdeviceapp.controller.print.ContentPrintManager
import jp.co.riso.smartdeviceapp.model.ContentPrintPrintSettings
import jp.co.riso.smartdeviceapp.model.ContentPrintPrinter
// Content Print - END
import jp.co.riso.smartdeviceapp.model.Printer
import jp.co.riso.smartdeviceapp.model.printsettings.*
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.*
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.InputTrayFtGlCerezonaSOga.Companion.valuesFtCerezonaS
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.InputTrayFtGlCerezonaSOga.Companion.valuesGlOga
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.PaperSize.Companion.valuesDefault
import jp.co.riso.smartprint.R
import jp.co.riso.smartprint.R.drawable
import jp.co.riso.smartprint.R.string
import java.util.*
import kotlin.math.max

/**
 * @class PrintSettingsView
 * 
 * @brief A View that displays the Print settings and Print controls.
 *
 * @param context A Context object used to access application assets
 * @param attrs An AttributeSet passed to our parent
 * @param defStyle The default style resource ID
 */
class PrintSettingsView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(
    context!!, attrs, defStyle
), View.OnClickListener, Handler.Callback, CompoundButton.OnCheckedChangeListener,
    UpdateStatusCallback, OnEditorActionListener {

    private var _printSettings: PrintSettings? = null
    private var _printerId = PrinterManager.EMPTY_ID
    private var _printersList: List<Printer?>? = null
    private var _mainView: LinearLayout? = null
    private var _printSettingsScrollView: ScrollView? = null
    private var _printSettingsLayout: LinearLayout? = null
    private var _subView: LinearLayout? = null
    private var _subScrollView: ScrollView? = null
    private var _subOptionsLayout: LinearLayout? = null
    private var _printControls: LinearLayout? = null
    private var _handler: Handler? = null
    private var _printSettingsTitles: ArrayList<LinearLayout>? = null
    private var _printerManager: PrinterManager? = null
    
    private lateinit var _listener: PrintSettingsViewInterface

    // Content Print - START
    private lateinit var _registerToBox: ContentPrintManager.IRegisterToBoxCallback
    // Content Print - END

    /**
     * @brief Constructs the PrintSettingsView
     */
    private fun init() {
        if (isInEditMode) {
            return
        }
        _printerManager = PrinterManager.getInstance(SmartDeviceApp.appContext!!)
        _printerManager?.setUpdateStatusCallback(this)
        _handler = Handler(Looper.myLooper()!!, this)
        loadPrintersList()
        initializeMainView()
        initializePrinterControls()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // if using trackpad/mouse, placing the cursor near the right edge of the scrollview shows the scrollbar
        // this has an overlap with the scrollview which blocks tap actions on the setting items
        // to keep behavior consistent with phone/tablet, hide on tap but show while dragging
        if (ev.isFromSource(InputDevice.SOURCE_MOUSE)) {
            val currentScrollView: View? =
                if (_mainView!!.isEnabled) _printSettingsScrollView else _subScrollView
            if (ev.action == MotionEvent.ACTION_MOVE) {
                currentScrollView!!.isVerticalScrollBarEnabled = true
            } else if (ev.action == MotionEvent.ACTION_DOWN) {
                currentScrollView!!.isVerticalScrollBarEnabled = false
            }
        }
        var view = _mainView!!.findViewWithTag<View>(PrintSettings.TAG_COPIES)
        if (view is EditText) {
            if (!AppUtils.checkViewHitTest(view, ev.rawX.toInt(), ev.rawY.toInt())) {
                checkEditTextValue(view)
            } else {
                return super.onInterceptTouchEvent(ev)
            }
        }
        view = _mainView!!.findViewById(R.id.view_id_pin_code_edit_text)
        if (view is EditText) {
            if (AppUtils.checkViewHitTest(view, ev.rawX.toInt(), ev.rawY.toInt())) {
                return super.onInterceptTouchEvent(ev)
            }
        }
        AppUtils.hideSoftKeyboard(context as Activity)
        return super.onInterceptTouchEvent(ev)
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        // scrolling with mouse wheel must be handled here
        if (event.isFromSource(InputDevice.SOURCE_MOUSE)) {
            val currentScrollView: View? =
                if (_mainView!!.isEnabled) _printSettingsScrollView else _subScrollView
            if (event.actionMasked == MotionEvent.ACTION_SCROLL) {
                currentScrollView!!.isVerticalScrollBarEnabled = true
            }
        }
        return super.onGenericMotionEvent(event)
    }

    /**
     * @brief Constructs the main view. Print/Printer controls and print settings
     */
    private fun initializeMainView() {
        _mainView = LinearLayout(context)
        _mainView!!.orientation = LinearLayout.VERTICAL
        _printSettingsLayout = LinearLayout(context)
        _printSettingsLayout!!.orientation = LinearLayout.VERTICAL
        _printSettingsScrollView = ScrollView(context)
        _printSettingsScrollView!!.addView(
            _printSettingsLayout,
            ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        )
        _mainView!!.addView(
            _printSettingsScrollView,
            ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        )
        addView(
            _mainView,
            ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        )
    }

    /**
     * @brief Gets the list of printers from manager
     */
    private fun loadPrintersList() {
        // Content Print - START
        if (!ContentPrintManager.isBoxRegistrationMode) {
            _printersList =
                PrinterManager.getInstance(SmartDeviceApp.appContext!!)!!.savedPrintersList
        }
        // Content Print - END
    }
    // ================================================================================
    // Save/Restore State
    // ================================================================================
    /**
     * @brief Save the state of the PrintSettingsView in the Bundle
     *
     * @param outState The Bundle to store the PrintSettingsView state.
     */
    fun saveState(outState: Bundle) {
        // Create String list of collapsed headers
        val selectedTitles = ArrayList<String>()
        for (i in _printSettingsTitles!!.indices) {
            if (_printSettingsTitles!![i].isSelected) {
                selectedTitles.add(_printSettingsTitles!![i].tag as String)
            }
        }

        // Need to convert to String array (Direct Object[] to String[] has JVM error
        var stockArr: Array<String?> = arrayOfNulls(selectedTitles.size)
        stockArr = selectedTitles.toArray(stockArr)

        // Save to bundle
        outState.putStringArray(KEY_SELECTED_TITLES, stockArr)

        // Save the scroll pos to bundle
        outState.putInt(KEY_SCROLL_POSITION, _printSettingsScrollView!!.scrollY)

        // If main scroll is visible, then sub view is displayed
        if (_mainView!!.visibility != VISIBLE) {
            outState.putString(KEY_SUBVIEW_DISPLAYED, _subView!!.tag.toString())
            outState.putInt(KEY_SUB_SCROLL_POSITION, _subScrollView!!.scrollY)
        }
    }

    /**
     * @brief Restore the state of the PrintSettingsView from a Bundle
     *
     * @param savedInstanceState The incoming Bundle of state.
     */
    fun restoreState(savedInstanceState: Bundle) {
        // Collapse the headers retrieved from the saved bundle
        val selectedTitle = savedInstanceState.getStringArray(KEY_SELECTED_TITLES)
        for (s in selectedTitle!!) {
            if (_mainView!!.findViewWithTag<View?>(s) != null) {
                collapseControl(_mainView!!.findViewWithTag(s), false)
            }
        }

        // Set the scroll position of the main scroll view, send delayed message
        // since view has not been initialized yet
        var newMessage = Message.obtain(_handler, MSG_SET_SCROLL)
        newMessage.arg1 = savedInstanceState.getInt(KEY_SCROLL_POSITION, 0)
        _handler!!.sendMessage(newMessage)
        if (savedInstanceState.containsKey(KEY_SUBVIEW_DISPLAYED)) {
            // Show the subview after a delay
            val tag = savedInstanceState.getString(KEY_SUBVIEW_DISPLAYED, "")
            newMessage = Message.obtain(_handler, MSG_SHOW_SUBVIEW)
            newMessage.obj = tag
            _handler!!.sendMessage(newMessage)

            // Set the scroll position of the subview's scroll view, send delayed message
            // since view has not been initialized yet
            newMessage = Message.obtain(_handler, MSG_SET_SUB_SCROLL)
            newMessage.arg1 = savedInstanceState.getInt(KEY_SUB_SCROLL_POSITION, 0)
            _handler!!.sendMessage(newMessage)
        }
    }
    // ================================================================================
    // Set Constraints
    // ================================================================================
    /**
     * @brief Checks if the option should be displayed
     *
     * @param tag Print settings tag
     * @param value Print settings value
     *
     * @retval true Option will be displayed
     * @retval false Option will not be displayed
     */
    private fun shouldDisplayOptionFromConstraints(tag: String, value: Int): Boolean {
        if (tag == PrintSettings.TAG_STAPLE) {
            val isTop =
                _printSettings!!.getValue(PrintSettings.TAG_FINISHING_SIDE) == FinishingSide.TOP.ordinal
            when (Staple.values()[value]) {
                Staple.ONE_UL, Staple.ONE_UR -> return isTop
                Staple.ONE -> return !isTop
                Staple.TWO, Staple.OFF -> {}
            }
        }
        if (tag == PrintSettings.TAG_IMPOSITION_ORDER) {
            val is2up =
                _printSettings!!.getValue(PrintSettings.TAG_IMPOSITION) == Imposition.TWO_UP.ordinal
            return when (ImpositionOrder.values()[value]) {
                ImpositionOrder.L_R, ImpositionOrder.R_L -> is2up
                ImpositionOrder.TL_B, ImpositionOrder.TL_R, ImpositionOrder.TR_B, ImpositionOrder.TR_L -> !is2up
            }
        }
        if (tag == PrintSettings.TAG_OUTPUT_TRAY) {
            val isPunch = _printSettings!!.punch !== Punch.OFF
            val isFold = _printSettings!!.bookletFinish !== BookletFinish.OFF
            return when (OutputTray.values()[value]) {
                OutputTray.AUTO -> true
                OutputTray.TOP, OutputTray.STACKING -> !isFold
                OutputTray.FACEDOWN -> !isPunch && !isFold
            }
        }
        // Content Print - START
        if ((ContentPrintManager.isBoxRegistrationMode && (cdsPrinter?.isPrinterFTorCEREZONA_S == true || cdsPrinter?.isPrinterGLorOGA == true)) ||
            (!ContentPrintManager.isBoxRegistrationMode && (printer!!.isPrinterFTorCEREZONA_S || printer!!.isPrinterGLorOGA))) {
        // Content Print - END
            /* Specify the input tray and paper size arrays to be used */
            val inputTrayOptions: Array<InputTrayFtGlCerezonaSOga>
            val paperSizeOptions: Array<PaperSize>
            // Content Print - START
            if ((ContentPrintManager.isBoxRegistrationMode && cdsPrinter?.isPrinterFTorCEREZONA_S == true) ||
                (!ContentPrintManager.isBoxRegistrationMode && printer!!.isPrinterFTorCEREZONA_S)) {
            // Content Print - END
                inputTrayOptions = valuesFtCerezonaS()
                paperSizeOptions = valuesDefault()
            } else {
                inputTrayOptions = valuesGlOga()
                paperSizeOptions = PaperSize.valuesGl()
            }
            if (tag == PrintSettings.TAG_PAPER_SIZE) {
                val isExternal =
                    inputTrayOptions[_printSettings!!.inputTray.ordinal] === InputTrayFtGlCerezonaSOga.EXTERNAL_FEEDER
                return when (paperSizeOptions[value]) {
                    PaperSize.A4, PaperSize.B5, PaperSize.LETTER, PaperSize.JUROKUKAI -> true
                    else -> !isExternal
                }
            }
            if (tag == PrintSettings.TAG_INPUT_TRAY) {
                val paperSize = _printSettings!!.paperSize
                // if paper size is equal to A4, B5, Letter, or 16k
                val isPaperSupported =
                    paperSize === PaperSize.A4 || paperSize === PaperSize.B5 || paperSize === PaperSize.LETTER || paperSize === PaperSize.JUROKUKAI
                return when (inputTrayOptions[value]) {
                    InputTrayFtGlCerezonaSOga.AUTO, InputTrayFtGlCerezonaSOga.STANDARD, InputTrayFtGlCerezonaSOga.TRAY1, InputTrayFtGlCerezonaSOga.TRAY2 -> true
                    // Content Print - START
                    InputTrayFtGlCerezonaSOga.TRAY3 -> if (ContentPrintManager.isBoxRegistrationMode) cdsPrinter?.isPrinterGLorOGA == true else printer!!.isPrinterGLorOGA
                    // Content Print - END
                    InputTrayFtGlCerezonaSOga.EXTERNAL_FEEDER -> isPaperSupported
                }
            }
        }
        return true
    }

    /**
     * @brief Gets the default value of a setting.
     *
     * @param tag Print settings tag
     *
     * @return Default value for the tag
     */
    private fun getDefaultValueWithConstraints(tag: String): Int {
        if (_printSettings == null) {
            return -1
        }
        return if (PrintSettings.sSettingsMaps!![_printSettings!!.settingMapKey] == null
            || PrintSettings.sSettingsMaps!![_printSettings!!.settingMapKey]!![tag] == null
        ) {
            -1
        } else PrintSettings.sSettingsMaps!![_printSettings!!.settingMapKey]!![tag]!!.defaultValue
    }

    /**
     * @brief Checks if the view is enabled.
     *
     * @param tag Print settings tag
     *
     * @retval true View is enabled
     * @retval false View is disabled
     */
    private fun isViewEnabled(tag: String): Boolean {
        if (_mainView != null) {
            if (_mainView!!.findViewWithTag<View?>(tag) != null) {
                return _mainView!!.findViewWithTag<View>(tag).isEnabled
            }
        }
        return false
    }

    /**
     * @brief Controls the state of the view
     *
     * @param tag Print settings tag
     * @param enabled The setting is enabled or not
     * @param hideControl The setting options is hidden or not
     */
    private fun setViewEnabledWithConstraints(tag: String, enabled: Boolean, hideControl: Boolean) {
        if (_mainView != null) {
            if (_mainView!!.findViewWithTag<View?>(tag) != null) {
                val view = _mainView!!.findViewWithTag<View>(tag)
                view.isEnabled = enabled
                val targetView = view.getTag(ID_TAG_TARGET_VIEW) as View
                targetView.isActivated = enabled
                val disclosureView = view.findViewById<View>(R.id.view_id_disclosure)
                if (disclosureView != null) {
                    when {
                        enabled -> {
                            view.findViewById<View>(R.id.listValueTextView).visibility = VISIBLE
                            view.findViewById<View>(R.id.disclosureIndicator).visibility = VISIBLE
                        }
                        hideControl -> {
                            view.findViewById<View>(R.id.listValueTextView).visibility =
                                INVISIBLE
                            view.findViewById<View>(R.id.disclosureIndicator).visibility = VISIBLE
                        }
                        else -> {
                            view.findViewById<View>(R.id.listValueTextView).visibility =
                                VISIBLE
                            view.findViewById<View>(R.id.disclosureIndicator).visibility =
                                VISIBLE
                        }
                    }
                } else {
                    val shouldHideControl = !enabled && hideControl
                    view.visibility = if (shouldHideControl) GONE else VISIBLE
                }
            }
        }
    }

    /**
     * @brief Updates the setting value and applies the corresponding constraint
     *
     * @param tag Print settings tag
     * @param value New settings value
     */
    private fun updateValueWithConstraints(tag: String, value: Int) {
        _printSettings!!.setValue(tag, value)
        updateDisplayedValue(tag)
    }

    /**
     * @brief Apply view constraints. Corresponds to hidden and disabled views
     *
     * @param tag Print settings tag
     */
    private fun applyViewConstraints(tag: String) {
        val value = _printSettings!!.getValue(tag)

        // Constraint #1 Booklet
        if (tag == PrintSettings.TAG_BOOKLET) {
            val enabled = value == 0
            setViewEnabledWithConstraints(PrintSettings.TAG_DUPLEX, enabled, false)
            setViewEnabledWithConstraints(PrintSettings.TAG_FINISHING_SIDE, enabled, false)
            setViewEnabledWithConstraints(PrintSettings.TAG_STAPLE, enabled, false)
            setViewEnabledWithConstraints(PrintSettings.TAG_PUNCH, enabled, false)
            //setViewEnabledWithConstraints(PrintSettings.TAG_IMPOSITION, enabled, true);
            //setViewEnabledWithConstraints(PrintSettings.TAG_IMPOSITION_ORDER, enabled, true);
            setViewEnabledWithConstraints(PrintSettings.TAG_BOOKLET_FINISH, !enabled, false)
            setViewEnabledWithConstraints(PrintSettings.TAG_BOOKLET_LAYOUT, !enabled, false)

            //applyViewConstraints(PrintSettings.TAG_IMPOSITION);
        }

        // Constraint #5 Imposition
        if (tag == PrintSettings.TAG_IMPOSITION) {
            val bookletEnabled = _printSettings!!.isBooklet
            if (bookletEnabled) {
                setViewEnabledWithConstraints(PrintSettings.TAG_IMPOSITION_ORDER,
                    enabled = false,
                    hideControl = false
                )
            } else {
                val enabled = value != Imposition.OFF.ordinal && isViewEnabled(tag)
                setViewEnabledWithConstraints(PrintSettings.TAG_IMPOSITION_ORDER, enabled, true)
            }
        }
    }

    /**
     * @brief Apply value constraints. Corresponds to dependency of values.
     *
     * @param tag Print Settings Tag
     * @param prevValue Previous value
     */
    private fun applyValueConstraints(tag: String, prevValue: Int) {
        val value = _printSettings!!.getValue(tag)

        // Constraint #1 Booklet
        if (tag == PrintSettings.TAG_BOOKLET) {
            updateValueWithConstraints(
                PrintSettings.TAG_DUPLEX,
                getDefaultValueWithConstraints(PrintSettings.TAG_DUPLEX)
            )
            updateValueWithConstraints(
                PrintSettings.TAG_FINISHING_SIDE,
                getDefaultValueWithConstraints(PrintSettings.TAG_FINISHING_SIDE)
            )
            updateValueWithConstraints(
                PrintSettings.TAG_STAPLE,
                getDefaultValueWithConstraints(PrintSettings.TAG_STAPLE)
            )
            updateValueWithConstraints(
                PrintSettings.TAG_PUNCH,
                getDefaultValueWithConstraints(PrintSettings.TAG_PUNCH)
            )
            updateValueWithConstraints(
                PrintSettings.TAG_BOOKLET_FINISH,
                getDefaultValueWithConstraints(PrintSettings.TAG_BOOKLET_FINISH)
            )
            updateValueWithConstraints(
                PrintSettings.TAG_BOOKLET_LAYOUT,
                getDefaultValueWithConstraints(PrintSettings.TAG_BOOKLET_LAYOUT)
            )
            updateValueWithConstraints(
                PrintSettings.TAG_IMPOSITION,
                getDefaultValueWithConstraints(PrintSettings.TAG_IMPOSITION)
            )
            updateValueWithConstraints(
                PrintSettings.TAG_IMPOSITION_ORDER,
                getDefaultValueWithConstraints(PrintSettings.TAG_IMPOSITION_ORDER)
            )
            if (_printSettings!!.isBooklet) {
                // OFF to ON
                updateValueWithConstraints(PrintSettings.TAG_DUPLEX, Duplex.SHORT_EDGE.ordinal)
            }
        }

        // Constraint #2 Finishing Side
        if (tag == PrintSettings.TAG_FINISHING_SIDE) {
            val stapleValue = _printSettings!!.staple.ordinal
            if (value == FinishingSide.TOP.ordinal) {
                if (stapleValue == Staple.ONE.ordinal) {
                    if (prevValue == FinishingSide.LEFT.ordinal) {
                        updateValueWithConstraints(PrintSettings.TAG_STAPLE, Staple.ONE_UL.ordinal)
                    } else {
                        updateValueWithConstraints(PrintSettings.TAG_STAPLE, Staple.ONE_UR.ordinal)
                    }
                }
            } else {
                if (stapleValue == Staple.ONE_UL.ordinal || stapleValue == Staple.ONE_UR.ordinal) {
                    updateValueWithConstraints(PrintSettings.TAG_STAPLE, Staple.ONE.ordinal)
                }
            }
        }

        // Constraint #4 Punch
        if (tag == PrintSettings.TAG_PUNCH) {
            if (value != Punch.OFF.ordinal) {
                val outputTrayValue = _printSettings!!.getValue(PrintSettings.TAG_OUTPUT_TRAY)
                if (outputTrayValue == OutputTray.FACEDOWN.ordinal) {
                    updateValueWithConstraints(
                        PrintSettings.TAG_OUTPUT_TRAY,
                        getDefaultValueWithConstraints(PrintSettings.TAG_OUTPUT_TRAY)
                    )
                }
            }
        }

        // Constraint #5 Imposition
        if (tag == PrintSettings.TAG_IMPOSITION) {
            if (value == Imposition.OFF.ordinal) {
                updateValueWithConstraints(
                    PrintSettings.TAG_IMPOSITION_ORDER,
                    ImpositionOrder.L_R.ordinal
                )
            } else if (_printSettings!!.isBooklet) {
                updateValueWithConstraints(
                    PrintSettings.TAG_BOOKLET,
                    getDefaultValueWithConstraints(PrintSettings.TAG_BOOKLET)
                )
                updateValueWithConstraints(
                    PrintSettings.TAG_BOOKLET_FINISH,
                    getDefaultValueWithConstraints(PrintSettings.TAG_BOOKLET_FINISH)
                )
                updateValueWithConstraints(
                    PrintSettings.TAG_BOOKLET_LAYOUT,
                    getDefaultValueWithConstraints(PrintSettings.TAG_BOOKLET_LAYOUT)
                )

                // Ver.2.0.4.7 Start
                // Duplex is set in OFF if imposition is changed to ON when booklet is ON (2017.01.17 RISO Saito)
                updateValueWithConstraints(
                    PrintSettings.TAG_DUPLEX,
                    getDefaultValueWithConstraints(PrintSettings.TAG_DUPLEX)
                )
                // Ver.2.0.4.7 End
            }
            if (value == Imposition.TWO_UP.ordinal) {
                if (prevValue == Imposition.FOUR_UP.ordinal) {
                    val impositionOrderValue = _printSettings!!.impositionOrder.ordinal
                    when (ImpositionOrder.values()[impositionOrderValue]) {
                        ImpositionOrder.TR_B, ImpositionOrder.TR_L -> updateValueWithConstraints(
                            PrintSettings.TAG_IMPOSITION_ORDER,
                            ImpositionOrder.R_L.ordinal
                        )
                        else -> updateValueWithConstraints(
                            PrintSettings.TAG_IMPOSITION_ORDER,
                            ImpositionOrder.L_R.ordinal
                        )
                    }
                } else {
                    updateValueWithConstraints(
                        PrintSettings.TAG_IMPOSITION_ORDER,
                        ImpositionOrder.L_R.ordinal
                    )
                }
            }
            if (value == Imposition.FOUR_UP.ordinal) {
                if (prevValue == Imposition.TWO_UP.ordinal) {
                    val impositionOrderValue = _printSettings!!.impositionOrder.ordinal
                    when (ImpositionOrder.values()[impositionOrderValue]) {
                        ImpositionOrder.R_L -> updateValueWithConstraints(
                            PrintSettings.TAG_IMPOSITION_ORDER,
                            ImpositionOrder.TR_L.ordinal
                        )
                        else -> updateValueWithConstraints(
                            PrintSettings.TAG_IMPOSITION_ORDER,
                            ImpositionOrder.TL_R.ordinal
                        )
                    }
                } else {
                    updateValueWithConstraints(
                        PrintSettings.TAG_IMPOSITION_ORDER,
                        ImpositionOrder.TL_R.ordinal
                    )
                }
            }
        }

        // Constraint #6 BookletFinish
        if (tag == PrintSettings.TAG_BOOKLET_FINISH) {
            if (value != BookletFinish.OFF.ordinal) {
                val outputTrayValue = _printSettings!!.getValue(PrintSettings.TAG_OUTPUT_TRAY)
                if (outputTrayValue != OutputTray.AUTO.ordinal) {
                    updateValueWithConstraints(
                        PrintSettings.TAG_OUTPUT_TRAY,
                        getDefaultValueWithConstraints(PrintSettings.TAG_OUTPUT_TRAY)
                    )
                }
            }
        }
        // Content Print - START
        if ((ContentPrintManager.isBoxRegistrationMode && (cdsPrinter?.isPrinterFTorCEREZONA_S == true || cdsPrinter?.isPrinterGLorOGA == true)) ||
           (!ContentPrintManager.isBoxRegistrationMode && (printer!!.isPrinterFTorCEREZONA_S || printer!!.isPrinterGLorOGA))) {
        // Content Print - END
            /* Specify the input tray and paper size arrays to be used */
            val inputTrayOptions: Array<InputTrayFtGlCerezonaSOga>
            val paperSizeOptions: Array<PaperSize>
            // Content Print - START
            if ((ContentPrintManager.isBoxRegistrationMode && cdsPrinter?.isPrinterFTorCEREZONA_S == true) ||
                (!ContentPrintManager.isBoxRegistrationMode && printer!!.isPrinterFTorCEREZONA_S)) {
            // Content Print - END
                inputTrayOptions = valuesFtCerezonaS()
                paperSizeOptions = valuesDefault()
            } else {
                inputTrayOptions = valuesGlOga()
                paperSizeOptions = PaperSize.valuesGl()
            }
            // Constraint #7 Paper Size - Input Tray (External) for FT or GL series
            if (tag == PrintSettings.TAG_PAPER_SIZE) {
                val inputTrayValue = _printSettings!!.getValue(PrintSettings.TAG_INPUT_TRAY)
                if (paperSizeOptions[value] !== PaperSize.A4 && paperSizeOptions[value] !== PaperSize.B5 && paperSizeOptions[value] !== PaperSize.LETTER && paperSizeOptions[value] !== PaperSize.JUROKUKAI && inputTrayOptions[inputTrayValue] === InputTrayFtGlCerezonaSOga.EXTERNAL_FEEDER) {
                    updateValueWithConstraints(
                        PrintSettings.TAG_INPUT_TRAY,
                        InputTrayFtGlCerezonaSOga.AUTO.ordinal
                    )
                }
            }

            // Constraint #8 Input Tray (External) - Paper Size for FT or GL series
            if (tag == PrintSettings.TAG_INPUT_TRAY) {
                val paperSize = _printSettings!!.paperSize
                if (inputTrayOptions[value] === InputTrayFtGlCerezonaSOga.EXTERNAL_FEEDER) {
                    if (paperSize !== PaperSize.A4 && paperSize !== PaperSize.B5 && paperSize !== PaperSize.LETTER && paperSize !== PaperSize.JUROKUKAI) {
                        updateValueWithConstraints(
                            PrintSettings.TAG_PAPER_SIZE,
                            PaperSize.A4.ordinal
                        )
                    }
                }
            }
        }
    }
    // ================================================================================
    // Set Values
    // ================================================================================
    /**
     * @brief Display the highlighted printer
     */
    private fun updateHighlightedPrinter() {
        if (_printControls == null) {
            return
        }
        val v = _printControls!!.findViewById<View>(R.id.view_id_print_selected_printer)
        val nameTextView = v.findViewById<TextView>(R.id.listValueTextView)
        val ipAddressTextView = v.findViewById<TextView>(R.id.listValueSubTextView)
        val menuContainer = v.findViewById<View>(R.id.menuContainer)
        var height = resources.getDimensionPixelSize(R.dimen.home_menu_height)
        ipAddressTextView.visibility = GONE

        // Content Print - START
        val targetPrinter = if (ContentPrintManager.isBoxRegistrationMode) cdsPrinter else printer
        // Content Print - END
        if (targetPrinter == null) {
            nameTextView.setText(string.ids_lbl_choose_printer)
        } else {
            // Content Print - START
            var printerName = if (ContentPrintManager.isBoxRegistrationMode) cdsPrinter?.model else printer?.name
            // Content Print - END
            if (printerName!!.isEmpty()) {
                printerName = context.resources.getString(string.ids_lbl_no_name)
            }
            nameTextView.text = printerName
            ipAddressTextView.visibility = VISIBLE
            // Content Print - START
            ipAddressTextView.text = if (ContentPrintManager.isBoxRegistrationMode) cdsPrinter?.printerName else printer?.ipAddress
            // Content Print - END
            height = resources.getDimensionPixelSize(R.dimen.printsettings_option_with_sub_height)
        }
        menuContainer.layoutParams.height = height
    }

    /**
     * @brief Sets the visibility of a view
     *
     * @param name Layout tag
     * @param visible Visibility of the view. (VISIBLE, INVISIBLE, or GONE)
     */
    private fun setViewVisible(name: String, visible: Boolean) {
        val view = _mainView!!.findViewWithTag<View>(name)
        val targetView = view.getTag(ID_TAG_TARGET_VIEW) as View
        targetView.visibility = if (visible) VISIBLE else GONE
    }

    /**
     * @brief Hide the settings disabled based on the printer capabilities
     */
    private fun hideDisabledPrintSettings() {
        // Content Print - START
        if (ContentPrintManager.isBoxRegistrationMode) {
            val printerCapabilities = cdsPrinter?.printerCapabilities
            val isStapleAvailable = printerCapabilities == null || printerCapabilities.staple
            setViewVisible(PrintSettings.TAG_STAPLE, isStapleAvailable)
            val isPunchAvailable = printerCapabilities == null || printerCapabilities.finisher0Holes ||
                    printerCapabilities.finisher23Holes || printerCapabilities.finisher24Holes
            setViewVisible(PrintSettings.TAG_PUNCH, isPunchAvailable)
            val isBookletFinishingAvailable =
                printerCapabilities == null || printerCapabilities.booklet
            setViewVisible(PrintSettings.TAG_BOOKLET_FINISH, isBookletFinishingAvailable)
        } else {
            val printer = printer
            val isStapleAvailable = printer == null || printer.config!!.isStaplerAvailable
            setViewVisible(PrintSettings.TAG_STAPLE, isStapleAvailable)
            val isPunchAvailable = printer == null || printer.config!!.isPunchAvailable
            setViewVisible(PrintSettings.TAG_PUNCH, isPunchAvailable)
            val isBookletFinishingAvailable =
                printer == null || printer.config!!.isBookletFinishingAvailable
            setViewVisible(PrintSettings.TAG_BOOKLET_FINISH, isBookletFinishingAvailable)
        }
        // Content Print - END
        setViewVisible(PrintSettings.TAG_ORIENTATION, !AppConstants.USE_PDF_ORIENTATION)
    }

    /**
     * @brief Hides options disabled based on printer capabilities
     * @note Currently only supported by Output Tray, Punch, and Input Tray
     *
     * @param name Print settings tag name
     * @param value Value of the option to be checked
     *
     * @retval true Printer option should be displayed.
     * @retval false Printer option should not be displayed.
     */
    private fun shouldDisplayOptionFromPrinter(name: String, value: Int): Boolean {
        // Content Print - START
        if ((ContentPrintManager.isBoxRegistrationMode && cdsPrinter != null) ||
            (!ContentPrintManager.isBoxRegistrationMode && printer != null)) {
            if (name == PrintSettings.TAG_PAPER_TYPE) {
                if (ContentPrintManager.isBoxRegistrationMode && PaperType.values()[value] == PaperType.LW_PAPER) {
                    return cdsPrinter!!.printerCapabilities?.paperTypeLightweight == true
                }
            }
        // Content Print - END
            if (name == PrintSettings.TAG_OUTPUT_TRAY) {
                return when (OutputTray.values()[value]) {
                    OutputTray.AUTO -> true
                    // Content Print - START
                    OutputTray.FACEDOWN ->                         //ver.2.0.1.2 We always display "Facedown" in the Output tray list(20160707 RISO Saito)
                        //return getPrinter().getConfig().isTrayFaceDownAvailable();
                        if (ContentPrintManager.isBoxRegistrationMode)
                            cdsPrinter!!.printerCapabilities?.outputTrayFacedown == true else true
                    OutputTray.TOP -> if (ContentPrintManager.isBoxRegistrationMode)
                        cdsPrinter!!.printerCapabilities?.outputTrayTop == true else printer!!.config!!.isTrayTopAvailable
                    OutputTray.STACKING -> if (ContentPrintManager.isBoxRegistrationMode)
                        cdsPrinter!!.printerCapabilities?.outputTrayStacking == true else printer!!.config!!.isTrayStackAvailable
                    // Content Print - END
                }
            }
            if (name == PrintSettings.TAG_PUNCH) {
                return when (Punch.values()[value]) {
                    // Content Print - START
                    Punch.OFF -> true
                    Punch.HOLES_2 -> if (ContentPrintManager.isBoxRegistrationMode)
                        cdsPrinter!!.printerCapabilities?.finisher0Holes == true else true
                    Punch.HOLES_3 -> if (ContentPrintManager.isBoxRegistrationMode)
                        cdsPrinter!!.printerCapabilities?.finisher23Holes == true else printer!!.config!!.isPunch3Available
                    Punch.HOLES_4 -> if (ContentPrintManager.isBoxRegistrationMode)
                        cdsPrinter!!.printerCapabilities?.finisher24Holes == true else printer!!.config!!.isPunch4Available
                    // Content Print - END
                }
            }
            if (name == PrintSettings.TAG_INPUT_TRAY) {
                // Content Print - START
                if ((ContentPrintManager.isBoxRegistrationMode && cdsPrinter?.isPrinterFTorCEREZONA_S == true) ||
                    (!ContentPrintManager.isBoxRegistrationMode && printer!!.isPrinterFTorCEREZONA_S)) {
                // Content Print - END
                    return when (valuesFtCerezonaS()[value]) {
                        // Content Print - START
                        InputTrayFtGlCerezonaSOga.AUTO -> true
                        InputTrayFtGlCerezonaSOga.STANDARD -> if (ContentPrintManager.isBoxRegistrationMode)
                            cdsPrinter!!.printerCapabilities?.inputTrayStandard == true else true
                        InputTrayFtGlCerezonaSOga.TRAY1 -> if (ContentPrintManager.isBoxRegistrationMode)
                            cdsPrinter!!.printerCapabilities?.inputTrayTray1 == true else true
                        InputTrayFtGlCerezonaSOga.TRAY2 -> if (ContentPrintManager.isBoxRegistrationMode)
                            cdsPrinter!!.printerCapabilities?.inputTrayTray2 == true else true
                        InputTrayFtGlCerezonaSOga.TRAY3 -> if (ContentPrintManager.isBoxRegistrationMode)
                            cdsPrinter!!.printerCapabilities?.inputTrayTray3 == true else false
                        InputTrayFtGlCerezonaSOga.EXTERNAL_FEEDER -> if (ContentPrintManager.isBoxRegistrationMode)
                            cdsPrinter!!.printerCapabilities?.inputTrayExternal == true else printer!!.config!!.isExternalFeederAvailable
                        // Content Print - END

                    }
                }
                // Content Print - START
                if ((ContentPrintManager.isBoxRegistrationMode && cdsPrinter?.isPrinterGLorOGA == true) ||
                    (!ContentPrintManager.isBoxRegistrationMode && printer!!.isPrinterGLorOGA)) {
                // Content Print - END
                    return when (valuesGlOga()[value]) {
                        // Content Print - START
                        InputTrayFtGlCerezonaSOga.AUTO -> true
                        InputTrayFtGlCerezonaSOga.STANDARD -> if (ContentPrintManager.isBoxRegistrationMode)
                            cdsPrinter!!.printerCapabilities?.inputTrayStandard == true else true
                        InputTrayFtGlCerezonaSOga.TRAY1 -> if (ContentPrintManager.isBoxRegistrationMode)
                            cdsPrinter!!.printerCapabilities?.inputTrayTray1 == true else true
                        InputTrayFtGlCerezonaSOga.TRAY2 -> if (ContentPrintManager.isBoxRegistrationMode)
                            cdsPrinter!!.printerCapabilities?.inputTrayTray2 == true else true
                        InputTrayFtGlCerezonaSOga.TRAY3 -> if (ContentPrintManager.isBoxRegistrationMode)
                            cdsPrinter!!.printerCapabilities?.inputTrayTray3 == true else printer!!.isPrinterGLorOGA
                        InputTrayFtGlCerezonaSOga.EXTERNAL_FEEDER -> if (ContentPrintManager.isBoxRegistrationMode)
                            cdsPrinter!!.printerCapabilities?.inputTrayExternal == true else printer!!.config!!.isExternalFeederAvailable
                        // Content Print - END
                    }
                }
            }
        }
        return true
    }

    /**
     * @brief Updates the value and applies the value and view constraints
     *
     * @param tag Print settings tag name
     * @param newValue New value
     */
    private fun updateValue(tag: String, newValue: Int) {
        val prevValue = _printSettings!!.getValue(tag)
        _printSettings!!.setValue(tag, newValue)
        applyValueConstraints(tag, prevValue)
        applyViewConstraints(tag)
    }

    /**
     * @brief Updates the value and applies the view constraints
     *
     * @param tag Print settings tag name
     */
    private fun updateDisplayedValue(tag: String) {
        val value = _printSettings!!.getValue(tag)
        val view = _mainView!!.findViewWithTag<View>(tag)
        if (view != null) {
            if (view is LinearLayout) {
                val textView = view.findViewById<TextView>(R.id.listValueTextView)
                var text = value.toString()
                val setting = view.getTag(ID_TAG_SETTING) as Setting
                val options = getOptionsStrings(setting.options)
                if (value >= 0 && value < options.size) {
                    text = options[value].toString()
                }
                textView.text = text
            }
            if (view is EditText) {
                view.setText(String.format(Locale.getDefault(), "%d", value))
            }
            if (view is Switch) {
                view.setOnCheckedChangeListener(null)
                view.isChecked = value == 1
                view.setOnCheckedChangeListener(this)
            }
        }
        applyViewConstraints(tag)
    }

    /**
     * @brief Sets the value changed listener
     *
     * @param listener PrintSettingsViewInterface listener
     */
    fun setValueChangedListener(listener: PrintSettingsViewInterface) {
        _listener = listener
    }

    /**
     * @brief Displays the print controls.
     *
     * @param showPrintControls Should show the print controls or not.
     */
    fun setShowPrintControls(showPrintControls: Boolean) {
        if (showPrintControls) {
            _printControls!!.findViewById<View>(R.id.view_id_print_header).visibility =
                VISIBLE
            val selectedPrinter =
                _printControls!!.findViewById<View>(R.id.view_id_print_selected_printer)
            selectedPrinter.findViewById<View>(R.id.disclosureIndicator).visibility =
                VISIBLE
            selectedPrinter.isClickable = true
            val authenticationGroup = _mainView!!.findViewWithTag<View>(KEY_TAG_AUTHENTICATION)
            authenticationGroup.visibility = VISIBLE
            val targetGroup = authenticationGroup.getTag(ID_COLLAPSE_TARGET_GROUP) as View
            targetGroup.visibility = VISIBLE
        } else {
            _printControls!!.findViewById<View>(R.id.view_id_print_header).visibility =
                GONE
            val selectedPrinter =
                _printControls!!.findViewById<View>(R.id.view_id_print_selected_printer)
            selectedPrinter.findViewById<View>(R.id.disclosureIndicator).visibility =
                GONE
            selectedPrinter.isClickable = false
            val authenticationGroup = _mainView!!.findViewWithTag<View>(KEY_TAG_AUTHENTICATION)
            authenticationGroup.visibility = GONE
            val targetGroup = authenticationGroup.getTag(ID_COLLAPSE_TARGET_GROUP) as View
            targetGroup.visibility = GONE
        }
    }

    /**
     * @brief Sets the printer id and print settings without applying constraints.
     *
     * @param printerId ID of the printer
     * @param printSettings Print Settings to be applied
     */
    fun setInitialValues(printerId: Int, printSettings: PrintSettings) {
        _printerId = printerId
        setPrintSettings(printSettings)
        setPrinterId(printerId)
    }

    /**
     * @brief Sets the selected printer. UI is also updated
     *
     * @param printerId ID of the printer
     */
    private fun setPrinterId(printerId: Int) {
        _printerId = printerId
        updateHighlightedPrinter()
        hideDisabledPrintSettings()
    }

    /**
     * @brief Creates a copy of the print settings and applies it in the view
     *
     * @param printSettings Print Settings to be applied
     */
    private fun setPrintSettings(printSettings: PrintSettings) {
        var initializeViews = true
        var clearAuthValues = false
        if (_printSettings != null) {
            if (!_printSettings!!.settingMapKey.equals(
                    printSettings.settingMapKey,
                    ignoreCase = true
                )
            ) {
                _printSettingsLayout!!.removeAllViews()
                clearAuthValues = true
            } else {
                initializeViews = false
            }
        }
        _printSettings = PrintSettings(printSettings)
        if (initializeViews) {
            initializePrintSettingsControls()
            initializeAuthenticationSettingsView()
            initializeAuthenticationValues(clearAuthValues)
        }
        val keySet: Set<String> = PrintSettings.sSettingsMaps!![printSettings.settingMapKey]!!.keys
        for (key in keySet) {
            updateDisplayedValue(key)
        }
    }

    /**
     * @brief Get printer specified by printerId
     *
     * @param printerId ID of the Printer
     *
     * @return Printer object instance
     * @retval null Printer is not on the list
     */
    private fun getPrinterFromList(printerId: Int): Printer? {
        for (p in _printersList!!) {
            if (p!!.id == printerId) {
                return p
            }
        }
        return null
    }

    /**
     * @brief Get the current printer highlighted
     *
     * @return Printer object instance
     * @retval null Printer is not on the list
     */
    val printer: Printer?
        get() = getPrinterFromList(_printerId)

    // Content Print - START
    private fun getPrinterFromList(): ContentPrintPrinter? {
        if (ContentPrintManager.isBoxRegistrationMode) {
            if (ContentPrintManager.selectedPrinter != null) {
                return ContentPrintManager.selectedPrinter
            } else if (ContentPrintManager.printerList.isNotEmpty()) {
                return ContentPrintManager.printerList.first()
            }
        }
        return null
    }

    val cdsPrinter: ContentPrintPrinter?
        get() = getPrinterFromList()
    // Content Print - END

    // ================================================================================
    // Printer controls functions
    // ================================================================================
    /**
     * @brief Create the printer controls
     */
    private fun initializePrinterControls() {
        _printControls = LinearLayout(context)
        _printControls!!.orientation = LinearLayout.VERTICAL

        // Create Header
        val li = LayoutInflater.from(context)
        // Content Print - START
        val header = li.inflate(
            if (ContentPrintManager.isBoxRegistrationMode) R.layout.printsettings_registertobox else R.layout.printsettings_print,
            null
        ) as LinearLayout
        // Content Print - END
        var height = resources.getDimensionPixelSize(R.dimen.home_menu_height)
        var params = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, height)
        params.gravity = Gravity.CENTER
        header.layoutParams = params
        header.id = R.id.view_id_print_header
        header.setOnClickListener(this)
        _printControls!!.addView(header)

        // Create disclosure for item
        var view = LayoutInflater.from(context)
            .inflate(R.layout.printsettings_value_disclosure_with_sub, null)
        view.isActivated = true
        val viewWidth = resources.getDimensionPixelSize(R.dimen.printsettings_list_value_width)
        params = LinearLayout.LayoutParams(viewWidth, LayoutParams.MATCH_PARENT, 0.0f)
        params.gravity = Gravity.CENTER_VERTICAL
        view.layoutParams = params

        // Create item
        val selectedPrinter =
            createItem(resources.getString(string.ids_lbl_printer), false, -1, false, view)
        selectedPrinter.id = R.id.view_id_print_selected_printer
        selectedPrinter.tag = KEY_TAG_PRINTER
        selectedPrinter.setOnClickListener(this)
        _printControls!!.addView(selectedPrinter)
        params = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        _printControls!!.layoutParams = params
        _mainView!!.addView(_printControls, 0)
        view = View(context)
        view.setBackgroundResource(R.color.theme_light_1)
        height = resources.getDimensionPixelSize(R.dimen.separator_size)
        params = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, height)
        view.layoutParams = params
        _mainView!!.addView(view, 1)
    }
    // ================================================================================
    // Print settings controls functions
    // ================================================================================
    /**
     * @brief Get the options strings.
     * The strings ids are updated based on constraints
     *
     * @param options Options list
     *
     * @return Option strings
     */
    private fun getOptionsStrings(options: List<Option>): Array<Any> {
        val optionsStrings = ArrayList<String>()
        for (option in options) {
            val value = option.textContent
            val id = AppUtils.getResourceId(value, string::class.java, -1)
            if (id != -1) {
                optionsStrings.add(resources.getString(id))
            } else {
                optionsStrings.add(id.toString())
            }
        }
        return optionsStrings.toTypedArray()
    }

    /**
     * @brief Add a Print Setting View to a Layout
     *
     * @param ll Layout to add the Print Setting
     * @param setting Setting information to be added
     * @param withSeparator Should add a separator
     */
    private fun addSettingsItemView(ll: LinearLayout, setting: Setting, withSeparator: Boolean) {
        val name = setting.getAttributeValue(XmlNode.ATTR_NAME)
        val icon = setting.getAttributeValue(XmlNode.ATTR_ICON)
        val text = setting.getAttributeValue(XmlNode.ATTR_TEXT)
        val type = setting.getAttributeValue(XmlNode.ATTR_TYPE)
        var titleText = ""
        val titleId = AppUtils.getResourceId(text, string::class.java, -1)
        if (titleId != -1) {
            titleText = resources.getString(titleId)
        }
        val iconId = AppUtils.getResourceId(icon, drawable::class.java, -1)
        val view = createControlView(type, name)
        val item = createItem(titleText, true, iconId, withSeparator, view)
        item.id = R.id.view_id_show_subview_container
        if (type.equals(Setting.ATTR_VAL_LIST, ignoreCase = true)) {
            view!!.id = R.id.view_id_disclosure
            item.tag = name
            item.setTag(ID_TAG_ICON, icon)
            item.setTag(ID_TAG_TEXT, text)
            item.setTag(ID_TAG_SETTING, setting)
            item.setTag(ID_TAG_TARGET_VIEW, item)
            item.setOnClickListener(this)
        } else {
            view!!.setTag(ID_TAG_TARGET_VIEW, item)
        }
        ll.addView(item)
    }

    /**
     * @brief Add a Print Setting Title to the Print Settings Title Layout
     *
     * @param group Information about the group to be added
     */
    private fun addSettingsTitleView(group: Group) {
        val nameStr = group.getAttributeValue(XmlNode.ATTR_NAME)
        val textStr = group.getAttributeValue(XmlNode.ATTR_TEXT)
        val itemsGroup = LinearLayout(context)
        itemsGroup.orientation = LinearLayout.VERTICAL
        for (setting in group.settings) {
            val index = group.settings.indexOf(setting)
            val size = group.settings.size
            addSettingsItemView(itemsGroup, setting, index != size - 1)
        }

        // Create Header
        var titleText = ""
        val id = AppUtils.getResourceId(textStr, string::class.java, -1)
        if (id != -1) {
            titleText = resources.getString(id)
        }
        val title =
            createTitle(titleText, true, drawable.selector_printsettings_collapsible,
                showBackButton = false
            )
        title.id = R.id.view_id_collapse_container
        title.setTag(ID_COLLAPSE_TARGET_GROUP, itemsGroup)
        title.tag = nameStr
        title.setOnClickListener(this)
        expandControl(title, false) // initially expand
        var params = title.layoutParams as LinearLayout.LayoutParams
        val marginTop = resources.getDimensionPixelSize(R.dimen.separator_size)
        params.setMargins(0, marginTop, 0, 0)
        title.layoutParams = params
        _printSettingsTitles!!.add(title) // Add to controls list
        _printSettingsLayout!!.addView(title)
        params = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        itemsGroup.layoutParams = params
        _printSettingsLayout!!.addView(itemsGroup)
    }

    /**
     * @brief Create the Print Setting Controls
     */
    private fun initializePrintSettingsControls() {
        _printSettingsTitles = ArrayList()
        if (PrintSettings.sGroupListMap!![_printSettings!!.settingMapKey] != null) {
            for (group in PrintSettings.sGroupListMap!![_printSettings!!.settingMapKey]!!) {
                addSettingsTitleView(group)
            }
        }
    }
    // ================================================================================
    // Authentication controls functions
    // ================================================================================
    /**
     * @brief Add an authentication view sub item to a layout
     *
     * @param ll Layout to add the Authentication view
     * @param titleText Text Title of the View
     * @param view Control view to be added
     * @param tag Item tag to be used for identification
     * @param withSeparator Should add separator view
     */
    private fun addAuthenticationItemView(
        ll: LinearLayout,
        titleText: String,
        view: View,
        tag: String,
        withSeparator: Boolean
    ) {
        val item =
            createItem(titleText, true, drawable.temp_img_printsettings_null, withSeparator, view)
        item.id = R.id.view_id_show_subview_container
        item.tag = tag
        ll.addView(item)
    }

    /**
     * @brief Create the authentication setting view
     */
    private fun initializeAuthenticationSettingsView() {
        val itemsGroup = LinearLayout(context)
        itemsGroup.orientation = LinearLayout.VERTICAL
        var params =
            LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.0f)
        params.gravity = Gravity.CENTER_VERTICAL
        val margin = resources.getDimensionPixelSize(R.dimen.printsettings_icon_setting_padding)
        params.leftMargin = margin
        params.rightMargin = margin
        val switchView =
            LayoutInflater.from(context).inflate(R.layout.printsettings_switch, null) as Switch
        switchView.layoutParams = params
        switchView.id = R.id.view_id_secure_print_switch
        switchView.setOnCheckedChangeListener(this)
        switchView.textOff =
            resources.getString(string.ids_lbl_off).uppercase(Locale.getDefault())
        switchView.textOn =
            resources.getString(string.ids_lbl_on).uppercase(Locale.getDefault())
        var titleText = resources.getString(string.ids_lbl_secure_print)
        addAuthenticationItemView(itemsGroup, titleText, switchView, KEY_TAG_SECURE_PRINT, true)
        params =
            LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.0f)
        params.gravity = Gravity.CENTER_VERTICAL
        params.width = resources.getDimensionPixelSize(R.dimen.printsettings_input_pincode_width)
        params.leftMargin = margin
        params.rightMargin = margin
        val editText = LayoutInflater.from(context)
            .inflate(R.layout.printsettings_input_edittext, null) as EditText
        editText.id = R.id.view_id_pin_code_edit_text
        editText.layoutParams = params
        editText.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        // RM#918 for chromebook, this is also needed to make virtual keyboard numeric only
        editText.setRawInputType(InputType.TYPE_CLASS_NUMBER)
        editText.filters = arrayOf<InputFilter>(
            LengthFilter(AppConstants.CONST_PIN_CODE_LIMIT)
        )
        editText.addTextChangedListener(PinCodeTextWatcher())
        editText.setOnEditorActionListener(this)
        titleText = resources.getString(string.ids_lbl_pin_code)
        addAuthenticationItemView(itemsGroup, titleText, editText, KEY_TAG_PIN_CODE, false)

        // Create Header
        titleText = resources.getString(string.ids_lbl_authentication)
        val title =
            createTitle(titleText, true, drawable.selector_printsettings_collapsible,
                showBackButton = false
            )
        title.id = R.id.view_id_collapse_container
        title.setTag(ID_COLLAPSE_TARGET_GROUP, itemsGroup)
        title.tag = KEY_TAG_AUTHENTICATION
        title.setOnClickListener(this)
        expandControl(title, false) // initially expand
        params = title.layoutParams as LinearLayout.LayoutParams
        val marginTop = resources.getDimensionPixelSize(R.dimen.separator_size)
        params.setMargins(0, marginTop, 0, 0)
        title.layoutParams = params
        _printSettingsTitles!!.add(title) // Add to controls list
        _printSettingsLayout!!.addView(title)
        params = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        itemsGroup.layoutParams = params
        _printSettingsLayout!!.addView(itemsGroup)
    }

    /**
     * @brief Initialize the authentication setting values
     *
     * @param shouldClear true if the authentication values must be cleared
     */
    private fun initializeAuthenticationValues(shouldClear: Boolean) {
        val pinCodeEditText = _mainView!!.findViewById<EditText>(R.id.view_id_pin_code_edit_text)
        val securePrintSwitch = _mainView!!.findViewById<Switch>(R.id.view_id_secure_print_switch)
        if (shouldClear) {
            pinCodeEditText.setText("")
            securePrintSwitch.isChecked = false
            setSecurePrintEnabled(false)
        } else {
            // Fix for BTS23939 (retain if DefaultPrintSettings is re-opened, shared prefs is always cleared when app is opened)
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val isSecurePrint = prefs.getBoolean(
                AppConstants.PREF_KEY_AUTH_SECURE_PRINT,
                AppConstants.PREF_DEFAULT_AUTH_SECURE_PRINT
            )
            val pin = prefs.getString(
                AppConstants.PREF_KEY_AUTH_PIN_CODE,
                AppConstants.PREF_DEFAULT_AUTH_PIN_CODE
            )
            if (isSecurePrint) {
                pinCodeEditText.setText(pin)
            }
            securePrintSwitch.isChecked = isSecurePrint
            setSecurePrintEnabled(isSecurePrint)
        }
    }
    // ================================================================================
    // Sub-view value Controls
    // ================================================================================
    /**
     * @brief Animates display of sub-view
     */
    private fun animateDisplaySubview() {
        val width = width
        val duration = resources.getInteger(android.R.integer.config_shortAnimTime)
        val interpolator = AccelerateInterpolator()
        val animIn = TranslateAnimation(width.toFloat(), 0F, 0F, 0F)
        animIn.duration = duration.toLong()
        animIn.interpolator = interpolator
        animIn.fillAfter = true
        _subView!!.startAnimation(animIn)
        val animOut = TranslateAnimation(0F, (-width).toFloat(), 0F, 0F)
        animOut.duration = duration.toLong()
        animOut.interpolator = interpolator
        animOut.fillAfter = true
        animOut.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                val newMessage = Message.obtain(_handler, MSG_SLIDE_IN)
                _handler!!.sendMessage(newMessage)
            }
        })
        _mainView!!.startAnimation(animOut)
    }

    /**
     * @brief Animates dismiss of sub-view
     */
    private fun animateDismissSubview() {
        val width = width
        val duration = resources.getInteger(android.R.integer.config_shortAnimTime)
        val interpolator = AccelerateInterpolator()
        val animIn = TranslateAnimation((-width).toFloat(), 0F, 0F, 0F)
        animIn.duration = duration.toLong()
        animIn.interpolator = interpolator
        animIn.fillAfter = true
        _mainView!!.startAnimation(animIn)
        val animOut = TranslateAnimation(0F, width.toFloat(), 0F, 0F)
        animOut.duration = duration.toLong()
        animOut.interpolator = interpolator
        animOut.fillAfter = true
        animOut.setAnimationListener(object : AnimationListener {
            /** {@inheritDoc}  */
            override fun onAnimationStart(animation: Animation) {}

            /** {@inheritDoc}  */
            override fun onAnimationRepeat(animation: Animation) {}

            /** {@inheritDoc}  */
            override fun onAnimationEnd(animation: Animation) {
                val newMessage = Message.obtain(_handler, MSG_SLIDE_OUT)
                _handler!!.sendMessage(newMessage)
            }
        })
        _subView!!.startAnimation(animOut)
    }

    /**
     * @brief Add sub-view options list
     *
     * @param str Main text
     * @param value Value
     * @param tagValue Tag value for identification
     * @param withSeparator View has separator
     * @param itemId Item ID
     */
    private fun addSubviewOptionsList(
        str: String,
        value: Int,
        tagValue: Int,
        withSeparator: Boolean,
        itemId: Int
    ) {
        addSubviewOptionsList(str, null, value, tagValue, withSeparator, itemId, -1)
    }

    /**
     * @brief Add sub-view options list
     *
     * @param str Main text
     * @param sub Sub text
     * @param value Value
     * @param tagValue Tag value for identification
     * @param withSeparator View has separator
     * @param itemId Item ID
     * @param iconId Icon resource ID
     */
    private fun addSubviewOptionsList(
        str: String,
        sub: String?,
        value: Int,
        tagValue: Int,
        withSeparator: Boolean,
        itemId: Int,
        iconId: Int
    ) {

        // Add disclosure
        val li = LayoutInflater.from(context)
        val view = li.inflate(R.layout.printsettings_radiobutton, null)
        view.id = R.id.view_id_subview_status
        if (value == tagValue) {
            view.isSelected = true
        }
        val size = resources.getDimensionPixelSize(R.dimen.printsettings_radiobutton_size)
        val params = LinearLayout.LayoutParams(size, size, 0.0f)
        params.gravity = Gravity.CENTER_VERTICAL
        val margin = resources.getDimensionPixelSize(R.dimen.printsettings_icon_setting_padding)
        params.leftMargin = margin
        params.rightMargin = margin
        view.layoutParams = params

        // TODO: iconID
        val item = createItem(str, sub, true, iconId, withSeparator, view)
        item.id = itemId
        item.tag = tagValue
        item.setOnClickListener(this)
        _subOptionsLayout!!.addView(item)
    }

    /**
     * @brief Add sub-view options title
     *
     * @param str Main text
     * @param showIcon Should show icon
     * @param iconId Icon resource ID
     */
    private fun addSubviewOptionsTitle(str: String, showIcon: Boolean, iconId: Int) {
        val title = createTitle(str, showIcon, iconId,
            showBackButton = true
        )
        title.id = R.id.view_id_hide_subview_container
        title.setOnClickListener(this)
        _subView!!.addView(title)
    }

    /**
     * @brief Create sub-view upon selecting a print setting
     *
     * @param v View to create subview from
     */
    private fun createSubview(v: View) {
        _subView!!.tag = v.tag
        if (v.tag.toString() == KEY_TAG_PRINTER) {
            val title = resources.getString(string.ids_lbl_printers)
            addSubviewOptionsTitle(title, false, -1)
            // Content Print - START
            if (ContentPrintManager.isBoxRegistrationMode) {
                for (i in ContentPrintManager.printerList.indices) {
                    val printerList = ContentPrintManager.printerList
                    val printer = printerList[i]
                    val showSeparator = i != ContentPrintManager.printerList.size - 1
                    var printerName = printer.printerName
                    if (printerName!!.isEmpty()) {
                        printerName = context.resources.getString(string.ids_lbl_no_name)
                    }
                    val selectedIndex = printerList.indexOf(ContentPrintManager.selectedPrinter)
                    addSubviewOptionsList(
                        printer.model ?: context.resources.getString(string.ids_lbl_no_name),
                        printerName,
                        selectedIndex,
                        i,
                        showSeparator,
                        R.id.view_id_subview_printer_item,
                        drawable.img_btn_printer_status_offline
                    )
                }
            } else {
                for (i in _printersList!!.indices) {
                    val printer = _printersList!![i]
                    val showSeparator = i != _printersList!!.size - 1
                    var printerName = printer!!.name
                    if (printerName!!.isEmpty()) {
                        printerName = context.resources.getString(string.ids_lbl_no_name)
                    }
                    addSubviewOptionsList(
                        printerName,
                        printer.ipAddress,
                        _printerId,
                        printer.id,
                        showSeparator,
                        R.id.view_id_subview_printer_item,
                        drawable.img_btn_printer_status_offline
                    )
                }
            }
            // Content Print - END
        } else {
            _subView!!.setTag(ID_TAG_TEXT, v.getTag(ID_TAG_TEXT))
            _subView!!.setTag(ID_TAG_ICON, v.getTag(ID_TAG_ICON))
            val setting = v.getTag(ID_TAG_SETTING) as Setting
            val options = getOptionsStrings(setting.options)
            _subView!!.setTag(ID_TAG_SUB_OPTIONS, options)
            val name = v.tag as String
            val text = v.getTag(ID_TAG_TEXT) as String
            val icon = v.getTag(ID_TAG_ICON) as String
            var titleText = ""
            val titleId = AppUtils.getResourceId(text, string::class.java, -1)
            if (titleId != -1) {
                titleText = resources.getString(titleId)
            }
            val iconId = AppUtils.getResourceId(icon, drawable::class.java, -1)
            addSubviewOptionsTitle(titleText, true, iconId)
            val value = _printSettings!!.getValue(name)
            var lastIdx = -1
            for (i in options.indices) {
                if (shouldDisplayOptionFromConstraints(name, i) && shouldDisplayOptionFromPrinter(
                        name,
                        i
                    )
                ) {
                    addSubviewOptionsList(
                        options[i].toString(),
                        value,
                        i,
                        true,
                        R.id.view_id_subview_option_item
                    )
                    lastIdx = i
                }
            }

            // hide the separator of the last item added
            if (_subOptionsLayout!!.findViewWithTag<View?>(lastIdx) != null) {
                val container = _subOptionsLayout!!.findViewWithTag<View>(lastIdx)
                container.findViewById<View>(R.id.menuSeparator).visibility = GONE
            }
        }
    }

    /**
     * @brief Display the options sub-view
     *
     * @param v View to create sub-view from
     * @param animate Should animate display
     */
    private fun displayOptionsSubview(v: View, animate: Boolean) {
        if (_subOptionsLayout == null) {
            _subOptionsLayout = LinearLayout(context)
            _subOptionsLayout!!.orientation = LinearLayout.VERTICAL
        } else {
            _subOptionsLayout!!.removeAllViews()
        }
        if (_subScrollView == null) {
            _subScrollView = ScrollView(context)
        }
        if (_subScrollView!!.childCount == 0) {
            _subScrollView!!.addView(
                _subOptionsLayout,
                ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            )
        }
        if (_subView == null) {
            _subView = LinearLayout(context)
            _subView!!.orientation = LinearLayout.VERTICAL
        } else {
            _subView!!.removeAllViews()
        }
        createSubview(v)
        _subView!!.addView(
            _subScrollView,
            ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        )
        if (_subView!!.parent == null) {
            addView(
                _subView,
                ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            )
        }
        _mainView!!.isEnabled = false
        _subView!!.isEnabled = true
        if (animate) {
            animateDisplaySubview()
        } else {
            _mainView!!.visibility = GONE
        }

        // AppUtils.changeChildrenFont(mSubView, SmartDeviceApp.getAppFont());
    }

    /**
     * @brief Dismiss the options sub-view
     *
     * @param animate Should animate dismiss
     */
    private fun dismissOptionsSubview(animate: Boolean) {
        _mainView!!.isEnabled = true
        _mainView!!.visibility = VISIBLE
        _subView!!.isEnabled = false
        if (animate) {
            animateDismissSubview()
        } else {
            removeView(_subView)
        }
    }

    /**
     * @brief Sub-view option item is clicked
     *
     * @param v View clicked
     */
    private fun subviewOptionsItemClicked(v: View) {
        val id = v.tag as Int
        val prevValue = _printSettings!!.getValue((_subView!!.tag as String))
        if (v.id == R.id.view_id_subview_option_item) {
            if (id == prevValue) {
                // If clicked option is same as currently set, no need to update
                return
            }
            updateValue(_subView!!.tag as String, id)
            _listener.onPrintSettingsValueChanged(_printSettings)
            // Update UI
            updateDisplayedValue(_subView!!.tag as String)
            val options = _subView!!.getTag(ID_TAG_SUB_OPTIONS) as Array<*>
            for (i in options.indices) {
                val view = _subView!!.findViewWithTag<View>(i)
                // Some views may be hidden
                if (view != null) {
                    val subView = view.findViewById<View>(R.id.view_id_subview_status)
                    subView.isSelected = id == i
                }
            }
        } else if (v.id == R.id.view_id_subview_printer_item) {
            // Content Print - START
            if (ContentPrintManager.isBoxRegistrationMode) {
                val printerList = ContentPrintManager.printerList
                ContentPrintManager.selectedPrinter = printerList[id]

                for (i in printerList.indices) {
                    val printer = printerList[i]
                    val view = _subView!!.findViewWithTag<View>(i)
                    val subView = view.findViewById<View>(R.id.view_id_subview_status)
                    subView.isSelected = ContentPrintManager.selectedPrinter === printer
                }

                updateHighlightedPrinter()
                hideDisabledPrintSettings()
            } else if (_printerId != id) {
            // Content Print - END
                // TODO: get new printer settings
                setPrintSettings(
                    PrintSettings(
                        id,
                        PrinterManager.getInstance(SmartDeviceApp.appContext!!)!!
                            .getPrinterType(id)!!
                    )
                )
                setPrinterId(id)
                _listener.onPrinterIdSelectedChanged(_printerId)
                _listener.onPrintSettingsValueChanged(_printSettings)
                for (i in _printersList!!.indices) {
                    val printer = _printersList!![i]
                    val view = _subView!!.findViewWithTag<View>(printer!!.id)
                    val subView = view.findViewById<View>(R.id.view_id_subview_status)
                    subView.isSelected = _printerId == printer.id
                }
            }
        }
    }

    /**
     * @brief Execute Print
     */
    private fun executePrint() {
        _listener.onPrint(getPrinterFromList(_printerId), _printSettings)
    }
    // ================================================================================
    // Convenience methods
    // ================================================================================
    /**
     * @brief Create title
     *
     * @param text Main text
     * @param showIcon Show icon
     * @param iconId Icon resource ID
     * @param showBackButton Show back button
     */
    private fun createTitle(
        text: String,
        showIcon: Boolean,
        iconId: Int,
        showBackButton: Boolean
    ): LinearLayout {
        val li = LayoutInflater.from(context)
        val title = li.inflate(R.layout.printsettings_container_title, null) as LinearLayout
        val textView = title.findViewById<TextView>(R.id.menuTextView)
        textView.text = text
        val height = resources.getDimensionPixelSize(R.dimen.home_menu_height)
        val params = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, height)
        title.layoutParams = params

        // Set icon
        var imgView = title.findViewById<ImageView>(R.id.menuIcon)
        if (showIcon) {
            imgView.visibility = VISIBLE
        }
        if (iconId != -1) {
            imgView.setImageResource(iconId)
        }
        imgView = title.findViewById(R.id.returnButtonIndicator)
        if (showBackButton) {
            imgView.visibility = VISIBLE
        }
        return title
    }

    /**
     * @brief Create item with empty sub-text
     *
     * @param text Main text
     * @param showIcon Show icon
     * @param iconId Icon resource ID
     * @param showSeparator Show showSeparator
     * @param view Control view to be added
     */
    private fun createItem(
        text: String,
        showIcon: Boolean,
        iconId: Int,
        showSeparator: Boolean,
        view: View?
    ): LinearLayout {
        return createItem(text, null, showIcon, iconId, showSeparator, view)
    }

    /**
     * @brief Create item
     *
     * @param text Main menu text
     * @param subText Sub-text
     * @param showIcon Show icon
     * @param iconId Icon resource ID
     * @param showSeparator Show showSeparator
     * @param view Control view to be added
     */
    private fun createItem(
        text: String,
        subText: String?,
        showIcon: Boolean,
        iconId: Int,
        showSeparator: Boolean,
        view: View?
    ): LinearLayout {
        val li = LayoutInflater.from(context)
        var layoutId = R.layout.printsettings_container_item
        if (subText != null) {
            layoutId = R.layout.printsettings_container_item_with_sub
        }
        val item = li.inflate(layoutId, null) as LinearLayout
        item.isActivated = true
        var textView = item.findViewById<TextView>(R.id.menuTextView)
        textView.text = text
        if (subText != null) {
            textView = item.findViewById(R.id.subTextView)
            textView.text = subText
        }
        val params = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        item.layoutParams = params

        // Set icon
        val imgView = item.findViewById<ImageView>(R.id.menuIcon)
        if (showIcon) {
            imgView.visibility = VISIBLE
        }
        if (iconId != -1) {
            imgView.setImageResource(iconId)
        }

        // hide separator
        item.findViewById<View>(R.id.menuSeparator).visibility = VISIBLE
        if (!showSeparator) {
            item.findViewById<View>(R.id.menuSeparator).visibility = GONE
        }
        val extraView = item.findViewById<LinearLayout>(R.id.menuContainer)
        extraView.addView(view)
        return item
    }

    /**
     * @brief  Create control view
     *
     * @param type type (boolean/numeric/list)
     * @param tag Identifying tag
     *
     * @return Return created view
     */
    private fun createControlView(type: String, tag: String): View? {
        val li = LayoutInflater.from(context)
        val params =
            LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.0f)
        params.gravity = Gravity.CENTER_VERTICAL
        when {
            type.equals(Setting.ATTR_VAL_BOOLEAN, ignoreCase = true) -> {
                val margin = resources.getDimensionPixelSize(R.dimen.printsettings_icon_setting_padding)
                params.leftMargin = margin
                params.rightMargin = margin
                val switchView = li.inflate(R.layout.printsettings_switch, null) as Switch
                switchView.setOnCheckedChangeListener(this)
                switchView.tag = tag
                switchView.layoutParams = params
                switchView.textOff =
                    resources.getString(string.ids_lbl_off).uppercase(Locale.getDefault())
                switchView.textOn =
                    resources.getString(string.ids_lbl_on).uppercase(Locale.getDefault())
                return switchView
            }
            type.equals(Setting.ATTR_VAL_NUMERIC, ignoreCase = true) -> {
                val margin = resources.getDimensionPixelSize(R.dimen.printsettings_icon_setting_padding)
                params.leftMargin = margin
                params.rightMargin = margin
                params.width = resources.getDimensionPixelSize(R.dimen.printsettings_input_width)
                val editText = li.inflate(R.layout.printsettings_input_edittext, null) as EditText
                editText.isActivated = true
                editText.layoutParams = params
                editText.tag = tag
                editText.inputType = InputType.TYPE_CLASS_NUMBER
                editText.filters = arrayOf<InputFilter>(
                    LengthFilter(AppConstants.CONST_COPIES_LIMIT)
                )
                editText.addTextChangedListener(EditTextWatcher(tag, 1))
                editText.setOnEditorActionListener(this)
                return editText
            }
            type.equals(Setting.ATTR_VAL_LIST, ignoreCase = true) -> {
                params.height = LayoutParams.MATCH_PARENT
                params.width = resources.getDimensionPixelSize(R.dimen.printsettings_list_value_width)
                val view = li.inflate(R.layout.printsettings_value_disclosure, null)
                view.isActivated = true
                view.layoutParams = params
                return view
            }
            else -> return null
        }
    }
    // ================================================================================
    // Collapsible Controls
    // ================================================================================
    /**
     * @brief Animate expand
     *
     * @param v View with collapse controls
     */
    private fun animateExpand(v: View?) {
        if (v == null) {
            return
        }
        val idx = _printSettingsTitles!!.indexOf(v)
        if (idx == -1) {
            return
        }
        val durationMultiplier = 0.2f
        val menuHeight = resources.getDimensionPixelSize(R.dimen.home_menu_height)
        var totalHeight = 0
        val target = v.getTag(ID_COLLAPSE_TARGET_GROUP) as ViewGroup
        for (i in 0 until target.childCount) {
            val child = target.getChildAt(i)
            if (child.visibility == GONE) {
                continue
            }
            var height = menuHeight
            if (i != target.childCount - 1) {
                height += resources.getDimensionPixelSize(R.dimen.separator_size)
            }
            totalHeight += height
            val animate = TranslateAnimation(0f, 0f, (-totalHeight).toFloat(), 0f)
            animate.duration = (totalHeight * durationMultiplier).toLong()

            // final child will end animation
            if (i == target.childCount - 1) {
                animate.setAnimationListener(object : AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}
                    override fun onAnimationRepeat(animation: Animation) {}
                    override fun onAnimationEnd(animation: Animation) {
                        val newMessage = Message.obtain(
                            _handler,
                            MSG_EXPAND
                        )
                        newMessage.arg1 = idx
                        _handler!!.sendMessage(newMessage)
                    }
                })
            }
            child.clearAnimation()
            child.startAnimation(animate)
        }
        for (i in idx + 1 until _printSettingsTitles!!.size) {
            val animate = TranslateAnimation(0f, 0f, (-totalHeight).toFloat(), 0f)
            animate.startOffset = (menuHeight * durationMultiplier).toLong()
            animate.duration = (totalHeight * durationMultiplier).toLong()
            _printSettingsTitles!![i].clearAnimation()
            _printSettingsTitles!![i].startAnimation(animate)
            val childTarget = _printSettingsTitles!![i].getTag(ID_COLLAPSE_TARGET_GROUP) as View
            if (childTarget.visibility != GONE) {
                childTarget.clearAnimation()
                childTarget.startAnimation(animate)
            }
        }
    }

    /**
     * @brief Animate collapse
     *
     * @param v View with collapse controls
     */
    private fun animateCollapse(v: View?) {
        if (v == null) {
            return
        }
        val idx = _printSettingsTitles!!.indexOf(v)
        if (idx == -1) {
            return
        }
        val target = v.getTag(ID_COLLAPSE_TARGET_GROUP) as ViewGroup
        val durationMultiplier = 0.2f
        val menuHeight = resources.getDimensionPixelSize(R.dimen.home_menu_height)
        val totalHeight = target.height
        var currentHeight = 0f
        for (i in 0 until target.childCount) {
            val child = target.getChildAt(i)
            if (child.visibility == GONE) {
                continue
            }
            var height = menuHeight
            if (i != target.childCount - 1) {
                height += resources.getDimensionPixelSize(R.dimen.separator_size)
            }
            currentHeight += height
            val animate = TranslateAnimation(0f, 0f, 0f, (-currentHeight))
            animate.duration = (currentHeight * durationMultiplier).toLong()
            animate.fillAfter = true
            if (i == target.childCount - 1) {
                animate.setAnimationListener(object : AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}
                    override fun onAnimationRepeat(animation: Animation) {}
                    override fun onAnimationEnd(animation: Animation) {
                        val newMessage = Message.obtain(
                            _handler,
                            MSG_COLLAPSE
                        )
                        newMessage.arg1 = idx
                        _handler!!.sendMessage(newMessage)
                    }
                })
            }
            child.clearAnimation()
            child.startAnimation(animate)
        }
        for (i in idx + 1 until _printSettingsTitles!!.size) {
            val animate = TranslateAnimation(0f, 0f, 0f, (-totalHeight).toFloat())
            animate.duration = (totalHeight * durationMultiplier).toLong()
            _printSettingsTitles!![i].clearAnimation()
            _printSettingsTitles!![i].startAnimation(animate)
            val childTarget = _printSettingsTitles!![i].getTag(ID_COLLAPSE_TARGET_GROUP) as View
            if (childTarget.visibility != GONE) {
                childTarget.clearAnimation()
                childTarget.startAnimation(animate)
            }
        }
    }

    /**
     * @brief Collapse a control
     *
     * @param v View with collapse controls
     * @param animate Should animate collapse
     */
    private fun collapseControl(v: View, animate: Boolean) {
        val target = v.getTag(ID_COLLAPSE_TARGET_GROUP) as View
        if (animate) {
            animateCollapse(v)
        } else {
            v.isSelected = true
            target.visibility = GONE
        }
    }

    /**
     * @brief Expand a control
     *
     * @param v View with collapse controls
     * @param animate Should animate expand
     */
    private fun expandControl(v: View, animate: Boolean) {
        val target = v.getTag(ID_COLLAPSE_TARGET_GROUP) as View
        target.visibility = VISIBLE
        if (animate) {
            animateExpand(v)
        } else {
            v.isSelected = false
        }
    }

    /**
     * @brief Toggle collapse
     *
     * @param v View to toggle
     */
    private fun toggleCollapse(v: View) {
        v.isClickable = false
        if (!v.isSelected) {
            collapseControl(v, true)
        } else {
            expandControl(v, true)
        }
    }

    /**
     * @brief Sets the secure print value. The UI and the persistent setting is updated
     *
     * @param isEnabled Secure Print is enabled.
     */
    private fun setSecurePrintEnabled(isEnabled: Boolean) {
        _mainView!!.findViewWithTag<View>(KEY_TAG_PIN_CODE).isActivated = isEnabled
        val editText = _mainView!!.findViewById<EditText>(R.id.view_id_pin_code_edit_text)
        editText.isEnabled = isEnabled
        editText.isActivated = isEnabled
        // RM1008 textbox should only be focusable when secure print is enabled
        editText.isFocusableInTouchMode = isEnabled
        //mMainView.findViewById(R.id.view_id_pin_code_edit_text).setActivated(isEnabled ? View.VISIBLE : View.INVISIBLE);
        //mMainView.findViewById(R.id.view_id_pin_code_edit_text).setVisibility(isEnabled ? View.VISIBLE : View.INVISIBLE);
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        editor.putBoolean(AppConstants.PREF_KEY_AUTH_SECURE_PRINT, isEnabled)
        editor.apply()
        if (isEnabled) {
            editText.setText(
                prefs.getString(
                    AppConstants.PREF_KEY_AUTH_PIN_CODE,
                    AppConstants.PREF_DEFAULT_AUTH_PIN_CODE
                )
            )
        } else {
            editText.setText("")
            // RM1008 prevent virtual keyboard from showing on relaunch
            editText.clearFocus()
        }
    }

    /**
     * @brief Checks the value of a TextView and sets it to 1 if text is
     * empty or less than or equal to 0
     *
     * @param v TextView to be edited
     */
    private fun checkEditTextValue(v: TextView) {
        if (v.inputType == InputType.TYPE_CLASS_NUMBER &&  // RM#910 + RM#918 if text input is for PIN code do not attempt to reset
            v.id != R.id.view_id_pin_code_edit_text
        ) {
            val value = v.text.toString()
            if (value.isEmpty()) {
                v.text = "1"
            } else {
                val `val` = value.toInt()
                if (`val` <= 0) {
                    v.text = "1"
                }
            }
        }
    }

    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.view_id_collapse_container) {
            toggleCollapse(v)
        } else if (id == R.id.view_id_show_subview_container) {
            if (_mainView!!.isEnabled) {
                displayOptionsSubview(v, true)
            }
        } else if (id == R.id.view_id_hide_subview_container) {
            if (_subView!!.isEnabled) {
                dismissOptionsSubview(true)
                _printerManager!!.createUpdateStatusThread()
            }
        } else if (id == R.id.view_id_subview_option_item ||
            id == R.id.view_id_subview_printer_item
        ) {
            subviewOptionsItemClicked(v)
        } else if (id == R.id.view_id_print_selected_printer) {
            if (_mainView!!.isEnabled) {
                displayOptionsSubview(v, true)
                _printerManager!!.createUpdateStatusThread()
            }
        } else if (id == R.id.view_id_print_header) {
            // Content Print - START
            if (ContentPrintManager.isBoxRegistrationMode) {
                val pinCodeEditText = _mainView!!.findViewById<EditText>(R.id.view_id_pin_code_edit_text)
                val loginId = pinCodeEditText.text.toString()
                val contentPrintManager = ContentPrintManager.getInstance()
                contentPrintManager?.registerToBox(
                    ContentPrintManager.selectedFile!!.fileId,
                    ContentPrintManager.selectedPrinter!!.serialNo!!,
                    ContentPrintPrintSettings.convertToContentPrintPrintSettings(_printSettings!!, loginId),
                    _registerToBox
                )
            } else {
                executePrint()
            }
            // Content Print - END
        }
    }

    // ================================================================================
    // INTERFACE - Callback
    // ================================================================================
    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            MSG_COLLAPSE -> {
                val view = _printSettingsTitles!![msg.arg1].getTag(ID_COLLAPSE_TARGET_GROUP) as View
                _printSettingsTitles!![msg.arg1].isSelected = true
                _printSettingsTitles!![msg.arg1].isClickable = true
                view.visibility = GONE
                return true
            }
            MSG_EXPAND -> {
                _printSettingsTitles!![msg.arg1].isSelected = false
                _printSettingsTitles!![msg.arg1].isClickable = true
                return true
            }
            MSG_SET_SCROLL -> {
                _printSettingsScrollView!!.scrollTo(0, msg.arg1)
                return true
            }
            MSG_SLIDE_IN -> {
                _mainView!!.visibility = GONE
                return true
            }
            MSG_SLIDE_OUT -> {
                PrinterManager.getInstance(SmartDeviceApp.appContext!!)!!
                    .cancelUpdateStatusThread()
                removeView(_subView)
                return true
            }
            MSG_SET_SUB_SCROLL -> {
                _subScrollView!!.scrollTo(0, msg.arg1)
                return true
            }
            MSG_SHOW_SUBVIEW -> {
                displayOptionsSubview(_mainView!!.findViewWithTag(msg.obj), false)
                if (msg.obj.toString() == KEY_TAG_PRINTER) {
                    _printerManager!!.createUpdateStatusThread()
                }
                return true
            }
        }
        return false
    }

    // ================================================================================
    // CompoundButton.OnCheckedChangeListener
    // ================================================================================
    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView.id == R.id.view_id_secure_print_switch) {
            setSecurePrintEnabled(isChecked)
            return
        }
        updateValue(buttonView.tag.toString(), if (isChecked) 1 else 0)
        _listener.onPrintSettingsValueChanged(_printSettings)
    }

    // ================================================================================
    // INTERFACE - Callback
    // ================================================================================
    override fun updateOnlineStatus() {
        // Content Print - START
        if (!ContentPrintManager.isBoxRegistrationMode) {
            for (i in _printersList!!.indices) {
                val printer = _printersList!![i]
                if (_subView == null) {
                    return
                }
                val view = _subView!!.findViewWithTag<View>(printer!!.id)
                if (view != null) {
                    val imageView = view.findViewById<View>(R.id.menuIcon)
                    _printerManager!!.updateOnlineStatus(printer.ipAddress, imageView)
                }
            }
        }
        // Content Print - END
    }

    // ================================================================================
    // INTERFACE - OnEditorActionListener
    // ================================================================================
    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            checkEditTextValue(v)
            return false
        }

        // RM#910 for chromebook, virtual keyboard has ENTER key instead of DONE key
        // it must be consumed (return true) to prevent focus from moving
        // it must also be hidden manually
        if (actionId == EditorInfo.IME_NULL) {
            if (event != null) {
                if (event.action == KeyEvent.ACTION_UP) {
                    checkEditTextValue(v)
                    AppUtils.hideSoftKeyboard(context as Activity)
                }
            }
            return true
        }
        return false
    }

    // Content Print - START
    // ================================================================================
    // ContentPrintManager IRegisterToBoxCallback
    // ================================================================================
    fun setRegisterToBoxCallback(callback: ContentPrintManager.IRegisterToBoxCallback) {
        _registerToBox = callback
    }
    // Content Print - END

    // ================================================================================
    // Internal classes
    // ================================================================================
    /**
     * @class EditTextWatcher
     *
     * @brief Class to manage input on the copies field
     *
     * @param _tag View tag
     * @param _minValue Minimum value of the field
     */
    private inner class EditTextWatcher (
        private val _tag: String,
        private val _minValue: Int
        ) : TextWatcher {

        private var _editing = false

        @Synchronized
        override fun afterTextChanged(s: Editable) {
            if (!_editing) {
                _editing = true
                val digits = s.toString().replace("\\D".toRegex(), "")
                try {
                    val formatted = digits.toInt().toString()
                    s.replace(0, s.length, formatted)
                } catch (nfe: NumberFormatException) {
                }
                var value = _minValue
                try {
                    value = s.toString().toInt()
                    value = max(_minValue, value)
                } catch (nfe: NumberFormatException) {
                }
                updateValue(_tag, value)
                _listener.onPrintSettingsValueChanged(_printSettings)
                _editing = false
            }
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    }

    /**
     * @class PinCodeTextWatcher
     *
     * @brief Class to manage input on the pincode textfield
     */
    private inner class PinCodeTextWatcher : TextWatcher {
        @Synchronized
        override fun afterTextChanged(s: Editable) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val isSecurePrint = prefs.getBoolean(
                AppConstants.PREF_KEY_AUTH_SECURE_PRINT,
                AppConstants.PREF_DEFAULT_AUTH_SECURE_PRINT
            )
            if (isSecurePrint) {
                val editor = prefs.edit()
                editor.putString(AppConstants.PREF_KEY_AUTH_PIN_CODE, s.toString())
                editor.apply()
            }
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        /** {@inheritDoc}  */
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    }

    /**
     * @interface PrintSettingsViewInterface
     *
     * @brief Interface for PrintSettingsView Events
     */
    interface PrintSettingsViewInterface {
        /**
         * @brief Printer ID Selected Changed Callback
         *
         * @param printerId printer ID
         */
        fun onPrinterIdSelectedChanged(printerId: Int)

        /**
         * @brief Print Settings Value Changed Callback
         *
         * @param printSettings Print settings
         */
        fun onPrintSettingsValueChanged(printSettings: PrintSettings?)

        /**
         * @brief Print Execution Callback
         *
         * @param printer Printer object
         * @param printSettings Print settings
         */
        fun onPrint(printer: Printer?, printSettings: PrintSettings?)
    }

    companion object {
        private const val KEY_SELECTED_TITLES = "key_selected_titles"
        private const val KEY_SCROLL_POSITION = "key_scroll_position"
        private const val KEY_SUBVIEW_DISPLAYED = "key_subview_displayed"
        private const val KEY_SUB_SCROLL_POSITION = "key_sub_scroll_position"
        private const val KEY_TAG_PRINTER = "key_tag_printer"
        private const val KEY_TAG_AUTHENTICATION = "key_tag_authentication"
        private const val KEY_TAG_SECURE_PRINT = "key_tag_secure_print"
        private const val KEY_TAG_PIN_CODE = "key_tag_pin_code"
        private const val MSG_COLLAPSE = 0
        private const val MSG_EXPAND = 1
        private const val MSG_SET_SCROLL = 2
        private const val MSG_SLIDE_IN = 3
        private const val MSG_SLIDE_OUT = 4
        private const val MSG_SET_SUB_SCROLL = 5
        private const val MSG_SHOW_SUBVIEW = 6
        private const val ID_COLLAPSE_TARGET_GROUP = 0x11000002
        private const val ID_TAG_TEXT = 0x11000008
        private const val ID_TAG_ICON = 0x11000009
        private const val ID_TAG_SETTING = 0x1100000A
        private const val ID_TAG_SUB_OPTIONS = 0x1100000B
        private const val ID_TAG_TARGET_VIEW = 0x1100000C
    }

    /**
     * @brief Constructs a new PrintSettingsView with layout parameters.
     *
     * @param context A Context object used to access application assets
     * @param attrs An AttributeSet passed to our parent
     */
    /**
     * @brief Constructs a new PrintSettingsView with a Context object
     */
    init {
        init()
    }
}