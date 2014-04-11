/* Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * SDAWebView.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

 package jp.co.riso.smartdeviceapp.view.webkit;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

public class SDAWebView extends WebView {
    
    public SDAWebView(Context context) {
        super(context);
        init();
    }
    
    public SDAWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
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
    private void setSettings() {
        getSettings().setSupportZoom(true);
        getSettings().setBuiltInZoomControls(true);
        getSettings().setDisplayZoomControls(false);
        
        getSettings().setUseWideViewPort(true);
        getSettings().setLoadWithOverviewMode(true);
    }
    
    /**
     * Sets the visible style of the WebView
     */
    private void setLook() {
        setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        setScrollbarFadingEnabled(true);
    }
}