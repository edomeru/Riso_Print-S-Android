/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintersFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;

import java.util.ArrayList;

import jp.co.riso.android.dialog.DialogUtils;
import jp.co.riso.android.dialog.InfoDialogFragment;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.PrintersCallback;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartdeviceapp.view.printers.PrinterArrayAdapter;
import jp.co.riso.smartdeviceapp.view.printers.PrintersListView;
import jp.co.riso.smartdeviceapp.view.printers.PrintersScreenTabletView;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class PrintersFragment extends BaseFragment implements PrintersCallback, Callback {
    public static final String FRAGMENT_TAG_PRINTER_SEARCH = "fragment_printer_search";
    private static final String KEY_PRINTER_ERR_DIALOG = "printer_err_dialog";
    private static final String KEY_PRINTER_LIST = "printers_list";
    private static final String KEY_PRINTER_LIST_DELETE = "printers_list_delete";
    private static final String KEY_PRINTER_LIST_STATE = "printers_list_state";
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
        if (isTablet()) {
            setTargetFragment(this, 0);
        }
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        Message newMessage = Message.obtain(mHandler, MSG_SET_POPULATE_PRINTERS_LIST);
        if (savedInstanceState != null) {
            mPrinter = savedInstanceState.getParcelableArrayList(KEY_PRINTER_LIST);
            newMessage.obj = savedInstanceState.getParcelable(KEY_PRINTER_LIST_STATE);
            newMessage.arg1 = savedInstanceState.getInt(KEY_PRINTER_LIST_DELETE, -1);
        } else if (isTablet()) {
            newMessage.arg1 = -1;
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
        if (isTablet()) {
            savedInstanceState.putInt(KEY_PRINTER_LIST_DELETE, mPrinterTabletView.getDeleteItemPosition());
        } else {
            if (mListView != null) {
                savedInstanceState.putParcelable(KEY_PRINTER_LIST_STATE, mListView.onSaveInstanceState());
                savedInstanceState.putInt(KEY_PRINTER_LIST_DELETE, ((PrintersListView) mListView).getDeleteItemPosition());
            }
        }
        super.onSaveInstanceState(savedInstanceState);
    }
    
    // ================================================================================
    // Public Methods
    // ================================================================================
    
    public void setPrintSettings(PrintSettings printSettings) {
        if (isTablet()) {
            mPrinterTabletView.updatePrintSettings(printSettings);
        }
    }
    
    // ================================================================================
    // Private Methods
    // ================================================================================
    
    private void displayPrinterSearchFragment() {
        if (isMaxPrinterCountReached()) {
            return;
        }
        PrinterSearchFragment fragment = new PrinterSearchFragment();
        switchToFragment(fragment, FRAGMENT_TAG_PRINTER_SEARCH);
    }
    
    private void displayAddPrinterFragment() {
        if (isMaxPrinterCountReached()) {
            return;
        }
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
            ft.setCustomAnimations(R.animator.left_slide_in, R.animator.left_slide_out, R.animator.right_slide_in, R.animator.right_slide_out);
            ft.addToBackStack(null);
            ft.replace(R.id.mainLayout, fragment, tag);
            ft.commit();
        }
    }
    
    private boolean isMaxPrinterCountReached() {
        if (mPrinterManager.getPrinterCount() == PrinterManager.MAX_PRINTER_COUNT) {
            String title = getResources().getString(R.string.ids_lbl_printer_info);
            String errMsg = null;
            errMsg = getResources().getString(R.string.ids_err_msg_max_printer_count);
            InfoDialogFragment info = InfoDialogFragment.newInstance(title, errMsg, getResources().getString(R.string.ids_lbl_ok));
            DialogUtils.displayDialog(getActivity(), KEY_PRINTER_ERR_DIALOG, info);
            return true;
        }
        return false;
    }
    
    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================
    
    @Override
    public void onClick(View v) {
        super.onClick(v);
        
        switch (v.getId()) {
            case ID_MENU_ACTION_SEARCH_BUTTON:
                if (getActivity() != null && getActivity() instanceof MainActivity) {
                    
                    MainActivity activity = (MainActivity) getActivity();
                    
                    if (!activity.isDrawerOpen(Gravity.RIGHT)) {
                        displayPrinterSearchFragment();
                    } else {
                        activity.closeDrawers();
                    }
                }
                break;
            case ID_MENU_ACTION_ADD_BUTTON:
                if (getActivity() != null && getActivity() instanceof MainActivity) {
                    MainActivity activity = (MainActivity) getActivity();
                    
                    if (!activity.isDrawerOpen(Gravity.RIGHT)) {
                        displayAddPrinterFragment();
                    } else {
                        activity.closeDrawers();
                    }
                }
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
                    mPrinterTabletView.restoreState(mPrinter, msg.arg1);
                } else {
                    mPrinterAdapter = new PrinterArrayAdapter(getActivity(), R.layout.printers_container_item, mPrinter);
                    mListView.setAdapter(mPrinterAdapter);
                    if (msg.obj != null) {
                        ((PrintersListView) mListView).onRestoreInstanceState((Parcelable) msg.obj, msg.arg1);
                    }
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
