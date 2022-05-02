package jp.co.riso.smartdeviceapp.view.anim

import android.test.ActivityInstrumentationTestCase2
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import jp.co.riso.smartdeviceapp.view.MainActivity
import junit.framework.TestCase

class DisplayDeleteAnimationTest : ActivityInstrumentationTestCase2<MainActivity> {
    private var mActivity: MainActivity? = null

    constructor() : super(MainActivity::class.java) {}
    constructor(activityClass: Class<MainActivity?>?) : super(activityClass) {}

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        mActivity = activity
        wakeUpScreen()
    }

    @Throws(Exception::class)
    override fun tearDown() {
        super.tearDown()
    }

    fun testBeginDeleteModeOnView_WithAnimation() {
        val mDeleteAnimation = DisplayDeleteAnimation()
        val ll = LinearLayout(mActivity)
        val deleteButton = Button(mActivity)
        deleteButton.text = "button"
        deleteButton.id = 1
        ll.addView(deleteButton)
        deleteButton.visibility = View.GONE
        TestCase.assertEquals(View.GONE, deleteButton.visibility)
        mDeleteAnimation.beginDeleteModeOnView(ll, true, 1)
        waitInMilliseconds(1000)
        TestCase.assertEquals(View.VISIBLE, deleteButton.visibility)
    }

    fun testBeginDeleteModeOnView_WithoutAnimation() {
        val mDeleteAnimation = DisplayDeleteAnimation()
        val ll = LinearLayout(mActivity)
        val deleteButton = Button(mActivity)
        deleteButton.text = "button"
        deleteButton.id = 1
        ll.addView(deleteButton)
        deleteButton.visibility = View.GONE
        TestCase.assertEquals(View.GONE, deleteButton.visibility)
        mDeleteAnimation.beginDeleteModeOnView(ll, false, 1)
        waitInMilliseconds(100)
        TestCase.assertEquals(View.VISIBLE, deleteButton.visibility)
    }

    fun testBeginDeleteModeOnView_WithOutAnimationHideOtherViews() {
        val mDeleteAnimation = DisplayDeleteAnimation()
        val ll = LinearLayout(mActivity)
        val deleteButton = Button(mActivity)
        deleteButton.text = "button"
        deleteButton.id = 1
        ll.addView(deleteButton)
        val otherButton = Button(mActivity)
        otherButton.text = "other button"
        otherButton.id = 2
        ll.addView(otherButton)
        val tv = TextView(mActivity)
        tv.text = "textview"
        tv.id = 3
        ll.addView(tv)
        deleteButton.visibility = View.GONE
        otherButton.visibility = View.VISIBLE
        tv.visibility = View.VISIBLE
        TestCase.assertEquals(View.GONE, deleteButton.visibility)
        TestCase.assertEquals(View.VISIBLE, otherButton.visibility)
        TestCase.assertEquals(View.VISIBLE, tv.visibility)
        mDeleteAnimation.beginDeleteModeOnView(ll, false, 1, 2, 3)
        waitInMilliseconds(100)
        TestCase.assertEquals(View.VISIBLE, deleteButton.visibility)
        TestCase.assertEquals(View.GONE, otherButton.visibility)
        TestCase.assertEquals(View.GONE, tv.visibility)
    }

    fun testBeginDeleteModeOnView_WithAnimationHideOtherViews() {
        // DisplayDeleteAnimation mDeleteAnimation = new
        // DisplayDeleteAnimation();
        val ll = LinearLayout(mActivity)
        val deleteButton = Button(mActivity)
        deleteButton.text = "button"
        deleteButton.id = 1
        ll.addView(deleteButton)
        val otherButton = Button(mActivity)
        otherButton.text = "other button"
        otherButton.id = 2
        ll.addView(otherButton)
        val tv = TextView(mActivity)
        tv.text = "textview"
        tv.id = 3
        ll.addView(tv)
        deleteButton.visibility = View.GONE
        otherButton.visibility = View.VISIBLE
        tv.visibility = View.VISIBLE
        TestCase.assertEquals(View.GONE, deleteButton.visibility)
        TestCase.assertEquals(View.VISIBLE, otherButton.visibility)
        TestCase.assertEquals(View.VISIBLE, tv.visibility)
        mActivity!!.runOnUiThread {
            mActivity!!.window.addContentView(
                ll, LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            val mDeleteAnimation = DisplayDeleteAnimation()
            mDeleteAnimation.beginDeleteModeOnView(ll, true, 1, 2, 3)
        }
        waitInMilliseconds(1000)
        TestCase.assertEquals(View.VISIBLE, deleteButton.visibility)
        TestCase.assertEquals(View.GONE, otherButton.visibility)
        TestCase.assertEquals(View.GONE, tv.visibility)
    }

    fun testBeginDeleteModeOnView_WithAnimationHideOtherViewsTwice() {
        // DisplayDeleteAnimation mDeleteAnimation = new
        // DisplayDeleteAnimation();
        val ll = LinearLayout(mActivity)
        val deleteButton = Button(mActivity)
        deleteButton.text = "button"
        deleteButton.id = 1
        ll.addView(deleteButton)
        val otherButton = Button(mActivity)
        otherButton.text = "other button"
        otherButton.id = 2
        ll.addView(otherButton)
        val tv = TextView(mActivity)
        tv.text = "textview"
        tv.id = 3
        ll.addView(tv)
        deleteButton.visibility = View.GONE
        otherButton.visibility = View.VISIBLE
        tv.visibility = View.VISIBLE
        TestCase.assertEquals(View.GONE, deleteButton.visibility)
        TestCase.assertEquals(View.VISIBLE, otherButton.visibility)
        TestCase.assertEquals(View.VISIBLE, tv.visibility)
        mActivity!!.runOnUiThread {
            mActivity!!.window.addContentView(
                ll, LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            val mDeleteAnimation = DisplayDeleteAnimation()
            mDeleteAnimation.beginDeleteModeOnView(ll, true, 1, 2, 3)
            mDeleteAnimation.beginDeleteModeOnView(ll, true, 1, 2, 3)
        }
        waitInMilliseconds(1000)
        TestCase.assertEquals(View.VISIBLE, deleteButton.visibility)
        TestCase.assertEquals(View.GONE, otherButton.visibility)
        TestCase.assertEquals(View.GONE, tv.visibility)
    }

    fun testEndDeleteModeOnView_WithAnimation() {
        // setActivityInitialTouchMode(true);
        val ll = LinearLayout(mActivity)
        val deleteButton = Button(mActivity)
        deleteButton.text = "button"
        deleteButton.id = 1
        ll.addView(deleteButton)
        TestCase.assertEquals(View.VISIBLE, deleteButton.visibility)
        mActivity!!.runOnUiThread { // ll.addView(deleteButton);
            mActivity!!.window.addContentView(
                ll,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            // assertEquals(View.VISIBLE, deleteButton.getVisibility());
            val mDeleteAnimation = DisplayDeleteAnimation()
            mDeleteAnimation.endDeleteMode(ll, true, 1)
        }
        waitInMilliseconds(1000)
        TestCase.assertEquals(View.GONE, deleteButton.visibility)
    }

    fun testEndDeleteModeOnView_WithOutAnimation() {
        val ll = LinearLayout(mActivity)
        val deleteButton = Button(mActivity)
        deleteButton.text = "button"
        deleteButton.id = 1
        ll.addView(deleteButton)
        TestCase.assertEquals(View.VISIBLE, deleteButton.visibility)
        val mDeleteAnimation = DisplayDeleteAnimation()
        mDeleteAnimation.endDeleteMode(ll, false, 1)
        waitInMilliseconds(100)
        TestCase.assertEquals(View.GONE, deleteButton.visibility)
    }

    fun testEndDeleteModeOnView_WithOutAnimationShowOtherViews() {
        val ll = LinearLayout(mActivity)
        val deleteButton = Button(mActivity)
        deleteButton.text = "button"
        deleteButton.id = 1
        ll.addView(deleteButton)
        val otherButton = Button(mActivity)
        otherButton.text = "other button"
        otherButton.id = 2
        ll.addView(otherButton)
        val tv = TextView(mActivity)
        tv.text = "textview"
        tv.id = 3
        ll.addView(tv)
        deleteButton.visibility = View.VISIBLE
        otherButton.visibility = View.GONE
        tv.visibility = View.GONE
        TestCase.assertEquals(View.VISIBLE, deleteButton.visibility)
        TestCase.assertEquals(View.GONE, otherButton.visibility)
        TestCase.assertEquals(View.GONE, tv.visibility)
        val mDeleteAnimation = DisplayDeleteAnimation()
        mDeleteAnimation.endDeleteMode(ll, false, 1, 2, 3)
        waitInMilliseconds(100)
        TestCase.assertEquals(View.GONE, deleteButton.visibility)
        TestCase.assertEquals(View.VISIBLE, otherButton.visibility)
        TestCase.assertEquals(View.VISIBLE, tv.visibility)
    }

    fun testEndDeleteModeOnView_WithAnimationShowOtherViews() {
        val ll = LinearLayout(mActivity)
        val deleteButton = Button(mActivity)
        deleteButton.text = "button"
        deleteButton.id = 1
        ll.addView(deleteButton)
        val otherButton = Button(mActivity)
        otherButton.text = "other button"
        otherButton.id = 2
        ll.addView(otherButton)
        val tv = TextView(mActivity)
        tv.text = "textview"
        tv.id = 3
        ll.addView(tv)
        deleteButton.visibility = View.VISIBLE
        otherButton.visibility = View.GONE
        tv.visibility = View.GONE
        TestCase.assertEquals(View.VISIBLE, deleteButton.visibility)
        TestCase.assertEquals(View.GONE, otherButton.visibility)
        TestCase.assertEquals(View.GONE, tv.visibility)
        mActivity!!.runOnUiThread {
            mActivity!!.window.addContentView(
                ll,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            val mDeleteAnimation = DisplayDeleteAnimation()
            mDeleteAnimation.endDeleteMode(ll, true, 1, 2, 3)
        }
        waitInMilliseconds(1000)
        TestCase.assertEquals(View.GONE, deleteButton.visibility)
        TestCase.assertEquals(View.VISIBLE, otherButton.visibility)
        TestCase.assertEquals(View.VISIBLE, tv.visibility)
    }

    fun testEndDeleteModeOnView_WithAnimationShowOtherViewsTwice() {
        val ll = LinearLayout(mActivity)
        val deleteButton = Button(mActivity)
        deleteButton.text = "button"
        deleteButton.id = 1
        ll.addView(deleteButton)
        val otherButton = Button(mActivity)
        otherButton.text = "other button"
        otherButton.id = 2
        ll.addView(otherButton)
        val tv = TextView(mActivity)
        tv.text = "textview"
        tv.id = 3
        ll.addView(tv)
        deleteButton.visibility = View.VISIBLE
        otherButton.visibility = View.GONE
        tv.visibility = View.GONE
        TestCase.assertEquals(View.VISIBLE, deleteButton.visibility)
        TestCase.assertEquals(View.GONE, otherButton.visibility)
        TestCase.assertEquals(View.GONE, tv.visibility)
        mActivity!!.runOnUiThread {
            mActivity!!.window.addContentView(
                ll,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            val mDeleteAnimation = DisplayDeleteAnimation()
            mDeleteAnimation.endDeleteMode(ll, true, 1, 2, 3)
            mDeleteAnimation.endDeleteMode(ll, true, 1, 2, 3)
        }
        waitInMilliseconds(1000)
        TestCase.assertEquals(View.GONE, deleteButton.visibility)
        TestCase.assertEquals(View.VISIBLE, otherButton.visibility)
        TestCase.assertEquals(View.VISIBLE, tv.visibility)
    }

    //================================================================================
    // Private methods
    //================================================================================
    // wait some milliseconds so that you can see the change on emulator/device.
    private fun waitInMilliseconds(ms: Int) {
        try {
            Thread.sleep(ms.toLong())
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun wakeUpScreen() {
        mActivity!!.runOnUiThread {
            mActivity!!.window.addFlags(
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            )
        }
        waitInMilliseconds(2000)
    }
}