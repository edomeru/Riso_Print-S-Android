package jp.co.riso.smartdeviceapp.view.printers;

import android.content.Context;
import android.widget.ArrayAdapter;

/**
 * @class DefaultPrinterArrayAdapter
 * 
 * @brief Array Adapter for spinner for selecting a printer as default.
 */
public class DefaultPrinterArrayAdapter extends ArrayAdapter<String> {

    
    public boolean isNoDisabled;
    
    /**
     * @brief Constructor.
     * 
     * @param context Application context
     * @param resource Resource ID to be used as a printer row
     */
    public DefaultPrinterArrayAdapter(Context context, int resource) {
        super(context, resource);
        
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
}
