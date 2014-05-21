/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrinterSearchFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;

import java.util.ArrayList;

import jp.co.riso.android.dialog.ConfirmDialogFragment;
import jp.co.riso.android.dialog.ConfirmDialogFragment.ConfirmDialogListener;
import jp.co.riso.android.dialog.DialogUtils;
import jp.co.riso.android.dialog.InfoDialogFragment;
import jp.co.riso.android.os.pauseablehandler.PauseableHandler;
import jp.co.riso.android.os.pauseablehandler.PauseableHandlerCallback;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.PrinterSearchCallback;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartdeviceapp.view.printers.PrinterSearchAdapter;
import jp.co.riso.smartdeviceapp.view.printers.PrinterSearchAdapter.PrinterSearchAdapterInterface;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import eu.erikw.PullToRefreshListView;
import eu.erikw.PullToRefreshListView.OnRefreshListener;

public class PrinterSearchFragment extends BaseFragment implements OnRefreshListener, PrinterSearchCallback, PrinterSearchAdapterInterface,
        ConfirmDialogListener, PauseableHandlerCallback {
    private static final String KEY_PRINTER_ERR_DIALOG = "printer_err_dialog";
    private static final String KEY_SEARCHED_PRINTER_LIST = "searched_printer_list";
    private static final String KEY_SEARCHED_PRINTER_DIALOG = "searched_printer_dialog";
    private static final int ID_MENU_BACK_BUTTON = 0x11000005;
    private static final int MSG_UPDATE_REFRESH_BAR = 0x0;
    
    // ListView parameters
    private PullToRefreshListView mListView = null;
    private ArrayList<Printer> mPrinter = null;
    private PrinterSearchAdapter mPrinterSearchAdapter = null;
    private PrinterManager mPrinterManager = null;
    private PauseableHandler mPauseableHandler = null;
    
    /** {@inheritDoc} */
    @Override
    public int getViewLayout() {
        return R.layout.fragment_printersearch;
    }
    
    /** {@inheritDoc} */
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mPrinter = savedInstanceState.getParcelableArrayList(KEY_SEARCHED_PRINTER_LIST);
        } else {
            mPrinter = new ArrayList<Printer>();
        }
        if (mPauseableHandler == null) {
            mPauseableHandler = new PauseableHandler(this);
        }
        mPrinterSearchAdapter = new PrinterSearchAdapter(getActivity(), R.layout.printersearch_container_item, mPrinter);
        mPrinterSearchAdapter.setSearchAdapterInterface(this);
        mPrinterManager = PrinterManager.getInstance(SmartDeviceApp.getAppContext());
        mPrinterManager.setPrinterSearchCallback(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        mListView = (PullToRefreshListView) view.findViewById(R.id.printer_list);
        mListView.setBackgroundColor(getResources().getColor(R.color.theme_light_3));
        mListView.setAdapter(mPrinterSearchAdapter);
        mListView.setOnRefreshListener(this);
        
        RelativeLayout.LayoutParams progressLayoutParams = (RelativeLayout.LayoutParams) mListView.findViewById(R.id.ptr_id_spinner).getLayoutParams();
        progressLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        
        RelativeLayout.LayoutParams arrowLayoutParams = (RelativeLayout.LayoutParams) mListView.findViewById(R.id.ptr_id_image).getLayoutParams();
        arrowLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
    }
    
    /** {@inheritDoc} */
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
    
    /** {@inheritDoc} */
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
    
    /** {@inheritDoc} */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelableArrayList(KEY_SEARCHED_PRINTER_LIST, mPrinter);
        super.onSaveInstanceState(savedInstanceState);
    }
    
    /** {@inheritDoc} */
    @Override
    public void onPause() {
        super.onPause();
        mPauseableHandler.pause();
    }
    
    /** {@inheritDoc} */
    @Override
    public void onResume() {
        super.onResume();
        mPauseableHandler.resume();
    }
    
    // ================================================================================
    // Private Methods
    // ================================================================================
    
    /**
     * Updates the status of the refresh bar
     */
    private void updateRefreshBar() {
        Message newMessage = Message.obtain(mPauseableHandler, MSG_UPDATE_REFRESH_BAR);
        
        mPauseableHandler.sendMessage(newMessage);
    }
    
    
    /**
     * Determines network connectivity
     * 
     * @return true if connected to network
     */
    private boolean isConnectedToNetwork() {
        Context context = getActivity();
        if (context == null) {
            return false;
        }        
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
    
    /**
     * Dialog which is displayed during error
     */
    private void dialogErrCb() {
        String title = getResources().getString(R.string.ids_lbl_search_printers);
        String errMsg = null;
        errMsg = getResources().getString(R.string.ids_err_msg_network_error);
        DialogFragment info = InfoDialogFragment.newInstance(title, errMsg, getResources().getString(R.string.ids_lbl_ok));

        DialogUtils.displayDialog(getActivity(), KEY_PRINTER_ERR_DIALOG, info);
    }
    
    /**
     * Closes the PrinterSearch Screen
     */
    private void closeScreen() {
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
    
    // ================================================================================
    // INTERFACE - onRefresh()
    // ================================================================================
    
    /** {@inheritDoc} */
    @Override
    public void onRefresh() {
        mPrinter.clear();
        if (!isConnectedToNetwork()) {
            dialogErrCb();
            updateRefreshBar();
            return;
        }
        mPrinterManager.startPrinterSearch();
    }
    
    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================
    
    /** {@inheritDoc} */
    @Override
    public void onClick(View v) {
        // Back Button
        if (v.getId() == ID_MENU_BACK_BUTTON) {
            mPrinterManager.cancelPrinterSearch();
            closeScreen();
        }
    }
    
    // ================================================================================
    // INTERFACE - OnPrinterSearchCallback
    // ================================================================================
    
    /** {@inheritDoc} */
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
                if(mPrinter.contains(printer)) {
                    return;
                }
                mPrinter.add(printer);
                mPrinterSearchAdapter.notifyDataSetChanged();
            }
        });
    }
    
    /** {@inheritDoc} */
    @Override
    public void onSearchEnd() {
        updateRefreshBar();
    }
    
    // ================================================================================
    // INTERFACE - PrinterSearchAdapterInterface
    // ================================================================================
    
    /** {@inheritDoc} */
    @Override
    public int onAddPrinter(Printer printer) {
        int ret = 0;
        
        if (printer == null) {
            return -1;
        }
        
        String title = getResources().getString(R.string.ids_lbl_search_printers);
        String msg = null;
        if (!mPrinterManager.savePrinterToDB(printer)) {
            ret = -1;
            msg = getResources().getString(R.string.ids_err_msg_cannot_add_printer);
        } else {
            msg = printer.getName() + " " + getResources().getString(R.string.ids_info_msg_printer_add_successful);
        }
        
        ConfirmDialogFragment info = ConfirmDialogFragment.newInstance(title, msg, getResources().getString(R.string.ids_lbl_ok), null);
        info.setTargetFragment(this, 0);
        DialogUtils.displayDialog(getActivity(), KEY_SEARCHED_PRINTER_DIALOG, info);
        return ret;
    }

    // ================================================================================
    // INTERFACE - ConfirmDialogListener
    // ================================================================================
    
    /** {@inheritDoc} */
    @Override
    public void onConfirm() {
        closeScreen();
    }
    
    /** {@inheritDoc} */
    @Override
    public void onCancel() {
        closeScreen();
    }

    // ================================================================================
    // INTERFACE - PauseableHandlerCallback
    // ================================================================================
    
    /** {@inheritDoc} */
    @Override
    public boolean storeMessage(Message message) {
        return message.what == MSG_UPDATE_REFRESH_BAR;
    }
    
    /** {@inheritDoc} */
    @Override
    public void processMessage(Message msg) {
        switch (msg.what) {
            case MSG_UPDATE_REFRESH_BAR:
                if (mPrinterManager.isSearching()) {
                    mListView.setRefreshing();
                } else {
                    mListView.onRefreshComplete();
                }
        }
    }
}