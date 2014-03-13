package jp.co.riso.smartdeviceapp.view.fragment;

import android.os.Bundle;
import android.view.View;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartdeviceapp.view.preview.PrintSettingsView;

public class PrintSettingsFragment extends BaseFragment {
    public static final String TAG = "PrintSettingsFragment";
    
    boolean mDefaultSettings;
    
    PrintSettingsView mRootView;
    
    @Override
    public int getViewLayout() {
        return R.layout.fragment_printsettings;
    }
    
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        mRootView = (PrintSettingsView) view.findViewById(R.id.rootView);
    }
    
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
    }
    
}
