/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * HelpFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;

import jp.co.riso.android.util.AppUtils;
import jp.co.riso.android.util.Logger;
import jp.co.riso.smartprint.R;
import jp.co.riso.smartdeviceapp.view.base.BaseWebFragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

/**
 * @class HelpFragment
 * 
 * @brief Fragment class for Help Screen
 */
public class HelpFragment extends BaseWebFragment {
    @Override
    public int getViewLayout() {
        return R.layout.fragment_helplegal;
    }
    
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        TextView textView = view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_help);
        
        addActionMenuButton(view);
    }
    
    @Override
    public void configureWebView(WebView webView) {
        webView.setWebViewClient(new WebViewClient() {
            
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                
                Logger.logStartTime(getActivity(), HelpFragment.class, "Help Screen load");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Logger.logStopTime(getActivity(), HelpFragment.class, "Help Screen load");
            }
        });
    }
    
    @Override
    public String getUrlString() {
        String htmlFolder = getString(R.string.html_folder);
        String helpHtml = getString(R.string.help_html);
        return AppUtils.getLocalizedAssetFullPath(getActivity(), htmlFolder, helpHtml);
    }
}
