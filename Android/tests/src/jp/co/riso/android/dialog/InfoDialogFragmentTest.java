package jp.co.riso.android.dialog;

import jp.co.riso.smartdeviceapp.view.MainActivity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.test.ActivityInstrumentationTestCase2;
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

    public void testNewInstance_WithNull() {
        InfoDialogFragment info = InfoDialogFragment.newInstance(null, null) ;
        assertNotNull(info);
        info.show(mActivity.getFragmentManager(), TAG);
        waitFewSeconds();

        Fragment fragment = mActivity.getFragmentManager().findFragmentByTag(TAG);
        assertTrue(fragment instanceof DialogFragment);
        assertTrue(((DialogFragment) fragment).getShowsDialog());

        AlertDialog dialog = (AlertDialog) ((DialogFragment) fragment).getDialog();

        assertNotNull(dialog);
        assertTrue(dialog.isShowing());
        info.dismissAllowingStateLoss();
    }

    public void testNewInstance_WithMessage() {
        InfoDialogFragment info = InfoDialogFragment.newInstance(MSG, BUTTON_TITLE) ;
        assertNotNull(info);
        info.show(mActivity.getFragmentManager(), TAG);
        waitFewSeconds();

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
        info.dismissAllowingStateLoss();
    }

    public void testNewInstance_WithTitle() {
        InfoDialogFragment info = InfoDialogFragment.newInstance(TITLE, MSG, BUTTON_TITLE) ;
        assertNotNull(info);
        info.show(mActivity.getFragmentManager(), TAG);
        waitFewSeconds();

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

        int titleId = mActivity.getResources().getIdentifier("alertTitle", "id", "android");
        assertFalse(titleId == 0);
        View title = dialog.findViewById(titleId);
        assertNotNull(title);
        assertEquals(TITLE, ((TextView) title).getText());

        Button b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        assertNotNull(b);
        assertEquals(BUTTON_TITLE, b.getText());
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
}