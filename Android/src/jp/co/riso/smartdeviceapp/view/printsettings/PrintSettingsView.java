/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintSettingsView.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.printsettings;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.model.printsettings.Group;
import jp.co.riso.smartdeviceapp.model.printsettings.Option;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.BookletLayout;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Duplex;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.OutputTray;
import jp.co.riso.smartdeviceapp.model.printsettings.XmlNode;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.FinishingSide;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Imposition;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.ImpositionOrder;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Orientation;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Punch;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Staple;
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings;
import jp.co.riso.smartdeviceapp.model.printsettings.Setting;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

public class PrintSettingsView extends FrameLayout implements View.OnClickListener, Callback, CompoundButton.OnCheckedChangeListener {
    public static final String TAG = "PrintSettingsView";
    
    private static final String KEY_SELECTED_TITLES = "key_selected_titles";
    private static final String KEY_SCROLL_POSITION = "key_scroll_position";
    private static final String KEY_SUBVIEW_DISPLAYED = "key_subview_displayed";
    private static final String KEY_SUB_SCROLL_POSITION = "key_sub_scroll_position";
    
    private static final String KEY_TAG_PRINTER = "key_tag_printer";
    
    private static final int MSG_COLLAPSE = 0;
    private static final int MSG_EXPAND = 1;
    private static final int MSG_SET_SCROLL = 2;
    private static final int MSG_SLIDE_IN = 3;
    private static final int MSG_SLIDE_OUT = 4;
    private static final int MSG_SET_SUB_SCROLL = 5;
    private static final int MSG_SHOW_SUBVIEW = 6;
    
    private static final int ID_COLLAPSE_CONTAINER = 0x11000001;
    private static final int ID_COLLAPSE_TARGET_GROUP = 0x11000002;
    
    private static final int ID_SHOW_SUBVIEW_CONTAINER = 0x11000003;
    private static final int ID_HIDE_SUBVIEW_CONTAINER = 0x11000004;
    
    private static final int ID_SUBVIEW_OPTION_ITEM = 0x11000005;
    private static final int ID_SUBVIEW_STATUS = 0x11000006;
    private static final int ID_SUBVIEW_PRINTER_ITEM = 0x11000007;
    
    private static final int ID_TAG_TEXT = 0x11000008;
    private static final int ID_TAG_ICON = 0x11000009;
    private static final int ID_TAG_OPTIONS = 0x1100000A;
    private static final int ID_TAG_TARGET_VIEW = 0x1100000B;
    
    private static final int ID_PRINT_HEADER = 0x1100000C;
    private static final int ID_PRINT_SELECTED_PRINTER = 0x1100000D;
    
    private static final int ID_DISCLOSURE_VIEW = 0x1100000E;
    
    private boolean mShowPrintControls = false;
    private PrintSettings mPrintSettings = null;
    private int mPrinterId = PrinterManager.EMPTY_ID;
    private List<Printer> mPrintersList = null;
    
    private ScrollView mMainScrollView = null;
    private LinearLayout mMainLayout = null;
    
    private LinearLayout mSubView = null;
    private ScrollView mSubScrollView = null;
    private LinearLayout mSubOptionsLayout = null;
    
    private LinearLayout mPrintControls = null;
    
    private Handler mHandler = null;
    private ArrayList<LinearLayout> mPrintSettingsTitles = null;
    
    private PrintSettingsViewInterface mListener = null;
    
    public PrintSettingsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    public PrintSettingsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public PrintSettingsView(Context context) {
        super(context);
        init();
    }
    
    private void init() {
        if (isInEditMode()) {
            return;
        }
        
        mHandler = new Handler(this);
        loadPrintersList();
        
        initializeMainView();
        initializePrinterControls();
        initializePrintSettingsControls();
    }
    
    private void initializeMainView() {
        mMainLayout = new LinearLayout(getContext());
        mMainLayout.setOrientation(LinearLayout.VERTICAL);
        
        mMainScrollView = new ScrollView(getContext());
        mMainScrollView.addView(mMainLayout, new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        
        addView(mMainScrollView, new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }
    
    private void loadPrintersList() {
        mPrintersList = PrinterManager.getInstance(SmartDeviceApp.getAppContext()).getSavedPrintersList();
    }
    
    // ================================================================================
    // Save/Restore State
    // ================================================================================
    
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
        outState.putInt(KEY_SCROLL_POSITION, mMainScrollView.getScrollY());
        
        // If main scroll is visible, then sub view is displayed
        if (mMainScrollView.getVisibility() != View.VISIBLE) {
            outState.putString(KEY_SUBVIEW_DISPLAYED, mSubView.getTag().toString());
            outState.putInt(KEY_SUB_SCROLL_POSITION, mSubScrollView.getScrollY());
        }
    }
    
    public void restoreState(Bundle savedInstanceState) {
        // Collapse the headers retrieved from the saved bundle
        String selectedTitle[] = savedInstanceState.getStringArray(KEY_SELECTED_TITLES);
        for (int i = 0; i < selectedTitle.length; i++) {
            if (mMainLayout.findViewWithTag(selectedTitle[i]) != null) {
                collapseControl(mMainLayout.findViewWithTag(selectedTitle[i]), false);
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
        
        if (tag.equals(PrintSettings.TAG_BOOKLET_LAYOUT)) {
            boolean isLandscape = (mPrintSettings.getValue(PrintSettings.TAG_ORIENTATION) == Orientation.LANDSCAPE.ordinal());
            switch (BookletLayout.values()[value]) {
                case L_R:
                case R_L:
                    return !isLandscape;
                case T_B:
                    return isLandscape;
            }
        }
        
        if (tag.equals(PrintSettings.TAG_OUTPUT_TRAY)) {
            boolean isBooklet = mPrintSettings.isBooklet();
            boolean isStaple = mPrintSettings.getStaple() != Staple.OFF;
            boolean isPunch = mPrintSettings.getPunch() != Punch.OFF;
            switch (OutputTray.values()[value]) {
                case AUTO:
                    return true;
                case FACEDOWN:
                    return !isPunch && !isBooklet;
                case FACEUP:
                    return !isStaple && !isPunch && !isBooklet;
                case TOP:
                case STACKING:
                    return !isBooklet;
            }
        }
        
        return true;
    }
    
    private int getDefaultValueWithConstraints(String tag) {
        if (PrintSettings.sSettingMap.get(tag) == null) {
            return -1;
        }
        
        int defaultValue = PrintSettings.sSettingMap.get(tag).getDefaultValue();
        
        if (tag.equals(PrintSettings.TAG_FINISHING_SIDE)) {
            if (mPrintSettings.getValue(PrintSettings.TAG_ORIENTATION) == Orientation.LANDSCAPE.ordinal()) {
                defaultValue = FinishingSide.TOP.ordinal();
            }
        }
        
        if (tag.equals(PrintSettings.TAG_BOOKLET_LAYOUT)) {
            if (mPrintSettings.getValue(PrintSettings.TAG_ORIENTATION) == Orientation.LANDSCAPE.ordinal()) {
                defaultValue = BookletLayout.T_B.ordinal();
            }
        }
        
        return defaultValue;
    }
    
    private boolean isViewEnabled(String tag) {
        if (mMainLayout != null) {
            if (mMainLayout.findViewWithTag(tag) != null) {
                return mMainLayout.findViewWithTag(tag).isEnabled();
            }
        }
        
        return false;
    }
    
    private void setViewEnabledWithConstraints(String tag, boolean enabled, boolean hideControl) {
        if (mMainLayout != null) {
            if (mMainLayout.findViewWithTag(tag) != null) {
                View view = mMainLayout.findViewWithTag(tag); 
                view.setEnabled(enabled);
                View targetView = (View) view.getTag(ID_TAG_TARGET_VIEW);
                if (targetView != null) {
                    targetView.setActivated(enabled);
                }
                
                View disclosureView = (View) view.findViewById(ID_DISCLOSURE_VIEW);
                if (disclosureView != null) {
                    if (enabled) {
                        view.findViewById(R.id.disclosureIndicator).setVisibility(View.VISIBLE);
                        disclosureView.setVisibility(View.VISIBLE);
                    } else if (hideControl) {
                        view.findViewById(R.id.disclosureIndicator).setVisibility(View.GONE);
                        disclosureView.setVisibility(View.GONE);
                    } else {
                        view.findViewById(R.id.disclosureIndicator).setVisibility(View.GONE);
                        disclosureView.setVisibility(View.VISIBLE);
                    }
                } else {
                    boolean shouldHideControl = !enabled && hideControl;
                    view.setVisibility(shouldHideControl ? View.GONE : View.VISIBLE);
                }
            }
        }
    }
    
    private void updateValueWithConstraints(String tag, int value) {
        mPrintSettings.setValue(tag, value);
        updateDisplayedValue(tag);
    }
    
    private void applyViewConstraints(String tag) {
        int value = mPrintSettings.getValue(tag);
        
        // Constraint #1 Booklet
        if (tag.equals(PrintSettings.TAG_BOOKLET)) {
            boolean enabled = (value == 0);
            setViewEnabledWithConstraints(PrintSettings.TAG_DUPLEX, enabled, false);
            setViewEnabledWithConstraints(PrintSettings.TAG_FINISHING_SIDE, enabled, true);
            setViewEnabledWithConstraints(PrintSettings.TAG_STAPLE, enabled, true);
            setViewEnabledWithConstraints(PrintSettings.TAG_PUNCH, enabled, true);
            setViewEnabledWithConstraints(PrintSettings.TAG_IMPOSITION, enabled, true);
            setViewEnabledWithConstraints(PrintSettings.TAG_IMPOSITION_ORDER, enabled, true);
            
            setViewEnabledWithConstraints(PrintSettings.TAG_BOOKLET_FINISH, !enabled, true);
            setViewEnabledWithConstraints(PrintSettings.TAG_BOOKLET_LAYOUT, !enabled, true);
            
            applyViewConstraints(PrintSettings.TAG_IMPOSITION);
        }
        
        // Constraint #5 Imposition
        if (tag.equals(PrintSettings.TAG_IMPOSITION)) {
            boolean enabled = (value != Imposition.OFF.ordinal()) && isViewEnabled(tag);
            setViewEnabledWithConstraints(PrintSettings.TAG_IMPOSITION_ORDER, enabled, true);
        }
    }
    
    private void applyValueConstraints(String tag, int prevValue) {
        int value = mPrintSettings.getValue(tag);
        
        // Constraint #1 Booklet
        if (tag.equals(PrintSettings.TAG_BOOKLET)) {
            updateValueWithConstraints(PrintSettings.TAG_DUPLEX,
                    getDefaultValueWithConstraints(PrintSettings.TAG_DUPLEX));
            updateValueWithConstraints(PrintSettings.TAG_FINISHING_SIDE,
                    getDefaultValueWithConstraints(PrintSettings.TAG_FINISHING_SIDE));
            updateValueWithConstraints(PrintSettings.TAG_STAPLE,
                    getDefaultValueWithConstraints(PrintSettings.TAG_STAPLE));
            updateValueWithConstraints(PrintSettings.TAG_PUNCH,
                    getDefaultValueWithConstraints(PrintSettings.TAG_PUNCH));
            
            updateValueWithConstraints(PrintSettings.TAG_BOOKLET_FINISH,
                    getDefaultValueWithConstraints(PrintSettings.TAG_BOOKLET_FINISH));
            updateValueWithConstraints(PrintSettings.TAG_BOOKLET_LAYOUT,
                    getDefaultValueWithConstraints(PrintSettings.TAG_BOOKLET_LAYOUT));
            
            updateValueWithConstraints(PrintSettings.TAG_OUTPUT_TRAY,
                    getDefaultValueWithConstraints(PrintSettings.TAG_OUTPUT_TRAY));
            
            if (mPrintSettings.isBooklet()) {
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
        
        // Constraint #3 Finishing Side or Orientation 
        if (tag.equals(PrintSettings.TAG_ORIENTATION) || tag.equals(PrintSettings.TAG_FINISHING_SIDE)) {
            int finishValue = mPrintSettings.getFinishingSide().ordinal();
            int finishDefault = getDefaultValueWithConstraints(PrintSettings.TAG_FINISHING_SIDE);
            if (mPrintSettings.getValue(PrintSettings.TAG_PUNCH) == Punch.HOLES_4.ordinal()) {
                if (finishValue != finishDefault) {
                    if (finishDefault != FinishingSide.LEFT.ordinal() || finishValue != FinishingSide.RIGHT.ordinal()) {
                        updateValueWithConstraints(PrintSettings.TAG_PUNCH, Punch.OFF.ordinal());
                    }
                }
            }
        }
        
        // Constraint #4 Punch
        if (tag.equals(PrintSettings.TAG_PUNCH)) {
            if (value == Punch.HOLES_4.ordinal()) {
                int finishValue = mPrintSettings.getFinishingSide().ordinal();
                int finishDefault = getDefaultValueWithConstraints(PrintSettings.TAG_FINISHING_SIDE);
                if (finishValue != finishDefault) {
                    if (finishDefault != FinishingSide.LEFT.ordinal() || finishValue != FinishingSide.RIGHT.ordinal()) {
                        updateValueWithConstraints(PrintSettings.TAG_FINISHING_SIDE, finishDefault);
                    }
                }
            }
            
            if (value != Punch.OFF.ordinal()) {
                int outputTrayValue = mPrintSettings.getValue(PrintSettings.TAG_OUTPUT_TRAY);
                if (outputTrayValue == OutputTray.FACEDOWN.ordinal() || outputTrayValue == OutputTray.FACEUP.ordinal()) {
                    updateValueWithConstraints(PrintSettings.TAG_OUTPUT_TRAY,
                            getDefaultValueWithConstraints(PrintSettings.TAG_OUTPUT_TRAY));
                }
            }
        }
        
        // Constraint #5 Imposition
        if (tag.equals(PrintSettings.TAG_IMPOSITION)) {
            if (value == Imposition.OFF.ordinal()) {
                updateValueWithConstraints(PrintSettings.TAG_IMPOSITION_ORDER, ImpositionOrder.L_R.ordinal());
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

        // Constraint #6 Orientation
        if (tag.equals(PrintSettings.TAG_ORIENTATION)) {
            int layoutValue = mPrintSettings.getBookletLayout().ordinal();
            
            if (value == Orientation.PORTRAIT.ordinal()) {
                if (layoutValue == BookletLayout.T_B.ordinal()) {
                    updateValueWithConstraints(PrintSettings.TAG_BOOKLET_LAYOUT,
                            getDefaultValueWithConstraints(PrintSettings.TAG_BOOKLET_LAYOUT));
                }
            } else {
                if (layoutValue == BookletLayout.L_R.ordinal() || layoutValue == BookletLayout.R_L.ordinal()) {
                    updateValueWithConstraints(PrintSettings.TAG_BOOKLET_LAYOUT,
                            getDefaultValueWithConstraints(PrintSettings.TAG_BOOKLET_LAYOUT));
                }
            }
        }
        
        // Constraint #7 Staple
        if (tag.equals(PrintSettings.TAG_STAPLE)) {
            if (value != Staple.OFF.ordinal()) {
                int outputTrayValue = mPrintSettings.getValue(PrintSettings.TAG_OUTPUT_TRAY);
                if (outputTrayValue == OutputTray.FACEUP.ordinal()) {
                    updateValueWithConstraints(PrintSettings.TAG_OUTPUT_TRAY,
                            getDefaultValueWithConstraints(PrintSettings.TAG_OUTPUT_TRAY));
                }
            }
        }
    }
    
    // ================================================================================
    // Set Values
    // ================================================================================
    
    private void updateHighlightedPrinter(int printerId) {
        if (mPrintControls == null) {
            return;
        }
        
        Printer targetPrinter = getPrinter();
        
        View v = mPrintControls.findViewById(ID_PRINT_SELECTED_PRINTER);
        TextView disclosureTextView = (TextView) v.findViewById(R.id.listValueTextView);
        if (targetPrinter == null) {
            disclosureTextView.setText(R.string.ids_lbl_choose_printer);
        } else {
            disclosureTextView.setText(targetPrinter.getName());
        }
    }
    
    private void setViewVisible(String name, boolean visible) {
        View view = mMainLayout.findViewWithTag(name);
        View targetView = (View) view.getTag(ID_TAG_TARGET_VIEW);
        targetView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
    
    private void hideDisabledPrintSettings() {
        Printer printer = getPrinter();

        boolean isStapleAvailable = printer == null || printer.getConfig().isStaplerAvailable();
        setViewVisible(PrintSettings.TAG_STAPLE, isStapleAvailable);
        
        boolean isBookletAvailable = printer == null || printer.getConfig().isBookletAvailable();
        setViewVisible(PrintSettings.TAG_BOOKLET, isBookletAvailable);
        setViewVisible(PrintSettings.TAG_BOOKLET_FINISH, isBookletAvailable);
        setViewVisible(PrintSettings.TAG_BOOKLET_LAYOUT, isBookletAvailable);
        
    }
    
    private boolean shouldDisplayOptionFromPrinter(String name, int value) {
        if (getPrinter() != null) {
            if (name.equals(PrintSettings.TAG_OUTPUT_TRAY)) {
                switch (OutputTray.values()[value]) {
                    case AUTO:
                        return true;
                    case FACEDOWN:
                        return getPrinter().getConfig().isTrayFaceDownAvailable();
                    case FACEUP:
                        return true;
                    case TOP:
                        return getPrinter().getConfig().isTrayTopAvailable();
                    case STACKING:
                        return getPrinter().getConfig().isTrayStackAvailable();
                }
            }
        }
        
        return true;
    }
    
    private int getUpdatedStringId(String name, int id) {
        if (getPrinter() != null) {
            if (name.equals(PrintSettings.TAG_PUNCH)) {
                if (id == R.string.ids_lbl_punch_3holes && getPrinter().getConfig().isPunch4Available()) {
                    return R.string.ids_lbl_punch_4holes;
                }
            }
        }
        
        return id;
    }
    
    private boolean updateValue(String tag, int newValue) {
        int prevValue = mPrintSettings.getValue(tag);
        if (mPrintSettings.setValue(tag, newValue)) {
            applyValueConstraints(tag, prevValue);
            applyViewConstraints(tag);
            return true;
        }
        
        return false;
    }
    
    private void updateDisplayedValue(String tag) {
        int value = mPrintSettings.getValue(tag);
        View view = mMainLayout.findViewWithTag(tag);
        
        if (view != null) {
            if (view instanceof LinearLayout) {
                TextView textView = (TextView) view.findViewById(R.id.listValueTextView);
                
                String text = Integer.toString(value);
                
                Object arr[] = (Object[]) view.getTag(ID_TAG_OPTIONS);
                if (value >= 0 && value < arr.length) {
                    text = arr[value].toString();
                }
                
                textView.setText(text);
            }
            
            if (view instanceof EditText) {
                ((EditText) view).setText(Integer.toString(value));
            }
            
            if (view instanceof Switch) {
                ((Switch) view).setChecked(value == 1);
            }
        }
        
        applyViewConstraints(tag);
    }
    
    public void setValueChangedListener(PrintSettingsViewInterface listener) {
        mListener = listener;
    }
    
    public void setShowPrintControls(boolean showPrintControls) {
        mShowPrintControls = showPrintControls;
        
        if (mShowPrintControls) {
            mPrintControls.setVisibility(View.VISIBLE);
        } else {
            mPrintControls.setVisibility(View.GONE);
        }
    }
    
    public void setPrinterId(int printerId) {
        mPrinterId = printerId;
        
        updateHighlightedPrinter(mPrinterId);
        hideDisabledPrintSettings();
    }
    
    public Printer getPrinter() {
        for (int i = 0; i < mPrintersList.size(); i++) {
            Printer printer = mPrintersList.get(i);
            if (mPrinterId == printer.getId()) {
                return printer;                
            } 
        }
        return null;
    }
    
    public void setPrintSettings(PrintSettings printSettings) {
        mPrintSettings = new PrintSettings(printSettings);
        
        if (mPrintSettings != null) {
            
            Set<String> keySet = PrintSettings.sSettingMap.keySet();
            
            for (String key : keySet) {
                updateDisplayedValue(key);
            }
        }
    }
    
    // ================================================================================
    // Printer controls functions
    // ================================================================================
    
    private void initializePrinterControls() {
        mPrintControls = new LinearLayout(getContext());
        mPrintControls.setOrientation(LinearLayout.VERTICAL);
        
        // Create Header
        LinearLayout header = createTitle(getResources().getString(R.string.ids_lbl_print), false, -1, false, true);
        header.setId(ID_PRINT_HEADER);
        header.setOnClickListener(this);
        mPrintControls.addView(header);
        
        // Create disclosure for item
        View view = LayoutInflater.from(getContext()).inflate(R.layout.printsettings_disclosure_withvalue, null);
        view.setActivated(true);

        int viewWidth = getResources().getDimensionPixelSize(R.dimen.printsettings_list_value_width);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(viewWidth, LayoutParams.MATCH_PARENT, 0.0f);
        params.gravity = Gravity.CENTER_VERTICAL;
        view.setLayoutParams(params);
        
        // Create item
        LinearLayout selectedPrinter = createItem(getResources().getString(R.string.ids_lbl_printer), false, -1, false, view);
        selectedPrinter.setId(ID_PRINT_SELECTED_PRINTER);
        selectedPrinter.setTag(KEY_TAG_PRINTER);
        selectedPrinter.setOnClickListener(this);
        mPrintControls.addView(selectedPrinter);
        
        params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        mMainLayout.addView(mPrintControls, params);
    }
    
    // ================================================================================
    // Print settings controls functions
    // ================================================================================
    
    private Object[] getOptionsStrings(String name, List<Option> options) {
        ArrayList<String> optionsStrings = new ArrayList<String>();
        
        for (Option option : options) {
            String value = option.getTextContent();
            
            int id = AppUtils.getResourseId(value, R.string.class, -1);
            id = getUpdatedStringId(name, id);
            
            if (id != -1) {
                optionsStrings.add(getResources().getString(id));
            } else {
                optionsStrings.add(Integer.toString(id));
            }
        }
        
        return optionsStrings.toArray();
    }
    
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
        item.setId(ID_SHOW_SUBVIEW_CONTAINER);
        
        if (type.equalsIgnoreCase(Setting.ATTR_VAL_LIST)) {
            view.setId(ID_DISCLOSURE_VIEW);
            item.setTag(name);
            item.setTag(ID_TAG_ICON, icon);
            item.setTag(ID_TAG_TEXT, text);
            item.setTag(ID_TAG_OPTIONS, getOptionsStrings(name, setting.getOptions()));
            item.setTag(ID_TAG_TARGET_VIEW, item);
            item.setOnClickListener(this);
        } else {
            view.setTag(ID_TAG_TARGET_VIEW, item);
        }
        
        ll.addView(item);
    }
    
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
        title.setId(ID_COLLAPSE_CONTAINER);
        title.setTag(ID_COLLAPSE_TARGET_GROUP, itemsGroup);
        title.setTag(nameStr);
        title.setOnClickListener(this);
        expandControl(title, false); // initially expand
        
        mPrintSettingsTitles.add(title); // Add to controls list
        mMainLayout.addView(title);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        itemsGroup.setLayoutParams(params);
        mMainLayout.addView(itemsGroup);
    }
    
    private void initializePrintSettingsControls() {
        mPrintSettingsTitles = new ArrayList<LinearLayout>();
        
        if (PrintSettings.sGroupList != null) {
            for (Group group : PrintSettings.sGroupList) {
                addSettingsTitleView(group);
            }
        }
    }
    
    // ================================================================================
    // Sub-view value Controls
    // ================================================================================
    
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
        
        mMainScrollView.startAnimation(animOut);
    }
    
    private void animateDismissSubview() {
        int width = getWidth();
        
        int duration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        AccelerateInterpolator interpolator = new AccelerateInterpolator();
        
        TranslateAnimation animIn = new TranslateAnimation(-width, 0, 0, 0);
        animIn.setDuration(duration);
        animIn.setInterpolator(interpolator);
        animIn.setFillAfter(true);
        mMainScrollView.startAnimation(animIn);
        
        TranslateAnimation animOut = new TranslateAnimation(0, width, 0, 0);
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
                Message newMessage = Message.obtain(mHandler, MSG_SLIDE_OUT);
                mHandler.sendMessage(newMessage);
            }
        });
        
        mSubView.startAnimation(animOut);
    }
    
    private void addSubviewOptionsList(String str, int value, int tagValue, boolean withSeparator, int itemId) {
        addSubviewOptionsList(str, null, value, tagValue, withSeparator, itemId);
    }
    
    private void addSubviewOptionsList(String str, String sub, int value, int tagValue, boolean withSeparator, int itemId) {
        
        // Add disclosure
        LayoutInflater li = LayoutInflater.from(getContext());
        View view = li.inflate(R.layout.printsettings_radiobutton, null);
        view.setId(ID_SUBVIEW_STATUS);
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
        LinearLayout item = createItem(str, sub, true, -1, withSeparator, view);
        item.setId(itemId);
        item.setTag(Integer.valueOf(tagValue));
        item.setOnClickListener(this);
        
        mSubOptionsLayout.addView(item);
    }
    
    private void addSubviewOptionsTitle(String str, boolean showIcon, int iconId) {
        LinearLayout title = createTitle(str, showIcon, iconId, true, false);
        title.setId(ID_HIDE_SUBVIEW_CONTAINER);
        title.setOnClickListener(this);
        
        mSubView.addView(title);
    }
    
    private void createSubview(View v) {
        mSubView.setTag(v.getTag());
        
        if (v.getTag().toString().equals(KEY_TAG_PRINTER)) {
            String title = getResources().getString(R.string.ids_lbl_printer);
            addSubviewOptionsTitle(title, false, -1);
            
            if (mPrintersList != null) {
                for (int i = 0; i < mPrintersList.size(); i++) {
                    Printer printer = mPrintersList.get(i);
                    
                    boolean showSeparator = (i != mPrintersList.size() - 1);
                    addSubviewOptionsList(printer.getName(), printer.getIpAddress(), mPrinterId, printer.getId(), showSeparator, ID_SUBVIEW_PRINTER_ITEM);
                }
            }
        } else {
            mSubView.setTag(ID_TAG_TEXT, v.getTag(ID_TAG_TEXT));
            mSubView.setTag(ID_TAG_ICON, v.getTag(ID_TAG_ICON));
            mSubView.setTag(ID_TAG_OPTIONS, v.getTag(ID_TAG_OPTIONS));
            
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
            Object[] options = (Object[]) v.getTag(ID_TAG_OPTIONS);
            
            int lastIdx = -1;
            for (int i = 0; i < options.length; i++) {
                if (shouldDisplayOptionFromConstraints(name, i) && shouldDisplayOptionFromPrinter(name, i)) {
                    addSubviewOptionsList(options[i].toString(), value, i, true, ID_SUBVIEW_OPTION_ITEM);
                    lastIdx = i;
                }
            }
            
            // hide the separator of the last item added
            if (mSubView.findViewWithTag(lastIdx) != null) {
                View container = mSubView.findViewWithTag(lastIdx);
                container.findViewById(R.id.menuSeparator).setVisibility(View.GONE);
            }
            
        }
    }
    
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
        
        mMainScrollView.setEnabled(false);
        mSubView.setEnabled(true);
        if (animate) {
            animateDisplaySubview();
        } else {
            mMainScrollView.setVisibility(View.GONE);
        }
        
        AppUtils.changeChildrenFont(mSubView, SmartDeviceApp.getAppFont());
    }
    
    private void dismissOptionsSubview(boolean animate) {
        mMainScrollView.setEnabled(true);
        mMainScrollView.setVisibility(View.VISIBLE);
        mSubView.setEnabled(false);
        if (animate) {
            animateDismissSubview();
        } else {
            removeView(mSubView);
        }
    }
    
    private void subviewOptionsItemClicked(View v) {
        int id = (Integer) v.getTag();
        
        if (v.getId() == ID_SUBVIEW_OPTION_ITEM) {
            if (updateValue((String) mSubView.getTag(), id)) {
                if (mListener != null) {
                    mListener.onPrintSettingsValueChanged(mPrintSettings);
                }
                
                // Update UI
                updateDisplayedValue((String) mSubView.getTag());
                
                Object[] options = (Object[]) mSubView.getTag(ID_TAG_OPTIONS);
                for (int i = 0; i < options.length; i++) {
                    View view = mSubView.findViewWithTag(Integer.valueOf(i));
                    // Some views may be hidden
                    if (view != null) {
                        View subView = view.findViewById(ID_SUBVIEW_STATUS);
                        subView.setSelected(id == i);
                    }
                }
            }
        } else if (v.getId() == ID_SUBVIEW_PRINTER_ITEM) {
            if (mPrinterId != id) {
                setPrinterId(id);
                // TODO: get new printer settings
                setPrintSettings(new PrintSettings(mPrinterId));
                
                if (mListener != null) {
                    mListener.onPrinterIdSelectedChanged(mPrinterId);
                    mListener.onPrintSettingsValueChanged(mPrintSettings);
                }

                for (int i = 0; i < mPrintersList.size(); i++) {
                    Printer printer = mPrintersList.get(i);
                    View view = mSubView.findViewWithTag(Integer.valueOf(printer.getId()));
                    View subView = view.findViewById(ID_SUBVIEW_STATUS);
                    subView.setSelected(mPrinterId == printer.getId());
                }
            }
            
        }
    }
    
    private void executePrint() {
        if (mListener != null) {
            mListener.onPrint(getPrinterFromList(mPrinterId), mPrintSettings);
        }
    }
    
    // ================================================================================
    // Convenience methods
    // ================================================================================
    
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
    
    private LinearLayout createItem(String text, boolean showIcon, int iconId, boolean showSeparator, View view) {
        return createItem(text, null, showIcon, iconId, showSeparator, view);
    }
    
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
            
            EditText editText = (EditText) li.inflate(R.layout.printsettings_input_numeric, null);
            editText.setLayoutParams(params);
            editText.setTag(tag);
            editText.addTextChangedListener(new EditTextWatcher(tag, 1));
            return editText;
        } else if (type.equalsIgnoreCase(Setting.ATTR_VAL_LIST)) {
            params.height = LayoutParams.MATCH_PARENT;
            
            int width = getResources().getDimensionPixelSize(R.dimen.printsettings_list_value_width);
            params.width = width;
            
            View view = li.inflate(R.layout.printsettings_disclosure_withvalue, null);
            view.setActivated(true);
            view.setLayoutParams(params);
            return view;
        }
        
        return null;
    }
    
    // ================================================================================
    // Collapsible Controls
    // ================================================================================
    
    // TODO: improve animation code
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
            int height = menuHeight;
            if (i != target.getChildCount() - 1) {
                height += getResources().getDimensionPixelSize(R.dimen.separator_size);
            }
            totalHeight += height;
            
            View child = target.getChildAt(i);
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
    
    // TODO: improve animation code
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
            int height = menuHeight;
            if (i != target.getChildCount() - 1) {
                height += getResources().getDimensionPixelSize(R.dimen.separator_size);
            }
            currentHeight += height;
            
            View child = target.getChildAt(i);
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
    
    private void collapseControl(View v, boolean animate) {
        View target = (View) v.getTag(ID_COLLAPSE_TARGET_GROUP);
        if (animate) {
            animateCollapse(v);
        } else {
            v.setSelected(true);
            target.setVisibility(View.GONE);
        }
    }
    
    private void expandControl(View v, boolean animate) {
        View target = (View) v.getTag(ID_COLLAPSE_TARGET_GROUP);
        target.setVisibility(View.VISIBLE);
        if (animate) {
            animateExpand(v);
        } else {
            v.setSelected(false);
        }
    }
    
    private void toggleCollapse(View v) {
        v.setClickable(false);
        if (!v.isSelected()) {
            collapseControl(v, true);
        } else {
            expandControl(v, true);
        }
    }
    
    private Printer getPrinterFromList(int printerId) {
        for (Printer p : mPrintersList) {
            if (p.getId() == printerId) {
                return p;
            }
        }
        return null;
    }
    
    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case ID_COLLAPSE_CONTAINER:
                toggleCollapse(v);
                break;
            case ID_SHOW_SUBVIEW_CONTAINER:
                if (mMainScrollView.isEnabled()) {
                    displayOptionsSubview(v, true);
                }
                break;
            case ID_HIDE_SUBVIEW_CONTAINER:
                if (mSubView.isEnabled()) {
                    dismissOptionsSubview(true);
                }
                break;
            case ID_SUBVIEW_OPTION_ITEM:
            case ID_SUBVIEW_PRINTER_ITEM:
                subviewOptionsItemClicked(v);
                break;
            case ID_PRINT_SELECTED_PRINTER:
                displayOptionsSubview(v, true);
                break;
            case ID_PRINT_HEADER:
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
                mMainScrollView.scrollTo(0, msg.arg1);
                return true;
            case MSG_SLIDE_IN:
                mMainScrollView.setVisibility(View.GONE);
                return true;
            case MSG_SLIDE_OUT:
                removeView(mSubView);
                return true;
            case MSG_SET_SUB_SCROLL:
                mSubScrollView.scrollTo(0, msg.arg1);
                return true;
            case MSG_SHOW_SUBVIEW:
                displayOptionsSubview(mMainLayout.findViewWithTag(msg.obj), false);
                return true;
        }
        return false;
    }
    
    // ================================================================================
    // CompoundButton.OnCheckedChangeListener
    // ================================================================================
    
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (updateValue(buttonView.getTag().toString(), isChecked ? 1 : 0)) {
            if (mListener != null) {
                mListener.onPrintSettingsValueChanged(mPrintSettings);
            }
        } else {
            // cancel onCheckedChanged?
        }
    }
    
    // ================================================================================
    // Internal classes
    // ================================================================================
    
    private class EditTextWatcher implements TextWatcher {
        private String mTag;
        private boolean mEditing;
        private int mMinValue;
        
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
                NumberFormat nf = NumberFormat.getIntegerInstance();
                try {
                    String formatted = nf.format(Integer.parseInt(digits));
                    s.replace(0, s.length(), formatted);
                } catch (NumberFormatException nfe) {
                    s.replace(0, s.length(), "0");
                }
                
                if (Integer.parseInt(s.toString()) <= mMinValue) {
                    s.replace(0, s.length(), Integer.toString(mMinValue));
                }
                
                if (updateValue(mTag, Integer.parseInt(s.toString()))) {
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
    
    public interface PrintSettingsViewInterface {
        public void onPrinterIdSelectedChanged(int printerId);
        public void onPrintSettingsValueChanged(PrintSettings printSettings);
        public void onPrint(Printer printer, PrintSettings printSettings);
    }
}
