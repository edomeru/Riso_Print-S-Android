package jp.co.riso.smartdeviceapp.view.fragment;

import android.os.Bundle;
import android.view.View;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;

public class PrintSettingsFragment extends BaseFragment {
    
    @Override
    public int getViewLayout() {
        // TODO Auto-generated method stub
        return R.layout.fragment_printsettings;
    }
    
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        MainActivity mainActivity = (MainActivity) getActivity();
        view.getLayoutParams().width = mainActivity.getDrawerWidth();
    }
    
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        
    }
    
}
