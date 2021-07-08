package com.scanlibrary;

import android.annotation.SuppressLint;
// aLINK edit - Start
// android.app.ProgressDialog was deprecated in API level 26
// Use a progress indicator like ProgressBar instead
import android.app.AlertDialog;
// aLINK edit - End
import android.app.Dialog;
// aLINK edit - Start
// android.app.DialogFragment was deprecated in API level 28
// Use androidx.fragment.app.DialogFragment instead
import androidx.fragment.app.DialogFragment;
// aLINK edit - End
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
// aLINK edit - Start
// android.app.ProgressDialog was deprecated in API level 26
// Use a progress indicator like ProgressBar instead
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
// aLINK edit - End

@SuppressLint("ValidFragment")
public class ProgressDialogFragment extends DialogFragment {

	public String message;

	public ProgressDialogFragment(String message) {
		this.message = message;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// aLINK edit - Start
		// android.app.ProgressDialog was deprecated in API level 26
		// Use a progress indicator like ProgressBar instead
		LinearLayout linearLayout = new LinearLayout(getContext());
		linearLayout.setOrientation(LinearLayout.HORIZONTAL);
		linearLayout.setGravity(Gravity.CENTER);
		LinearLayout.LayoutParams linearLayoutParam = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		linearLayoutParam.gravity = Gravity.CENTER;
		linearLayout.setLayoutParams(linearLayoutParam);

		linearLayoutParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		linearLayoutParam.gravity = Gravity.CENTER;

		TextView textView = new TextView(getContext());
		textView.setText(message);
		textView.setTextColor(getContext().getResources().getColor(android.R.color.background_dark, null));
		textView.setTextSize(20);
		textView.setLayoutParams(linearLayoutParam);

		ProgressBar progressBar = new ProgressBar(getContext());
		progressBar.setIndeterminate(true);
		progressBar.setLayoutParams(linearLayoutParam);

		linearLayout.addView(progressBar);
		linearLayout.addView(textView);

		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setCancelable(false);
		builder.setView(linearLayout);

		final AlertDialog dialog = builder.create();
		dialog.setMessage(message);
		dialog.setCanceledOnTouchOutside(false);
		// aLINK edit - End

		// Disable the back button
		OnKeyListener keyListener = (dialog1, keyCode, event) -> {

            if (keyCode == KeyEvent.KEYCODE_BACK) {
                return true;
            }
            return false;
        };
		dialog.setOnKeyListener(keyListener);
		return dialog;
	}
}