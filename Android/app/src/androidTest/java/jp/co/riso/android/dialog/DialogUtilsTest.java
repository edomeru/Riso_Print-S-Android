
package jp.co.riso.android.dialog;

import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartprint.R;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import android.test.ActivityInstrumentationTestCase2;
import android.view.WindowManager;

public class DialogUtilsTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private static final String TAG = "DialogUtilsTest";
    private static final int MSG = R.string.ids_app_name;
    private static final int BUTTON_TITLE = R.string.ids_lbl_ok;

    public DialogUtilsTest() {
        super(MainActivity.class);
    }

    public DialogUtilsTest(Class<MainActivity> activityClass) {
        super(activityClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        wakeUpScreen();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testConstructor() {
        assertNotNull(new DialogUtils());
    }

    public void testDisplayDialog() {
        InfoDialogFragment d = InfoDialogFragment.newInstance(SmartDeviceApp.getAppContext().getResources().getString(MSG),
                SmartDeviceApp.getAppContext().getResources().getString(BUTTON_TITLE));
        DialogUtils.displayDialog(getActivity(), TAG, d);
        waitFewSeconds();
        Fragment dialog = getActivity().getSupportFragmentManager().findFragmentByTag(TAG);
        assertTrue(dialog instanceof DialogFragment);
        assertTrue(((DialogFragment) dialog).getShowsDialog());
        assertTrue(((DialogFragment) dialog).getDialog().isShowing());
    }

    public void testDismissDialog() {
        InfoDialogFragment d = InfoDialogFragment.newInstance(SmartDeviceApp.getAppContext().getResources().getString(MSG),
                SmartDeviceApp.getAppContext().getResources().getString(BUTTON_TITLE));
        DialogUtils.displayDialog(getActivity(), TAG, d);
        waitFewSeconds();
        Fragment dialog = getActivity().getSupportFragmentManager().findFragmentByTag(TAG);
        assertTrue(dialog instanceof DialogFragment);
        assertTrue(((DialogFragment) dialog).getShowsDialog());
        assertTrue(((DialogFragment) dialog).getDialog().isShowing());

        DialogUtils.dismissDialog(getActivity(), TAG);
        waitFewSeconds();
        assertNull(((DialogFragment) dialog).getDialog());

        dialog = getActivity().getSupportFragmentManager().findFragmentByTag(TAG);

        assertNull(dialog);
    }

    // ================================================================================
    // Private methods
    // ================================================================================

    // wait some seconds so that you can see the change on emulator/device.
    private void waitFewSeconds() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private void wakeUpScreen() {
        getActivity().runOnUiThread((Runnable) () -> getActivity().getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED));

        waitFewSeconds();
    }
}
