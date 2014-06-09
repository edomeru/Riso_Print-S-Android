/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrinterSearchAdapter.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.printers;

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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class PrinterSearchAdapter extends ArrayAdapter<Printer> implements View.OnClickListener {
    private PrinterSearchAdapterInterface mSearchAdapterInterface = null;
    private int layoutId;
    private PrinterManager mPrinterManager = null;
    
    /**
     * Constructor
     * 
     * @param context
     * @param resource
     * @param values
     */
    public PrinterSearchAdapter(Context context, int resource, List<Printer> values) {
        super(context, resource, values);
        this.layoutId = resource;
        mPrinterManager = PrinterManager.getInstance(SmartDeviceApp.getAppContext());
    }
    
    /** {@inheritDoc} */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Printer printer = getItem(position);
        ViewHolder viewHolder;
        
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            
            convertView = inflater.inflate(layoutId, parent, false);
            AppUtils.changeChildrenFont((ViewGroup) convertView, SmartDeviceApp.getAppFont());
            viewHolder = new ViewHolder();
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        initializeView(viewHolder, convertView, printer, position);

        return convertView;
    }
    
    /**
     * Initialize the view of the printer
     * 
     * @param viewHolder
     *            View Holder of the printer object
     * @param convertView
     *            Row view
     * @param position
     *            Position or index of the printer object
     */
    public void initializeView(ViewHolder viewHolder, View convertView, Printer printer, int position) {
        if (viewHolder == null || convertView == null || printer == null) {
            return;
        }
        View separator = null;
        String printerName = printer.getName();
        
        viewHolder.mPrinterName = (TextView) convertView.findViewById(R.id.printerText);
        viewHolder.mIpAddress = (TextView) convertView.findViewById(R.id.ipAddressText);
        viewHolder.mAddedIndicator = (ImageButton) convertView.findViewById(R.id.addPrinterButton);
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
     * Set Printer Search Screen Adapter Interface
     */
    public void setSearchAdapterInterface(PrinterSearchAdapterInterface searchAdapterInterface) {
        mSearchAdapterInterface = searchAdapterInterface;
    }
    
    // ================================================================================
    // Internal Classes
    // ================================================================================
    
    /**
     * Printer Search Screen view holder
     */
    public class ViewHolder {
        public ImageView mAddedIndicator;
        public TextView mPrinterName;
        public TextView mIpAddress;
    }
    
    // ================================================================================
    // Interface
    // ================================================================================
    
    /**
     * Printer Search Screen interface
     */
    public interface PrinterSearchAdapterInterface {
        /**
         * On add printer callback.
         * <p>
         * Callback called to add a searched printer to the Printers Screen.
         * 
         * @param printer
         *            searched printer
         */
        public int onAddPrinter(Printer printer);
    }
    
    // ================================================================================
    // Interface View.OnClick
    // ================================================================================
    
    /** {@inheritDoc} */
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
