/*
 * Copyright (c) 2014 All rights reserved.
 *
 * SplashActivity.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.alinkgroup.smartdeviceapp.view;

import jp.co.alinkgroup.android.log.Logger;
import jp.co.alinkgroup.android.os.pauseablehandler.PauseableHandler;
import jp.co.alinkgroup.android.os.pauseablehandler.PauseableHandlerCallback;
import jp.co.alinkgroup.android.util.AppUtils;
import jp.co.alinkgroup.smartdeviceapp.AppConstants;
import jp.co.alinkgroup.smartdeviceapp.R;
import jp.co.alinkgroup.smartdeviceapp.view.base.BaseActivity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
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
        
        if (data != null) {
            launchIntent.setData(data);
        }

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
