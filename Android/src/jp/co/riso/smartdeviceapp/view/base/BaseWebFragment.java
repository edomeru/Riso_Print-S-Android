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
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

public abstract class BaseWebFragment extends BaseFragment {
    protected SDAWebView mWebView = null;
    
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
    }

    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        mWebView = (SDAWebView) view.findViewById(R.id.contentWebView);
        
        configureWebView(mWebView);
        mWebView.loadUrl(getUrlString());
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
