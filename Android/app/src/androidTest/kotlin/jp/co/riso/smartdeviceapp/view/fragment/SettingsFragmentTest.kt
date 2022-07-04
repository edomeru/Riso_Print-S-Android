package jp.co.riso.smartdeviceapp.view.fragment

import android.content.pm.ActivityInfo
import android.view.KeyEvent
import android.view.View
import androidx.preference.PreferenceManager
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import jp.co.riso.android.util.AppUtils
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import jp.co.riso.smartprint.R
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.math.min

@LargeTest
class SettingsFragmentTest : BaseActivityTestUtil() {
    private var settingsFragment: SettingsFragment? = null

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
        settingsFragment = fragment as SettingsFragment?
    }

    @After
    fun cleanUp() {
        onView(withId(R.id.loginIdEditText)).perform(clearText())
        waitForAnimation()
    }

    @Test
    fun testNewInstance() {
        Assert.assertNotNull(settingsFragment)

        // check initial value
        val prefs = PreferenceManager.getDefaultSharedPreferences(mainActivity)
        onView(withId(R.id.loginIdEditText)).check(matches(withText(
            prefs.getString(
                AppConstants.PREF_KEY_LOGIN_ID,
                AppConstants.PREF_DEFAULT_LOGIN_ID
        ))))
    }

    @Test
    fun testKeyboardHide_MenuButton() {
        // show keyboard then hide via menu button
        onView(withId(R.id.loginIdEditText)).perform(click())
        waitForAnimation()
        Assert.assertTrue(isKeyboardOpen(settingsFragment!!))

        getViewInteractionFromMatchAtPosition(
            R.id.menu_id_action_button,
            0
        ).perform(click())
        waitForAnimation()
        Assert.assertFalse(isKeyboardOpen(settingsFragment!!))
    }

    @Test
    fun testKeyboardHide_ActionButton() {
        // show keyboard then hide via enter
        onView(withId(R.id.loginIdEditText)).perform(click())
        waitForAnimation()
        Assert.assertTrue(isKeyboardOpen(settingsFragment!!))

        onView(withId(R.id.loginIdEditText)).perform(pressImeActionButton())
        waitForAnimation()
        Assert.assertFalse(isKeyboardOpen(settingsFragment!!))
    }

    @Test
    fun testKeyboardHide_Enter() {
        // show keyboard then hide via enter
        onView(withId(R.id.loginIdEditText)).perform(click())
        waitForAnimation()
        Assert.assertTrue(isKeyboardOpen(settingsFragment!!))

        onView(withId(R.id.loginIdEditText)).perform(pressKey(KeyEvent.KEYCODE_ENTER))
        waitForAnimation()
        Assert.assertFalse(isKeyboardOpen(settingsFragment!!))
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

            val expectedWidth = min(
                AppUtils.getScreenDimensions(mainActivity)!!.x,
                AppUtils.getScreenDimensions(mainActivity)!!.y)

            mainActivity!!.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            waitForAnimation()
            Assert.assertEquals(
                expectedWidth,
                settingsFragment!!.view!!.findViewById<View>(R.id.rootView).width
            )

            mainActivity!!.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            waitForAnimation()
            Assert.assertEquals(
                expectedWidth,
                settingsFragment!!.view!!.findViewById<View>(R.id.rootView).width
            )

            mainActivity!!.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            waitForAnimation()
            Assert.assertEquals(
                expectedWidth,
                settingsFragment!!.view!!.findViewById<View>(R.id.rootView).width
            )

            mainActivity!!.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
            waitForAnimation()
            Assert.assertEquals(
                expectedWidth,
                settingsFragment!!.view!!.findViewById<View>(R.id.rootView).width
            )
        }
    }
}