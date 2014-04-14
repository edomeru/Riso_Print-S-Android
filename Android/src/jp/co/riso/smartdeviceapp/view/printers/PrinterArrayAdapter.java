/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrinterArrayAdapter.java
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
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartdeviceapp.view.fragment.PrinterInfoFragment;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
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
    public final static String FRAGMENT_TAG_PRINTER_INFO = "fragment_printer_info";
    private static final int MSG_REMOVE_PRINTER = 0x01;
    
    private PrinterManager mPrinterManager = null;
    private ViewHolder mDeleteViewHolder = null;
    private ViewHolder mDefaultViewHolder = null;
    private int mLayoutId = 0;
    private Handler mHandler = null;
    
    public PrinterArrayAdapter(Context context, int resource, List<Printer> values) {
        super(context, resource, values);
        this.mLayoutId = resource;
        mPrinterManager = PrinterManager.getInstance(SmartDeviceApp.getAppContext());
        mDeleteViewHolder = null;
        mDefaultViewHolder = null;
        mHandler = new Handler(this);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewHolder viewHolder = null;
        Printer printer = getItem(position);
        View separator = null;
        
        if (convertView == null) {
            convertView = inflater.inflate(mLayoutId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mOnlineIndcator = (ImageView) convertView.findViewById(R.id.img_onOff);
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
            viewHolder.mDiscloseImage.setOnClickListener(this);
            viewHolder.mDeleteButton.setOnClickListener(this);
            
            convertView.setTag(viewHolder);
            
            AppUtils.changeChildrenFont((ViewGroup) convertView, SmartDeviceApp.getAppFont());
            
        } else {
            PrintersContainer printerItem = (PrintersContainer) convertView;
            
            viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.mPrinterName.setText(printer.getName());
            viewHolder.mIpAddress.setText(printer.getIpAddress());
            
            if (printerItem.getDefault()) {
                printerItem.setDefault(false);
            }
            if (printerItem.getDelete()) {
                viewHolder.mDeleteButton.setVisibility(View.INVISIBLE);
                printerItem.setDelete(false);
            }
            viewHolder.mDiscloseImage.setTag(printer);
            viewHolder.mPrinterName.setTag(printer);
            viewHolder.mDeleteButton.setTag(convertView);
        }
        
        separator = convertView.findViewById(R.id.printers_separator);
        if (position == getCount() - 1) {
            separator.setVisibility(View.GONE);
        } else {
            separator.setVisibility(View.VISIBLE);
        }
        setPrinterRow(viewHolder);
        mPrinterManager.updateOnlineStatus(printer.getIpAddress(), viewHolder.mOnlineIndcator);
        
        return convertView;
    }
    
    @Override
    public boolean isEnabled(int position) {
        return false;
    }
    
    // ================================================================================
    // Public Methods
    // ================================================================================
    
    public void hideDeleteButton() {
        if (mDeleteViewHolder != null) {
            setPrinterRow(mDeleteViewHolder);
        }
    }
    
    public void setPrinterRowToDelete(View convertView) {
        if (convertView == null) {
            return;
        }
        PrintersContainer printerItem = (PrintersContainer) convertView;
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
    
    private void setPrinterRowToNormal(ViewHolder viewHolder) {
        if (viewHolder == null) {
            return;
        }
        PrintersContainer printerItem = (PrintersContainer) viewHolder.mDeleteButton.getTag();
        
        if (printerItem.getDefault()) {
            printerItem.setDefault(false);
            mDefaultViewHolder = null;
        }
        if (printerItem.getDelete()) {
            printerItem.setDelete(false);
            mDeleteViewHolder = null;
        }
    }
    
    private void setPrinterRowToDefault(ViewHolder viewHolder) {
        if (viewHolder == null) {
            return;
        }
        PrintersContainer printerItem = (PrintersContainer) viewHolder.mDeleteButton.getTag();
        
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
    
    private void setPrinterRow(ViewHolder viewHolder) {
        Printer printer = (Printer) viewHolder.mPrinterName.getTag();
        
        if (printer.getId() == mPrinterManager.getDefaultPrinter()) {
            setPrinterRowToDefault(viewHolder);
        } else {
            setPrinterRowToNormal(viewHolder);
        }
    }
    
    private void switchToFragment(BaseFragment fragment, String tag) {
        FragmentManager fm = ((Activity) getContext()).getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        
        ft.setCustomAnimations(R.animator.left_slide_in, R.animator.left_slide_out, R.animator.right_slide_in, R.animator.right_slide_out);
        ft.addToBackStack(null);
        ft.replace(R.id.mainLayout, fragment, tag);
        ft.commit();
    }
    
    // ================================================================================
    // INTERFACE - View.OnClick
    // ================================================================================
    
    @Override
    public void onClick(View v) {
        
        switch (v.getId()) {
            case R.id.img_disclosure:
                Activity activity = (Activity) getContext();
                Printer printer = (Printer) v.getTag();
                
                Intent intent = activity.getIntent();
                intent.putExtra(PrinterInfoFragment.KEY_PRINTER_INFO, printer);
                activity.setIntent(intent);
                
                BaseFragment fragment = new PrinterInfoFragment();
                switchToFragment(fragment, FRAGMENT_TAG_PRINTER_INFO);
                break;
            case R.id.printerListRow:
                if (mDeleteViewHolder != null) {
                    return;
                }
                ViewHolder viewHolder = (ViewHolder) v.getTag();
                printer = (Printer) viewHolder.mDiscloseImage.getTag();
                
                setPrinterRowToDefault(viewHolder);
                mPrinterManager.setDefaultPrinter(printer);
                break;
            case R.id.btn_delete:
                PrintersContainer printerContainer = (PrintersContainer) v.getTag();
                viewHolder = (ViewHolder) printerContainer.getTag();
                printer = (Printer) viewHolder.mDiscloseImage.getTag();
                
                Message newMessage = Message.obtain(mHandler, MSG_REMOVE_PRINTER);
                newMessage.obj = printer;
                mHandler.sendMessage(newMessage);
                break;
        }
    }
    
    // ================================================================================
    // Interface Callback
    // ================================================================================
    
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_REMOVE_PRINTER:
                Printer printer = (Printer) msg.obj;
                mPrinterManager.removePrinter(printer);
                remove(printer);
                notifyDataSetChanged();
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
        private TextView mIpAddress;
        private Button mDeleteButton;
        private ImageView mDiscloseImage;
    }
}