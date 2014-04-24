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

public class SDAWebView extends WebView {
    
    /**
     * Constructor
     * <p>
     * Instantiate custom WebView
     * 
     * @param context
     */
    public SDAWebView(Context context) {
        super(context);
        init();
    }
    
    /**
     * Constructor
     * <p>
     * Instantiate custom WebView
     * 
     * @param context
     * @param attrs
     */
    public SDAWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    /**
     * Constructor
     * <p>
     * Instantiate custom WebView
     * 
     * @param context
     * @param attrs
     * @param defStyle
     */
    public SDAWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    /**
     * Initializes the custom web view
     */
    public void init() {
        if (!isInEditMode()) {
            setSettings();
            setLook();
        }
    }

    /**
     * Sets the settings of the WebView
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
     * Sets the visible style of the WebView
     */
    private void setLook() {
        setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        setScrollbarFadingEnabled(true);
    }
}
