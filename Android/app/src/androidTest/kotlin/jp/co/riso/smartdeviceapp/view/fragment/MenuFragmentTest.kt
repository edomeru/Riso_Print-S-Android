package jp.co.riso.smartdeviceapp.view.fragment

import android.Manifest
import android.view.View
import androidx.fragment.app.Fragment
import androidx.test.espresso.intent.Intents
import androidx.test.rule.GrantPermissionRule
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import jp.co.riso.smartdeviceapp.view.fragment.MenuFragment.Companion.STATE_HOME
import jp.co.riso.smartprint.R
import org.junit.*

class MenuFragmentTest : BaseActivityTestUtil() {

    private var _menuFragment: MenuFragment? = null

    @get:Rule
    var storagePermission: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

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

    @Test
    fun testNewInstance() {
        Assert.assertNotNull(_menuFragment)
    }

    @Test
    fun testOnClick() {
        switchScreen(STATE_HOME)
        selectDocument(getUriFromPath(DOC_PDF))
        Assert.assertTrue(currentScreen is PrintPreviewFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.printPreviewButton))
        testClickAndWait(R.id.printersButton)
        Assert.assertTrue(currentScreen is PrintersFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.printersButton))
        testClick(R.id.printersButton)
        Assert.assertTrue(currentScreen is PrintersFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.printersButton))
        testClickAndWait(R.id.printJobsButton)
        Assert.assertTrue(currentScreen is PrintJobsFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.printJobsButton))
        testClick(R.id.printJobsButton)
        Assert.assertTrue(currentScreen is PrintJobsFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.printJobsButton))
        testClickAndWait(R.id.settingsButton)
        Assert.assertTrue(currentScreen is SettingsFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.settingsButton))
        testClick(R.id.settingsButton)
        Assert.assertTrue(currentScreen is SettingsFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.settingsButton))
        testClickAndWait(R.id.helpButton)
        Assert.assertTrue(currentScreen is HelpFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.helpButton))
        testClick(R.id.helpButton)
        Assert.assertTrue(currentScreen is HelpFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.helpButton))
        testClickAndWait(R.id.legalButton)
        Assert.assertTrue(currentScreen is LegalFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.legalButton))
        testClick(R.id.legalButton)
        Assert.assertTrue(currentScreen is LegalFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.legalButton))
        testClickAndWait(R.id.printPreviewButton)
        Assert.assertTrue(currentScreen is PrintPreviewFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.printPreviewButton))
        testClick(R.id.printPreviewButton)
        Assert.assertTrue(currentScreen is PrintPreviewFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.printPreviewButton))

        // HIDE_NEW_FEATURES when new features are hidden replace Home with PrintPreview
        testClickAndWait(R.id.homeButton)
        Assert.assertTrue(currentScreen is HomeFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.homeButton))
        testClick(R.id.homeButton)
        Assert.assertTrue(currentScreen is HomeFragment)
        Assert.assertTrue(isSelectedButtonCorrect(R.id.homeButton))
    }
}