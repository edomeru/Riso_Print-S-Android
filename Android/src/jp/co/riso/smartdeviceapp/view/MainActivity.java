/*
 * Copyright (c) 2014 All rights reserved.
 *
 * MainActivity.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.view.base.BaseActivity;
import jp.co.riso.smartdeviceapp.view.fragment.NavigationFragment;
import jp.co.riso.smartdeviceapp.view.fragment.HomePreviewFragment;

public class MainActivity extends BaseActivity {
    
    public static final String KEY_TRANSLATION = "translate";
    
    private DrawerLayout mDrawerLayout;
    private ViewGroup mMainLayout;
    private ViewGroup mLeftLayout;
    private ViewGroup mRightLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    
    @Override
    protected void onCreateContent(Bundle savedInstanceState) {
        
        setContentView(R.layout.activity_main);
        
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mDrawerLayout.setScrimColor(Color.TRANSPARENT);
        
        mMainLayout = (ViewGroup) findViewById(R.id.mainLayout);
        mLeftLayout = (ViewGroup) findViewById(R.id.leftLayout);
        mRightLayout = (ViewGroup) findViewById(R.id.rightLayout);
        
        Point screenSize = AppUtils.getScreenDimensions(this);
        float drawerWidthPercentage = getResources().getFraction(R.dimen.drawer_width_percentage, 1, 1);
        float minDrawerWidth = getResources().getDimension(R.dimen.drawer_width_min);
        float maxDrawerWidth = getResources().getDimension(R.dimen.drawer_width_max);
        
        float drawerWidth = screenSize.x * drawerWidthPercentage;
        drawerWidth = Math.max(drawerWidth, minDrawerWidth);
        drawerWidth = Math.min(drawerWidth, maxDrawerWidth);
        
        mLeftLayout.getLayoutParams().width = (int)drawerWidth;
        mRightLayout.getLayoutParams().width = (int)drawerWidth;
        
        mDrawerToggle = new SDAActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.default_content_description,
                R.string.default_content_description);
        
        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }
        
        // Begin Fragments
        if (savedInstanceState == null) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            
            ft.add(R.id.mainLayout, new HomePreviewFragment());
            ft.add(R.id.leftLayout, new NavigationFragment());
            
            ft.commit();
        } else {
            mMainLayout.setTranslationX(savedInstanceState.getFloat(KEY_TRANSLATION, 0.0f));
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
        
        outState.putFloat(KEY_TRANSLATION, mMainLayout.getTranslationX());
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...
        
        return super.onOptionsItemSelected(item);
    }
    
    // ================================================================================
    // Public Functions
    // ================================================================================
    
    public void openDrawer(int gravity) {
        mDrawerLayout.openDrawer(gravity);
    }
    
    public void closeDrawers() {
        mDrawerLayout.closeDrawers();
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
        public void onDrawerSlide(View drawerView, float slideOffset) {
            float moveFactor = (mLeftLayout.getWidth() * slideOffset);
            if (drawerView.getId() == mRightLayout.getId()) {
                moveFactor *= -1;
            }
            
            mMainLayout.setTranslationX(moveFactor);
        }
        
        @Override
        public void onDrawerStateChanged(int newState) {
            
            super.onDrawerStateChanged(newState);
            
            if (newState == DrawerLayout.STATE_IDLE) {
                if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.END);
                } else if (mDrawerLayout.isDrawerOpen(Gravity.END)) {
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.START);
                } else {
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
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
