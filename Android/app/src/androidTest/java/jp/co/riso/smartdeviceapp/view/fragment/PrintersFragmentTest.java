package jp.co.riso.smartdeviceapp.view.fragment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.os.SystemClock;
import android.view.Gravity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;

import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil;
import jp.co.riso.smartprint.R;

public class PrintersFragmentTest extends BaseActivityTestUtil {
    PrintersFragment mPrintersFragment = null;

    // sleep is needed because drawer calls goes through mHandler
    private void waitForDrawer() {
        SystemClock.sleep(1000);
    }

    @Before
    public void initPrintersFragment() {
        final FragmentManager fm = mainActivity.getSupportFragmentManager();

        mainActivity.runOnUiThread(() -> {
            fm.beginTransaction().add(R.id.mainLayout, new PrintersFragment()).commit();
            fm.executePendingTransactions();
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        Fragment printersFragment = fm.findFragmentById(R.id.mainLayout);
        assertTrue(printersFragment instanceof PrintersFragment);
        mPrintersFragment = (PrintersFragment) printersFragment;
    }

    @Test
    public void testNewInstance() {
        assertNotNull(mPrintersFragment);
    }

    @Test
    public void testOnClick_AddPrinter() {
        testClick(R.id.menu_id_action_add_button);
        int layoutId = R.id.mainLayout;
        if (mPrintersFragment.isTablet()) {
            waitForDrawer();
            assertTrue(mainActivity.isDrawerOpen(Gravity.RIGHT));
            layoutId = R.id.rightLayout;
        }
        final FragmentManager fm = mainActivity.getSupportFragmentManager();
        Fragment addPrinterFragment = fm.findFragmentById(layoutId);
        assertTrue(addPrinterFragment instanceof AddPrinterFragment);

        if (mPrintersFragment.isTablet()) {
            testClick(R.id.menu_id_action_add_button);
            waitForDrawer();
            assertFalse(mainActivity.isDrawerOpen(Gravity.RIGHT));
        } else {
            testClick(R.id.menu_id_back_button);
            Fragment printersFragment = fm.findFragmentById(layoutId);
            assertTrue(printersFragment instanceof PrintersFragment);
        }
    }

    @Test
    public void testOnClick_SearchPrinter() {
        testClick(R.id.menu_id_action_search_button);
        int layoutId = R.id.mainLayout;
        if (mPrintersFragment.isTablet()) {
            waitForDrawer();
            assertTrue(mainActivity.isDrawerOpen(Gravity.RIGHT));
            layoutId = R.id.rightLayout;
        }
        final FragmentManager fm = mainActivity.getSupportFragmentManager();
        Fragment searchPrintersFragment = fm.findFragmentById(layoutId);
        assertTrue(searchPrintersFragment instanceof PrinterSearchFragment);

        if (mPrintersFragment.isTablet()) {
            testClick(R.id.menu_id_action_search_button);
            waitForDrawer();
            assertFalse(mainActivity.isDrawerOpen(Gravity.RIGHT));
        } else {
            testClick(R.id.menu_id_back_button);
            Fragment printersFragment = fm.findFragmentById(layoutId);
            assertTrue(printersFragment instanceof PrintersFragment);
        }
    }

    @Test
    public void testOnClick_PrinterSearchSettings() {
        testClick(R.id.menu_id_printer_search_settings_button);
        int layoutId = R.id.mainLayout;
        if (mPrintersFragment.isTablet()) {
            waitForDrawer();
            assertTrue(mainActivity.isDrawerOpen(Gravity.RIGHT));
            layoutId = R.id.rightLayout;
        }
        final FragmentManager fm = mainActivity.getSupportFragmentManager();
        Fragment printerSearchSettingsFragment = fm.findFragmentById(layoutId);
        assertTrue(printerSearchSettingsFragment instanceof PrinterSearchSettingsFragment);

        if (mPrintersFragment.isTablet()) {
            testClick(R.id.menu_id_printer_search_settings_button);
            waitForDrawer();
            assertFalse(mainActivity.isDrawerOpen(Gravity.RIGHT));
        } else {
            testClick(R.id.menu_id_back_button);
            Fragment printersFragment = fm.findFragmentById(layoutId);
            assertTrue(printersFragment instanceof PrintersFragment);
        }
    }
}
