/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * LegalFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import jp.co.riso.android.util.AppUtils;
import jp.co.riso.android.util.Logger;
import jp.co.riso.smartprint.R;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartdeviceapp.view.base.BaseWebFragment;

public class LegalFragment extends BaseWebFragment {
    public static final String JS_REPLACE_FORMAT = "javascript:document.getElementById('%s').innerHTML='%s';";
    public static final String VERSION_HTML_ID = "localize_version";
    
    /** {@inheritDoc} */
    @Override
    public int getViewLayout() {
        return R.layout.fragment_helplegal;
    }
    
    /** {@inheritDoc} */
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_legal);
        
        addActionMenuButton(view);
    }
    
    /** {@inheritDoc} */
    @SuppressLint("NewApi") // Difference in injection in Kitkat and previous devices
    @Override
    public void configureWebView(WebView webView) {
        webView.setWebViewClient(new WebViewClient() {
            
            @Override
            public void onPageFinished(WebView view, String url) {

                try {
                    if (getActivity() != null && getActivity() instanceof MainActivity) {
                        PackageManager packageManager = getActivity().getPackageManager();
                        String versionName = packageManager.getPackageInfo(getActivity().getPackageName(), 0).versionName;
                        
                        String javascript = String.format(Locale.getDefault(), JS_REPLACE_FORMAT, VERSION_HTML_ID, versionName);
                        
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            view.evaluateJavascript(javascript, null);
                        } else {
                            view.loadUrl(javascript);
                        }
                    }
                } catch (NameNotFoundException e) {
                    Logger.logWarn(LegalFragment.class, "No version name found");
                }
            }
        });
    }
    
    /** {@inheritDoc} */
    @Override
    public String getUrlString() {
        String htmlFolder = getString(R.string.html_folder);
        String legalHtml = getString(R.string.legal_html);
        return AppUtils.getLocalizedAssetFullPath(getActivity(), htmlFolder, legalHtml);
    }
}
