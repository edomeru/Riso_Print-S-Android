/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * BaseWebFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.base;

import jp.co.riso.smartprint.R;
import jp.co.riso.smartdeviceapp.view.webkit.SDAWebView;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

/**
 * @class BaseWebFragment
 * 
 * @brief Base web fragment class
 */
public abstract class BaseWebFragment extends BaseFragment {
    protected SDAWebView mWebView = null;
    
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
    }

    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        mWebView = view.findViewById(R.id.contentWebView);
        if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
            mWebView.setDefaultFocusHighlightEnabled(false);
        }
        
        configureWebView(mWebView);

        if (isChromeBook() && savedInstanceState != null) {
            mWebView.restoreState(savedInstanceState);
        } else {
            mWebView.loadUrl(getUrlString());
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (isChromeBook()) {
            mWebView.saveState(outState);
        }
    }
    
    /**
     * @brief Performs any additional configuration to the webview before URL Loading.
     * 
     * @param webView WebView to be configured
     */
    public abstract void configureWebView(WebView webView);
    
    /**
     * @brief Gets the URL to be loaded in the web view.
     * 
     * @return URL String
     */
    public abstract String getUrlString();
    
    
}
