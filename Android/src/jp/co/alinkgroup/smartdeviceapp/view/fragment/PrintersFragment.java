
package jp.co.alinkgroup.smartdeviceapp.view.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import jp.co.alinkgroup.smartdeviceapp.R;
import jp.co.alinkgroup.smartdeviceapp.view.base.BaseFragment;

public class PrintersFragment extends BaseFragment {
    
    @Override
    public int getViewLayout() {
        return R.layout.fragment_printers;
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
    }

    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        super.initializeCustomActionBar(view, savedInstanceState);
        
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_printers);
    }
    
}
