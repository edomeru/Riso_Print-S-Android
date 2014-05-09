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
import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.PrintersCallback;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.UpdateStatusCallback;
import jp.co.riso.smartdeviceapp.model.Printer;
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

public class PrintersFragment extends BaseFragment implements PrintersCallback, UpdateStatusCallback, Callback {
    public static final String FRAGMENT_TAG_PRINTER_SEARCH = "fragment_printer_search";
    public static final String FRAGMENT_TAG_ADD_PRINTER = "fragment_add_printer";
    public static final int ID_MENU_ACTION_SEARCH_BUTTON = 0x11000002;
    public static final int ID_MENU_ACTION_ADD_BUTTON = 0x11000003;
    
    private static final String KEY_PRINTER_ERR_DIALOG = "printer_err_dialog";
    private static final int MSG_POPULATE_PRINTERS_LIST = 0x0;
    private static final int MSG_ADD_NEW_PRINTER = 0x1;
    private static final int MSG_INITIALIZE_ONLINE_STATUS = 0x2;
    
    private Handler mHandler = null;
    // ListView parameters
    private ListView mListView = null;
    private ArrayList<Printer> mPrinter = null;
    private ArrayAdapter<Printer> mPrinterAdapter = null;
    // Tablet parameters
    private PrintersScreenTabletView mPrinterTabletView = null;
    // Printer Manager
    private PrinterManager mPrinterManager = null;
    private int mDeleteItem = PrinterManager.EMPTY_ID;
    private Parcelable mScrollState = null;
    private int mSettingItem = PrinterManager.EMPTY_ID;;
    
    
    /** {@inheritDoc} */
    @Override
    public int getViewLayout() {
        return R.layout.fragment_printers;
    }
    
    /** {@inheritDoc} */
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        setRetainInstance(true);
        
        mPrinterManager = PrinterManager.getInstance(SmartDeviceApp.getAppContext());
        mHandler = new Handler(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        
        Message newMessage = Message.obtain(mHandler, MSG_POPULATE_PRINTERS_LIST);
        if (!isTablet()) {
            newMessage.obj = mScrollState;
        }
        newMessage.arg1 = mDeleteItem;
        newMessage.arg2 = mSettingItem;
        
        if (mPrinter == null) {
            mPrinter = (ArrayList<Printer>) mPrinterManager.getSavedPrintersList();
        }
        if (isTablet()) {
            mPrinterTabletView = (PrintersScreenTabletView) view.findViewById(R.id.printerParentView);
        } else {
            mListView = (ListView) view.findViewById(R.id.printer_list);
        }
        mPrinterManager.setPrintersCallback(this);
        mPrinterManager.setUpdateStatusCallback(this);
        mHandler.sendMessage(newMessage);
        mHandler.sendEmptyMessageDelayed(MSG_INITIALIZE_ONLINE_STATUS, 10);
    }
    
    /** {@inheritDoc} */
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_printers);
        addMenuButton(view, R.id.rightActionLayout, ID_MENU_ACTION_ADD_BUTTON, R.drawable.selector_actionbar_add_printer, this);
        addMenuButton(view, R.id.rightActionLayout, ID_MENU_ACTION_SEARCH_BUTTON, R.drawable.selector_actionbar_printersearch, this);
        addActionMenuButton(view);
    }
    
    /** {@inheritDoc} */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        
        if (isTablet()) {
            mDeleteItem = mPrinterTabletView.getDeleteItemPosition();
            mSettingItem  = mPrinterTabletView.getDefaultSettingSelected();
        } else {
            if (mListView != null) {
                mScrollState = mListView.onSaveInstanceState();
                mDeleteItem = ((PrintersListView) mListView).getDeleteItemPosition();
            }
        }
        
    }
    
    /** {@inheritDoc} */
    @Override
    public void onResume() {
        super.onResume();
        MainActivity activity = (MainActivity) getActivity();
        if (!activity.isDrawerOpen(Gravity.RIGHT) && mPrinterManager.isSearching()) {
            mPrinterManager.cancelPrinterSearch();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void onPause() {
        super.onPause();
        mPrinterManager.cancelUpdateStatusThread();
        
        if (isTablet()) {
            mDeleteItem = mPrinterTabletView.getDeleteItemPosition();
        } else {
            if (mListView != null) {
                mDeleteItem = ((PrintersListView) mListView).getDeleteItemPosition();
            }
        }
    }
    
    /**
     * Sets the selected state of a Printer's default setting in tablet view
     * 
     * @param boolean
     *            is in selected state
     */
    public void setDefaultSettingSelected(boolean state) {
        if (mPrinterTabletView != null) {
            mPrinterTabletView.setDefaultSettingSelected(state);
        }
    }
    
    // ================================================================================
    // Private Methods
    // ================================================================================
    
    /**
     * Displays the Printer Search Screen
     */
    private void displayPrinterSearchFragment() {
        if (isMaxPrinterCountReached()) {
            return;
        }
        PrinterSearchFragment fragment = new PrinterSearchFragment();
        switchToFragment(fragment, FRAGMENT_TAG_PRINTER_SEARCH);
    }
    
    /**
     * Displays the Add Printer Screen
     */
    private void displayAddPrinterFragment() {
        if (isMaxPrinterCountReached()) {
            return;
        }
        AddPrinterFragment fragment = new AddPrinterFragment();
        switchToFragment(fragment, FRAGMENT_TAG_ADD_PRINTER);
    }
    
    /**
     * Switch to a fragment
     * 
     * @param fragment
     *            Fragment object
     * @param tag
     *            Fragment tag
     */
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
    
    /**
     * Determines if the maximum number of saved printers is reached
     * 
     * @return true if maximum printer count is reached
     */
    private boolean isMaxPrinterCountReached() {
        if (mPrinterManager.getPrinterCount() == AppConstants.CONST_MAX_PRINTER_COUNT) {
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
    
    /** {@inheritDoc} */
    @Override
    public void onClick(View v) {
        super.onClick(v);
        
        switch (v.getId()) {
            case ID_MENU_ACTION_SEARCH_BUTTON:
                if (getActivity() != null && getActivity() instanceof MainActivity) {
                    
                    MainActivity activity = (MainActivity) getActivity();
                    
                    if (!activity.isDrawerOpen(Gravity.RIGHT)) {
                        //                        v.setSelected(true);
                        if (isTablet()){
                            setIconState(v.getId(), true);
                        }
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
                        //v.setSelected(true);
                        if (isTablet()){
                            setIconState(v.getId(), true);
                        }
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
    // INTERFACE - PrintersCallback
    // ================================================================================
    
    /** {@inheritDoc} */
    @Override
    public void onAddedNewPrinter(Printer printer) {
        Message newMessage = Message.obtain(mHandler, MSG_ADD_NEW_PRINTER);
        newMessage.obj = printer;
        mHandler.sendMessage(newMessage);
    }
    
    // ================================================================================
    // INTERFACE - UpdateStatusCallback
    // ================================================================================
    
    /** {@inheritDoc} */
    @Override
    public void updateOnlineStatus() {
        for (int i = 0; i < mPrinter.size(); i++) {
            View targetView = null;
            if (isTablet()) {
                if (mPrinterTabletView != null && mPrinterTabletView.getChildAt(i) != null) {
                    targetView = mPrinterTabletView.getChildAt(i).findViewById(R.id.img_onOff);
                }
            } else {
                if (mListView != null && mListView.getChildAt(i) != null) {
                    targetView = mListView.getChildAt(i).findViewById(R.id.img_onOff);
                }
            }
            if (targetView != null) {
                mPrinterManager.updateOnlineStatus(mPrinter.get(i).getIpAddress(), targetView);
            }
        }
    }
    
    // ================================================================================
    // INTERFACE - Callback
    // ================================================================================
    
    /** {@inheritDoc} */
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_POPULATE_PRINTERS_LIST:
                if (isTablet()) {
                    mPrinterTabletView.restoreState(mPrinter, msg.arg1, msg.arg2);
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
                    mPrinterAdapter.notifyDataSetChanged();
                }
                return true;
            case MSG_INITIALIZE_ONLINE_STATUS:
                mPrinterManager.createUpdateStatusThread();
                return true;
        }
        return false;
    }
    
}
