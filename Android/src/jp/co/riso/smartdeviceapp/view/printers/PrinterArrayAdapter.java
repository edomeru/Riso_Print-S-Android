/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrinterArrayAdapter.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.printers;

import java.lang.ref.WeakReference;
import java.util.List;

import jp.co.riso.android.os.pauseablehandler.PauseableHandler;
import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.fragment.PrinterInfoFragment;
import jp.co.riso.smartdeviceapp.view.fragment.PrintersFragment;
import jp.co.riso.smartdeviceapp.view.printers.PrintersScreenTabletView.PrintersViewCallback;
import android.content.Context;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class PrinterArrayAdapter extends ArrayAdapter<Printer> implements View.OnClickListener, Callback {
    private static final int MSG_REMOVE_PRINTER = 0x01;
    
    private WeakReference<PrintersViewCallback> mCallbackRef = null;
    private PrinterManager mPrinterManager = null;
    private ViewHolder mDeleteViewHolder = null;
    private ViewHolder mDefaultViewHolder = null;
    private int mLayoutId = 0;
    private Handler mHandler = null;
    private PauseableHandler mPauseableHandler = null;
    
    /**
     * Constructor
     * 
     * @param context
     * @param resource
     * @param values
     */
    public PrinterArrayAdapter(Context context, int resource, List<Printer> values) {
        super(context, resource, values);
        this.mLayoutId = resource;
        mPrinterManager = PrinterManager.getInstance(SmartDeviceApp.getAppContext());
        mDeleteViewHolder = null;
        mDefaultViewHolder = null;
        mHandler = new Handler(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewHolder viewHolder = null;
        Printer printer = getItem(position);
        View separator = null;
        String printerName = printer.getName();
        
        if (convertView == null) {
            convertView = inflater.inflate(mLayoutId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mPrinterName = (TextView) convertView.findViewById(R.id.txt_printerName);
            viewHolder.mIpAddress = (TextView) convertView.findViewById(R.id.txt_ipAddress);
            viewHolder.mPrinterName.setText(printer.getName());
            viewHolder.mIpAddress.setText(printer.getIpAddress());
            
            viewHolder.mDiscloseImage = (ImageView) convertView.findViewById(R.id.img_disclosure);
            viewHolder.mDeleteButton = (Button) convertView.findViewById(R.id.btn_delete);
            
            viewHolder.mDiscloseImage.setTag(printer);
            viewHolder.mPrinterName.setTag(printer);
            viewHolder.mDeleteButton.setTag(convertView);
            
            convertView.setOnClickListener(this);
            viewHolder.mDeleteButton.setOnClickListener(this);
            
            convertView.setTag(viewHolder);
            
            AppUtils.changeChildrenFont((ViewGroup) convertView, SmartDeviceApp.getAppFont());
            
        } else {
            PrintersContainerView printerItem = (PrintersContainerView) convertView;
            
            viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.mPrinterName.setText(printer.getName());
            viewHolder.mIpAddress.setText(printer.getIpAddress());

            if (printerItem.getDefault()) {
                printerItem.setDefault(false);
            }
            if (printerItem.getDelete()) {
                viewHolder.mDeleteButton.setVisibility(View.GONE);
                printerItem.setDelete(false);
            }
            viewHolder.mDiscloseImage.setTag(printer);
            viewHolder.mPrinterName.setTag(printer);
            viewHolder.mDeleteButton.setTag(convertView);
            convertView.setActivated(false);
        }
        
        viewHolder.mDiscloseImage.setAnimation(null);
        viewHolder.mDeleteButton.setAnimation(null);
        viewHolder.mDiscloseImage.setVisibility(View.VISIBLE);
        viewHolder.mDeleteButton.setVisibility(View.GONE);

        if (printerName == null || printerName.isEmpty()) {
            viewHolder.mPrinterName.setText(getContext().getResources().getString(R.string.ids_lbl_no_name));
        }
        separator = convertView.findViewById(R.id.printers_separator);
        if (position == getCount() - 1) {
            separator.setVisibility(View.GONE);
        } else {
            separator.setVisibility(View.VISIBLE);
        }
        setPrinterRow(viewHolder);
        
        return convertView;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isEnabled(int position) {
        return false;
    }
    
    /**
     * Sets the PrintersViewCallback function
     * 
     * @param callback
     *            Callback function
     */
    public void setPrintersViewCallback (PrintersViewCallback callback) {
        mCallbackRef = new WeakReference<PrintersViewCallback>(callback);
    }
    
    /**
     * This function is called when deletion of the printer view is confirmed
     */
    public void confirmDeletePrinterView() {
        if (mDeleteViewHolder == null) {
            return;
        }
        Printer printer = (Printer) mDeleteViewHolder.mDiscloseImage.getTag();        
        Message newMessage = Message.obtain(mHandler, MSG_REMOVE_PRINTER);
        newMessage.obj = printer;
        mHandler.sendMessage(newMessage);
        mDeleteViewHolder = null;     
    }
    
    /**
     * This function is called to reset the delete view
     */
    public void resetDeletePrinterView() {
        if (mDeleteViewHolder != null) {
            mDeleteViewHolder = null;
        }
    }
    
    /**
     * Set the pausable handler object
     * 
     * @param handler
     */
    public void setPausableHandler(PauseableHandler handler) {
        mPauseableHandler = handler;
    }
    
    // ================================================================================
    // Public Methods
    // ================================================================================
    
    /**
     * Clears the delete view
     */
    public void hideDeleteButton() {
        if (mDeleteViewHolder != null) {
            setPrinterRow(mDeleteViewHolder);
        }
    }
    
    /**
     * Set the view to delete
     * 
     * @param convertView
     *            view to set as delete view
     */
    public void setPrinterRowToDelete(View convertView) {
        if (convertView == null) {
            return;
        }
        PrintersContainerView printerItem = (PrintersContainerView) convertView;
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        
        if (printerItem.getDelete()) {
            return;
        }
        if (mDeleteViewHolder != null) {
            setPrinterRowToNormal(mDeleteViewHolder);
        }
        printerItem.setDelete(true);
        mDeleteViewHolder = viewHolder;
    }
    
    /**
     * Reset the view
     * 
     * @param convertView
     *            view to reset
     */
    public void setPrinterRow(View convertView) {
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        Printer printer = (Printer) viewHolder.mPrinterName.getTag();
        
        if (printer.getId() == mPrinterManager.getDefaultPrinter()) {
            setPrinterRowToDefault(viewHolder);
        } else {
            setPrinterRowToNormal(viewHolder);
        }
    }
    
    // ================================================================================
    // Private Methods
    // ================================================================================
    
    /**
     * Set the view holder to normal
     * 
     * @param viewHolder
     *            view holder to set as normal view
     */
    private void setPrinterRowToNormal(ViewHolder viewHolder) {
        if (viewHolder == null) {
            return;
        }
        PrintersContainerView printerItem = (PrintersContainerView) viewHolder.mDeleteButton.getTag();
        
        if (printerItem.getDefault()) {
            printerItem.setDefault(false);
            mDefaultViewHolder = null;
        }
        if (printerItem.getDelete()) {
            printerItem.setDelete(false);
            mDeleteViewHolder = null;
        }
    }
    
    /**
     * Set the view holder to default
     * 
     * @param viewHolder
     *            view holder to set as default view
     */
    private void setPrinterRowToDefault(ViewHolder viewHolder) {
        if (viewHolder == null) {
            return;
        }
        PrintersContainerView printerItem = (PrintersContainerView) viewHolder.mDeleteButton.getTag();
        
        if (printerItem.getDelete()) {
            printerItem.setDelete(false);
            mDeleteViewHolder = null;
        }
        if (printerItem.getDefault()) {
            return;
        }
        if (mDefaultViewHolder != null) {
            setPrinterRowToNormal(mDefaultViewHolder);
            mDefaultViewHolder = null;
        }
        printerItem.setDefault(true);
        mDefaultViewHolder = viewHolder;
    }
    
    /**
     * Reset the view holder
     * 
     * @param viewHolder
     *            view holder to reset
     */
    private void setPrinterRow(ViewHolder viewHolder) {
        Printer printer = (Printer) viewHolder.mPrinterName.getTag();
        
        if (printer.getId() == mPrinterManager.getDefaultPrinter()) {
            setPrinterRowToDefault(viewHolder);
        } else {
            setPrinterRowToNormal(viewHolder);
        }
    }
    
    // ================================================================================
    // INTERFACE - View.OnClick
    // ================================================================================
    
    @Override
    public void onClick(View v) {
        
        switch (v.getId()) {
            case R.id.printerListRow:
                Printer printer = (Printer) v.findViewById(R.id.img_disclosure).getTag();
                PrinterInfoFragment fragment = new PrinterInfoFragment();
                fragment.setPrinter(printer);
                if (mPauseableHandler != null) {
                    Message msg = Message.obtain(mPauseableHandler, PrintersFragment.MSG_SUBMENU_BUTTON);
                    msg.obj = fragment;
                    mPauseableHandler.sendMessage(msg);
                }
                break;
            case R.id.btn_delete:
                if (mCallbackRef != null && mCallbackRef.get() != null) {
                    mCallbackRef.get().dialogConfirmDelete();
                }
                PrintersContainerView printerContainer = (PrintersContainerView) v.getTag();
                mDeleteViewHolder = (ViewHolder) printerContainer.getTag();
                break;
        }
    }
    
    // ================================================================================
    // INTERFACE - Callback
    // ================================================================================
    
    /** {@inheritDoc} */
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_REMOVE_PRINTER:
                Printer printer = (Printer) msg.obj;
                if (mPrinterManager.removePrinter(printer)) {
                    remove(printer);
                    notifyDataSetChanged();
                }
                return true;
        }
        return false;
    }

    // ================================================================================
    // Internal Classes
    // ================================================================================
    
    /**
     * Printers Screen view holder for phone
     */
    public class ViewHolder {
        private TextView mPrinterName;
        private TextView mIpAddress;
        private Button mDeleteButton;
        private ImageView mDiscloseImage;
    }
}