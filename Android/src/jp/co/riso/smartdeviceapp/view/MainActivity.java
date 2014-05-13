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
import jp.co.riso.smartdeviceapp.view.fragment.AddPrinterFragment;
import jp.co.riso.smartdeviceapp.view.fragment.HomeFragment;
import jp.co.riso.smartdeviceapp.view.fragment.PrintJobsFragment;
import jp.co.riso.smartdeviceapp.view.fragment.PrintPreviewFragment;
import jp.co.riso.smartdeviceapp.view.fragment.PrintSettingsFragment;
import jp.co.riso.smartdeviceapp.view.fragment.PrinterInfoFragment;
import jp.co.riso.smartdeviceapp.view.fragment.PrinterSearchFragment;
import jp.co.riso.smartdeviceapp.view.fragment.PrintersFragment;
import jp.co.riso.smartdeviceapp.view.widget.SDADrawerLayout;
import android.app.Activity;
import android.app.Fragment;
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

import com.radaee.pdf.Global;

public class MainActivity extends BaseActivity {
    
    public static final String KEY_TRANSLATION = "translate";
    public static final String KEY_RIGHT_OPEN = "right_drawer_open";
    public static final String KEY_RESIZE_VIEW = "resize_view";
    
    private SDADrawerLayout mDrawerLayout = null;
    private ViewGroup mMainLayout = null;
    private ViewGroup mLeftLayout = null;
    private ViewGroup mRightLayout = null;
    private ActionBarDrawerToggle mDrawerToggle = null;
    private boolean mResizeView = false;
    
    /** {@inheritDoc} */
    @Override
    protected void onCreateContent(Bundle savedInstanceState) {
        Global.Init(this);
        
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
            float translate = savedInstanceState.getFloat(KEY_TRANSLATION, 0.0f);
            if (mResizeView && savedInstanceState.getBoolean(KEY_RIGHT_OPEN, true)) {
                mMainLayout.setPadding(0, 0, (int)Math.abs(translate), 0);
                mMainLayout.requestLayout();
            } else {
                mMainLayout.setTranslationX(translate);
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
        
        outState.putBoolean(KEY_RESIZE_VIEW, mResizeView);
        outState.putFloat(KEY_TRANSLATION, mMainLayout.getTranslationX());
        outState.putBoolean(KEY_RIGHT_OPEN, mDrawerLayout.isDrawerOpen(Gravity.RIGHT));
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
        closeDrawers();
        openDrawer(gravity, false);
        if (gravity == Gravity.LEFT) {
            ((BaseFragment) getFragmentManager().findFragmentById(R.id.mainLayout)).setIconState(BaseFragment.ID_MENU_ACTION_BUTTON, true);
        }
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
        if (gravity == Gravity.RIGHT) {
            mResizeView = preventIntercept;
        }
        mDrawerLayout.setPreventInterceptTouches(preventIntercept);
        mDrawerLayout.openDrawer(gravity);
    }
    
    /**
     * Close drawers
     */
    public void closeDrawers() {
        mDrawerLayout.setPreventInterceptTouches(false);
        mDrawerLayout.closeDrawers();
        
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
                
                // #3614 fix
                if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
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
            
            if (mDrawerLayout.findViewById(R.id.rightLayout) == view) {
                getFragmentManager().findFragmentById(R.id.rightLayout).onPause();
                clearIconStates(false);
            } else {
                clearIconStates(true);
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
        
        /**
         * Clears the icon's selected states after the drawer is closed.
         * 
         * @param isLeft
         *          true if left drawer is closed, false if right drawer
         */
        private void clearIconStates(boolean isLeft) {
            BaseFragment fragment = (BaseFragment) getFragmentManager().findFragmentById(R.id.mainLayout);
            if (isLeft) {
                View menuButton = mMainLayout.findViewById(BaseFragment.ID_MENU_ACTION_BUTTON);
                
                if (menuButton != null) {
                    fragment.setIconState(BaseFragment.ID_MENU_ACTION_BUTTON, false);
                }
            }
            else {
                Fragment rightLayout = getFragmentManager().findFragmentById(R.id.rightLayout);
                if (rightLayout instanceof AddPrinterFragment) {
                    fragment.setIconState(PrintersFragment.ID_MENU_ACTION_ADD_BUTTON, false);
                } else if (rightLayout instanceof PrinterSearchFragment) {
                    fragment.setIconState(PrintersFragment.ID_MENU_ACTION_SEARCH_BUTTON, false);
                } else if (rightLayout instanceof PrintSettingsFragment) {
                    if (fragment instanceof PrinterInfoFragment){
                        fragment.setIconState(PrinterInfoFragment.ID_MENU_ACTION_PRINT_SETTINGS_BUTTON, false);
                    } else if (fragment instanceof PrintPreviewFragment){
                        fragment.setIconState(PrintPreviewFragment.ID_PRINT_BUTTON, false);
                    } else if (fragment instanceof PrintersFragment){
                        ((PrintersFragment) fragment).setDefaultSettingSelected(false);
                    } else if (fragment instanceof PrintJobsFragment){
                        fragment.setIconState(BaseFragment.ID_MENU_ACTION_BUTTON, false);
                    }
                }
            }
        }
    }
}
