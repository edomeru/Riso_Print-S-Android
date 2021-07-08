/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintersScreenTabletView.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.printers;

import java.lang.ref.WeakReference;
import java.util.List;

import jp.co.riso.android.dialog.DialogUtils;
import jp.co.riso.android.dialog.InfoDialogFragment;
import jp.co.riso.android.os.pauseablehandler.PauseableHandler;
import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartprint.R;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.model.Printer.PortSetting;
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartdeviceapp.view.fragment.PrintSettingsFragment;
import jp.co.riso.smartdeviceapp.view.fragment.PrintersFragment;

import androidx.fragment.app.FragmentActivity;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * @class PrintersScreenTabletView
 * 
 * @brief View for Printers Screen of tablet.
 */
public class PrintersScreenTabletView extends ViewGroup implements View.OnClickListener, Callback, OnItemSelectedListener {
    private static final int MSG_ADD_PRINTER = 0x01;
    private static final int MSG_SET_UPDATE_VIEWS = 0x02;
    private static final int MIN_COLUMN = 2;
    private static final int MIN_ROW = 1;
    private static final int ID_TAG_DEFAULTSETTINGS = 0x11000001;
    
    private PrinterManager mPrinterManager = null;
    private Printer mSelectedPrinter = null;
    private List<Printer> mPrinterList = null;
    private ViewHolder mDeleteViewHolder = null;
    private ViewHolder mDefaultViewHolder = null;
    private Handler mHandler = null;
    private int mDeleteItem = -1;
    private int mWidth = 0;
    private int mHeight = 0;
    private int mSettingItem = PrinterManager.EMPTY_ID;
    private WeakReference<PrintersViewCallback> mCallbackRef = null;
    private PauseableHandler mPauseableHandler = null;
    /**
     * @brief Constructor. <br>
     *
     * Instantiate Printers Screen tablet view
     * 
     * @param context Application context
     */
    public PrintersScreenTabletView(Context context) {
        super(context);
        init(context);
    }
    
    /**
     * @brief Constructor. <br>
     *
     * Instantiate Printers Screen tablet view
     * 
     * @param context Application context
     * @param attrs Layout attributes
     */
    public PrintersScreenTabletView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    /**
     * @brief Constructor. <br>
     * 
     * Instantiate Printers Screen tablet view
     * 
     * @param context Application context
     * @param attrs Layout attributes
     * @param defStyle Layout styles
     */
    public PrintersScreenTabletView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }
    
    @Override
    protected LayoutParams generateLayoutParams(LayoutParams layoutParams) {
        return new MarginLayoutParams(layoutParams);
    }
    
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        final int screenWidth = MeasureSpec.getSize(widthMeasureSpec);
        int childCount = getChildCount();
        int childWidth = getContext().getResources().getDimensionPixelSize(R.dimen.printers_view_width);
        int childHeight = getContext().getResources().getDimensionPixelSize(R.dimen.printers_view_height);
        int numberOfColumn = Math.max(screenWidth / childWidth, MIN_COLUMN);
        int numberOfRow = Math.max((childCount + numberOfColumn - 1) / numberOfColumn, MIN_ROW);
        
        if (numberOfColumn == MIN_COLUMN) {
            if (childWidth * MIN_COLUMN > screenWidth) {
                childWidth = screenWidth / numberOfColumn;
            }
        }
        final int newRowMeasureSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY);
        final int newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY);
        
        mWidth = childWidth;
        mHeight = childHeight;
        
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                // Measure the child.
                measureChildWithMargins(child, newRowMeasureSpec, 0, newHeightMeasureSpec, 0);
            }
        }
        
        setMeasuredDimension(screenWidth, childHeight * numberOfRow);
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int childCount = getChildCount();
        
        Point screenSize = AppUtils.getScreenDimensions((Activity) getContext());
        
        int numberOfColumn = Math.max(screenSize.x / mWidth, MIN_COLUMN);
        int margin = (screenSize.x - mWidth * numberOfColumn) / 2;
        for (int i = 0, y = 0; i < childCount; y++) {
            for (int x = 0; x < numberOfColumn; x++) {
                View child = getChildAt(i);
                
                if (child == null) {
                    return;
                }
                MarginLayoutParams lps = (MarginLayoutParams) child.getLayoutParams();
                
                int fLeft = left + (mWidth * x) + lps.leftMargin;
                int fRight = left + (mWidth * (x + 1)) - lps.rightMargin;
                int fTop = top + (mHeight * y) + lps.topMargin;
                int fBot = top + (mHeight * (y + 1)) - lps.bottomMargin;
                child.layout(fLeft + margin, fTop, fRight + margin, fBot);
                i++;
            }
        }
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int coords[] = new int[2];
        if (mSelectedPrinter != null) {
            mSelectedPrinter = null;
        }
        if (mDeleteViewHolder != null) {
            
            if (mDeleteViewHolder.mDeleteButton != null) {
                mDeleteViewHolder.mDeleteButton.getLocationOnScreen(coords);
                
                Rect rect = new Rect(coords[0], coords[1], coords[0] + mDeleteViewHolder.mDeleteButton.getWidth(), coords[1]
                        + mDeleteViewHolder.mDeleteButton.getHeight());
                if (rect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    return super.onInterceptTouchEvent(ev);
                }
            }
            if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
                setPrinterView(mDeleteViewHolder);
            }
            return true;
        }
        return false;
    }
    
    // ================================================================================
    // Public Methods
    // ================================================================================
    
    /**
     * @brief Add printer to the Printers Screen.
     * 
     * @param printer Printer object
     * @param isOnline Printer online status
     */
    public void onAddedNewPrinter(Printer printer, boolean isOnline) {
        Message newMessage = Message.obtain(mHandler, MSG_ADD_PRINTER);
        newMessage.obj = printer;
        if (isOnline) {
            newMessage.arg1 = 1;
        } else {
            newMessage.arg1 = 0;
        }
        mHandler.sendMessage(newMessage);
    }
    
    /**
     * @brief Restore the Printers Screen previous state.
     * 
     * @param printer Printer object
     * @param deleteItem Delete item index
     * @param settingItem Default Print Setting item
     */
    public void restoreState(List<Printer> printer, int deleteItem, int settingItem) {
        mPrinterList = printer;
        mDeleteItem = deleteItem;
        mSettingItem = settingItem;
        for (int i = 0; i < printer.size(); i++) {
            addToTabletPrinterScreen(printer.get(i), false);
        }
        Message newMessage = Message.obtain(mHandler, MSG_SET_UPDATE_VIEWS);
        mHandler.sendMessage(newMessage);
        
    }
    
    /**
     * @brief Get the delete view index.
     * 
     * @return Delete view index
     */
    public int getDeleteItemPosition() {
        if (mDeleteViewHolder != null) {
            mDeleteItem = indexOfChild((View) mDeleteViewHolder.mOnlineIndicator.getTag());
        }
        return mDeleteItem;
    }
    
    
    /**
     * @brief Get the index of the selected printer having an opened Default Print Settings.
     * 
     * @return Index of the selected default print settings
     * @retval EMPTY_ID No selected printers having an opened Default Print Settings
     */
    public int getDefaultSettingSelected() {
        return mSettingItem;
    }
    
    /**
     * @brief Sets the selected state of a Printer. <br>
     * 
     * This selected state is set when Default Print Settings is pressed.
     * 
     * @param printerId Printer ID of the selected printer
     * @param state Selected state of the Printer
     */
    public void setDefaultSettingSelected (int printerId, boolean state) {
        if (printerId != PrinterManager.EMPTY_ID) {
            for (int index = 0; index < mPrinterList.size(); index++) {
                if (mPrinterList.get(index).getId() == printerId) {
                    mSettingItem = index;
                }
            }
        }
        if (mSettingItem != PrinterManager.EMPTY_ID) {
            getChildAt(mSettingItem).findViewById(R.id.default_print_settings).setSelected(state);
        }
        if (!state) {
            mSettingItem = PrinterManager.EMPTY_ID;
        }
    }

    /**
     * @brief Sets the PrintersViewCallback function.
     * 
     * @param callback Callback function
     */
    public void setPrintersViewCallback (PrintersViewCallback callback) {
        mCallbackRef = new WeakReference<>(callback);
    }
    
    /**
     * @brief This function is called when deletion of the printer view is confirmed.
     * 
     * @param relayout Re-layout the entire view
     */
    public void confirmDeletePrinterView(boolean relayout) {
        if (mDeleteViewHolder == null) {
            return;
        }
        
        removeView((View) mDeleteViewHolder.mOnlineIndicator.getTag());
        mDeleteViewHolder = null;
        mDeleteItem = PrinterManager.EMPTY_ID;
        
        if (relayout) {
            removeAllViews();
            restoreState(mPrinterList, PrinterManager.EMPTY_ID, PrinterManager.EMPTY_ID);
        }
    }
    
    /**
     * @brief This function is called when deletion of the printer view is confirmed.
     */
    public void resetDeletePrinterView() {
        if (mDeleteViewHolder != null) {
            mDeleteViewHolder = null;
            mDeleteItem = PrinterManager.EMPTY_ID;
        }
    }
    
    /**
     * @brief Set the pauseable handler object.
     * 
     * @param handler Pauseable handler
     */
    public void setPauseableHandler(PauseableHandler handler) {
        mPauseableHandler = handler;
    }
    
    // ================================================================================
    // Private methods
    // ================================================================================
    
    /**
     * @brief Initialize PrinterScreenTabletView.
     * 
     * @param context Application context
     */
    private void init(Context context) {
        mPrinterManager = PrinterManager.getInstance(SmartDeviceApp.getAppContext());
        mHandler = new Handler(Looper.myLooper(), this);
    }
    
    /**
     * @brief Set view holder to normal.
     * 
     * @param viewHolder View holder to set as normal.
     */
    private void setPrinterViewToNormal(ViewHolder viewHolder) {
        if (viewHolder == null) {
            return;
        }
        PrintersContainerView printerItem = (PrintersContainerView) viewHolder.mDeleteButton.getParent();
        
        if (printerItem.getDefault()) {
            printerItem.setDefault(false);
            
            viewHolder.mDefaultPrinter.setSelection(1, true);
            viewHolder.mDefaultPrinterAdapter.isNoDisabled = false;
        }
        resetDeletePrinterView();
    }
    
    /**
     * @brief Set view holder to default.
     * 
     * @param viewHolder View holder to set as default
     */
    private void setPrinterViewToDefault(ViewHolder viewHolder) {
        if (viewHolder == null) {
            return;
        }
        
        if (mDefaultViewHolder != null) {
            setPrinterViewToNormal(mDefaultViewHolder);
        }
        PrintersContainerView printerItem = ((PrintersContainerView) viewHolder.mPrinterName.getParent());
        
        resetDeletePrinterView();
        
        if (printerItem.getDefault()) {
            return;
        }
        
        if (mDefaultViewHolder != null) {
            setPrinterViewToNormal(mDefaultViewHolder);
            mDefaultViewHolder = null;
        }
        
        printerItem.setDefault(true);
        
        viewHolder.mDefaultPrinter.setSelection(0, true);
        viewHolder.mDefaultPrinterAdapter.isNoDisabled = true;
                
        mDefaultViewHolder = viewHolder;
    }
    
    /**
     * @brief Reset view holder.
     * 
     * @param viewHolder View holder to reset
     */
    private void setPrinterView(ViewHolder viewHolder) {
        Printer printer = (Printer) viewHolder.mIpAddress.getTag();
        
        if (mPrinterManager.getDefaultPrinter() == printer.getId()) {
            setPrinterViewToDefault(viewHolder);
        } else {
            setPrinterViewToNormal(viewHolder);
        }
    }
    
    /**
     * @brief Adds printer object to the Printers Screen.
     * 
     * @param printer Printer object
     * @param isOnline Printer online status
     */
    private void addToTabletPrinterScreen(Printer printer, boolean isOnline) {
        if (printer == null) {
            return;
        }
        
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        View pView = inflater.inflate(R.layout.printers_container_item, this, false);
        // AppUtils.changeChildrenFont((ViewGroup) pView, SmartDeviceApp.getAppFont());
        
        String printerName = printer.getName();
        if(printerName == null || printerName.isEmpty()) {
            printerName = getContext().getResources().getString(R.string.ids_lbl_no_name);
        }
        
        addView(pView);
        
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.mPrinterName = pView.findViewById(R.id.txt_printerName);
        viewHolder.mDeleteButton = pView.findViewById(R.id.btn_delete);
        viewHolder.mOnlineIndicator = pView.findViewById(R.id.img_onOff);
        viewHolder.mIpAddress = pView.findViewById(R.id.inputIpAddress);
        
        viewHolder.mPrintSettings = pView.findViewById(R.id.default_print_settings);
        viewHolder.mPort = pView.findViewById(R.id.input_port);
        viewHolder.mDefaultPrinter = pView.findViewById(R.id.default_printer_spinner);
        
        viewHolder.mDefaultPrinterAdapter = new DefaultPrinterArrayAdapter(getContext(), R.layout.printerinfo_port_item);
        viewHolder.mDefaultPrinterAdapter.add(getContext().getString(R.string.ids_lbl_yes));
        viewHolder.mDefaultPrinterAdapter.add(getContext().getString(R.string.ids_lbl_no));
        viewHolder.mDefaultPrinterAdapter.setDropDownViewResource(R.layout.printerinfo_port_dropdownitem);
        viewHolder.mDefaultPrinter.setAdapter(viewHolder.mDefaultPrinterAdapter);    
        if (mPrinterManager.getDefaultPrinter() == printer.getId())
            viewHolder.mDefaultPrinter.setSelection(0,  true);//yes
        else
            viewHolder.mDefaultPrinter.setSelection(1,  true);//no
                
        ArrayAdapter<String> portAdapter = new ArrayAdapter<>(getContext(), R.layout.printerinfo_port_item);
        // Assumption is that LPR is always available
        portAdapter.add(getContext().getString(R.string.ids_lbl_port_lpr));
        if (printer.getConfig().isRawAvailable()) {
            portAdapter.add(getContext().getString(R.string.ids_lbl_port_raw));
            portAdapter.setDropDownViewResource(R.layout.printerinfo_port_dropdownitem);
        } else {
            viewHolder.mPort.setVisibility(View.GONE);
            // Port setting is always displayed as LPR
            pView.findViewById(R.id.defaultPort).setVisibility(View.VISIBLE);
        }
        viewHolder.mPort.setAdapter(portAdapter);
        viewHolder.mPort.setSelection(printer.getPortSetting().ordinal());     
        
        viewHolder.mPrinterName.setText(printerName);
        viewHolder.mIpAddress.setText(printer.getIpAddress());
        
        viewHolder.mDeleteButton.setOnClickListener(this);
        viewHolder.mPrintSettings.setOnClickListener(this);
        viewHolder.mPrintSettings.findViewById(R.id.print_settings).setClickable(false);
        viewHolder.mPort.setOnItemSelectedListener(this);
        viewHolder.mDefaultPrinter.setOnItemSelectedListener(this);
        
        pView.setTag(viewHolder);
        viewHolder.mPrinterName.setTag(viewHolder);
        viewHolder.mDeleteButton.setTag(viewHolder);
        viewHolder.mIpAddress.setTag(printer);
        viewHolder.mPrintSettings.setTag(printer);
        viewHolder.mPrintSettings.setTag(ID_TAG_DEFAULTSETTINGS, viewHolder);
        viewHolder.mOnlineIndicator.setTag(pView);
        viewHolder.mPort.setTag(printer);
        viewHolder.mDefaultPrinter.setTag(viewHolder);
        
        if (isOnline) {
            viewHolder.mOnlineIndicator.setImageResource(R.drawable.img_btn_printer_status_online);
        }
        setPrinterView(viewHolder);
    }
    
    // ================================================================================
    // INTERFACE - onClick
    // ================================================================================
    
    @Override
    public void onClick(View v) {
        Printer printer;
        
        switch (v.getId()) {
            case R.id.btn_delete:
                mDeleteViewHolder = (ViewHolder) v.getTag();
                if (mCallbackRef != null && mCallbackRef.get() != null) {
                    printer = (Printer) mDeleteViewHolder.mIpAddress.getTag();
                    mCallbackRef.get().onPrinterDeleteClicked(printer);
                }
                break;
            case R.id.default_print_settings:
                mSelectedPrinter = (Printer) v.getTag();
                if (getContext() != null && getContext() instanceof MainActivity) {
                    MainActivity activity = (MainActivity) getContext();
                    
                    if (!activity.isDrawerOpen(Gravity.RIGHT)) {                        
                        // Always make new
                        PrintSettingsFragment fragment = null;
                        
                        if (fragment == null) {
                            
                            fragment = new PrintSettingsFragment();
                            fragment.setPrinterId(mSelectedPrinter.getId());
                            // use new print settings retrieved from the database
                            fragment.setPrintSettings(new PrintSettings(mSelectedPrinter.getId(), mSelectedPrinter.getPrinterType()));
                            
                            if (mPauseableHandler != null) {
                                Message msg = Message.obtain(mPauseableHandler, PrintersFragment.MSG_PRINTSETTINGS_BUTTON);
                                msg.obj = fragment;
                                msg.arg1 = mSelectedPrinter.getId();
                                mPauseableHandler.sendMessage(msg);
                            }
                        }
                    } else {
                        activity.closeDrawers();
                    }
                }
                break;
        }
    }    
    // ================================================================================
    // INTERFACE - Callback
    // ================================================================================
    
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_SET_UPDATE_VIEWS:
                if (mDeleteItem != -1) {
                    View view = getChildAt(mDeleteItem);
                    if (view != null) {
                        mDeleteViewHolder = (ViewHolder) view.findViewById(R.id.txt_printerName).getTag();
                    }
                }
                if (mSettingItem != PrinterManager.EMPTY_ID) {
                    setDefaultSettingSelected(PrinterManager.EMPTY_ID, true);
                }
                
                return true;
            case MSG_ADD_PRINTER:
                // BUG#10003: Check if printer to add already exists OR is added already on printers list view to avoid adding multiple views for the same printer
                if (!mPrinterManager.isExists((Printer) msg.obj) || getChildCount() != mPrinterManager.getPrinterCount()) {
                    addToTabletPrinterScreen((Printer) msg.obj, msg.arg1 > 0);
                }
                return true;
        }
        return false;
    }
    
    // ================================================================================
    // INTERFACE - onItemSelected
    // ================================================================================
    
    @Override
    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
    
        switch(parentView.getId())
        {
            case R.id.input_port:
            {
                Printer printer = (Printer) parentView.getTag();
                PortSetting port = PortSetting.LPR;
                switch (position) {
                    case 1:
                        port = PortSetting.RAW;
                        break;
                    default:
                        break;
                }
                printer.setPortSetting(port);
                mPrinterManager.updatePortSettings(printer.getId(), port);                
            }
            break;
            case R.id.default_printer_spinner:
            {
                switch (position) {
                    case 0://
                        {
                            ViewHolder viewHolder = (ViewHolder) parentView.getTag();
                            Printer printer = (Printer) viewHolder.mIpAddress.getTag();
                                                        
                            if (mPrinterManager.getDefaultPrinter() == printer.getId()) {
                                return;
                            }
                            if (mPrinterManager.setDefaultPrinter(printer)) {
                                setPrinterViewToDefault(viewHolder);
                            } else {
                                InfoDialogFragment info = InfoDialogFragment.newInstance(getContext().getString(R.string.ids_lbl_printers),
                                        getContext().getString(R.string.ids_err_msg_db_failure), getContext().getString(R.string.ids_lbl_ok));
                                DialogUtils.displayDialog((FragmentActivity) getContext(), PrintersFragment.KEY_PRINTER_ERR_DIALOG, info);
                            }
                        }
                        break;
                    default:
                        break;
                }
                
            }
            break;
            default:
                break;
        }
    }
    
    @Override
    public void onNothingSelected(AdapterView<?> parentView) {
        // Do nothing
    }
    
    
    // ================================================================================
    // INTERFACE - PrintersViewCallback
    // ================================================================================
    
    /**
     * @interface PrintersViewCallback
     * 
     * @brief Printers Screen Interface.
     */
    public interface PrintersViewCallback {
        /**
         * @brief Dialog which is displayed to confirm printer delete.
         * 
         * @param printer Printer to be deleted
         */
        public void onPrinterDeleteClicked(Printer printer);
    }
    
    // ================================================================================
    // Internal Classes
    // ================================================================================
    
    /**
     * @class ViewHolder
     * 
     * @brief Printers Screen view holder for tablet.
     */
    public static class ViewHolder {
        private ImageView mOnlineIndicator;
        private TextView mPrinterName;
        private Button mDeleteButton;
        private TextView mIpAddress;
        private Spinner mPort;
        private Spinner mDefaultPrinter;
        private DefaultPrinterArrayAdapter mDefaultPrinterAdapter;
        private LinearLayout mPrintSettings;
    }
}
