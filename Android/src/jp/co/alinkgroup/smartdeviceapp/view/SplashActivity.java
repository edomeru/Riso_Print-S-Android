/*
 * Copyright (c) 2014 All rights reserved.
 *
 * SplashActivity.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.alinkgroup.smartdeviceapp.view;

import jp.co.alinkgroup.android.os.pauseablehandler.PauseableHandler;
import jp.co.alinkgroup.android.os.pauseablehandler.PauseableHandlerCallback;
import jp.co.alinkgroup.smartdeviceapp.AppConstants;
import jp.co.alinkgroup.smartdeviceapp.R;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Message;

public class SplashActivity extends Activity implements PauseableHandlerCallback {
    public static final int MESSAGE_RUN_MAINACTIVITY = 0x10001;
    
    public PauseableHandler mHandler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mHandler = new PauseableHandler(this);
        
        if (AppConstants.APP_SHOW_SPLASH || isTaskRoot()) {
            setContentView(R.layout.activity_splash);
            
            mHandler.sendEmptyMessageDelayed(MESSAGE_RUN_MAINACTIVITY, AppConstants.APP_SPLASH_DURATION);
        } else {
            runMainActivity();
        }
        
        // Disable orientation 
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        if (!isTablet) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
    
    // Public Methods
    
    // Private Methods
    
    private void runMainActivity() {
        /*
        Intent newIntent = AppUtils.createActivityIntent(this, MainActivity.class);
        
        if (newIntent == null) {
            throw new NullPointerException("Cannot create intent");
        } else {
            try {
                if (getIntent() != null) {
                    String action = getIntent().getAction();
                    if (Intent.ACTION_VIEW.equals(action)) {
                        newIntent.setData(getIntent().getData());
                    }
                }
                
                newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(newIntent);
            } catch (ActivityNotFoundException e) {
                throw e;
            } catch (AndroidRuntimeException e) {
                throw e;
            }
        }
        */
        
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
