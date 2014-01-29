
package jp.co.alinkgroup.smartdeviceapp.view.fragment;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import jp.co.alinkgroup.smartdeviceapp.R;
import jp.co.alinkgroup.smartdeviceapp.view.base.BaseFragment;

public class HomeFragment extends BaseFragment implements View.OnClickListener {
    
    public static final String FRAGMENT_TAG_PRINTERS = "fragment_printers";
    public static final String FRAGMENT_TAG_PRINTJOBS = "fragment_printjobs";
    public static final String FRAGMENT_TAG_SETTINGS = "fragment_settings";
    
    public static final String KEY_STATE = "state";
    
    public static final int STATE_PRINTERS = 0;
    public static final int STATE_PRINTJOBS = 1;
    public static final int STATE_SETTINGS = 2;
    public static final int STATE_HELP = 3;
    
    public int mState = STATE_PRINTERS;
    
    public HomeFragment() {
    }
    
    @Override
    public int getViewLayout() {
        return R.layout.fragment_home;
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        
        view.findViewById(R.id.tempPrintersButton).setOnClickListener(this);
        view.findViewById(R.id.tempPrintJobsButton).setOnClickListener(this);
        view.findViewById(R.id.tempSettingsButton).setOnClickListener(this);
        
        if (savedInstanceState == null) {
            // No states were saved
            
            // Load Printer Fragment as default
            if (isTablet()) {
                switchToPrintersFragment(false);
            }
            
        } else {
            mState = savedInstanceState.getInt(KEY_STATE, STATE_PRINTERS);
            
            // No need to restore the fragment state as this is already handled
        }
    }

    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        super.initializeCustomActionBar(view, savedInstanceState);
        
        // No navigation bar
        view.findViewById(R.id.actionBarLayout).setBackgroundColor(Color.TRANSPARENT);
        view.findViewById(R.id.actionBarTitle).setVisibility(View.GONE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putInt(KEY_STATE, mState);
    }
    
    // ================================================================================
    // Public Methods
    // ================================================================================

    // ================================================================================
    // Private Methods
    // ================================================================================

    private void switchToPrintersFragment(boolean animate) {
        mState = STATE_PRINTERS;
        
        BaseFragment fragment = new PrintersFragment();
        
        switchToFragment(fragment, FRAGMENT_TAG_PRINTERS, animate);
    }

    private void switchToPrintJobsFragment(boolean animate) {
        mState = STATE_PRINTJOBS;
        
        BaseFragment fragment = new PrintJobsFragment();
        
        switchToFragment(fragment, FRAGMENT_TAG_PRINTJOBS, animate);
    }

    private void switchToSettingsFragment(boolean animate) {
        mState = STATE_SETTINGS;
        
        BaseFragment fragment = new SettingsFragment();
        
        switchToFragment(fragment, FRAGMENT_TAG_SETTINGS, animate);
    }

    private void switchToFragment(BaseFragment fragment, String tag, boolean animate) {
        FragmentManager fm = getFragmentManager();
        
        if (isTablet()) {
            if (fm.findFragmentByTag(tag) == null) {
                FragmentTransaction ft = fm.beginTransaction();
                if (animate) {
                    ft.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_right, R.animator.slide_in_right, R.animator.slide_out_right);
                }
                ft.replace(R.id.rightLayout, fragment, tag);
                ft.commit();
            }
        } else {
            FragmentTransaction ft = fm.beginTransaction();
            if (animate) {
                ft.setCustomAnimations(R.animator.zoom_in, R.animator.zoom_out, R.animator.zoom_in, R.animator.zoom_out);
            }
            ft.addToBackStack(null);
            ft.replace(R.id.homeLayout, fragment);
            ft.commit();
        }
    }

    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tempPrintersButton:
                switchToPrintersFragment(true);
                break;
            case R.id.tempPrintJobsButton:
                switchToPrintJobsFragment(true);
                break;
            case R.id.tempSettingsButton:
                switchToSettingsFragment(true);
                break;
            default:
                break;
        }
    }
}
