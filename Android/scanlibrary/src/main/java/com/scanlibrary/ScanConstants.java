package com.scanlibrary;

import android.os.Environment;
// aLINK edit - Start
import android.graphics.Bitmap;
// aLINK edit - End

/**
 * Created by jhansi on 15/03/15.
 */
public class ScanConstants {

    public final static int PICKFILE_REQUEST_CODE = 1;
    public final static int START_CAMERA_REQUEST_CODE = 2;
    public final static String OPEN_INTENT_PREFERENCE = "selectContent";
    public final static String IMAGE_BASE_PATH_EXTRA = "ImageBasePath";
    public final static int OPEN_CAMERA = 4;
    public final static int OPEN_MEDIA = 5;
    public final static String SCANNED_RESULT = "scannedResult";
    // aLINK edit - Start
    // Environment.getExternalStorageDirectory() was deprecated in API level 29.
    // The suggestion is to use Context.getExternalFilesDir(String type), but since
    // the IMAGE_PATH variable is not being used, just comment it out
    //public final static String IMAGE_PATH = Environment
    //        .getExternalStorageDirectory().getPath() + "/scanSample";
    // aLINK edit - End

    public final static String SELECTED_BITMAP = "selectedBitmap";

    // aLINK edit - Start
    // From Android 10, Media Scanner can't be used on custom paths
    public final static String IMAGE_RELATIVE_PATH = Environment.DIRECTORY_PICTURES + "/SDA";
    // Used to convert default Bitmap.Config.HARDWARE to supported config
    public final static Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    // aLINK edit: RM#907 for checking if chromebook
    public final static String CHROME_BOOK = "org.chromium.arc.device_management";
    // aLINK edit - End
}
