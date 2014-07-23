/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * BaseFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.base;

import jp.co.riso.android.util.AppUtils;
import jp.co.riso.android.util.Logger;
import jp.co.riso.smartprint.R;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import android.app.ActionBar.LayoutParams;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * @class BaseFragment
 * 
 * @brief Base fragment class
 */
public abstract class BaseFragment extends DialogFragment implements View.OnLayoutChangeListener, View.OnClickListener {
    /// Menu action button ID
    public static final int ID_MENU_ACTION_BUTTON = 0x11000001;
    private static final String KEY_ICON_STATE = "icon_state";
    private static final String KEY_ICON_ID = "icon_id";
    
    private boolean mIconState = false;
    private int mIconId = 0;
    private int mIconIdToRestore = 0;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Logger.logStartTime(getActivity(), this.getClass(), "Fragment Instance");
        
        super.onCreate(savedInstanceState);
        
        if (savedInstanceState != null){
            mIconState = savedInstanceState.getBoolean(KEY_ICON_STATE);
            mIconIdToRestore = savedInstanceState.getInt(KEY_ICON_ID);
        }
        
        initializeFragment(savedInstanceState);
        
        Logger.logStopTime(getActivity(), this.getClass(), "Fragment Instance");
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.logStartTime(getActivity(), this.getClass(), "Fragment View");
        
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
        if (getDialog() != null) {
            
            /* set height of item after title bar */
            View mainView = view.findViewById(R.id.rootView);
            
            if (mainView != null && mainView.getLayoutParams() != null) {
                
                int width = getResources().getDimensionPixelSize(R.dimen.dialog_width);
                int height = getResources().getDimensionPixelSize(R.dimen.dialog_height);
                
                mainView.getLayoutParams().width = width;
                mainView.getLayoutParams().height = height;
            }
        }
        
        // AppUtils.changeChildrenFont((ViewGroup) view, SmartDeviceApp.getAppFont());
        
        Logger.logStopTime(getActivity(), this.getClass(), "Fragment View");
        
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // restore selected only if screen is rotated
        if (mIconIdToRestore != 0 && mIconState) {
            setIconState(mIconIdToRestore, true);
        }
        mIconIdToRestore = 0;
        AppUtils.hideSoftKeyboard(getActivity());
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putBoolean(KEY_ICON_STATE, mIconState);
        outState.putInt(KEY_ICON_ID, mIconId);
        mIconIdToRestore = mIconId;
    }
    
    // ================================================================================
    // Abstract Functions
    // ================================================================================
    
    /**
     * @brief Gets the view layout id associated with this fragment.
     * 
     * @return Layout id of the view
     */
    public abstract int getViewLayout();
    
    /**
     * @brief Initialization of the fragment is performed.
     * 
     * @param savedInstanceState Bundle which contains a saved state during recreation
     */
    public abstract void initializeFragment(Bundle savedInstanceState);
    
    /**
     * @brief Initialization of the fragment view is performed.
     * 
     * @param view The view of the fragment
     * @param savedInstanceState Bundle which contains a saved state during recreation
     */
    public abstract void initializeView(View view, Bundle savedInstanceState);
    
    /**
     * @brief Initializes the custom action bar.
     * 
     * @param view The view of the fragment
     * @param savedInstanceState Bundle which contains the saved state during recreation
     */
    public abstract void initializeCustomActionBar(View view, Bundle savedInstanceState);
    
    // ================================================================================
    // Public Functions
    // ================================================================================
    
    /**
     * @brief Checks whether the device is in tablet mode.
     * 
     * @retval true Device is a tablet
     * @retval false Device is a phone
     */
    public boolean isTablet() {
        if (getActivity() == null) {
            return false;
        }
        
        return getResources().getBoolean(R.bool.is_tablet);
    }
    
    /**
     * @brief Checks whether the device is in tablet landscape mode.
     * 
     * @retval true Device is in tablet landscape mode
     * @retval false Device is not in tablet landscape mode
     */
    public boolean isTabletLand() {
        if (getActivity() == null) {
            return false;
        }
        
        return getResources().getBoolean(R.bool.is_tablet_land);
    }
    
    /**
     * @brief Adds an action menu button which by defaults draws the left drawer.
     * 
     * @param v Action bar view
     */
    public void addActionMenuButton(View v) {
        addMenuButton(v, R.id.leftActionLayout, ID_MENU_ACTION_BUTTON, R.drawable.selector_actionbar_mainmenu, this);
    }
    
    /**
     * @brief Adds a menu button with the specified parameters.
     * 
     * @param v Action bar view
     * @param layoutId Layout id of the view
     * @param viewId View id to be associated with the menu button
     * @param imageResId Image resource id of the menu button
     * @param listener OnClickListener of the menu button
     */
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
    
    /**
     * @brief Sets icon's selected state.
     * 
     * @param id Icon id
     * @param state Icon selected state
     */
    public void setIconState(int id, boolean state) {
        if (getView() != null && getView().findViewById(id) != null) {
            getView().findViewById(id).setSelected(state);
            mIconState = state;
            mIconId = id;
        }
    }
    
    /**
     * @brief Resets the icon states to not selected.
     */
    public void clearIconStates() {
        if (getView() != null) {
            View menuButton = getView().findViewById(BaseFragment.ID_MENU_ACTION_BUTTON);
            
            if (menuButton != null) {
                setIconState(BaseFragment.ID_MENU_ACTION_BUTTON, false);
            }
        }
        mIconState = false;
        mIconId = 0;
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
