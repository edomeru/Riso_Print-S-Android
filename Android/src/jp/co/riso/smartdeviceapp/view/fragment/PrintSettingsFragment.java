package jp.co.riso.smartdeviceapp.view.fragment;

import android.os.Bundle;
import android.view.View;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.model.PrintSettings;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartdeviceapp.view.printsettings.PrintSettingsView;

public class PrintSettingsFragment extends BaseFragment implements PrintSettingsView.ValueChangedListener {
    public static final String TAG = "PrintSettingsFragment";
    
    PrintSettings mPrintSettings;
    PrintSettingsView mRootView;
    Bundle printSettingsBundle = null;
    
    @Override
    public int getViewLayout() {
        return R.layout.fragment_printsettings;
    }
    
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        setRetainInstance(true);
        
        if (mPrintSettings == null) {
            mPrintSettings = new PrintSettings();
        }
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        mRootView = (PrintSettingsView) view.findViewById(R.id.rootView);
        
        mRootView.setValueChangedListener(this);
        mRootView.setPrintSettings(mPrintSettings);
        
        if (printSettingsBundle != null) {
            mRootView.restoreState(printSettingsBundle);
            printSettingsBundle = null;
        }
    }
    
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        if (mRootView != null) {
            printSettingsBundle = new Bundle();
            mRootView.saveState(printSettingsBundle);
        }
    }
    
    public void setPrintSettings(PrintSettings printSettings) {
        mPrintSettings = new PrintSettings(printSettings);
    }
    
    // ================================================================================
    // INTERFACE - ValueChangedListener
    // ================================================================================
    
    @Override
    public void onPrintSettingsValueChanged(PrintSettings printSettings) {
        setPrintSettings(printSettings);
    }
}
