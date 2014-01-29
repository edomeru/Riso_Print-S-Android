
package jp.co.alinkgroup.smartdeviceapp.view.fragment;

import android.app.Fragment;
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
            switchToPrintersFragment();
            
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

    private void switchToPrintersFragment() {
        mState = STATE_PRINTERS;
        
        Fragment fragment = new PrintersFragment();
        
        switchToFragment(fragment, FRAGMENT_TAG_PRINTERS);
    }

    private void switchToPrintJobsFragment() {
        mState = STATE_PRINTJOBS;
        
        Fragment fragment = new PrintJobsFragment();
        
        switchToFragment(fragment, FRAGMENT_TAG_PRINTJOBS);
    }

    private void switchToSettingsFragment() {
        mState = STATE_SETTINGS;
        
        Fragment fragment = new SettingsFragment();
        
        switchToFragment(fragment, FRAGMENT_TAG_SETTINGS);
    }

    // ================================================================================
    // Private Methods
    // ================================================================================

    public void switchToFragment(Fragment fragment, String tag) {
        FragmentManager fm = getFragmentManager();
        
        if (isTablet()) {
            if (fm.findFragmentByTag(tag) == null) {
                FragmentTransaction ft = fm.beginTransaction();
                ft.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_right, R.animator.slide_in_right, R.animator.slide_out_right);
                ft.replace(R.id.rightLayout, fragment, tag);
                ft.commit();
            }
        } else {
            FragmentTransaction ft = fm.beginTransaction();
            ft.setCustomAnimations(R.animator.zoom_in, R.animator.zoom_out, R.animator.zoom_in, R.animator.zoom_out);
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
                switchToPrintersFragment();
                break;
            case R.id.tempPrintJobsButton:
                switchToPrintJobsFragment();
                break;
            case R.id.tempSettingsButton:
                switchToSettingsFragment();
                break;
            default:
                break;
        }
        
    }

}
