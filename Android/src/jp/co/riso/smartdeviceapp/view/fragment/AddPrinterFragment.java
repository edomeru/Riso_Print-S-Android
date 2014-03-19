
package jp.co.riso.smartdeviceapp.view.fragment;

import jp.co.riso.android.dialog.DialogUtils;
import jp.co.riso.android.dialog.InfoDialogFragment;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.OnPrinterSearch;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartdeviceapp.view.base.BaseActivity;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartdeviceapp.view.fragment.PrintersFragment.PrinteSearchTabletInterface;
import android.app.ActionBar.LayoutParams;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class AddPrinterFragment extends BaseFragment implements OnPrinterSearch {
    
    
    private EditText mIpAddress = null;
    PrinterManager mPrinterManager = null;
    private boolean mIsSearching = false;
    private boolean mAdded = false;
    private static final String KEY_ADD_PRINTER_DIALOG = "add_printer_dialog";
    private static final String KEY_SEARCH_STATE = "add_searched_state";
    
    public final int ID_MENU_SAVE_BUTTON    = 0x11000004;
    
    //Interface
    private PrinteSearchTabletInterface mPrinteSearchTabletInterface = null;
    
    public void setPrinteSearchTabletInterface(PrinteSearchTabletInterface printeSearchTabletInterface) {
        mPrinteSearchTabletInterface = printeSearchTabletInterface;
    }
    
    @Override
    public int getViewLayout() {
        return R.layout.fragment_addprinter;
    }
    
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        mAdded = false;
        mPrinterManager = PrinterManager.sharedManager(getActivity());
        mPrinterManager.setOnPrinterSearchListener(this);
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        mIpAddress = (EditText) view.findViewById(R.id.inputIpAddress);
        mIpAddress.setBackgroundColor(getResources().getColor(R.color.theme_light_1));
    }
    
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        if(isTablet()) {
            view.setPadding((int) (getResources().getDimension(R.dimen.preview_view_margin)), 0, 0, 0);
        }
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_add_printer);
        
        addActionMenuButton(view);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null) {
            mIsSearching = savedInstanceState.getBoolean(KEY_SEARCH_STATE, false);
            if(mIsSearching) {
                findPrinter(mIpAddress.getText().toString());
            }
        }
        
        InputFilter[] ipv4Filter = new InputFilter[2];
        //IPv4 
        ipv4Filter[0] = new InputFilter.LengthFilter(15);
        ipv4Filter[1] = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                String regEx = "^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?";
                if (end > start) {
                    String destTxt = dest.toString();
                    String ipv4Address = destTxt.substring(0, dstart) +
                            source.subSequence(start, end) + destTxt.substring(dend);
                    if (!ipv4Address.matches (regEx)) { 
                        return "";
                    } else {
                        String[] splits = ipv4Address.split("\\.");
                        for (int i=0; i < splits.length; i++) {
                            if (Integer.valueOf(splits[i]) > 255) {
                                return "";
                            }
                        }
                    }
                }
                return null;
            }
        };
        
        mIpAddress.setFilters(ipv4Filter);
    }
    
    @Override
    public void addActionMenuButton(View v) {
        int width = ((BaseActivity)getActivity()).getActionBarHeight();
        ImageButton actionMenuButton = new ImageButton(v.getContext());
        ImageButton saveMenuButton = new ImageButton(v.getContext());
        ViewGroup leftActionLayout = (ViewGroup) v.findViewById(R.id.leftActionLayout);
        ViewGroup rightActionLayout = (ViewGroup) v.findViewById(R.id.rightActionLayout);
        
        int padding = getResources().getDimensionPixelSize(R.dimen.actionbar_icon_padding);
        
        //Back Button
        actionMenuButton.setId(ID_MENU_ACTION_BUTTON);
        actionMenuButton.setImageResource(R.drawable.selector_actionbar_back);
        actionMenuButton.setBackgroundResource(R.color.theme_color_2);
        actionMenuButton.setScaleType(ScaleType.FIT_CENTER);
        actionMenuButton.setPadding(padding, padding, padding, padding);
        actionMenuButton.setOnClickListener(this);
        
        //Save Button
        saveMenuButton.setId(ID_MENU_SAVE_BUTTON);
        saveMenuButton.setImageResource(R.drawable.temp_img_btn_save_printer);
        saveMenuButton.setBackgroundResource(R.color.theme_color_2);
        actionMenuButton.setScaleType(ScaleType.FIT_CENTER);
        saveMenuButton.setPadding(padding, padding, padding, padding);
        saveMenuButton.setOnClickListener(this);
        
        leftActionLayout.addView(actionMenuButton, width, LayoutParams.MATCH_PARENT);
        rightActionLayout.addView(saveMenuButton, width, LayoutParams.MATCH_PARENT);
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(KEY_SEARCH_STATE, mIsSearching);
        super.onSaveInstanceState(savedInstanceState);
    }
    
    // ================================================================================
    // Private Methods
    // ================================================================================
    private void findPrinter(String ipAddress) {
        if(ipAddress.isEmpty()) {
            mIsSearching = false;
            return;
        }
        mIsSearching = true;
        mPrinterManager.searchPrinter(ipAddress);
    }
    
    private void dialogCb(Printer printer) {
        // TODO: Get values from resources 
        // Open Dialog box 
        InfoDialogFragment info = InfoDialogFragment.newInstance("Printer Info", 
                printer.getName() + " was added successfully", 
                getResources().getString(R.string.ids_lbl_ok));
        DialogUtils.displayDialog(getActivity(), KEY_ADD_PRINTER_DIALOG, info);
    }
    
    private void dialogErrCb(Printer printer) {
        // TODO: Get values from resources 
        // Open Dialog box 
        InfoDialogFragment info = InfoDialogFragment.newInstance("Printer Info", 
                printer.getName() + " already exsists", 
                getResources().getString(R.string.ids_lbl_ok));
        DialogUtils.displayDialog(getActivity(), KEY_ADD_PRINTER_DIALOG, info);
    }
    
    private void dialogErrCb(String ipAddress) {
        // TODO: Get values from resources 
        // Open Dialog box 
        InfoDialogFragment info = InfoDialogFragment.newInstance("Printer Info", 
                ipAddress + " Request TIMEDOUT", 
                getResources().getString(R.string.ids_lbl_ok));
        DialogUtils.displayDialog(getActivity(), KEY_ADD_PRINTER_DIALOG, info);
    }
    private void closeScreen() {
        if (isTablet()) {
            if (getActivity() != null && getActivity() instanceof MainActivity) {               
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            MainActivity activity = (MainActivity) getActivity();              
                            activity.closeDrawers();        
                            mPrinteSearchTabletInterface.refreshPrintersList();
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } else {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();
                ft.commit();
            }
        }
    }
    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================
    @Override
    public void onClick(View v) {
        if(v.getId() == ID_MENU_ACTION_BUTTON) {
            closeScreen();
        }
        else if (v.getId() == ID_MENU_SAVE_BUTTON) {
            if(!mIsSearching) {
                findPrinter(mIpAddress.getText().toString());
            }
        }
    }
    
    // ================================================================================
    // INTERFACE - OnPrinterSearch
    // ================================================================================
    @Override
    public void onPrinterAdd(Printer printer) {
        if(!mIsSearching)
            return;
        
        if(mPrinterManager.isExists(printer)) {
            dialogErrCb(printer);
        }
        else {
            if(mPrinterManager.savePrinterToDB(printer) != -1) {
                mAdded = true;
                dialogCb(printer);
                closeScreen();
            }
        }
    }
    
    @Override
    public void onSearchEnd() {
        String ipAddress = mIpAddress.getText().toString();
        mIsSearching = false;
        if(!mAdded && !ipAddress.isEmpty()) {
            dialogErrCb(ipAddress);
        }
        return;
    }
    
    
}