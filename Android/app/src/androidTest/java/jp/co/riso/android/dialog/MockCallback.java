package jp.co.riso.android.dialog;

//================================================================================
// Internal Classes
//================================================================================

import android.annotation.SuppressLint;

import androidx.fragment.app.Fragment;

// for testing only
@SuppressLint("ValidFragment")
public class MockCallback extends Fragment
        implements  ConfirmDialogFragment.ConfirmDialogListener,
                    WaitingDialogFragment.WaitingDialogListener {

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