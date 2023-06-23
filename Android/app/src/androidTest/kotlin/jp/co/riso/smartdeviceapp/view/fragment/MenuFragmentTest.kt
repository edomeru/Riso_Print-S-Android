package jp.co.riso.smartdeviceapp.view.fragment

import android.Manifest
import android.os.Build
import android.view.View
import androidx.fragment.app.Fragment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import jp.co.riso.smartdeviceapp.view.fragment.MenuFragment.Companion.STATE_HOME
import jp.co.riso.smartprint.R
import org.junit.*

class MenuFragmentTest : BaseActivityTestUtil() {

    private var _menuFragment: MenuFragment? = null

    @get:Rule
    var storagePermission: GrantPermissionRule = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        GrantPermissionRule.grant(Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    @Before
    fun setUp() {
        Intents.init()
        wakeUpScreen()
        initFragment()
    }

    @After
    fun cleanUp() {
        Intents.release()
    }

    private fun initFragment() {
        val fm = mainActivity!!.supportFragmentManager
        mainActivity!!.runOnUiThread {
            fm.beginTransaction().add(R.id.mainLayout, HomeFragment()).commit()
            fm.executePendingTransactions()
        }
        _menuFragment = fm.findFragmentById(R.id.leftLayout) as MenuFragment?
    }

    private fun isButtonSelected(buttonId: Int): Boolean {
        val button = mainActivity!!.findViewById<View>(buttonId)
        return button != null && button.isSelected && !button.isClickable
    }

    private fun isSelectedButtonCorrect(buttonId: Int): Boolean {
        var isCorrect = isButtonSelected(buttonId)
        for (id in MenuFragment.MENU_ITEMS) {
            if (id != buttonId) {
                isCorrect = isCorrect and !isButtonSelected(id)
            }
        }
        return isCorrect
    }

    private val currentScreen: Fragment?
        get() {
            val fm = mainActivity!!.supportFragmentManager
            return fm.findFragmentById(R.id.mainLayout)
        }

    private fun clickMenuButton() {
        getViewInteractionFromMatchAtPosition(
            R.id.menu_id_action_button,
            0
        ).perform(click())
        waitForAnimation()
    }

    @Test
    fun testNewInstance() {
        Assert.assertNotNull(_menuFragment)
    }

    @Test
    fun testOnClick() {

        // Load PDF in Preview screen
        clickMenuButton()
        switchScreen(STATE_HOME)
        selectDocument(getUriFromPath(DOC_PDF))
        Assert.assertTrue(currentScreen is PrintPreviewFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.printPreviewButton))

        clickMenuButton()
        onView(withId(R.id.leftLayout)).check(matches(isDisplayed()))
        testClickAndWait(R.id.printersButton)
        Assert.assertTrue(currentScreen is PrintersFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.printersButton))
        clickMenuButton()
        onView(withId(R.id.leftLayout)).check(matches(isDisplayed()))
        testClickAndWait(R.id.printersButton)
        Assert.assertTrue(currentScreen is PrintersFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.printersButton))

        clickMenuButton()
        onView(withId(R.id.leftLayout)).check(matches(isDisplayed()))
        testClickAndWait(R.id.printJobsButton)
        Assert.assertTrue(currentScreen is PrintJobsFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.printJobsButton))
        clickMenuButton()
        onView(withId(R.id.leftLayout)).check(matches(isDisplayed()))
        testClickAndWait(R.id.printJobsButton)
        Assert.assertTrue(currentScreen is PrintJobsFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.printJobsButton))

        clickMenuButton()
        onView(withId(R.id.leftLayout)).check(matches(isDisplayed()))
        testClickAndWait(R.id.settingsButton)
        Assert.assertTrue(currentScreen is SettingsFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.settingsButton))
        clickMenuButton()
        onView(withId(R.id.leftLayout)).check(matches(isDisplayed()))
        testClickAndWait(R.id.settingsButton)
        Assert.assertTrue(currentScreen is SettingsFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.settingsButton))

        clickMenuButton()
        onView(withId(R.id.leftLayout)).check(matches(isDisplayed()))
        testClickAndWait(R.id.helpButton)
        Assert.assertTrue(currentScreen is HelpFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.helpButton))
        clickMenuButton()
        onView(withId(R.id.leftLayout)).check(matches(isDisplayed()))
        testClickAndWait(R.id.helpButton)
        Assert.assertTrue(currentScreen is HelpFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.helpButton))

        clickMenuButton()
        onView(withId(R.id.leftLayout)).check(matches(isDisplayed()))
        testClickAndWait(R.id.legalButton)
        Assert.assertTrue(currentScreen is LegalFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.legalButton))
        clickMenuButton()
        onView(withId(R.id.leftLayout)).check(matches(isDisplayed()))
        testClickAndWait(R.id.legalButton)
        Assert.assertTrue(currentScreen is LegalFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.legalButton))

        clickMenuButton()
        onView(withId(R.id.leftLayout)).check(matches(isDisplayed()))
        testClickAndWait(R.id.printPreviewButton)
        Assert.assertTrue(currentScreen is PrintPreviewFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.printPreviewButton))
        clickMenuButton()
        onView(withId(R.id.leftLayout)).check(matches(isDisplayed()))
        testClickAndWait(R.id.printPreviewButton)
        Assert.assertTrue(currentScreen is PrintPreviewFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.printPreviewButton))

        // HIDE_NEW_FEATURES when new features are hidden replace Home with PrintPreview
        clickMenuButton()
        onView(withId(R.id.leftLayout)).check(matches(isDisplayed()))
        testClickAndWait(R.id.homeButton)
        Assert.assertTrue(currentScreen is HomeFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.homeButton))
        clickMenuButton()
        onView(withId(R.id.leftLayout)).check(matches(isDisplayed()))
        testClickAndWait(R.id.homeButton)
        Assert.assertTrue(currentScreen is HomeFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.homeButton))
    }
}