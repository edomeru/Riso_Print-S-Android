/*
 * Copyright (c) 2014 All rights reserved.
 *
 * LegalFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;

public class LegalFragment extends BaseFragment {
    
    @Override
    public int getViewLayout() {
        return R.layout.fragment_helplegal;
    }
    
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
    }
    
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_legal);
        
        addActionMenuButton(view);
    }
    
}
