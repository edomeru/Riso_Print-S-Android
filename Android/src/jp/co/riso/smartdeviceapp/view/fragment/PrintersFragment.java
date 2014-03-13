/*
 * Copyright (c) 2014 All rights reserved.
 *
 * PrintersFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.fragment;

import java.util.ArrayList;
import java.util.List;

import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import android.app.ActionBar.LayoutParams;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class PrintersFragment extends BaseFragment implements View.OnTouchListener {
    
    public static final String FRAGMENT_TAG_PRINTER_SEARCH = "fragment_printer_search";
    public static final String FRAGMENT_TAG_ADD_PRINTER = "fragment_add_printer";

    public static final String FRAGMENT_TAG_PRINTER_INFO = "fragment_printer_info";
    
    public final int ID_MENU_ACTION_SEARCH_BUTTON = 0x11000002;
    public final int ID_MENU_ACTION_ADD_BUTTON    = 0x11000003;
    
    //ListView parameters
    private ListView mListView = null;
    private ArrayList<Printer> mPrinter = null;
    private ArrayAdapter<Printer> mPrinterAdapter = null;
    
    private PrinterManager mPrinterManager = null;
    @Override
    public int getViewLayout() {
        if(isTablet()) {
            return R.layout.fragment_printers_tablet;
        }
        else {
            return R.layout.fragment_printers;
        }
    }
    
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        mPrinterManager =  PrinterManager.sharedManager(getActivity());
        mPrinter = new ArrayList<Printer>();
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        if(isTablet()) {
            return;
        }
        else {
            mListView = (ListView) view.findViewById(R.id.printer_list);
            mListView.setOnTouchListener(this);
        }
    }
    
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_printers);
        addPrinterActionMenuButton(view);
    }
    
    
    public void addPrinterActionMenuButton(View v) {
        ImageButton addMenuButton = new ImageButton(v.getContext());
        ImageButton searchMenuButton = new ImageButton(v.getContext());
        
        ViewGroup rightActionLayout = (ViewGroup) v.findViewById(R.id.rightActionLayout);
        
        //Manual Add Button
        addMenuButton.setId(ID_MENU_ACTION_ADD_BUTTON);
        addMenuButton.setImageResource(R.drawable.add);
        addMenuButton.setBackgroundResource(R.drawable.button_actionmenu_bg_selector);
        
        //Search Button
        searchMenuButton.setId(ID_MENU_ACTION_SEARCH_BUTTON);
        searchMenuButton.setImageResource(R.drawable.search);
        searchMenuButton.setBackgroundResource(R.drawable.button_actionmenu_bg_selector);
        
        rightActionLayout.addView(addMenuButton, LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        rightActionLayout.addView(searchMenuButton, LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        
        searchMenuButton.setOnClickListener(this);
        addMenuButton.setOnClickListener(this);
        
        //Left Action Button
        addActionMenuButton(v);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        setPrinterArrayList();
        if(isTablet()) {
            return;
        }
        else {
            mPrinterAdapter = new PrinterArrayAdapter(getActivity(),
                    R.layout.printer_list_item, mPrinter);
            mListView.setAdapter(mPrinterAdapter);
        }
    }
    
    // ================================================================================
    // Private Methods
    // ================================================================================
    private void displayPrinterSearchFragment() {
        BaseFragment fragment = new PrinterSearchFragment();
        switchToFragment(fragment, FRAGMENT_TAG_PRINTER_SEARCH);
    }
    
    private void displayAddPrinterFragment() {
        BaseFragment fragment = new AddPrinterFragment();
        switchToFragment(fragment, FRAGMENT_TAG_ADD_PRINTER);
    }
    
    public void switchToFragment(BaseFragment fragment, String tag) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (isTablet()) {
            if (getActivity() != null && getActivity() instanceof MainActivity) {
                MainActivity activity = (MainActivity) getActivity();
                ft.replace(R.id.rightLayout, fragment, tag);
                ft.commit();
                activity.openDrawer(Gravity.RIGHT);
            }
        } else {
            //TODO Add Animation: Must Slide
            ft.addToBackStack(null);
            ft.replace(R.id.mainLayout, fragment, tag);
            ft.commit();
        }
    }
    
    private void setPrinterArrayList() {
        mPrinter.clear();
        mPrinter = (ArrayList<Printer>) mPrinterManager.getSavedPrintersList();
    }
    
    // ================================================================================
    // INTERFACE - PrinterArrayAdapter
    // ================================================================================
    public class PrinterArrayAdapter extends ArrayAdapter<Printer> implements View.OnClickListener, View.OnTouchListener {
        public class ViewHolder{
            ImageView onlineIndcator;
            TextView printerName;
            TextView defaultPrinter;
            Button deleteButton;
            ImageView discloseImage;
        }
        
        private final Context context;
        private int layoutId;
        private ViewHolder mHolder;
        
        public PrinterArrayAdapter(Context context, int resource, List<Printer> values) {
            super(context, resource, values);
            this.context = context;            
            this.layoutId = resource;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            Printer rowView = getItem(position);
            
            
            if (convertView == null) {    
                convertView = inflater.inflate(layoutId, parent, false);
                mHolder = new ViewHolder();
                mHolder.printerName = (TextView) convertView.findViewById(R.id.txt_printerName);
                mHolder.printerName.setText(rowView.getName());
                
                mHolder.discloseImage = (ImageView) convertView.findViewById(R.id.img_disclosure);     
                mHolder.deleteButton = (Button) convertView.findViewById(R.id.btn_delete);
                
                //Set listener for disclosure icon
                mHolder.discloseImage.setOnClickListener(this);
                
                mHolder.discloseImage.setTag(position);
                mHolder.printerName.setTag(position);
                mHolder.deleteButton.setTag(position);
                
                convertView.setOnClickListener(this);                
                convertView.setOnTouchListener(this);
                mHolder.deleteButton.setOnClickListener(this);
                
                convertView.setTag(mHolder);
            }
            else {
                mHolder = (ViewHolder) convertView.getTag();
                mHolder.printerName.setText(rowView.getName());
                mHolder.deleteButton.setVisibility(View.GONE);
                mHolder.printerName.setTextColor(getResources().getColor(R.color.theme_dark_1));
                convertView.setBackgroundColor(getResources().getColor(R.color.theme_light_2));
            }
            
            // Check if default printer
            if(mPrinter.get(position).getId() == mPrinterManager.getDefaultPrinter()) {
                prevView = convertView;
                mHolder.printerName.setTextColor(getResources().getColor(R.color.theme_light_1));
                convertView.setBackgroundColor(getResources().getColor(R.color.theme_dark_1));
            }
            
            return convertView;
        }
        
        private View prevView = null;
        
        // ================================================================================
        // ADAPTER INTERFACE - View.OnTouch
        // ================================================================================
        private float downX = 0;
        private float upX = 0;
        private float HORIZONTAL_MIN_DISTANCE = 50;
        private Button deleteButton = null;
        
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
                            deleteButton = (Button) v.findViewById(R.id.btn_delete);
                            deleteButton.setVisibility(View.VISIBLE);
                            deleteButton.setBackgroundColor(Color.RED);
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
                hideDeleteButton();
                
                Printer printer = mPrinter.get((Integer)v.getTag());
                Bundle args = getArguments();
                if (args == null) {
                    Intent intent = getActivity().getIntent();
                    intent.putExtra(PrinterInfoFragment.KEY_PRINTER_INFO, printer);
                    getActivity().setIntent(intent);
                } else {
                    args.putParcelable(PrinterInfoFragment.KEY_PRINTER_INFO, printer);
                }    
                
                BaseFragment fragment = new PrinterInfoFragment();
                switchToFragment(fragment, FRAGMENT_TAG_PRINTER_INFO);
            }
            else if(v.getId() == R.id.printerListRow) {
                hideDeleteButton();
                
                v.setBackgroundColor(getResources().getColor(R.color.theme_dark_1));
                TextView textView = (TextView)v.findViewById(R.id.txt_printerName);
                textView.setTextColor(getResources().getColor(R.color.theme_light_1));
                
                mPrinterManager.setDefaultPrinter(mPrinter.get((Integer)textView.getTag()));
                if(prevView == v) {
                    
                }
                else if(prevView == null)
                    prevView = v;
                else {
                    prevView.setBackgroundColor(getResources().getColor(R.color.theme_light_2));
                    textView = (TextView)prevView.findViewById(R.id.txt_printerName);
                    textView.setTextColor(getResources().getColor(R.color.theme_dark_1));
                    prevView = v;
                }
            }
            else if(v.getId() == R.id.btn_delete) {
          
                final Printer printer = mPrinter.get((Integer) v.getTag());
                mPrinterManager.removePrinter(printer);
                
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mPrinter.remove(printer);
                            mPrinter = (ArrayList<Printer>) mPrinterManager.getSavedPrintersList();
                            mPrinterAdapter.notifyDataSetChanged();
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
        
        public void hideDeleteButton() {
            if(deleteButton != null)
                deleteButton.setVisibility(View.GONE);
            deleteButton = null;
        }
        
    }
    
    
    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================
    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case ID_MENU_ACTION_SEARCH_BUTTON:
                displayPrinterSearchFragment();
                break;
            case ID_MENU_ACTION_ADD_BUTTON:
                displayAddPrinterFragment();
                break;
            default:
                break;
        }
    }
    
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        PrinterArrayAdapter printerArrayAdapter = (PrinterArrayAdapter) mPrinterAdapter;
        printerArrayAdapter.hideDeleteButton();        
        return false;
    }
}
