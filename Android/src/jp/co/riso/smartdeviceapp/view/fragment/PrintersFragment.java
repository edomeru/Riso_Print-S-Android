/*
 * Copyright (c) 2014 All rights reserved.
 *
 * PrintersFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.fragment;

import java.util.ArrayList;

import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartdeviceapp.view.custom.PrinterArrayAdapter;
import jp.co.riso.smartdeviceapp.view.custom.PrintersScreenTabletView;
import android.app.ActionBar.LayoutParams;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class PrintersFragment extends BaseFragment implements View.OnTouchListener {
    public interface PrinteSearchTabletInterface {
        public void refreshPrintersList();
    }
    
    public static final String FRAGMENT_TAG_PRINTER_SEARCH = "fragment_printer_search";
    public static final String FRAGMENT_TAG_ADD_PRINTER = "fragment_add_printer";
    
    public final int ID_MENU_ACTION_SEARCH_BUTTON = 0x11000002;
    public final int ID_MENU_ACTION_ADD_BUTTON    = 0x11000003;
    
    //ListView parameters
    private ListView mListView = null;
    private ArrayList<Printer> mPrinter = null;
    private ArrayAdapter<Printer> mPrinterAdapter = null;
    
    //Tablet parameters
    PrintersScreenTabletView mPrinterTabletView = null;
    
    //Printer Manager
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
        if(!isTablet()) {
            mPrinter = new ArrayList<Printer>();
        }
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        if(isTablet()) {
            mPrinterTabletView = (PrintersScreenTabletView) view.findViewById(R.id.printerParentView);
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
        addActionMenuButton(view);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        if(isTablet()) {       
            mPrinterTabletView.refreshPrintersList();
        }
        else {
            setPrinterArrayList();
            mPrinterAdapter = new PrinterArrayAdapter(getActivity(),
                    R.layout.printer_list_item, mPrinter);
            mListView.setAdapter(mPrinterAdapter);
        }
    }    
    
    @Override
    public void addActionMenuButton(View v) {
        super.addActionMenuButton(v);
        int padding = getResources().getDimensionPixelSize(R.dimen.actionbar_button_padding);        

        ImageButton addMenuButton = new ImageButton(v.getContext());
        ImageButton searchMenuButton = new ImageButton(v.getContext());
        
        ViewGroup rightActionLayout = (ViewGroup) v.findViewById(R.id.rightActionLayout);
        
        //Manual Add Button
        addMenuButton.setId(ID_MENU_ACTION_ADD_BUTTON);
        addMenuButton.setImageResource(R.drawable.temp_img_btn_add_printer_normal);
        addMenuButton.setBackgroundResource(R.drawable.button_actionmenu_bg_selector);
        addMenuButton.setPadding(padding, padding, padding, padding);
     
        //Search Button
        searchMenuButton.setId(ID_MENU_ACTION_SEARCH_BUTTON);
        searchMenuButton.setImageResource(R.drawable.temp_img_btn_search_printer_normal);
        searchMenuButton.setBackgroundResource(R.drawable.button_actionmenu_bg_selector);
        searchMenuButton.setPadding(padding, padding, padding, padding);

        rightActionLayout.addView(addMenuButton, LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        rightActionLayout.addView(searchMenuButton, LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        
        searchMenuButton.setOnClickListener(this);
        addMenuButton.setOnClickListener(this);
    }
    
    // ================================================================================
    // Private Methods
    // ================================================================================   
    private void displayPrinterSearchFragment() {
        PrinterSearchFragment fragment = new PrinterSearchFragment();
        if(isTablet()) {
            fragment.setPrinteSearchTabletInterface(mPrinterTabletView);
        }
        switchToFragment(fragment, FRAGMENT_TAG_PRINTER_SEARCH);
    }
    
    private void displayAddPrinterFragment() {
        AddPrinterFragment fragment = new AddPrinterFragment();
        if(isTablet()) {
            fragment.setPrinteSearchTabletInterface(mPrinterTabletView);
        }
        switchToFragment(fragment, FRAGMENT_TAG_ADD_PRINTER);
    }
    
    private void switchToFragment(BaseFragment fragment, String tag) {
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
    // INTERFACE - View.OnClickListener
    // ================================================================================
    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case ID_MENU_ACTION_SEARCH_BUTTON:
                displayPrinterSearchFragment();
                onPause();
                break;
            case ID_MENU_ACTION_ADD_BUTTON:
                displayAddPrinterFragment();
                onPause();
                break;
            default:
                break;
        }
    }
    
    // ================================================================================
    // INTERFACE - View.onTouch
    // ================================================================================
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        PrinterArrayAdapter printerArrayAdapter = (PrinterArrayAdapter) mPrinterAdapter;
        printerArrayAdapter.hideDeleteButton();
        return false;
    }
    
    // ================================================================================
    // INTERFACE - OnRefreshPrintersList
    // ================================================================================
    public void onRefreshPrintersList() {
        mPrinterTabletView.refreshPrintersList();        
    }    
}
