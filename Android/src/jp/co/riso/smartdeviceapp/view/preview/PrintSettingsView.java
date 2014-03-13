package jp.co.riso.smartdeviceapp.view.preview;

import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.model.PrintSettings;
import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PrintSettingsView extends LinearLayout {
    
    public static final int SETTING_TYPE_HEADER = 1;
    public static final int SETTING_TYPE_NORMAL = 2;
    
    PrintSettings mPrintSettings;
    
    LinearLayout mPrintControls;
    LinearLayout mBasicSettingsControls;

    public PrintSettingsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public PrintSettingsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PrintSettingsView(Context context) {
        super(context);
        init();
    }
    
    public void setPrintSettings(PrintSettings printSettings) {
        mPrintSettings = printSettings;
    }

    // ================================================================================
    // View functions
    // ================================================================================
    
    private void init() {
        if (isInEditMode()) {
            return;
        }
        
        addPrintControls();
        addBasicSettingsControls();
    }
    
    private void addPrintControls() {
        mPrintControls = new LinearLayout(getContext());
        mPrintControls.setOrientation(LinearLayout.VERTICAL);
        
        LinearLayout header = createContainer(SETTING_TYPE_HEADER);
        header.addView(createTextView(SETTING_TYPE_HEADER, "Print"));
        mPrintControls.addView(header);
        
        LinearLayout selectedPrinter = createContainer(SETTING_TYPE_NORMAL);
        selectedPrinter.addView(createTextView(SETTING_TYPE_NORMAL, R.string.ids_lbl_select_printer));
        mPrintControls.addView(selectedPrinter);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.0f);
        addView(mPrintControls, params);
    }
    
    private void addBasicSettingsControls() {
        mBasicSettingsControls = new LinearLayout(getContext());
        mBasicSettingsControls.setOrientation(LinearLayout.VERTICAL);
        
        LinearLayout header = createContainer(SETTING_TYPE_HEADER);
        header.addView(createTextView(SETTING_TYPE_HEADER, R.string.ids_lbl_basic));
        mBasicSettingsControls.addView(header);
        
        int items[] = {
                R.string.ids_lbl_colormode,
                R.string.ids_lbl_orientation,
                R.string.ids_lbl_number_of_copies,
                R.string.ids_lbl_duplex,
                R.string.ids_lbl_papersize,
                R.string.ids_lbl_scaletofit,
                R.string.ids_lbl_papertype,
                R.string.ids_lbl_inputtray
        };
        for (int i = 0; i < items.length; i++) {
            LinearLayout layout = createContainer(SETTING_TYPE_NORMAL);
            layout.addView(createTextView(SETTING_TYPE_NORMAL, items[i]));
            mBasicSettingsControls.addView(layout);
            
            if (i != items.length - 1) {

                int height = getResources().getDimensionPixelSize(R.dimen.separator_size);
                
                MarginLayoutParams params = new MarginLayoutParams(LayoutParams.MATCH_PARENT, height);
                int leftPadding = getResources().getDimensionPixelSize(R.dimen.home_title_padding);
                params.setMargins(leftPadding, 0, 0, 0);
                mBasicSettingsControls.addView(createSeparatorView(), params);
            }
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.0f);
        addView(mBasicSettingsControls, params);
    }

    // ================================================================================
    // Convenient functions
    // ================================================================================
    
    private LinearLayout createContainer(int type) {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        
        int leftPadding = getResources().getDimensionPixelSize(R.dimen.home_title_padding);
        layout.setPadding(leftPadding, 0, 0, 0);

        int menuHeight = getResources().getDimensionPixelSize(R.dimen.home_menu_height);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, menuHeight);
        layout.setLayoutParams(params);
        
        switch (type) {
            case SETTING_TYPE_HEADER:
                layout.setBackgroundResource(R.drawable.selector_printsettings_header);
                break;
            case SETTING_TYPE_NORMAL:
                layout.setBackgroundResource(R.drawable.selector_printsettings_normal);
                break;
        }
        
        return layout;
    }
    
    private TextView createTextView(int type, int resId) {
        return createTextView(type, getResources().getString(resId));
    }
    
    private TextView createTextView(int type, String text) {
        TextView textView = new TextView(getContext());
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setText(text);
        
        int textSize = getResources().getDimensionPixelSize(R.dimen.home_text_size);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0.0f);
        textView.setLayoutParams(params);
        
        switch (type) {
            case SETTING_TYPE_HEADER:
                textView.setAllCaps(true);
                AppUtils.setTextViewTextColorState(textView, getResources(), R.color.selector_printsettings_header_text);
                break;
            case SETTING_TYPE_NORMAL:
                AppUtils.setTextViewTextColorState(textView, getResources(), R.color.selector_printsettings_normal_text);
                break;
        }
        
        return textView;
    }
    
    private View createSeparatorView() {
        View view = new View(getContext());
        view.setBackgroundResource(R.drawable.selector_printsettings_separator);

        int height = getResources().getDimensionPixelSize(R.dimen.separator_size);
        
        MarginLayoutParams params = new MarginLayoutParams(LayoutParams.MATCH_PARENT, height);
        int leftPadding = getResources().getDimensionPixelSize(R.dimen.home_title_padding);
        params.setMargins(leftPadding, 0, 0, 0);
        view.setLayoutParams(params);
        
        return view;
    }
    
}
