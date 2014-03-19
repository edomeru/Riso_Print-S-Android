package jp.co.riso.smartdeviceapp.view.custom;

import java.util.ArrayList;
import java.util.List;

import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.fragment.PrintersFragment.PrinteSearchTabletInterface;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class PrintersScreenTabletView extends LinearLayout implements OnLongClickListener, 
    View.OnClickListener, OnTouchListener, PrinteSearchTabletInterface, OnCheckedChangeListener {
    public class ViewHolder{
        ImageView onlineIndcator;
        TextView printerName;
        ImageView deleteButton;
        TextView ipAddress;
        Switch defaultPrinter;
    }
    
    private PrinterManager mPrinterManager = null;
    private ArrayList<ViewGroup> mPrinterViewArray = null;
    private Context mContext = null;
    private int mOrientation = 0;
    
    public PrintersScreenTabletView(Context context) {
        super(context);
        this.mPrinterViewArray = new ArrayList<ViewGroup>();
        this.mContext = context;
        this.mOrientation = mContext.getResources().getConfiguration().orientation;
        init(context);
    }
    
    public PrintersScreenTabletView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mPrinterViewArray = new ArrayList<ViewGroup>();
        this.mContext = context;         
        this.mOrientation = mContext.getResources().getConfiguration().orientation;
        
        init(context);
        
    }
    
    public PrintersScreenTabletView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mPrinterViewArray = new ArrayList<ViewGroup>();
        this.mContext = context;        
        this.mOrientation = mContext.getResources().getConfiguration().orientation;
        
        init(context);
        
    }
    
    public void refreshPrintersList() {
        List<Printer> printer = mPrinterManager.getSavedPrintersList();
        for(int i = 0; i< mPrinterViewArray.size(); i++)
            mPrinterViewArray.get(i).removeAllViews();
        for(int i=0; i < printer.size(); i++) {
            addToTabletPrinterScreen(printer.get(i));
        }
    }
    
    // ================================================================================
    // Private methods
    // ================================================================================
    private void init(Context context) {
        ViewGroup viewGroup = (ViewGroup) View.inflate(context, R.layout.printers_tablet_container, this);
        mPrinterViewArray.add((LinearLayout) viewGroup.findViewById(R.id.column1));
        mPrinterViewArray.add((LinearLayout) viewGroup.findViewById(R.id.column2));
        mPrinterViewArray.add((LinearLayout) viewGroup.findViewById(R.id.column3));
        // Add Columns Depending on the orientation
        if(mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            mPrinterViewArray.get(2).setVisibility(GONE);
        }
        mPrinterManager = PrinterManager.sharedManager(context);
        viewGroup.setOnTouchListener(this);
    }
    
    private void setPrinterViewToNormal(ViewHolder viewHolder) {
        viewHolder.deleteButton.setVisibility(View.GONE);
        ((View)viewHolder.printerName.getParent()).setBackgroundColor(getResources().getColor(R.color.theme_light_4));
        viewHolder.onlineIndcator.setBackgroundColor(getResources().getColor(R.color.theme_light_4));
        viewHolder.printerName.setBackgroundColor(getResources().getColor(R.color.theme_light_4));
        viewHolder.printerName.setTextColor(getResources().getColor(R.color.theme_dark_1));
        viewHolder.deleteButton.setBackgroundColor(getResources().getColor(R.color.theme_light_4));
        viewHolder.defaultPrinter.setChecked(false);
    }
    
    private void setPrinterViewToDefault(ViewHolder viewHolder) {
        if(mDefaultViewHolder != null) {
            setPrinterViewToNormal(mDefaultViewHolder);
        }
        viewHolder.deleteButton.setVisibility(View.GONE);
        ((View)viewHolder.printerName.getParent()).setBackgroundColor(getResources().getColor(R.color.theme_dark_1));
        viewHolder.onlineIndcator.setBackgroundColor(getResources().getColor(R.color.theme_dark_1));
        viewHolder.printerName.setBackgroundColor(getResources().getColor(R.color.theme_dark_1));
        viewHolder.printerName.setTextColor(getResources().getColor(R.color.theme_light_1));
        viewHolder.deleteButton.setBackgroundColor(getResources().getColor(R.color.theme_dark_1));
        viewHolder.defaultPrinter.setChecked(true);
        mDefaultViewHolder = viewHolder;
    }
    
    private void setPrinterViewToDelete(ViewHolder viewHolder) {
        viewHolder.deleteButton.setVisibility(View.VISIBLE);
        ((View)viewHolder.printerName.getParent()).setBackgroundColor(getResources().getColor(R.color.theme_color_2));
        viewHolder.onlineIndcator.setBackgroundColor(getResources().getColor(R.color.theme_color_2));
        viewHolder.printerName.setBackgroundColor(getResources().getColor(R.color.theme_color_2));
        viewHolder.printerName.setTextColor(getResources().getColor(R.color.theme_light_1));
        viewHolder.deleteButton.setBackgroundColor(getResources().getColor(R.color.theme_color_2));
    }
    
    private void setPrinterView(ViewHolder viewHolder) {
        Printer printer = (Printer) viewHolder.deleteButton.getTag();

        if(mPrinterManager.getDefaultPrinter() == printer.getId()) {
            setPrinterViewToDefault(viewHolder);
        }
        else {
            setPrinterViewToNormal(viewHolder);
        } 
    }
    
    private int getActionBarHeight() {
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (mContext.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, mContext.getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }
    
    private ViewGroup getParentView() {
        switch(mOrientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                if(mPrinterViewArray.get(0).getChildCount() <= mPrinterViewArray.get(1).getChildCount() &&
                mPrinterViewArray.get(0).getChildCount() <= mPrinterViewArray.get(2).getChildCount()) {
                    return mPrinterViewArray.get(0);
                }
                else if(mPrinterViewArray.get(1).getChildCount() <= mPrinterViewArray.get(2).getChildCount()) {
                    return mPrinterViewArray.get(1);
                    
                }
                else {
                    return mPrinterViewArray.get(2);
                }
            case Configuration.ORIENTATION_PORTRAIT:
                if(mPrinterViewArray.get(0).getChildCount() <= mPrinterViewArray.get(1).getChildCount()) {
                    return mPrinterViewArray.get(0);
                }
                else {
                    return mPrinterViewArray.get(1);
                }
        }
        return null;
    }
    
    private void addToTabletPrinterScreen(Printer printer) {
        
        Point screenSize = AppUtils.getScreenDimensions((Activity) mContext);
        
        int width = 0;
        int height = 0;
        int left = 5;
        int top = 5;
        int right = 5;
        int bottom = 5;
        int actionBarHeight = getActionBarHeight();
        
        //Initial values are for landscape screen (2x3)
        int numberOfRow = 2;
        int numberOfColumn = 3;
        
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup parentView = getParentView();
        
        if(mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            //Update values for portrait screen (3x2)
            numberOfRow = 3;
            numberOfColumn = 2;
        }
        
        width = (int) (screenSize.x/numberOfColumn); 
        height = (int) (0.95 * (screenSize.y - actionBarHeight)/numberOfRow);
        
        if(parentView == null) {
            return;
        }
        
        View pView = inflater.inflate(R.layout.printers_tablet_container_item, parentView, false);
        AppUtils.changeChildrenFont((ViewGroup) pView, SmartDeviceApp.getAppFont());

        pView.setPadding(left, top, right, bottom);
        
        parentView.addView(pView, width, height);
 
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.printerName = (TextView) pView.findViewById(R.id.txt_printerName);
        viewHolder.deleteButton = (ImageView) pView.findViewById(R.id.btn_delete);
        viewHolder.onlineIndcator = (ImageView) pView.findViewById(R.id.img_tablet_onOff);
        viewHolder.ipAddress = (TextView) pView.findViewById(R.id.inputIpAddress);
        viewHolder.defaultPrinter = (Switch) pView.findViewById(R.id.default_printer_switch);
                
        viewHolder.printerName.setText(printer.getName());
        viewHolder.ipAddress.setText(printer.getIpAddress());
        
        viewHolder.printerName.setOnLongClickListener(this);
        viewHolder.deleteButton.setOnClickListener(this);
        viewHolder.defaultPrinter.setOnCheckedChangeListener(this);

        viewHolder.printerName.setTag(viewHolder);
        viewHolder.defaultPrinter.setTag(viewHolder);
        viewHolder.deleteButton.setTag(printer);
        
        
        setPrinterView(viewHolder);       
            
        return;
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left,top, right, bottom);
    }
    
    // ================================================================================
    // INTERFACE - onLongClick
    // ================================================================================
    private ViewHolder mDeleteViewHolder = null;
    private ViewHolder mDefaultViewHolder = null;

    @Override
    public boolean onLongClick(View v) {
        if(mDeleteViewHolder != null) {
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
        Printer printer = (Printer) v.getTag();
        mPrinterManager.removePrinter(printer);
        refreshPrintersList();
        return;
    }
    
    // ================================================================================
    // INTERFACE - onTouch
    // ================================================================================
    @Override
    public boolean onTouch(View arg0, MotionEvent arg1) {
        if(mDeleteViewHolder != null) {
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
        Printer printer = (Printer) viewHolder.deleteButton.getTag();
        if(isChecked) {
            setPrinterViewToDefault(viewHolder);            
            mPrinterManager.setDefaultPrinter(printer);
        } else {
            mPrinterManager.clearDefaultPrinter();
            setPrinterViewToNormal(viewHolder);
        }
    }
    
}
