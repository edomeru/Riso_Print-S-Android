/*
 * Copyright (c) 2014 All rights reserved.
 *
 * SplashActivity.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view;

import jp.co.riso.android.os.pauseablehandler.PauseableHandler;
import jp.co.riso.android.os.pauseablehandler.PauseableHandlerCallback;
import jp.co.riso.android.util.AppUtils;
import jp.co.riso.android.util.Logger;
import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager;
import jp.co.riso.smartdeviceapp.view.base.BaseActivity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.AndroidRuntimeException;

public class SplashActivity extends BaseActivity implements PauseableHandlerCallback {
    // Messages
    public static final int MESSAGE_RUN_MAINACTIVITY = 0x10001;
    
    public PauseableHandler mHandler;
    
    @Override
    protected void onCreateContent(Bundle savedInstanceState) {
        mHandler = new PauseableHandler(this);
        
        if (isTaskRoot()) {
            setContentView(R.layout.activity_splash);
            
            mHandler.sendEmptyMessageDelayed(MESSAGE_RUN_MAINACTIVITY, AppConstants.APP_SPLASH_DURATION);
        } else {
            if (getIntent() != null) {
                String action = getIntent().getAction();
                
                if (Intent.ACTION_VIEW.equals(action)) {
                    runMainActivity();
                    return;
                }
            }
            
            // Do nothing
            finish();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        mHandler.removeCallbacksAndMessages(null);
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
    
    // ================================================================================
    // Public Functions
    // ================================================================================
    
    // ================================================================================
    // Private Functions
    // ================================================================================
    
    private void runMainActivity() {
        Intent launchIntent = AppUtils.createActivityIntent(this, MainActivity.class);
        
        if (launchIntent == null) {
            String err = "Cannot create Intent";
            Logger.logError(this, err);
            throw new NullPointerException(err);
        }
        
        Uri data = null;
        if (getIntent() != null) {
            String action = getIntent().getAction();
            
            if (Intent.ACTION_VIEW.equals(action)) {
                data = getIntent().getData();
            }
        }
        
        // Notify PDF File Data that there is a new PDF
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext());
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(PDFFileManager.KEY_NEW_PDF_DATA, false);
        
        if (data != null) {
            edit.putBoolean(PDFFileManager.KEY_NEW_PDF_DATA, true);
            launchIntent.setData(data);
        }
        
        edit.commit();

        int flags = Intent.FLAG_ACTIVITY_CLEAR_TOP;
        
        if (isTaskRoot()) {
            flags |= Intent.FLAG_ACTIVITY_NO_ANIMATION;
        }
        
        launchIntent.setFlags(flags);
        
        try {
            startActivity(launchIntent);
        } catch (ActivityNotFoundException e) {
            String err = "Fatal Error: Intent MainActivity Not Found is not defined";
            Logger.logError(this, err);
            throw e;
        } catch (AndroidRuntimeException e) {
            String err = "Fatal Error: Android runtime";
            Logger.logError(this, err);
            throw e;
        }
        
        finish();
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
            runMainActivity();
        }
    }
}
