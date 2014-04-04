/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintersFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;

import java.util.ArrayList;

import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.PrintersCallback;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartdeviceapp.view.printers.PrinterArrayAdapter;
import jp.co.riso.smartdeviceapp.view.printers.PrintersScreenTabletView;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class PrintersFragment extends BaseFragment implements PrintersCallback, Callback {
    private static final String KEY_PRINTER_LIST = "printers_list";
    public static final String FRAGMENT_TAG_PRINTER_SEARCH = "fragment_printer_search";
    public static final String FRAGMENT_TAG_ADD_PRINTER = "fragment_add_printer";
    private static final int MSG_SET_POPULATE_PRINTERS_LIST = 0x0;
    private static final int MSG_ADD_NEW_PRINTER = 0x1;
    
    public final int ID_MENU_ACTION_SEARCH_BUTTON = 0x11000002;
    public final int ID_MENU_ACTION_ADD_BUTTON = 0x11000003;
    
    private Handler mHandler = null;
    // ListView parameters
    private ListView mListView = null;
    private ArrayList<Printer> mPrinter = null;
    private ArrayAdapter<Printer> mPrinterAdapter = null;
    // Tablet parameters
    private PrintersScreenTabletView mPrinterTabletView = null;
    // Printer Manager
    private PrinterManager mPrinterManager = null;
    
    @Override
    public int getViewLayout() {
        return R.layout.fragment_printers;
    }
    
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        ((MainActivity) getActivity()).mPrintPreviewScreen = false;
        mPrinterManager = PrinterManager.getInstance(SmartDeviceApp.getAppContext());
        mHandler = new Handler(this);
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        
        if (savedInstanceState != null) {
            mPrinter = savedInstanceState.getParcelableArrayList(KEY_PRINTER_LIST);
        }
        if (mPrinter == null) {
            mPrinter = (ArrayList<Printer>) mPrinterManager.getSavedPrintersList();
        }
        
        if (isTablet()) {
            mPrinterTabletView = (PrintersScreenTabletView) view.findViewById(R.id.printerParentView);
        } else {
            mListView = (ListView) view.findViewById(R.id.printer_list);
        }
        mPrinterManager.setPrintersCallback(this);
        Message newMessage = Message.obtain(mHandler, MSG_SET_POPULATE_PRINTERS_LIST);
        mHandler.sendMessage(newMessage);
    }
    
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_printers);
        addMenuButton(view, R.id.rightActionLayout, ID_MENU_ACTION_ADD_BUTTON, R.drawable.selector_actionbar_add_printer, this);
        addMenuButton(view, R.id.rightActionLayout, ID_MENU_ACTION_SEARCH_BUTTON, R.drawable.selector_actionbar_printersearch, this);
        addActionMenuButton(view);
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelableArrayList(KEY_PRINTER_LIST, mPrinter);
        super.onSaveInstanceState(savedInstanceState);
    }
    
    // ================================================================================
    // Private Methods
    // ================================================================================
    
    private void displayPrinterSearchFragment() {
        PrinterSearchFragment fragment = new PrinterSearchFragment();
        switchToFragment(fragment, FRAGMENT_TAG_PRINTER_SEARCH);
    }
    
    private void displayAddPrinterFragment() {
        AddPrinterFragment fragment = new AddPrinterFragment();
        switchToFragment(fragment, FRAGMENT_TAG_ADD_PRINTER);
    }
    
    private void switchToFragment(BaseFragment fragment, String tag) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (isTablet()) {
            if (getActivity() != null && getActivity() instanceof MainActivity) {
                MainActivity activity = (MainActivity) getActivity();
                ft.replace(R.id.rightLayout, fragment, tag);
                ft.commit();
                activity.openDrawer(Gravity.RIGHT);
            }
        } else {
            // TODO Add Animation: Must Slide
            ft.addToBackStack(null);
            ft.replace(R.id.mainLayout, fragment, tag);
            ft.commit();
        }
    }
    
    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================
    
    @Override
    public void onClick(View v) {
        super.onClick(v);
        
        switch (v.getId()) {
            case ID_MENU_ACTION_SEARCH_BUTTON:
                displayPrinterSearchFragment();
                onPause();
                break;
            case ID_MENU_ACTION_ADD_BUTTON:
                displayAddPrinterFragment();
                onPause();
                break;
            default:
                break;
        }
    }
    
    // ================================================================================
    // INTERFACE - OnPrintersListChangeCallback
    // ================================================================================
    
    @Override
    public void onAddedNewPrinter(Printer printer) {
        Message newMessage = Message.obtain(mHandler, MSG_ADD_NEW_PRINTER);
        newMessage.obj = printer;
        mHandler.sendMessage(newMessage);
    }
    
    // ================================================================================
    // INTERFACE - Callback
    // ================================================================================
    
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_SET_POPULATE_PRINTERS_LIST:
                if (isTablet()) {
                    mPrinterTabletView.restoreState(mPrinter);
                    
                } else {
                    mPrinterAdapter = new PrinterArrayAdapter(getActivity(), R.layout.printers_container_item, mPrinter);
                    mListView.setAdapter(mPrinterAdapter);
                }
                return true;
            case MSG_ADD_NEW_PRINTER:
                Printer printer = (Printer) msg.obj;
                if (isTablet()) {
                    mPrinterTabletView.onAddedNewPrinter(printer);
                } else {
                    mPrinterAdapter.add(printer);
                    mPrinterAdapter.notifyDataSetChanged();
                }
        }
        return false;
    }
}
