package jp.co.riso.android.dialog;

import jp.co.riso.android.dialog.ConfirmDialogFragment.ConfirmDialogListener;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ConfirmDialogFragmentTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private static final String TAG = "ConfirmDialogFragmentTest";
    private static final String TITLE = "title";
    private static final String MSG = "message";
    private static final String POSITIVE_BUTTON = "OK";
    private static final String NEGATIVE_BUTTON = "cancel";

    private MainActivity mActivity;

    private boolean mCallbackCalled;

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
        mCallbackCalled = false;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testNewInstance_WithNull() {
        ConfirmDialogFragment c = ConfirmDialogFragment.newInstance(null, null, null) ;
        assertNotNull(c);
        c.show(mActivity.getFragmentManager(), TAG);
        waitFewSeconds();
        Fragment fragment = mActivity.getFragmentManager().findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        AlertDialog dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();

        assertNotNull(dialog);
        assertTrue(dialog.isShowing());

        c.dismissAllowingStateLoss();
    }


    public void testNewInstance_WithMessage() {
        ConfirmDialogFragment c = ConfirmDialogFragment.newInstance(MSG, POSITIVE_BUTTON, NEGATIVE_BUTTON) ;
        assertNotNull(c);
        c.show(mActivity.getFragmentManager(), TAG);
        waitFewSeconds();
        Fragment fragment = mActivity.getFragmentManager().findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        AlertDialog dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();

        assertNotNull(dialog);
        assertTrue(dialog.isShowing());

        View msg = dialog.findViewById(android.R.id.message);
        assertNotNull(msg);
        assertEquals(MSG, ((TextView) msg).getText());

        Button pos = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button neg = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        assertNotNull(pos);
        assertNotNull(neg);

        assertEquals(POSITIVE_BUTTON, pos.getText());
        assertEquals(NEGATIVE_BUTTON, neg.getText());
        c.dismissAllowingStateLoss();
    }

    public void testNewInstance_WithTitle() {
        ConfirmDialogFragment c = ConfirmDialogFragment.newInstance(TITLE, MSG, POSITIVE_BUTTON, NEGATIVE_BUTTON) ;
        assertNotNull(c);
        c.show(mActivity.getFragmentManager(), TAG);
        waitFewSeconds();
        Fragment fragment = mActivity.getFragmentManager().findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        AlertDialog dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();

        assertNotNull(dialog);
        assertTrue(dialog.isShowing());

        View msg = dialog.findViewById(android.R.id.message);
        assertNotNull(msg);
        assertEquals(MSG, ((TextView) msg).getText());

        int titleId = mActivity.getResources().getIdentifier("alertTitle", "id", "android");
        assertFalse(titleId == 0);
        View title = dialog.findViewById(titleId);
        assertNotNull(title);
        assertEquals(TITLE, ((TextView) title).getText());

        Button pos = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button neg = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        assertNotNull(pos);
        assertNotNull(neg);

        assertEquals(POSITIVE_BUTTON, pos.getText());
        assertEquals(NEGATIVE_BUTTON, neg.getText());

        c.dismissAllowingStateLoss();
    }

    public void testOnClick_Positive() {
        ConfirmDialogFragment c = ConfirmDialogFragment.newInstance(MSG, POSITIVE_BUTTON, NEGATIVE_BUTTON) ;
        assertNotNull(c);
        c.show(mActivity.getFragmentManager(), TAG);
        waitFewSeconds();
        Fragment fragment = mActivity.getFragmentManager().findFragmentByTag(TAG);
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
        ConfirmDialogFragment c = ConfirmDialogFragment.newInstance(MSG, POSITIVE_BUTTON, NEGATIVE_BUTTON) ;
        assertNotNull(c);
        c.show(mActivity.getFragmentManager(), TAG);
        waitFewSeconds();
        Fragment fragment = mActivity.getFragmentManager().findFragmentByTag(TAG);
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
        ConfirmDialogFragment c = ConfirmDialogFragment.newInstance(MSG, POSITIVE_BUTTON, NEGATIVE_BUTTON) ;
        assertNotNull(c);
        c.setTargetFragment(new MockCallback(), 1);

        c.show(mActivity.getFragmentManager(), TAG);
        waitFewSeconds();

        Fragment fragment = mActivity.getFragmentManager().findFragmentByTag(TAG);
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

        checkCallbackCalled();
    }

    public void testOnClick_NegativeListener() {
        ConfirmDialogFragment c = ConfirmDialogFragment.newInstance(MSG, POSITIVE_BUTTON, NEGATIVE_BUTTON) ;
        assertNotNull(c);
        c.setTargetFragment(new MockCallback(), 1);
        c.show(mActivity.getFragmentManager(), TAG);

        waitFewSeconds();

        Fragment fragment = mActivity.getFragmentManager().findFragmentByTag(TAG);
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

        checkCallbackCalled();
    }

    public void testOnCancel() {
        ConfirmDialogFragment c = ConfirmDialogFragment.newInstance(TITLE, MSG, POSITIVE_BUTTON, NEGATIVE_BUTTON) ;
        assertNotNull(c);
        c.setTargetFragment(new MockCallback(), 1);
        c.show(getActivity().getFragmentManager(), TAG);

        waitFewSeconds();


        Fragment fragment = getActivity().getFragmentManager().findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        AlertDialog dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();

        assertNotNull(dialog);
        assertTrue(dialog.isShowing());
        assertTrue(((DialogFragment) fragment).isCancelable());

        sendKeys(KeyEvent.KEYCODE_BACK);

        waitFewSeconds();

        assertNull(((DialogFragment) fragment).getDialog());
        assertNull(getActivity().getFragmentManager().findFragmentByTag(TAG));

        assertTrue(mCallbackCalled);
    }


    //================================================================================
    // Private
    //================================================================================

    private void performClick(final Button button) throws Throwable {
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {

                button.performClick();
            }
        });
        waitFewSeconds();
    }

    //================================================================================
    // Private methods
    //================================================================================

    private void checkCallbackCalled() {
        assertTrue(mCallbackCalled);
    }

    // wait some seconds so that you can see the change on emulator/device.
    private void waitFewSeconds(){
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getInstrumentation().waitForIdleSync();
    }

    //================================================================================
    // Internal Classes
    //================================================================================

    // for testing only
    @SuppressLint("ValidFragment")
    public class MockCallback extends Fragment implements ConfirmDialogListener {
        @Override
        public void onConfirm() {
            mCallbackCalled = true;
        }

        @Override
        public void onCancel() {
            mCallbackCalled = true;
        }
    }
}
