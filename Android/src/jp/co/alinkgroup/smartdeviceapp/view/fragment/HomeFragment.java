
package jp.co.alinkgroup.smartdeviceapp.view.fragment;

import android.graphics.Color;
import android.view.View;
import jp.co.alinkgroup.smartdeviceapp.R;
import jp.co.alinkgroup.smartdeviceapp.view.base.BaseFragment;

public class HomeFragment extends BaseFragment {
    
    public HomeFragment() {
    }
    
    @Override
    public int getViewLayout() {
        return R.layout.fragment_home;
    }
    
    @Override
    public void initializeView(View view) {
    }

    @Override
    public void initializeCustomActionBar(View view) {
        super.initializeCustomActionBar(view);

        view.findViewById(R.id.actionBarLayout).setBackgroundColor(Color.TRANSPARENT);
        view.findViewById(R.id.actionBarTitle).setVisibility(View.GONE);
    }
    
    
}
