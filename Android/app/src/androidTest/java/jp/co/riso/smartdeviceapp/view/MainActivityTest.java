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

public class MainActivityTest extends BaseActivityTestUtil {
    TestFragment mTestRightFragment = null;
    TestFragment mTestMainFragment = null;

    // sleep is needed because drawer calls goes through mHandler
    private void waitForDrawer() {
        SystemClock.sleep(1000);
    }

    // fragments for testing calls on right and main layout
    private void initLayoutFragments() {
        final FragmentManager fm = mainActivity.getSupportFragmentManager();

        mainActivity.runOnUiThread(() -> {
            fm.beginTransaction().add(R.id.rightLayout, new TestFragment()).commit();
            fm.executePendingTransactions();
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        Fragment rightFragment = fm.findFragmentById(R.id.rightLayout);
        assertTrue(rightFragment instanceof TestFragment);
        mTestRightFragment = (TestFragment) rightFragment;

        mainActivity.runOnUiThread(() -> {
            fm.beginTransaction().add(R.id.mainLayout, new TestFragment()).commit();
            fm.executePendingTransactions();
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        Fragment mainFragment = fm.findFragmentById(R.id.mainLayout);
        assertTrue(mainFragment instanceof TestFragment);
        mTestMainFragment = (TestFragment) mainFragment;
    }

    @Test
    public void testOpenClose_LeftDrawer() {
        mainActivity.openDrawer(Gravity.LEFT);
        waitForDrawer();
        assertTrue(mainActivity.isDrawerOpen(Gravity.LEFT));

        mainActivity.closeDrawers();
        waitForDrawer();
        assertFalse(mainActivity.isDrawerOpen(Gravity.LEFT));
    }

    @Test
    public void testOpenClose_RightDrawer() {
        initLayoutFragments();
        mainActivity.openDrawer(Gravity.RIGHT);
        waitForDrawer();
        assertTrue(mainActivity.isDrawerOpen(Gravity.RIGHT));
        assertTrue(mTestMainFragment.onPauseCalled);
        assertTrue(mTestRightFragment.onResumeCalled);

        mainActivity.closeDrawers();
        waitForDrawer();
        assertFalse(mainActivity.isDrawerOpen(Gravity.RIGHT));
        assertTrue(mTestMainFragment.onResumeCalled);
        assertTrue(mTestRightFragment.onPauseCalled);
    }

    @Test
    public void testOnBackPressed() {
        mainActivity.openDrawer(Gravity.LEFT);
        waitForDrawer();
        assertTrue(mainActivity.isDrawerOpen(Gravity.LEFT));

        mainActivity.onBackPressed();
        waitForDrawer();
        assertFalse(mainActivity.isDrawerOpen(Gravity.LEFT));

        initLayoutFragments();
        mainActivity.openDrawer(Gravity.RIGHT);
        waitForDrawer();
        assertTrue(mainActivity.isDrawerOpen(Gravity.RIGHT));

        mainActivity.onBackPressed();
        waitForDrawer();
        assertFalse(mainActivity.isDrawerOpen(Gravity.RIGHT));
    }

    @Test
    public void testOnSaveInstanceState_LeftDrawerOpen() {
        mainActivity.openDrawer(Gravity.LEFT);
        waitForDrawer();

        Bundle testBundle = new Bundle();
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.onSaveInstanceState(testBundle);
                assertTrue(testBundle.getBoolean(MainActivity.KEY_LEFT_OPEN));
                assertFalse(testBundle.getBoolean(MainActivity.KEY_RIGHT_OPEN));
                assertFalse(testBundle.getBoolean(MainActivity.KEY_RESIZE_VIEW));
            }
        });
    }

    @Test
    public void testOnSaveInstanceState_RightDrawerOpen() {
        initLayoutFragments();
        mainActivity.openDrawer(Gravity.RIGHT);
        waitForDrawer();

        Bundle testBundle = new Bundle();
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.onSaveInstanceState(testBundle);
                assertTrue(testBundle.getBoolean(MainActivity.KEY_RIGHT_OPEN));
                assertFalse(testBundle.getBoolean(MainActivity.KEY_LEFT_OPEN));
                assertFalse(testBundle.getBoolean(MainActivity.KEY_RESIZE_VIEW));
            }
        });
    }

    @Test
    public void testOnSaveInstanceState_ResizeView() {
        initLayoutFragments();
        mainActivity.openDrawer(Gravity.RIGHT, true);
        waitForDrawer();

        Bundle testBundle = new Bundle();
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.onSaveInstanceState(testBundle);
                assertFalse(testBundle.getBoolean(MainActivity.KEY_LEFT_OPEN));
                assertTrue(testBundle.getBoolean(MainActivity.KEY_RIGHT_OPEN));
                assertTrue(testBundle.getBoolean(MainActivity.KEY_RESIZE_VIEW));
            }
        });
    }

    @Test
    public void testStoreMessage() {
        Message msg = new Message();
        msg.what = 0; //MSG_OPEN_DRAWER
        assertTrue(mainActivity.storeMessage(msg));
        msg.what = 1; //MSG_CLOSE_DRAWER
        assertTrue(mainActivity.storeMessage(msg));
        msg.what = 2; //MSG_CLEAR_ICON_STATES
        assertTrue(mainActivity.storeMessage(msg));
        msg.what = -1; //others
        assertFalse(mainActivity.storeMessage(msg));
        msg.what = 3; //others
        assertFalse(mainActivity.storeMessage(msg));
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
