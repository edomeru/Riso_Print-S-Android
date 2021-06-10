/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintersFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import jp.co.riso.android.dialog.ConfirmDialogFragment;
import jp.co.riso.android.dialog.ConfirmDialogFragment.ConfirmDialogListener;
import jp.co.riso.android.dialog.DialogUtils;
import jp.co.riso.android.dialog.InfoDialogFragment;
import jp.co.riso.android.os.pauseablehandler.PauseableHandler;
import jp.co.riso.android.os.pauseablehandler.PauseableHandlerCallback;
import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.PrintersCallback;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartdeviceapp.view.printers.PrinterArrayAdapter;
import jp.co.riso.smartdeviceapp.view.printers.PrinterArrayAdapter.PrinterArrayAdapterInterface;
import jp.co.riso.smartdeviceapp.view.printers.PrintersListView;
import jp.co.riso.smartdeviceapp.view.printers.PrintersScreenTabletView;
import jp.co.riso.smartdeviceapp.view.printers.PrintersScreenTabletView.PrintersViewCallback;
import jp.co.riso.smartprint.R;

/**
 * @class PrintersFragment
 * 
 * @brief Fragment for Printers Screen
 */
public class PrintersFragment extends BaseFragment implements PrintersCallback, PauseableHandlerCallback, PrintersViewCallback, ConfirmDialogListener,
        PrinterArrayAdapterInterface {
    public static final String FRAGMENT_TAG_PRINTER_SEARCH = "fragment_printer_search";
    public static final String FRAGMENT_TAG_ADD_PRINTER = "fragment_add_printer";
    public static final String FRAGMENT_TAG_PRINTER_SEARCH_SETTINGS = "fragment_tag_printer_search_settings";
    public final static String FRAGMENT_TAG_PRINTER_INFO = "fragment_printer_info";
    public static final String KEY_PRINTER_ERR_DIALOG = "printer_err_dialog";
    public static final int MSG_ADD_NEW_PRINTER = 0x1;
    public static final int MSG_SUBMENU_BUTTON = 0x2;
    public static final int MSG_PRINTSETTINGS_BUTTON = 0x3;
    
    private static final String KEY_PRINTERS_DIALOG = "printers_dialog";
    private static final int MSG_POPULATE_PRINTERS_LIST = 0x0;
    
    private PauseableHandler mPauseableHandler = null;
    private Printer mDeletePrinter = null;
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
    private Runnable mUpdateOnlineStatus = null;
    private TextView mEmptyPrintersText;
    
    @Override
    public int getViewLayout() {
        return R.layout.fragment_printers;
    }
    
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        setRetainInstance(true);
        
        mPrinterManager = PrinterManager.getInstance(SmartDeviceApp.getAppContext());
        if (mPauseableHandler == null) {
            mPauseableHandler = new PauseableHandler(Looper.myLooper(), this);
        }
        mUpdateOnlineStatus = new Runnable() {
            @Override
            public void run() {
                /* Update online status*/
                updateOnlineStatus();
                /* Run every 5 seconds */
                mPauseableHandler.postDelayed(this, AppConstants.CONST_UPDATE_INTERVAL);
            }
        };
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        
        Message newMessage = Message.obtain(mPauseableHandler, MSG_POPULATE_PRINTERS_LIST);
        if (!isTablet()) {
            newMessage.obj = mScrollState;
        }
        newMessage.arg1 = mDeleteItem;
        newMessage.arg2 = mSettingItem;
        
        if (mPrinter == null) {
            mPrinter = (ArrayList<Printer>) mPrinterManager.getSavedPrintersList();
        }
        
        mEmptyPrintersText = (TextView) view.findViewById(R.id.emptyPrintersText);
        
        if (isTablet()) {
            mPrinterTabletView = (PrintersScreenTabletView) view.findViewById(R.id.printerParentView);
            mPrinterTabletView.setPrintersViewCallback(this);
        } else {
            mListView = (ListView) view.findViewById(R.id.printer_list);
        }
        mPrinterManager.setPrintersCallback(this);

        mPauseableHandler.sendMessage(newMessage);
        
        if (isTablet()) {
            mSettingItem = PrinterManager.EMPTY_ID;
        } else {
            mScrollState = null;
        }
        mDeleteItem = PrinterManager.EMPTY_ID;

        // for chromebook, scrollview must not be focusable when printers list is empty
        // scrollview constructor enables focusable so it can't be disabled in layout.xml
        if (isChromeBook()) {
            View scrollView = view.findViewById(R.id.printersTabletScrollView);
            if (scrollView != null) {
                scrollView.setFocusable(false);
            }
        }
    }
    
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_printers);
        addMenuButton(view, R.id.rightActionLayout, R.id.menu_id_action_add_button, R.drawable.selector_actionbar_add_printer, this);
		if (!isChromeBook()) {
            addMenuButton(view, R.id.rightActionLayout, R.id.menu_id_action_search_button, R.drawable.selector_actionbar_printersearch, this);
		}
        addMenuButton(view, R.id.rightActionLayout, R.id.menu_id_printer_search_settings_button, R.drawable.selector_actionbar_printersearchsettings, this);
        addActionMenuButton(view);
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        
        if (isTablet() && mPrinterTabletView != null) {
            mDeleteItem = mPrinterTabletView.getDeleteItemPosition();
            mSettingItem = mPrinterTabletView.getDefaultSettingSelected();
        } else {
            if (mListView != null) {
                mScrollState = mListView.onSaveInstanceState();
                mDeleteItem = ((PrintersListView) mListView).getDeleteItemPosition();
            } else {
                mDeleteItem = PrinterManager.EMPTY_ID;
            }
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        MainActivity activity = (MainActivity) getActivity();
        if (!activity.isDrawerOpen(Gravity.RIGHT) && mPrinterManager.isSearching()) {
            mPrinterManager.cancelPrinterSearch();
        }
        
        if (mUpdateOnlineStatus != null && mPauseableHandler != null) {
            if (mPrinterManager.getSavedPrintersList().isEmpty()) {
                showEmptyText();
            } else {
                showPrintersView();
            }
            mPauseableHandler.resume();
        }
        
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        if (mPauseableHandler != null) {
            mPauseableHandler.resume();
        }

        if (isTablet() && mPrinterTabletView != null) {
            mPrinterTabletView.requestLayout();
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        if (mUpdateOnlineStatus != null && mPauseableHandler != null) {
            mPauseableHandler.removeCallbacks(mUpdateOnlineStatus);
        }
    }
    
    /**
     * @brief Sets the selected state of a Printer's default setting in tablet view.
     * 
     * @param state Set Printer to selected state
     */
    public void setDefaultSettingSelected(boolean state) {
        if (mPrinterTabletView != null) {
            mPrinterTabletView.setDefaultSettingSelected(PrinterManager.EMPTY_ID, state);
        }
    }
    
    @Override
    public void clearIconStates() {
        super.clearIconStates();
        setDefaultSettingSelected(false);
        setIconState(R.id.menu_id_action_button, false);
        setIconState(R.id.menu_id_action_search_button, false);
        setIconState(R.id.menu_id_printer_search_settings_button, false);
        setIconState(R.id.menu_id_action_add_button, false);
    }
    
    // ================================================================================
    // Private Methods
    // ================================================================================
    
    /**
     * @brief Displays the Printer Search Screen.
     */
    private void displayPrinterSearchFragment() {
        if (isMaxPrinterCountReached()) {
            mPauseableHandler.resume();
            return;
        }
        if (isTablet()) {
            setIconState(R.id.menu_id_action_search_button, true);
        }
        PrinterSearchFragment fragment = new PrinterSearchFragment();
        switchToFragment(fragment, FRAGMENT_TAG_PRINTER_SEARCH);
    }
    
    /**
     * @brief Displays the Add Printer Screen.
     */
    private void displayAddPrinterFragment() {
        if (isMaxPrinterCountReached()) {
            mPauseableHandler.resume();
            return;
        }
        if (isTablet()) {
            setIconState(R.id.menu_id_action_add_button, true);
        }
        AddPrinterFragment fragment = new AddPrinterFragment();
        switchToFragment(fragment, FRAGMENT_TAG_ADD_PRINTER);
    }

    /**
     * @brief Displays the Printer Search Settings Screen.
     */
    private void displaySearchPrinterFragment() {
        if (isTablet()) {
            setIconState(R.id.menu_id_printer_search_settings_button, true);
        }

        PrinterSearchSettingsFragment fragment = new PrinterSearchSettingsFragment();
        switchToFragment(fragment, FRAGMENT_TAG_PRINTER_SEARCH_SETTINGS);
    }

    /**
     * @brief Displays the Printer Info Screen.
     * 
     * @param printer Printer to be displayed in the PrinterInfo Screen
     */
    private void displayPrinterInfoFragment(Printer printer) {
        PrinterInfoFragment fragment = new PrinterInfoFragment();
        fragment.setPrinter(printer);
        switchToFragment(fragment, FRAGMENT_TAG_PRINTER_INFO);
    }
    
    /**
     * @brief Displays the Default Print Settings Screen.
     * 
     * @param fragment Default Print Settings Fragment
     */
    private void displayDefaultPrintSettings(PrintSettingsFragment fragment) {
        switchToFragment(fragment, PrintPreviewFragment.FRAGMENT_TAG_PRINTSETTINGS);
    }

    /**
     * @brief Switch to a fragment.
     * 
     * @param fragment Fragment object
     * @param tag Fragment tag
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
     * @brief Determines if the maximum number of saved printers is reached.
     * 
     * @retval true Maximum printer count is reached
     * @retval false Maximum printer count is not yet reached
     */
    private boolean isMaxPrinterCountReached() {
        if (mPrinterManager.getPrinterCount() == AppConstants.CONST_MAX_PRINTER_COUNT) {
            String title = getResources().getString(R.string.ids_lbl_printers);
            String errMsg = null;
            errMsg = getResources().getString(R.string.ids_err_msg_max_printer_count);
            InfoDialogFragment info = InfoDialogFragment.newInstance(title, errMsg, getResources().getString(R.string.ids_lbl_ok));
            DialogUtils.displayDialog(getActivity(), KEY_PRINTER_ERR_DIALOG, info);
            return true;
        }
        return false;
    }
    
    /**
     * @brief Updates the online status for the whole view.
     */
    private void updateOnlineStatus() {
        int childCount = 0;
        int position = 0;
        if (isTablet() && mPrinterTabletView != null) {
            childCount = mPrinterTabletView.getChildCount();
        } else {
            position = mListView.getFirstVisiblePosition();
            childCount = mListView.getChildCount();
        }
        
        for (int i = 0; i < childCount; i++) {
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
            if (targetView != null &&
                // RM#914 add safety checking for access to mPrinter array list
                mPrinter.size() > (i + position)) {
                mPrinterManager.updateOnlineStatus(mPrinter.get(i + position).getIpAddress(), targetView);
            }
        }
    }
    
    /**
     * @brief Displays empty message, hides printers view and stops updates of online status.
     */
    private void showEmptyText() {
        mPauseableHandler.removeCallbacks(mUpdateOnlineStatus);
        mEmptyPrintersText.setVisibility(View.VISIBLE);
        
        if (isTablet() && mPrinterTabletView != null) {
            mPrinterTabletView.setVisibility(View.GONE);
        } else if(mListView != null){
            mListView.setVisibility(View.GONE);
        }
    }
    
    /**
     * @brief Displays printers view, hides empty message and starts updates of online status.
     */
    private void showPrintersView() {
        mPauseableHandler.post(mUpdateOnlineStatus);
        mEmptyPrintersText.setVisibility(View.GONE);
        
        if (isTablet() && mPrinterTabletView != null) {
            mPrinterTabletView.setVisibility(View.VISIBLE);
        } else if (mListView != null){
            mListView.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * @brief Display dialog during failed database access.
     */
    private void dialogErrCb() {
        String title = getResources().getString(R.string.ids_lbl_printers);
        String errMsg = getResources().getString(R.string.ids_err_msg_db_failure);

        DialogFragment info = InfoDialogFragment.newInstance(title, errMsg, getResources().getString(R.string.ids_lbl_ok));
        DialogUtils.displayDialog(getActivity(), KEY_PRINTER_ERR_DIALOG, info);
    }
    
    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================
    
    @Override
    public void onClick(View v) {        
        switch (v.getId()) {
            case R.id.menu_id_action_search_button:
                if (getActivity() != null && getActivity() instanceof MainActivity) {
                    
                    MainActivity activity = (MainActivity) getActivity();
                    
                    if (!activity.isDrawerOpen(Gravity.RIGHT)) {
                        mPauseableHandler.sendEmptyMessage(R.id.menu_id_action_search_button);
                    } else {
                        activity.closeDrawers();
                    }
                }
                break;
            case R.id.menu_id_action_add_button:
                if (getActivity() != null && getActivity() instanceof MainActivity) {
                    MainActivity activity = (MainActivity) getActivity();
                    
                    if (!activity.isDrawerOpen(Gravity.RIGHT)) {
                        mPauseableHandler.sendEmptyMessage(R.id.menu_id_action_add_button);
                    } else {
                        activity.closeDrawers();
                    }
                }
                break;
            case R.id.menu_id_printer_search_settings_button:
                if (getActivity() != null && getActivity() instanceof MainActivity) {

                    MainActivity activity = (MainActivity) getActivity();

                    if (!activity.isDrawerOpen(Gravity.RIGHT)) {
                        mPauseableHandler.sendEmptyMessage(R.id.menu_id_printer_search_settings_button);
                    } else {
                        activity.closeDrawers();
                    }
                }
                break;
            case R.id.menu_id_action_button:
                if (getActivity() != null && getActivity() instanceof MainActivity) {
                    mPauseableHandler.sendEmptyMessage(R.id.menu_id_action_button);
                }
                break;
            default:
                break;
        }
    }
    
    // ================================================================================
    // INTERFACE - PrintersCallback
    // ================================================================================
    
    @Override
    public void onAddedNewPrinter(Printer printer, boolean isOnline) {
        Message newMessage = Message.obtain(mPauseableHandler, MSG_ADD_NEW_PRINTER);
        newMessage.obj = printer;
        newMessage.arg1 = isOnline ? 1 : 0;
        mPauseableHandler.sendMessage(newMessage);
    }
    
    // ================================================================================
    // INTERFACE - PrintersViewCallback/PrinterArrayAdapterInterface 
    // ================================================================================
    
    @Override
    public void onPrinterDeleteClicked(Printer printer) {
        String title = getResources().getString(R.string.ids_lbl_printer);
        String errMsg = getResources().getString(R.string.ids_info_msg_delete_jobs);

        DialogFragment info = null;

        info = ConfirmDialogFragment.newInstance(title, errMsg, getResources().getString(R.string.ids_lbl_ok),
                getResources().getString(R.string.ids_lbl_cancel));
        info.setTargetFragment(this, 0);
        mDeletePrinter = printer;
        DialogUtils.displayDialog((Activity) getActivity(), KEY_PRINTERS_DIALOG, info);
    }
    
    @Override
    public void onPrinterListClicked(Printer printer) {
        if (mPauseableHandler != null) {
            Message msg = Message.obtain(mPauseableHandler, MSG_SUBMENU_BUTTON);
            msg.obj = printer;
            mPauseableHandler.sendMessage(msg);
        }
    }
    
    // ================================================================================
    // INTERFACE - ConfirmDialogListener
    // ================================================================================
    
    @Override
    public void onConfirm() {
        if (isTablet()) {
            boolean relayout = mDeletePrinter.getId() == mPrinterManager.getDefaultPrinter() ? true : false;
            
            if (mPrinterManager.removePrinter(mDeletePrinter)) {
                mPrinterTabletView.confirmDeletePrinterView(relayout);
            } else {
                dialogErrCb();
            }
        } else {
            if (mPrinterManager.removePrinter(mDeletePrinter)) {
                mPrinterAdapter.notifyDataSetChanged();
            } else {
                dialogErrCb();
            }
            ((PrintersListView) mListView).resetDeleteView(false);
        }
        
        if (mPrinterManager.getSavedPrintersList().isEmpty()) {
            showEmptyText();
        }
    }
    
    @Override
    public void onCancel() {
        if (isTablet() && mPrinterTabletView != null) {
            mPrinterTabletView.resetDeletePrinterView();
        } else {
            mDeletePrinter = null;
            ((PrinterArrayAdapter) mPrinterAdapter).resetDeletePrinterView();
            ((PrintersListView) mListView).resetDeleteView(true);
        }
    }

    // ================================================================================
    // INTERFACE - PauseableHandlerCallback
    // ================================================================================
    
    @Override
    public boolean storeMessage(Message message) {
        return message.what != R.id.menu_id_action_add_button && message.what != R.id.menu_id_action_search_button && message.what != R.id.menu_id_action_button
                && message.what != MSG_SUBMENU_BUTTON && message.what != MSG_PRINTSETTINGS_BUTTON;
    }

    @Override
    public void processMessage(Message msg) {
        switch (msg.what) {
            case MSG_POPULATE_PRINTERS_LIST:
                if (isTablet()) {
                    mPrinterTabletView.restoreState(mPrinter, msg.arg1, msg.arg2);
                    mPrinterTabletView.setPausableHandler(mPauseableHandler);
                } else {
                    mPrinterAdapter = new PrinterArrayAdapter(getActivity(), R.layout.printers_container_item, mPrinter);
                    ((PrinterArrayAdapter) mPrinterAdapter).setPrintersArrayAdapterInterface(this);
                    mListView.setAdapter(mPrinterAdapter);
                    if (msg.obj != null) {
                        ((PrintersListView) mListView).onRestoreInstanceState((Parcelable) msg.obj, msg.arg1);
                    }
                }
                return;
            case MSG_ADD_NEW_PRINTER:
                Printer printer = (Printer) msg.obj;
                if (isTablet()) {
                    mPrinterTabletView.onAddedNewPrinter(printer, msg.arg1 > 0);
                } else {
                    mPrinterAdapter.notifyDataSetChanged();
                }
                return;
            case MSG_SUBMENU_BUTTON:
                mPauseableHandler.pause();
                displayPrinterInfoFragment((Printer) msg.obj);
                break;
            case MSG_PRINTSETTINGS_BUTTON:
                mPauseableHandler.pause();
                PrintSettingsFragment fragment = (PrintSettingsFragment) msg.obj;
                displayDefaultPrintSettings(fragment);
                mPrinterTabletView.setDefaultSettingSelected(msg.arg1, true);
                break;
            case R.id.menu_id_action_search_button:
                mPauseableHandler.pause();
                displayPrinterSearchFragment();
                return;
            case R.id.menu_id_action_add_button:
                mPauseableHandler.pause();
                displayAddPrinterFragment();
                return;
            case R.id.menu_id_printer_search_settings_button:
                mPauseableHandler.pause();
                displaySearchPrinterFragment();
                return;
            case R.id.menu_id_action_button:
                mPauseableHandler.pause();
                MainActivity activity = (MainActivity) getActivity();
                activity.openDrawer(Gravity.LEFT);
                return;
        }
    }
}