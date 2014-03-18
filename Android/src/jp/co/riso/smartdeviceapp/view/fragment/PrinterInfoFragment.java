
package jp.co.riso.smartdeviceapp.view.fragment;

import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import android.app.ActionBar.LayoutParams;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class PrinterInfoFragment extends BaseFragment{
    
    public static final String KEY_PRINTER_INFO = "fragment_printer_info";
    public final int ID_MENU_ACTION_PRINT_SETTINGS_BUTTON    = 0x11000004;
    
    Printer mPrinter = null; 
    TextView mPrinterName = null;
    TextView mIpAddress = null;
    
    @Override
    public int getViewLayout() {
        return R.layout.fragment_printer_info;
    }
    
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        view.setBackgroundColor(getResources().getColor(R.color.theme_light_2));
        mPrinterName = (TextView) view.findViewById(R.id.inputPrinterName);
        mIpAddress = (TextView) view.findViewById(R.id.inputIpAddress);
    }
    
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {      
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_printer_info);
        
        addActionMenuButton(view);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        if(savedInstanceState != null) {
            
        }
        Bundle extras = getArguments();
        if (extras == null) {
            extras = getActivity().getIntent().getExtras();
        }
        mPrinter = extras.getParcelable(KEY_PRINTER_INFO);      
        
        mPrinterName.setText(mPrinter.getName());
        mIpAddress.setText(mPrinter.getIpAddress());
        
    }
    
    @Override
    public void addActionMenuButton(View v) {
        ImageButton defaultPrintSettingsMenuButton = new ImageButton(v.getContext());
        ImageButton actionMenuButton = new ImageButton(v.getContext());
        int padding = getResources().getDimensionPixelSize(R.dimen.actionbar_button_padding);        
        
        ViewGroup rightActionLayout = (ViewGroup) v.findViewById(R.id.rightActionLayout);
        ViewGroup leftActionLayout = (ViewGroup) v.findViewById(R.id.leftActionLayout);
        
        //Back Menu Button
        actionMenuButton.setId(ID_MENU_ACTION_BUTTON);
        actionMenuButton.setImageResource(R.drawable.img_btn_back_normal);
        actionMenuButton.setBackgroundResource(R.drawable.button_actionmenu_bg_selector);
        actionMenuButton.setPadding(padding, padding, padding, padding);
        
        //Default Printer Settings Menu Button
        defaultPrintSettingsMenuButton.setId(ID_MENU_ACTION_PRINT_SETTINGS_BUTTON);
        defaultPrintSettingsMenuButton.setImageResource(R.drawable.img_btn_default_print_settings);
        defaultPrintSettingsMenuButton.setBackgroundResource(R.drawable.button_actionmenu_bg_selector); 
        defaultPrintSettingsMenuButton.setPadding(padding, padding, padding, padding);

        rightActionLayout.addView(defaultPrintSettingsMenuButton, LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        leftActionLayout.addView(actionMenuButton, LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        
        actionMenuButton.setOnClickListener(this);
        defaultPrintSettingsMenuButton.setOnClickListener(this);
        
    }
    
    // ================================================================================
    // Private Methods
    // ================================================================================
    
    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case ID_MENU_ACTION_PRINT_SETTINGS_BUTTON:
                break;
            default:  
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();                
                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack();
                    ft.commit();
                }
                break;
        }
    }
     
}