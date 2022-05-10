package jp.co.riso.smartdeviceapp.view.anim

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import jp.co.riso.smartdeviceapp.view.MainActivity
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test

class DisplayDeleteAnimationTest : BaseActivityTestUtil() {
    private var _activity: MainActivity? = null
    
    @Before
    fun setUp() {
        _activity = mainActivity
        wakeUpScreen()
    }
    
    @Test
    fun testBeginDeleteModeOnView_WithAnimation() {
        val mDeleteAnimation = DisplayDeleteAnimation()
        val ll = LinearLayout(_activity)
        val deleteButton = Button(_activity)
        deleteButton.text = "button"
        deleteButton.id = 1
        ll.addView(deleteButton)
        deleteButton.visibility = View.GONE
        TestCase.assertEquals(View.GONE, deleteButton.visibility)
        mDeleteAnimation.beginDeleteModeOnView(ll, true, 1)
        waitInMilliseconds(1000)
        TestCase.assertEquals(View.VISIBLE, deleteButton.visibility)
    }

    @Test
    fun testBeginDeleteModeOnView_WithoutAnimation() {
        val mDeleteAnimation = DisplayDeleteAnimation()
        val ll = LinearLayout(_activity)
        val deleteButton = Button(_activity)
        deleteButton.text = "button"
        deleteButton.id = 1
        ll.addView(deleteButton)
        deleteButton.visibility = View.GONE
        TestCase.assertEquals(View.GONE, deleteButton.visibility)
        mDeleteAnimation.beginDeleteModeOnView(ll, false, 1)
        waitInMilliseconds(100)
        TestCase.assertEquals(View.VISIBLE, deleteButton.visibility)
    }
    
    @Test
    fun testBeginDeleteModeOnView_WithOutAnimationHideOtherViews() {
        val mDeleteAnimation = DisplayDeleteAnimation()
        val ll = LinearLayout(_activity)
        val deleteButton = Button(_activity)
        deleteButton.text = "button"
        deleteButton.id = 1
        ll.addView(deleteButton)
        val otherButton = Button(_activity)
        otherButton.text = "other button"
        otherButton.id = 2
        ll.addView(otherButton)
        val tv = TextView(_activity)
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

    @Test
    fun testBeginDeleteModeOnView_WithAnimationHideOtherViews() {
        // DisplayDeleteAnimation mDeleteAnimation = new
        // DisplayDeleteAnimation();
        val ll = LinearLayout(_activity)
        val deleteButton = Button(_activity)
        deleteButton.text = "button"
        deleteButton.id = 1
        ll.addView(deleteButton)
        val otherButton = Button(_activity)
        otherButton.text = "other button"
        otherButton.id = 2
        ll.addView(otherButton)
        val tv = TextView(_activity)
        tv.text = "textview"
        tv.id = 3
        ll.addView(tv)
        deleteButton.visibility = View.GONE
        otherButton.visibility = View.VISIBLE
        tv.visibility = View.VISIBLE
        TestCase.assertEquals(View.GONE, deleteButton.visibility)
        TestCase.assertEquals(View.VISIBLE, otherButton.visibility)
        TestCase.assertEquals(View.VISIBLE, tv.visibility)
        _activity!!.runOnUiThread {
            _activity!!.window.addContentView(
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

    @Test
    fun testBeginDeleteModeOnView_WithAnimationHideOtherViewsTwice() {
        // DisplayDeleteAnimation mDeleteAnimation = new
        // DisplayDeleteAnimation();
        val ll = LinearLayout(_activity)
        val deleteButton = Button(_activity)
        deleteButton.text = "button"
        deleteButton.id = 1
        ll.addView(deleteButton)
        val otherButton = Button(_activity)
        otherButton.text = "other button"
        otherButton.id = 2
        ll.addView(otherButton)
        val tv = TextView(_activity)
        tv.text = "textview"
        tv.id = 3
        ll.addView(tv)
        deleteButton.visibility = View.GONE
        otherButton.visibility = View.VISIBLE
        tv.visibility = View.VISIBLE
        TestCase.assertEquals(View.GONE, deleteButton.visibility)
        TestCase.assertEquals(View.VISIBLE, otherButton.visibility)
        TestCase.assertEquals(View.VISIBLE, tv.visibility)
        _activity!!.runOnUiThread {
            _activity!!.window.addContentView(
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

    @Test
    fun testEndDeleteModeOnView_WithAnimation() {
        // setActivityInitialTouchMode(true);
        val ll = LinearLayout(_activity)
        val deleteButton = Button(_activity)
        deleteButton.text = "button"
        deleteButton.id = 1
        ll.addView(deleteButton)
        TestCase.assertEquals(View.VISIBLE, deleteButton.visibility)
        _activity!!.runOnUiThread { // ll.addView(deleteButton);
            _activity!!.window.addContentView(
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

    @Test
    fun testEndDeleteModeOnView_WithOutAnimation() {
        val ll = LinearLayout(_activity)
        val deleteButton = Button(_activity)
        deleteButton.text = "button"
        deleteButton.id = 1
        ll.addView(deleteButton)
        TestCase.assertEquals(View.VISIBLE, deleteButton.visibility)
        val mDeleteAnimation = DisplayDeleteAnimation()
        mDeleteAnimation.endDeleteMode(ll, false, 1)
        waitInMilliseconds(100)
        TestCase.assertEquals(View.GONE, deleteButton.visibility)
    }

    @Test
    fun testEndDeleteModeOnView_WithOutAnimationShowOtherViews() {
        val ll = LinearLayout(_activity)
        val deleteButton = Button(_activity)
        deleteButton.text = "button"
        deleteButton.id = 1
        ll.addView(deleteButton)
        val otherButton = Button(_activity)
        otherButton.text = "other button"
        otherButton.id = 2
        ll.addView(otherButton)
        val tv = TextView(_activity)
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

    @Test
    fun testEndDeleteModeOnView_WithAnimationShowOtherViews() {
        val ll = LinearLayout(_activity)
        val deleteButton = Button(_activity)
        deleteButton.text = "button"
        deleteButton.id = 1
        ll.addView(deleteButton)
        val otherButton = Button(_activity)
        otherButton.text = "other button"
        otherButton.id = 2
        ll.addView(otherButton)
        val tv = TextView(_activity)
        tv.text = "textview"
        tv.id = 3
        ll.addView(tv)
        deleteButton.visibility = View.VISIBLE
        otherButton.visibility = View.GONE
        tv.visibility = View.GONE
        TestCase.assertEquals(View.VISIBLE, deleteButton.visibility)
        TestCase.assertEquals(View.GONE, otherButton.visibility)
        TestCase.assertEquals(View.GONE, tv.visibility)
        _activity!!.runOnUiThread {
            _activity!!.window.addContentView(
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

    @Test
    fun testEndDeleteModeOnView_WithAnimationShowOtherViewsTwice() {
        val ll = LinearLayout(_activity)
        val deleteButton = Button(_activity)
        deleteButton.text = "button"
        deleteButton.id = 1
        ll.addView(deleteButton)
        val otherButton = Button(_activity)
        otherButton.text = "other button"
        otherButton.id = 2
        ll.addView(otherButton)
        val tv = TextView(_activity)
        tv.text = "textview"
        tv.id = 3
        ll.addView(tv)
        deleteButton.visibility = View.VISIBLE
        otherButton.visibility = View.GONE
        tv.visibility = View.GONE
        TestCase.assertEquals(View.VISIBLE, deleteButton.visibility)
        TestCase.assertEquals(View.GONE, otherButton.visibility)
        TestCase.assertEquals(View.GONE, tv.visibility)
        _activity!!.runOnUiThread {
            _activity!!.window.addContentView(
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
}