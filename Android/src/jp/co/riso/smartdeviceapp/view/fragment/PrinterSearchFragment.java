/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrinterSearchFragment.java
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
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.PrinterSearchCallback;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartdeviceapp.view.printers.PrinterSearchAdapter;
import jp.co.riso.smartdeviceapp.view.printers.PrinterSearchAdapter.PrinterSearchAdapterInterface;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import eu.erikw.PullToRefreshListView;
import eu.erikw.PullToRefreshListView.OnRefreshListener;

public class PrinterSearchFragment extends BaseFragment implements OnRefreshListener, PrinterSearchCallback, PrinterSearchAdapterInterface, Callback {
    private static final String KEY_PRINTER_ERR_DIALOG = "printer_err_dialog";
    //private static final String KEY_SEARCHED_PRINTER_LIST = "searched_printer_list";
    private static final String KEY_SEARCHED_PRINTER_DIALOG = "searched_printer_dialog";
    private static final int ID_MENU_BACK_BUTTON = 0x11000005;
    private static final int MSG_UPDATE_REFRESH_BAR = 0x0;
    
    // ListView parameters
    private PullToRefreshListView mListView = null;
    private ArrayList<Printer> mPrinter = null;
    private PrinterSearchAdapter mPrinterSearchAdapter = null;
    private PrinterManager mPrinterManager = null;
    private Handler mHandler = null;
    
    @Override
    public int getViewLayout() {
        return R.layout.fragment_printersearch;
    }
    
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // TODO: change implementation - compile error since Printer is not parcelable
            // mPrinter = savedInstanceState.getParcelableArrayList(KEY_SEARCHED_PRINTER_LIST);
        } else {
            mPrinter = new ArrayList<Printer>();
        }
        mPrinterSearchAdapter = new PrinterSearchAdapter(getActivity(), R.layout.printersearch_container_item, mPrinter);
        mPrinterSearchAdapter.setSearchAdapterInterface(this);
        mPrinterManager = PrinterManager.getInstance(SmartDeviceApp.getAppContext());
        mPrinterManager.setPrinterSearchCallback(this);
        mHandler = new Handler(this);
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        mListView = (PullToRefreshListView) view.findViewById(R.id.printer_list);
        mListView.setBackgroundColor(getResources().getColor(R.color.theme_light_3));
        mListView.setLockScrollWhileRefreshing(false);
        mListView.setAdapter(mPrinterSearchAdapter);
        mListView.setOnRefreshListener(this);
    }
    
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_search_printers);
        if (isTablet()) {
            int leftViewPadding = (int) getResources().getDimension(R.dimen.printers_subview_margin);
            int leftTextPadding = (int) getResources().getDimension(R.dimen.home_title_padding);
            
            view.setPadding(leftViewPadding, 0, 0, 0);
            textView.setPadding(leftTextPadding, 0, 0, 0);
        } else {
            addMenuButton(view, R.id.leftActionLayout, ID_MENU_BACK_BUTTON, R.drawable.selector_actionbar_back, this);
        }
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mPrinterManager.isSearching()) {
            updateRefreshBar();
        }
        
        if (savedInstanceState == null) {
            onRefresh();
            updateRefreshBar();
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // TODO: change implementation - compile error since Printer is not parcelable
        // savedInstanceState.putParcelableArrayList(KEY_SEARCHED_PRINTER_LIST, mPrinter);
        super.onSaveInstanceState(savedInstanceState);
    }
    
    // ================================================================================
    // Private Methods
    // ================================================================================
    
    public void updateRefreshBar() {
        Message newMessage = Message.obtain(mHandler, MSG_UPDATE_REFRESH_BAR);
        
        mHandler.sendMessage(newMessage);
    }
    
    // ================================================================================
    // INTERFACE - onRefresh()
    // ================================================================================
    
    @Override
    public void onRefresh() {
        mPrinter.clear();
        mPrinterManager.startPrinterSearch();
    }
    
    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================
    
    @Override
    public void onClick(View v) {
        // Back Button
        if (v.getId() == ID_MENU_BACK_BUTTON) {
            mPrinterManager.cancelPrinterSearch();
            if (isTablet()) {
                MainActivity activity = (MainActivity) getActivity();
                activity.closeDrawers();
            } else {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack();
                    ft.commit();
                }
            }
        }
    }
    
    // ================================================================================
    // INTERFACE - OnPrinterSearchCallback
    // ================================================================================
    
    @Override
    public void onPrinterAdd(final Printer printer) {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!mPrinterManager.isSearching()) {
                    return;
                }
                mPrinter.add(printer);
                mPrinterSearchAdapter.notifyDataSetChanged();
            }
        });
    }
    
    @Override
    public void onSearchEnd() {
        updateRefreshBar();
    }
    
    // ================================================================================
    // INTERFACE - PrinterSearchAdapterInterface
    // ================================================================================
    
    @Override
    public int onAddPrinter(Printer printer) {
        int ret = 0;
        
        if (printer == null || mPrinterManager.isSearching()) {
            return -1;
        }
        
        String title = getResources().getString(R.string.ids_lbl_search_printers);
        String msg = null;
        if (!mPrinterManager.savePrinterToDB(printer)) {
            ret = -1;
            msg = getResources().getString(R.string.ids_err_msg_cannot_add_printer);
        } else {
            msg = printer.getName() + " " + getResources().getString(R.string.ids_lbl_add_successful);
        }
        
        InfoDialogFragment info = InfoDialogFragment.newInstance(title, msg, getResources().getString(R.string.ids_lbl_ok));
        DialogUtils.displayDialog(getActivity(), KEY_SEARCHED_PRINTER_DIALOG, info);
        return ret;
    }
    
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_UPDATE_REFRESH_BAR:
                if (mPrinterManager.isSearching()) {
                    mListView.setRefreshing();
                } else {
                    mListView.onRefreshComplete();
                }
                return true;
        }
        return false;
    }
    
    @Override
    public boolean isMaxPrinterCountReached() {
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
}
