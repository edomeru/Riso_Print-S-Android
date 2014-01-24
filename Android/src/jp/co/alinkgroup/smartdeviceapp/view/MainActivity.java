/*
 * Copyright (c) 2014 All rights reserved.
 *
 * MainActivity.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.alinkgroup.smartdeviceapp.view;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import jp.co.alinkgroup.smartdeviceapp.R;
import jp.co.alinkgroup.smartdeviceapp.view.base.BaseActivity;
import jp.co.alinkgroup.smartdeviceapp.view.fragment.HomeFragment;

public class MainActivity extends BaseActivity {
    
    @Override
    protected void onCreateContent(Bundle savedInstanceState) {

        setContentView(R.layout.activity_main);
        
        // Begin Fragments
        if (savedInstanceState == null) {
            Fragment homeFragment = new HomeFragment();
            
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            
            ft.add(R.id.homeLayout, homeFragment);
            
            ft.commit();
        }
    }
    
    
    
}
