package jp.co.riso.smartdeviceapp.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import jp.co.riso.android.util.ImageUtils;
import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.view.base.BaseActivity;
import jp.co.riso.smartdeviceapp.view.fragment.HomeFragment;
import jp.co.riso.smartprint.R;

public class ImageCapturedActivity extends BaseActivity implements View.OnClickListener {

    private ImageView imageView;
    private ImageButton backButton;
    private Button previewButton, imageAdjustButton;
    private TextView actionBarTitle;

    private Uri imageUri;

    @Override
    protected void onCreateContent(Bundle savedInstanceState) {
        setContentView(R.layout.activity_image_captured);

        backButton = findViewById(R.id.backButton);
        actionBarTitle = findViewById(R.id.actionBarTitle);
        imageView = findViewById(R.id.imageView);
        previewButton = findViewById(R.id.previewButton);
        imageAdjustButton = findViewById(R.id.adjustImageButton);

        actionBarTitle.setGravity(Gravity.CENTER);

        imageUri = getIntent().getData();
        try {
            Bitmap bitmap = ImageUtils.getBitmapFromUri(getApplication(), imageUri);
            bitmap = ImageUtils.rotateImageIfRequired(getApplication(), bitmap, imageUri);
            imageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        setOnClickListeners();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.backButton:
                onBackPressed();
                break;
            case R.id.previewButton:
                Intent previewIntent = new Intent(this, PDFHandlerActivity.class);
                previewIntent.setAction(Intent.ACTION_VIEW);
                previewIntent.putExtra(AppConstants.EXTRA_FILE_FROM_PICKER, HomeFragment.IMAGE_FROM_PICKER);
                previewIntent.setData(imageUri);

                previewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(previewIntent);
                break;
            case R.id.adjustImageButton:
                Intent cropIntent = new Intent(getApplicationContext(), EditPhotoActivity.class);
                cropIntent.setData(imageUri);
                startActivity(cropIntent);
                finish();
                break;
        }
    }

    private void setOnClickListeners() {
        backButton.setOnClickListener(this);
        previewButton.setOnClickListener(this);
        imageAdjustButton.setOnClickListener(this);
    }
}