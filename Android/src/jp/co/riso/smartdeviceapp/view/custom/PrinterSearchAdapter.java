package jp.co.riso.smartdeviceapp.view.custom;

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
    public class ViewHolder{
        ImageView addedIndicator;
        TextView printerName;
    }
    
    private PrinteSearchAdapterInterface mSearchAdapterInterface = null;
    private Context mContext;
    private int layoutId;
    private ViewHolder mHolder;
    private PrinterManager mPrinterManager = null;
    
    public interface PrinteSearchAdapterInterface {
        public boolean isSearching();
        public void dialog();
    }
    
    public void setSearchAdapterInterface(PrinteSearchAdapterInterface searchAdapterInterface) {
        mSearchAdapterInterface = searchAdapterInterface;
    }
    
    public PrinterSearchAdapter(Context context, int resource, List<Printer> values) {
        super(context, resource, values);
        this.mContext = context;
        this.layoutId = resource;
        mPrinterManager = PrinterManager.sharedManager(context);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Printer printer =  getItem(position);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {             
            convertView = inflater.inflate(layoutId, parent, false);
            AppUtils.changeChildrenFont((ViewGroup) convertView, SmartDeviceApp.getAppFont());

            mHolder = new ViewHolder();
            mHolder.printerName = (TextView) convertView.findViewById(R.id.printerText);
            mHolder.addedIndicator = (ImageButton) convertView.findViewById(R.id.addPrinterButton); 
            
            mHolder.printerName.setText(printer.getName());
            
            if(mPrinterManager.isExists(printer)){
                //mHolder.addedIndicator
                mHolder.addedIndicator.setBackgroundResource(R.drawable.img_btn_add_printer_ok_pressed);
            }
            else {
                mHolder.addedIndicator.setBackgroundResource(R.drawable.selector_printersearch_add_printer);  
            }
            
            mHolder.addedIndicator.setTag(position);
            
            //Set listener for Add Button
            mHolder.addedIndicator.setOnClickListener(this);            
        }
        else
            mHolder = (ViewHolder) convertView.getTag();        
        
        return convertView;
    }
    
    @Override
    public void onClick(View v) {
        Printer printer =  getItem((Integer)v.getTag());

        if(v.getId() == R.id.addPrinterButton) {
            if(!mSearchAdapterInterface.isSearching()) {
                // For Add Button Press
                if(printer == null) {
                    return;
                }
                if(mPrinterManager.savePrinterToDB(printer) != -1) {
                    v.setBackgroundResource(R.drawable.img_btn_add_printer_ok_pressed);
                    mSearchAdapterInterface.dialog();
                }
            }
        }
    }
    
}
