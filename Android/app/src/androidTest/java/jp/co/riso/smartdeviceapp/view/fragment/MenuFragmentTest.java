package jp.co.riso.smartdeviceapp.view.fragment;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import org.junit.Before;
import org.junit.Test;

import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil;
import jp.co.riso.smartprint.R;

public class MenuFragmentTest extends BaseActivityTestUtil {
    private MenuFragment mMenuFragment = null;

    @Before
    public void initMenuFragment() {
        final FragmentManager fm = mActivity.getSupportFragmentManager();
        mMenuFragment = (MenuFragment) fm.findFragmentById(R.id.leftLayout);
    }

    private boolean isButtonSelected(int buttonId) {
        View button = mActivity.findViewById(buttonId);
        return (button != null) && button.isSelected() && !button.isClickable();
    }

    private boolean isSelectedButtonCorrect(int buttonId) {
        boolean isCorrect = isButtonSelected(buttonId);
        for (int id : MenuFragment.MENU_ITEMS) {
            if (id != buttonId) {
                isCorrect &= !isButtonSelected(id);
            }
        }
        return isCorrect;
    }

    private Fragment getCurrentScreen() {
        final FragmentManager fm = mActivity.getSupportFragmentManager();
        return fm.findFragmentById(R.id.mainLayout);
    }

    @Test
    public void testNewInstance() {
        assertNotNull(mMenuFragment);

        // HIDE_NEW_FEATURES when new features are returned, Home screen is default, else Print Preview screen
        assertTrue(getCurrentScreen() instanceof HomeFragment);
        assertTrue(isSelectedButtonCorrect(R.id.homeButton));
    }

    @Test
    public void testOnClick() {
        testClick(R.id.printersButton);
        assertTrue(getCurrentScreen() instanceof PrintersFragment);
        assertTrue(isSelectedButtonCorrect(R.id.printersButton));

        testClick(R.id.printersButton);
        assertTrue(getCurrentScreen() instanceof PrintersFragment);
        assertTrue(isSelectedButtonCorrect(R.id.printersButton));

        testClick(R.id.printJobsButton);
        assertTrue(getCurrentScreen() instanceof PrintJobsFragment);
        assertTrue(isSelectedButtonCorrect(R.id.printJobsButton));

        testClick(R.id.printJobsButton);
        assertTrue(getCurrentScreen() instanceof PrintJobsFragment);
        assertTrue(isSelectedButtonCorrect(R.id.printJobsButton));

        testClick(R.id.settingsButton);
        assertTrue(getCurrentScreen() instanceof SettingsFragment);
        assertTrue(isSelectedButtonCorrect(R.id.settingsButton));

        testClick(R.id.settingsButton);
        assertTrue(getCurrentScreen() instanceof SettingsFragment);
        assertTrue(isSelectedButtonCorrect(R.id.settingsButton));

        testClick(R.id.helpButton);
        assertTrue(getCurrentScreen() instanceof HelpFragment);
        assertTrue(isSelectedButtonCorrect(R.id.helpButton));

        testClick(R.id.helpButton);
        assertTrue(getCurrentScreen() instanceof HelpFragment);
        assertTrue(isSelectedButtonCorrect(R.id.helpButton));

        testClick(R.id.legalButton);
        assertTrue(getCurrentScreen() instanceof LegalFragment);
        assertTrue(isSelectedButtonCorrect(R.id.legalButton));

        testClick(R.id.legalButton);
        assertTrue(getCurrentScreen() instanceof LegalFragment);
        assertTrue(isSelectedButtonCorrect(R.id.legalButton));

        // HIDE_NEW_FEATURES when new features are hidden replace Home with PrintPreview
        testClick(R.id.homeButton);
        assertTrue(getCurrentScreen() instanceof HomeFragment);
        assertTrue(isSelectedButtonCorrect(R.id.homeButton));

        testClick(R.id.homeButton);
        assertTrue(getCurrentScreen() instanceof HomeFragment);
        assertTrue(isSelectedButtonCorrect(R.id.homeButton));

    }
}
