/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * MainActivity.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view;

import com.radaee.pdf.Global;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.view.base.BaseActivity;
import jp.co.riso.smartdeviceapp.view.fragment.HomeFragment;
import jp.co.riso.smartdeviceapp.view.fragment.PrintPreviewFragment;
import jp.co.riso.smartdeviceapp.view.widget.SDADrawerLayout;

public class MainActivity extends BaseActivity {
    
    public static final String KEY_TRANSLATION = "translate";
    public static final String KEY_RIGHT_OPEN = "right_drawer_open";
    public static final String KEY_RESIZE_VIEW = "resize_view";
    
    private SDADrawerLayout mDrawerLayout;
    private ViewGroup mMainLayout;
    private ViewGroup mLeftLayout;
    private ViewGroup mRightLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private boolean mResizeView = false;
    
    @Override
    protected void onCreateContent(Bundle savedInstanceState) {
        Global.Init(this);
        
        setContentView(R.layout.activity_main);
        
        mDrawerLayout = (SDADrawerLayout) findViewById(R.id.drawerLayout);
        mDrawerLayout.setScrimColor(Color.TRANSPARENT);
        
        mMainLayout = (ViewGroup) findViewById(R.id.mainLayout);
        mLeftLayout = (ViewGroup) findViewById(R.id.leftLayout);
        mRightLayout = (ViewGroup) findViewById(R.id.rightLayout);
        
        mLeftLayout.getLayoutParams().width = (int)getDrawerWidth();
        mRightLayout.getLayoutParams().width = (int)getDrawerWidth();
        
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
            mResizeView = savedInstanceState.getBoolean(KEY_RIGHT_OPEN, false);
            float translate = savedInstanceState.getFloat(KEY_TRANSLATION, 0.0f);
            if (mResizeView && savedInstanceState.getBoolean(KEY_RIGHT_OPEN, true)) {
                mMainLayout.setPadding(0, 0, (int)Math.abs(translate), 0);
                mMainLayout.requestLayout();
            } else {
                mMainLayout.setTranslationX(translate);
            }
        }
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putBoolean(KEY_RESIZE_VIEW, mResizeView);
        outState.putFloat(KEY_TRANSLATION, mMainLayout.getTranslationX());
        outState.putBoolean(KEY_RIGHT_OPEN, mDrawerLayout.isDrawerOpen(Gravity.RIGHT));
    }
    
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

    public void openDrawer(int gravity) {
        closeDrawers();
        openDrawer(gravity, false);
    }
        
    public void openDrawer(int gravity, boolean preventIntercept) {
        if (gravity == Gravity.RIGHT) {
            mResizeView = preventIntercept;
        }
        mDrawerLayout.setPreventInterceptTouches(preventIntercept);
        mDrawerLayout.openDrawer(gravity);
    }
    
    public void closeDrawers() {
        mDrawerLayout.setPreventInterceptTouches(false);
        mDrawerLayout.closeDrawers();
    }
    
    public boolean isDrawerOpen(int gravity) {
        return mDrawerLayout.isDrawerOpen(gravity);
    }
    
    // ================================================================================
    // Internal Classes
    // ================================================================================
    
    private class SDAActionBarDrawerToggle extends ActionBarDrawerToggle {
        
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
                
                // #3614 fix
                if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                    mMainLayout.requestLayout();
                }
            }
        }
        
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
        public void onDrawerClosed(View view) {
            super.onDrawerClosed(view);
            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        }
        
        /**
         * Called when a drawer has settled in a completely opened state.
         */
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);
            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        }
    }
}
