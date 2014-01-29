
package jp.co.alinkgroup.smartdeviceapp.view.base;

import jp.co.alinkgroup.smartdeviceapp.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(getViewLayout(), container, false);
        
        initializeView(view, savedInstanceState);
        if (view.findViewById(R.id.actionBarLayout) != null) {
            initializeCustomActionBar(view, savedInstanceState);
        }
        
        return view;
    }
    
    // ================================================================================
    // Abstract Functions
    // ================================================================================
    
    public abstract int getViewLayout();
    
    public abstract void initializeView(View view, Bundle savedInstanceState);

    // ================================================================================
    // Public Functions
    // ================================================================================
    
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        
    }
    
    public boolean isTablet() {
        if (getActivity() == null) {
            return false;
        }
        
        return getResources().getBoolean(R.bool.isTablet);
    }
    
}
