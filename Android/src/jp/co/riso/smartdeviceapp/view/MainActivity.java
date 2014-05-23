/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * MainActivity.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view;

import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.view.base.BaseActivity;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartdeviceapp.view.fragment.HomeFragment;
import jp.co.riso.smartdeviceapp.view.fragment.PrintPreviewFragment;
import jp.co.riso.smartdeviceapp.view.widget.SDADrawerLayout;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.radaee.pdf.Global;

public class MainActivity extends BaseActivity implements Callback {

    public static final String KEY_RIGHT_OPEN = "right_drawer_open";
    public static final String KEY_LEFT_OPEN = "left_drawer_open";
    public static final String KEY_RESIZE_VIEW = "resize_view";
    //public static final String KEY_TRANSLATION = "translate";
    
    private static final int MSG_OPEN_DRAWER = 0;
    private static final int MSG_OPEN_DRAWER_INTERCEPT = 1;
    private static final int MSG_CLOSE_DRAWER = 2;
    private static final int MSG_CLEAR_ICON_STATES = 3;
    
    private SDADrawerLayout mDrawerLayout = null;
    private ViewGroup mMainLayout = null;
    private ViewGroup mLeftLayout = null;
    private ViewGroup mRightLayout = null;
    private ActionBarDrawerToggle mDrawerToggle = null;
    private boolean mResizeView = false;
    
    private Handler mHandler = null;
    
    /** {@inheritDoc} */
    @Override
    protected void onCreateContent(Bundle savedInstanceState) {
        Global.Init(this);

        mHandler = new Handler(this);
        
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
            
            ft.add(R.id.mainLayout, new PrintPreviewFragment());
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
    
    /** {@inheritDoc} */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }
    
    /** {@inheritDoc} */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putBoolean(KEY_LEFT_OPEN, mDrawerLayout.isDrawerOpen(Gravity.LEFT));
        outState.putBoolean(KEY_RIGHT_OPEN, mDrawerLayout.isDrawerOpen(Gravity.RIGHT));
        outState.putBoolean(KEY_RESIZE_VIEW, mResizeView);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    // ================================================================================
    // Public Functions
    // ================================================================================
    
    /**
     * Open Drawer
     * 
     * @param gravity
     *            Drawer gravity
     */
    public void openDrawer(int gravity) {
        Message msg = Message.obtain(mHandler, MSG_OPEN_DRAWER);
        msg.arg1 = gravity;
        mHandler.sendMessage(msg);
    }
    
    /**
     * Open Drawer
     * 
     * @param gravity
     *            Drawer gravity
     * @param preventIntercept
     *            Prevent layout from touches
     */
    public void openDrawer(int gravity, boolean preventIntercept) {
        Message msg = Message.obtain(mHandler, MSG_OPEN_DRAWER_INTERCEPT);
        msg.arg1 = gravity;
        msg.arg2 = preventIntercept ? 1 : 0;
        mHandler.sendMessage(msg);
    }
    
    /**
     * Close drawers
     */
    public void closeDrawers() {
        Message msg = Message.obtain(mHandler, MSG_CLOSE_DRAWER);
        mHandler.sendMessage(msg);
    }
    
    /**
     * Determines if the drawer indicated by gravity is open
     * 
     * @param gravity
     *            Drawer gravity
     * @return true if the drawer is open
     */
    public boolean isDrawerOpen(int gravity) {
        return mDrawerLayout.isDrawerOpen(gravity);
    }
    
    // ================================================================================
    // INTERFACE - Callback 
    // ================================================================================

    /** {@inheritDoc} */
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case MSG_OPEN_DRAWER:
                closeDrawers();
                openDrawer(msg.arg1, false);
                if (msg.arg1 == Gravity.LEFT) {
                    ((BaseFragment) getFragmentManager().findFragmentById(R.id.mainLayout)).setIconState(BaseFragment.ID_MENU_ACTION_BUTTON, true);
                }
                return true;
            case MSG_OPEN_DRAWER_INTERCEPT:
                if (msg.arg1 == Gravity.RIGHT) {
                    mResizeView = (msg.arg2 == 1);
                }
                mDrawerLayout.setPreventInterceptTouches((msg.arg2 == 1));
                mDrawerLayout.openDrawer(msg.arg1);
                return true;
            case MSG_CLOSE_DRAWER:
                mDrawerLayout.setPreventInterceptTouches(false);
                mDrawerLayout.closeDrawers();
                return true;
            case MSG_CLEAR_ICON_STATES:
                BaseFragment fragment = (BaseFragment) getFragmentManager().findFragmentById(R.id.mainLayout);
                fragment.clearIconStates();
                return true;
        }
        return false;
    }
    
    // ================================================================================
    // Internal Classes
    // ================================================================================
    
    private class SDAActionBarDrawerToggle extends ActionBarDrawerToggle {
        
        /**
         * Constructor
         * 
         * @param activity
         *            activity
         * @param drawerLayout
         *            drawer layout
         * @param drawerImageRes
         *            drawer image resources
         * @param openDrawerContentDescRes
         *            drawer content description
         * @param closeDrawerContentDescRes
         *            drawer content description
         */
        public SDAActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, int drawerImageRes, int openDrawerContentDescRes,
                int closeDrawerContentDescRes) {
            super(activity, drawerLayout, drawerImageRes, openDrawerContentDescRes, closeDrawerContentDescRes);
        }
        
        /** {@inheritDoc} */
        @Override
        public void syncState() {
            super.syncState();
            
            if (mResizeView && mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                mMainLayout.setPadding(0, 0, getDrawerWidth(), 0);
                mMainLayout.requestLayout();
            }
        }
        
        /** {@inheritDoc} */
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
                
                // #3614 and #3734 fix
                if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    mMainLayout.requestLayout();
                }
            }
        }
        
        /** {@inheritDoc} */
        @Override
        public void onDrawerStateChanged(int newState) {
            
            super.onDrawerStateChanged(newState);
            
            if (newState == DrawerLayout.STATE_IDLE) {
                if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.START);
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.END);
                } else if (mDrawerLayout.isDrawerOpen(Gravity.END)) {
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.START);
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.END);
                } else {
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                }
            }
            
        }
        
        /**
         * Called when a drawer has settled in a completely closed state.
         */
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
        
        /**
         * Called when a drawer has settled in a completely opened state.
         */
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
}
