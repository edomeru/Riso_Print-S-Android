
package jp.co.riso.smartdeviceapp.view.base;

import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import android.app.DialogFragment;
import android.app.ActionBar.LayoutParams;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;

public abstract class BaseFragment extends DialogFragment implements View.OnLayoutChangeListener, View.OnClickListener {
    
    public final int ID_MENU_ACTION_BUTTON = 0x11000001;
    
    /** {@inheritDoc} */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        initializeFragment(savedInstanceState);
    }

    /** {@inheritDoc} */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (getDialog() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        View view = inflater.inflate(getViewLayout(), container, false);
        
        initializeView(view, savedInstanceState);
        
        if (view.findViewById(R.id.actionBarLayout) != null) {
            
            initializeCustomActionBar(view, savedInstanceState);
            
            // Add size change listener to prevent overlaps
            view.findViewById(R.id.actionBarLayout).addOnLayoutChangeListener(this);
        }

        // set width and height of dialog
        if (getDialog() != null){
            
            /*set height of item after title bar*/
            View mainView = view.findViewById(R.id.rootView);
            
            if (mainView != null && mainView.getLayoutParams() != null){
                
                int width = getResources().getDimensionPixelSize(R.dimen.dialog_width);
                int height = getResources().getDimensionPixelSize(R.dimen.dialog_height);
                
                mainView.getLayoutParams().width = width;
                mainView.getLayoutParams().height = height;
            }
        }
        
        return view;
    }
    
    // ================================================================================
    // Abstract Functions
    // ================================================================================
    
    public abstract int getViewLayout();
    
    public abstract void initializeFragment(Bundle savedInstanceState);
    
    public abstract void initializeView(View view, Bundle savedInstanceState);
    
    public abstract void initializeCustomActionBar(View view, Bundle savedInstanceState);

    // ================================================================================
    // Public Functions
    // ================================================================================
    
    public boolean isTablet() {
        if (getActivity() == null) {
            return false;
        }
        
        return getResources().getBoolean(R.bool.is_tablet);
    }
    
    public boolean isTabletLand() {
        if (getActivity() == null) {
            return false;
        }
        
        return getResources().getBoolean(R.bool.is_tablet_land);
    }
    
    public void addActionMenuButton(View v) {
        ImageButton actionMenuButton = new ImageButton(v.getContext());
        
        actionMenuButton.setId(ID_MENU_ACTION_BUTTON);
        actionMenuButton.setImageResource(R.drawable.ic_action_menu);
        actionMenuButton.setBackgroundResource(R.drawable.button_actionmenu_bg_selector);
        
        ViewGroup leftActionLayout = (ViewGroup) v.findViewById(R.id.leftActionLayout);
        
        leftActionLayout.addView(actionMenuButton, LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        
        actionMenuButton.setOnClickListener(this);
    }

    // ================================================================================
    // INTERFACE - View.OnLayoutChangeListener
    // ================================================================================

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (v.getId() == R.id.actionBarLayout) {
            int leftWidth = v.findViewById(R.id.leftActionLayout).getWidth();
            int rightWidth = v.findViewById(R.id.rightActionLayout).getWidth();
            
            v.findViewById(R.id.actionBarTitle).getLayoutParams().width = right - left - (Math.max(leftWidth, rightWidth) * 2);
        }
    }

    // ================================================================================
    // INTERFACE - View.OnLayoutChangeListener
    // ================================================================================

    /** {@inheritDoc} */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case ID_MENU_ACTION_BUTTON:
                if (getActivity() != null && getActivity() instanceof MainActivity) {
                    MainActivity activity = (MainActivity) getActivity();
                    activity.openDrawer(Gravity.LEFT);
                }
                break;
        }
    }
    
}
