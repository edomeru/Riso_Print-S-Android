/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * SplashActivity.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view;

import jp.co.riso.android.os.pauseablehandler.PauseableHandler;
import jp.co.riso.android.os.pauseablehandler.PauseableHandlerCallback;
import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager;
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.view.base.BaseActivity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.AndroidRuntimeException;
import android.util.Log;

public class SplashActivity extends BaseActivity implements PauseableHandlerCallback {
    public static final String TAG = "SplashActivity";
    
    // Messages
    public static final int MESSAGE_RUN_MAINACTIVITY = 0x10001;
    
    public static final String KEY_DB_INITIALIZED = "database_initialized";

    private PauseableHandler mHandler = null;
    private DBInitTask mInitTask = null;
    private boolean mDatabaseInitialized;
    
    /** {@inheritDoc} */
    @SuppressWarnings("unused") // AppConstant.APP_SHOW_SPLASH is a config setting
    @Override
    protected void onCreateContent(Bundle savedInstanceState) {

        if (mHandler == null) {
            mHandler = new PauseableHandler(this);
        }
        
        mDatabaseInitialized = false;
        if (savedInstanceState != null) {
            mDatabaseInitialized = savedInstanceState.getBoolean(KEY_DB_INITIALIZED, mDatabaseInitialized);
        }
        
        if (isTaskRoot()) {
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
        } else {
            mDatabaseInitialized = true; //initialized if splash activity is not task root
            
            if (getIntent() != null) {
                String action = getIntent().getAction();
                
                if (Intent.ACTION_VIEW.equals(action) ||
                        Intent.ACTION_SEND.equals(action)) {
                    runMainActivity();
                    return;
                }
            }
            
            // Do nothing if no action
            finish();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    
    /** {@inheritDoc} */
    @Override
    protected void onResume() {
        super.onResume();
        
        mHandler.resume();
    }
    
    /** {@inheritDoc} */
    @Override
    protected void onPause() {
        super.onPause();
        
        mHandler.pause();
    }
    
    /** {@inheritDoc} */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        
        // On a new intent, set the current intent so that URL Scheme works during Splash Screen display
        setIntent(intent);
    }
    
    /** {@inheritDoc} */
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

    /*
     * Run MainActivity
     */
    private void runMainActivity() {
        Intent launchIntent = AppUtils.createActivityIntent(this, MainActivity.class);
        
        if (launchIntent == null) {
            Log.e(TAG, "Cannot create Intent");
            throw new NullPointerException("Cannot create Intent");
        }
        
        Uri data = null;
        if (getIntent() != null) {
            String action = getIntent().getAction();
            
            if (Intent.ACTION_VIEW.equals(action)) {
                data = getIntent().getData();
            } else if (Intent.ACTION_SEND.equals(action)) {
                data = Uri.parse(getIntent().getExtras().get(Intent.EXTRA_STREAM).toString());
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
            Log.e(TAG, "Fatal Error: Intent MainActivity Not Found is not defined");
            throw e;
        } catch (AndroidRuntimeException e) {
            Log.e(TAG, "Fatal Error: Android runtime");
            throw e;
        }
        
        finish();
    }
    
    // ================================================================================
    // INTERFACE - PauseableHandlerCallback
    // ================================================================================
    
    /** {@inheritDoc} */
    @Override
    public boolean storeMessage(Message message) {
        return message.what == MESSAGE_RUN_MAINACTIVITY;
    }
    
    /** {@inheritDoc} */
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
    
    private class DBInitTask extends AsyncTask<Void, Void, Void> {
        
        /** {@inheritDoc} */
        @Override
        protected Void doInBackground(Void... params) {
            DatabaseManager manager = new DatabaseManager(SplashActivity.this);
            manager.getWritableDatabase();
            manager.close();
            
            saveToPrefs();
            
            return null;
        }

        /** {@inheritDoc} */
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
         * Save database version to shared preference
         */
        private void saveToPrefs() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SplashActivity.this);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putInt(AppConstants.PREF_KEY_DB_VERSION, DatabaseManager.DATABASE_VERSION);
            editor.apply();
        }
    }
}
