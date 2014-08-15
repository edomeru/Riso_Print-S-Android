package jp.co.riso.smartdeviceapp.view.printers;

import jp.co.riso.smartprint.R;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * @class DefaultPrinterArrayAdapter
 * 
 * @brief Array Adapter for spinner for selecting a printer as default.
 */
public class DefaultPrinterArrayAdapter extends ArrayAdapter<String> {

    private Activity activity;
    
    public boolean isNoDisabled;
    
    /**
     * @brief Constructor.
     * 
     * @param context Application context
     * @param resource Resource ID to be used as a printer row
     */
    public DefaultPrinterArrayAdapter(Context context, int resource) {
        super(context, resource);
        this.activity = (Activity)context;
        this.isNoDisabled = false;
    }

    @Override
    public boolean areAllItemsEnabled() {
        if(this.isNoDisabled)
            return false;
        
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        if(position == 1 && this.isNoDisabled)
            return false;
        
        return true;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = (TextView)super.getDropDownView(position, convertView, parent);
        
        if(isNoDisabled && position == 1)//No
        {
            view.setBackgroundColor(activity.getResources().getColor(R.color.theme_light_3));
            ((TextView)view).setTextColor(activity.getResources().getColor(R.color.theme_light_4));
        }
        else
        {
            view.setBackgroundResource(R.drawable.selector_printerinfo_port);
            ((TextView)view).setTextColor(activity.getResources().getColorStateList(R.color.selector_printers_text));
        }
        
        return view;
    }
}
