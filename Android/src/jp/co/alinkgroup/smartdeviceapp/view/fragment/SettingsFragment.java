/*
 * Copyright (c) 2014 All rights reserved.
 *
 * SettingsFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.alinkgroup.smartdeviceapp.view.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import jp.co.alinkgroup.smartdeviceapp.R;
import jp.co.alinkgroup.smartdeviceapp.view.base.BaseFragment;

public class SettingsFragment extends BaseFragment {
    
    @Override
    public int getViewLayout() {
        return R.layout.temp_fragment_base;
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
    }

    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_settings);
    }
    
}
