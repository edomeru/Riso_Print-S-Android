package jp.co.riso.smartdeviceapp.view.base;

import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.view.webkit.SDAWebView;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

public abstract class BaseWebFragment extends BaseFragment {
    public static final String TAG = "HelpFragment";
    
    protected SDAWebView mWebView;
    
    /** {@inheritDoc} */
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
    }

    /** {@inheritDoc} */
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        mWebView = (SDAWebView) view.findViewById(R.id.contentWebView);
        
        configureWebView(mWebView);
        
        // Bug on ICS rotate when using anchor links href="#.."
        boolean isICS = (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1);
        if (!isICS && savedInstanceState != null) {
            mWebView.restoreState(savedInstanceState);
        } else {
            mWebView.loadUrl(getUrlString());
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        mWebView.saveState(outState);
    }
    
    /**
     * Performs any additional configuration to the webview before URL Loading
     * 
     * @param webView
     *            WebView to be configured
     */
    public abstract void configureWebView(WebView webView);
    
    /**
     * Gets the URL to be loaded in the web view
     * 
     * @return URL String
     */
    public abstract String getUrlString();
    
    
}
