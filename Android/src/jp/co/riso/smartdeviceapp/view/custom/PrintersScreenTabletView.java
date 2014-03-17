package jp.co.riso.smartdeviceapp.view.custom;

import java.util.ArrayList;
import java.util.List;

import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.model.Printer;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PrintersScreenTabletView extends LinearLayout {
    
    private ArrayList<ViewGroup> mPrinterViewArray = null;
    private Context mContext = null;
    private int mLayoutId = 0;
    private int mOrientation = 0;
    
    public PrintersScreenTabletView(Context context) {
        super(context);
        this.mPrinterViewArray = new ArrayList<ViewGroup>();
        this.mContext = context;   //TODO
        this.mLayoutId = R.layout.printer_tablet_view;
        this.mOrientation = mContext.getResources().getConfiguration().orientation;
        init(context);
    }
    
    public PrintersScreenTabletView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mPrinterViewArray = new ArrayList<ViewGroup>();
        this.mContext = context;   //TODO         
        this.mLayoutId = R.layout.printer_tablet_view;
        this.mOrientation = mContext.getResources().getConfiguration().orientation;
        
        init(context);
        
    }
    
    public PrintersScreenTabletView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mPrinterViewArray = new ArrayList<ViewGroup>();
        this.mContext = context;   //TODO         
        this.mLayoutId = R.layout.printer_tablet_view;
        this.mOrientation = mContext.getResources().getConfiguration().orientation;
        
        init(context);
        
    }
    
    private void init(Context context) {
        View viewGroup = View.inflate(context, R.layout.view_columns, this);
        mPrinterViewArray.add((LinearLayout) viewGroup.findViewById(R.id.column1));
        mPrinterViewArray.add((LinearLayout) viewGroup.findViewById(R.id.column2));
        mPrinterViewArray.add((LinearLayout) viewGroup.findViewById(R.id.column3));
        // Add Columns Depending on the orientation
        if(mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            mPrinterViewArray.get(2).setVisibility(GONE);
        }
    }
    
    public void notifyDataSetChanged(List<Printer> mPrinter) {
        for(int i=0; i < mPrinter.size(); i++) {
            addToTabletPrinterScreen(mPrinter.get(i));
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
        
        if(mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            //Update values for portrait screen (3x2)
            numberOfRow = 3;
            numberOfColumn = 2;
        }
        
        width = (int) (screenSize.x/numberOfColumn); 
        height = (int) (0.95 * (screenSize.y - actionBarHeight)/numberOfRow);
        
        
        //android.R.attr.actionBarSize
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View pView = inflater.inflate(mLayoutId, mPrinterViewArray.get(0), false);
        pView.setPadding(left, top, right, bottom);
        
        //Add printerView to the printerScreenLayout
        switch(mOrientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                if(mPrinterViewArray.get(0).getChildCount() <= mPrinterViewArray.get(1).getChildCount() &&
                mPrinterViewArray.get(0).getChildCount() <= mPrinterViewArray.get(2).getChildCount()) {
                    mPrinterViewArray.get(0).addView(pView, width, height);
                }
                else if(mPrinterViewArray.get(1).getChildCount() <= mPrinterViewArray.get(2).getChildCount()) {
                    mPrinterViewArray.get(1).addView(pView, width, height);
                    
                }
                else {
                    mPrinterViewArray.get(2).addView(pView, width, height);
                }
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                if(mPrinterViewArray.get(0).getChildCount() <= mPrinterViewArray.get(1).getChildCount()) {
                    mPrinterViewArray.get(0).addView(pView, width, height);
                }
                else {
                    mPrinterViewArray.get(1).addView(pView, width, height);
                    
                }
                break;
        }
        TextView printerName = (TextView) pView.findViewById(R.id.txt_printerName);
        printerName.setText(printer.getName());
        TextView ipAddress = (TextView) pView.findViewById(R.id.inputIpAddress);
        ipAddress.setText(printer.getIpAddress());
        
        return;
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // TODO Auto-generated method stub
        super.onLayout(changed, left,top, right, bottom);
    }
    
}
