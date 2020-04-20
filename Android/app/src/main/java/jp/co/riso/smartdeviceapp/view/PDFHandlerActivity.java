/*
 * Copyright (c) 2015 Ricoh Company, Ltd. All rights reserved.
 *
 * PDFHandlerActivity.java
 * Tamago Clicker
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager;

public class PDFHandlerActivity extends Activity {

    public static final String FILE_SCHEME = "file";
    public static final String CONTENT_SCHEME = "content";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        if(intent != null) {
            // From Home Screen file picker
            if (intent.getIntExtra(AppConstants.EXTRA_FILE_FROM_PICKER, 1) < 1) {
                boolean hasNewData = intent.getData() != null || intent.getClipData() != null;
                PDFFileManager.clearSandboxPDFName(SmartDeviceApp.getAppContext());
                PDFFileManager.setHasNewPDFData(SmartDeviceApp.getAppContext(), hasNewData);

                intent.setClass(this, MainActivity.class);
                handleViewIntentData(intent);
            } else {
                intent.setClass(this, SplashActivity.class);
                String action = intent.getAction();

                if (action != null && (action.equals(Intent.ACTION_VIEW) || action.equals(Intent.ACTION_SEND_MULTIPLE))) {
                    handleViewIntentData(intent);
                }
            }
        } else {
            intent = new Intent(this, SplashActivity.class);
        }

        startActivity(intent);
        finish();
    }

    private void handleViewIntentData(Intent intent) {
        Uri intentData = intent.getData();
        ClipData intentClipData = intent.getClipData();

        if(intentData != null) {
            if(intentData.getScheme().equals(FILE_SCHEME) || intentData.getScheme().equals(CONTENT_SCHEME)) {
                intent.setData(intentData);
            } else { // load the PDF input stream from this activity only in order to handle special "content" URIs that cannot be opened by other activities (such as Gmail attachment URI)
                intent.setData(PDFFileManager.createTemporaryPdfFromContentUri(this, intent.getData()));
            }
        } else if (intentClipData != null) {
            intent.setClipData(intentClipData);
        }
    }
}
