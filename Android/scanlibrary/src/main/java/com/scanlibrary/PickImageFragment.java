package com.scanlibrary;

import android.app.Activity;
// aLINK edit - Start
// android.app.Fragment was deprecated in API level 28
// Use androidx.fragment.app.Fragment instead
import androidx.fragment.app.Fragment;

// androidx.fragment.app.Fragment.onAttach(Activity activity) is deprecated
// Use androidx.fragment.app.Fragment.onAttach(Context context) instead
import android.content.Context;
// aLINK edit - End
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jhansi on 04/04/15.
 */
public class PickImageFragment extends Fragment {

    private View view;
    private ImageButton cameraButton;
    private ImageButton galleryButton;
    private Uri fileUri;
    private IScanner scanner;
    private String imagePath;

    // aLINK edit - Start
    // androidx.fragment.app.Fragment.onAttach(Activity activity) is deprecated
    // Use androidx.fragment.app.Fragment.onAttach(Context context) instead
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof IScanner)) {
            throw new ClassCastException("Activity must implement IScanner");
        }
        this.scanner = (IScanner) context;
    }
    // aLINK edit - End

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.pick_image_fragment, null);
        init();
        return view;
    }

    private void init() {
        // aLINK edit - Start
        // getExternalStorageDirectory() is deprecated from Android Q.
        // https://developer.android.com/reference/android/os/Environment#getExternalStorageDirectory()
        // Temporarily use external cache directory
        imagePath = getActivity().getApplicationContext().getExternalCacheDir() + "/scanSample";
        // aLINK edit - End
        cameraButton = (ImageButton) view.findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new CameraButtonClickListener());
        galleryButton = (ImageButton) view.findViewById(R.id.selectButton);
        galleryButton.setOnClickListener(new GalleryClickListener());
        if (isIntentPreferenceSet()) {
            handleIntentPreference();
        } else {
            getActivity().finish();
        }
    }

    private void clearTempImages() {
        try {
//            File tempFolder = new File(ScanConstants.IMAGE_PATH);
            File tempFolder = new File(imagePath);
            for (File f : tempFolder.listFiles())
                f.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleIntentPreference() {
        int preference = getIntentPreference();
        if (preference == ScanConstants.OPEN_CAMERA) {
            openCamera();
        } else if (preference == ScanConstants.OPEN_MEDIA) {
            openMediaContent();
        }
    }

    private boolean isIntentPreferenceSet() {
        int preference = getArguments().getInt(ScanConstants.OPEN_INTENT_PREFERENCE, 0);
        return preference != 0;
    }

    private int getIntentPreference() {
        int preference = getArguments().getInt(ScanConstants.OPEN_INTENT_PREFERENCE, 0);
        return preference;
    }


    private class CameraButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            openCamera();
        }
    }

    private class GalleryClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            openMediaContent();
        }
    }

    public void openMediaContent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, ScanConstants.PICKFILE_REQUEST_CODE);
    }

    public void openCamera() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        File file = createImageFile();
        boolean isDirectoryCreated = file.getParentFile().mkdirs();
        Log.d("", "openCamera: isDirectoryCreated: " + isDirectoryCreated);
        // aLINK edit: Android 13 New OS Support: minimum SDK version is increased to 26
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            Uri tempFileUri = FileProvider.getUriForFile(getActivity().getApplicationContext(),
//                    "com.scanlibrary.provider", // As defined in Manifest
//                    file);
//            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempFileUri);
//        } else {
//            Uri tempFileUri = Uri.fromFile(file);
//            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempFileUri);
//        }
        Uri tempFileUri = FileProvider.getUriForFile(getActivity().getApplicationContext(),
                "com.scanlibrary.provider", // As defined in Manifest
                file);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempFileUri);
        startActivityForResult(cameraIntent, ScanConstants.START_CAMERA_REQUEST_CODE);
    }

    private File createImageFile() {
        clearTempImages();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new
                Date());
//        File file = new File(ScanConstants.IMAGE_PATH, "IMG_" + timeStamp +
//                ".jpg");
        File file = new File(imagePath, "IMG_" + timeStamp +
                ".jpg");
        fileUri = Uri.fromFile(file);
        return file;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("", "onActivityResult" + resultCode);
        Bitmap bitmap = null;
        if (resultCode == Activity.RESULT_OK) {
            try {
                switch (requestCode) {
                    case ScanConstants.START_CAMERA_REQUEST_CODE:
                        bitmap = getBitmap(fileUri);
                        break;

                    case ScanConstants.PICKFILE_REQUEST_CODE:
                        bitmap = getBitmap(data.getData());
                        break;
                }
                // aLINK edit: rotate image if needed
                bitmap = Utils.rotateImageIfRequired(getActivity(), bitmap, fileUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            getActivity().finish();
        }
        if (bitmap != null) {
            postImagePick(bitmap);
        }
    }

    protected void postImagePick(Bitmap bitmap) {
        Uri uri = Utils.getUri(getActivity(), bitmap);
        bitmap.recycle();
        scanner.onBitmapSelect(uri);
    }

    private Bitmap getBitmap(Uri selectedimg) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 3;
        AssetFileDescriptor fileDescriptor = null;
        fileDescriptor =
                getActivity().getContentResolver().openAssetFileDescriptor(selectedimg, "r");
        Bitmap original
                = BitmapFactory.decodeFileDescriptor(
                fileDescriptor.getFileDescriptor(), null, options);
        return original;
    }
}