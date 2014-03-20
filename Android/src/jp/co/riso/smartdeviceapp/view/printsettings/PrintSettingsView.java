/*
 * Copyright (c) 2014 All rights reserved.
 *
 * PrintSettingsView.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.printsettings;

import java.io.IOException;
import java.io.StringReader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.model.PrintSettings;
import jp.co.riso.smartdeviceapp.model.PrintSettingsConstants;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
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
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

public class PrintSettingsView extends FrameLayout implements View.OnClickListener, Callback, CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "PrintSettingsView";
    
    private static final String PRINT_SETTINGS_CONTENT = "printsettings.xml";
    
    private static final String XML_NODE_NAME = "name";
    private static final String XML_NODE_TEXT = "text";
    private static final String XML_NODE_ICON = "icon";
    private static final String XML_NODE_TYPE = "type";
    
    private static final String KEY_SELECTED_TITLES = "key_selected_titles";
    private static final String KEY_SCROLL_POSITION = "key_scroll_position";
    private static final String KEY_SUBVIEW_DISPLAYED = "key_subview_displayed";
    private static final String KEY_SUB_SCROLL_POSITION = "key_sub_scroll_position";
    
    public static final int MSG_COLLAPSE = 0;
    public static final int MSG_EXPAND = 1;
    public static final int MSG_SET_SCROLL = 2;
    public static final int MSG_SLIDE_IN = 3;
    public static final int MSG_SLIDE_OUT = 4;
    public static final int MSG_SET_SUB_SCROLL = 5;
    public static final int MSG_SHOW_SUBVIEW = 6;
    
    public static final int ID_COLLAPSE_CONTAINER = 0x11000001;
    public static final int ID_COLLAPSE_TARGET_GROUP = 0x11000002;
    
    public static final int ID_SHOW_SUBVIEW_CONTAINER = 0x11000003;
    public static final int ID_HIDE_SUBVIEW_CONTAINER = 0x11000004;
    
    public static final int ID_SUBVIEW_OPTION_ITEM = 0x11000005;
    public static final int ID_SUBVIEW_STATUS = 0x11000006;
    
    public static final int ID_TAG_TEXT = 0x11000007;
    public static final int ID_TAG_ICON = 0x11000008;
    public static final int ID_TAG_OPTIONS = 0x11000009;
    
    private Document mPrintSettingsContent;
    private PrintSettings mPrintSettings;
    
    private ScrollView mMainScrollView;
    private LinearLayout mMainLayout;
    
    private ScrollView mSubScrollView;
    private LinearLayout mSubLayout;
    
    private LinearLayout mPrintControls;
    
    private Handler mHandler;
    private ArrayList<LinearLayout> mPrintSettingsTitles = null;
    
    private ValueChangedListener mListener = null;
    
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
        initializeMainView();
        parsePrintSettingsContent();
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
            outState.putString(KEY_SUBVIEW_DISPLAYED, mSubLayout.getTag().toString());
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
            String tag = savedInstanceState.getString(KEY_SUBVIEW_DISPLAYED);
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
    // Set Values
    // ================================================================================
    
    private void updateItemValue(String tag) {
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
    }
    
    public void setValueChangedListener(ValueChangedListener listener) {
        mListener = listener;
    }
    
    public void setPrintSettings(PrintSettings printSettings) {
        mPrintSettings = new PrintSettings(printSettings);
        
        if (mPrintSettings != null) {
            
            Set<String> keySet = PrintSettingsConstants.SETTING_MAP.keySet();
            
            for (String key : keySet) {
                updateItemValue(key);
            }
        }
    }
    
    // ================================================================================
    // Parse Settings
    // ================================================================================
    
    private void parsePrintSettingsContent() {
        String xmlString = AppUtils.getFileContentsFromAssets(getContext(), PRINT_SETTINGS_CONTENT);
        
        mPrintSettingsContent = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xmlString));
            mPrintSettingsContent = db.parse(is);
        } catch (ParserConfigurationException e) {
            Log.e(TAG, "Error: " + e.getMessage());
        } catch (SAXException e) {
            Log.e(TAG, "Error: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
    }
    
    // ================================================================================
    // Printer controls functions
    // ================================================================================
    
    private void initializePrinterControls() {
        mPrintControls = new LinearLayout(getContext());
        mPrintControls.setOrientation(LinearLayout.VERTICAL);
        
        // Create Header
        LinearLayout header = createTitle(getResources().getString(R.string.ids_lbl_print), false, -1, false);
        mPrintControls.addView(header);
        
        // Create disclosure for item
        View view = LayoutInflater.from(getContext()).inflate(R.layout.printsettings_disclosure_withvalue, null);
        view.setOnClickListener(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 0.0f);
        params.gravity = Gravity.CENTER_VERTICAL;
        view.setLayoutParams(params);
        
        // Create item
        LinearLayout selectedPrinter = createItem(getResources().getString(R.string.ids_lbl_printer), false, -1, false, view);
        mPrintControls.addView(selectedPrinter);
        
        params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        mMainLayout.addView(mPrintControls, params);
    }
    
    // ================================================================================
    // Print settings controls functions
    // ================================================================================
    
    private Object[] getSettingsOptions(Node node) {
        ArrayList<String> options = new ArrayList<String>();
        
        NodeList optionsList = node.getChildNodes();
        for (int i = 1; i < optionsList.getLength(); i += 2) {
            
            int id = AppUtils.getResourseId(optionsList.item(i).getTextContent(), R.string.class, -1);
            if (id != -1) {
                options.add(getResources().getString(id));
            } else {
                options.add(Integer.toString(i));
            }
            
        }
        return options.toArray();
    }
    
    private String getNamedItemValue(NamedNodeMap namedNodeMap, String key) {
        if (namedNodeMap.getNamedItem(key) == null) {
            return "";
        }
        
        return namedNodeMap.getNamedItem(key).getNodeValue();
    }
    
    private void addSettingsItemView(LinearLayout ll, Node setting, boolean withSeparator) {
        NamedNodeMap attrs = setting.getAttributes();
        
        String name = getNamedItemValue(attrs, XML_NODE_NAME);
        String icon = getNamedItemValue(attrs, XML_NODE_ICON);
        String text = getNamedItemValue(attrs, XML_NODE_TEXT);
        String type = getNamedItemValue(attrs, XML_NODE_TYPE);
        
        String titleText = "";
        int titleId = AppUtils.getResourseId(text, R.string.class, -1);
        if (titleId != -1) {
            titleText = getResources().getString(titleId);
        }
        
        int iconId = AppUtils.getResourseId(icon, R.drawable.class, -1);
        
        View view = createControlView(type, name);
        
        LinearLayout item = createItem(titleText, true, iconId, withSeparator, view);
        item.setId(ID_SHOW_SUBVIEW_CONTAINER);
        
        if (type.equalsIgnoreCase("list")) {
            item.setTag(name);
            item.setTag(ID_TAG_ICON, icon);
            item.setTag(ID_TAG_TEXT, text);
            item.setTag(ID_TAG_OPTIONS, getSettingsOptions(setting));
            item.setOnClickListener(this);
        }
        
        ll.addView(item);
    }
    
    private void addSettingsTitleView(Node node) {
        NamedNodeMap attrs = node.getAttributes();
        
        String nameStr = getNamedItemValue(attrs, XML_NODE_NAME);
        String textStr = getNamedItemValue(attrs, XML_NODE_TEXT);
        
        LinearLayout itemsGroup = new LinearLayout(getContext());
        itemsGroup.setOrientation(LinearLayout.VERTICAL);
        
        NodeList settingList = node.getChildNodes();
        for (int i = 1; i < settingList.getLength(); i += 2) {
            addSettingsItemView(itemsGroup, settingList.item(i), i < settingList.getLength() - 2);
        }
        
        // Create Header
        String titleText = "";
        int id = AppUtils.getResourseId(textStr, R.string.class, -1);
        if (id != -1) {
            titleText = getResources().getString(id);
        }
        
        LinearLayout title = createTitle(titleText, true, R.drawable.selector_printsettings_collapsible, false);
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
        
        if (mPrintSettingsContent != null) {
            NodeList groupList = mPrintSettingsContent.getElementsByTagName("group");
            
            // looping through all item nodes <item>
            for (int i = 0; i < groupList.getLength(); i++) {
                addSettingsTitleView(groupList.item(i));
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
        mSubScrollView.startAnimation(animIn);
        
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
        
        mSubScrollView.startAnimation(animOut);
    }
    
    private void addSubviewOptionsList(String str, int value, int id, boolean withSeparator) {
        
        // Add disclosure
        LayoutInflater li = LayoutInflater.from(getContext());
        RadioButton radioButton = (RadioButton) li.inflate(R.layout.printsettings_radiobutton, null);
        radioButton.setId(ID_SUBVIEW_STATUS);
        radioButton.setClickable(false);
        if (value == id) {
            radioButton.setChecked(true);
        }
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 0.0f);
        params.gravity = Gravity.CENTER_VERTICAL;
        
        int margin = getResources().getDimensionPixelSize(R.dimen.printsettings_icon_setting_padding);
        params.leftMargin = margin;
        params.rightMargin = margin;
        
        radioButton.setLayoutParams(params);
        
        // TODO: iconID
        LinearLayout item = createItem(str, true, -1, withSeparator, radioButton);
        item.setId(ID_SUBVIEW_OPTION_ITEM);
        item.setTag(Integer.valueOf(id));
        item.setOnClickListener(this);
        
        mSubLayout.addView(item);
    }
    
    private void addSubviewOptionsTitle(String str, int iconId) {
        LinearLayout title = createTitle(str, true, iconId, true);
        title.setId(ID_HIDE_SUBVIEW_CONTAINER);
        title.setOnClickListener(this);
        
        ImageView imgView = (ImageView) title.findViewById(R.id.returnButtonIndicator);
        imgView.setRotation(180);
        
        mSubLayout.addView(title);
    }
    
    private void createSubview(View v) {
        mSubLayout.setTag(v.getTag());
        mSubLayout.setTag(ID_TAG_TEXT, v.getTag(ID_TAG_TEXT));
        mSubLayout.setTag(ID_TAG_ICON, v.getTag(ID_TAG_ICON));
        mSubLayout.setTag(ID_TAG_OPTIONS, v.getTag(ID_TAG_OPTIONS));
        
        String name = (String) v.getTag();
        String text = (String) v.getTag(ID_TAG_TEXT);
        String icon = (String) v.getTag(ID_TAG_ICON);
        
        String titleText = "";
        int titleId = AppUtils.getResourseId(text, R.string.class, -1);
        if (titleId != -1) {
            titleText = getResources().getString(titleId);
        }
        
        int iconId = AppUtils.getResourseId(icon, R.drawable.class, -1);
        
        addSubviewOptionsTitle(titleText, iconId);
        
        int value = mPrintSettings.getValue(name);
        Object[] options = (Object[]) v.getTag(ID_TAG_OPTIONS);
        for (int i = 0; i < options.length; i++) {
            addSubviewOptionsList(options[i].toString(), value, i, i != options.length - 1);
        }
    }
    
    private void displayOptionsSubview(View v, boolean animate) {
        if (mSubLayout == null) {
            mSubLayout = new LinearLayout(getContext());
            mSubLayout.setOrientation(LinearLayout.VERTICAL);
        } else {
            mSubLayout.removeAllViews();
        }
        
        if (mSubScrollView == null) {
            mSubScrollView = new ScrollView(getContext());
        }
        
        if (mSubScrollView.getChildCount() == 0) {
            mSubScrollView.addView(mSubLayout, new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
        
        createSubview(v);
        
        if (mSubScrollView.getParent() == null) {
            addView(mSubScrollView, new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
        
        mMainScrollView.setClickable(false);
        mSubScrollView.setClickable(true);
        if (animate) {
            animateDisplaySubview();
        } else {
            mMainScrollView.setVisibility(View.GONE);
        }
        
        AppUtils.changeChildrenFont(mSubScrollView, SmartDeviceApp.getAppFont());
    }
    
    private void dismissOptionsSubview(boolean animate) {
        mMainScrollView.setClickable(true);
        mMainScrollView.setVisibility(View.VISIBLE);
        mSubScrollView.setClickable(false);
        if (animate) {
            animateDismissSubview();
        } else {
            removeView(mSubScrollView);
        }
    }
    
    private void subviewOptionsItemClicked(View v) {
        int id = (Integer) v.getTag();
        
        if (mPrintSettings.setValue((String) mSubLayout.getTag(), id)) {
            if (mListener != null) {
                mListener.onPrintSettingsValueChanged(mPrintSettings);
            }
            
            // Update UI
            updateItemValue((String) mSubLayout.getTag());
            
            Object[] options = (Object[]) mSubLayout.getTag(ID_TAG_OPTIONS);
            for (int i = 0; i < options.length; i++) {
                View view = mSubLayout.findViewWithTag(Integer.valueOf(i));
                RadioButton radioButton = (RadioButton) view.findViewById(ID_SUBVIEW_STATUS);
                radioButton.setChecked(id == i);
            }
        }
    }
    
    // ================================================================================
    // Convenience methods
    // ================================================================================
    
    private LinearLayout createTitle(String text, boolean showIcon, int iconId, boolean showBackButton) {
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
        
        return title;
    }
    
    private LinearLayout createItem(String text, boolean showIcon, int iconId, boolean showSeparator, View view) {
        LayoutInflater li = LayoutInflater.from(getContext());
        LinearLayout item = (LinearLayout) li.inflate(R.layout.printsettings_container_item, null);
        
        TextView textView = (TextView) item.findViewById(R.id.menuTextView);
        textView.setText(text);
        
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
        
        if (type.equalsIgnoreCase("boolean")) {
            int margin = getResources().getDimensionPixelSize(R.dimen.printsettings_icon_setting_padding);
            params.leftMargin = margin;
            params.rightMargin = margin;
            
            Switch switchView = (Switch) li.inflate(R.layout.printsettings_switch, null);
            switchView.setOnCheckedChangeListener(this);
            switchView.setTag(tag);
            switchView.setLayoutParams(params);
            return switchView;
        } else if (type.equalsIgnoreCase("numeric")) {
            int margin = getResources().getDimensionPixelSize(R.dimen.printsettings_icon_setting_padding);
            params.leftMargin = margin;
            params.rightMargin = margin;
            
            int width = getResources().getDimensionPixelSize(R.dimen.printsettings_input_width);
            params.width = width;
            
            EditText editText = (EditText) li.inflate(R.layout.printsettings_input_numeric, null);
            editText.setLayoutParams(params);
            editText.setTag(tag);
            editText.addTextChangedListener(new EditTextWatcher(tag));
            return editText;
        } else if (type.equalsIgnoreCase("list")) {
            params.height = LayoutParams.MATCH_PARENT;
            
            View view = li.inflate(R.layout.printsettings_disclosure_withvalue, null);
            
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
                displayOptionsSubview(v, true);
                break;
            case ID_HIDE_SUBVIEW_CONTAINER:
                dismissOptionsSubview(true);
                break;
            case ID_SUBVIEW_OPTION_ITEM:
                subviewOptionsItemClicked(v);
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
                removeView(mSubScrollView);
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
        if (mPrintSettings.setValue(buttonView.getTag().toString(), isChecked ? 1 : 0)) {
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
        
        public EditTextWatcher(String tag) {
            mTag = tag;
            mEditing = false;
        }
        
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
                
                if (mPrintSettings.setValue(mTag, Integer.parseInt(s.toString()))) {
                    if (mListener != null) {
                        mListener.onPrintSettingsValueChanged(mPrintSettings);
                    }
                }
                
                mEditing = false;
            }
        }
        
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
        
    }
    
    public interface ValueChangedListener {
        public void onPrintSettingsValueChanged(PrintSettings printSettings);
    }
}
