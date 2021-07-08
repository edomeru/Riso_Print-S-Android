package jp.co.riso.android.dialog;

import jp.co.riso.android.dialog.ConfirmDialogFragment.ConfirmDialogListener;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartprint.R;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class ConfirmDialogFragmentTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private static final String TAG = "ConfirmDialogFragmentTest";
    private static final int TITLE = R.string.ids_app_name;
    private static final int MSG = R.string.ids_app_name;
    private static final int POSITIVE_BUTTON = R.string.ids_lbl_ok;
    private static final int NEGATIVE_BUTTON = R.string.ids_lbl_cancel;

    private MainActivity mActivity;

    public ConfirmDialogFragmentTest() {
        super(MainActivity.class);

    }

    public ConfirmDialogFragmentTest(Class<MainActivity> activityClass) {
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
        ConfirmDialogFragment c = ConfirmDialogFragment.newInstance(null, null, null);
        assertNotNull(c);
        c.show(mActivity.getSupportFragmentManager(), TAG);
        waitFewSeconds();
        Fragment fragment = mActivity.getSupportFragmentManager().findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        AlertDialog dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();

        assertNotNull(dialog);
        assertTrue(dialog.isShowing());

        c.dismissAllowingStateLoss();
    }


    public void testNewInstance_WithMessage() {
        ConfirmDialogFragment c = ConfirmDialogFragment.newInstance(SmartDeviceApp.getAppContext().getResources().getString(MSG),
                SmartDeviceApp.getAppContext().getResources().getString(POSITIVE_BUTTON),
                SmartDeviceApp.getAppContext().getResources().getString(NEGATIVE_BUTTON));
        assertNotNull(c);
        c.show(mActivity.getSupportFragmentManager(), TAG);
        waitFewSeconds();
        Fragment fragment = mActivity.getSupportFragmentManager().findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        AlertDialog dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();

        assertNotNull(dialog);
        assertTrue(dialog.isShowing());

        View msg = dialog.findViewById(android.R.id.message);
        assertNotNull(msg);
        assertEquals(SmartDeviceApp.getAppContext().getResources().getString(MSG), ((TextView) msg).getText());

        Button pos = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button neg = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        assertNotNull(pos);
        assertNotNull(neg);

        assertEquals(SmartDeviceApp.getAppContext().getResources().getString(POSITIVE_BUTTON), pos.getText());
        assertEquals(SmartDeviceApp.getAppContext().getResources().getString(NEGATIVE_BUTTON), neg.getText());
        c.dismissAllowingStateLoss();
    }

    public void testNewInstance_WithTitle() {
        ConfirmDialogFragment c = ConfirmDialogFragment.newInstance(SmartDeviceApp.getAppContext().getResources().getString(TITLE),
                SmartDeviceApp.getAppContext().getResources().getString(MSG),
                SmartDeviceApp.getAppContext().getResources().getString(POSITIVE_BUTTON),
                SmartDeviceApp.getAppContext().getResources().getString(NEGATIVE_BUTTON));
        assertNotNull(c);
        c.show(mActivity.getSupportFragmentManager(), TAG);
        waitFewSeconds();
        Fragment fragment = mActivity.getSupportFragmentManager().findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        AlertDialog dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();

        assertNotNull(dialog);
        assertTrue(dialog.isShowing());

        View msg = dialog.findViewById(android.R.id.message);
        assertNotNull(msg);
        assertEquals(SmartDeviceApp.getAppContext().getResources().getString(MSG), ((TextView) msg).getText());

        int titleId = mActivity.getResources().getIdentifier("alertTitle", "id", "android");
        assertFalse(titleId == 0);
        View title = dialog.findViewById(titleId);
        assertNotNull(title);
        assertEquals(SmartDeviceApp.getAppContext().getResources().getString(TITLE), ((TextView) title).getText());

        Button pos = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button neg = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        assertNotNull(pos);
        assertNotNull(neg);

        assertEquals(SmartDeviceApp.getAppContext().getResources().getString(POSITIVE_BUTTON), pos.getText());
        assertEquals(SmartDeviceApp.getAppContext().getResources().getString(NEGATIVE_BUTTON), neg.getText());

        c.dismissAllowingStateLoss();
    }

    public void testOnClick_Positive() {
        ConfirmDialogFragment c = ConfirmDialogFragment.newInstance(SmartDeviceApp.getAppContext().getResources().getString(MSG),
                SmartDeviceApp.getAppContext().getResources().getString(POSITIVE_BUTTON),
                SmartDeviceApp.getAppContext().getResources().getString(NEGATIVE_BUTTON));
        assertNotNull(c);
        c.show(mActivity.getSupportFragmentManager(), TAG);
        waitFewSeconds();
        Fragment fragment = mActivity.getSupportFragmentManager().findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        AlertDialog dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();
        assertNotNull(dialog);
        assertTrue(dialog.isShowing());

        Button pos = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        assertNotNull(pos);

        try {
            performClick(pos);
        } catch (Throwable e) {
            Log.d(TAG, e.getMessage());
        }

        assertNull(((DialogFragment) fragment).getDialog());
        assertNull(mActivity.getFragmentManager().findFragmentByTag(TAG));
    }

    public void testOnClick_Negative() {
        ConfirmDialogFragment c = ConfirmDialogFragment.newInstance(SmartDeviceApp.getAppContext().getResources().getString(MSG),
                SmartDeviceApp.getAppContext().getResources().getString(POSITIVE_BUTTON),
                SmartDeviceApp.getAppContext().getResources().getString(NEGATIVE_BUTTON));
        assertNotNull(c);
        c.show(mActivity.getSupportFragmentManager(), TAG);
        waitFewSeconds();
        Fragment fragment = mActivity.getSupportFragmentManager().findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        AlertDialog dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();
        assertNotNull(dialog);
        assertTrue(dialog.isShowing());

        Button neg = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        assertNotNull(neg);

        try {
            performClick(neg);
        } catch (Throwable e) {
            Log.d(TAG, e.getMessage());
        }

        assertNull(((DialogFragment) fragment).getDialog());
        assertNull(mActivity.getFragmentManager().findFragmentByTag(TAG));

    }

    public void testOnClick_PositiveListener() {
        ConfirmDialogFragment c = ConfirmDialogFragment.newInstance(SmartDeviceApp.getAppContext().getResources().getString(MSG),
                SmartDeviceApp.getAppContext().getResources().getString(POSITIVE_BUTTON),
                SmartDeviceApp.getAppContext().getResources().getString(NEGATIVE_BUTTON));
        assertNotNull(c);

        MockCallback mockCallback = new MockCallback();
        mActivity.getSupportFragmentManager().beginTransaction().add(mockCallback, null).commit();
        c.setTargetFragment(mockCallback, 1);

        c.show(mActivity.getSupportFragmentManager(), TAG);
        waitFewSeconds();

        Fragment fragment = mActivity.getSupportFragmentManager().findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        AlertDialog dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();
        assertNotNull(dialog);
        assertTrue(dialog.isShowing());

        Button pos = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        assertNotNull(pos);

        try {
            performClick(pos);
        } catch (Throwable e) {
            Log.d(TAG, e.getMessage());
        }

        assertNull(((DialogFragment) fragment).getDialog());
        assertNull(mActivity.getSupportFragmentManager().findFragmentByTag(TAG));

        assertTrue(mockCallback.isConfirmCalled());
    }

    public void testOnClick_NegativeListener() {
        ConfirmDialogFragment c = ConfirmDialogFragment.newInstance(SmartDeviceApp.getAppContext().getResources().getString(MSG),
                SmartDeviceApp.getAppContext().getResources().getString(POSITIVE_BUTTON),
                SmartDeviceApp.getAppContext().getResources().getString(NEGATIVE_BUTTON));
        assertNotNull(c);

        MockCallback mockCallback = new MockCallback();
        mActivity.getSupportFragmentManager().beginTransaction().add(mockCallback, null).commit();
        c.setTargetFragment(mockCallback, 1);
        c.show(mActivity.getSupportFragmentManager(), TAG);

        waitFewSeconds();

        Fragment fragment = mActivity.getSupportFragmentManager().findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        AlertDialog dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();
        assertNotNull(dialog);
        assertTrue(dialog.isShowing());

        Button neg = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        assertNotNull(neg);

        try {
            performClick(neg);
        } catch (Throwable e) {
            Log.d(TAG, e.getMessage());
        }

        assertNull(((DialogFragment) fragment).getDialog());
        assertNull(mActivity.getSupportFragmentManager().findFragmentByTag(TAG));

        assertTrue(mockCallback.isCancelCalled());
    }

    public void testOnCancel() {
        ConfirmDialogFragment c = ConfirmDialogFragment.newInstance(SmartDeviceApp.getAppContext().getResources().getString(TITLE),
                SmartDeviceApp.getAppContext().getResources().getString(MSG),
                SmartDeviceApp.getAppContext().getResources().getString(POSITIVE_BUTTON),
                SmartDeviceApp.getAppContext().getResources().getString(NEGATIVE_BUTTON));
        assertNotNull(c);

        MockCallback mockCallback = new MockCallback();
        mActivity.getSupportFragmentManager().beginTransaction().add(mockCallback, null).commit();
        c.setTargetFragment(mockCallback, 1);
        c.show(getActivity().getSupportFragmentManager(), TAG);

        waitFewSeconds();

        Fragment fragment = getActivity().getSupportFragmentManager().findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        AlertDialog dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();

        assertNotNull(dialog);
        assertTrue(dialog.isShowing());
        assertTrue(((DialogFragment) fragment).isCancelable());

        sendKeys(KeyEvent.KEYCODE_BACK);

        waitFewSeconds();

        assertNull(((DialogFragment) fragment).getDialog());
        assertNull(getActivity().getSupportFragmentManager().findFragmentByTag(TAG));

        assertTrue(mockCallback.isCancelCalled());
    }

    public void testIsShowing_Null() {
        ConfirmDialogFragment c = ConfirmDialogFragment.newInstance(SmartDeviceApp.getAppContext().getResources().getString(MSG),
                SmartDeviceApp.getAppContext().getResources().getString(POSITIVE_BUTTON),
                SmartDeviceApp.getAppContext().getResources().getString(NEGATIVE_BUTTON));
        assertNotNull(c);
        assertFalse(c.isShowing());
    }

    public void testIsShowing_False() {
        ConfirmDialogFragment c = ConfirmDialogFragment.newInstance(SmartDeviceApp.getAppContext().getResources().getString(MSG),
                SmartDeviceApp.getAppContext().getResources().getString(POSITIVE_BUTTON),
                SmartDeviceApp.getAppContext().getResources().getString(NEGATIVE_BUTTON));
        assertNotNull(c);
        waitFewSeconds();
        assertFalse(c.isShowing());
    }

    public void testIsShowing_True() {
        ConfirmDialogFragment c = ConfirmDialogFragment.newInstance(SmartDeviceApp.getAppContext().getResources().getString(MSG),
                SmartDeviceApp.getAppContext().getResources().getString(POSITIVE_BUTTON),
                SmartDeviceApp.getAppContext().getResources().getString(NEGATIVE_BUTTON));
        assertNotNull(c);
        c.show(mActivity.getSupportFragmentManager(), TAG);
        waitFewSeconds();
        Fragment fragment = mActivity.getSupportFragmentManager().findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());
        assertTrue(c.isShowing());
    }

    //================================================================================
    // Private
    //================================================================================

    private void performClick(final Button button) throws Throwable {
        runTestOnUiThread((Runnable) () -> button.performClick());
        waitFewSeconds();
    }

    //================================================================================
    // Private methods
    //================================================================================

    // wait some seconds so that you can see the change on emulator/device.
    private void waitFewSeconds(){
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getInstrumentation().waitForIdleSync();
    }
    
    private void wakeUpScreen() {
        mActivity.runOnUiThread((Runnable) () -> mActivity.getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED));

        waitFewSeconds();
    }

    //================================================================================
    // Internal Classes
    //================================================================================

    // for testing only
    @SuppressLint("ValidFragment")
    public static class MockCallback extends Fragment implements ConfirmDialogListener {

        private boolean isConfirmCalled = false;
        private boolean isCancelCalled = false;

        public boolean isConfirmCalled() {
            return isConfirmCalled;
        }

        public boolean isCancelCalled() {
            return isCancelCalled;
        }

        @Override
        public void onConfirm() {
            isConfirmCalled = true;
        }

        @Override
        public void onCancel() {
            isCancelCalled = true;
        }
    }
}
