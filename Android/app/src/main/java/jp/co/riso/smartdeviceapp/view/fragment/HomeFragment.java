/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * HomeFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;

import jp.co.riso.smartprint.R;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;

/**
 * @class HomeFragment
 * 
 * @brief Web fragment class for Home Screen.
 */
public class HomeFragment extends BaseFragment implements View.OnClickListener {
    
    /// Print Preview Screen
    public static final int STATE_PRINTPREVIEW = 0;
    /// Printers Screen
    public static final int STATE_PRINTERS = 1;
    /// Print Jobs Screen
    public static final int STATE_PRINTJOBS = 2;
    /// Settings Screen
    public static final int STATE_SETTINGS = 3;
    /// Help Screen
    public static final int STATE_HELP = 4;
    /// Legal Screen
    public static final int STATE_LEGAL = 5;
    
    /// Home Fragment key state
    public static final String KEY_STATE = "HomeFragment_State";
    
    public static int MENU_ITEMS[] = {
        R.id.printPreviewButton,
        R.id.printersButton,
        R.id.printJobsButton,
        R.id.settingsButton,
        R.id.helpButton,
        R.id.legalButton
    };
    
    public static String FRAGMENT_TAGS[] = {
        "fragment_printpreview",
        "fragment_printers",
        "fragment_printjobs",
        "fragment_settings",
        "fragment_help",
        "fragment_legal"
    };
    
    public int mState = STATE_PRINTPREVIEW;
    
    @Override
    public int getViewLayout() {
        return R.layout.fragment_home;
    }
    
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        view.findViewById(R.id.printPreviewButton).setOnClickListener(this);
        view.findViewById(R.id.printersButton).setOnClickListener(this);
        view.findViewById(R.id.printJobsButton).setOnClickListener(this);
        view.findViewById(R.id.settingsButton).setOnClickListener(this);
        view.findViewById(R.id.helpButton).setOnClickListener(this);
        view.findViewById(R.id.legalButton).setOnClickListener(this);
        
        if (savedInstanceState == null) {
            // No states were saved
            //setCurrentState(STATE_PRINTPREVIEW, false);
            mState = STATE_PRINTPREVIEW;
        } else {
            mState = savedInstanceState.getInt(KEY_STATE, STATE_PRINTERS);
            // No need to restore the fragment state as this is already handled
        }
        setSelectedButton(view, mState);
    }
    
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        //This has no custom action bar
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putInt(KEY_STATE, mState);
    }
    
    // ================================================================================
    // Private Methods
    // ================================================================================
    
    /**
     * @brief This method sets the state of the selected button.
     * 
     * @param view Parent view
     * @param state Fragment state
     */
    private void setSelectedButton(View view, int state) {
        if (view == null) {
            return;
        }
        if (state < 0 || state >= MENU_ITEMS.length) {
            return;
        }
        
        for (int i = 0; i < MENU_ITEMS.length; i++) {
            view.findViewById(MENU_ITEMS[i]).setSelected(false);
            view.findViewById(MENU_ITEMS[i]).setClickable(true);
        }
        
        view.findViewById(MENU_ITEMS[state]).setSelected(true);
        view.findViewById(MENU_ITEMS[state]).setClickable(false);
    }
    
    /**
     * @brief This method sets the state of the Home Fragment.
     * 
     * @param state Fragment state
     */
    private void setCurrentState(int state) {
        setCurrentState(state, true);
    }
    
    /**
     * @brief This method sets the state of the Home Fragment.
     * 
     * @param state Fragment state
     * @param animate Animate changes in layout
     */
    private void setCurrentState(int state, boolean animate) {
        if (mState != state) {
            setSelectedButton(getView(), state);
            switchToFragment(state, animate);
            mState = state;
        }
        
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            activity.closeDrawers();
        }
    }
    
    /**
     * @brief Switch to fragment.
     * 
     * @param state Fragment state
     * @param animate Animate changes in layout
     */
    private void switchToFragment(int state, boolean animate) {
        FragmentManager fm = getFragmentManager();
        fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        
        FragmentTransaction ft = fm.beginTransaction();
        
        Fragment container = fm.findFragmentById(R.id.mainLayout);
        if (container != null) {
            if (container.getRetainInstance()) {
                ft.detach(container);
            } else {
                ft.remove(container);
            }
        }
        
        String tag = FRAGMENT_TAGS[state];
        
        // Check retained fragments
        BaseFragment fragment = (BaseFragment) fm.findFragmentByTag(tag);
        if (fragment == null) {
            switch (state) {
                case STATE_PRINTPREVIEW:
                    fragment = new PrintPreviewFragment();
                    break;
                case STATE_PRINTERS:
                    fragment = new PrintersFragment();
                    break;
                case STATE_PRINTJOBS:
                    fragment = new PrintJobsFragment();
                    break;
                case STATE_SETTINGS:
                    fragment = new SettingsFragment();
                    break;
                case STATE_HELP:
                    fragment = new HelpFragment();
                    break;
                case STATE_LEGAL:
                    fragment = new LegalFragment();
                    break;
            }
            
            ft.add(R.id.mainLayout, fragment, tag);
        } else {
            ft.attach(fragment);
        }
        
        if (fragment instanceof BaseFragment) {
            setIconState(R.id.menu_id_action_button, true);
        }
        
        ft.commit();
    }
    
    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.printPreviewButton:
                setCurrentState(STATE_PRINTPREVIEW);
                break;
            case R.id.printersButton:
                setCurrentState(STATE_PRINTERS);
                break;
            case R.id.printJobsButton:
                setCurrentState(STATE_PRINTJOBS);
                break;
            case R.id.settingsButton:
                setCurrentState(STATE_SETTINGS);
                break;
            case R.id.helpButton:
                setCurrentState(STATE_HELP);
                break;
            case R.id.legalButton:
                setCurrentState(STATE_LEGAL);
                break;
            default:
                break;
        }
    }
}
