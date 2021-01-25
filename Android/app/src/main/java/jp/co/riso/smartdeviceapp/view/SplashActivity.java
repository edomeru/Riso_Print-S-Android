/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * SplashActivity.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
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
import android.widget.ViewFlipper;

import java.io.File;
import java.io.IOException;

import jp.co.riso.android.os.pauseablehandler.PauseableHandler;
import jp.co.riso.android.os.pauseablehandler.PauseableHandlerCallback;
import jp.co.riso.android.util.AppUtils;
import jp.co.riso.android.util.FileUtils;
import jp.co.riso.android.util.Logger;
import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.common.BaseTask;
import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager;
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager;
import jp.co.riso.smartdeviceapp.view.base.BaseActivity;
import jp.co.riso.smartdeviceapp.view.webkit.SDAWebView;
import jp.co.riso.smartprint.R;

/**
 * @class SplashActivity
 * 
 * @brief Splash activity class.
 */
public class SplashActivity extends BaseActivity implements PauseableHandlerCallback, OnTouchListener {
    
    /// Message ID for running main activity
    public static final int MESSAGE_RUN_MAINACTIVITY = 0x10001;
    
    public static final String KEY_DB_INITIALIZED = "database_initialized";

    private static final int DUMMY_REQUEST_CODE = 0;

    private PauseableHandler mHandler = null;
    private DBInitTask mInitTask = null;
    private boolean mDatabaseInitialized;
    private SDAWebView mWebView = null;
    
    @SuppressWarnings("unused") // AppConstant.APP_SHOW_SPLASH is a config setting
    @Override
    protected void onCreateContent(Bundle savedInstanceState) {

        if (mHandler == null) {
            mHandler = new PauseableHandler(Looper.myLooper(), this);
        }
        
        mDatabaseInitialized = false;
        if (savedInstanceState != null) {
            mDatabaseInitialized = savedInstanceState.getBoolean(KEY_DB_INITIALIZED, mDatabaseInitialized);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SplashActivity.this);
        boolean dbIsOK = prefs.contains(AppConstants.PREF_KEY_DB_VERSION);

        if (!mDatabaseInitialized) {
            if (!dbIsOK) {
                if (mInitTask == null) {
                    mInitTask = new DBInitTask();
                    mInitTask.execute();
                }
            } else {
                mDatabaseInitialized = true;
            }
        }

        setContentView(R.layout.activity_splash);

        if (!mHandler.hasMessages(MESSAGE_RUN_MAINACTIVITY)) {
            if (!AppConstants.APP_SHOW_SPLASH && dbIsOK) {
                runMainActivity();
            } else {
                mHandler.sendEmptyMessageDelayed(MESSAGE_RUN_MAINACTIVITY, AppConstants.APP_SPLASH_DURATION);
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        mHandler.resume();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        mHandler.pause();
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        
        // On a new intent, set the current intent so that URL Scheme works during Splash Screen display
        setIntent(intent);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putBoolean(KEY_DB_INITIALIZED, mDatabaseInitialized);
    }
    
    
    // ================================================================================
    // Public Functions
    // ================================================================================
    
    // ================================================================================
    // Private Functions
    // ================================================================================

    /**
     * @brief Run the main activity.
     */
    private void runMainActivity() {
        Intent launchIntent;

        SharedPreferences preferences = getSharedPreferences("licenseAgreementPrefs",MODE_PRIVATE);
        if (preferences.getBoolean("licenseAgreementDone",false)){
            //if user has already agreed to the license agreement
            launchIntent = AppUtils.createActivityIntent(this, MainActivity.class);
        } else {
            //if user has not yet agreed to the license agreement
            TextView textView = (TextView) this.findViewById(R.id.actionBarTitle);
            textView.setText(R.string.ids_lbl_license);
            textView.setPadding(18, 0, 0, 0);
            
            mWebView = (SDAWebView) this.findViewById(R.id.contentWebView);              
            
            final Context context = this;
            mWebView.setWebViewClient(new WebViewClient() {
                
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    
                    Logger.logStartTime(context, SplashActivity.class, "License Activity load");
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    Logger.logStopTime(context, SplashActivity.class, "License Activity load");
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


            ViewFlipper vf = (ViewFlipper) findViewById( R.id.viewFlipper);
            vf.showNext();
            
            return;
        }
        
        //reset secure print values
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext());
        
        Editor editor = prefs.edit();
        editor.putBoolean(AppConstants.PREF_KEY_AUTH_SECURE_PRINT, AppConstants.PREF_DEFAULT_AUTH_SECURE_PRINT);
        editor.putString(AppConstants.PREF_KEY_AUTH_PIN_CODE, AppConstants.PREF_DEFAULT_AUTH_PIN_CODE);
        editor.commit();
        
        
        if (launchIntent == null) {
            Logger.logError(SplashActivity.class, "Cannot create Intent");
            throw new NullPointerException("Cannot create Intent");
        }
        
        Uri data = null;
        ClipData clipData = null;
        Intent intent = getIntent();
        if (getIntent() != null) {
            String action = getIntent().getAction();
            
            if (Intent.ACTION_VIEW.equals(action)) {
                data = getIntent().getData();
            } else if (Intent.ACTION_SEND.equals(action)) {
                if (getIntent().getExtras().get(Intent.EXTRA_STREAM) != null) {
                    data = Uri.parse(getIntent().getExtras().get(Intent.EXTRA_STREAM).toString());
                }
            } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                clipData = getIntent().getClipData();
            }
        }

        // Notify PDF File Data that there is a new PDF
        PDFFileManager.clearSandboxPDFName(SmartDeviceApp.getAppContext());
        PDFFileManager.setHasNewPDFData(SmartDeviceApp.getAppContext(), data != null || clipData != null);
        
        if (data != null) {
            launchIntent.setData(data);
        } else if (clipData != null) {
            launchIntent.setClipData(clipData);
            launchIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        } else {
            // delete PDF cache
            File file = new File(PDFFileManager.getSandboxPath());
            try {
                FileUtils.delete(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        int flags = Intent.FLAG_ACTIVITY_CLEAR_TOP;
        //workaround for android 5.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            flags |= Intent.FLAG_ACTIVITY_NEW_TASK;
        }
        
        if (isTaskRoot()) {
            flags |= Intent.FLAG_ACTIVITY_NO_ANIMATION;
        }

        // https://stackoverflow.com/questions/41743978/permission-denial-media-documents-provider
        // Propagate permission to MainActivity so that when data/clipData gets Allowed from
        // SplashActivity (when user selects file from Open In), URI can be accessed
        flags |= Intent.FLAG_GRANT_READ_URI_PERMISSION;
        
        launchIntent.setFlags(flags);
        
        try {
            startActivity(launchIntent);
        } catch (ActivityNotFoundException e) {
            Logger.logError(SplashActivity.class, "Fatal Error: Intent MainActivity Not Found is not defined");
            throw e;
        } catch (AndroidRuntimeException e) {
            Logger.logError(SplashActivity.class, "Fatal Error: Android runtime");
            throw e;
        }
        
        finish();
    }
    
    private String getUrlString() {
        String htmlFolder = getString(R.string.html_folder);
        String helpHtml = getString(R.string.license_html);
        return AppUtils.getLocalizedAssetFullPath(this, htmlFolder, helpHtml);
    }
    
    // ================================================================================
    // INTERFACE - PauseableHandlerCallback
    // ================================================================================
    
    @Override
    public boolean storeMessage(Message message) {
        return message.what == MESSAGE_RUN_MAINACTIVITY;
    }
    
    @Override
    public void processMessage(Message message) {
        if (message.what == MESSAGE_RUN_MAINACTIVITY) {
            if (mDatabaseInitialized) {
                runMainActivity();
            }
        }
    }

    
    // ================================================================================
    // Internal Classes
    // ================================================================================
    
    /**
     * @class DBInitTask
     * 
     * @brief Async task for initializing database.
     */
    private class DBInitTask extends BaseTask<Void, Void> {
        
        @Override
        protected Void doInBackground(Void... params) {
            DatabaseManager manager = new DatabaseManager(SplashActivity.this);
            manager.getWritableDatabase();
            manager.close();
            
            saveToPrefs();
            
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            
            if (!SplashActivity.this.isFinishing()) {
                if (mHandler.hasStoredMessage(MESSAGE_RUN_MAINACTIVITY)) {
                    mDatabaseInitialized = true;
                } else {
                    runMainActivity();
                }
            }
        }
        
        /**
         * @brief Save database version to shared preference.
         */
        private void saveToPrefs() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SplashActivity.this);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putInt(AppConstants.PREF_KEY_DB_VERSION, DatabaseManager.DATABASE_VERSION);
            editor.apply();
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP){
            switch (v.getId()) {
                case R.id.licenseAgreeButton:
                    // save to shared preferences
                    SharedPreferences preferences = getSharedPreferences("licenseAgreementPrefs", MODE_PRIVATE);

                    SharedPreferences.Editor edit = preferences.edit();
                    edit.putBoolean("licenseAgreementDone", true);
                    //edit.putBoolean("licenseAgreementDone",false);
                    edit.apply();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        // show permission onboarding screens
                        findViewById(R.id.settingsButton).setOnTouchListener(this);
                        findViewById(R.id.startButton).setOnTouchListener(this);

                        TextView infoText = (TextView) findViewById(R.id.txtPermissionInfo);
                        infoText.setText(getString(R.string.ids_lbl_permission_information, getString(R.string.ids_app_name)));
                        ((ViewFlipper) findViewById(R.id.viewFlipper)).showNext();
                    } else {
                        // start home screen
                        runMainActivity();
                    }
                    return true;

                case R.id.licenseDisagreeButton:

                    // alert box
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

                    return true;
                case R.id.startButton:
                    // start Home Screen
                    runMainActivity();
                    return true;
                case R.id.settingsButton:
                    // Go to Settings screen screen
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, DUMMY_REQUEST_CODE);
                    return true;
                default:
                    v.performClick();
            }
        }
        
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        runMainActivity();
    }
}
