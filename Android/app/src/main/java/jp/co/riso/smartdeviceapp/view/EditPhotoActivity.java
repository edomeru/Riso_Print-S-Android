package jp.co.riso.smartdeviceapp.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.model.AspectRatio;

import java.io.File;

import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartprint.R;

public class EditPhotoActivity extends Activity {
    private Uri imageUri, destUri;
    public static final int A4_WIDTH = 595, A4_HEIGHT = 842;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imageUri = getIntent().getData();
        uCropEdit();
    }

    public void uCropEdit() {
        destUri = Uri.fromFile(getDestFile());

        UCrop.Options options = new UCrop.Options();
        options.setFreeStyleCropEnabled(false);
        options.setToolbarTitle(getResources().getString(R.string.ids_lbl_img_edit_title));
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(100);
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.theme_dark_1));
        options.setToolbarColor(ContextCompat.getColor(this, R.color.theme_color_2));
        options.setActiveWidgetColor(ContextCompat.getColor(this, R.color.theme_color_2));
        options.setAspectRatioOptions(0, new AspectRatio(null, 3, 4));

        UCrop.of(imageUri, destUri)
                .withOptions(options)
                .withMaxResultSize(A4_WIDTH, A4_HEIGHT)
                .start(this);
    }

    public File getDestFile() {
        File dir = new File(getCacheDir() + AppConstants.CONST_CROPPED_DIRECTORY);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return new File(dir, AppConstants.CONST_IMAGE_CAPTURED_FILENAME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            imageUri = UCrop.getOutput(data);
            Intent intent = new Intent(getApplicationContext(), ImageCapturedActivity.class);
            intent.setData(imageUri);
            startActivity(intent);
            finish();
        }
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_CANCELED) {
            Intent intent = new Intent(getApplicationContext(), ImageCapturedActivity.class);
            intent.setData(imageUri);
            startActivity(intent);
            finish();
        }
    }
}