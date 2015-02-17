package jp.co.riso.smartdeviceapp.view;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.opengl.Visibility;
import android.os.Bundle;
import android.util.AndroidRuntimeException;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import jp.co.riso.android.util.AppUtils;
import jp.co.riso.android.util.Logger;
import jp.co.riso.smartdeviceapp.view.base.BaseActivity;
import jp.co.riso.smartdeviceapp.view.fragment.HelpFragment;
import jp.co.riso.smartdeviceapp.view.webkit.SDAWebView;
import jp.co.riso.smartprint.R;

public class LicenseActivity extends BaseActivity implements OnTouchListener {
    
    private SDAWebView mWebView = null;
    
    @Override
    protected void onCreateContent(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_helplegal);
        
        TextView textView = (TextView) this.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_license);
        textView.setPadding(18, 0, 0, 0);
        
        mWebView = (SDAWebView) this.findViewById(R.id.contentWebView);              
        
        final Context context = this;
        mWebView.setWebViewClient(new WebViewClient() {
            
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                
                Logger.logStartTime(context, LicenseActivity.class, "License Activity load");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Logger.logStopTime(context, LicenseActivity.class, "License Activity load");
            }
        });
        
        mWebView.loadUrl(getUrlString());
        
        LinearLayout buttonLayout = (LinearLayout)this.findViewById(R.id.LicenseButtonLayout);
        buttonLayout.setVisibility(View.VISIBLE);
        
        Button agreebutton = (Button)buttonLayout.findViewById(R.id.licenseAgreeButton);
        agreebutton.setText(R.string.ids_lbl_agree);
        agreebutton.setOnTouchListener(this);
        
        Button disagreebutton = (Button)buttonLayout.findViewById(R.id.licenseDisagreeButton);
        disagreebutton.setText(R.string.ids_lbl_disagree);
        disagreebutton.setOnTouchListener(this);
    }
    
    public String getUrlString() {
        String htmlFolder = getString(R.string.html_folder);
        String helpHtml = getString(R.string.license_html);
        return AppUtils.getLocalizedAssetFullPath(this, htmlFolder, helpHtml);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_UP){
            if (v.getId() == R.id.licenseAgreeButton){
             
                // save to shared preferences
                SharedPreferences preferences = getSharedPreferences("licenseAgreementPrefs",MODE_PRIVATE);
                
                SharedPreferences.Editor edit = preferences.edit();
                edit.putBoolean("licenseAgreementDone",true);
                edit.apply();
                
                Intent startIntent = this.getIntent();
                Intent launchIntent = new Intent(startIntent);
                launchIntent.setClass(this, MainActivity.class);    
                
                try {
                    startActivity(launchIntent);
                } catch (ActivityNotFoundException e) {
                    Logger.logError(LicenseActivity.class, "Fatal Error: Intent MainActivity Not Found is not defined");
                    throw e;
                } catch (AndroidRuntimeException e) {
                    Logger.logError(LicenseActivity.class, "Fatal Error: Android runtime");
                    throw e;
                }
                
                finish();
                
                return true;
                
            } else if (v.getId() == R.id.licenseDisagreeButton){
                
                // alert box
                createDialog();
                
                return true;
            } else {
                v.performClick();
            }
        }
        
        return false;
    }    
    
    private void createDialog(){
        String title = getString(R.string.ids_lbl_license);
        String message = getString(R.string.ids_err_msg_disagree_to_license);
        String buttonTitle = getString(R.string.ids_lbl_ok);

        ContextThemeWrapper newContext = new ContextThemeWrapper(this, android.R.style.TextAppearance_Holo_DialogWindowTitle);
        AlertDialog.Builder builder = new AlertDialog.Builder(newContext);
        
        if (title != null) {
            builder.setTitle(title);
        }
        
        if (message != null) {
            builder.setMessage(message);
        }
        
        if (buttonTitle != null) {
            builder.setNegativeButton(buttonTitle, null);
        }
        
        AlertDialog dialog = null;
        dialog = builder.create();
        
        dialog.show();
    }
}
