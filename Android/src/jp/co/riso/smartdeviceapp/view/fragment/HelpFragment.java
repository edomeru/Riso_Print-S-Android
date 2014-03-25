/*
 * Copyright (c) 2014 All rights reserved.
 *
 * HelpFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;

import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;

public class HelpFragment extends BaseFragment {
    public static final String TAG = "HelpFragment";
    
    @Override
    public int getViewLayout() {
        return R.layout.fragment_helplegal;
    }
    
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        // Set version text
        try {
            PackageManager packageManager = getActivity().getPackageManager();
            String versionName = packageManager.getPackageInfo(getActivity().getPackageName(), 0).versionName;

            TextView textView = (TextView) view.findViewById(R.id.tempVersionLabel);
            textView.setText(versionName);
            textView.setVisibility(View.VISIBLE);
        } catch (NameNotFoundException e) {
            Log.w(TAG, "No version name found");
        }
    }
    
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_help);
        
        addActionMenuButton(view);
    }
    
}
