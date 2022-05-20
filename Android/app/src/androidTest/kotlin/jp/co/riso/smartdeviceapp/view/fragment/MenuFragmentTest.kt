package jp.co.riso.smartdeviceapp.view.fragment

import android.view.View
import androidx.fragment.app.Fragment
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import org.junit.Before
import jp.co.riso.smartprint.R
import org.junit.Assert
import org.junit.Test

class MenuFragmentTest : BaseActivityTestUtil() {

    private var _menuFragment: MenuFragment? = null

    @Before
    fun initMenuFragment() {
        val fm = mainActivity!!.supportFragmentManager
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

    @Test
    fun testNewInstance() {
        Assert.assertNotNull(_menuFragment)

        // HIDE_NEW_FEATURES when new features are returned, Home screen is default, else Print Preview screen
        Assert.assertTrue(currentScreen is HomeFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.homeButton))
    }

    @Test
    fun testOnClick() {
        testClick(R.id.printersButton)
        Assert.assertTrue(currentScreen is PrintersFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.printersButton))
        testClick(R.id.printersButton)
        Assert.assertTrue(currentScreen is PrintersFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.printersButton))
        testClick(R.id.printJobsButton)
        Assert.assertTrue(currentScreen is PrintJobsFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.printJobsButton))
        testClick(R.id.printJobsButton)
        Assert.assertTrue(currentScreen is PrintJobsFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.printJobsButton))
        testClick(R.id.settingsButton)
        Assert.assertTrue(currentScreen is SettingsFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.settingsButton))
        testClick(R.id.settingsButton)
        Assert.assertTrue(currentScreen is SettingsFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.settingsButton))
        testClick(R.id.helpButton)
        Assert.assertTrue(currentScreen is HelpFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.helpButton))
        testClick(R.id.helpButton)
        Assert.assertTrue(currentScreen is HelpFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.helpButton))
        testClick(R.id.legalButton)
        Assert.assertTrue(currentScreen is LegalFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.legalButton))
        testClick(R.id.legalButton)
        Assert.assertTrue(currentScreen is LegalFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.legalButton))

        // HIDE_NEW_FEATURES when new features are hidden replace Home with PrintPreview
        testClick(R.id.homeButton)
        Assert.assertTrue(currentScreen is HomeFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.homeButton))
        testClick(R.id.homeButton)
        Assert.assertTrue(currentScreen is HomeFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.homeButton))
    }
}