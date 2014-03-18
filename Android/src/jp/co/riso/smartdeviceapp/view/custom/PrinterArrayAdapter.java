package jp.co.riso.smartdeviceapp.view.custom;

import java.util.List;

import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartdeviceapp.view.fragment.PrinterInfoFragment;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class PrinterArrayAdapter extends ArrayAdapter<Printer> implements View.OnClickListener, View.OnTouchListener {
    public class ViewHolder{
        ImageView onlineIndcator;
        TextView printerName;
        Button deleteButton;
        ImageView discloseImage;
    }
    
    public final String FRAGMENT_TAG_PRINTER_INFO = "fragment_printer_info";
    private final Context mContext;
    private int mLayoutId = 0;
    private ViewHolder mHolder;
    private PrinterManager mPrinterManager = null;
    
    public PrinterArrayAdapter(Context context, int resource, List<Printer> values) {
        super(context, resource, values);
        this.mContext = context;            
        this.mLayoutId = resource;
        mPrinterManager = PrinterManager.sharedManager(context);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Printer printer = getItem(position);
        
        
        if (convertView == null) {    
            convertView = inflater.inflate(mLayoutId, parent, false);
            mHolder = new ViewHolder();
            mHolder.printerName = (TextView) convertView.findViewById(R.id.txt_printerName);
            mHolder.printerName.setText(printer.getName());
            
            mHolder.discloseImage = (ImageView) convertView.findViewById(R.id.img_disclosure);     
            mHolder.deleteButton = (Button) convertView.findViewById(R.id.btn_delete);
            
            //Set listener for disclosure icon
            mHolder.discloseImage.setOnClickListener(this);
            
            mHolder.discloseImage.setTag(printer);
            mHolder.printerName.setTag(printer);
            mHolder.deleteButton.setTag(printer);
            
            convertView.setOnClickListener(this);                
            convertView.setOnTouchListener(this);
            mHolder.deleteButton.setOnClickListener(this);
            
            convertView.setTag(mHolder);
        }
        else {
            mHolder = (ViewHolder) convertView.getTag();
            mHolder.printerName.setText(printer.getName());
            mHolder.deleteButton.setVisibility(View.GONE);
            mHolder.printerName.setTextColor(mContext.getResources().getColor(R.color.theme_dark_1));
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.theme_light_2));
        }
        
        setPrinterRow(mHolder);
        
        return convertView;
    }
    
    private View prevView = null;
    
    // ================================================================================
    // ADAPTER INTERFACE - View.OnTouch
    // ================================================================================
    private float downX = 0;
    private float upX = 0;
    private float HORIZONTAL_MIN_DISTANCE = 50;
    private ViewHolder mDeleteViewHolder = null;
    private ViewHolder mDefaultViewHolder = null;
    
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                downX = 0;
                upX = 0;
                
                downX = event.getX();
                return false;
            }
            case MotionEvent.ACTION_MOVE: {
                upX = event.getX();
                float deltaX = downX - upX;
                
                // horizontal swipe detection
                if (deltaX > HORIZONTAL_MIN_DISTANCE) {                        
                    if(v.getId() == R.id.printerListRow){
                        hideDeleteButton();
                        setPrinterRowToDelete((ViewHolder)v.getTag());
                        return true;
                    }   
                }       
            }
        }
        return false;
    }
    
    // ================================================================================
    // ADAPTER INTERFACE - View.OnClick
    // ================================================================================
    @Override
    public void onClick(View v) {
        
        if(v.getId() == R.id.img_disclosure) {
            Printer printer = (Printer) v.getTag();
            hideDeleteButton();
            
            Intent intent = ((Activity) mContext).getIntent();
            intent.putExtra(PrinterInfoFragment.KEY_PRINTER_INFO, printer);
            ((Activity) mContext).setIntent(intent);
            
            BaseFragment fragment = new PrinterInfoFragment();
            switchToFragment(fragment, FRAGMENT_TAG_PRINTER_INFO);
        }
        else if(v.getId() == R.id.printerListRow) {
            ViewHolder viewHolder = (ViewHolder) v.getTag();
            Printer printer = (Printer) viewHolder.discloseImage.getTag();
            hideDeleteButton();
            
            setPrinterRowToDefault(viewHolder);
            
            
            mPrinterManager.setDefaultPrinter(printer);
            if(prevView == null)
                prevView = v;
            else if(prevView != v){
                setPrinterRowToNormal((ViewHolder)prevView.getTag());        
                prevView = v;
            }
        }
        else if(v.getId() == R.id.btn_delete) {
            final Printer printer = (Printer) v.getTag();
            
            mPrinterManager.removePrinter(printer);
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        remove(printer);
                        notifyDataSetChanged();
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
    
    // ================================================================================
    // Public Methods
    // ================================================================================
    public void hideDeleteButton() {
        if(mDeleteViewHolder != null) {
            setPrinterRow(mDeleteViewHolder);
        }
        mDeleteViewHolder = null;
    }
    
    // ================================================================================
    // Private Methods
    // ================================================================================
    private void setPrinterRowToNormal(ViewHolder viewHolder) {
        viewHolder.printerName.setTextColor(mContext.getResources().getColor(R.color.theme_dark_1));
        ((View)viewHolder.printerName.getParent()).setBackgroundColor(mContext.getResources().getColor(R.color.theme_light_2));
        viewHolder.deleteButton.setVisibility(View.GONE);    
    }
    
    private void setPrinterRowToDefault(ViewHolder viewHolder) {
        if(mDefaultViewHolder != null) {
            setPrinterRowToNormal(mDefaultViewHolder);
        }            
        viewHolder.printerName.setTextColor(mContext.getResources().getColor(R.color.theme_light_1));
        ((View)viewHolder.printerName.getParent()).setBackgroundColor(mContext.getResources().getColor(R.color.theme_dark_1));
        viewHolder.deleteButton.setVisibility(View.GONE);
        mDefaultViewHolder = viewHolder;
    }
    
    private void setPrinterRowToDelete(ViewHolder viewHolder) {
        viewHolder.printerName.setTextColor(mContext.getResources().getColor(R.color.theme_light_1));
        ((View)viewHolder.printerName.getParent()).setBackgroundColor(mContext.getResources().getColor(R.color.theme_color_2));
        viewHolder.deleteButton.setVisibility(View.VISIBLE);
        viewHolder.deleteButton.setBackgroundColor(mContext.getResources().getColor(R.color.theme_red_1));
        mDeleteViewHolder = viewHolder;
    }
    
    private void setPrinterRow(ViewHolder viewHolder) {
        Printer printer = (Printer) viewHolder.printerName.getTag();
        if(printer.getId() == mPrinterManager.getDefaultPrinter()) {
            setPrinterRowToDefault(viewHolder);
        }
        else {
            setPrinterRowToNormal(viewHolder);
        }  
    }
    
    private void switchToFragment(BaseFragment fragment, String tag) {
        FragmentManager fm = ((Activity) mContext).getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        
        //TODO Add Animation: Must Slide
        ft.addToBackStack(null);
        ft.replace(R.id.mainLayout, fragment, tag);
        ft.commit();
    }   
}