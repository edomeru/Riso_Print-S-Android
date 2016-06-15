/*
 * Copyright (c) 2015 Ricoh Company, Ltd. All rights reserved.
 *
 * TestActivity.java
 * Tamago Clicker
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

// This activity serves as a launcher only for SplashActivity and prevents any issues with multiple instances of the app
public class LauncherActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);
        finish();
    }
}
