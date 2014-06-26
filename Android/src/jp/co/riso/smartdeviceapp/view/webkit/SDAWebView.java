/* Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * SDAWebView.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

 package jp.co.riso.smartdeviceapp.view.webkit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

/**
 * @class SDAWebView
 * @brief Subclass of WebView class. Sets the standard functionalities of the WebView.
 */
public class SDAWebView extends WebView {
    
    /**
     * @brief Constructs a new WebView with a Context object
     * 
     * @param context a Context object used to access application assets 
     */
    public SDAWebView(Context context) {
        this(context, null);
    }
    
    /**
     * @brief Constructs a new WebView with layout parameters.
     * 
     * @param context a Context object used to access application assets
     * @param attrs an AttributeSet passed to our parent 
     */
    public SDAWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    /**
     * @brief Constructs a new WebView with layout parameters and a default style.
     * 
     * @param context a Context object used to access application assets
     * @param attrs an AttributeSet passed to our parent 
     * @param defStyle the default style resource ID  
     */
    public SDAWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    /**
     * @brief Initializes the WebView with the standard settings
     */
    public void init() {
        if (!isInEditMode()) {
            setSettings();
            setLook();
        }
    }
    
    /**
     * @brief Sets the settings of the WebView to match the default android webview.
     */
    @SuppressLint("SetJavaScriptEnabled") // Javascript is enabled
    private void setSettings() {
        getSettings().setSupportZoom(true);
        getSettings().setBuiltInZoomControls(true);
        getSettings().setDisplayZoomControls(false);
        
        getSettings().setUseWideViewPort(true);
        getSettings().setLoadWithOverviewMode(true);
        
        getSettings().setJavaScriptEnabled(true);
    }
    
    /**
     * @brief Sets the visible style of the WebView
     */
    private void setLook() {
        setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        setScrollbarFadingEnabled(true);
    }
}
