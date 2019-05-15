/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintSettingsView.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.printsettings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.UpdateStatusCallback;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.model.printsettings.Group;
import jp.co.riso.smartdeviceapp.model.printsettings.Option;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.BookletFinish;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Duplex;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.FinishingSide;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Imposition;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.ImpositionOrder;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.OutputTray;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Punch;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Staple;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.InputTray_RAG_LIO;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.PaperSize;
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings;
import jp.co.riso.smartdeviceapp.model.printsettings.Setting;
import jp.co.riso.smartdeviceapp.model.printsettings.XmlNode;
import jp.co.riso.smartprint.R;

/**
 * @class PrintSettingsView
 * 
 * @brief A View that displays the Print settings and Print controls.
 */
public class PrintSettingsView extends FrameLayout implements View.OnClickListener, Callback, CompoundButton.OnCheckedChangeListener, UpdateStatusCallback, OnEditorActionListener {
    
    private static final String KEY_SELECTED_TITLES = "key_selected_titles";
    private static final String KEY_SCROLL_POSITION = "key_scroll_position";
    private static final String KEY_SUBVIEW_DISPLAYED = "key_subview_displayed";
    private static final String KEY_SUB_SCROLL_POSITION = "key_sub_scroll_position";
    
    private static final String KEY_TAG_PRINTER = "key_tag_printer";
    private static final String KEY_TAG_AUTHENTICATION = "key_tag_authentication";
    private static final String KEY_TAG_SECURE_PRINT = "key_tag_secure_print";
    private static final String KEY_TAG_PIN_CODE = "key_tag_pin_code";
    
    private static final int MSG_COLLAPSE = 0;
    private static final int MSG_EXPAND = 1;
    private static final int MSG_SET_SCROLL = 2;
    private static final int MSG_SLIDE_IN = 3;
    private static final int MSG_SLIDE_OUT = 4;
    private static final int MSG_SET_SUB_SCROLL = 5;
    private static final int MSG_SHOW_SUBVIEW = 6;
    
    private static final int ID_COLLAPSE_TARGET_GROUP = 0x11000002;
    
    private static final int ID_TAG_TEXT = 0x11000008;
    private static final int ID_TAG_ICON = 0x11000009;
    private static final int ID_TAG_SETTING = 0x1100000A;
    private static final int ID_TAG_SUB_OPTIONS = 0x1100000B;
    private static final int ID_TAG_TARGET_VIEW = 0x1100000C;

    private PrintSettings mPrintSettings = null;
    private int mPrinterId = PrinterManager.EMPTY_ID;
    private List<Printer> mPrintersList = null;
    
    private LinearLayout mMainView = null;
    private ScrollView mPrintSettingsScrollView = null;
    private LinearLayout mPrintSettingsLayout = null;
    
    private LinearLayout mSubView = null;
    private ScrollView mSubScrollView = null;
    private LinearLayout mSubOptionsLayout = null;
    
    private LinearLayout mPrintControls = null;
    
    private Handler mHandler = null;
    private ArrayList<LinearLayout> mPrintSettingsTitles = null;
    
    private PrintSettingsViewInterface mListener = null;
    private PrinterManager mPrinterManager = null;

    /**
     * @brief Constructs a new PrintSettingsView with a Context object
     * 
     * @param context A Context object used to access application assets 
     */
    public PrintSettingsView(Context context) {
        this(context, null);
    }
    
    /**
     * @brief Constructs a new PrintSettingsView with layout parameters.
     * 
     * @param context A Context object used to access application assets
     * @param attrs An AttributeSet passed to our parent 
     */
    public PrintSettingsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    /**
     * @brief Constructs a new PrintSettingsView with layout parameters and a default style.
     * 
     * @param context A Context object used to access application assets
     * @param attrs An AttributeSet passed to our parent 
     * @param defStyle The default style resource ID  
     */
    public PrintSettingsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    /**
     * @brief Constructs the PrintSettingsView
     */
    private void init() {
        if (isInEditMode()) {
            return;
        }
        
        mPrinterManager = PrinterManager.getInstance(SmartDeviceApp.getAppContext());
        mPrinterManager.setUpdateStatusCallback(this);
        
        mHandler = new Handler(this);
        loadPrintersList();
        
        initializeMainView();
        initializePrinterControls();
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        View view = mMainView.findViewWithTag(PrintSettings.TAG_COPIES);
        if (view != null && view instanceof EditText) {
            if (!AppUtils.checkViewHitTest(view, (int) ev.getRawX(), (int) ev.getRawY())) {
                checkEditTextValue((EditText)view);
            } else {
                return super.onInterceptTouchEvent(ev);
            }
        }
        view = mMainView.findViewById(R.id.view_id_pin_code_edit_text);
        if (view != null && view instanceof EditText) {
            if (AppUtils.checkViewHitTest(view, (int) ev.getRawX(), (int) ev.getRawY())) {
                return super.onInterceptTouchEvent(ev);
            }
        }
        
        AppUtils.hideSoftKeyboard((Activity) getContext());
        return super.onInterceptTouchEvent(ev);
        
    }
    
    /**
     * @brief Constructs the main view. Print/Printer controls and print settings
     */
    private void initializeMainView() {
        mMainView = new LinearLayout(getContext());
        mMainView.setOrientation(LinearLayout.VERTICAL);
        
        mPrintSettingsLayout = new LinearLayout(getContext());
        mPrintSettingsLayout.setOrientation(LinearLayout.VERTICAL);
        
        mPrintSettingsScrollView = new ScrollView(getContext());
        mPrintSettingsScrollView.addView(mPrintSettingsLayout, new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        
        mMainView.addView(mPrintSettingsScrollView, new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        addView(mMainView, new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    /**
     * @brief Gets the list of printers from manager
     */
    private void loadPrintersList() {
        mPrintersList = PrinterManager.getInstance(SmartDeviceApp.getAppContext()).getSavedPrintersList();
    }
    
    // ================================================================================
    // Save/Restore State
    // ================================================================================
    
    /**
     * @brief Save the state of the PrintSettingsView in the Bundle
     * 
     * @param outState The Bundle to store the PrintSettingsView state.
     */
    public void saveState(Bundle outState) {
        // Create String list of collapsed headers
        ArrayList<String> selectedTitles = new ArrayList<String>();
        for (int i = 0; i < mPrintSettingsTitles.size(); i++) {
            if (mPrintSettingsTitles.get(i).isSelected()) {
                selectedTitles.add((String) mPrintSettingsTitles.get(i).getTag());
            }
        }
        
        // Need to convert to String array (Direct Object[] to String[] has JVM error
        String[] stockArr = new String[selectedTitles.size()];
        stockArr = selectedTitles.toArray(stockArr);
        
        // Save to bundle
        outState.putStringArray(KEY_SELECTED_TITLES, stockArr);
        
        // Save the scroll pos to bundle
        outState.putInt(KEY_SCROLL_POSITION, mPrintSettingsScrollView.getScrollY());
        
        // If main scroll is visible, then sub view is displayed
        if (mMainView.getVisibility() != View.VISIBLE) {
            outState.putString(KEY_SUBVIEW_DISPLAYED, mSubView.getTag().toString());
            outState.putInt(KEY_SUB_SCROLL_POSITION, mSubScrollView.getScrollY());
        }
    }
    
    /**
     * @brief Restore the state of the PrintSettingsView from a Bundle
     * 
     * @param savedInstanceState The incoming Bundle of state.
     */
    public void restoreState(Bundle savedInstanceState) {
        // Collapse the headers retrieved from the saved bundle
        String selectedTitle[] = savedInstanceState.getStringArray(KEY_SELECTED_TITLES);
        for (int i = 0; i < selectedTitle.length; i++) {
            if (mMainView.findViewWithTag(selectedTitle[i]) != null) {
                collapseControl(mMainView.findViewWithTag(selectedTitle[i]), false);
            }
        }
        
        // Set the scroll position of the main scroll view, send delayed message
        // since view has not been initialized yet
        Message newMessage = Message.obtain(mHandler, MSG_SET_SCROLL);
        newMessage.arg1 = savedInstanceState.getInt(KEY_SCROLL_POSITION, 0);
        mHandler.sendMessage(newMessage);
        
        if (savedInstanceState.containsKey(KEY_SUBVIEW_DISPLAYED)) {
            // Show the subview after a delay
            String tag = savedInstanceState.getString(KEY_SUBVIEW_DISPLAYED, "");
            newMessage = Message.obtain(mHandler, MSG_SHOW_SUBVIEW);
            newMessage.obj = tag;
            mHandler.sendMessage(newMessage);
            
            // Set the scroll position of the subview's scroll view, send delayed message
            // since view has not been initialized yet
            newMessage = Message.obtain(mHandler, MSG_SET_SUB_SCROLL);
            newMessage.arg1 = savedInstanceState.getInt(KEY_SUB_SCROLL_POSITION, 0);
            mHandler.sendMessage(newMessage);
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
    private boolean shouldDisplayOptionFromConstraints(String tag, int value) {
        if (tag.equals(PrintSettings.TAG_STAPLE)) {
            boolean isTop = (mPrintSettings.getValue(PrintSettings.TAG_FINISHING_SIDE) == FinishingSide.TOP.ordinal());
            switch (Staple.values()[value]) {
                case ONE_UL:
                case ONE_UR:
                    return isTop;
                case ONE:
                    return !isTop;
                case TWO:
                case OFF:
            }
        }
        
        if (tag.equals(PrintSettings.TAG_IMPOSITION_ORDER)) {
            boolean is2up = (mPrintSettings.getValue(PrintSettings.TAG_IMPOSITION) == Imposition.TWO_UP.ordinal());
            switch (ImpositionOrder.values()[value]) {
                case L_R:
                case R_L:
                    return is2up;
                case TL_B:
                case TL_R:
                case TR_B:
                case TR_L:
                    return !is2up;
            }
        }
        
        if (tag.equals(PrintSettings.TAG_OUTPUT_TRAY)) {
            boolean isPunch = mPrintSettings.getPunch() != Punch.OFF;
            boolean isFold = mPrintSettings.getBookletFinish() != BookletFinish.OFF;
            switch (OutputTray.values()[value]) {
                case AUTO:
                    return true;
                case TOP:
                case STACKING:
                    return !isFold;
                case FACEDOWN:
                    return (!isPunch && !isFold);
            }
        }

        if (tag.equals(PrintSettings.TAG_PAPER_SIZE) && (getPrinter().isPrinterRag() || getPrinter().isPrinterLio())) {
            boolean isExternal = mPrintSettings.getInputTray() == InputTray_RAG_LIO.EXTERNAL_FEEDER;
            switch (PaperSize.values()[value]) {
                case A4:
                case B5:
                case LETTER:
                case JUROKUKAI:
                    return true;
                case A3:
                case A3W:
                case SRA3:
                case A5:
                case A6:
                case B4:
                case B6:
                case FOOLSCAP:
                case TABLOID:
                case LEGAL:
                case STATEMENT:
                case LEGAL13:
                case HACHIKAI:
                    return !isExternal;
            }
        }

        if (tag.equals(PrintSettings.TAG_INPUT_TRAY) && (getPrinter().isPrinterRag() || getPrinter().isPrinterLio())) {
            // if paper size is equal to A4, B5, Letter, or 16k
            boolean isPaperSupported = (mPrintSettings.getPaperSize() == PaperSize.A4 ||
                    mPrintSettings.getPaperSize() == PaperSize.B5 ||
                    mPrintSettings.getPaperSize() == PaperSize.LETTER ||
                    mPrintSettings.getPaperSize() == PaperSize.JUROKUKAI);
            switch (InputTray_RAG_LIO.values()[value]) {
                case AUTO:
                case STANDARD:
                case TRAY1:
                case TRAY2:
                    return true;
                case EXTERNAL_FEEDER:
                    return isPaperSupported;
            }
        }
        return true;
    }

    /**
     * @brief Gets the default value of a setting.
     * 
     * @param tag Print settings tag
     * 
     * @return Default value for the tag
     */
    private int getDefaultValueWithConstraints(String tag) {
        if(mPrintSettings == null){
            return -1;
        }

        if (PrintSettings.sSettingsMaps.get(mPrintSettings.getSettingMapKey()) == null
                || PrintSettings.sSettingsMaps.get(mPrintSettings.getSettingMapKey()).get(tag) == null) {
            return -1;
        }
        
        int defaultValue = PrintSettings.sSettingsMaps.get(mPrintSettings.getSettingMapKey()).get(tag).getDefaultValue();
        
        return defaultValue;
    }
    
    /**
     * @brief Checks if the view is enabled.
     * 
     * @param tag Print settings tag
     * 
     * @retval true View is enabled
     * @retval false View is disabled
     */
    private boolean isViewEnabled(String tag) {
        if (mMainView != null) {
            if (mMainView.findViewWithTag(tag) != null) {
                return mMainView.findViewWithTag(tag).isEnabled();
            }
        }
        
        return false;
    }
    
    /**
     * @brief Controls the state of the view
     * 
     * @param tag Print settings tag
     * @param enabled The setting is enabled or not
     * @param hideControl The setting options is hidden or not
     */
    private void setViewEnabledWithConstraints(String tag, boolean enabled, boolean hideControl) {
        if (mMainView != null) {
            if (mMainView.findViewWithTag(tag) != null) {
                View view = mMainView.findViewWithTag(tag); 
                view.setEnabled(enabled);
                View targetView = (View) view.getTag(ID_TAG_TARGET_VIEW);
                if (targetView != null) {
                    targetView.setActivated(enabled);
                }
                
                View disclosureView = (View) view.findViewById(R.id.view_id_disclosure);
                if (disclosureView != null) {
                    if (enabled) {
                        view.findViewById(R.id.listValueTextView).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.disclosureIndicator).setVisibility(View.VISIBLE);
                    } else if (hideControl) {
                        view.findViewById(R.id.listValueTextView).setVisibility(View.INVISIBLE);
                        view.findViewById(R.id.disclosureIndicator).setVisibility(View.VISIBLE);
                    } else {
                        view.findViewById(R.id.listValueTextView).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.disclosureIndicator).setVisibility(View.VISIBLE);
                    }
                } else {
                    boolean shouldHideControl = !enabled && hideControl;
                    view.setVisibility(shouldHideControl ? View.GONE : View.VISIBLE);
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
    private void updateValueWithConstraints(String tag, int value) {
        mPrintSettings.setValue(tag, value);
        updateDisplayedValue(tag);
    }
    
    /**
     * @brief Apply view constraints. Corresponds to hidden and disabled views
     * 
     * @param tag Print settings tag
     */
    private void applyViewConstraints(String tag) {
        int value = mPrintSettings.getValue(tag);
        
        // Constraint #1 Booklet
        if (tag.equals(PrintSettings.TAG_BOOKLET)) {
            boolean enabled = (value == 0);
            setViewEnabledWithConstraints(PrintSettings.TAG_DUPLEX, enabled, false);
            setViewEnabledWithConstraints(PrintSettings.TAG_FINISHING_SIDE, enabled, false);
            setViewEnabledWithConstraints(PrintSettings.TAG_STAPLE, enabled, false);
            setViewEnabledWithConstraints(PrintSettings.TAG_PUNCH, enabled, false);
            //setViewEnabledWithConstraints(PrintSettings.TAG_IMPOSITION, enabled, true);
            //setViewEnabledWithConstraints(PrintSettings.TAG_IMPOSITION_ORDER, enabled, true);
            
            setViewEnabledWithConstraints(PrintSettings.TAG_BOOKLET_FINISH, !enabled, false);
            setViewEnabledWithConstraints(PrintSettings.TAG_BOOKLET_LAYOUT, !enabled, false);
            
            //applyViewConstraints(PrintSettings.TAG_IMPOSITION);
        }
        
        // Constraint #5 Imposition
        if (tag.equals(PrintSettings.TAG_IMPOSITION)) {
            boolean bookletEnabled = mPrintSettings.isBooklet();
            
            if (bookletEnabled) {
                setViewEnabledWithConstraints(PrintSettings.TAG_IMPOSITION_ORDER, false, false);
            } else {
                boolean enabled = (value != Imposition.OFF.ordinal()) && isViewEnabled(tag);
                setViewEnabledWithConstraints(PrintSettings.TAG_IMPOSITION_ORDER, enabled, true);
            }
        }
    }
    
    /**
     * @brief Apply value constraints. Corresponds to dependency of values.
     * 
     * @param tag Print Settings Tag
     * @param prevValue Previous value
     */
    private void applyValueConstraints(String tag, int prevValue) {
        int value = mPrintSettings.getValue(tag);
        
        // Constraint #1 Booklet
        if (tag.equals(PrintSettings.TAG_BOOKLET)) {
            updateValueWithConstraints(PrintSettings.TAG_DUPLEX, getDefaultValueWithConstraints(PrintSettings.TAG_DUPLEX));
            updateValueWithConstraints(PrintSettings.TAG_FINISHING_SIDE, getDefaultValueWithConstraints(PrintSettings.TAG_FINISHING_SIDE));
            updateValueWithConstraints(PrintSettings.TAG_STAPLE, getDefaultValueWithConstraints(PrintSettings.TAG_STAPLE));
            updateValueWithConstraints(PrintSettings.TAG_PUNCH, getDefaultValueWithConstraints(PrintSettings.TAG_PUNCH));
            
            updateValueWithConstraints(PrintSettings.TAG_BOOKLET_FINISH, getDefaultValueWithConstraints(PrintSettings.TAG_BOOKLET_FINISH));
            updateValueWithConstraints(PrintSettings.TAG_BOOKLET_LAYOUT, getDefaultValueWithConstraints(PrintSettings.TAG_BOOKLET_LAYOUT));
            
            updateValueWithConstraints(PrintSettings.TAG_IMPOSITION, getDefaultValueWithConstraints(PrintSettings.TAG_IMPOSITION));
            updateValueWithConstraints(PrintSettings.TAG_IMPOSITION_ORDER, getDefaultValueWithConstraints(PrintSettings.TAG_IMPOSITION_ORDER));
            
            if (mPrintSettings.isBooklet()) {
                // OFF to ON
                updateValueWithConstraints(PrintSettings.TAG_DUPLEX, Duplex.SHORT_EDGE.ordinal());
            }
            
        }
        
        // Constraint #2 Finishing Side
        if (tag.equals(PrintSettings.TAG_FINISHING_SIDE)) {
            int stapleValue = mPrintSettings.getStaple().ordinal();
            if (value == FinishingSide.TOP.ordinal()) {
                if (stapleValue == Staple.ONE.ordinal()) {
                    if (prevValue == FinishingSide.LEFT.ordinal()) {
                        updateValueWithConstraints(PrintSettings.TAG_STAPLE, Staple.ONE_UL.ordinal());
                    } else {
                        updateValueWithConstraints(PrintSettings.TAG_STAPLE, Staple.ONE_UR.ordinal());
                    }
                }
            } else {
                if (stapleValue == Staple.ONE_UL.ordinal() || stapleValue == Staple.ONE_UR.ordinal()) {
                    updateValueWithConstraints(PrintSettings.TAG_STAPLE, Staple.ONE.ordinal());
                }
            }
        }
        
        // Constraint #4 Punch
        if (tag.equals(PrintSettings.TAG_PUNCH)) {
            if (value != Punch.OFF.ordinal()) {
                int outputTrayValue = mPrintSettings.getValue(PrintSettings.TAG_OUTPUT_TRAY);
                if (outputTrayValue == OutputTray.FACEDOWN.ordinal()) {
                    updateValueWithConstraints(PrintSettings.TAG_OUTPUT_TRAY, getDefaultValueWithConstraints(PrintSettings.TAG_OUTPUT_TRAY));
                }
            }
        }
        
        // Constraint #5 Imposition
        if (tag.equals(PrintSettings.TAG_IMPOSITION)) {
            if (value == Imposition.OFF.ordinal()) {
                updateValueWithConstraints(PrintSettings.TAG_IMPOSITION_ORDER, ImpositionOrder.L_R.ordinal());

                } else if (mPrintSettings.isBooklet()){
                    updateValueWithConstraints(PrintSettings.TAG_BOOKLET, getDefaultValueWithConstraints(PrintSettings.TAG_BOOKLET));
                    updateValueWithConstraints(PrintSettings.TAG_BOOKLET_FINISH, getDefaultValueWithConstraints(PrintSettings.TAG_BOOKLET_FINISH));
                    updateValueWithConstraints(PrintSettings.TAG_BOOKLET_LAYOUT, getDefaultValueWithConstraints(PrintSettings.TAG_BOOKLET_LAYOUT));

                // Ver.2.0.4.7 Start
                // Duplex is set in OFF if imposition is changed to ON when booklet is ON (2017.01.17 RISO Saito)
                updateValueWithConstraints(PrintSettings.TAG_DUPLEX, getDefaultValueWithConstraints(PrintSettings.TAG_DUPLEX));
                // Ver.2.0.4.7 End
                }


            if (value == Imposition.TWO_UP.ordinal()) {
                if (prevValue == Imposition.FOUR_UP.ordinal()) {
                    int impositionOrderValue = mPrintSettings.getImpositionOrder().ordinal();
                    switch (ImpositionOrder.values()[impositionOrderValue]) {
                        case TR_B:
                        case TR_L:
                            updateValueWithConstraints(PrintSettings.TAG_IMPOSITION_ORDER, ImpositionOrder.R_L.ordinal());
                            break;
                        default:
                            updateValueWithConstraints(PrintSettings.TAG_IMPOSITION_ORDER, ImpositionOrder.L_R.ordinal());
                            break;
                    }
                } else {
                    updateValueWithConstraints(PrintSettings.TAG_IMPOSITION_ORDER, ImpositionOrder.L_R.ordinal());
                }
            }
            if (value == Imposition.FOUR_UP.ordinal()) {
                if (prevValue == Imposition.TWO_UP.ordinal()) {
                    int impositionOrderValue = mPrintSettings.getImpositionOrder().ordinal();
                    switch (ImpositionOrder.values()[impositionOrderValue]) {
                        case R_L:
                            updateValueWithConstraints(PrintSettings.TAG_IMPOSITION_ORDER, ImpositionOrder.TR_L.ordinal());
                            break;
                        default:
                            updateValueWithConstraints(PrintSettings.TAG_IMPOSITION_ORDER, ImpositionOrder.TL_R.ordinal());
                            break;
                    }
                } else {
                    updateValueWithConstraints(PrintSettings.TAG_IMPOSITION_ORDER, ImpositionOrder.TL_R.ordinal());
                }
            }
        }
        
        // Constraint #6 BookletFinish
        if (tag.equals(PrintSettings.TAG_BOOKLET_FINISH)) {
            if (value != BookletFinish.OFF.ordinal()) {
                int outputTrayValue = mPrintSettings.getValue(PrintSettings.TAG_OUTPUT_TRAY);
                if (outputTrayValue != OutputTray.AUTO.ordinal()) {
                    updateValueWithConstraints(PrintSettings.TAG_OUTPUT_TRAY, getDefaultValueWithConstraints(PrintSettings.TAG_OUTPUT_TRAY));
                }
            }
        }

        // Constraint #7 Paper Size - Input Tray (External) for RAG or LIO series
        if (tag.equals(PrintSettings.TAG_PAPER_SIZE) && (getPrinter().isPrinterRag() || getPrinter().isPrinterLio())) {
            int inputTrayValue = mPrintSettings.getValue(PrintSettings.TAG_INPUT_TRAY);
            if (value != PaperSize.A4.ordinal() && value != PaperSize.B5.ordinal() &&
                    value != PaperSize.LETTER.ordinal() && value != PaperSize.JUROKUKAI.ordinal() &&
                    inputTrayValue == InputTray_RAG_LIO.EXTERNAL_FEEDER.ordinal()) {
                updateValueWithConstraints(PrintSettings.TAG_INPUT_TRAY, InputTray_RAG_LIO.AUTO.ordinal());
            }
        }

        // Constraint #8 Input Tray (External) - Paper Size for RAG or LIO series
        if (tag.equals(PrintSettings.TAG_INPUT_TRAY) && (getPrinter().isPrinterRag() || getPrinter().isPrinterLio())) {
            int paperSizeValue = mPrintSettings.getPaperSize().ordinal();
            if (value == InputTray_RAG_LIO.EXTERNAL_FEEDER.ordinal()) {
                if (paperSizeValue != PaperSize.A4.ordinal() && paperSizeValue != PaperSize.B5.ordinal() &&
                        paperSizeValue != PaperSize.LETTER.ordinal() && paperSizeValue != PaperSize.JUROKUKAI.ordinal()) {
                    updateValueWithConstraints(PrintSettings.TAG_PAPER_SIZE, PaperSize.A4.ordinal());
                }
            }
        }
    }
    
    // ================================================================================
    // Set Values
    // ================================================================================
    
    /**
     * @brief Display the highlighted printer
     * 
     * @param printerId Printer ID 
     */
    private void updateHighlightedPrinter(int printerId) {
        if (mPrintControls == null) {
            return;
        }
        
        Printer targetPrinter = getPrinter();
        
        View v = mPrintControls.findViewById(R.id.view_id_print_selected_printer);
        
        TextView nameTextView = (TextView) v.findViewById(R.id.listValueTextView);
        TextView ipAddressTextView = (TextView) v.findViewById(R.id.listValueSubTextView);
        View menuContainer = v.findViewById(R.id.menuContainer);

        int height = getResources().getDimensionPixelSize(R.dimen.home_menu_height);
        ipAddressTextView.setVisibility(View.GONE);
        
        if (targetPrinter == null) {
            nameTextView.setText(R.string.ids_lbl_choose_printer);
        } else {
            String printerName = targetPrinter.getName();
            if (printerName == null || printerName.isEmpty()) {
                printerName = getContext().getResources().getString(R.string.ids_lbl_no_name);
            }
            nameTextView.setText(printerName);
            ipAddressTextView.setVisibility(View.VISIBLE);
            ipAddressTextView.setText(targetPrinter.getIpAddress());
            height = getResources().getDimensionPixelSize(R.dimen.printsettings_option_with_sub_height);
        }
        
        menuContainer.getLayoutParams().height = height;
    }
    
    /**
     * @brief Sets the visibility of a view
     * 
     * @param name Layout tag
     * @param visible Visibility of the view. (VISIBLE, INVISIBLE, or GONE)
     */
    private void setViewVisible(String name, boolean visible) {
        View view = mMainView.findViewWithTag(name);
        View targetView = (View) view.getTag(ID_TAG_TARGET_VIEW);
        targetView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
    
    /**
     * @brief Hide the settings disabled based on the printer capabilities
     */
    private void hideDisabledPrintSettings() {
        Printer printer = getPrinter();
        
        boolean isStapleAvailable = printer == null || printer.getConfig().isStaplerAvailable();
        setViewVisible(PrintSettings.TAG_STAPLE, isStapleAvailable);
        
        boolean isPunchAvailable = printer == null || printer.getConfig().isPunchAvailable();
        setViewVisible(PrintSettings.TAG_PUNCH, isPunchAvailable);
        
        boolean isBookletFinishingAvailable = printer == null || printer.getConfig().isBookletFinishingAvailable();
        setViewVisible(PrintSettings.TAG_BOOKLET_FINISH, isBookletFinishingAvailable);
        
        setViewVisible(PrintSettings.TAG_ORIENTATION, !AppConstants.USE_PDF_ORIENTATION);
        
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
    private boolean shouldDisplayOptionFromPrinter(String name, int value) {
        if (getPrinter() != null) {
            if (name.equals(PrintSettings.TAG_OUTPUT_TRAY)) {
                switch (OutputTray.values()[value]) {
                    case AUTO:
                        return true;
                    case FACEDOWN:
                        //ver.2.0.1.2 We always display "Facedown" in the Output tray list(20160707 RISO Saito)
                        //return getPrinter().getConfig().isTrayFaceDownAvailable();
                        return true;
                        //End
                    case TOP:
                        return getPrinter().getConfig().isTrayTopAvailable();
                    case STACKING:
                        return getPrinter().getConfig().isTrayStackAvailable();
                }
            }
            if (name.equals(PrintSettings.TAG_PUNCH)) {
                switch (Punch.values()[value]){
                    case OFF:
                    case HOLES_2:
                        return true;
                    case HOLES_3:
                        return getPrinter().getConfig().isPunch3Available();
                    case HOLES_4:
                        return getPrinter().getConfig().isPunch4Available();
                }
            }
            if ((getPrinter().isPrinterRag() || getPrinter().isPrinterLio()) &&
                    name.equals(PrintSettings.TAG_INPUT_TRAY)) {
                switch (InputTray_RAG_LIO.values()[value]) {
                    case AUTO:
                    case STANDARD:
                    case TRAY1:
                    case TRAY2:
                        return true;
                    case EXTERNAL_FEEDER:
                        return getPrinter().getConfig().isExternalFeederAvailable();
                }
            }
        }
        
        return true;
    }

    /**
     * @brief Updates the value and applies the value and view constraints
     * 
     * @param tag Print settings tag name
     * @param newValue New value
     * 
     * @retval true Update is successful
     * @retval false Update failed
     */
    private boolean updateValue(String tag, int newValue) {
        int prevValue = mPrintSettings.getValue(tag);
        if (mPrintSettings.setValue(tag, newValue)) {
            applyValueConstraints(tag, prevValue);
            applyViewConstraints(tag);
            return true;
        }
        
        return false;
    }
    
    /**
     * @brief Updates the value and applies the view constraints
     * 
     * @param tag Print settings tag name
     */
    private void updateDisplayedValue(String tag) {
        int value = mPrintSettings.getValue(tag);
        View view = mMainView.findViewWithTag(tag);
        
        if (view != null) {
            if (view instanceof LinearLayout) {
                TextView textView = (TextView) view.findViewById(R.id.listValueTextView);
                
                String text = Integer.toString(value);

                Setting setting = (Setting) view.getTag(ID_TAG_SETTING);
                Object[] options = getOptionsStrings(tag, setting.getOptions());
                if (value >= 0 && value < options.length) {
                    text = options[value].toString();
                }
                
                textView.setText(text);
            }
            
            if (view instanceof EditText) {
                ((EditText) view).setText(String.format("%d", value));
            }
            
            if (view instanceof Switch) {
                ((Switch) view).setOnCheckedChangeListener(null);
                ((Switch) view).setChecked(value == 1);
                ((Switch) view).setOnCheckedChangeListener(this);
            }
        }
        
        applyViewConstraints(tag);
    }
    
    /**
     * @brief Sets the value changed listener
     * 
     * @param listener PrintSettingsViewInterface listener
     */
    public void setValueChangedListener(PrintSettingsViewInterface listener) {
        mListener = listener;
    }
    
    /**
     * @brief Displays the print controls.
     * 
     * @param showPrintControls Should show the print controls or not.
     */
    public void setShowPrintControls(boolean showPrintControls) {
        if (showPrintControls) {
            mPrintControls.findViewById(R.id.view_id_print_header).setVisibility(View.VISIBLE);
            View selectedPrinter = mPrintControls.findViewById(R.id.view_id_print_selected_printer);
            selectedPrinter.findViewById(R.id.disclosureIndicator).setVisibility(View.VISIBLE);
            selectedPrinter.setClickable(true);
            
            View authenticationGroup = mMainView.findViewWithTag(KEY_TAG_AUTHENTICATION);
            authenticationGroup.setVisibility(View.VISIBLE);
            
            View targetGroup = (View) authenticationGroup.getTag(ID_COLLAPSE_TARGET_GROUP);
            targetGroup.setVisibility(View.VISIBLE);
        } else {
            mPrintControls.findViewById(R.id.view_id_print_header).setVisibility(View.GONE);
            View selectedPrinter = mPrintControls.findViewById(R.id.view_id_print_selected_printer);
            selectedPrinter.findViewById(R.id.disclosureIndicator).setVisibility(View.GONE);
            selectedPrinter.setClickable(false);

            View authenticationGroup = mMainView.findViewWithTag(KEY_TAG_AUTHENTICATION);
            authenticationGroup.setVisibility(View.GONE);
            
            View targetGroup = (View) authenticationGroup.getTag(ID_COLLAPSE_TARGET_GROUP);
            targetGroup.setVisibility(View.GONE);
        }
    }
    
    /**
     * @brief Sets the printer id and print settings without applying constraints.
     * 
     * @param printerId ID of the printer
     * @param printSettings Print Settings to be applied
     */
    public void setInitialValues(int printerId, PrintSettings printSettings) {
        mPrinterId = printerId;
        
        setPrintSettings(printSettings);
        setPrinterId(printerId);
    }
    
    /**
     * @brief Sets the selected printer. UI is also updated
     * 
     * @param printerId ID of the printer
     */
    protected void setPrinterId(int printerId) {
        mPrinterId = printerId;
        
        updateHighlightedPrinter(mPrinterId);
        hideDisabledPrintSettings();
    }
    
    /**
     * @brief Creates a copy of the print settings and applies it in the view
     * 
     * @param printSettings Print Settings to be applied
     */
    protected void setPrintSettings(PrintSettings printSettings) {
        boolean initializeViews = true;
        boolean clearAuthValues = false;
        if(mPrintSettings != null){
            if(!mPrintSettings.getSettingMapKey().equalsIgnoreCase(printSettings.getSettingMapKey())) {
                mPrintSettingsLayout.removeAllViews();
                clearAuthValues = true;
            }
            else {
                initializeViews = false;
            }
        }
        mPrintSettings = new PrintSettings(printSettings);

        if (initializeViews) {
            initializePrintSettingsControls();
            initializeAuthenticationSettingsView();
            initializeAuthenticationValues(clearAuthValues);
        }

        Set<String> keySet = PrintSettings.sSettingsMaps.get(printSettings.getSettingMapKey()).keySet();

        for (String key : keySet) {
            updateDisplayedValue(key);
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
    private Printer getPrinterFromList(int printerId) {
        for (Printer p : mPrintersList) {
            if (p.getId() == printerId) {
                return p;
            }
        }
        return null;
    }
    
    /**
     * @brief Get the current printer highlighted
     * 
     * @return Printer object instance
     * @retval null Printer is not on the list
     */
    public Printer getPrinter() {
        return getPrinterFromList(mPrinterId);
    }
    
    // ================================================================================
    // Printer controls functions
    // ================================================================================
    
    /**
     * @brief Create the printer controls
     */
    private void initializePrinterControls() {
        mPrintControls = new LinearLayout(getContext());
        mPrintControls.setOrientation(LinearLayout.VERTICAL);
        
        // Create Header
        LayoutInflater li = LayoutInflater.from(getContext());
        LinearLayout header = (LinearLayout) li.inflate(R.layout.printsettings_print, null);
        
        int height = getResources().getDimensionPixelSize(R.dimen.home_menu_height);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, height);
        params.gravity = Gravity.CENTER;
        header.setLayoutParams(params);
        
        header.setId(R.id.view_id_print_header);
        header.setOnClickListener(this);
        mPrintControls.addView(header);
        
        // Create disclosure for item
        View view = LayoutInflater.from(getContext()).inflate(R.layout.printsettings_value_disclosure_with_sub, null);
        view.setActivated(true);
        
        int viewWidth = getResources().getDimensionPixelSize(R.dimen.printsettings_list_value_width);
        params = new LinearLayout.LayoutParams(viewWidth, LayoutParams.MATCH_PARENT, 0.0f);
        params.gravity = Gravity.CENTER_VERTICAL;
        view.setLayoutParams(params);
        
        // Create item
        LinearLayout selectedPrinter = createItem(getResources().getString(R.string.ids_lbl_printer), false, -1, false, view);
        
        selectedPrinter.setId(R.id.view_id_print_selected_printer);
        selectedPrinter.setTag(KEY_TAG_PRINTER);
        selectedPrinter.setOnClickListener(this);
        mPrintControls.addView(selectedPrinter);
        
        params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        mPrintControls.setLayoutParams(params);
        mMainView.addView(mPrintControls, 0);

        view = new View(getContext());
        view.setBackgroundResource(R.color.theme_light_1);
        height = getResources().getDimensionPixelSize(R.dimen.separator_size);
        params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, height);
        view.setLayoutParams(params);
        mMainView.addView(view, 1);
    }
    
    // ================================================================================
    // Print settings controls functions
    // ================================================================================
    
    /**
     * @brief Get the options strings.
     * The strings ids are updated based on constraints
     * 
     * @param name Print settings tag
     * @param options Options list
     * 
     * @return Option strings
     */
    private Object[] getOptionsStrings(String name, List<Option> options) {
        ArrayList<String> optionsStrings = new ArrayList<String>();
        
        for (Option option : options) {
            String value = option.getTextContent();
            
            int id = AppUtils.getResourseId(value, R.string.class, -1);
            
            if (id != -1) {
                optionsStrings.add(getResources().getString(id));
            } else {
                optionsStrings.add(Integer.toString(id));
            }
        }
        
        return optionsStrings.toArray();
    }
    
    /**
     * @brief Add a Print Setting View to a Layout
     * 
     * @param ll Layout to add the Print Setting
     * @param setting Setting information to be added
     * @param withSeparator Should add a separator
     */
    private void addSettingsItemView(LinearLayout ll, Setting setting, boolean withSeparator) {
        String name = setting.getAttributeValue(XmlNode.ATTR_NAME);
        String icon = setting.getAttributeValue(XmlNode.ATTR_ICON);
        String text = setting.getAttributeValue(XmlNode.ATTR_TEXT);
        String type = setting.getAttributeValue(XmlNode.ATTR_TYPE);
        
        String titleText = "";
        int titleId = AppUtils.getResourseId(text, R.string.class, -1);
        if (titleId != -1) {
            titleText = getResources().getString(titleId);
        }
        
        int iconId = AppUtils.getResourseId(icon, R.drawable.class, -1);
        
        View view = createControlView(type, name);
        
        LinearLayout item = createItem(titleText, true, iconId, withSeparator, view);
        item.setId(R.id.view_id_show_subview_container);
        
        if (type.equalsIgnoreCase(Setting.ATTR_VAL_LIST)) {
            view.setId(R.id.view_id_disclosure);
            item.setTag(name);
            item.setTag(ID_TAG_ICON, icon);
            item.setTag(ID_TAG_TEXT, text);
            item.setTag(ID_TAG_SETTING, setting);
            item.setTag(ID_TAG_TARGET_VIEW, item);
            item.setOnClickListener(this);
        } else {
            view.setTag(ID_TAG_TARGET_VIEW, item);
        }
        
        ll.addView(item);
    }
    
    /**
     * @brief Add a Print Setting Title to the Print Settings Title Layout
     * 
     * @param group Information about the group to be added
     */
    private void addSettingsTitleView(Group group) {
        String nameStr = group.getAttributeValue(XmlNode.ATTR_NAME);
        String textStr = group.getAttributeValue(XmlNode.ATTR_TEXT);
        
        LinearLayout itemsGroup = new LinearLayout(getContext());
        itemsGroup.setOrientation(LinearLayout.VERTICAL);
        
        for (Setting setting : group.getSettings()) {
            int index = group.getSettings().indexOf(setting);
            int size = group.getSettings().size();
            
            addSettingsItemView(itemsGroup, setting, index != size - 1);
        }
        
        // Create Header
        String titleText = "";
        int id = AppUtils.getResourseId(textStr, R.string.class, -1);
        if (id != -1) {
            titleText = getResources().getString(id);
        }
        
        LinearLayout title = createTitle(titleText, true, R.drawable.selector_printsettings_collapsible, false, false);
        title.setId(R.id.view_id_collapse_container);
        title.setTag(ID_COLLAPSE_TARGET_GROUP, itemsGroup);
        title.setTag(nameStr);
        title.setOnClickListener(this);
        expandControl(title, false); // initially expand

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) title.getLayoutParams();
        int marginTop = getResources().getDimensionPixelSize(R.dimen.separator_size);
        params.setMargins(0, marginTop, 0, 0);
        title.setLayoutParams(params);
        
        mPrintSettingsTitles.add(title); // Add to controls list
        mPrintSettingsLayout.addView(title);
        
        params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        itemsGroup.setLayoutParams(params);
        mPrintSettingsLayout.addView(itemsGroup);
    }
    
    /**
     * @brief Create the Print Setting Controls
     */
    private void initializePrintSettingsControls() {
       if(mPrintSettings == null){
            return;
        }
        mPrintSettingsTitles = new ArrayList<LinearLayout>();

        if (PrintSettings.sGroupListMap.get(mPrintSettings.getSettingMapKey()) != null) {
            for (Group group : PrintSettings.sGroupListMap.get(mPrintSettings.getSettingMapKey())) {
                addSettingsTitleView(group);
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
    private void addAuthenticationItemView(LinearLayout ll, String titleText, View view, String tag, boolean withSeparator) {
        LinearLayout item = createItem(titleText, true, R.drawable.temp_img_printsettings_null, withSeparator, view);
        item.setId(R.id.view_id_show_subview_container);
        
        item.setTag(tag);
        
        ll.addView(item);
    }
    
    /**
     * @brief Create the authentication setting view
     */
    private void initializeAuthenticationSettingsView() {
        LinearLayout itemsGroup = new LinearLayout(getContext());
        itemsGroup.setOrientation(LinearLayout.VERTICAL);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.0f);
        params.gravity = Gravity.CENTER_VERTICAL;
        int margin = getResources().getDimensionPixelSize(R.dimen.printsettings_icon_setting_padding);
        params.leftMargin = margin;
        params.rightMargin = margin;
        
        Switch switchView = (Switch) LayoutInflater.from(getContext()).inflate(R.layout.printsettings_switch, null);
        switchView.setLayoutParams(params);
        switchView.setId(R.id.view_id_secure_print_switch);
        switchView.setOnCheckedChangeListener(this);
            
        String titleText = getResources().getString(R.string.ids_lbl_secure_print);
        addAuthenticationItemView(itemsGroup, titleText, switchView, KEY_TAG_SECURE_PRINT, true);
        
        params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.0f);
        params.gravity = Gravity.CENTER_VERTICAL;
        int width = getResources().getDimensionPixelSize(R.dimen.printsettings_input_pincode_width);
        params.width = width;
        params.leftMargin = margin;
        params.rightMargin = margin;
        
        EditText editText = (EditText) LayoutInflater.from(getContext()).inflate(R.layout.printsettings_input_edittext, null);
        editText.setId(R.id.view_id_pin_code_edit_text);
        editText.setLayoutParams(params);
        
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        editText.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(AppConstants.CONST_PIN_CODE_LIMIT)
        });
        
        editText.addTextChangedListener(new PinCodeTextWatcher());

        titleText = getResources().getString(R.string.ids_lbl_pin_code);
        addAuthenticationItemView(itemsGroup, titleText, editText, KEY_TAG_PIN_CODE, false);
        
        // Create Header
        titleText = getResources().getString(R.string.ids_lbl_authentication);
        
        LinearLayout title = createTitle(titleText, true, R.drawable.selector_printsettings_collapsible, false, false);
        title.setId(R.id.view_id_collapse_container);
        title.setTag(ID_COLLAPSE_TARGET_GROUP, itemsGroup);
        title.setTag(KEY_TAG_AUTHENTICATION);
        title.setOnClickListener(this);
        expandControl(title, false); // initially expand

        params = (LinearLayout.LayoutParams) title.getLayoutParams();
        int marginTop = getResources().getDimensionPixelSize(R.dimen.separator_size);
        params.setMargins(0, marginTop, 0, 0);
        title.setLayoutParams(params);
        
        mPrintSettingsTitles.add(title); // Add to controls list
        mPrintSettingsLayout.addView(title);
        
        params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        itemsGroup.setLayoutParams(params);
        mPrintSettingsLayout.addView(itemsGroup);
    }

    /**
     * @brief Initialize the authentication setting values
     *
     * @param shouldClear true if the authentication values must be cleared
     */
    private void initializeAuthenticationValues(boolean shouldClear) {
        EditText pinCodeEditText = (EditText) mMainView.findViewById(R.id.view_id_pin_code_edit_text);
        Switch securePrintSwitch = (Switch) mMainView.findViewById(R.id.view_id_secure_print_switch);

        if (shouldClear) {
            pinCodeEditText.setText("");
            securePrintSwitch.setChecked(false);
            setSecurePrintEnabled(false);
        } else {
            // Fix for BTS23939 (retain if DefaultPrintSettings is re-opened, shared prefs is always cleared when app is opened)
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            boolean isSecurePrint = prefs.getBoolean(AppConstants.PREF_KEY_AUTH_SECURE_PRINT, AppConstants.PREF_DEFAULT_AUTH_SECURE_PRINT);
            String pin = prefs.getString(AppConstants.PREF_KEY_AUTH_PIN_CODE, AppConstants.PREF_DEFAULT_AUTH_PIN_CODE);

            if (isSecurePrint) {
                pinCodeEditText.setText(pin);
            }
            securePrintSwitch.setChecked(isSecurePrint);
            setSecurePrintEnabled(isSecurePrint);
        }
    }
    
    // ================================================================================
    // Sub-view value Controls
    // ================================================================================

    /**
     * @brief Animates display of sub-view
     */
    private void animateDisplaySubview() {
        int width = getWidth();
        
        int duration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        AccelerateInterpolator interpolator = new AccelerateInterpolator();
        
        TranslateAnimation animIn = new TranslateAnimation(width, 0, 0, 0);
        animIn.setDuration(duration);
        animIn.setInterpolator(interpolator);
        animIn.setFillAfter(true);
        mSubView.startAnimation(animIn);
        
        TranslateAnimation animOut = new TranslateAnimation(0, -width, 0, 0);
        animOut.setDuration(duration);
        animOut.setInterpolator(interpolator);
        animOut.setFillAfter(true);
        
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
            
            @Override
            public void onAnimationEnd(Animation animation) {
                Message newMessage = Message.obtain(mHandler, MSG_SLIDE_IN);
                mHandler.sendMessage(newMessage);
            }
        });
        
        mMainView.startAnimation(animOut);
    }
    
    /**
     * @brief Animates dismiss of sub-view
     */
    private void animateDismissSubview() {
        int width = getWidth();
        
        int duration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        AccelerateInterpolator interpolator = new AccelerateInterpolator();
        
        TranslateAnimation animIn = new TranslateAnimation(-width, 0, 0, 0);
        animIn.setDuration(duration);
        animIn.setInterpolator(interpolator);
        animIn.setFillAfter(true);
        mMainView.startAnimation(animIn);
        
        TranslateAnimation animOut = new TranslateAnimation(0, width, 0, 0);
        animOut.setDuration(duration);
        animOut.setInterpolator(interpolator);
        animOut.setFillAfter(true);
        
        animOut.setAnimationListener(new Animation.AnimationListener() {
            /** {@inheritDoc} */
            @Override
            public void onAnimationStart(Animation animation) {
            }
            
            /** {@inheritDoc} */            
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
            
            /** {@inheritDoc} */
            @Override
            public void onAnimationEnd(Animation animation) {
                Message newMessage = Message.obtain(mHandler, MSG_SLIDE_OUT);
                mHandler.sendMessage(newMessage);
            }
        });
        
        mSubView.startAnimation(animOut);
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
    private void addSubviewOptionsList(String str, int value, int tagValue, boolean withSeparator, int itemId) {
        addSubviewOptionsList(str, null, value, tagValue, withSeparator, itemId, -1);
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
    private void addSubviewOptionsList(String str, String sub, int value, int tagValue, boolean withSeparator, int itemId, int iconId) {
        
        // Add disclosure
        LayoutInflater li = LayoutInflater.from(getContext());
        View view = li.inflate(R.layout.printsettings_radiobutton, null);
        view.setId(R.id.view_id_subview_status);
        if (value == tagValue) {
            view.setSelected(true);
        }
        
        int size = getResources().getDimensionPixelSize(R.dimen.printsettings_radiobutton_size);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size, 0.0f);
        params.gravity = Gravity.CENTER_VERTICAL;
        
        int margin = getResources().getDimensionPixelSize(R.dimen.printsettings_icon_setting_padding);
        params.leftMargin = margin;
        params.rightMargin = margin;
        
        view.setLayoutParams(params);
        
        // TODO: iconID
        LinearLayout item = createItem(str, sub, true, iconId, withSeparator, view);
        item.setId(itemId);
        item.setTag(Integer.valueOf(tagValue));
        item.setOnClickListener(this);
        
        mSubOptionsLayout.addView(item);
    }
    
    /**
     * @brief Add sub-view options title
     * 
     * @param str Main text
     * @param showIcon Should show icon
     * @param iconId Icon resource ID
     */
    private void addSubviewOptionsTitle(String str, boolean showIcon, int iconId) {
        LinearLayout title = createTitle(str, showIcon, iconId, true, false);
        title.setId(R.id.view_id_hide_subview_container);
        title.setOnClickListener(this);
        
        mSubView.addView(title);
    }
    
    /**
     * @brief Create sub-view upon selecting a print setting
     * 
     * @param v View to create subview from
     */
    private void createSubview(View v) {
        mSubView.setTag(v.getTag());
        
        if (v.getTag().toString().equals(KEY_TAG_PRINTER)) {
            String title = getResources().getString(R.string.ids_lbl_printer);
            addSubviewOptionsTitle(title, false, -1);
            
            if (mPrintersList != null) {
                for (int i = 0; i < mPrintersList.size(); i++) {
                    Printer printer = mPrintersList.get(i);
                    
                    boolean showSeparator = (i != mPrintersList.size() - 1);
                    String printerName = printer.getName();
                    if (printerName == null || printerName.isEmpty()) {
                        printerName = getContext().getResources().getString(R.string.ids_lbl_no_name);
                    }
                    addSubviewOptionsList(printerName, printer.getIpAddress(), mPrinterId, printer.getId(), showSeparator, R.id.view_id_subview_printer_item,
                            R.drawable.img_btn_printer_status_offline);
                }
            }
        } else {
            mSubView.setTag(ID_TAG_TEXT, v.getTag(ID_TAG_TEXT));
            mSubView.setTag(ID_TAG_ICON, v.getTag(ID_TAG_ICON));
            
            Setting setting = (Setting) v.getTag(ID_TAG_SETTING);
            Object[] options = getOptionsStrings((String) v.getTag(), setting.getOptions());
            mSubView.setTag(ID_TAG_SUB_OPTIONS, options);
            
            String name = (String) v.getTag();
            String text = (String) v.getTag(ID_TAG_TEXT);
            String icon = (String) v.getTag(ID_TAG_ICON);
            
            String titleText = "";
            int titleId = AppUtils.getResourseId(text, R.string.class, -1);
            if (titleId != -1) {
                titleText = getResources().getString(titleId);
            }
            
            int iconId = AppUtils.getResourseId(icon, R.drawable.class, -1);
            
            addSubviewOptionsTitle(titleText, true, iconId);
            
            int value = mPrintSettings.getValue(name);
            
            int lastIdx = -1;
            for (int i = 0; i < options.length; i++) {
                if (shouldDisplayOptionFromConstraints(name, i) && shouldDisplayOptionFromPrinter(name, i)) {
                    addSubviewOptionsList(options[i].toString(), value, i, true, R.id.view_id_subview_option_item);
                    lastIdx = i;
                }
            }
            
            // hide the separator of the last item added
            if (mSubOptionsLayout.findViewWithTag(lastIdx) != null) {
                View container = mSubOptionsLayout.findViewWithTag(lastIdx);
                container.findViewById(R.id.menuSeparator).setVisibility(View.GONE);
            }
            
        }
    }
    
    /**
     * @brief Display the options sub-view
     * 
     * @param v View to create sub-view from
     * @param animate Should animate display
     */
    private void displayOptionsSubview(View v, boolean animate) {
        if (mSubOptionsLayout == null) {
            mSubOptionsLayout = new LinearLayout(getContext());
            mSubOptionsLayout.setOrientation(LinearLayout.VERTICAL);
        } else {
            mSubOptionsLayout.removeAllViews();
        }
        
        if (mSubScrollView == null) {
            mSubScrollView = new ScrollView(getContext());
        }
        if (mSubScrollView.getChildCount() == 0) {
            mSubScrollView.addView(mSubOptionsLayout, new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
        
        if (mSubView == null) {
            mSubView = new LinearLayout(getContext());
            mSubView.setOrientation(LinearLayout.VERTICAL);
        } else {
            mSubView.removeAllViews();
        }
        
        createSubview(v);
        
        mSubView.addView(mSubScrollView, new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        
        if (mSubView.getParent() == null) {
            addView(mSubView, new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
        
        mMainView.setEnabled(false);
        mSubView.setEnabled(true);
        if (animate) {
            animateDisplaySubview();
        } else {
            mMainView.setVisibility(View.GONE);
        }
        
        // AppUtils.changeChildrenFont(mSubView, SmartDeviceApp.getAppFont());
    }
    
    /**
     * @brief Dismiss the options sub-view
     * 
     * @param animate Should animate dismiss
     */
    private void dismissOptionsSubview(boolean animate) {
        mMainView.setEnabled(true);
        mMainView.setVisibility(View.VISIBLE);
        mSubView.setEnabled(false);
        if (animate) {
            animateDismissSubview();
        } else {
            removeView(mSubView);
        }
    }
    
    /**
     * @brief Sub-view option item is clicked
     * 
     * @param v View clicked
     */
    private void subviewOptionsItemClicked(View v) {
        int id = (Integer) v.getTag();
        
        if (v.getId() == R.id.view_id_subview_option_item) {
            if (updateValue((String) mSubView.getTag(), id)) {
                if (mListener != null) {
                    mListener.onPrintSettingsValueChanged(mPrintSettings);
                }
                
                // Update UI
                updateDisplayedValue((String) mSubView.getTag());
                
                Object[] options = (Object[]) mSubView.getTag(ID_TAG_SUB_OPTIONS);
                for (int i = 0; i < options.length; i++) {
                    View view = mSubView.findViewWithTag(Integer.valueOf(i));
                    // Some views may be hidden
                    if (view != null) {
                        View subView = view.findViewById(R.id.view_id_subview_status);
                        subView.setSelected(id == i);
                    }
                }
            }
        } else if (v.getId() == R.id.view_id_subview_printer_item) {
            if (mPrinterId != id) {
                // TODO: get new printer settings
                setPrintSettings(new PrintSettings(id,  PrinterManager.getInstance(SmartDeviceApp.getAppContext()).getPrinterType(id)));
                setPrinterId(id);

                if (mListener != null) {
                    mListener.onPrinterIdSelectedChanged(mPrinterId);
                    mListener.onPrintSettingsValueChanged(mPrintSettings);
                }
                
                for (int i = 0; i < mPrintersList.size(); i++) {
                    Printer printer = mPrintersList.get(i);
                    View view = mSubView.findViewWithTag(Integer.valueOf(printer.getId()));
                    View subView = view.findViewById(R.id.view_id_subview_status);
                    subView.setSelected(mPrinterId == printer.getId());
                }
            }
            
        }
    }
    
    /**
     * @brief Execute Print
     */
    private void executePrint() {
        if (mListener != null) {
            mListener.onPrint(getPrinterFromList(mPrinterId), mPrintSettings);
        }
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
     * @param showDisclosure Show Disclosure
     */
    private LinearLayout createTitle(String text, boolean showIcon, int iconId, boolean showBackButton, boolean showDisclosure) {
        LayoutInflater li = LayoutInflater.from(getContext());
        LinearLayout title = (LinearLayout) li.inflate(R.layout.printsettings_container_title, null);
        
        TextView textView = (TextView) title.findViewById(R.id.menuTextView);
        textView.setText(text);
        
        int height = getResources().getDimensionPixelSize(R.dimen.home_menu_height);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, height);
        title.setLayoutParams(params);
        
        // Set icon
        ImageView imgView = (ImageView) title.findViewById(R.id.menuIcon);
        if (showIcon) {
            imgView.setVisibility(View.VISIBLE);
        }
        if (iconId != -1) {
            imgView.setImageResource(iconId);
        }
        
        imgView = (ImageView) title.findViewById(R.id.returnButtonIndicator);
        if (showBackButton) {
            imgView.setVisibility(View.VISIBLE);
        }
        
        imgView = (ImageView) title.findViewById(R.id.disclosureView);
        if (showDisclosure) {
            imgView.setVisibility(View.VISIBLE);
        }
        
        return title;
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
    private LinearLayout createItem(String text, boolean showIcon, int iconId, boolean showSeparator, View view) {
        return createItem(text, null, showIcon, iconId, showSeparator, view);
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
    private LinearLayout createItem(String text, String subText, boolean showIcon, int iconId, boolean showSeparator, View view) {
        LayoutInflater li = LayoutInflater.from(getContext());
        
        int layoutId = R.layout.printsettings_container_item;
        if (subText != null) {
            layoutId = R.layout.printsettings_container_item_with_sub;
        }
        LinearLayout item = (LinearLayout) li.inflate(layoutId, null);
        item.setActivated(true);
        
        TextView textView = (TextView) item.findViewById(R.id.menuTextView);
        textView.setText(text);
        
        if (subText != null) {
            textView = (TextView) item.findViewById(R.id.subTextView);
            textView.setText(subText);
        }
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        item.setLayoutParams(params);
        
        // Set icon
        ImageView imgView = (ImageView) item.findViewById(R.id.menuIcon);
        if (showIcon) {
            imgView.setVisibility(View.VISIBLE);
        }
        if (iconId != -1) {
            imgView.setImageResource(iconId);
        }
        
        // hide separator
        item.findViewById(R.id.menuSeparator).setVisibility(View.VISIBLE);
        if (!showSeparator) {
            item.findViewById(R.id.menuSeparator).setVisibility(View.GONE);
        }
        
        LinearLayout extraView = (LinearLayout) item.findViewById(R.id.menuContainer);
        extraView.addView(view);
        
        return item;
    }
    
    /**
     * @brief  Create control view
     * 
     * @param type type (boolean/numeric/list)
     * @param tag Identifying tag
     * 
     * @return Return created view
     */
    private View createControlView(String type, String tag) {
        LayoutInflater li = LayoutInflater.from(getContext());
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.0f);
        params.gravity = Gravity.CENTER_VERTICAL;
        
        if (type.equalsIgnoreCase(Setting.ATTR_VAL_BOOLEAN)) {
            int margin = getResources().getDimensionPixelSize(R.dimen.printsettings_icon_setting_padding);
            params.leftMargin = margin;
            params.rightMargin = margin;
            
            Switch switchView = (Switch) li.inflate(R.layout.printsettings_switch, null);
            switchView.setOnCheckedChangeListener(this);
            switchView.setTag(tag);
            switchView.setLayoutParams(params);
            return switchView;
        } else if (type.equalsIgnoreCase(Setting.ATTR_VAL_NUMERIC)) {
            int margin = getResources().getDimensionPixelSize(R.dimen.printsettings_icon_setting_padding);
            params.leftMargin = margin;
            params.rightMargin = margin;
            
            int width = getResources().getDimensionPixelSize(R.dimen.printsettings_input_width);
            params.width = width;
            
            EditText editText = (EditText) li.inflate(R.layout.printsettings_input_edittext, null);
            editText.setActivated(true);
            editText.setLayoutParams(params);
            editText.setTag(tag);
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setFilters(new InputFilter[] {
                    new InputFilter.LengthFilter(AppConstants.CONST_COPIES_LIMIT)
            });
            editText.addTextChangedListener(new EditTextWatcher(tag, 1));
            editText.setOnEditorActionListener(this);
            
            return editText;
        } else if (type.equalsIgnoreCase(Setting.ATTR_VAL_LIST)) {
            params.height = LayoutParams.MATCH_PARENT;
            
            int width = getResources().getDimensionPixelSize(R.dimen.printsettings_list_value_width);
            params.width = width;
            
            View view = li.inflate(R.layout.printsettings_value_disclosure, null);
            view.setActivated(true);
            view.setLayoutParams(params);
            return view;
        }
        
        return null;
    }
    
    // ================================================================================
    // Collapsible Controls
    // ================================================================================
    
    /**
     * @brief Animate expand
     * 
     * @param v View with collapse controls
     */
    private void animateExpand(View v) {
        if (v == null) {
            return;
        }
        
        int idx = mPrintSettingsTitles.indexOf(v);
        if (idx == -1) {
            return;
        }
        
        float durationMultiplier = 0.2f;
        int menuHeight = getResources().getDimensionPixelSize(R.dimen.home_menu_height);
        int totalHeight = 0;
        
        ViewGroup target = (ViewGroup) v.getTag(ID_COLLAPSE_TARGET_GROUP);
        for (int i = 0; i < target.getChildCount(); i++) {
            View child = target.getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }
            
            int height = menuHeight;
            if (i != target.getChildCount() - 1) {
                height += getResources().getDimensionPixelSize(R.dimen.separator_size);
            }
            totalHeight += height;
            
            TranslateAnimation animate = new TranslateAnimation(0, 0, -totalHeight, 0);
            animate.setDuration((int) (totalHeight * durationMultiplier));
            
            // final child will end animation
            if (i == target.getChildCount() - 1) {
                final int viewIdx = idx;
                animate.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }
                    
                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                    
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        Message newMessage = Message.obtain(mHandler, MSG_EXPAND);
                        newMessage.arg1 = viewIdx;
                        mHandler.sendMessage(newMessage);
                    }
                });
            }
            
            child.clearAnimation();
            child.startAnimation(animate);
        }
        
        for (int i = idx + 1; i < mPrintSettingsTitles.size(); i++) {
            TranslateAnimation animate = new TranslateAnimation(0, 0, -totalHeight, 0);
            animate.setStartOffset((int) (menuHeight * durationMultiplier));
            animate.setDuration((int) (totalHeight * durationMultiplier));
            
            mPrintSettingsTitles.get(i).clearAnimation();
            mPrintSettingsTitles.get(i).startAnimation(animate);
            
            View childTarget = (View) mPrintSettingsTitles.get(i).getTag(ID_COLLAPSE_TARGET_GROUP);
            if (childTarget.getVisibility() != View.GONE) {
                childTarget.clearAnimation();
                childTarget.startAnimation(animate);
            }
        }
    }
    
    /**
     * @brief Animate collapse
     * 
     * @param v View with collapse controls
     */
    private void animateCollapse(final View v) {
        if (v == null) {
            return;
        }
        
        int idx = mPrintSettingsTitles.indexOf(v);
        if (idx == -1) {
            return;
        }
        
        final ViewGroup target = (ViewGroup) v.getTag(ID_COLLAPSE_TARGET_GROUP);
        
        float durationMultiplier = 0.2f;
        int menuHeight = getResources().getDimensionPixelSize(R.dimen.home_menu_height);
        int totalHeight = target.getHeight();
        
        int currentHeight = 0;
        for (int i = 0; i < target.getChildCount(); i++) {
            View child = target.getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }
            
            int height = menuHeight;
            if (i != target.getChildCount() - 1) {
                height += getResources().getDimensionPixelSize(R.dimen.separator_size);
            }
            currentHeight += height;
            
            TranslateAnimation animate = new TranslateAnimation(0, 0, 0, -currentHeight);
            animate.setDuration((int) (currentHeight * durationMultiplier));
            animate.setFillAfter(true);
            
            if (i == target.getChildCount() - 1) {
                final int viewIdx = idx;
                animate.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }
                    
                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                    
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        Message newMessage = Message.obtain(mHandler, MSG_COLLAPSE);
                        newMessage.arg1 = viewIdx;
                        mHandler.sendMessage(newMessage);
                    }
                });
            }
            
            child.clearAnimation();
            child.startAnimation(animate);
        }
        
        for (int i = idx + 1; i < mPrintSettingsTitles.size(); i++) {
            TranslateAnimation animate = new TranslateAnimation(0, 0, 0, -totalHeight);
            animate.setDuration((int) (totalHeight * durationMultiplier));
            
            mPrintSettingsTitles.get(i).clearAnimation();
            mPrintSettingsTitles.get(i).startAnimation(animate);
            
            View childTarget = (View) mPrintSettingsTitles.get(i).getTag(ID_COLLAPSE_TARGET_GROUP);
            if (childTarget.getVisibility() != View.GONE) {
                childTarget.clearAnimation();
                childTarget.startAnimation(animate);
            }
        }
    }
    
    /**
     * @brief Collapse a control
     * 
     * @param v View with collapse controls
     * @param animate Should animate collapse
     */
    private void collapseControl(View v, boolean animate) {
        View target = (View) v.getTag(ID_COLLAPSE_TARGET_GROUP);
        if (animate) {
            animateCollapse(v);
        } else {
            v.setSelected(true);
            target.setVisibility(View.GONE);
        }
    }
    
    /**
     * @brief Expand a control
     * 
     * @param v View with collapse controls
     * @param animate Should animate expand
     */
    private void expandControl(View v, boolean animate) {
        View target = (View) v.getTag(ID_COLLAPSE_TARGET_GROUP);
        target.setVisibility(View.VISIBLE);
        if (animate) {
            animateExpand(v);
        } else {
            v.setSelected(false);
        }
    }
    
    /**
     * @brief Toggle collapse
     * 
     * @param v View to toggle
     */
    private void toggleCollapse(View v) {
        v.setClickable(false);
        if (!v.isSelected()) {
            collapseControl(v, true);
        } else {
            expandControl(v, true);
        }
    }
    
    /**
     * @brief Sets the secure print value. The UI and the persistent setting is updated
     * 
     * @param isEnabled Secure Print is enabled.
     */
    private void setSecurePrintEnabled(boolean isEnabled) {
        mMainView.findViewWithTag(KEY_TAG_PIN_CODE).setActivated(isEnabled);
        
        EditText editText = (EditText) mMainView.findViewById(R.id.view_id_pin_code_edit_text);
        editText.setEnabled(isEnabled);
        editText.setActivated(isEnabled);
        //mMainView.findViewById(R.id.view_id_pin_code_edit_text).setActivated(isEnabled ? View.VISIBLE : View.INVISIBLE);
        //mMainView.findViewById(R.id.view_id_pin_code_edit_text).setVisibility(isEnabled ? View.VISIBLE : View.INVISIBLE);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean(AppConstants.PREF_KEY_AUTH_SECURE_PRINT, isEnabled);
        editor.apply();
        
        if (isEnabled) {
            editText.setText(prefs.getString(AppConstants.PREF_KEY_AUTH_PIN_CODE, AppConstants.PREF_DEFAULT_AUTH_PIN_CODE));
        } else {
            editText.setText("");
        }
    }
    
    /**
     * @brief Checks the value of a TextView and sets it to 1 if text is
     * empty or less than or equal to 0
     * 
     * @param v TextView to be edited
     */
    private void checkEditTextValue(TextView v) {
        if (v.getInputType() == InputType.TYPE_CLASS_NUMBER) {
            String value = v.getText().toString();
            if (value.isEmpty()) {
                v.setText("1");
            } else {
                int val = Integer.parseInt(value);
                if (val <= 0) {
                    v.setText("1");
                }
            }
        }
    }
    
    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_id_collapse_container:
                toggleCollapse(v);
                break;
            case R.id.view_id_show_subview_container:
                if (mMainView.isEnabled()) {
                    displayOptionsSubview(v, true);
                }
                break;
            case R.id.view_id_hide_subview_container:
                if (mSubView.isEnabled()) {
                    dismissOptionsSubview(true);
                    mPrinterManager.createUpdateStatusThread();
                }
                break;
            case R.id.view_id_subview_option_item:
            case R.id.view_id_subview_printer_item:
                subviewOptionsItemClicked(v);
                break;
            case R.id.view_id_print_selected_printer:
                if (mMainView.isEnabled()) {
                    displayOptionsSubview(v, true);
                    mPrinterManager.createUpdateStatusThread();
                }
                break;
            case R.id.view_id_print_header:
                executePrint();
                break;
        }
    }
    
    // ================================================================================
    // INTERFACE - Callback
    // ================================================================================
    
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_COLLAPSE:
                View view = (View) mPrintSettingsTitles.get(msg.arg1).getTag(ID_COLLAPSE_TARGET_GROUP);
                mPrintSettingsTitles.get(msg.arg1).setSelected(true);
                mPrintSettingsTitles.get(msg.arg1).setClickable(true);
                view.setVisibility(View.GONE);
                return true;
            case MSG_EXPAND:
                mPrintSettingsTitles.get(msg.arg1).setSelected(false);
                mPrintSettingsTitles.get(msg.arg1).setClickable(true);
                return true;
            case MSG_SET_SCROLL:
                mPrintSettingsScrollView.scrollTo(0, msg.arg1);
                return true;
            case MSG_SLIDE_IN:
                mMainView.setVisibility(View.GONE);
                return true;
            case MSG_SLIDE_OUT:
                PrinterManager.getInstance(SmartDeviceApp.getAppContext()).cancelUpdateStatusThread();
                removeView(mSubView);
                return true;
            case MSG_SET_SUB_SCROLL:
                mSubScrollView.scrollTo(0, msg.arg1);
                return true;
            case MSG_SHOW_SUBVIEW:
                displayOptionsSubview(mMainView.findViewWithTag(msg.obj), false);
                if (msg.obj.toString().equals(KEY_TAG_PRINTER)) {
                    mPrinterManager.createUpdateStatusThread();
                }
                return true;
        }
        return false;
    }
    
    // ================================================================================
    // CompoundButton.OnCheckedChangeListener
    // ================================================================================
    
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.view_id_secure_print_switch) {
            setSecurePrintEnabled(isChecked);
            return;
        }
        
        if (updateValue(buttonView.getTag().toString(), isChecked ? 1 : 0)) {
            if (mListener != null) {
                mListener.onPrintSettingsValueChanged(mPrintSettings);
            }
        } else {
            // cancel onCheckedChanged?
        }
    }
    
    // ================================================================================
    // INTERFACE - Callback
    // ================================================================================
    
    @Override
    public void updateOnlineStatus() {
        for (int i = 0; i < mPrintersList.size(); i++) {
            Printer printer = mPrintersList.get(i);
            if (mSubView == null) {
                return;
            }
            View view = mSubView.findViewWithTag(Integer.valueOf(printer.getId()));
            if (view != null) {
                View imageView = view.findViewById(R.id.menuIcon);
                mPrinterManager.updateOnlineStatus(printer.getIpAddress(), imageView);
            }
        }
    }
    
    // ================================================================================
    // INTERFACE - OnEditorActionListener
    // ================================================================================
    
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            checkEditTextValue(v);
            return false;
        }
        return false;
    }
    
    // ================================================================================
    // Internal classes
    // ================================================================================
    
    /**
     * @class EditTextWatcher
     * 
     * @brief Class to manage input on the copies field
     */
    private class EditTextWatcher implements TextWatcher {
        private String mTag;
        private boolean mEditing;
        private int mMinValue;
        
        /**
         * @brief Instantiate EditTextWatcher
         * 
         * @param tag View tag
         * @param minValue Minimum value of the field
         */
        public EditTextWatcher(String tag, int minValue) {
            mTag = tag;
            mEditing = false;
            mMinValue = minValue;
        }
        
        @Override
        public synchronized void afterTextChanged(Editable s) {
            if (!mEditing) {
                mEditing = true;
                String digits = s.toString().replaceAll("\\D", "");
                
                try {
                    String formatted = Integer.toString(Integer.parseInt(digits));
                    s.replace(0, s.length(), formatted);
                } catch (NumberFormatException nfe) {
                }
                
                int value = mMinValue;
                try {
                    value = Integer.parseInt(s.toString());
                    value = Math.max(mMinValue, value);
                } catch (NumberFormatException nfe) {
                }
                
                if (updateValue(mTag, value)) {
                    if (mListener != null) {
                        mListener.onPrintSettingsValueChanged(mPrintSettings);
                    }
                }
                
                mEditing = false;
            }
        }
        
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }

    /**
     * @class PinCodeTextWatcher
     * 
     * @brief Class to manage input on the pincode textfield
     */
    private class PinCodeTextWatcher implements TextWatcher {
        
        @Override
        public synchronized void afterTextChanged(Editable s) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            boolean isSecurePrint = prefs.getBoolean(AppConstants.PREF_KEY_AUTH_SECURE_PRINT, AppConstants.PREF_DEFAULT_AUTH_SECURE_PRINT);
            
            if (isSecurePrint) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(AppConstants.PREF_KEY_AUTH_PIN_CODE, s.toString());
                editor.apply();
            }
        }
        
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        
        /** {@inheritDoc} */
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
        
    }
    
    /**
     * @interface PrintSettingsViewInterface 
     * 
     * @brief Interface for PrintSettingsView Events
     */
    public interface PrintSettingsViewInterface {
        
        /**
         * @brief Printer ID Selected Changed Callback
         * 
         * @param printerId printer ID
         */
        public void onPrinterIdSelectedChanged(int printerId);

        /**
         * @brief Print Settings Value Changed Callback
         * 
         * @param printSettings Print settings
         */
        public void onPrintSettingsValueChanged(PrintSettings printSettings);
        
        /**
         * @brief Print Execution Callback
         * 
         * @param printer Printer object
         * @param printSettings Print settings
         */
        public void onPrint(Printer printer, PrintSettings printSettings);
    }
}
