package jp.co.riso.smartdeviceapp.view

import jp.co.riso.smartprint.R
import android.view.Gravity
import android.os.Bundle
import android.os.Message
import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import jp.co.riso.smartdeviceapp.view.fragment.HomeFragment
import org.junit.Assert
import org.junit.Test

class MainActivityTest : BaseActivityTestUtil() {
    private var _testRightFragment: TestFragment? = null
    private var _testMainFragment: TestFragment? = null

    // sleep is needed because drawer calls goes through mHandler
    private fun waitForDrawer() {
        SystemClock.sleep(1000)
    }

    // fragments for testing calls on right and main layout
    private fun initLayoutFragments() {
        val fm = mainActivity!!.supportFragmentManager
        mainActivity!!.runOnUiThread {
            fm.beginTransaction().add(R.id.rightLayout, TestFragment()).commit()
            fm.executePendingTransactions()
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        val rightFragment = fm.findFragmentById(R.id.rightLayout)
        Assert.assertTrue(rightFragment is TestFragment)
        _testRightFragment = rightFragment as TestFragment?
        mainActivity!!.runOnUiThread {
            fm.beginTransaction().add(R.id.mainLayout, TestFragment()).commit()
            fm.executePendingTransactions()
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        val mainFragment = fm.findFragmentById(R.id.mainLayout)
        Assert.assertTrue(mainFragment is TestFragment)
        _testMainFragment = mainFragment as TestFragment?
    }

    @Test
    fun testOpenClose_LeftDrawer() {
        mainActivity!!.openDrawer(Gravity.LEFT)
        waitForDrawer()
        Assert.assertTrue(mainActivity!!.isDrawerOpen(Gravity.LEFT))
        mainActivity!!.closeDrawers()
        waitForDrawer()
        Assert.assertFalse(mainActivity!!.isDrawerOpen(Gravity.LEFT))
    }

    @Test
    fun testOpenClose_RightDrawer() {
        initLayoutFragments()
        mainActivity!!.openDrawer(Gravity.RIGHT)
        waitForDrawer()
        Assert.assertTrue(mainActivity!!.isDrawerOpen(Gravity.RIGHT))
        Assert.assertTrue(_testMainFragment!!.onPauseCalled)
        Assert.assertTrue(_testRightFragment!!.onResumeCalled)
        mainActivity!!.closeDrawers()
        waitForDrawer()
        Assert.assertFalse(mainActivity!!.isDrawerOpen(Gravity.RIGHT))
        Assert.assertTrue(_testMainFragment!!.onResumeCalled)
        Assert.assertTrue(_testRightFragment!!.onPauseCalled)
    }

    @Test
    fun testOnBackPressed() {
        mainActivity!!.openDrawer(Gravity.LEFT)
        waitForDrawer()
        Assert.assertTrue(mainActivity!!.isDrawerOpen(Gravity.LEFT))
        mainActivity!!.onBackPressed()
        waitForDrawer()
        Assert.assertFalse(mainActivity!!.isDrawerOpen(Gravity.LEFT))
        initLayoutFragments()
        mainActivity!!.openDrawer(Gravity.RIGHT)
        waitForDrawer()
        Assert.assertTrue(mainActivity!!.isDrawerOpen(Gravity.RIGHT))
        mainActivity!!.onBackPressed()
        waitForDrawer()
        Assert.assertFalse(mainActivity!!.isDrawerOpen(Gravity.RIGHT))
    }

    @Test
    fun testOnSaveInstanceState_LeftDrawerOpen() {
        mainActivity!!.openDrawer(Gravity.LEFT)
        waitForDrawer()
        val testBundle = Bundle()
        mainActivity!!.runOnUiThread {
            mainActivity!!.onSaveInstanceState(testBundle)
            Assert.assertTrue(testBundle.getBoolean(MainActivity.KEY_LEFT_OPEN))
            Assert.assertFalse(testBundle.getBoolean(MainActivity.KEY_RIGHT_OPEN))
            Assert.assertFalse(testBundle.getBoolean(MainActivity.KEY_RESIZE_VIEW))
        }
    }

    @Test
    fun testOnSaveInstanceState_RightDrawerOpen() {
        initLayoutFragments()
        mainActivity!!.openDrawer(Gravity.RIGHT)
        waitForDrawer()
        val testBundle = Bundle()
        mainActivity!!.runOnUiThread {
            mainActivity!!.onSaveInstanceState(testBundle)
            Assert.assertTrue(testBundle.getBoolean(MainActivity.KEY_RIGHT_OPEN))
            Assert.assertFalse(testBundle.getBoolean(MainActivity.KEY_LEFT_OPEN))
            Assert.assertFalse(testBundle.getBoolean(MainActivity.KEY_RESIZE_VIEW))
        }
    }

    @Test
    fun testOnSaveInstanceState_ResizeView() {
        initLayoutFragments()
        mainActivity!!.openDrawer(Gravity.RIGHT, true)
        waitForDrawer()
        val testBundle = Bundle()
        mainActivity!!.runOnUiThread {
            mainActivity!!.onSaveInstanceState(testBundle)
            Assert.assertFalse(testBundle.getBoolean(MainActivity.KEY_LEFT_OPEN))
            Assert.assertTrue(testBundle.getBoolean(MainActivity.KEY_RIGHT_OPEN))
            Assert.assertTrue(testBundle.getBoolean(MainActivity.KEY_RESIZE_VIEW))
        }
    }

    @Test
    fun testStoreMessage() {
        val msg = Message()
        msg.what = 0 //MSG_OPEN_DRAWER
        Assert.assertTrue(mainActivity!!.storeMessage(msg))
        msg.what = 1 //MSG_CLOSE_DRAWER
        Assert.assertTrue(mainActivity!!.storeMessage(msg))
        msg.what = 2 //MSG_CLEAR_ICON_STATES
        Assert.assertTrue(mainActivity!!.storeMessage(msg))
        msg.what = -1 //others
        Assert.assertFalse(mainActivity!!.storeMessage(msg))
        msg.what = 3 //others
        Assert.assertFalse(mainActivity!!.storeMessage(msg))
    }

    class TestFragment : HomeFragment() {
        var onResumeCalled = false
        var onPauseCalled = false
        override fun onResume() {
            super.onResume()
            onResumeCalled = true
        }

        override fun onPause() {
            super.onPause()
            onPauseCalled = true
        }
    }
}