/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintersScreenTabletView.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.printers;

import java.util.List;

import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartdeviceapp.view.fragment.PrintPreviewFragment;
import jp.co.riso.smartdeviceapp.view.fragment.PrintSettingsFragment;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

public class PrintersScreenTabletView extends ViewGroup implements OnLongClickListener, View.OnClickListener, OnCheckedChangeListener, Callback {
    private static final int MSG_ADD_PRINTER = 0x01;
    private static final int MSG_SET_UPDATE_VIEWS = 0x02;
    private static final int MIN_COLUMN = 2;
    private static final int MIN_ROW = 1;
    
    private PrinterManager mPrinterManager = null;
    private Printer mSelectedPrinter = null;
    private List<Printer> mPrinterList = null;
    private ViewHolder mDeleteViewHolder = null;
    private ViewHolder mDefaultViewHolder = null;
    private Handler mHandler = null;
    private int mDeleteItem = -1;
    private int mWidth = 0;
    private int mHeight = 0;
    
    public PrintersScreenTabletView(Context context) {
        super(context);
        init(context);
    }
    
    public PrintersScreenTabletView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
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
        
        int numberOfColumn = Math.max(screenSize.x / mWidth, 2);
        int margin = (screenSize.x - mWidth * numberOfColumn) / 2;
        for (int i = 0, y = 0; i < childCount; y++) {
            for (int x = 0; x < numberOfColumn; x++) {
                View child = getChildAt(i);
                
                if (child == null)
                    return;
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
    
    public void onAddedNewPrinter(Printer printer) {
        mPrinterList.add(printer);
        Message newMessage = Message.obtain(mHandler, MSG_ADD_PRINTER);
        newMessage.obj = printer;
        mHandler.sendMessage(newMessage);
    }
    
    public void restoreState(List<Printer> printer, int deleteItem) {
        mPrinterList = printer;
        mDeleteItem = deleteItem;
        for (int i = 0; i < printer.size(); i++) {
            addToTabletPrinterScreen(printer.get(i));
        }
        Message newMessage = Message.obtain(mHandler, MSG_SET_UPDATE_VIEWS);
        mHandler.sendMessage(newMessage);
        
    }
    
    public int getDeleteItemPosition() {
        if (mDeleteViewHolder != null) {
            mDeleteItem = indexOfChild((View) mDeleteViewHolder.mOnlineIndcator.getTag());
        }
        return mDeleteItem;
    }
    
    public void updatePrintSettings(PrintSettings printSettings) {
        // TODO: Call Update PrintSettings
        // mPrintSettings.update(printSettings);
    }
    
    // ================================================================================
    // Private methods
    // ================================================================================
    
    private void init(Context context) {
        mPrinterManager = PrinterManager.getInstance(SmartDeviceApp.getAppContext());
        mHandler = new Handler(this);
    }
    
    private void setPrinterViewToNormal(ViewHolder viewHolder) {
        if (viewHolder == null) {
            return;
        }
        PrintersContainer printerItem = (PrintersContainer) viewHolder.mDeleteButton.getParent();
        
        if (printerItem.getDefault()) {
            printerItem.setDefault(false);
            viewHolder.mDefaultPrinter.setChecked(false);
        }
        if (printerItem.getDelete()) {
            printerItem.setDelete(false);
            viewHolder.mDeleteButton.setVisibility(View.GONE);
            mDeleteViewHolder = null;
            mDeleteItem = -1;
        }
    }
    
    private void setPrinterViewToDefault(ViewHolder viewHolder) {
        if (viewHolder == null) {
            return;
        }
        
        if (mDefaultViewHolder != null) {
            setPrinterViewToNormal(mDefaultViewHolder);
        }
        PrintersContainer printerItem = ((PrintersContainer) viewHolder.mPrinterName.getParent());
        
        if (printerItem.getDelete()) {
            printerItem.setDelete(false);
            viewHolder.mDeleteButton.setVisibility(View.GONE);
            mDeleteViewHolder = null;
            mDeleteItem = -1;
        }
        if (printerItem.getDefault()) {
            return;
        }
        if (mDefaultViewHolder != null) {
            setPrinterViewToNormal(mDefaultViewHolder);
            mDefaultViewHolder = null;
        }
        printerItem.setDefault(true);
        viewHolder.mDefaultPrinter.setChecked(true);
        mDefaultViewHolder = viewHolder;
    }
    
    private void setPrinterViewToDelete(ViewHolder viewHolder) {
        if (viewHolder == null) {
            return;
        }
        PrintersContainer printerItem = ((PrintersContainer) viewHolder.mPrinterName.getParent());
        
        if (printerItem.getDelete()) {
            return;
        }
        printerItem.setDelete(true);
        viewHolder.mDeleteButton.setVisibility(View.VISIBLE);
        mDeleteViewHolder = viewHolder;
    }
    
    private void setPrinterView(ViewHolder viewHolder) {
        Printer printer = (Printer) viewHolder.mIpAddress.getTag();
        
        if (mPrinterManager.getDefaultPrinter() == printer.getId()) {
            setPrinterViewToDefault(viewHolder);
        } else {
            setPrinterViewToNormal(viewHolder);
        }
    }
    
    private void addToTabletPrinterScreen(Printer printer) {
        if (printer == null) {
            return;
        }
        
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        View pView = inflater.inflate(R.layout.printers_container_item, this, false);
        AppUtils.changeChildrenFont((ViewGroup) pView, SmartDeviceApp.getAppFont());
        
        addView(pView);
        
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.mPrinterName = (TextView) pView.findViewById(R.id.txt_printerName);
        viewHolder.mDeleteButton = (ImageView) pView.findViewById(R.id.btn_delete);
        viewHolder.mOnlineIndcator = (ImageView) pView.findViewById(R.id.img_onOff);
        viewHolder.mIpAddress = (TextView) pView.findViewById(R.id.inputIpAddress);
        viewHolder.mDefaultPrinter = (Switch) pView.findViewById(R.id.default_printer_switch);
        viewHolder.mPrintSettings = (LinearLayout) pView.findViewById(R.id.default_print_settings);
        viewHolder.mPort = (Spinner) pView.findViewById(R.id.input_port);
        
        ArrayAdapter<String> portAdapter = new ArrayAdapter<String>(getContext(), R.layout.printerinfo_port_item);
        portAdapter.add(getContext().getString(R.string.ids_lbl_port_raw));
        portAdapter.add(getContext().getString(R.string.ids_lbl_port_lpr));
        portAdapter.setDropDownViewResource(R.layout.printerinfo_port_dropdownitem);
        viewHolder.mPort.setAdapter(portAdapter);
        
        viewHolder.mPrinterName.setText(printer.getName());
        viewHolder.mIpAddress.setText(printer.getIpAddress());
        
        ((View) viewHolder.mPrinterName.getParent()).setOnLongClickListener(this);
        viewHolder.mDeleteButton.setOnClickListener(this);
        viewHolder.mPrintSettings.setOnClickListener(this);
        viewHolder.mDefaultPrinter.setOnCheckedChangeListener(this);
        viewHolder.mPrintSettings.findViewById(R.id.print_settings).setClickable(false);
        
        pView.setTag(viewHolder);
        viewHolder.mPrinterName.setTag(viewHolder);
        viewHolder.mDefaultPrinter.setTag(viewHolder);
        viewHolder.mDeleteButton.setTag(viewHolder);
        viewHolder.mIpAddress.setTag(printer);
        viewHolder.mPrintSettings.setTag(printer);
        viewHolder.mOnlineIndcator.setTag(pView);
        
        mPrinterManager.updateOnlineStatus(printer.getIpAddress(), viewHolder.mOnlineIndcator);
        setPrinterView(viewHolder);
    }
    
    // ================================================================================
    // INTERFACE - onLongClick
    // ================================================================================
    
    @Override
    public boolean onLongClick(View v) {
        mDeleteViewHolder = (ViewHolder) v.findViewById(R.id.txt_printerName).getTag();
        setPrinterViewToDelete(mDeleteViewHolder);
        return true;
    }
    
    // ================================================================================
    // INTERFACE - onClick
    // ================================================================================
    
    @Override
    public void onClick(View v) {
        ViewHolder viewHolder = null;
        Printer printer = null;
        
        switch (v.getId()) {
            case R.id.btn_delete:
                viewHolder = (ViewHolder) v.getTag();
                printer = (Printer) viewHolder.mIpAddress.getTag();
                mPrinterManager.removePrinter(printer);
                mPrinterList.remove(printer);
                removeView((View) viewHolder.mOnlineIndcator.getTag());
                mDefaultViewHolder = null;
                mDeleteItem = -1;
                break;
            case R.id.default_printer_switch:
                viewHolder = (ViewHolder) v.getTag();
                printer = (Printer) viewHolder.mIpAddress.getTag();
                
                if (viewHolder.mDefaultPrinter.isChecked()) {
                    setPrinterViewToDefault(viewHolder);
                    mPrinterManager.setDefaultPrinter(printer);
                } else {
                    mPrinterManager.clearDefaultPrinter();
                    setPrinterViewToNormal(viewHolder);
                }
                break;
            case R.id.default_print_settings:
                mSelectedPrinter = (Printer) v.getTag();
                if (getContext() != null && getContext() instanceof MainActivity) {
                    MainActivity activity = (MainActivity) getContext();
                    
                    if (!activity.isDrawerOpen(Gravity.RIGHT)) {
                        FragmentManager fm = activity.getFragmentManager();
                        
                        // Always make new
                        PrintSettingsFragment fragment = null;
                        
                        if (fragment == null) {
                            FragmentTransaction ft = fm.beginTransaction();
                            fragment = new PrintSettingsFragment();
                            ft.replace(R.id.rightLayout, fragment, PrintPreviewFragment.FRAGMENT_TAG_PRINTSETTINGS);
                            ft.commit();
                        }
                        
                        fragment.setPrintSettings(mSelectedPrinter.getPrintSettings());
                        activity.openDrawer(Gravity.RIGHT, false);
                    } else {
                        activity.closeDrawers();
                    }
                }
                break;
        }
    }
    
    // ================================================================================
    // INTERFACE - onCheckedChanged
    // ================================================================================
    
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        ViewHolder viewHolder = (ViewHolder) buttonView.getTag();
        Printer printer = (Printer) viewHolder.mIpAddress.getTag();
        if (isChecked) {
            setPrinterViewToDefault(viewHolder);
            mPrinterManager.setDefaultPrinter(printer);
        } else {
            mPrinterManager.clearDefaultPrinter();
            setPrinterViewToNormal(viewHolder);
        }
    }
    
    // ================================================================================
    // INTERFACE - Callback
    // ================================================================================
    
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_SET_UPDATE_VIEWS:
                setPrinterViewToDefault(mDefaultViewHolder);
                if (mDeleteItem != -1) {
                    View view = (View) getChildAt(mDeleteItem);
                    if (view != null) {
                        ViewHolder viewHolder = (ViewHolder) view.findViewById(R.id.txt_printerName).getTag();
                        setPrinterViewToDelete(viewHolder);
                    }
                }
                return true;
            case MSG_ADD_PRINTER:
                addToTabletPrinterScreen((Printer) msg.obj);
                return true;
        }
        return false;
    }
    
    // ================================================================================
    // Internal Classes
    // ================================================================================
    
    public class ViewHolder {
        private ImageView mOnlineIndcator;
        private TextView mPrinterName;
        private ImageView mDeleteButton;
        private TextView mIpAddress;
        private Switch mDefaultPrinter;
        private Spinner mPort;
        private LinearLayout mPrintSettings;
    }
}
