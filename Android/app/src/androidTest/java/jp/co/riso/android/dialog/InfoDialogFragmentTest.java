package jp.co.riso.android.dialog;

import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartprint.R;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class InfoDialogFragmentTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private static final String TAG = "InfoDialogFragmentTest";
    private static final int TITLE = R.string.ids_app_name;
    private static final int MSG = R.string.ids_app_name;
    private static final int BUTTON_TITLE = R.string.ids_lbl_ok;
    private MainActivity mActivity;

    public InfoDialogFragmentTest() {
        super(MainActivity.class);
    }

    public InfoDialogFragmentTest(Class<MainActivity> activityClass) {
        super(activityClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        wakeUpScreen();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testNewInstance_WithNull() {
        InfoDialogFragment info = InfoDialogFragment.newInstance(null, null);
        assertNotNull(info);
        info.show(mActivity.getSupportFragmentManager(), TAG);
        waitFewSeconds();

        Fragment fragment = mActivity.getSupportFragmentManager().findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        AlertDialog dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();

        assertNotNull(dialog);
        assertTrue(dialog.isShowing());
        info.dismissAllowingStateLoss();
    }

    public void testNewInstance_WithMessage() {
        InfoDialogFragment info = InfoDialogFragment.newInstance(SmartDeviceApp.getAppContext().getResources().getString(MSG),
                SmartDeviceApp.getAppContext().getResources().getString((BUTTON_TITLE)));
        assertNotNull(info);
        info.show(mActivity.getSupportFragmentManager(), TAG);
        waitFewSeconds();

        Fragment fragment = mActivity.getSupportFragmentManager().findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        AlertDialog dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();

        assertNotNull(dialog);
        assertTrue(dialog.isShowing());
        assertTrue(((DialogFragment) fragment).isCancelable());

        View msg = dialog.findViewById(android.R.id.message);
        assertNotNull(msg);
        assertEquals(SmartDeviceApp.getAppContext().getResources().getString(MSG), ((TextView) msg).getText());

        Button b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        assertNotNull(b);
        assertEquals(SmartDeviceApp.getAppContext().getResources().getString(BUTTON_TITLE), b.getText());
        info.dismissAllowingStateLoss();
    }

    public void testNewInstance_WithTitle() {
        InfoDialogFragment info = InfoDialogFragment.newInstance(SmartDeviceApp.getAppContext().getResources().getString(TITLE),
                SmartDeviceApp.getAppContext().getResources().getString(MSG),
                SmartDeviceApp.getAppContext().getResources().getString(BUTTON_TITLE));
        assertNotNull(info);
        info.show(mActivity.getSupportFragmentManager(), TAG);
        waitFewSeconds();

        Fragment fragment = mActivity.getSupportFragmentManager().findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        AlertDialog dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();

        assertNotNull(dialog);
        assertTrue(dialog.isShowing());
        assertTrue(((DialogFragment) fragment).isCancelable());

        View msg = dialog.findViewById(android.R.id.message);
        assertNotNull(msg);
        assertEquals(SmartDeviceApp.getAppContext().getResources().getString(MSG), ((TextView) msg).getText());

        int titleId = mActivity.getResources().getIdentifier("alertTitle", "id", "android");
        assertFalse(titleId == 0);
        View title = dialog.findViewById(titleId);
        assertNotNull(title);
        assertEquals(SmartDeviceApp.getAppContext().getResources().getString(TITLE), ((TextView) title).getText());

        Button b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        assertNotNull(b);
        assertEquals(SmartDeviceApp.getAppContext().getResources().getString(BUTTON_TITLE), b.getText());
        info.dismissAllowingStateLoss();
    }

    //================================================================================
    // Private methods
    //================================================================================

    // wait some seconds so that you can see the change on emulator/device.
    private void waitFewSeconds(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private void wakeUpScreen() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActivity.getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            }
        });

        waitFewSeconds();
    }
}