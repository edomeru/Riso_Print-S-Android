
package jp.co.alinkgroup.smartdeviceapp.view.base;

import jp.co.alinkgroup.smartdeviceapp.R;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

public abstract class BaseFragment extends DialogFragment {
    
    /** {@inheritDoc} */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

            view.findViewById(R.id.actionBarLayout).addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    int leftWidth = v.findViewById(R.id.leftActionLayout).getWidth();
                    int rightWidth = v.findViewById(R.id.rightActionLayout).getWidth();
                    
                    v.findViewById(R.id.actionBarTitle).getLayoutParams().width = right - left - (Math.max(leftWidth, rightWidth) * 2);
                }
            });
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
    
    public abstract void initializeView(View view, Bundle savedInstanceState);

    // ================================================================================
    // Public Functions
    // ================================================================================
    
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        
    }
    
    public boolean isTablet() {
        if (getActivity() == null) {
            return false;
        }
        
        return getResources().getBoolean(R.bool.is_tablet);
    }
    
}
