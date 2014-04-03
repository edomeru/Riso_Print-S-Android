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
import jp.co.riso.smartdeviceapp.R;
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
    private Context mContext;
    private int layoutId;
    private PrinterManager mPrinterManager = null;
    
    public PrinterSearchAdapter(Context context, int resource, List<Printer> values) {
        super(context, resource, values);
        this.mContext = context;
        this.layoutId = resource;
        mPrinterManager = PrinterManager.getInstance(SmartDeviceApp.getAppContext());
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Printer printer = getItem(position);
        ViewHolder viewHolder;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = inflater.inflate(layoutId, parent, false);
            AppUtils.changeChildrenFont((ViewGroup) convertView, SmartDeviceApp.getAppFont());
            
            viewHolder = new ViewHolder();
            viewHolder.mPrinterName = (TextView) convertView.findViewById(R.id.printerText);
            viewHolder.mAddedIndicator = (ImageButton) convertView.findViewById(R.id.addPrinterButton);
            viewHolder.mPrinterName.setText(printer.getName());
            viewHolder.mAddedIndicator.setBackgroundResource(R.drawable.selector_printersearch_addprinter);            
            viewHolder.mAddedIndicator.setTag(position);
            
            // Set listener for Add Button
            viewHolder.mAddedIndicator.setOnClickListener(this);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.mPrinterName.setText(printer.getName());
            viewHolder.mAddedIndicator.setTag(position);
        }
        
        if (mPrinterManager.isExists(printer)) {
            viewHolder.mAddedIndicator.setActivated(true);
        }
        else {
            viewHolder.mAddedIndicator.setActivated(false);
        }
        return convertView;
    }
    
    public void setSearchAdapterInterface(PrinterSearchAdapterInterface searchAdapterInterface) {
        mSearchAdapterInterface = searchAdapterInterface;
    }
    
    // ================================================================================
    // Internal Classes
    // ================================================================================
    
    public class ViewHolder {
        public ImageView mAddedIndicator;
        public TextView mPrinterName;
    }
    
    // ================================================================================
    // Interface
    // ================================================================================
    
    public interface PrinterSearchAdapterInterface {
        public int onAddPrinter(Printer printer);
    }
    
    // ================================================================================
    // Interface View.OnClick
    // ================================================================================
    
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.addPrinterButton) {
            Printer printer = getItem((Integer) v.getTag());
            if (mSearchAdapterInterface.onAddPrinter(printer) != -1) {
                v.setActivated(true);
            }
        }
    }
}
