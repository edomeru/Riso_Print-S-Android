/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * MainActivity.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.radaee.pdf.Global;

import java.io.File;
import java.io.IOException;

import jp.co.riso.android.os.pauseablehandler.PauseableHandler;
import jp.co.riso.android.os.pauseablehandler.PauseableHandlerCallback;
import jp.co.riso.android.util.FileUtils;
import jp.co.riso.android.util.Logger;
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager;
import jp.co.riso.smartdeviceapp.view.base.BaseActivity;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartdeviceapp.view.fragment.HelpFragment;
import jp.co.riso.smartdeviceapp.view.fragment.HomeFragment;
import jp.co.riso.smartdeviceapp.view.fragment.LegalFragment;
import jp.co.riso.smartdeviceapp.view.fragment.PrintPreviewFragment;
import jp.co.riso.smartdeviceapp.view.widget.SDADrawerLayout;
import jp.co.riso.smartprint.R;

/**
 * @class MainActivity
 * 
 * @brief Main activity class.
 */
public class MainActivity extends BaseActivity implements PauseableHandlerCallback {

    /// Key for right drawer
    public static final String KEY_RIGHT_OPEN = "right_drawer_open";
    /// Key for left drawer
    public static final String KEY_LEFT_OPEN = "left_drawer_open";
    /// Key for view resize
    public static final String KEY_RESIZE_VIEW = "resize_view";
    //public static final String KEY_TRANSLATION = "translate";
    
    private static final int MSG_OPEN_DRAWER = 0;
    private static final int MSG_CLOSE_DRAWER = 1;
    private static final int MSG_CLEAR_ICON_STATES = 2;
    
    private SDADrawerLayout mDrawerLayout = null;
    private ViewGroup mMainLayout = null;
    private ViewGroup mLeftLayout = null;
    private ViewGroup mRightLayout = null;
    private ActionBarDrawerToggle mDrawerToggle = null;
    private boolean mResizeView = false;
    
    private PauseableHandler mHandler = null;
    private volatile boolean isRadaeeInitialized = false;

    @Override
    protected void onCreateContent(Bundle savedInstanceState) {
        if (getIntent() != null && getIntent().getData() != null) {
            Logger.logStartTime(this, this.getClass(), "Open-in");
        } else {
            Logger.logStartTime(this, this.getClass(), "AppLaunch");
        }

        if (getIntent() != null && getIntent().getData() != null) { // check if Open-In
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                // permission is granted, initialize Radaee (uses external storage)
                initializeRadaee();
            }
        }
        mHandler = new PauseableHandler(this);
        
        setContentView(R.layout.activity_main);
        
        mDrawerLayout = (SDADrawerLayout) findViewById(R.id.drawerLayout);
        mDrawerLayout.setScrimColor(Color.TRANSPARENT);
        
        mMainLayout = (ViewGroup) findViewById(R.id.mainLayout);
        mLeftLayout = (ViewGroup) findViewById(R.id.leftLayout);
        mRightLayout = (ViewGroup) findViewById(R.id.rightLayout);
        
        mLeftLayout.getLayoutParams().width = getDrawerWidth();
        mRightLayout.getLayoutParams().width = getDrawerWidth();
        
        mDrawerToggle = new SDAActionBarDrawerToggle(this, mDrawerLayout, R.drawable.img_btn_main_menu_normal, R.string.default_content_description,
                R.string.default_content_description);
        
        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }
        
        // Begin Fragments
        if (savedInstanceState == null) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            
            ft.add(R.id.mainLayout, new PrintPreviewFragment(), HomeFragment.FRAGMENT_TAGS[HomeFragment.STATE_PRINTPREVIEW]);
            ft.add(R.id.leftLayout, new HomeFragment());
            
            ft.commit();
        } else {
            mResizeView = savedInstanceState.getBoolean(KEY_RESIZE_VIEW, false);
            
            if (savedInstanceState.getBoolean(KEY_LEFT_OPEN, false)) {
                mMainLayout.setTranslationX(getDrawerWidth());                    
            } else if (savedInstanceState.getBoolean(KEY_RIGHT_OPEN, false)) {
                if (!mResizeView) { 
                    mMainLayout.setTranslationX(-getDrawerWidth());
                }
            } else {
                Message msg = Message.obtain(mHandler, MSG_CLEAR_ICON_STATES);
                mHandler.sendMessage(msg);
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        //Debug.waitForDebugger();
        if (isFinishing()){
            // delete PDF cache
            File file = new File(PDFFileManager.getSandboxPath());
            try {
                FileUtils.delete(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }        
        super.onDestroy();
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();

        if (getIntent() != null && getIntent().getData() != null) {
            Logger.logStopTime(this, this.getClass(), "Open-in");
        } else {
            Logger.logStopTime(this, this.getClass(), "AppLaunch");
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putBoolean(KEY_LEFT_OPEN, mDrawerLayout.isDrawerOpen(Gravity.LEFT));
        outState.putBoolean(KEY_RIGHT_OPEN, mDrawerLayout.isDrawerOpen(Gravity.RIGHT));
        outState.putBoolean(KEY_RESIZE_VIEW, mResizeView);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        mHandler.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        mHandler.resume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        mDrawerToggle.onConfigurationChanged(newConfig);
        
        BaseFragment fragment = (BaseFragment) getFragmentManager().findFragmentById(R.id.mainLayout);
        if (!mDrawerLayout.isDrawerOpen(Gravity.RIGHT) && !mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            fragment.clearIconStates();
        }
    }
    
    // ================================================================================
    // Public Functions
    // ================================================================================

    /**
     * @brief Open Drawer.
     * 
     * @param gravity Drawer gravity
     */
    public void openDrawer(int gravity) {
        openDrawer(gravity, false);
    }
    
    /**
     * @brief Open Drawer.
     * 
     * @param gravity Drawer gravity
     * @param resizeView Prevent layout from touches
     */
    public void openDrawer(int gravity, boolean resizeView) {
        Message msg = Message.obtain(mHandler, MSG_OPEN_DRAWER);
        msg.arg1 = gravity;
        msg.arg2 = 0;
        if (gravity == Gravity.RIGHT) {
            msg.arg2 = resizeView ? 1 : 0;
        }
        mHandler.sendMessage(msg);
    }
    
    /**
     * @brief Close drawers.
     */
    public void closeDrawers() {
        Message msg = Message.obtain(mHandler, MSG_CLOSE_DRAWER);
        mHandler.sendMessage(msg);
    }
    
    /**
     * @brief Determines if the drawer indicated by gravity is open.
     * 
     * @param gravity Drawer gravity
     * 
     * @retval true The drawer is open
     * @retval false The drawer is close
     */
    public boolean isDrawerOpen(int gravity) {
        return mDrawerLayout.isDrawerOpen(gravity);
    }
    
    // ================================================================================
    // INTERFACE - PauseableHandlerCallback 
    // ================================================================================
    
    @Override
    public boolean storeMessage(Message message) {
        return (message.what == MSG_OPEN_DRAWER
                || message.what == MSG_CLOSE_DRAWER
                || message.what == MSG_CLEAR_ICON_STATES);
    }

    @Override
    public void processMessage(Message msg) {
        BaseFragment fragment = ((BaseFragment) getFragmentManager().findFragmentById(R.id.mainLayout));
        boolean gravityLeft = (msg.arg1 == Gravity.LEFT);
        
        switch (msg.what){
            case MSG_OPEN_DRAWER:
                mDrawerLayout.closeDrawers();
                
                if (fragment != null) {
                    fragment.setIconState(R.id.menu_id_action_button, gravityLeft);
                }

                mResizeView = (msg.arg2 == 1);
                
                mDrawerLayout.openDrawer(msg.arg1);
                break;
            case MSG_CLOSE_DRAWER:
                mDrawerLayout.closeDrawers();
                break;
            case MSG_CLEAR_ICON_STATES:
                if (fragment != null) {
                    fragment.clearIconStates();
                }
                break;
        }
    }
    
    // ================================================================================
    // Internal Classes
    // ================================================================================
    
    /**
     * @class SDAActionBarDrawerToggle
     * 
     * @brief Class for Action Bar Drawer toggle.
     */
    private class SDAActionBarDrawerToggle extends ActionBarDrawerToggle {
        
        /**
         * @brief Constructor.
         * 
         * @param activity Activity
         * @param drawerLayout Drawer layout
         * @param drawerImageRes Drawer image resource
         * @param openDrawerContentDescRes Drawer content description resource
         * @param closeDrawerContentDescRes Drawer content description resource
         */
        public SDAActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, int drawerImageRes, int openDrawerContentDescRes,
                int closeDrawerContentDescRes) {
            super(activity, drawerLayout, drawerImageRes, openDrawerContentDescRes, closeDrawerContentDescRes);
        }
        
        @Override
        public void syncState() {
            super.syncState();
            
            if (mResizeView && mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                mMainLayout.setPadding(0, 0, getDrawerWidth(), 0);
                mMainLayout.requestLayout();
            }
        }
        
        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {
            float moveFactor = (mLeftLayout.getWidth() * slideOffset);
            if (drawerView.getId() == mRightLayout.getId()) {
                moveFactor *= -1;
            }
            
            if (mResizeView && drawerView.getId() == mRightLayout.getId()) {
                mMainLayout.setPadding(0, 0, (int)Math.abs(moveFactor), 0);
            } else {
                mMainLayout.setTranslationX(moveFactor);
            }
        }
        
        @Override
        public void onDrawerStateChanged(int newState) {
            
            super.onDrawerStateChanged(newState);
            
            final int layoutState = newState;
            
            // https://code.google.com/p/android/issues/detail?id=60671
            mDrawerLayout.post(new Runnable() {
                @Override
                public void run() {
                    if (layoutState == DrawerLayout.STATE_IDLE) {
                        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.LEFT);
                            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
                        } else if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.LEFT);
                            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.RIGHT);
                        } else {
                            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                        }
                    }
                }
            });
            
        }
        
        @Override
        public void onDrawerClosed(View view) {
            super.onDrawerClosed(view);
            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()

            BaseFragment fragment = (BaseFragment) getFragmentManager().findFragmentById(R.id.mainLayout);
            fragment.clearIconStates();
            if (mDrawerLayout.findViewById(R.id.rightLayout) == view) {
                getFragmentManager().findFragmentById(R.id.rightLayout).onPause();
            }
            getFragmentManager().findFragmentById(R.id.mainLayout).onResume();
            
        }
        
        @Override
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);
            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            
            if (mDrawerLayout.findViewById(R.id.rightLayout) == drawerView) {
                getFragmentManager().findFragmentById(R.id.rightLayout).onResume();
            }
            if (!mResizeView) {
                getFragmentManager().findFragmentById(R.id.mainLayout).onPause();
            }
        }
    }

    /**
     * @brief Initializes 3rd party PDF library
     */
    public synchronized void initializeRadaee() {
        if (!isRadaeeInitialized) {
            Global.Init(this);
            isRadaeeInitialized = true;
        }
    }
}