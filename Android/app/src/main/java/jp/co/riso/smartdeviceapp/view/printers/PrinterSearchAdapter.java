/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrinterSearchAdapter.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.printers;

import java.util.List;

import jp.co.riso.smartprint.R;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.model.Printer;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @class PrinterSearchAdapter
 * 
 * @brief Array Adapter used for Printers Search Screen
 */
public class PrinterSearchAdapter extends ArrayAdapter<Printer> implements View.OnClickListener {
    private PrinterSearchAdapterInterface mSearchAdapterInterface = null;
    private final int layoutId;
    private final PrinterManager mPrinterManager;
    
    /**
     * @brief Constructor.
     * 
     * @param context Application context
     * @param resource Resource ID to be used as Searched printer row
     * @param values Searched printers list
     */
    public PrinterSearchAdapter(Context context, int resource, List<Printer> values) {
        super(context, resource, values);
        this.layoutId = resource;
        mPrinterManager = PrinterManager.getInstance(SmartDeviceApp.getAppContext());
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Printer printer = getItem(position);
        ViewHolder viewHolder;
        
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            
            convertView = inflater.inflate(layoutId, parent, false);
            // AppUtils.changeChildrenFont((ViewGroup) convertView, SmartDeviceApp.getAppFont());
            viewHolder = new ViewHolder();
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        initializeView(viewHolder, convertView, printer, position);

        return convertView;
    }
    
    /**
     * @brief Initialize the view of the printer.
     * 
     * @param viewHolder View Holder of the printer object
     * @param convertView Row view
     * @param printer Printer object
     * @param position Position or index of the printer object
     */
    public void initializeView(ViewHolder viewHolder, View convertView, Printer printer, int position) {
        if (viewHolder == null || convertView == null || printer == null) {
            return;
        }
        View separator;
        String printerName = printer.getName();
        
        viewHolder.mPrinterName = convertView.findViewById(R.id.printerText);
        viewHolder.mIpAddress = convertView.findViewById(R.id.ipAddressText);
        viewHolder.mAddedIndicator = convertView.findViewById(R.id.addPrinterButton);
        viewHolder.mPrinterName.setText(printer.getName());
        viewHolder.mIpAddress.setText(printer.getIpAddress());
        viewHolder.mAddedIndicator.setBackgroundResource(R.drawable.selector_printersearch_addprinter);
        viewHolder.mAddedIndicator.setTag(position);
        viewHolder.mAddedIndicator.setClickable(false);
        
        separator = convertView.findViewById(R.id.printers_separator);
        if (position == getCount() - 1) {
            separator.setVisibility(View.GONE);
        } else {
            separator.setVisibility(View.VISIBLE);
        }
        if (mPrinterManager.isExists(printer)) {
            convertView.setClickable(false);
            viewHolder.mAddedIndicator.setActivated(true);
        } else {
            viewHolder.mAddedIndicator.setActivated(false);
        }
        
        if (printerName.isEmpty()) {
            viewHolder.mPrinterName.setText(getContext().getResources().getString(R.string.ids_lbl_no_name));
        }
        
        convertView.setOnClickListener(this);
        convertView.setTag(viewHolder);
    }
    
    /**
     * @brief Set Printer Search Screen Adapter Interface.
     * 
     * @param searchAdapterInterface Printer search adapter interface
     */
    public void setSearchAdapterInterface(PrinterSearchAdapterInterface searchAdapterInterface) {
        mSearchAdapterInterface = searchAdapterInterface;
    }
    
    // ================================================================================
    // Internal Classes
    // ================================================================================
    
    /**
     * @class ViewHolder
     * 
     * @brief Printer Search Screen view holder.
     */
    public static class ViewHolder {
        public ImageView mAddedIndicator;
        public TextView mPrinterName;
        public TextView mIpAddress;
    }
    
    // ================================================================================
    // Interface
    // ================================================================================
    
    /**
     * @interface PrinterSearchAdapterInterface
     * 
     * @brief Printer Search Screen interface.
     */
    public interface PrinterSearchAdapterInterface {
        /**
         * @brief On add printer callback. <br>
         *
         * Callback called to add a searched printer to the Printers Screen.
         * 
         * @param printer Searched printer
         * 
         * @retval 0 Success
         * @retval -1 Error
         */
        int onAddPrinter(Printer printer);
    }
    
    // ================================================================================
    // Interface View.OnClick
    // ================================================================================
    
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.printer_search_row) {
            ViewHolder viewHolder = (ViewHolder) v.getTag();
            Printer printer = getItem((Integer) viewHolder.mAddedIndicator.getTag());
            
            if (viewHolder.mAddedIndicator.isActivated()) {
                return;
            }
            if (mSearchAdapterInterface.onAddPrinter(printer) != -1) {
                v.setActivated(true);
            }
        }
    }
}
