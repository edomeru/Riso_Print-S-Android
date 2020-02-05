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
import jp.co.riso.android.util.NetUtils;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.PrinterSearchCallback;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartdeviceapp.view.printers.PrinterSearchAdapter;
import jp.co.riso.smartdeviceapp.view.printers.PrinterSearchAdapter.PrinterSearchAdapterInterface;
import jp.co.riso.smartprint.R;
import android.animation.ValueAnimator;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Message;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import eu.erikw.PullToRefreshListView;
import eu.erikw.PullToRefreshListView.OnRefreshListener;

/**
 * @class PrinterSearchFragment
 * 
 * @brief Fragment for Printer Search Screen
 */
public class PrinterSearchFragment extends BaseFragment implements OnRefreshListener, PrinterSearchCallback, PrinterSearchAdapterInterface,
        ConfirmDialogListener, PauseableHandlerCallback {
    private static final String KEY_PRINTER_ERR_DIALOG = "printer_err_dialog";
    private static final String KEY_SEARCHED_PRINTER_LIST = "searched_printer_list";
    private static final String KEY_SEARCHED_PRINTER_DIALOG = "searched_printer_dialog";
    private static final int MSG_UPDATE_REFRESH_BAR = 0x0;
    
    // ListView parameters
    private PullToRefreshListView mListView = null;
    private ArrayList<Printer> mPrinter = null;
    private PrinterSearchAdapter mPrinterSearchAdapter = null;
    private PrinterManager mPrinterManager = null;
    private PauseableHandler mPauseableHandler = null;
    private TextView mEmptySearchText;
    private boolean mNoNetwork;
    
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
        if (mPauseableHandler == null) {
            mPauseableHandler = new PauseableHandler(this);
        }
        mPrinterSearchAdapter = new PrinterSearchAdapter(getActivity(), R.layout.printersearch_container_item, mPrinter);
        mPrinterSearchAdapter.setSearchAdapterInterface(this);
        mPrinterManager = PrinterManager.getInstance(SmartDeviceApp.getAppContext());
        mPrinterManager.setPrinterSearchCallback(this);
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        mListView = (PullToRefreshListView) view.findViewById(R.id.printer_list);
        mListView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.theme_light_3));
        mListView.setAdapter(mPrinterSearchAdapter);
        mListView.setOnRefreshListener(this);
        
        mEmptySearchText = (TextView) view.findViewById(R.id.emptySearchText);
        
        RelativeLayout.LayoutParams progressLayoutParams = (RelativeLayout.LayoutParams) mListView.findViewById(R.id.ptr_id_spinner).getLayoutParams();
        progressLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        
        RelativeLayout.LayoutParams arrowLayoutParams = (RelativeLayout.LayoutParams) mListView.findViewById(R.id.ptr_id_image).getLayoutParams();
        arrowLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
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
            addMenuButton(view, R.id.leftActionLayout, R.id.menu_id_back_button, R.drawable.selector_actionbar_back, this);
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
        savedInstanceState.putParcelableArrayList(KEY_SEARCHED_PRINTER_LIST, mPrinter);
        super.onSaveInstanceState(savedInstanceState);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        mPauseableHandler.pause();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        mPauseableHandler.resume();
    }
    
    // ================================================================================
    // Private Methods
    // ================================================================================
    
    /**
     * @brief Updates the status of the refresh bar.
     */
    private void updateRefreshBar() {
        Message newMessage = Message.obtain(mPauseableHandler, MSG_UPDATE_REFRESH_BAR);
        
        mPauseableHandler.sendMessage(newMessage);
    }
    
    /**
     * @brief Display error dialog during failed printer search
     */
    private void dialogErrCb() {
        int title = R.string.ids_lbl_search_printers;
        int errMsg = R.string.ids_err_msg_network_error;
        DialogFragment info = InfoDialogFragment.newInstance(title, errMsg, R.string.ids_lbl_ok);

        DialogUtils.displayDialog(getActivity(), KEY_PRINTER_ERR_DIALOG, info);
    }
    
    /**
     * @brief Closes the PrinterSearch Screen.
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
    
    @Override
    public void onRefresh() {
        mPrinter.clear();
        mEmptySearchText.setVisibility(View.GONE);
        mNoNetwork = false;
        if (!NetUtils.isWifiAvailable(SmartDeviceApp.getAppContext())) {
            mNoNetwork = true;
            dialogErrCb();
            updateRefreshBar();
            return;
        }

        mPrinterManager.startPrinterSearch();
    }
    
    @Override
    public void onHeaderAdjusted(int margin) {
        if (mEmptySearchText != null) {
            final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mEmptySearchText.getLayoutParams();
            params.topMargin = margin;
            mEmptySearchText.setLayoutParams(params);
        }
    }
    
    @Override
    public void onBounceBackHeader(int duration) {
        // http://stackoverflow.com/questions/13881419/android-change-left-margin-using-animation
        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mEmptySearchText.getLayoutParams();
        ValueAnimator animation = ValueAnimator.ofInt(params.topMargin, 0);
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.topMargin = (Integer) valueAnimator.getAnimatedValue();
                mEmptySearchText.requestLayout();
            }
        });
        animation.setDuration(duration);
        animation.start();
    }

    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================
    
    @Override
    public void onClick(View v) {
        // Back Button
        if (v.getId() == R.id.menu_id_back_button) {
            mPrinterManager.cancelPrinterSearch();
            closeScreen();
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
                if(mPrinter.contains(printer)) {
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
        
        if (printer == null) {
            return -1;
        }
        DialogFragment info = null;

        int title = R.string.ids_lbl_search_printers;
        int msg = 0;
        if (!mPrinterManager.savePrinterToDB(printer, true)) {
            ret = -1;
            msg = R.string.ids_err_msg_db_failure;
            info = InfoDialogFragment.newInstance(title, msg, R.string.ids_lbl_ok);
        } else {
            msg = R.string.ids_info_msg_printer_add_successful;
            info = ConfirmDialogFragment.newInstance(title, msg, R.string.ids_lbl_ok, 0);
            info.setTargetFragment(this, 0);
        }
        
        DialogUtils.displayDialog(getActivity(), KEY_SEARCHED_PRINTER_DIALOG, info);
        return ret;
    }

    // ================================================================================
    // INTERFACE - ConfirmDialogListener
    // ================================================================================
    
    @Override
    public void onConfirm() {
        closeScreen();
    }
    
    @Override
    public void onCancel() {
        closeScreen();
    }

    // ================================================================================
    // INTERFACE - PauseableHandlerCallback
    // ================================================================================
    
    @Override
    public boolean storeMessage(Message message) {
        return message.what == MSG_UPDATE_REFRESH_BAR;
    }
    
    @Override
    public void processMessage(Message msg) {
        switch (msg.what) {
            case MSG_UPDATE_REFRESH_BAR:
                if (mPrinterManager.isSearching()) {
                    mListView.setRefreshing();
                } else {
                    mListView.onRefreshComplete();
                    if (mPrinter.isEmpty() && !mNoNetwork) {
                        mEmptySearchText.setVisibility(View.VISIBLE);
                    }
                }
        }
    }
}
