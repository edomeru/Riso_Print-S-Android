/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrinterInfoFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;

import java.util.List;

import jp.co.riso.android.dialog.DialogUtils;
import jp.co.riso.android.dialog.InfoDialogFragment;
import jp.co.riso.android.os.pauseablehandler.PauseableHandler;
import jp.co.riso.android.os.pauseablehandler.PauseableHandlerCallback;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.model.Printer.PortSetting;
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartdeviceapp.view.printers.DefaultPrinterArrayAdapter;
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
import android.widget.Spinner;
import android.widget.TextView;

/**
 * @class PrinterInfo
 * 
 * @brief Fragment for Printer Info Screen
 */
public class PrinterInfoFragment extends BaseFragment implements OnItemSelectedListener, PauseableHandlerCallback {
    private static final String FRAGMENT_TAG_PRINTERS = "fragment_printers";
    private static final String KEY_PRINTER_INFO_ID = "fragment_printer_info_id";
    
    private static final String KEY_PRINTER_INFO_ERR_DIALOG = "printer_info_err_dialog";
    
    private Printer mPrinter = null;
    
    private TextView mPrinterName = null;
    private TextView mIpAddress = null;
    private Spinner mPort = null;
    private Spinner mDefaultPrinter = null;
    
    private PrinterManager mPrinterManager = null;
    private PrintSettingsFragment mPrintSettingsFragment = null;
    private PauseableHandler mPauseableHandler = null;
    
    private DefaultPrinterArrayAdapter mDefaultPrinterAdapter = null;

    @Override
    public int getViewLayout() {
        return R.layout.fragment_printerinfo;
    }
    
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        mPrinterManager = PrinterManager.getInstance(SmartDeviceApp.getAppContext());
        mPauseableHandler = new PauseableHandler(this);
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {        
        mPrinterName = (TextView) view.findViewById(R.id.inputPrinterName);
        mIpAddress = (TextView) view.findViewById(R.id.inputIpAddress);
        mPort = (Spinner) view.findViewById(R.id.inputPort);
        mPort.setOnItemSelectedListener(this);
        mDefaultPrinter = (Spinner) view.findViewById(R.id.defaultPrinter);
        mDefaultPrinter.setOnItemSelectedListener(this);

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

        ArrayAdapter<String> portAdapter = new ArrayAdapter<String>(getActivity(), R.layout.printerinfo_port_item);
        // Assumption is that LPR is always available
        portAdapter.add(getString(R.string.ids_lbl_port_lpr));
        if (mPrinter.getConfig().isRawAvailable()) {
            portAdapter.add(getString(R.string.ids_lbl_port_raw));
            portAdapter.setDropDownViewResource(R.layout.printerinfo_port_dropdownitem);
        } else {
            mPort.setVisibility(View.GONE);
            // Port setting is always displayed as LPR
            view.findViewById(R.id.defaultPort).setVisibility(View.VISIBLE);
        }
        mPort.setAdapter(portAdapter);
        mPort.setSelection(mPrinter.getPortSetting().ordinal());
        
        mDefaultPrinterAdapter = new DefaultPrinterArrayAdapter(getActivity(), R.layout.printerinfo_port_item);
        mDefaultPrinterAdapter.add(getString(R.string.ids_lbl_yes));
        mDefaultPrinterAdapter.add(getString(R.string.ids_lbl_no));
        mDefaultPrinterAdapter.setDropDownViewResource(R.layout.printerinfo_port_dropdownitem);

        mDefaultPrinter.setAdapter(mDefaultPrinterAdapter);
        
        if (mPrinterManager.getDefaultPrinter() == mPrinter.getId()) {
            mDefaultPrinter.setSelection(0);//yes
        }
        else
            mDefaultPrinter.setSelection(1);//no            
    }
    
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_printer_info);

        addMenuButton(view, R.id.rightActionLayout, R.id.menu_id_action_print_settings_button, R.drawable.selector_actionbar_printerinfo, this);
        addMenuButton(view, R.id.leftActionLayout, R.id.menu_id_back_button, R.drawable.selector_actionbar_back, this);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String printerName = mPrinter.getName();
        if(printerName == null || printerName.isEmpty()) {
            printerName = getActivity().getResources().getString(R.string.ids_lbl_no_name);
        }
        mPrinterName.setText(printerName);
        mIpAddress.setText(mPrinter.getIpAddress());
                
        if (mPrinterManager.getDefaultPrinter() == mPrinter.getId()) {
            mDefaultPrinter.setSelection(0);
        }
        else
            mDefaultPrinter.setSelection(1);
        
        mPort.setSelection(mPrinter.getPortSetting().ordinal());
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(KEY_PRINTER_INFO_ID, mPrinter.getId());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void clearIconStates() {
        super.clearIconStates();
        setIconState(R.id.menu_id_action_print_settings_button, false);
    }
  
    @Override
    public void onResume() {
        super.onResume();
        mPauseableHandler.resume();
    }

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
     * @brief Sets the printer object to be displayed by the Printer Info Screen.
     * 
     * @param printer Printer object
     */
    public void setPrinter(Printer printer) {
        mPrinter = printer;
    }

    // ================================================================================
    // INTERFACE - PauseableHandlerCallback
    // ================================================================================
    
    @Override
    public boolean storeMessage(Message msg) {
        return false;
    }

    @Override
    public void processMessage(Message msg) {
        switch (msg.what) {
            case R.id.menu_id_action_print_settings_button:
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
                        mPrintSettingsFragment.setPrintSettings(new PrintSettings(mPrinter.getId(), mPrinter.getPrinterType()));
                        
                        PrintersFragment printersFragment = (PrintersFragment) fm.findFragmentByTag(FRAGMENT_TAG_PRINTERS);
                        mPrintSettingsFragment.setTargetFragment(printersFragment, 0);
                        activity.openDrawer(Gravity.RIGHT, false);
                    } else {
                        activity.closeDrawers();
                    }
                }
                break;
            case R.id.menu_id_back_button:
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
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu_id_action_print_settings_button:
                Message newMessage = Message.obtain(mPauseableHandler, R.id.menu_id_action_print_settings_button);
                newMessage.obj = v;
                mPauseableHandler.sendMessage(newMessage);
                break;
            case R.id.menu_id_back_button:
                mPauseableHandler.sendEmptyMessage(R.id.menu_id_back_button);
                break;
        }
    }

    // ================================================================================
    // INTERFACE - OnItemSelectedListener
    // ================================================================================
    
    @Override
    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
        switch(parentView.getId())
        {
            case R.id.defaultPort:
            {
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
                break;
            }
            case R.id.defaultPrinter:
            {
                switch (position) {
                    case 0://yes
                        {
                            if (mPrinterManager.setDefaultPrinter(mPrinter)) {
                                mDefaultPrinterAdapter.isNoDisabled = true;
                            } else {
                                InfoDialogFragment info = InfoDialogFragment.newInstance(getActivity().getString(R.string.ids_lbl_printer_info),
                                        getActivity().getString(R.string.ids_err_msg_db_failure), getActivity().getString(R.string.ids_lbl_ok));
                                DialogUtils.displayDialog(getActivity(), KEY_PRINTER_INFO_ERR_DIALOG, info);
                            }
                        }
                        break;
                    default:
                        break;
                }
                break;
            }
            default:
                break;
        }
        

    }
    
    @Override
    public void onNothingSelected(AdapterView<?> parentView) {
        // Do nothing
    }
}