package jp.co.riso.smartdeviceapp.view;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.view.Gravity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;

import jp.co.riso.smartdeviceapp.view.fragment.HomeFragment;
import jp.co.riso.smartprint.R;

public class MainActivityTest extends BaseMainActivityTest {
    TestFragment mTestRightFragment = null;
    TestFragment mTestMainFragment = null;

    // sleep is needed because drawer calls goes through mHandler
    private void waitForDrawer() {
        SystemClock.sleep(1000);
    }

    // fragments for testing calls on right and main layout
    private void initLayoutFragments() {
        final FragmentManager fm = mActivity.getSupportFragmentManager();

        mActivity.runOnUiThread(() -> {
            fm.beginTransaction().add(R.id.rightLayout, new TestFragment()).commit();
            fm.executePendingTransactions();
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        Fragment rightFragment = fm.findFragmentById(R.id.rightLayout);
        assertTrue(rightFragment instanceof TestFragment);
        mTestRightFragment = (TestFragment) rightFragment;

        mActivity.runOnUiThread(() -> {
            fm.beginTransaction().add(R.id.mainLayout, new TestFragment()).commit();
            fm.executePendingTransactions();
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        Fragment mainFragment = fm.findFragmentById(R.id.mainLayout);
        assertTrue(mainFragment instanceof TestFragment);
        mTestMainFragment = (TestFragment) mainFragment;
    }

    @Test
    public void openCloseLeftDrawer() {
        mActivity.openDrawer(Gravity.LEFT);
        waitForDrawer();
        assertTrue(mActivity.isDrawerOpen(Gravity.LEFT));

        mActivity.closeDrawers();
        waitForDrawer();
        assertFalse(mActivity.isDrawerOpen(Gravity.LEFT));
    }

    @Test
    public void openCloseRightDrawer() {
        initLayoutFragments();
        mActivity.openDrawer(Gravity.RIGHT);
        waitForDrawer();
        assertTrue(mActivity.isDrawerOpen(Gravity.RIGHT));
        assertTrue(mTestMainFragment.onPauseCalled);
        assertTrue(mTestRightFragment.onResumeCalled);

        mActivity.closeDrawers();
        waitForDrawer();
        assertFalse(mActivity.isDrawerOpen(Gravity.RIGHT));
        assertTrue(mTestMainFragment.onResumeCalled);
        assertTrue(mTestRightFragment.onPauseCalled);
    }

    @Test
    public void onBackPressed() {
        mActivity.openDrawer(Gravity.LEFT);
        waitForDrawer();
        assertTrue(mActivity.isDrawerOpen(Gravity.LEFT));

        mActivity.onBackPressed();
        waitForDrawer();
        assertFalse(mActivity.isDrawerOpen(Gravity.LEFT));

        initLayoutFragments();
        mActivity.openDrawer(Gravity.RIGHT);
        waitForDrawer();
        assertTrue(mActivity.isDrawerOpen(Gravity.RIGHT));

        mActivity.onBackPressed();
        waitForDrawer();
        assertFalse(mActivity.isDrawerOpen(Gravity.RIGHT));
    }

    @Test
    public void onSaveInstanceStateLeftDrawerOpen() {
        mActivity.openDrawer(Gravity.LEFT);
        waitForDrawer();

        Bundle testBundle = new Bundle();
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActivity.onSaveInstanceState(testBundle);
                assertTrue(testBundle.getBoolean(MainActivity.KEY_LEFT_OPEN));
                assertFalse(testBundle.getBoolean(MainActivity.KEY_RIGHT_OPEN));
                assertFalse(testBundle.getBoolean(MainActivity.KEY_RESIZE_VIEW));
            }
        });
    }

    @Test
    public void onSaveInstanceStateRightDrawerOpen() {
        initLayoutFragments();
        mActivity.openDrawer(Gravity.RIGHT);
        waitForDrawer();

        Bundle testBundle = new Bundle();
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActivity.onSaveInstanceState(testBundle);
                assertTrue(testBundle.getBoolean(MainActivity.KEY_RIGHT_OPEN));
                assertFalse(testBundle.getBoolean(MainActivity.KEY_LEFT_OPEN));
                assertFalse(testBundle.getBoolean(MainActivity.KEY_RESIZE_VIEW));
            }
        });
    }

    @Test
    public void onSaveInstanceStateResizeView() {
        initLayoutFragments();
        mActivity.openDrawer(Gravity.RIGHT, true);
        waitForDrawer();

        Bundle testBundle = new Bundle();
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActivity.onSaveInstanceState(testBundle);
                assertFalse(testBundle.getBoolean(MainActivity.KEY_LEFT_OPEN));
                assertTrue(testBundle.getBoolean(MainActivity.KEY_RIGHT_OPEN));
                assertTrue(testBundle.getBoolean(MainActivity.KEY_RESIZE_VIEW));
            }
        });
    }

    @Test
    public void storeMessage() {
        Message msg = new Message();
        msg.what = 0; //MSG_OPEN_DRAWER
        assertTrue(mActivity.storeMessage(msg));
        msg.what = 1; //MSG_CLOSE_DRAWER
        assertTrue(mActivity.storeMessage(msg));
        msg.what = 2; //MSG_CLEAR_ICON_STATES
        assertTrue(mActivity.storeMessage(msg));
        msg.what = -1; //others
        assertFalse(mActivity.storeMessage(msg));
        msg.what = 3; //others
        assertFalse(mActivity.storeMessage(msg));
    }

     public static class TestFragment extends HomeFragment {
        public boolean onResumeCalled = false;
        public boolean onPauseCalled = false;
        @Override
        public void onResume() {
            super.onResume();
            onResumeCalled = true;
        }
        @Override
        public void onPause() {
            super.onPause();
            onPauseCalled = true;
        }
    }
}
