
package jp.co.alinkgroup.smartdeviceapp.view.fragment;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import jp.co.alinkgroup.smartdeviceapp.R;
import jp.co.alinkgroup.smartdeviceapp.view.base.BaseFragment;

public class PrintersFragment extends BaseFragment implements View.OnClickListener {

    public static final String FRAGMENT_TAG_PRINTER_SEARCH = "fragment_printer_search";
    
    @Override
    public int getViewLayout() {
        return R.layout.fragment_printers;
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        
        view.findViewById(R.id.tempPrinterSearchButton).setOnClickListener(this);
    }

    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_printers);
    }
    
    // ================================================================================
    // Private Methods
    // ================================================================================

    private void displayPrinterSearchFragment() {
        BaseFragment fragment = new PrinterSearchFragment();
        
        switchToFragment(fragment, FRAGMENT_TAG_PRINTER_SEARCH);
    }

    public void switchToFragment(BaseFragment fragment, String tag) {
        FragmentManager fm = getFragmentManager();
        
        if (isTablet()) {

            fragment.show(fm, tag);
        } else {
            FragmentTransaction ft = fm.beginTransaction();
            ft.setCustomAnimations(R.animator.zoom_in, R.animator.zoom_out, R.animator.zoom_in, R.animator.zoom_out);
            ft.addToBackStack(null);
            ft.replace(R.id.mainLayout, fragment, tag);
            ft.commit();
        }
    }
    
    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================
    
    /** {@inheritDoc} */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tempPrinterSearchButton:
                displayPrinterSearchFragment();
                break;
            default:
                break;
        }
        
    }
}
