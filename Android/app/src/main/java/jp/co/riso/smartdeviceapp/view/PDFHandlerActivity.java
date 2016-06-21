/*
 * Copyright (c) 2015 Ricoh Company, Ltd. All rights reserved.
 *
 * PDFHandlerActivity.java
 * Tamago Clicker
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager;

public class PDFHandlerActivity extends Activity {

    public static final String FILE_SCHEME = "file";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        if(intent != null) {
            intent.setClass(this, SplashActivity.class);
            String action = intent.getAction();

            if (action.equals(Intent.ACTION_VIEW)) {
                handleViewIntentData(intent);
            }
        } else {
            intent = new Intent(this, SplashActivity.class);
        }

        startActivity(intent);
        finish();
    }

    private void handleViewIntentData(Intent intent) {
        Uri intentData = intent.getData();

        if(intentData.getScheme().equals(FILE_SCHEME)) {
            intent.setData(intentData);
        } else { // load the PDF input stream from this activity only in order to handle special "content" URIs that cannot be opened by other activities (such as Gmail attachment URI)
            intent.setData(PDFFileManager.createTemporaryPdfFromContentUri(this, intent.getData()));
        }
    }
}
