/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintersScreenTabletView.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.printers;

import java.util.ArrayList;
import java.util.List;

import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.model.Printer;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class PrintersScreenTabletView extends LinearLayout implements OnLongClickListener, View.OnClickListener, OnTouchListener, OnCheckedChangeListener,
        Callback {
    private static final int MSG_ADD_PRINTER = 0x01;
    private static final int MSG_SET_DEFAULT_PRINTER = 0x02;
    
    private PrinterManager mPrinterManager = null;
    private ArrayList<ViewGroup> mPrinterViewArray = null;
    private List<Printer> mPrinterList = null;
    private ViewHolder mDeleteViewHolder = null;
    private ViewHolder mDefaultViewHolder = null;
    private int mOrientation = 0;
    private Handler mHandler = null;
    
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
    
    // ================================================================================
    // Public Methods
    // ================================================================================
    
    public void onAddedNewPrinter(Printer printer) {
        mPrinterList.add(printer);
        Message newMessage = Message.obtain(mHandler, MSG_ADD_PRINTER);
        newMessage.obj = printer;
        mHandler.sendMessage(newMessage);
    }
    
    public void restoreState(List<Printer> printer) {
        mPrinterList = printer;
        for (int i = 0; i < printer.size(); i++) {
            addToTabletPrinterScreen(printer.get(i));
        }
        Message newMessage = Message.obtain(mHandler, MSG_SET_DEFAULT_PRINTER);
        newMessage.obj = mDefaultViewHolder;
        mHandler.sendMessage(newMessage);
    }
    
    public void refreshPrintersList(List<Printer> printer) {
        mPrinterList = printer;
        for (int i = 0; i < mPrinterViewArray.size(); i++) {
            mPrinterViewArray.get(i).removeAllViews();
        }
        for (int i = 0; i < printer.size(); i++) {
            addToTabletPrinterScreen(printer.get(i));
        }
    }
    
    // ================================================================================
    // Private methods
    // ================================================================================
    
    private void init(Context context) {
        ViewGroup viewGroup = (ViewGroup) View.inflate(context, R.layout.printers_container, this);
        
        mPrinterViewArray = new ArrayList<ViewGroup>();
        mOrientation = context.getResources().getConfiguration().orientation;
        mPrinterViewArray.add((LinearLayout) viewGroup.findViewById(R.id.column1));
        mPrinterViewArray.add((LinearLayout) viewGroup.findViewById(R.id.column2));
        mPrinterViewArray.add((LinearLayout) viewGroup.findViewById(R.id.column3));
        mPrinterManager = PrinterManager.getInstance(SmartDeviceApp.getAppContext());
        viewGroup.setOnTouchListener(this);
        // Hide the column that is not needed
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            mPrinterViewArray.get(2).setVisibility(View.GONE);
        }
        
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
            viewHolder.mDeleteButton.setVisibility(View.GONE);
            printerItem.setDelete(false);
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
        if (mDeleteViewHolder != null) {
            setPrinterViewToNormal(mDeleteViewHolder);
            mDeleteViewHolder = null;
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
    
    private int getActionBarHeight() {
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getContext().getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }
    
    private ViewGroup getParentView() {
        switch (mOrientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                if (mPrinterViewArray.get(0).getChildCount() <= mPrinterViewArray.get(1).getChildCount()
                        && mPrinterViewArray.get(0).getChildCount() <= mPrinterViewArray.get(2).getChildCount()) {
                    return mPrinterViewArray.get(0);
                } else if (mPrinterViewArray.get(1).getChildCount() <= mPrinterViewArray.get(2).getChildCount()) {
                    return mPrinterViewArray.get(1);
                    
                } else {
                    return mPrinterViewArray.get(2);
                }
            case Configuration.ORIENTATION_PORTRAIT:
                if (mPrinterViewArray.get(0).getChildCount() <= mPrinterViewArray.get(1).getChildCount()) {
                    return mPrinterViewArray.get(0);
                } else {
                    return mPrinterViewArray.get(1);
                }
        }
        return null;
    }
    
    private void addToTabletPrinterScreen(Printer printer) {
        if (printer == null) {
            return;
        }
        
        Point screenSize = AppUtils.getScreenDimensions((Activity) getContext());
        
        int width = 0;
        int height = 0;
        int left = getContext().getResources().getDimensionPixelSize(R.dimen.printers_view_tablet_padding);
        int top = getContext().getResources().getDimensionPixelSize(R.dimen.printers_view_tablet_padding);
        int right = getContext().getResources().getDimensionPixelSize(R.dimen.printers_view_tablet_padding);
        int bottom = getContext().getResources().getDimensionPixelSize(R.dimen.printers_view_tablet_padding);
        int actionBarHeight = getActionBarHeight();
        
        // Initial values are for landscape screen (2x3)
        int numberOfRow = 2;
        int numberOfColumn = 3;
        
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup parentView = getParentView();
        
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            // Update values for portrait screen (3x2)
            numberOfRow = 3;
            numberOfColumn = 2;
        }
        
        width = (int) (screenSize.x / numberOfColumn);
        height = (int) (0.95 * (screenSize.y - actionBarHeight) / numberOfRow);
        
        if (parentView == null) {
            return;
        }
        View pView = inflater.inflate(R.layout.printers_container_item, parentView, false);
        AppUtils.changeChildrenFont((ViewGroup) pView, SmartDeviceApp.getAppFont());
        
        pView.setPadding(left, top, right, bottom);
        
        parentView.addView(pView, width, height);
        
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.mPrinterName = (TextView) pView.findViewById(R.id.txt_printerName);
        viewHolder.mDeleteButton = (ImageView) pView.findViewById(R.id.btn_delete);
        viewHolder.mOnlineIndcator = (ImageView) pView.findViewById(R.id.img_onOff);
        viewHolder.mIpAddress = (TextView) pView.findViewById(R.id.inputIpAddress);
        viewHolder.mDefaultPrinter = (Switch) pView.findViewById(R.id.default_printer_switch);
        
        viewHolder.mPrinterName.setText(printer.getName());
        viewHolder.mIpAddress.setText(printer.getIpAddress());
        
        viewHolder.mPrinterName.setOnLongClickListener(this);
        viewHolder.mDeleteButton.setOnClickListener(this);
        viewHolder.mDefaultPrinter.setOnCheckedChangeListener(this);
        
        pView.setTag(viewHolder);
        viewHolder.mPrinterName.setTag(viewHolder);
        viewHolder.mDefaultPrinter.setTag(viewHolder);
        viewHolder.mDeleteButton.setTag(viewHolder);
        viewHolder.mIpAddress.setTag(printer);
        viewHolder.mOnlineIndcator.setTag(pView);
        mPrinterManager.updateOnlineStatus(printer.getIpAddress(), viewHolder.mOnlineIndcator);
        setPrinterView(viewHolder);
        
        return;
    }
    
    private void removeFromLayout(ViewHolder viewHolder) {
        int maxColumnIndex = 2;
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            // Update values for portrait screen (3x2)
            maxColumnIndex = 1;
        }
        
        View curPrinterView = (View) viewHolder.mOnlineIndcator.getTag();
        ViewGroup curColumnView = (ViewGroup) curPrinterView.getParent();
        
        int row = curColumnView.indexOfChild(curPrinterView);
        int column = removePrinterItem(viewHolder);
        // Adjust remaining PrinterItems
        while (true) {
            for (int c = column; c <= maxColumnIndex; c++) {
                if (c == -1) {
                } else if (c == maxColumnIndex) {
                    if (!movePrinterItem(row + 1, 0, true)) {
                        return;
                    }
                } else {
                    if (!movePrinterItem(row, c + 1, true)) {
                        return;
                    }
                }
            }
            column = 0;
            row++;
        }
    }
    
    /**
     * Moves a PrinterView.
     * <p>
     * Moves a Printer View
     * 
     * @param viewHolder
     *            The viewHolder to be removed
     * @return The column number/index of the removed viewHolder. (-1) if failed.
     */
    private int removePrinterItem(ViewHolder viewHolder) {
        View printerView = (View) viewHolder.mOnlineIndcator.getTag();
        ViewGroup parentView = (ViewGroup) printerView.getParent();
        
        parentView.removeView(printerView);
        switch (parentView.getId()) {
            case R.id.column1:
                return 0;
            case R.id.column2:
                return 1;
            case R.id.column3:
                return 2;
        }
        return -1;
    }
    
    /**
     * Moves a PrinterView.
     * <p>
     * Moves a PrinterView one block before its original position
     * 
     * @param row
     *            The row of the source view
     * @param column
     *            The column of the source view
     * @return true/false
     */
    private boolean movePrinterItem(int row, int column, boolean animate) {
        int maxColumnIndex = 2;
        boolean isPortrait = mOrientation == Configuration.ORIENTATION_PORTRAIT;
        
        if (isPortrait) {
            // Update values for portrait screen (3x2)
            maxColumnIndex = 1;
        }
        
        // Printer item (PrinterView)
        View printerView = (View) mPrinterViewArray.get(column).getChildAt(row);
        
        if (printerView == null) {
            return false;
        }
        ViewGroup srcColumnView = (ViewGroup) printerView.getParent();
        ViewGroup destColumnView = null;
        
        // Obtain Destination
        if (column == 0) {
            row--;
            destColumnView = mPrinterViewArray.get(maxColumnIndex);
        } else {
            destColumnView = mPrinterViewArray.get(column - 1);
        }
        printerView.clearAnimation();
        srcColumnView.removeView(printerView);
        destColumnView.addView(printerView, row);
        
        if (animate) {
            TranslateAnimation translate = null;
            if (column == 0) {
                translate = new TranslateAnimation(0, printerView.getWidth(), printerView.getHeight(), 0);
            } else {
                translate = new TranslateAnimation(printerView.getWidth(), 0, 0, 0);
            }
            translate.setDuration(250);
            printerView.startAnimation(translate);
        }
        return true;
    }
    
    // ================================================================================
    // INTERFACE - onLongClick
    // ================================================================================
    
    @Override
    public boolean onLongClick(View v) {
        if (mDeleteViewHolder != null) {
            setPrinterView(mDeleteViewHolder);
        }
        mDeleteViewHolder = (ViewHolder) v.getTag();
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
                removeFromLayout(viewHolder);
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
        }
    }
    
    // ================================================================================
    // INTERFACE - onTouch
    // ================================================================================
    
    @Override
    public boolean onTouch(View arg0, MotionEvent arg1) {
        if (mDeleteViewHolder != null) {
            setPrinterView(mDeleteViewHolder);
            mDeleteViewHolder = null;
        }
        return false;
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
            case MSG_SET_DEFAULT_PRINTER:
                setPrinterViewToDefault((ViewHolder) msg.obj);
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
        ImageView mOnlineIndcator;
        TextView mPrinterName;
        ImageView mDeleteButton;
        TextView mIpAddress;
        Switch mDefaultPrinter;
    }
}
