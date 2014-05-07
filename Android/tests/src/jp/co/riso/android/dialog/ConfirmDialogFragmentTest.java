package jp.co.riso.android.dialog;

import jp.co.riso.android.dialog.ConfirmDialogFragment.ConfirmDialogListener;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.Instrumentation;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
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
        mCallbackCalled = false;
    }

    public ConfirmDialogFragmentTest(Class<MainActivity> activityClass) {
        super(activityClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testNewInstanceWithMessage() {
        ConfirmDialogFragment c = ConfirmDialogFragment.newInstance(MSG, POSITIVE_BUTTON, NEGATIVE_BUTTON) ;
        assertNotNull(c);
        c.show(mActivity.getFragmentManager(), TAG);
        getInstrumentation().waitForIdleSync();
        Fragment fragment = mActivity.getFragmentManager().findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        AlertDialog dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();

        assertNotNull(dialog);
        assertTrue(dialog.isShowing());

        Button pos = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button neg = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        assertNotNull(pos);
        assertNotNull(neg);

        assertEquals(POSITIVE_BUTTON, pos.getText());
        assertEquals(NEGATIVE_BUTTON, neg.getText());
    }

    public void testNewInstanceWithTitle() {
        ConfirmDialogFragment c = ConfirmDialogFragment.newInstance(TITLE, MSG, POSITIVE_BUTTON, NEGATIVE_BUTTON) ;
        assertNotNull(c);
        c.show(mActivity.getFragmentManager(), TAG);
        getInstrumentation().waitForIdleSync();
        Fragment fragment = mActivity.getFragmentManager().findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        AlertDialog dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();

        assertNotNull(dialog);
        assertTrue(dialog.isShowing());

        View msg = dialog.findViewById(android.R.id.message);
        assertNotNull(msg);
        assertEquals(MSG, ((TextView) msg).getText());

        int titleId = mActivity.getResources().getIdentifier( "alertTitle", "id", "android" );
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


    }

    public void testOnClickPositive() {
        ConfirmDialogFragment c = ConfirmDialogFragment.newInstance(MSG, POSITIVE_BUTTON, NEGATIVE_BUTTON) ;
        assertNotNull(c);
        c.show(mActivity.getFragmentManager(), TAG);
        getInstrumentation().waitForIdleSync();
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

    public void testOnClickNegative() {
        ConfirmDialogFragment c = ConfirmDialogFragment.newInstance(MSG, POSITIVE_BUTTON, NEGATIVE_BUTTON) ;
        assertNotNull(c);
        c.show(mActivity.getFragmentManager(), TAG);
        getInstrumentation().waitForIdleSync();
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

    public void testOnClickPositiveListener() {
        ConfirmDialogFragment c = ConfirmDialogFragment.newInstance(MSG, POSITIVE_BUTTON, NEGATIVE_BUTTON) ;
        assertNotNull(c);
        c.setTargetFragment(new MockCallback(), 1);

        c.show(mActivity.getFragmentManager(), TAG);
        getInstrumentation().waitForIdleSync();

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

    public void testOnClickNegativeListener() {
        ConfirmDialogFragment c = ConfirmDialogFragment.newInstance(MSG, POSITIVE_BUTTON, NEGATIVE_BUTTON) ;
        assertNotNull(c);
        c.setTargetFragment(new MockCallback(), 1);
        c.show(mActivity.getFragmentManager(), TAG);

        getInstrumentation().waitForIdleSync();

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


    public void testChangeOrientation() {
        ConfirmDialogFragment c = ConfirmDialogFragment.newInstance(TITLE, MSG, POSITIVE_BUTTON, NEGATIVE_BUTTON) ;
        assertNotNull(c);
        c.show(mActivity.getFragmentManager(), TAG);
        getInstrumentation().waitForIdleSync();

        Fragment fragment = mActivity.getFragmentManager().findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        AlertDialog dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();

        assertNotNull(dialog);
        assertTrue(dialog.isShowing());

        View msg = dialog.findViewById(android.R.id.message);
        assertNotNull(msg);
        assertEquals(MSG, ((TextView) msg).getText());

        int titleId = mActivity.getResources().getIdentifier( "alertTitle", "id", "android" );
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

        changeOrientation();
        getInstrumentation().waitForIdleSync();


        //after rotation

        fragment = mActivity.getFragmentManager().findFragmentByTag(TAG);
        getInstrumentation().waitForIdleSync();
        assertNotNull(fragment);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();

        assertNotNull(dialog);
        assertTrue(dialog.isShowing());

        msg = dialog.findViewById(android.R.id.message);
        assertNotNull(msg);
        assertEquals(MSG, ((TextView) msg).getText());

        titleId = mActivity.getResources().getIdentifier( "alertTitle", "id", "android" );
        assertFalse(titleId == 0);
        title = dialog.findViewById(titleId);
        assertNotNull(title);
        assertEquals(TITLE, ((TextView) title).getText());

        pos = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        neg = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        assertNotNull(pos);
        assertNotNull(neg);

        assertEquals(POSITIVE_BUTTON, pos.getText());
        assertEquals(NEGATIVE_BUTTON, neg.getText());
    }

    public void testOnCancelKey() {
        ConfirmDialogFragment c = ConfirmDialogFragment.newInstance(TITLE, MSG, POSITIVE_BUTTON, NEGATIVE_BUTTON) ;
        assertNotNull(c);
        c.setTargetFragment(new MockCallback(), 1);
        c.show(getActivity().getFragmentManager(), TAG);

        getInstrumentation().waitForIdleSync();


        Fragment fragment = getActivity().getFragmentManager().findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        AlertDialog dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();

        assertNotNull(dialog);
        assertTrue(dialog.isShowing());
        assertTrue(((DialogFragment) fragment).isCancelable());

        sendKeys(KeyEvent.KEYCODE_BACK);

        getInstrumentation().waitForIdleSync();

        assertNull(((DialogFragment) fragment).getDialog());
        assertNull(getActivity().getFragmentManager().findFragmentByTag(TAG));

        assertTrue(mCallbackCalled);
    }


    private void performClick(final Button button) throws Throwable {
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                button.performClick();
            }
        });
        getInstrumentation().waitForIdleSync();
    }

    private void changeOrientation(){
        int orientation = mActivity.getResources().getConfiguration().orientation;
        int nextOrientation = orientation == Configuration.ORIENTATION_LANDSCAPE ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

        Instrumentation.ActivityMonitor monitor = new Instrumentation.ActivityMonitor(getActivity().getClass().getName(), null, false);
        getInstrumentation().addMonitor(monitor);
        getActivity().setRequestedOrientation(nextOrientation);
        getInstrumentation().waitForIdleSync();
        mActivity = (MainActivity) getInstrumentation().waitForMonitor(monitor);
    }

    private void checkCallbackCalled() {
        assertTrue(mCallbackCalled);
    }

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
