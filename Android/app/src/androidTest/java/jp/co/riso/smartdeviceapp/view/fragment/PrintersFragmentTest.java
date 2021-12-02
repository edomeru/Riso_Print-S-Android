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

import jp.co.riso.smartdeviceapp.view.BaseMainActivityTest;
import jp.co.riso.smartprint.R;

public class PrintersFragmentTest extends BaseMainActivityTest {
    TestPrintersFragment mPrintersFragment = null;

    // sleep is needed because drawer calls goes through mHandler
    private void waitForDrawer() {
        SystemClock.sleep(1000);
    }

    @Before
    public void initPrintersFragment() {
        final FragmentManager fm = mActivity.getSupportFragmentManager();

        mActivity.runOnUiThread(() -> {
            fm.beginTransaction().add(R.id.mainLayout, new TestPrintersFragment()).commit();
            fm.executePendingTransactions();
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        Fragment printersFragment = fm.findFragmentById(R.id.mainLayout);
        assertTrue(printersFragment instanceof TestPrintersFragment);
        mPrintersFragment = (TestPrintersFragment) printersFragment;
    }

    @Test
    public void newInstance() {
        assertNotNull(mPrintersFragment);
    }

    @Test
    public void onClickAddPrinter() {
        testClick(R.id.menu_id_action_add_button);
        final FragmentManager fm = mActivity.getSupportFragmentManager();
        Fragment addPrinterFragment = fm.findFragmentById(R.id.mainLayout);
        assertTrue(addPrinterFragment instanceof AddPrinterFragment);

        testClick(R.id.menu_id_back_button);
        Fragment printersFragment = fm.findFragmentById(R.id.mainLayout);
        assertTrue(printersFragment instanceof PrintersFragment);
    }

    @Test
    public void onClickAddPrinterAsTablet() {
        mPrintersFragment.setAsTablet();
        testClick(R.id.menu_id_action_add_button);
        waitForDrawer();
        final FragmentManager fm = mActivity.getSupportFragmentManager();
        Fragment addPrinterFragment = fm.findFragmentById(R.id.rightLayout);
        assertTrue(addPrinterFragment instanceof AddPrinterFragment);
        assertTrue(mActivity.isDrawerOpen(Gravity.RIGHT));

        testClick(R.id.menu_id_action_add_button);
        waitForDrawer();
        assertFalse(mActivity.isDrawerOpen(Gravity.RIGHT));
    }

    @Test
    public void onClickSearchPrinter() {
        testClick(R.id.menu_id_action_search_button);
        final FragmentManager fm = mActivity.getSupportFragmentManager();
        Fragment searchPrintersFragment = fm.findFragmentById(R.id.mainLayout);
        assertTrue(searchPrintersFragment instanceof PrinterSearchFragment);

        testClick(R.id.menu_id_back_button);
        Fragment printersFragment = fm.findFragmentById(R.id.mainLayout);
        assertTrue(printersFragment instanceof PrintersFragment);
    }

    @Test
    public void onClickSearchPrinterAsTablet() {
        mPrintersFragment.setAsTablet();
        testClick(R.id.menu_id_action_search_button);
        waitForDrawer();
        final FragmentManager fm = mActivity.getSupportFragmentManager();
        Fragment searchPrintersFragment = fm.findFragmentById(R.id.rightLayout);
        assertTrue(searchPrintersFragment instanceof PrinterSearchFragment);
        assertTrue(mActivity.isDrawerOpen(Gravity.RIGHT));

        testClick(R.id.menu_id_action_search_button);
        waitForDrawer();
        assertFalse(mActivity.isDrawerOpen(Gravity.RIGHT));
    }

    @Test
    public void onClickPrinterSearchSettings() {
        testClick(R.id.menu_id_printer_search_settings_button);
        final FragmentManager fm = mActivity.getSupportFragmentManager();
        Fragment printerSearchSettingsFragment = fm.findFragmentById(R.id.mainLayout);
        assertTrue(printerSearchSettingsFragment instanceof PrinterSearchSettingsFragment);

        testClick(R.id.menu_id_back_button);
        Fragment printersFragment = fm.findFragmentById(R.id.mainLayout);
        assertTrue(printersFragment instanceof PrintersFragment);
    }

    @Test
    public void onClickPrinterSearchSettingsAsTablet() {
        mPrintersFragment.setAsTablet();
        testClick(R.id.menu_id_printer_search_settings_button);
        waitForDrawer();
        final FragmentManager fm = mActivity.getSupportFragmentManager();
        Fragment printerSearchSettingsFragment = fm.findFragmentById(R.id.rightLayout);
        assertTrue(printerSearchSettingsFragment instanceof PrinterSearchSettingsFragment);
        assertTrue(mActivity.isDrawerOpen(Gravity.RIGHT));

        testClick(R.id.menu_id_printer_search_settings_button);
        waitForDrawer();
        assertFalse(mActivity.isDrawerOpen(Gravity.RIGHT));
    }

    public static class TestPrintersFragment extends PrintersFragment {
        private boolean isTablet = false;
        public void setAsTablet() {
            isTablet = true;
        }
        @Override
        public boolean isTablet() {
            return isTablet;
        }
    }
}
