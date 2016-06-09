/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * AddPrinterFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Point;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import jp.co.riso.android.text.CommunityNameFilter;
import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartprint.R;

/**
 * @class AddPrinterFragment
 * 
 * @brief Fragment for Add Printer Screen.
 */
public class PrinterSearchSettingsFragment extends BaseFragment {
    @Override
    public int getViewLayout() {
        return R.layout.fragment_printersearchsettings;
    }
    private EditText communityName;

    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        setRetainInstance(true);
    }

    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        communityName = (EditText)view.findViewById(R.id.inputSnmpCommunityName);
        communityName.setFilters(new InputFilter[] { new CommunityNameFilter(), new InputFilter.LengthFilter(AppConstants.CONST_COMMUNITY_NAME_LIMIT) });

        if (!isTablet()) {
            Point screenSize = AppUtils.getScreenDimensions(getActivity());
            View rootView = view.findViewById(R.id.rootView);
            if (rootView == null) {
                return;
            }
            ViewGroup.LayoutParams params = rootView.getLayoutParams();
            if (screenSize.x > screenSize.y) {
                params.width = screenSize.y;
            } else {
                params.width = screenSize.x;
            }
        }
    }
    
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_search_printers_settings);
        
        if (isTablet()) {
            int leftTextPadding = (int) getResources().getDimension(R.dimen.home_title_padding);            
            textView.setPadding(leftTextPadding, 0, 0, 0);
        } else {
            addMenuButton(view, R.id.leftActionLayout, R.id.menu_id_back_button, R.drawable.selector_actionbar_back, this);
        }
    }
    
    // ================================================================================
    // Private Methods
    // ================================================================================

    /**
     * @brief Close the Add Printer screen
     */
    private void closeScreen() {

        if (isTablet()) {
            if (getActivity() != null && getActivity() instanceof MainActivity) {
                final MainActivity activity = (MainActivity) getActivity();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.closeDrawers();
                    }
                });
            }
        } else {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();
                ft.commit();
            }
        }
        AppUtils.hideSoftKeyboard(getActivity());
    }

    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.menu_id_back_button:
                closeScreen();
                break;
        }
    }
}