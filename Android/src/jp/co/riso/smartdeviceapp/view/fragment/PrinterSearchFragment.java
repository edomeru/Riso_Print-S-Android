/*
 * Copyright (c) 2014 All rights reserved.
 *
 * PrinterSearchFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.fragment;

import java.util.ArrayList;
import java.util.List;

import jp.co.riso.android.dialog.DialogUtils;
import jp.co.riso.android.dialog.InfoDialogFragment;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.OnPrinterSearch;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import eu.erikw.PullToRefreshListView;
import eu.erikw.PullToRefreshListView.OnRefreshListener;


public class PrinterSearchFragment extends BaseFragment implements OnRefreshListener, OnPrinterSearch {
    private static final String KEY_SEARCHED_PRINTER_LIST = "searched_printer_list";
    private static final String KEY_SEARCHED_PRINTER_DIALOG = "searched_printer_dialog";
    private static final String KEY_SEARCH_STATE = "searched_state";
    
    //ProgressBar parameters
    private boolean mIsSearching = false;
    
    //ListView parameters
    private PullToRefreshListView mListView = null;
    private ArrayList<Printer> mPrinter = null;
    private ArrayAdapter<Printer> mPrinterSearchAdapter = null;
    private PrinterManager mPrinterManager = null;
    
    @Override
    public int getViewLayout() {
        return R.layout.temp_fragment_base;
    }
    
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        mPrinter = new ArrayList<Printer>();
        mPrinterManager = PrinterManager.sharedManager(getActivity());
        mPrinterManager.setOnPrinterSearchListener(this);
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        mListView = (PullToRefreshListView) view.findViewById(R.id.printer_list);
        mListView.setBackgroundColor(getResources().getColor(R.color.theme_light_3));
        mListView.setLockScrollWhileRefreshing(false);
    }
    
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_search_printers);
        addActionMenuButton(view);
    }
    
    @Override
    public void addActionMenuButton(View v) {
        if(isTablet()) {
            v.setPadding((int) (getResources().getDimension(R.dimen.preview_view_margin)/getResources().getDisplayMetrics().density), 0, 0, 0);
        }
        ImageButton actionMenuButton = new ImageButton(v.getContext());
        ViewGroup leftActionLayout = (ViewGroup) v.findViewById(R.id.leftActionLayout);
        
        actionMenuButton.setId(ID_MENU_ACTION_BUTTON);
        actionMenuButton.setImageResource(R.drawable.back);
        actionMenuButton.setBackgroundResource(R.drawable.button_actionmenu_bg_selector);
        actionMenuButton.setBackgroundColor(getResources().getColor(R.color.theme_color_2));
        actionMenuButton.setOnClickListener(this);
        
        leftActionLayout.addView(actionMenuButton, LayoutParams.WRAP_CONTENT, 
                LayoutParams.MATCH_PARENT);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        if(savedInstanceState != null) {
            mPrinter = savedInstanceState.getParcelableArrayList(KEY_SEARCHED_PRINTER_LIST);
            mIsSearching = savedInstanceState.getBoolean(KEY_SEARCH_STATE, false);
        }
        
        mPrinterSearchAdapter = new PrinterSearchAdapter(getActivity(),
                R.layout.printer_search_list_item, mPrinter);
        mListView.setAdapter(mPrinterSearchAdapter);
        // Refresh Listener
        mListView.setOnRefreshListener(this);    
        
        if(mIsSearching){
            updateRefreshBar();
            mIsSearching = false;
            mListView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mListView.onRefreshComplete();
                }
            }, 500);
        }
        
        if(savedInstanceState == null) {
            onRefresh();
            updateRefreshBar();
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelableArrayList(KEY_SEARCHED_PRINTER_LIST, mPrinter);
        savedInstanceState.putBoolean(KEY_SEARCH_STATE, mIsSearching);
        super.onSaveInstanceState(savedInstanceState);
    }
    
    // ================================================================================
    // Private Methods
    // ================================================================================
    private void dialogCb() {
        // TODO: Get values from resources 
        // Open Dialog box 
        InfoDialogFragment info = InfoDialogFragment.newInstance("Add Printer Info", 
                "The new printer was added successfully", 
                getResources().getString(R.string.ids_lbl_ok));
        DialogUtils.displayDialog(getActivity(), KEY_SEARCHED_PRINTER_DIALOG, info);
    }
    
    public void updateRefreshBar() {
        getActivity().runOnUiThread(new Runnable(){
            @Override
            public void run() {
                try{
                    if(mIsSearching)
                        mListView.setRefreshing();
                    else
                        mListView.onRefreshComplete();
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    // ================================================================================
    // Adapter - PrinterSearchAdapter
    // ================================================================================
    public class PrinterSearchAdapter extends ArrayAdapter<Printer> implements View.OnClickListener {
        public class ViewHolder{
            ImageView addedIndicator;
            TextView printerName;
        }
        
        private Context mContext;
        private int layoutId;
        private ViewHolder mHolder;
        
        public PrinterSearchAdapter(Context context, int resource, List<Printer> values) {
            super(context, resource, values);
            this.mContext = context;
            this.layoutId = resource;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Printer rowView =  getItem(position);
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            
            if (convertView == null) {             
                convertView = inflater.inflate(layoutId, parent, false);
                mHolder = new ViewHolder();
                mHolder.printerName = (TextView) convertView.findViewById(R.id.printer_text);
                mHolder.addedIndicator = (ImageButton) convertView.findViewById(R.id.addPrinterButton); 
                
                mHolder.printerName.setText(rowView.getName());
                if(mPrinterManager.isExists(mPrinter.get(position))){
                    mHolder.addedIndicator.setBackgroundResource(R.drawable.check);
                }
                else {
                    mHolder.addedIndicator.setBackgroundResource(R.drawable.add);  
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
            if(v.getId() == R.id.addPrinterButton) {
                if(!mIsSearching) {
                    // For Add Button Press
                    Printer printer = mPrinter.get((Integer) v.getTag());
                    if(printer == null) {
                        return;
                    }                
                    if(mPrinterManager.savePrinterToDB(printer) != -1) {
                        v.setBackgroundResource(R.drawable.check);
                        dialogCb();
                    }
                }
            }
        }
    }
    
    // ================================================================================
    // INTERFACE - onRefresh()
    // ================================================================================
    @Override
    public void onRefresh() {
        mPrinter.clear();
        mIsSearching = true;
        mPrinterManager.startPrinterSearch();
    }
    
    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================
    @Override
    public void onClick(View v) {
        //Back Button
        if(v.getId() == ID_MENU_ACTION_BUTTON) {
            if (isTablet()) {
                if (getActivity() != null && getActivity() instanceof MainActivity) {
                    MainActivity activity = (MainActivity) getActivity();              
                    activity.closeDrawers();        
                }
            } else {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack();
                    ft.commit();
                }
            }
        }
    }
    
    // ================================================================================
    // INTERFACE - OnPrinterSearch
    // ================================================================================
    @Override
    public void onPrinterAdd(final Printer printer) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try{
                    mPrinter.add(printer);
                    mPrinterSearchAdapter.notifyDataSetChanged();
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    @Override
    public void onSearchEnd() {
        mIsSearching = false;
        updateRefreshBar();
    }
}
