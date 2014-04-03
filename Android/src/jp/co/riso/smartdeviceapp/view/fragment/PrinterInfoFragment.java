/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrinterInfoFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;

import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

public class PrinterInfoFragment extends BaseFragment implements OnCheckedChangeListener {
    public static final String KEY_PRINTER_INFO = "fragment_printer_info";
    private static final int ID_MENU_ACTION_PRINT_SETTINGS_BUTTON = 0x11000004;
    private static final int ID_MENU_BACK_BUTTON = 0x11000005;
    
    private Printer mPrinter = null;
    private TextView mPrinterName = null;
    private TextView mIpAddress = null;
    private Switch mDefaultPrinter = null;
    
    private PrinterManager mPrinterManager = null;
    
    @Override
    public int getViewLayout() {
        return R.layout.fragment_printerinfo;
    }
    
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        mPrinterManager = PrinterManager.getInstance(SmartDeviceApp.getAppContext());
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        mDefaultPrinter = (Switch) view.findViewById(R.id.default_printer_switch);
        mDefaultPrinter.setOnCheckedChangeListener(this);
        
        mPrinterName = (TextView) view.findViewById(R.id.inputPrinterName);
        mIpAddress = (TextView) view.findViewById(R.id.inputIpAddress);
    }
    
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_printer_info);
        
        addMenuButton(view, R.id.rightActionLayout, ID_MENU_ACTION_PRINT_SETTINGS_BUTTON, R.drawable.img_btn_default_print_settings, this);
        addMenuButton(view, R.id.leftActionLayout, ID_MENU_BACK_BUTTON, R.drawable.selector_actionbar_back, this);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle extras = getArguments();
        if (extras == null) {
            extras = getActivity().getIntent().getExtras();
        }
        mPrinter = extras.getParcelable(KEY_PRINTER_INFO);
        
        mPrinterName.setText(mPrinter.getName());
        mIpAddress.setText(mPrinter.getIpAddress());
        if (mPrinterManager.getDefaultPrinter() == mPrinter.getId()) {
            mDefaultPrinter.setChecked(true);
        }
        
    }
    
    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case ID_MENU_ACTION_PRINT_SETTINGS_BUTTON:
                break;
            case ID_MENU_BACK_BUTTON:
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack();
                    ft.commit();
                }
                break;
        }
    }
    
    // ================================================================================
    // INTERFACE - onCheckedChanged
    // ================================================================================
    
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            mPrinterManager.setDefaultPrinter(mPrinter);
        } else {
            mPrinterManager.clearDefaultPrinter();
        }
    }
}