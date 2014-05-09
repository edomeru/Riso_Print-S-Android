
package jp.co.riso.android.dialog;

import jp.co.riso.smartdeviceapp.view.MainActivity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.test.ActivityInstrumentationTestCase2;

public class DialogUtilsTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private static final String TAG = "DialogUtilsTest";
    private static final String MSG = "message";
    private static final String BUTTON_TITLE = "OK";

    public DialogUtilsTest() {
        super(MainActivity.class);
    }

    public DialogUtilsTest(Class<MainActivity> activityClass) {
        super(activityClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testConstructor() {
        assertNotNull(new DialogUtils());
    }
    
    public void testDisplayDialog() {
        InfoDialogFragment d = InfoDialogFragment.newInstance(MSG, BUTTON_TITLE);
        DialogUtils.displayDialog(getActivity(), TAG, d);
        waitFewSeconds();
        Fragment dialog = getActivity().getFragmentManager().findFragmentByTag(TAG);
        assertTrue(dialog instanceof DialogFragment);
        assertTrue(((DialogFragment) dialog).getShowsDialog());
        assertTrue(((DialogFragment) dialog).getDialog().isShowing());
    }

    public void testDismissDialog() {
        InfoDialogFragment d = InfoDialogFragment.newInstance(MSG, BUTTON_TITLE);
        DialogUtils.displayDialog(getActivity(), TAG, d);
        waitFewSeconds();
        Fragment dialog = getActivity().getFragmentManager().findFragmentByTag(TAG);
        assertTrue(dialog instanceof DialogFragment);
        assertTrue(((DialogFragment) dialog).getShowsDialog());
        assertTrue(((DialogFragment) dialog).getDialog().isShowing());

        DialogUtils.dismissDialog(getActivity(), TAG);
        waitFewSeconds();
        assertNull(((DialogFragment) dialog).getDialog());

        dialog = getActivity().getFragmentManager().findFragmentByTag(TAG);

        assertNull(dialog);
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
