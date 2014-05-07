package jp.co.riso.android.dialog;

import jp.co.riso.android.dialog.WaitingDialogFragment.WaitingDialogListener;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class WaitingDialogFragmentTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private static final String TAG = "WaitingDialogFragmentTest";
    private static final String TITLE = "title";
    private static final String MSG = "message";
    private static final String BUTTON_TITLE = "OK";

    private MainActivity mActivity;

    private boolean mCallbackCalled = false;

    public WaitingDialogFragmentTest() {
        super(MainActivity.class);
    }

    public WaitingDialogFragmentTest(Class<MainActivity> activityClass) {
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

    public void testNewInstance() {
        WaitingDialogFragment w = WaitingDialogFragment.newInstance(TITLE, MSG, true, BUTTON_TITLE) ;
        assertNotNull(w);
        w.show(mActivity.getFragmentManager(), TAG);
        getInstrumentation().waitForIdleSync();

        Fragment fragment = mActivity.getFragmentManager().findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        AlertDialog dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();

        assertNotNull(dialog);
        assertTrue(dialog.isShowing());
        assertTrue(((DialogFragment) fragment).isCancelable());

        View msg = dialog.findViewById(android.R.id.message);
        assertNotNull(msg);
        assertEquals(MSG, ((TextView) msg).getText());

        int titleId = mActivity.getResources().getIdentifier( "alertTitle", "id", "android" );
        assertFalse(titleId == 0);
        View title = dialog.findViewById(titleId);
        assertNotNull(title);
        assertEquals(TITLE, ((TextView) title).getText());

        Button b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        assertNotNull(b);
        assertEquals(BUTTON_TITLE, b.getText());

    }

    public void testOnCancelClicked() {
        WaitingDialogFragment w = WaitingDialogFragment.newInstance(TITLE, MSG, true, BUTTON_TITLE) ;
        assertNotNull(w);
        w.setTargetFragment(new MockCallback(), 1);
        w.show(mActivity.getFragmentManager(), TAG);
        getInstrumentation().waitForIdleSync();

        Fragment fragment = mActivity.getFragmentManager().findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        AlertDialog dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();

        assertNotNull(dialog);
        assertTrue(dialog.isShowing());
        assertTrue(((DialogFragment) fragment).isCancelable());

        Button b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        assertNotNull(b);
        assertEquals(BUTTON_TITLE, b.getText());

        try {
            performClick(b);
        } catch (Throwable e) {
            Log.d(TAG, e.getMessage());
        }

        assertNull(((DialogFragment) fragment).getDialog());
        assertNull(mActivity.getFragmentManager().findFragmentByTag(TAG));

        assertTrue(mCallbackCalled);

    }

    public void testOnCancelKey() {
        WaitingDialogFragment w = WaitingDialogFragment.newInstance(TITLE,MSG, true, BUTTON_TITLE) ;
        assertNotNull(w);
        w.setTargetFragment(new MockCallback(), 1);
        w.show(mActivity.getFragmentManager(), TAG);

        getInstrumentation().waitForIdleSync();


        Fragment fragment = mActivity.getFragmentManager().findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        AlertDialog dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();

        assertNotNull(dialog);
        assertTrue(dialog.isShowing());
        assertTrue(((DialogFragment) fragment).isCancelable());

        sendKeys(KeyEvent.KEYCODE_BACK);

        getInstrumentation().waitForIdleSync();

        assertNull(((DialogFragment) fragment).getDialog());
        assertNull(mActivity.getFragmentManager().findFragmentByTag(TAG));

        assertTrue(mCallbackCalled);
    }

    public void testNotCancelable() {
        WaitingDialogFragment w = WaitingDialogFragment.newInstance(TITLE, MSG, false, BUTTON_TITLE) ;
        assertNotNull(w);
        w.setTargetFragment(new MockCallback(), 1);
        w.show(mActivity.getFragmentManager(), TAG);
        getInstrumentation().waitForIdleSync();

        Fragment fragment = mActivity.getFragmentManager().findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        AlertDialog dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();

        assertNotNull(dialog);
        assertTrue(dialog.isShowing());
        assertFalse(((DialogFragment) fragment).isCancelable());

        Button b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        assertEquals("", b.getText());
        assertFalse(dialog.getButton(DialogInterface.BUTTON_NEGATIVE).isShown());

        sendKeys(KeyEvent.KEYCODE_BACK);
        getInstrumentation().waitForIdleSync();

        assertNotNull(((DialogFragment) fragment).getDialog());
        assertNotNull(mActivity.getFragmentManager().findFragmentByTag(TAG));

        assertFalse(mCallbackCalled);
    }

    public void testOnCancelOutside() {
        WaitingDialogFragment w = WaitingDialogFragment.newInstance(TITLE,MSG, true, BUTTON_TITLE) ;
        assertNotNull(w);
        w.setTargetFragment(new MockCallback(), 1);

        w.show(mActivity.getFragmentManager(), TAG);
        getInstrumentation().waitForIdleSync();

        Fragment fragment = mActivity.getFragmentManager().findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        AlertDialog dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();

        assertNotNull(dialog);
        assertTrue(dialog.isShowing());
        assertTrue(((DialogFragment) fragment).isCancelable());

        View msg = dialog.findViewById(android.R.id.message);
        assertNotNull(msg);
        assertEquals(MSG, ((TextView) msg).getText());

        int titleId = mActivity.getResources().getIdentifier( "alertTitle", "id", "android" );
        assertFalse(titleId == 0);
        View title = dialog.findViewById(titleId);
        assertNotNull(title);
        assertEquals(TITLE, ((TextView) title).getText());

        Button b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        assertNotNull(b);
        assertEquals(BUTTON_TITLE, b.getText());

        w.dismiss();
        getInstrumentation().waitForIdleSync();

        assertNull(((DialogFragment) fragment).getDialog());
        assertNull(mActivity.getFragmentManager().findFragmentByTag(TAG));

    }

    public void testOnCancelLater() {
        final WaitingDialogFragment w = WaitingDialogFragment.newInstance(TITLE,MSG, true, BUTTON_TITLE) ;
        assertNotNull(w);
        w.setTargetFragment(new MockCallback(), 1);
        w.show(mActivity.getFragmentManager(), TAG);
        getInstrumentation().waitForIdleSync();
        final Fragment dialog = mActivity.getFragmentManager().findFragmentByTag(TAG);
        assertTrue(dialog instanceof DialogFragment);
        assertTrue(((DialogFragment) dialog).getShowsDialog());
        assertTrue(((DialogFragment) dialog).getDialog() instanceof ProgressDialog);
        assertTrue(((DialogFragment) dialog).getDialog().isShowing());

        assertEquals(BUTTON_TITLE,((AlertDialog) w.getDialog()).getButton(DialogInterface.BUTTON_NEGATIVE).getText());
        assertNotNull(((AlertDialog) w.getDialog()).getButton(DialogInterface.BUTTON_NEGATIVE));

        try {
            dismisslater(w);
        } catch (Throwable e) {
            Log.d(TAG, e.getMessage());
        }


        assertNotNull(((DialogFragment) dialog).getDialog());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.d(TAG, e.getMessage());
        }
        getInstrumentation().waitForIdleSync();

        assertNull(((DialogFragment) dialog).getDialog());
        //        try {
        //            checkAfter((DialogFragment) dialog);
        //        } catch (Throwable e) {
        //            Log.d(TAG, e.getMessage());
        //        }


    }

    public void testChangeOrientation() {
        WaitingDialogFragment w = WaitingDialogFragment.newInstance(TITLE, MSG, true, BUTTON_TITLE) ;
        assertNotNull(w);
        w.show(mActivity.getFragmentManager(), TAG);
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

        Button b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        assertNotNull(b);

        assertEquals(BUTTON_TITLE, b.getText());

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

        b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        assertNotNull(b);

        assertEquals(BUTTON_TITLE, b.getText());

    }

    private void changeOrientation(){
        int orientation = mActivity.getResources().getConfiguration().orientation;
        int nextOrientation = orientation == Configuration.ORIENTATION_LANDSCAPE ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

        Instrumentation.ActivityMonitor monitor = new Instrumentation.ActivityMonitor(mActivity.getClass().getName(), null, false);
        getInstrumentation().addMonitor(monitor);
        mActivity.setRequestedOrientation(nextOrientation);
        getInstrumentation().waitForIdleSync();
        mActivity = (MainActivity) getInstrumentation().waitForMonitor(monitor);
    }

    private void performClick(final Button button) throws Throwable {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                button.performClick();
            }
        });
        getInstrumentation().waitForIdleSync();
    }

    private void dismisslater(final WaitingDialogFragment dialog) throws Throwable {

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.d(TAG, e.getMessage());
                }
                dialog.dismiss();
            }
        });

    }

    // for testing only
    @SuppressLint("ValidFragment")
    public class MockCallback extends Fragment implements WaitingDialogListener {

        @Override
        public void onCancel() {
            mCallbackCalled = true;
        }

    }

}
