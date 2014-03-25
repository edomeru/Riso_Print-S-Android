/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * BaseFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.base;

import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import android.app.DialogFragment;
import android.app.ActionBar.LayoutParams;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;

public abstract class BaseFragment extends DialogFragment implements View.OnLayoutChangeListener, View.OnClickListener {
    
    public static final int ID_MENU_ACTION_BUTTON = 0x11000001;
    
    /** {@inheritDoc} */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        initializeFragment(savedInstanceState);
    }

    /** {@inheritDoc} */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (getDialog() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        View view = inflater.inflate(getViewLayout(), container, false);
        
        if (view.findViewById(R.id.actionBarLayout) != null) {
            
            initializeCustomActionBar(view, savedInstanceState);
            
            // Add size change listener to prevent overlaps
            if (view.findViewById(R.id.actionBarLayout) instanceof FrameLayout) {
                view.findViewById(R.id.actionBarLayout).addOnLayoutChangeListener(this);
            }
        }
        
        // Let the action bar be initialized first
        initializeView(view, savedInstanceState);

        // set width and height of dialog
        if (getDialog() != null){
            
            /*set height of item after title bar*/
            View mainView = view.findViewById(R.id.rootView);
            
            if (mainView != null && mainView.getLayoutParams() != null){
                
                int width = getResources().getDimensionPixelSize(R.dimen.dialog_width);
                int height = getResources().getDimensionPixelSize(R.dimen.dialog_height);
                
                mainView.getLayoutParams().width = width;
                mainView.getLayoutParams().height = height;
            }
        }
        
        AppUtils.changeChildrenFont((ViewGroup)view, SmartDeviceApp.getAppFont());
        
        return view;
    }
    
    // ================================================================================
    // Abstract Functions
    // ================================================================================
    
    public abstract int getViewLayout();
    
    public abstract void initializeFragment(Bundle savedInstanceState);
    
    public abstract void initializeView(View view, Bundle savedInstanceState);
    
    public abstract void initializeCustomActionBar(View view, Bundle savedInstanceState);

    // ================================================================================
    // Public Functions
    // ================================================================================
    
    public boolean isTablet() {
        if (getActivity() == null) {
            return false;
        }
        
        return getResources().getBoolean(R.bool.is_tablet);
    }
    
    public boolean isTabletLand() {
        if (getActivity() == null) {
            return false;
        }
        
        return getResources().getBoolean(R.bool.is_tablet_land);
    }
    
    public void addActionMenuButton(View v) {
        addMenuButton(v, R.id.leftActionLayout, ID_MENU_ACTION_BUTTON, R.drawable.selector_actionbar_mainmenu, this);
    }
    
    public void addMenuButton(View v, int layoutId, int viewId, int imageResId, View.OnClickListener listener) {
        LayoutInflater li = LayoutInflater.from(v.getContext());
        ImageView button = (ImageView) li.inflate(R.layout.actionbar_button, null);
        button.setId(viewId);
        button.setImageResource(imageResId);
        button.setOnClickListener(listener);

        int width = ((BaseActivity) getActivity()).getActionBarHeight();
        ViewGroup layout = (ViewGroup) v.findViewById(layoutId);
        layout.addView(button, width, LayoutParams.MATCH_PARENT);
    }

    // ================================================================================
    // INTERFACE - View.OnLayoutChangeListener
    // ================================================================================

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (v.getId() == R.id.actionBarLayout) {
            int leftWidth = v.findViewById(R.id.leftActionLayout).getWidth();
            int rightWidth = v.findViewById(R.id.rightActionLayout).getWidth();
            
            v.findViewById(R.id.actionBarTitle).getLayoutParams().width = right - left - (Math.max(leftWidth, rightWidth) * 2);
        }
    }

    // ================================================================================
    // INTERFACE - View.OnLayoutChangeListener
    // ================================================================================

    /** {@inheritDoc} */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case ID_MENU_ACTION_BUTTON:
                if (getActivity() != null && getActivity() instanceof MainActivity) {
                    MainActivity activity = (MainActivity) getActivity();
                    activity.openDrawer(Gravity.LEFT);
                }
                break;
        }
    }
    
}
