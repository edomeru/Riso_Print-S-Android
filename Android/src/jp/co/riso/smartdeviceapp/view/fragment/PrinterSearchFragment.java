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
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.OnPrinterSearchCallback;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartdeviceapp.view.printers.PrinterSearchAdapter;
import jp.co.riso.smartdeviceapp.view.printers.PrinterSearchAdapter.PrinterSearchAdapterInterface;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import eu.erikw.PullToRefreshListView;
import eu.erikw.PullToRefreshListView.OnRefreshListener;

public class PrinterSearchFragment extends BaseFragment implements OnRefreshListener, OnPrinterSearchCallback, PrinterSearchAdapterInterface {
    private static final String KEY_SEARCHED_PRINTER_LIST = "searched_printer_list";
    private static final String KEY_SEARCHED_PRINTER_DIALOG = "searched_printer_dialog";
    private static final int ID_MENU_BACK_BUTTON = 0x11000005;
    
    // ListView parameters
    private PullToRefreshListView mListView = null;
    private ArrayList<Printer> mPrinter = null;
    private PrinterSearchAdapter mPrinterSearchAdapter = null;
    private PrinterManager mPrinterManager = null;
    
    @Override
    public int getViewLayout() {
        return R.layout.fragment_printersearch;
    }
    
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mPrinter = savedInstanceState.getParcelableArrayList(KEY_SEARCHED_PRINTER_LIST);
        } else {
            mPrinter = new ArrayList<Printer>();
        }
        mPrinterSearchAdapter = new PrinterSearchAdapter(getActivity(), R.layout.printersearch_container_item, mPrinter);
        mPrinterSearchAdapter.setSearchAdapterInterface(this);
        mPrinterManager = PrinterManager.sharedManager(SmartDeviceApp.getAppContext());
        mPrinterManager.setOnPrinterSearchCallback(this);
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
        if (isTablet()) {
            view.setPadding((int) (getResources().getDimension(R.dimen.preview_view_margin)), 0, 0, 0);
        }
        
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_search_printers);
        addMenuButton(view, R.id.leftActionLayout, ID_MENU_BACK_BUTTON, R.drawable.selector_actionbar_back, this);
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
        savedInstanceState.putParcelableArrayList(KEY_SEARCHED_PRINTER_LIST, mPrinter);
        super.onSaveInstanceState(savedInstanceState);
    }
    
    // ================================================================================
    // Private Methods
    // ================================================================================
    
    public void updateRefreshBar() {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mPrinterManager.isSearching())
                        mListView.setRefreshing();
                    else
                        mListView.onRefreshComplete();
                } catch (Exception e) {
                    mListView.onRefreshComplete();
                }
            }
        });
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
    // INTERFACE - OnPrinterSearch
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
    // Internal Classes
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
}