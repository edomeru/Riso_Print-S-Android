package jp.co.riso.smartdeviceapp.view.fragment

import android.content.pm.ActivityInfo
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.preference.PreferenceManager
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import jp.co.riso.android.util.AppUtils
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import jp.co.riso.smartprint.R
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.junit.Assert
import org.junit.Before
import org.junit.Test


class SettingsFragmentTest : BaseActivityTestUtil() {
    private var _fragment: SettingsFragment? = null

    @Before
    fun initFragment() {
        wakeUpScreen()
        val fm = mainActivity!!.supportFragmentManager
        mainActivity!!.runOnUiThread {
            fm.beginTransaction().add(R.id.mainLayout, SettingsFragment()).commit()
            fm.executePendingTransactions()
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        val fragment = fm.findFragmentById(R.id.mainLayout)
        Assert.assertTrue(fragment is SettingsFragment)
        _fragment = fragment as SettingsFragment?
    }

    private fun isKeyboardOpen():Boolean {
        return WindowInsetsCompat
        .toWindowInsetsCompat(_fragment!!.view!!.rootWindowInsets)
        .isVisible(WindowInsetsCompat.Type.ime())
    }

    @Test
    fun testNewInstance() {
        Assert.assertNotNull(_fragment)

        // check initial value
        val prefs = PreferenceManager.getDefaultSharedPreferences(mainActivity)
        onView(withId(R.id.loginIdEditText)).check(matches(withText(
            prefs.getString(
                AppConstants.PREF_KEY_LOGIN_ID,
                AppConstants.PREF_DEFAULT_LOGIN_ID
        ))))
    }

    @Test
    fun testKeyboard_HideUsingMenuButton() {
        // show keyboard then hide via menu button
        onView(withId(R.id.loginIdEditText)).perform(click())
        waitForAnimation()
        Assert.assertTrue(isKeyboardOpen())

        onView(allOf(
            getElementFromMatchAtPosition(
                allOf(withId(R.id.menu_id_action_button)), 1),
            isDisplayed())
        ).perform(click())
        waitForAnimation()
        Assert.assertFalse(isKeyboardOpen())
    }

    @Test
    fun testKeyboard_HideUsingEnter() {
        // show keyboard then hide via enter
        onView(withId(R.id.loginIdEditText)).perform(click())
        waitForAnimation()
        Assert.assertTrue(isKeyboardOpen())

        //onView(withId(R.id.loginIdEditText)).perform(pressKey(KeyEvent.ACTION_UP))
        onView(withId(R.id.loginIdEditText)).perform(pressImeActionButton())
        waitForAnimation()
        Assert.assertFalse(isKeyboardOpen())
    }

    @Test
    fun testEditText_UpdateValue() {
        val text = "Sample Text 123"
        onView(withId(R.id.loginIdEditText)).perform(clearText())
        waitForAnimation()
        onView(withId(R.id.loginIdEditText)).perform(typeText(text))
        waitForAnimation()
        onView(withId(R.id.loginIdEditText)).check(matches(withText(text)))
    }

    @Test
    fun testSettingsFragment_OrientationChange() {
        if (!mainActivity!!.isTablet) {

            val expectedWidth = if (mainActivity!!.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                AppUtils.getScreenDimensions(mainActivity)!!.y
            } else {
                AppUtils.getScreenDimensions(mainActivity)!!.x
            }

            mainActivity!!.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            waitForAnimation()
            Assert.assertEquals(
                _fragment!!.view!!.findViewById<View>(R.id.rootView).width,
                expectedWidth
            )

            mainActivity!!.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            waitForAnimation()
            Assert.assertEquals(
                _fragment!!.view!!.findViewById<View>(R.id.rootView).width,
                expectedWidth
            )

            mainActivity!!.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            waitForAnimation()
            Assert.assertEquals(
                _fragment!!.view!!.findViewById<View>(R.id.rootView).width,
                expectedWidth
            )

            mainActivity!!.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
            waitForAnimation()
            Assert.assertEquals(
                _fragment!!.view!!.findViewById<View>(R.id.rootView).width,
                expectedWidth
            )
        }
    }
}