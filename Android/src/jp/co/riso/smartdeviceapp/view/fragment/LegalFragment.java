/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * LegalFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.view.base.BaseWebFragment;

public class LegalFragment extends BaseWebFragment {
    public static final String TAG = "LegalFragment";
    
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
    @Override
    public String getUrlString() {
        String htmlFolder = getString(R.string.html_folder);
        String legalHtml = getString(R.string.legal_html);
        return AppUtils.getLocalizedAssetFullPath(getActivity(), htmlFolder, legalHtml);
    }
}
