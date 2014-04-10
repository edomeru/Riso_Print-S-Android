/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * AddPrinterFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;

import jp.co.riso.android.dialog.DialogUtils;
import jp.co.riso.android.dialog.InfoDialogFragment;
import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.PrinterSearchCallback;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.TextView;

public class AddPrinterFragment extends BaseFragment implements PrinterSearchCallback, OnKeyListener {
    private static final int ID_MENU_SAVE_BUTTON = 0x11000004;
    private static final int ID_MENU_BACK_BUTTON = 0x11000005;
    private static final int ERR_INVALID_IP_ADDRESS = -1;
    private static final int ERR_CAN_NOT_ADD_PRINTER = -2;
    private static final String KEY_ADD_PRINTER_DIALOG = "add_printer_dialog";
    private static final InputFilter[] IP_ADDRESS_FILTER;
    
    private EditText mIpAddress = null;
    private PrinterManager mPrinterManager = null;
    private boolean mAdded = false;
    
    @Override
    public int getViewLayout() {
        return R.layout.fragment_addprinter;
    }
    
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        mAdded = false;
        mPrinterManager = PrinterManager.getInstance(SmartDeviceApp.getAppContext());
        mPrinterManager.setPrinterSearchCallback(this);
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        mIpAddress = (EditText) view.findViewById(R.id.inputIpAddress);
        mIpAddress.setBackgroundColor(getResources().getColor(R.color.theme_light_1));
        mIpAddress.setFilters(IP_ADDRESS_FILTER);
        mIpAddress.setOnKeyListener(this);
    }
    
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_add_printer);
        
        if (isTablet()) {
            int left = (int) getResources().getDimension(R.dimen.preview_view_margin);
            view.setPadding(left, 0, 0, 0);
        }
        addMenuButton(view, R.id.rightActionLayout, ID_MENU_SAVE_BUTTON, R.drawable.selector_addprinter_save, this);
        addMenuButton(view, R.id.leftActionLayout, ID_MENU_BACK_BUTTON, R.drawable.selector_actionbar_back, this);
    }
    
    // ================================================================================
    // Private Methods
    // ================================================================================
    
    private void findPrinter(String ipAddress) {
        mPrinterManager.searchPrinter(ipAddress);
    }
    
    private void dialogCb(Printer printer) {
        String title = getResources().getString(R.string.ids_lbl_printer_info);
        String msg = printer.getName() + " " + getResources().getString(R.string.ids_lbl_add_successful);
        InfoDialogFragment info = InfoDialogFragment.newInstance(title, msg, getResources().getString(R.string.ids_lbl_ok));
        DialogUtils.displayDialog(getActivity(), KEY_ADD_PRINTER_DIALOG, info);
    }
    
    private void dialogErrCb(int err) {
        String title = getResources().getString(R.string.ids_lbl_printer_info);
        String errMsg = null;
        if (err == ERR_INVALID_IP_ADDRESS) {
            errMsg = getResources().getString(R.string.ids_err_msg_invalid_ip_address);
        } else if (err == ERR_CAN_NOT_ADD_PRINTER) {
            errMsg = getResources().getString(R.string.ids_err_msg_cannot_add_printer);
        }
        InfoDialogFragment info = InfoDialogFragment.newInstance(title, errMsg, getResources().getString(R.string.ids_lbl_ok));
        DialogUtils.displayDialog(getActivity(), KEY_ADD_PRINTER_DIALOG, info);
    }
    
    private void closeScreen() {
        
        if (isTablet()) {
            if (getActivity() != null && getActivity() instanceof MainActivity) {
                final MainActivity activity = (MainActivity) getActivity();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.closeDrawers();
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
        super.onClick(v);
        switch (v.getId()) {
            case ID_MENU_BACK_BUTTON:
                closeScreen();
                break;
            case ID_MENU_SAVE_BUTTON:
                String ipAddress = mIpAddress.getText().toString();
                
                if (mPrinterManager.isExists(ipAddress)) {
                    dialogErrCb(ERR_CAN_NOT_ADD_PRINTER);
                    return;
                }
                if (!mPrinterManager.isSearching() && !ipAddress.isEmpty()) {
                    findPrinter(mIpAddress.getText().toString());
                }
                break;
        }
        AppUtils.hideSoftKeyboard(getActivity());
    }
    
    // ================================================================================
    // INTERFACE - OnPrinterSearch
    // ================================================================================
    
    @Override
    public void onPrinterAdd(Printer printer) {
        if (mPrinterManager.isExists(printer)) {
            dialogErrCb(ERR_INVALID_IP_ADDRESS);
        } else if (mPrinterManager.savePrinterToDB(printer)) {
            mAdded = true;
            closeScreen();
            dialogCb(printer);
        }
    }
    
    @Override
    public void onSearchEnd() {
        String ipAddress = mIpAddress.getText().toString();
        
        if (!mAdded && !ipAddress.isEmpty()) {
            dialogErrCb(ERR_INVALID_IP_ADDRESS);
        }
    }
    
    // ================================================================================
    // INTERFACE - OnKeyListener
    // ================================================================================
    
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            AppUtils.hideSoftKeyboard(getActivity());
            return true;
        }
        return false;
    }
    
    // ================================================================================
    // Internal Classes
    // ================================================================================
    
    private static class Ipv4Filter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            String regEx = "^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?";
            
            if (end > start) {
                String destTxt = dest.toString();
                String ipv4Address = destTxt.substring(0, dstart) + source.subSequence(start, end) + destTxt.substring(dend);
                if (!ipv4Address.matches(regEx)) {
                    return "";
                } else {
                    String[] splits = ipv4Address.split("\\.");
                    for (int i = 0; i < splits.length; i++) {
                        if (Integer.valueOf(splits[i]) > 255) {
                            return "";
                        }
                    }
                }
            }
            return null;
        }
    }
    
    static {
        IP_ADDRESS_FILTER = new InputFilter[] { new InputFilter.LengthFilter(15), new Ipv4Filter() };
    }
}