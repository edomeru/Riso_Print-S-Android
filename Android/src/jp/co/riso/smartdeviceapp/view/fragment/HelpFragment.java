/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * HelpFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;

import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.view.base.BaseWebFragment;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class HelpFragment extends BaseWebFragment {
    public static final String TAG = "HelpFragment";
    
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

        // TODO: Move this on actual
        // Set version text
        try {
            PackageManager packageManager = getActivity().getPackageManager();
            String versionName = packageManager.getPackageInfo(getActivity().getPackageName(), 0).versionName;
            String help = getString(R.string.ids_lbl_help);
            textView.setText(help + " " + versionName);
            textView.setVisibility(View.VISIBLE);
        } catch (NameNotFoundException e) {
            Log.w(TAG, "No version name found");
        }
        
        addActionMenuButton(view);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getUrlString() {
        String htmlFolder = getString(R.string.html_folder);
        String helpHtml = getString(R.string.help_html);
        return AppUtils.getLocalizedAssetFullPath(getActivity(), htmlFolder, helpHtml);
    }
}
