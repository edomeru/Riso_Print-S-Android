/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrinterInfoFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;

import java.util.List;

import jp.co.riso.android.os.pauseablehandler.PauseableHandler;
import jp.co.riso.android.os.pauseablehandler.PauseableHandlerCallback;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.model.Printer.PortSetting;
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartprint.R;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

public class PrinterInfoFragment extends BaseFragment implements OnCheckedChangeListener, OnItemSelectedListener, PauseableHandlerCallback {
    private static final String FRAGMENT_TAG_PRINTERS = "fragment_printers";
    public static final String KEY_PRINTER_INFO = "fragment_printer_info";
    public static final String KEY_PRINTER_INFO_ID = "fragment_printer_info_id";
    public static final int ID_MENU_ACTION_PRINT_SETTINGS_BUTTON = 0x11000004;
    private static final int ID_MENU_BACK_BUTTON = 0x11000005;
    
    private Printer mPrinter = null;
    
    private TextView mPrinterName = null;
    private TextView mIpAddress = null;
    private Switch mDefaultPrinter = null;
    private ImageView mDefaultView = null;
    private Spinner mPort = null;
    
    private PrinterManager mPrinterManager = null;
    private PrintSettingsFragment mPrintSettingsFragment = null;
    private PauseableHandler mPauseableHandler = null;

    /** {@inheritDoc} */
    @Override
    public int getViewLayout() {
        return R.layout.fragment_printerinfo;
    }
    
    /** {@inheritDoc} */
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        mPrinterManager = PrinterManager.getInstance(SmartDeviceApp.getAppContext());
        mPauseableHandler = new PauseableHandler(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        mDefaultView = (ImageView) view.findViewById(R.id.img_default);
        mDefaultPrinter = (Switch) view.findViewById(R.id.default_printer_switch);
        mDefaultPrinter.setOnCheckedChangeListener(this);
        
        mPrinterName = (TextView) view.findViewById(R.id.inputPrinterName);
        mIpAddress = (TextView) view.findViewById(R.id.inputIpAddress);
        mPort = (Spinner) view.findViewById(R.id.inputPort);
        mPort.setOnItemSelectedListener(this);
        
        ArrayAdapter<String> portAdapter = new ArrayAdapter<String>(getActivity(), R.layout.printerinfo_port_item);
        // Assumption is that LPR is always available
        portAdapter.add(getString(R.string.ids_lbl_port_lpr));
        if (mPrinter.getConfig().isRawAvailable()) {
            portAdapter.add(getString(R.string.ids_lbl_port_raw));
        }
        portAdapter.setDropDownViewResource(R.layout.printerinfo_port_dropdownitem);
        mPort.setAdapter(portAdapter);
        mPort.setSelection(mPrinter.getPortSetting().ordinal());
    }
    
    /** {@inheritDoc} */
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_printer_info);
        
        addMenuButton(view, R.id.rightActionLayout, ID_MENU_ACTION_PRINT_SETTINGS_BUTTON, R.drawable.selector_actionbar_printerinfo, this);
        addMenuButton(view, R.id.leftActionLayout, ID_MENU_BACK_BUTTON, R.drawable.selector_actionbar_back, this);
    }
    
    /** {@inheritDoc} */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            if (mPrinter == null) {
                List<Printer> printersList = mPrinterManager.getSavedPrintersList();
                int printerId = savedInstanceState.getInt(KEY_PRINTER_INFO_ID);
                for (Printer printer : printersList) {
                    if (printer.getId() == printerId) {
                        mPrinter = printer;
                    }
                }
            }
        }
        String printerName = mPrinter.getName();
        if(printerName == null || printerName.isEmpty()) {
            printerName = getActivity().getResources().getString(R.string.ids_lbl_no_name);
        }
        mPrinterName.setText(printerName);
        mIpAddress.setText(mPrinter.getIpAddress());
        if (mPrinterManager.getDefaultPrinter() == mPrinter.getId()) {
            mDefaultPrinter.setVisibility(View.GONE);
            mDefaultView.setVisibility(View.VISIBLE);
        }
        mPort.setSelection(mPrinter.getPortSetting().ordinal());
    }
    
    /** {@inheritDoc} */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(KEY_PRINTER_INFO_ID, mPrinter.getId());
        super.onSaveInstanceState(savedInstanceState);
    }

    /** {@inheritDoc} */
    @Override
    public void clearIconStates() {
        super.clearIconStates();
        setIconState(ID_MENU_ACTION_PRINT_SETTINGS_BUTTON, false);
    }
  
    /** {@inheritDoc} */
    @Override
    public void onResume() {
        super.onResume();
        mPauseableHandler.resume();
    }

    /** {@inheritDoc} */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        if (mPauseableHandler != null) {
            mPauseableHandler.resume();
        }
    }
    
    // ================================================================================
    // Public Methods
    // ================================================================================
    
    /**
     * Sets the printer object to be displayed by the Printer Info Screen
     */
    public void setPrinter(Printer printer) {
        mPrinter = printer;
    }

    // ================================================================================
    // INTERFACE - PauseableHandlerCallback
    // ================================================================================
    
    /** {@inheritDoc} */
    @Override
    public boolean storeMessage(Message msg) {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void processMessage(Message msg) {
        switch (msg.what) {
            case ID_MENU_ACTION_PRINT_SETTINGS_BUTTON:
                mPauseableHandler.pause();
                if (getActivity() != null && getActivity() instanceof MainActivity) {
                    MainActivity activity = (MainActivity) getActivity();
                    
                    if (!activity.isDrawerOpen(Gravity.RIGHT)) {
                        View v = (View) msg.obj;
                        FragmentManager fm = getFragmentManager();
                        setIconState(v.getId(), true);
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
                mPauseableHandler.pause();
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
    // INTERFACE - View.OnClickListener
    // ================================================================================
    
    /** {@inheritDoc} */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case ID_MENU_ACTION_PRINT_SETTINGS_BUTTON:
                Message newMessage = Message.obtain(mPauseableHandler, ID_MENU_ACTION_PRINT_SETTINGS_BUTTON);
                newMessage.obj = v;
                mPauseableHandler.sendMessage(newMessage);
                break;
            case ID_MENU_BACK_BUTTON:
                mPauseableHandler.sendEmptyMessage(ID_MENU_BACK_BUTTON);
                break;
        }
    }
    
    // ================================================================================
    // INTERFACE - onCheckedChanged
    // ================================================================================
    
    /** {@inheritDoc} */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            mPrinterManager.setDefaultPrinter(mPrinter);
            mDefaultPrinter.setVisibility(View.GONE);
            mDefaultView.setVisibility(View.VISIBLE);
        }
    }
    
    // ================================================================================
    // INTERFACE - OnItemSelectedListener
    // ================================================================================
    
    /** {@inheritDoc} */
    @Override
    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
        PortSetting port = PortSetting.LPR;
        switch (position) {
            case 1:
                port = PortSetting.RAW;
                break;
            default:
                break;
        }
        mPrinter.setPortSetting(port);
        mPrinterManager.updatePortSettings(mPrinter.getId(), port);
    }
    
    /** {@inheritDoc} */
    @Override
    public void onNothingSelected(AdapterView<?> parentView) {
        // Do nothing
    }
}