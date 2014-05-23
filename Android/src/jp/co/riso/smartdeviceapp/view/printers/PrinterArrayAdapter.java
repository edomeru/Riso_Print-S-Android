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

import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartprint.R;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.model.Printer;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class PrinterArrayAdapter extends ArrayAdapter<Printer> implements View.OnClickListener {    
    private WeakReference<PrinterArrayAdapterInterface> mCallbackRef = null;
    private PrinterManager mPrinterManager = null;
    private ViewHolder mDeleteViewHolder = null;
    private ViewHolder mDefaultViewHolder = null;
    private int mLayoutId = 0;

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
            viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.mPrinterName.setText(printer.getName());
            viewHolder.mIpAddress.setText(printer.getIpAddress());
            viewHolder.mDiscloseImage.setTag(printer);
            viewHolder.mPrinterName.setTag(printer);
            viewHolder.mDeleteButton.setTag(convertView);
        }
        
        if (printerName == null || printerName.isEmpty()) {
            viewHolder.mPrinterName.setText(getContext().getResources().getString(R.string.ids_lbl_no_name));
        }
        separator = convertView.findViewById(R.id.printers_separator);
        if (position == getCount() - 1) {
            separator.setVisibility(View.GONE);
        } else {
            separator.setVisibility(View.VISIBLE);
        }
        
        return convertView;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isEnabled(int position) {
        return false;
    }
    
    /**
     * Sets the PrinterArrayAdapterInterface function
     * 
     * @param callback
     *            Callback function
     */
    public void setPrintersArrayAdapterInterface (PrinterArrayAdapterInterface callback) {
        mCallbackRef = new WeakReference<PrinterArrayAdapterInterface>(callback);
    }
      
    /**
     * This function is called to reset the delete view
     */
    public void resetDeletePrinterView() {
        if (mDeleteViewHolder != null) {
            mDeleteViewHolder = null;
        }
    }
    
    // ================================================================================
    // Public Methods
    // ================================================================================
    
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
    
    // ================================================================================
    // INTERFACE - View.OnClick
    // ================================================================================
    
    @Override
    public void onClick(View v) {
        
        switch (v.getId()) {
            case R.id.printerListRow:
                Printer printer = (Printer) v.findViewById(R.id.img_disclosure).getTag();
                if(mCallbackRef != null && mCallbackRef.get() != null) {
                    mCallbackRef.get().onPrinterListClicked(printer);
                }
                break;
            case R.id.btn_delete:
                if (mCallbackRef != null && mCallbackRef.get() != null) {
                    PrintersContainerView printerContainer = (PrintersContainerView) v.getTag();
                    mDeleteViewHolder = (ViewHolder) printerContainer.getTag();
                    mCallbackRef.get().onPrinterDeleteClicked();
                    mCallbackRef.get().setDeletePrinter((Printer) mDeleteViewHolder.mDiscloseImage.getTag());
                }
                break;
        }
    }
    
    // ================================================================================
    // INTERFACE - PrinterArrayAdapterInterface
    // ================================================================================
    
    /**
     * PrinterArrayAdapter Interface
     */
    public interface PrinterArrayAdapterInterface {
        /**
         * Dialog which is displayed to confirm printer delete
         */
        public void onPrinterDeleteClicked();
        
        /**
         * Set the printer to be deleted
         * 
         * @param printer
         *            Printer to be deleted
         */
        public void setDeletePrinter(Printer printer);
        
        /**
         * Display the PrinterInfoFragment of the corresponding printer item clicked
         * 
         * @param fragment
         *            PrinterInfoFragment to be displayed
         */
        public void onPrinterListClicked(Printer printer);
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