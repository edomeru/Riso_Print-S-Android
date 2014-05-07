package jp.co.riso.android.dialog;

import jp.co.riso.smartdeviceapp.view.MainActivity;
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

public class InfoDialogFragmentTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private static final String TAG = "InfoDialogFragmentTest";
    private static final String TITLE = "title";
    private static final String MSG = "message";
    private static final String BUTTON_TITLE = "OK";
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
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testNewInstanceWithMessage() {
        InfoDialogFragment info = InfoDialogFragment.newInstance(MSG, BUTTON_TITLE) ;
        assertNotNull(info);
        info.show(mActivity.getFragmentManager(), TAG);
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

        Button b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        assertNotNull(b);
        assertEquals(BUTTON_TITLE, b.getText());

    }

    public void testNewInstanceWithTitle() {
        InfoDialogFragment info = InfoDialogFragment.newInstance(TITLE, MSG, BUTTON_TITLE) ;
        assertNotNull(info);
        info.show(mActivity.getFragmentManager(), TAG);
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

    public void testOnClick() {
        InfoDialogFragment info = InfoDialogFragment.newInstance(MSG, BUTTON_TITLE) ;
        assertNotNull(info);
        info.show(mActivity.getFragmentManager(), TAG);
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

    }


    public void testOnCancel() {
        InfoDialogFragment info = InfoDialogFragment.newInstance(MSG, BUTTON_TITLE) ;
        assertNotNull(info);
        info.show(mActivity.getFragmentManager(), TAG);
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


    public void testChangeOrientation() {
        InfoDialogFragment info = InfoDialogFragment.newInstance(TITLE, MSG, BUTTON_TITLE) ;
        assertNotNull(info);
        info.show(mActivity.getFragmentManager(), TAG);
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
}
