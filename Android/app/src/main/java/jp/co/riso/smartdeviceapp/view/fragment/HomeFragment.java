/*
 * Copyright (c) 2018 RISO, Inc. All rights reserved.
 *
 * HomeFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import jp.co.riso.android.dialog.ConfirmDialogFragment;
import jp.co.riso.android.dialog.DialogUtils;
import jp.co.riso.android.dialog.InfoDialogFragment;
import jp.co.riso.android.util.FileUtils;
import jp.co.riso.android.util.ImageUtils;
import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.view.PDFHandlerActivity;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartprint.R;

import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

/**
 * @class HomeFragment
 *
 * @brief Fragment which contains the Home Screen
 */
public class HomeFragment extends BaseFragment implements View.OnClickListener, ConfirmDialogFragment.ConfirmDialogListener {

    // flags for file types picked
    public static final int PDF_FROM_PICKER = 0;
    public static final int TEXT_FROM_PICKER = -1;
    public static final int IMAGE_FROM_PICKER = -2;
    public static final int IMAGES_FROM_PICKER = -3;
    public static final int IMAGE_FROM_CAMERA = -4;

    public static final String FRAGMENT_TAG_DIALOG = "file_error_dialog";
    private final int REQUEST_FILE = 1;
    private final int REQUEST_PHOTO = 2;
    private final int REQUEST_CAMERA = 3;
    private final int REQUEST_WRITE_EXTERNAL_STORAGE = 4;
    private final int REQUEST_CAMERA_STORAGE_PERMISSION = 5;

    private LinearLayout homeButtons;
    private LinearLayout fileButton, photosButton, cameraButton;

    private static final String TAG_PERMISSION_DIALOG = "external_storage_tag";
    private ConfirmDialogFragment mConfirmDialogFragment = null;
    private LinearLayout buttonTapped = null;
    // to prevent double tap
    private long lastClickTime = 0;

    private boolean checkPermission = false;

    @Override
    public int getViewLayout() {
        return R.layout.fragment_home;
    }

    @Override
    public void initializeFragment(Bundle savedInstanceState) {

    }

    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        homeButtons = view.findViewById(R.id.homeButtons);
        setOnClickListeners(view);
    }

    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_home);

        addActionMenuButton(view);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.fileButton:
                buttonTapped = fileButton;
                checkPermission = checkPermission(true);
                if (checkPermission && SystemClock.elapsedRealtime() - lastClickTime > 1000) {
                    // prevent double tap
                    lastClickTime = SystemClock.elapsedRealtime();
                    Intent filePickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    filePickerIntent.setType("*/*");
                    filePickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, AppConstants.DOC_TYPES);
                    startActivityForResult(Intent.createChooser(filePickerIntent, getString(R.string.ids_lbl_select_document)), REQUEST_FILE);
                }
                break;
            case R.id.photosButton:
                buttonTapped = photosButton;
                checkPermission = checkPermission(true);
                if (checkPermission && SystemClock.elapsedRealtime() - lastClickTime > 1000) {
                    // prevent double tap
                    lastClickTime = SystemClock.elapsedRealtime();
                    Intent photosPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    photosPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    photosPickerIntent.setType("*/*");
                    photosPickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, AppConstants.IMAGE_TYPES);
                    startActivityForResult(Intent.createChooser(photosPickerIntent, getString(R.string.ids_lbl_select_photos)), REQUEST_PHOTO);
                }
                break;
            case R.id.cameraButton:
                buttonTapped = cameraButton;
                checkPermission = checkPermission(false);
                if (checkPermission && SystemClock.elapsedRealtime() - lastClickTime > 1000) {
                    // prevent double tap
                    lastClickTime = SystemClock.elapsedRealtime();
                    Intent intent = new Intent(getActivity(), ScanActivity.class);
                    intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, ScanConstants.OPEN_CAMERA);
                    startActivityForResult(intent, REQUEST_CAMERA);
                }
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        LinearLayout mainView = getView().findViewById(R.id.contentView);
        mainView.removeView(homeButtons);

        LinearLayout newView = (LinearLayout)View.inflate(getActivity(), R.layout.home_buttons, null);
        newView.setLayoutParams(homeButtons.getLayoutParams());

        mainView.addView(newView);
        homeButtons = newView;
        setOnClickListeners(homeButtons);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_FILE && resultCode == Activity.RESULT_OK && data != null) {
            String contentType = FileUtils.getMimeType(getActivity(), data.getData());
            if (contentType == null || Arrays.asList(AppConstants.DOC_TYPES).indexOf(contentType) == -1) {
                String message = getResources().getString(R.string.ids_err_msg_open_failed);
                String button = getResources().getString(R.string.ids_lbl_ok);
                DialogUtils.displayDialog(getActivity(), FRAGMENT_TAG_DIALOG, InfoDialogFragment.newInstance(message, button));
            } else {
                int fileType = contentType.equals(AppConstants.DOC_TYPES[0]) ? PDF_FROM_PICKER : TEXT_FROM_PICKER;
                openFile(data.getData(), null, fileType);
            }
        }  else if (requestCode == REQUEST_PHOTO && resultCode == Activity.RESULT_OK && data != null) {
            if (data.getClipData() != null) { // multiple image files
                if (!ImageUtils.isImageFileSupported(getActivity(), data.getClipData())) {
                    String message = getResources().getString(R.string.ids_err_msg_open_failed);
                    String button = getResources().getString(R.string.ids_lbl_ok);
                    DialogUtils.displayDialog(getActivity(), FRAGMENT_TAG_DIALOG, InfoDialogFragment.newInstance(message, button));
                } else {
                    openFile(null, data.getClipData(), IMAGES_FROM_PICKER);
                }
            } else {    // single image file
                if (!ImageUtils.isImageFileSupported(getActivity(), data.getData())) {
                    String message = getResources().getString(R.string.ids_err_msg_open_failed);
                    String button = getResources().getString(R.string.ids_lbl_ok);
                    DialogUtils.displayDialog(getActivity(), FRAGMENT_TAG_DIALOG, InfoDialogFragment.newInstance(message, button));
                } else {
                    openFile(data.getData(), null, IMAGE_FROM_PICKER);
                }
            }
        } else if (requestCode == REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {
            Uri imageUri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
            Intent previewIntent = new Intent(getActivity(), PDFHandlerActivity.class);
            previewIntent.setAction(Intent.ACTION_VIEW);
            previewIntent.putExtra(AppConstants.EXTRA_FILE_FROM_PICKER, HomeFragment.IMAGE_FROM_CAMERA);
            previewIntent.setData(imageUri);

            previewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(previewIntent);
        }
    }

    private void setOnClickListeners(View view) {
        fileButton = view.findViewById(R.id.fileButton);
        photosButton = view.findViewById(R.id.photosButton);
        cameraButton = view.findViewById(R.id.cameraButton);

        fileButton.setOnClickListener(this);
        photosButton.setOnClickListener(this);
        cameraButton.setOnClickListener(this);
    }

    private boolean checkPermission(boolean isStorageOnly) {
        if (isStorageOnly && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (mConfirmDialogFragment == null) {
                        final String message = getActivity().getString(R.string.ids_err_msg_storage_permission_not_allowed);
                        final String positiveButton = getActivity().getString(R.string.ids_lbl_ok);
                        mConfirmDialogFragment = ConfirmDialogFragment.newInstance(message, positiveButton, null);
                        mConfirmDialogFragment.setTargetFragment(HomeFragment.this, 0);
                        DialogUtils.displayDialog(getActivity(), TAG_PERMISSION_DIALOG, mConfirmDialogFragment);
                    }
                } else {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
                }
            }
            return false;
        } else if (!isStorageOnly && (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) || shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (mConfirmDialogFragment == null) {
                        final String message = getActivity().getString(R.string.ids_err_msg_camera_permission_not_allowed);
                        final String positiveButton = getActivity().getString(R.string.ids_lbl_ok);
                        mConfirmDialogFragment = ConfirmDialogFragment.newInstance(message, positiveButton, null);
                        mConfirmDialogFragment.setTargetFragment(HomeFragment.this, 0);
                        DialogUtils.displayDialog(getActivity(), TAG_PERMISSION_DIALOG, mConfirmDialogFragment);
                    }
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_STORAGE_PERMISSION);
                }
            }
            return false;
        }
        return true;
    }

    /**
     * @brief Opens file in app
     *
     * @param data File uri
     * @param clipData Clip data for multiple selected files
     * @param fileType File type
     */
    private void openFile(Uri data, ClipData clipData, int fileType) {
        Intent intent = new Intent(getActivity(), PDFHandlerActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra(AppConstants.EXTRA_FILE_FROM_PICKER, fileType);

        if (fileType == IMAGES_FROM_PICKER) {
            intent.setClipData(clipData);
        } else {
            intent.setData(data);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, start picker intent
                    if (buttonTapped != null) {
                        buttonTapped.performClick();
                    }
                }

                if(!checkPermission && !shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    String message = getResources().getString(R.string.ids_err_msg_write_external_storage_permission_not_granted);
                    String button = getResources().getString(R.string.ids_lbl_ok);
                    DialogUtils.displayDialog(getActivity(), FRAGMENT_TAG_DIALOG, InfoDialogFragment.newInstance(message, button));
                }
                break;
            case REQUEST_CAMERA_STORAGE_PERMISSION:
                // camera(0) and storage(1) permission
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, start picker intent
                    if (buttonTapped != null) {
                        buttonTapped.performClick();
                    }
                }

                if(!checkPermission && (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) &&
                        !shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE))){
                    String message = getResources().getString(R.string.ids_err_msg_camera_permission_not_granted);
                    String button = getResources().getString(R.string.ids_lbl_ok);
                    DialogUtils.displayDialog(getActivity(), FRAGMENT_TAG_DIALOG, InfoDialogFragment.newInstance(message, button));
                }
                break;
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onConfirm() {
        mConfirmDialogFragment = null;
        if (buttonTapped == this.cameraButton) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_STORAGE_PERMISSION);
        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onCancel() {
        mConfirmDialogFragment = null;
    }
}

