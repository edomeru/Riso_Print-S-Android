/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrinterInfoFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;

import java.util.Timer;
import java.util.TimerTask;

import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

public class PrinterInfoFragment extends BaseFragment implements OnCheckedChangeListener {
    private static final String FRAGMENT_TAG_PRINTERS = "fragment_printers";
    public static final String KEY_PRINTER_INFO = "fragment_printer_info";
    public static final String KEY_PRINTER_INFO_NAME = "fragment_printer_info_name";
    public static final String KEY_PRINTER_INFO_ADDRESS = "fragment_printer_info_address";
    public static final String KEY_PRINTER_INFO_ID = "fragment_printer_info_defualt";
    private static final int ID_MENU_ACTION_PRINT_SETTINGS_BUTTON = 0x11000004;
    private static final int ID_MENU_BACK_BUTTON = 0x11000005;
    
    private Printer mPrinter = null;
    
    private TextView mPrinterName = null;
    private TextView mIpAddress = null;
    private TextView mStatus = null;
    private Switch mDefaultPrinter = null;
    private Spinner mPort = null;
    
    private PrinterManager mPrinterManager = null;
    private PrintSettingsFragment mPrintSettingsFragment = null;
    
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
        mStatus = (TextView) view.findViewById(R.id.inputStatus);
        mPort = (Spinner) view.findViewById(R.id.inputPort);
        
        ArrayAdapter<String> portAdapter = new ArrayAdapter<String>(getActivity(), R.layout.printerinfo_port_item);
        portAdapter.add(getString(R.string.ids_lbl_port_raw));
        portAdapter.add(getString(R.string.ids_lbl_port_lpr));
        portAdapter.setDropDownViewResource(R.layout.printerinfo_port_dropdownitem);
        mPort.setAdapter(portAdapter);
    }
    
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_printer_info);
        
        addMenuButton(view, R.id.rightActionLayout, ID_MENU_ACTION_PRINT_SETTINGS_BUTTON, R.drawable.selector_actionbar_printerinfo, this);
        addMenuButton(view, R.id.leftActionLayout, ID_MENU_BACK_BUTTON, R.drawable.selector_actionbar_back, this);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            if (mPrinter == null) {
                mPrinter = mPrinterManager.getSavedPrintersList().get(savedInstanceState.getInt(KEY_PRINTER_INFO_ID) - 1);
            }
        }
        mPrinterName.setText(mPrinter.getName());
        mIpAddress.setText(mPrinter.getIpAddress());
        if (mPrinterManager.getDefaultPrinter() == mPrinter.getId()) {
            mDefaultPrinter.setChecked(true);
        }
        
        updateOnlineStatus();
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(KEY_PRINTER_INFO_NAME, mPrinter.getName());
        savedInstanceState.putString(KEY_PRINTER_INFO_ADDRESS, mPrinter.getName());
        savedInstanceState.putInt(KEY_PRINTER_INFO_ID, mPrinter.getId());
        super.onSaveInstanceState(savedInstanceState);
    }
    
    // ================================================================================
    // Public Methods
    // ================================================================================
    
    public void setPrinter(Printer printer) {
        mPrinter = printer;
    }
    
    // ================================================================================
    // Private Methods
    // ================================================================================
    
    public void updateOnlineStatus() {
        Timer updateStatus = new Timer();
        updateStatus.schedule(new TimerTask() {
            
            @Override
            public void run() {
                try {
                    if (mPrinterManager.isOnline(mPrinter.getIpAddress())) {
                        mStatus.setText(getString(R.string.ids_lbl_printer_status_online));
                    }
                } catch (Exception e) {
                    mStatus.setText(getString(R.string.ids_lbl_printer_status_offline));
                }
            }
        }, 0, AppConstants.CONST_UPDATE_INTERVAL);
    }
    
    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case ID_MENU_ACTION_PRINT_SETTINGS_BUTTON:
                if (getActivity() != null && getActivity() instanceof MainActivity) {
                    MainActivity activity = (MainActivity) getActivity();
                    
                    if (!activity.isDrawerOpen(Gravity.RIGHT)) {
                        FragmentManager fm = getFragmentManager();
                        
                        mPrintSettingsFragment = null;
                        
                        if (mPrintSettingsFragment == null) {
                            FragmentTransaction ft = fm.beginTransaction();
                            mPrintSettingsFragment = new PrintSettingsFragment();
                            ft.replace(R.id.rightLayout, mPrintSettingsFragment, PrintPreviewFragment.FRAGMENT_TAG_PRINTSETTINGS);
                            ft.commit();
                        }
                        mPrintSettingsFragment.setPrinterId(mPrinter.getId());
                        // use new print settings retrieved from the database
                        mPrintSettingsFragment.setPrintSettings(new PrintSettings(mPrinter.getId()));
                        
                        PrintersFragment printersFragment = (PrintersFragment) fm.findFragmentByTag(FRAGMENT_TAG_PRINTERS);
                        mPrintSettingsFragment.setTargetFragment(printersFragment, 0);
                        activity.openDrawer(Gravity.RIGHT, false);
                    } else {
                        activity.closeDrawers();
                    }
                }
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