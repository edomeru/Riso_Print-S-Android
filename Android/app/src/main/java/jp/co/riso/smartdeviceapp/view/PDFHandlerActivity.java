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
import android.os.Bundle;

import jp.co.riso.android.util.AppUtils;

public class PDFHandlerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = AppUtils.createActivityIntent(this, SplashActivity.class);
        Intent currentIntent = getIntent();

        if(currentIntent != null) {
            intent.setData(getIntent().getData());
            intent.setAction(getIntent().getAction());
        }

        startActivity(intent);

        finish();
    }
}
