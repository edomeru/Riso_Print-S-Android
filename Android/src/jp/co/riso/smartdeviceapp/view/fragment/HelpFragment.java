/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * HelpFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;

import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartprint.R;
import jp.co.riso.smartdeviceapp.view.base.BaseWebFragment;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

public class HelpFragment extends BaseWebFragment {
    /** {@inheritDoc} */
    @Override
    public int getViewLayout() {
        return R.layout.fragment_helplegal;
    }
    
    /** {@inheritDoc} */
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_help);
        
        addActionMenuButton(view);
    }
    
    /** {@inheritDoc} */
    @Override
    public void configureWebView(WebView webView) {
        
    }
    
    /** {@inheritDoc} */
    @Override
    public String getUrlString() {
        String htmlFolder = getString(R.string.html_folder);
        String helpHtml = getString(R.string.help_html);
        return AppUtils.getLocalizedAssetFullPath(getActivity(), htmlFolder, helpHtml);
    }
}
