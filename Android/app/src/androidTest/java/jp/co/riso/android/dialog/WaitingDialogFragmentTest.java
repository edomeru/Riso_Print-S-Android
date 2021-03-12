
package jp.co.riso.android.dialog;

import jp.co.riso.android.dialog.WaitingDialogFragment.WaitingDialogListener;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartprint.R;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Build;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class WaitingDialogFragmentTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private static final String TAG = "WaitingDialogFragmentTest";
    private static final String TITLE = "title";
    private static final String MSG = "message";
    private static final String BUTTON_TITLE = "OK";

    private MainActivity mActivity;
    private FragmentManager fm;

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
        fm = mActivity.getFragmentManager();
        mCallbackCalled = false;
        
        wakeUpScreen();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testNewInstance_WithNull() {
        WaitingDialogFragment w = WaitingDialogFragment.newInstance(null, null, true, null);
        assertNotNull(w);
        w.show(fm, TAG);

        // wait some seconds so that you can see the change on emulator/device.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Fragment fragment = fm.findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        AlertDialog dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();
        assertNotNull(dialog);
        assertTrue(dialog.isShowing());
        assertTrue(((DialogFragment) fragment).isCancelable());

        w.dismissAllowingStateLoss();
    }

    public void testNewInstance_NotCancelable() {
        WaitingDialogFragment w = WaitingDialogFragment.newInstance(TITLE, MSG, false, BUTTON_TITLE);
        assertNotNull(w);
        w.setRetainInstance(true);
        w.show(fm, TAG);
        waitFewSeconds();

        Fragment fragment = fm.findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        AlertDialog dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();

        assertNotNull(dialog);
        assertTrue(dialog.isShowing());
        assertFalse(((DialogFragment) fragment).isCancelable());

        getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
        Button b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        assertEquals("", b.getText());
        assertFalse(dialog.getButton(DialogInterface.BUTTON_NEGATIVE).isShown());
        w.dismissAllowingStateLoss();
    }

    public void testOnClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Setting MockCallback as target fragment causes test app to crash in Android 8
            return;
        }

        WaitingDialogFragment w = WaitingDialogFragment.newInstance(TITLE, MSG, true, BUTTON_TITLE);
        assertNotNull(w);
        w.setTargetFragment(new MockCallback(), 1);
        w.show(fm, TAG);
        waitFewSeconds();

        Fragment fragment = fm.findFragmentByTag(TAG);
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
        assertNull(fm.findFragmentByTag(TAG));

        assertTrue(mCallbackCalled);

    }

    public void testOnCancel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Setting MockCallback as target fragment causes test app to crash in Android 8
            return;
        }

        WaitingDialogFragment w = WaitingDialogFragment.newInstance(TITLE, MSG, true, BUTTON_TITLE);
        assertNotNull(w);
        w.setTargetFragment(new MockCallback(), 1);
        w.show(fm, TAG);

        waitFewSeconds();

        Fragment fragment = fm.findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        AlertDialog dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();

        assertNotNull(dialog);
        assertTrue(dialog.isShowing());
        assertTrue(((DialogFragment) fragment).isCancelable());

        // back button
        getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
        waitFewSeconds();
        assertNull(((DialogFragment) fragment).getDialog());
        assertNull(fm.findFragmentByTag(TAG));

        assertTrue(mCallbackCalled);
    }

    public void testSetMessage() {
        WaitingDialogFragment w = WaitingDialogFragment.newInstance(TITLE, MSG, true, BUTTON_TITLE);
        assertNotNull(w);
        w.show(fm, TAG);

        waitFewSeconds();

        Fragment fragment = fm.findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        AlertDialog dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();

        assertNotNull(dialog);
        assertTrue(dialog.isShowing());
        assertTrue(((DialogFragment) fragment).isCancelable());

        w.setMessage("new message");
        waitFewSeconds();
        assertNotNull(((DialogFragment) fragment).getDialog());
        assertNotNull(fm.findFragmentByTag(TAG));

        View msg = dialog.findViewById(R.id.progressText);
        assertNotNull(msg);
        assertEquals("new message", ((TextView) msg).getText());

        w.dismissAllowingStateLoss();
    }

    public void testSetMessage_NullDialog() {
        WaitingDialogFragment w = WaitingDialogFragment.newInstance(TITLE, MSG, true, BUTTON_TITLE);
        assertNotNull(w);

        try {
            w.setMessage("New message before displaying the dialog");
        } catch (NullPointerException e) {
            fail("setMessage null");
        }
        
        waitFewSeconds();
        w.show(fm, TAG);

        waitFewSeconds();

        Fragment fragment = fm.findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        AlertDialog dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();

        assertNotNull(dialog);
        assertTrue(dialog.isShowing());
        assertTrue(((DialogFragment) fragment).isCancelable());
        View msg = dialog.findViewById(R.id.progressText);
        assertNotNull(msg);
        assertEquals(MSG, ((TextView) msg).getText());
        dialog.dismiss();

        waitFewSeconds();
        assertNull(((DialogFragment) fragment).getDialog());
        assertNull(fm.findFragmentByTag(TAG));

        try {
            w.setMessage("New message after closing the dialog");
        } catch (NullPointerException e) {
            fail("setMessage null");
            
        }

        w.dismissAllowingStateLoss();
    }

    
    public void testOnKey_NotBack() {
        WaitingDialogFragment w = WaitingDialogFragment.newInstance(TITLE, MSG, false, BUTTON_TITLE);
        assertNotNull(w);
        w.show(fm, TAG);
        waitFewSeconds();

        Fragment fragment = fm.findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        AlertDialog dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();

        assertNotNull(dialog);
        assertTrue(dialog.isShowing());
        assertFalse(((DialogFragment) fragment).isCancelable());

        getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_VOLUME_DOWN);

        waitFewSeconds();

        w.dismissAllowingStateLoss();
    }
    
    // ================================================================================
    // Private methods
    // ================================================================================

    private void performClick(final Button button) throws Throwable {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                button.performClick();
            }
        });
        waitFewSeconds();
    }

    // wait for a few seconds so that you can see the change on emulator/device.
    private void waitFewSeconds() {
        try {
            Thread.sleep(2000);
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

    // ================================================================================
    // Internal Class
    // ================================================================================

    // for testing only
    @SuppressLint("ValidFragment")
    public class MockCallback extends Fragment implements WaitingDialogListener {

        public MockCallback() {
        }

        @Override
        public void onCancel() {
            mCallbackCalled = true;
        }

    }

}
