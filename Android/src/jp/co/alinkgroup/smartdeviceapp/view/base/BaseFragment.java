
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
        
        initializeView(view);
        if (view.findViewById(R.layout.actionbar_main) != null) {
            initializeCustomActionBar(view);
        }
        
        return super.onCreateView(inflater, container, savedInstanceState);
    }
    
    // ================================================================================
    // Abstract Functions
    // ================================================================================
    
    public abstract int getViewLayout();
    
    public abstract void initializeView(View view);

    // ================================================================================
    // Public Functions
    // ================================================================================
    
    public void initializeCustomActionBar(View view) {
        
    }
}
