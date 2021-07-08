/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * AddPrinterFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import jp.co.riso.android.dialog.ConfirmDialogFragment;
import jp.co.riso.android.dialog.ConfirmDialogFragment.ConfirmDialogListener;
import jp.co.riso.android.dialog.DialogUtils;
import jp.co.riso.android.dialog.InfoDialogFragment;
import jp.co.riso.android.os.pauseablehandler.PauseableHandler;
import jp.co.riso.android.os.pauseablehandler.PauseableHandlerCallback;
import jp.co.riso.android.text.IpAddressFilter;
import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.common.JniUtils;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.PrinterSearchCallback;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartprint.R;

/**
 * @class AddPrinterFragment
 * 
 * @brief Fragment for Add Printer Screen.
 */
public class AddPrinterFragment extends BaseFragment implements PrinterSearchCallback, OnEditorActionListener, ConfirmDialogListener, PauseableHandlerCallback {
    private static final String KEY_ADD_PRINTER_DIALOG = "add_printer_dialog";
    private static final int ERR_INVALID_IP_ADDRESS = -1;
    private static final int ERR_CAN_NOT_ADD_PRINTER = -2;
    private static final int ERR_PRINTER_ADDED_WARNING = -3;
    private static final int ERR_DB_FAILURE = -4;
    private static final int MSG_ERROR = 0;
    private static final int MSG_ADD_SUCCESS = 1;
    private static final String BROADCAST_ADDRESS = "255.255.255.255";
    
    private ViewHolder mAddPrinterView = null;
    private PrinterManager mPrinterManager = null;
    private boolean mAdded = false;
    private PauseableHandler mPauseableHandler = null;
    
    @Override
    public int getViewLayout() {
        return R.layout.fragment_addprinter;
    }
    
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        setRetainInstance(true);
        
        mAdded = false;
        mPrinterManager = PrinterManager.getInstance(SmartDeviceApp.getAppContext());
        mPrinterManager.setPrinterSearchCallback(this);
        mAddPrinterView = new ViewHolder();
        if (mPauseableHandler == null) {
            mPauseableHandler = new PauseableHandler(Looper.myLooper(), this);
        }
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        mAddPrinterView.mIpAddress = view.findViewById(R.id.inputIpAddress);
        mAddPrinterView.mSaveButton = view.findViewById(R.id.img_save_button);
        mAddPrinterView.mProgressBar = view.findViewById(R.id.actionbar_progressbar);

        mAddPrinterView.mIpAddress.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.theme_light_1));
        mAddPrinterView.mIpAddress.setOnEditorActionListener(this);
        mAddPrinterView.mSaveButton.setOnClickListener(this);

        mAddPrinterView.mIpAddress.setFilters(new InputFilter[] { new IpAddressFilter() });
        if (mPrinterManager.isSearching()) {
            setViewToDisable(mAddPrinterView);
        }
        if (!isTablet()) {
            Point screenSize = AppUtils.getScreenDimensions(getActivity());
            View rootView = view.findViewById(R.id.rootView);
            if (rootView == null) {
                return;
            }
            ViewGroup.LayoutParams params = rootView.getLayoutParams();
            params.width = Math.min(screenSize.x, screenSize.y);
        }
    }
    
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        TextView textView = view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_add_printer);
        
        if (isTablet()) {
            int leftTextPadding = (int) getResources().getDimension(R.dimen.home_title_padding);            
            textView.setPadding(leftTextPadding, 0, 0, 0);
        } else {
            addMenuButton(view, R.id.leftActionLayout, R.id.menu_id_back_button, R.drawable.selector_actionbar_back, this);
        }
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
     * @brief Search for printer device
     * 
     * @param ipAddress Printer IP Address
     */
    private void findPrinter(String ipAddress) {
        mPrinterManager.searchPrinter(ipAddress);
    }
    
    /**
     * @brief Display success dialog during successful printer search
     *
     * @param printer Searched printer
     */
    private void dialogCb(Printer printer) {
        if (isTablet() && getActivity() != null && getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            if (!activity.isDrawerOpen(Gravity.RIGHT)) {
                return;
            }
        } else if (isTablet()) {
            return;
        }

        if (isAdded()) {
            String title = getResources().getString(R.string.ids_lbl_add_printer);
            String msg = getResources().getString(R.string.ids_info_msg_printer_add_successful);

            ConfirmDialogFragment info = ConfirmDialogFragment.newInstance(title, msg, getResources().getString(R.string.ids_lbl_ok), null);
            info.setTargetFragment(this, 0);

            if (getActivity() != null && getActivity() instanceof MainActivity) {
                DialogUtils.displayDialog(getActivity(), KEY_ADD_PRINTER_DIALOG, info);
            }
        }
    }
    
    /**
     * @brief Display error dialog during failed printer search
     * 
     * @param err Error code
     */
    private void dialogErrCb(int err) {
        if (isTablet()) {
            if (getActivity() != null && getActivity() instanceof MainActivity) {
                MainActivity activity = (MainActivity) getActivity();
                if (!activity.isDrawerOpen(Gravity.RIGHT)) {
                    return;
                }
            } else if (getActivity() == null) {
                return;
            }
        }
        String title = getResources().getString(R.string.ids_lbl_add_printer);
        String errMsg = null;
        DialogFragment info;
        
        switch (err) {
            case ERR_INVALID_IP_ADDRESS:
                errMsg = getResources().getString(R.string.ids_err_msg_invalid_ip_address);
                break;
            case ERR_CAN_NOT_ADD_PRINTER:
                errMsg = getResources().getString(R.string.ids_err_msg_cannot_add_printer);
                break;
            case ERR_PRINTER_ADDED_WARNING:
                errMsg = getResources().getString(R.string.ids_info_msg_warning_cannot_find_printer);
                break;
            case ERR_DB_FAILURE:
                errMsg = getResources().getString(R.string.ids_err_msg_db_failure);
                break;
        }
        
        if(err == ERR_PRINTER_ADDED_WARNING) {
            info = ConfirmDialogFragment.newInstance(title, errMsg, getResources().getString(R.string.ids_lbl_ok), null);
            info.setTargetFragment(this, 0);
        } else {
            info = InfoDialogFragment.newInstance(title, errMsg, getResources().getString(R.string.ids_lbl_ok));
        }
        DialogUtils.displayDialog(getActivity(), KEY_ADD_PRINTER_DIALOG, info);
    }
    
    /**
     * @brief Close the Add Printer screen
     */
    private void closeScreen() {
        
        if (isTablet()) {
            if (getActivity() != null && getActivity() instanceof MainActivity) {
                final MainActivity activity = (MainActivity) getActivity();
                activity.runOnUiThread((Runnable) () -> activity.closeDrawers());
            }
        } else if (isAdded()) {
            FragmentManager fm = getParentFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();
                ft.commit();
                fm.executePendingTransactions();
            }
        }

        if (getActivity() != null) {
            AppUtils.hideSoftKeyboard(getActivity());
        }
    }
    
    /**
     * @brief Set the Add Printer Screen to disabled mode to prevent changes from user input
     * 
     * @param viewHolder Add Printer Screen view holder
     */
    private void setViewToDisable(ViewHolder viewHolder) {
        if (viewHolder == null) {
            return;
        }
        
        viewHolder.mSaveButton.setVisibility(View.GONE);
        viewHolder.mProgressBar.setVisibility(View.VISIBLE);
        viewHolder.mIpAddress.setTextColor(ContextCompat.getColor(getActivity(), R.color.theme_light_4));
        viewHolder.mIpAddress.setFocusable(false);
        
    }
    
    /**
     * @brief Set the Add Printer Screen to normal
     * 
     * @param viewHolder Add Printer Screen view holder
     */
    private void setViewToNormal(ViewHolder viewHolder) {
        if (viewHolder == null) {
            return;
        }
        
        viewHolder.mSaveButton.setVisibility(View.VISIBLE);
        viewHolder.mProgressBar.setVisibility(View.GONE);
        viewHolder.mIpAddress.setTextColor(ContextCompat.getColor(getActivity(), R.color.theme_dark_1));
        viewHolder.mIpAddress.setFocusableInTouchMode(true);
    }
    
    /**
     * @brief Start manual printer search
     */
    private void startManualSearch() {       
        String ipAddress = mAddPrinterView.mIpAddress.getText().toString();
        
        ipAddress = JniUtils.validateIpAddress(ipAddress);
        if (ipAddress == null || ipAddress.contentEquals(BROADCAST_ADDRESS)) {
            dialogErrCb(ERR_INVALID_IP_ADDRESS);
            return;
        }
        mAddPrinterView.mIpAddress.setText(ipAddress);
        if (mPrinterManager.isExists(ipAddress)) {
            dialogErrCb(ERR_CAN_NOT_ADD_PRINTER);
            return;
        }
        if (!mPrinterManager.isSearching()) {
            setViewToDisable(mAddPrinterView);
            findPrinter(mAddPrinterView.mIpAddress.getText().toString());
        }
        AppUtils.hideSoftKeyboard(getActivity());
    }
    
    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================
    
    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.menu_id_back_button:
                closeScreen();
                break;
            case R.id.img_save_button:
                startManualSearch();
                break;
        }
    }
    
    // ================================================================================
    // INTERFACE - OnPrinterSearch
    // ================================================================================
    
    @Override
    public void onPrinterAdd(Printer printer) {
        if (mPrinterManager.isCancelled()) {
            return;
        }
        Message newMessage;
        if (mPrinterManager.isExists(printer)) {
            newMessage = Message.obtain(mPauseableHandler, MSG_ERROR);
            newMessage.arg1 = ERR_INVALID_IP_ADDRESS;
        } else if (mPrinterManager.savePrinterToDB(printer, true)) {
            mAdded = true;
            newMessage = Message.obtain(mPauseableHandler, MSG_ADD_SUCCESS);
            newMessage.obj = printer;
        } else {
            newMessage = Message.obtain(mPauseableHandler, MSG_ERROR);
            newMessage.arg1 = ERR_DB_FAILURE;
        }
        mPauseableHandler.sendMessage(newMessage);
    }
    
    @Override
    public void onSearchEnd() {
        if(mPrinterManager.isCancelled()) {
            return;
        }

        final MainActivity activity = (MainActivity) getActivity();
        activity.runOnUiThread((Runnable) () -> setViewToNormal(mAddPrinterView));

        if(!mAdded) {
            Message newWarningMsg = Message.obtain(mPauseableHandler, MSG_ERROR);
            newWarningMsg.arg1 = ERR_PRINTER_ADDED_WARNING;
            mPauseableHandler.sendMessage(newWarningMsg);
        }
    }
    
    // ================================================================================
    // INTERFACE - OnEditorActionListener
    // ================================================================================
    
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if ((actionId & EditorInfo.IME_MASK_ACTION) == EditorInfo.IME_ACTION_DONE) {
            startManualSearch();
            return true;
        }
        return false;
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
    // INTERNAL Classes
    // ================================================================================
    
    /**
     * @class ViewHolder
     * 
     * @brief Add Printer Screen view holder
     */
    public static class ViewHolder {
        private EditText mIpAddress;
        private View mProgressBar;
        private View mSaveButton;
    }

    // ================================================================================
    // INTERFACE - PauseableHandlerCallback
    // ================================================================================
    
    @Override
    public boolean storeMessage(Message message) {
        return message.what == MSG_ERROR || message.what == MSG_ADD_SUCCESS;
    }
    
    @Override
    public void processMessage(Message msg) {
        if (msg != null) {
            switch (msg.what) {
                case MSG_ERROR:
                    dialogErrCb(msg.arg1);
                    break;
                case MSG_ADD_SUCCESS:
                    dialogCb((Printer) msg.obj);
                    break;
            }
        }
    }
}