package jp.co.riso.smartdeviceapp.view.printers;

import jp.co.riso.smartprint.R;
import android.app.Activity;
import android.content.Context;
import androidx.core.content.ContextCompat;
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

    private final Activity activity;
    
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
        return !this.isNoDisabled;
    }

    @Override
    public boolean isEnabled(int position) {
        return ((position != 1) || !this.isNoDisabled);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView)super.getDropDownView(position, convertView, parent);
        
        if(isNoDisabled && position == 1)//No
        {
            view.setBackgroundColor(ContextCompat.getColor(activity, R.color.theme_light_3));
            view.setTextColor(ContextCompat.getColor(activity, R.color.theme_light_4));
        }
        else
        {
            view.setBackgroundResource(R.drawable.selector_printerinfo_port);
            view.setTextColor(ContextCompat.getColorStateList(activity, R.color.selector_printers_text));
        }
        
        return view;
    }
}
