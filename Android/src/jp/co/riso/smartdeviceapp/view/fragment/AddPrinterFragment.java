/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * AddPrinterFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;

import jp.co.riso.android.dialog.ConfirmDialogFragment;
import jp.co.riso.android.dialog.ConfirmDialogFragment.ConfirmDialogListener;
import jp.co.riso.android.dialog.DialogUtils;
import jp.co.riso.android.dialog.InfoDialogFragment;
import jp.co.riso.android.os.pauseablehandler.PauseableHandler;
import jp.co.riso.android.os.pauseablehandler.PauseableHandlerCallback;
import jp.co.riso.android.text.IpAddressFilter;
import jp.co.riso.android.util.AppUtils;
import jp.co.riso.android.util.NetUtils;
import jp.co.riso.smartprint.R;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.common.JniUtils;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.PrinterSearchCallback;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Message;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class AddPrinterFragment extends BaseFragment implements PrinterSearchCallback, OnEditorActionListener, ConfirmDialogListener, PauseableHandlerCallback {
    private static final String KEY_ADD_PRINTER_DIALOG = "add_printer_dialog";
    private static final int ID_MENU_SAVE_BUTTON = 0x11000004;
    private static final int ID_MENU_BACK_BUTTON = 0x11000005;
    private static final int ERR_INVALID_IP_ADDRESS = -1;
    private static final int ERR_CAN_NOT_ADD_PRINTER = -2;
    private static final int ERR_PRINTER_ADDED_WARNING = -3;
    private static final int MSG_ERR_DB = 0;
    private static final int MSG_ADD_SUCCESS = 1;
    
    private ViewHolder mAddPrinterView = null;
    private PrinterManager mPrinterManager = null;
    private boolean mAdded = false;
    private PauseableHandler mPauseableHandler = null;
    
    /** {@inheritDoc} */
    @Override
    public int getViewLayout() {
        return R.layout.fragment_addprinter;
    }
    
    /** {@inheritDoc} */
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        setRetainInstance(true);
        
        mAdded = false;
        mPrinterManager = PrinterManager.getInstance(SmartDeviceApp.getAppContext());
        mPrinterManager.setPrinterSearchCallback(this);
        mAddPrinterView = new ViewHolder();
        if (mPauseableHandler == null) {
            mPauseableHandler = new PauseableHandler(this);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        mAddPrinterView.mIpAddress = (EditText) view.findViewById(R.id.inputIpAddress);
        mAddPrinterView.mIpAddressLabel = (TextView) view.findViewById(R.id.ipAddressLabel);
        
        mAddPrinterView.mIpAddress.setBackgroundColor(getResources().getColor(R.color.theme_light_1));
        mAddPrinterView.mIpAddress.setOnEditorActionListener(this);
        mAddPrinterView.mIpAddress.setFilters(new InputFilter[] { new IpAddressFilter() });
        if (mPrinterManager.isSearching()) {
            setViewToDisable(mAddPrinterView);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_add_printer);
        
        if (isTablet()) {
            int leftTextPadding = (int) getResources().getDimension(R.dimen.home_title_padding);            
            textView.setPadding(leftTextPadding, 0, 0, 0);
        } else {
            addMenuButton(view, R.id.leftActionLayout, ID_MENU_BACK_BUTTON, R.drawable.selector_actionbar_back, this);
        }
        addMenuButton(view, R.id.rightActionLayout, ID_MENU_SAVE_BUTTON, R.drawable.selector_addprinter_save, this);
        mAddPrinterView.mSaveButton = view.findViewById(ID_MENU_SAVE_BUTTON);
        mAddPrinterView.mProgressBar = view.findViewById(R.id.actionbar_progressbar);
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
     * Search for printer device
     * 
     * @param ipAddress
     *            Printer IP Address
     */
    private void findPrinter(String ipAddress) {
        mPrinterManager.searchPrinter(ipAddress);
    }
    
    /**
     * Dialog which is displayed during successful printer search
     * 
     * @param printer
     *            Searched printer
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
        String title = getResources().getString(R.string.ids_lbl_add_printer);
        String msg = null;
        
        if (printer.getName().isEmpty()) {
            msg = getResources().getString(R.string.ids_lbl_no_name) + " " + getResources().getString(R.string.ids_info_msg_printer_add_successful);
        } else {
            msg = printer.getName() + " " + getResources().getString(R.string.ids_info_msg_printer_add_successful);
        }     
        ConfirmDialogFragment info = ConfirmDialogFragment.newInstance(title, msg, getResources().getString(R.string.ids_lbl_ok), null);
        info.setTargetFragment(this, 0);
        
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            DialogUtils.displayDialog(getActivity(), KEY_ADD_PRINTER_DIALOG, info);
        }
    }
    
    /**
     * Dialog which is displayed during failed printer search
     * 
     * @param err
     *            Error code
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
        if (err == ERR_INVALID_IP_ADDRESS) {
            errMsg = getResources().getString(R.string.ids_err_msg_invalid_ip_address);
        } else if (err == ERR_CAN_NOT_ADD_PRINTER) {
            errMsg = getResources().getString(R.string.ids_err_msg_cannot_add_printer);
        } else if (err == ERR_PRINTER_ADDED_WARNING) {
            errMsg = getResources().getString(R.string.ids_info_msg_warning_cannot_find_printer);
            errMsg += "\n" + mAddPrinterView.mIpAddress.getText().toString() + " " + getResources().getString(R.string.ids_info_msg_printer_add_successful);
        }
        DialogFragment info = null;
        
        if(err == ERR_PRINTER_ADDED_WARNING) {
            info = ConfirmDialogFragment.newInstance(title, errMsg, getResources().getString(R.string.ids_lbl_ok), null);
            info.setTargetFragment(this, 0);
        } else {
            info = InfoDialogFragment.newInstance(title, errMsg, getResources().getString(R.string.ids_lbl_ok));    
        }
        DialogUtils.displayDialog(getActivity(), KEY_ADD_PRINTER_DIALOG, info);
    }
    
    /**
     * Close Add Printer screen
     */
    private void closeScreen() {
        
        if (isTablet()) {
            if (getActivity() != null && getActivity() instanceof MainActivity) {
                final MainActivity activity = (MainActivity) getActivity();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.closeDrawers();
                    }
                });
            }
        } else {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            
            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();
                ft.commit();
            }
        }
        AppUtils.hideSoftKeyboard(getActivity());
    }
    
    /**
     * Set the Add Printer Screen to disabled mode to prevent changes from user input
     * 
     * @param viewHolder
     *            Add Printer Screen view holder
     */
    private void setViewToDisable(ViewHolder viewHolder) {
        if (viewHolder == null) {
            return;
        }
        
        viewHolder.mSaveButton.setVisibility(View.GONE);
        viewHolder.mProgressBar.setVisibility(View.VISIBLE);
        viewHolder.mIpAddressLabel.setTextColor(getResources().getColor(R.color.theme_light_4));
        viewHolder.mIpAddress.setTextColor(getResources().getColor(R.color.theme_light_4));
        viewHolder.mIpAddress.setFocusable(false);
        
    }
    
    /**
     * Set the Add Printer Screen to normal
     * 
     * @param viewHolder
     *            Add Printer Screen view holder
     */
    private void setViewToNormal(ViewHolder viewHolder) {
        if (viewHolder == null) {
            return;
        }
        
        viewHolder.mSaveButton.setVisibility(View.VISIBLE);
        viewHolder.mProgressBar.setVisibility(View.GONE);
        viewHolder.mIpAddressLabel.setTextColor(getResources().getColor(R.color.theme_dark_1));
        viewHolder.mIpAddress.setTextColor(getResources().getColor(R.color.theme_dark_1));
        viewHolder.mIpAddress.setFocusableInTouchMode(true);
    }
    
    /**
     * Start manual printer search
     * 
     */
    private void startManualSearch() {       
        String ipAddress = mAddPrinterView.mIpAddress.getText().toString();
        
        if (ipAddress.isEmpty()) {
            dialogErrCb(ERR_INVALID_IP_ADDRESS);
            return;
        }
        
        if (NetUtils.isIPv4Address(ipAddress) || NetUtils.isIPv6Address(ipAddress)) {
            ipAddress = NetUtils.trimZeroes(ipAddress);
        } else {
            dialogErrCb(ERR_INVALID_IP_ADDRESS);
            return;
        }        
        if (NetUtils.isIPv6Address(ipAddress)) {
            ipAddress = JniUtils.validateIp(ipAddress, ipAddress.length() > NetUtils.IPV6_MIN_BUFFER_SIZE ? ipAddress.length() : NetUtils.IPV6_MIN_BUFFER_SIZE);
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
    
    /** {@inheritDoc} */
    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case ID_MENU_BACK_BUTTON:
                closeScreen();
                break;
            case ID_MENU_SAVE_BUTTON:
                startManualSearch();
                break;
        }
    }
    
    // ================================================================================
    // INTERFACE - OnPrinterSearch
    // ================================================================================
    
    /** {@inheritDoc} */
    @Override
    public void onPrinterAdd(Printer printer) {
        if (mPrinterManager.isCancelled()) {
            return;
        }
        if (mPrinterManager.isExists(printer)) {
            dialogErrCb(ERR_INVALID_IP_ADDRESS);
        } else if (mPrinterManager.savePrinterToDB(printer, true)) {
            mAdded = true;
            Message newMessage = Message.obtain(mPauseableHandler, MSG_ADD_SUCCESS);
            newMessage.obj = printer;
            mPauseableHandler.sendMessage(newMessage);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void onSearchEnd() {
        if(mPrinterManager.isCancelled()) {
            return;
        }
        String ipAddress = mAddPrinterView.mIpAddress.getText().toString();
        
        final MainActivity activity = (MainActivity) getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setViewToNormal(mAddPrinterView);
            }
        });
        
        if (!mAdded) {
            // Create Printer object
            Printer printer = new Printer("", ipAddress);
            
            if (mPrinterManager.savePrinterToDB(printer, false)) {                                
                Message newWarningMsg = new Message();
                
                newWarningMsg.arg1 = ERR_PRINTER_ADDED_WARNING;
                mPauseableHandler.sendMessage(newWarningMsg);
                mAdded = true;
            }
        }
    }
    
    // ================================================================================
    // INTERFACE - OnEditorActionListener
    // ================================================================================
    
    /** {@inheritDoc} */
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
    // INTENAL Classes
    // ================================================================================
    
    /**
     * Add Printer Screen view holder
     */
    public class ViewHolder {
        private TextView mIpAddressLabel;
        private EditText mIpAddress;
        private View mProgressBar;
        private View mSaveButton;
    }

    // ================================================================================
    // INTERFACE - PauseableHandlerCallback
    // ================================================================================
    
    /** {@inheritDoc} */
    @Override
    public boolean storeMessage(Message message) {
        return message.what == MSG_ERR_DB || message.what == MSG_ADD_SUCCESS;
    }
    
    /** {@inheritDoc} */
    @Override
    public void processMessage(Message msg) {
        switch (msg.what) {
            case MSG_ERR_DB:
                dialogErrCb(msg.arg1);
                break;
            case MSG_ADD_SUCCESS:
                dialogCb((Printer) msg.obj);
                break;
        }
    }
}