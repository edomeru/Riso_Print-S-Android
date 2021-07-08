package com.scanlibrary;

import android.app.Activity;
// aLINK edit - Start
// android.app.Fragment was deprecated in API level 28
// Use androidx.fragment.app.Fragment instead
import androidx.fragment.app.Fragment;
// android.app.FragmentManager was deprecated in API level 28
// Use androidx.fragment.app.FragmentManager instead
import androidx.fragment.app.FragmentManager;
// aLINK edit - End
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
// aLINK edit - Start
// android.os.AsyncTask was deprecated in API level 30.
// Use threading instead
// aLINK edit - End
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;

/**
 * Created by jhansi on 29/03/15.
 */
public class ResultFragment extends Fragment {

    private View view;
    private ImageView scannedImageView;
    private Button doneButton;
    private Bitmap original;
    private Button originalButton;
    private Button MagicColorButton;
    private Button grayModeButton;
    private Button bwButton;
    private Bitmap transformed;
    private static ProgressDialogFragment progressDialogFragment;

    public ResultFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.result_layout, null);
        init();
        return view;
    }

    private void init() {
        scannedImageView = view.findViewById(R.id.scannedImage);
        originalButton = view.findViewById(R.id.original);
        originalButton.setOnClickListener(new OriginalButtonClickListener());
        MagicColorButton = view.findViewById(R.id.magicColor);
        MagicColorButton.setOnClickListener(new MagicColorButtonClickListener());
        grayModeButton = view.findViewById(R.id.grayMode);
        grayModeButton.setOnClickListener(new GrayButtonClickListener());
        bwButton = view.findViewById(R.id.BWMode);
        bwButton.setOnClickListener(new BWButtonClickListener());
        Bitmap bitmap = getBitmap();
        setScannedImage(bitmap);
        doneButton = view.findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new DoneButtonClickListener());
    }

    private Bitmap getBitmap() {
        Uri uri = getUri();
        try {
            original = Utils.getBitmap(getActivity(), uri);
            getActivity().getContentResolver().delete(uri, null, null);
            return original;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Uri getUri() {
        Uri uri = getArguments().getParcelable(ScanConstants.SCANNED_RESULT);
        return uri;
    }

    public void setScannedImage(Bitmap scannedImage) {
        scannedImageView.setImageBitmap(scannedImage);
    }

    private class DoneButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            showProgressDialog(getResources().getString(R.string.loading));
            // aLINK edit - Start
            // android.os.AsyncTask was deprecated in API level 30.
            // Use threading instead
            new Thread(() -> {
                    try {
                        Intent data = new Intent();
                        Bitmap bitmap = transformed;
                        if (bitmap == null) {
                            bitmap = original;
                        }
                        Uri uri = Utils.getUri(getActivity(), bitmap);
                        data.putExtra(ScanConstants.SCANNED_RESULT, uri);
                        getActivity().setResult(Activity.RESULT_OK, data);
                        original.recycle();
                        System.gc();
                        getActivity().runOnUiThread((Runnable) () -> {
                            dismissDialog();
                            getActivity().finish();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            // aLINK edit - End
        }
    }

    private class BWButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            showProgressDialog(getResources().getString(R.string.applying_filter));
            // aLINK edit - Start
            // android.os.AsyncTask was deprecated in API level 30.
            // Use threading instead
            // aLINK edit - End
// aLINK edit - Start
// android.os.AsyncTask was deprecated in API level 30.
// Use threading instead
            new Thread(() -> {
                    try {
                        transformed = ((ScanActivity) getActivity()).getBWBitmap(original);
                    } catch (final OutOfMemoryError e) {
                        getActivity().runOnUiThread((Runnable) () -> {
                            transformed = original;
                            scannedImageView.setImageBitmap(original);
                            e.printStackTrace();
                            dismissDialog();
                            onClick(v);
                        });
                    }
                    getActivity().runOnUiThread((Runnable) () -> {
                        scannedImageView.setImageBitmap(transformed);
                        dismissDialog();
                    });
                }).start();
            // aLINK edit - End
        }
    }

    private class MagicColorButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            showProgressDialog(getResources().getString(R.string.applying_filter));
            // aLINK edit - Start
            // android.os.AsyncTask was deprecated in API level 30.
            // Use threading instead
            // aLINK edit - End
// aLINK edit - Start
// android.os.AsyncTask was deprecated in API level 30.
// Use threading instead
            new Thread(() -> {
                    try {
                        transformed = ((ScanActivity) getActivity()).getMagicColorBitmap(original);
                    } catch (final OutOfMemoryError e) {
                        getActivity().runOnUiThread((Runnable) () -> {
                            transformed = original;
                            scannedImageView.setImageBitmap(original);
                            e.printStackTrace();
                            dismissDialog();
                            onClick(v);
                        });
                    }
                    getActivity().runOnUiThread((Runnable) () -> {
                        scannedImageView.setImageBitmap(transformed);
                        dismissDialog();
                    });
                }).start();
            // aLINK edit - End
        }
    }

    private class OriginalButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            try {
                showProgressDialog(getResources().getString(R.string.applying_filter));
                transformed = original;
                scannedImageView.setImageBitmap(original);
                dismissDialog();
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                dismissDialog();
            }
        }
    }

    private class GrayButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            showProgressDialog(getResources().getString(R.string.applying_filter));
            // aLINK edit - Start
            // android.os.AsyncTask was deprecated in API level 30.
            // Use threading instead
            // aLINK edit - End
// aLINK edit - Start
// android.os.AsyncTask was deprecated in API level 30.
// Use threading instead
            new Thread(() -> {
                    try {
                        transformed = ((ScanActivity) getActivity()).getGrayBitmap(original);
                    } catch (final OutOfMemoryError e) {
                        getActivity().runOnUiThread((Runnable) () -> {
                            transformed = original;
                            scannedImageView.setImageBitmap(original);
                            e.printStackTrace();
                            dismissDialog();
                            onClick(v);
                        });
                    }
                    getActivity().runOnUiThread((Runnable) () -> {
                        scannedImageView.setImageBitmap(transformed);
                        dismissDialog();
                    });
                }).start();
            // aLINK edit - End
        }
    }

    protected synchronized void showProgressDialog(String message) {
        if (progressDialogFragment != null && progressDialogFragment.isVisible()) {
            // Before creating another loading dialog, close all opened loading dialogs (if any)
            progressDialogFragment.dismissAllowingStateLoss();
        }
        progressDialogFragment = null;
        progressDialogFragment = new ProgressDialogFragment(message);
        FragmentManager fm = getFragmentManager();
        progressDialogFragment.show(fm, ProgressDialogFragment.class.toString());
    }

    protected synchronized void dismissDialog() {
        progressDialogFragment.dismissAllowingStateLoss();
    }
}