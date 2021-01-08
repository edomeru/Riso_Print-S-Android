package com.scanlibrary;

// aLINK edit - Start
// android.app.Activity was not compatible with androidx.fragment.app.FragmentManager
// Use androidx.fragment.app.FragmentActivity instead
import androidx.fragment.app.FragmentActivity;
// aLINK edit - End
import android.app.AlertDialog;
// aLINK edit - Start
// android.app.FragmentTransaction was deprecated in API level 28
// Use androidx.fragment.app.FragmentTransaction instead
import androidx.fragment.app.FragmentTransaction;
// android.app.FragmentManager was deprecated in API level 28
// Use androidx.fragment.app.FragmentManager instead
import androidx.fragment.app.FragmentManager;
// aLINK edit - End
import android.content.ComponentCallbacks2;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

/**
 * Created by jhansi on 28/03/15.
 */
// aLINK edit - Start
// android.app.Activity was not compatible with androidx.fragment.app.FragmentManager
// Use androidx.fragment.app.FragmentActivity instead
public class ScanActivity extends FragmentActivity implements IScanner, ComponentCallbacks2 {
// aLINK edit - End

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // aLINK edit: set supported orientation to portrait only
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // end
        setContentView(R.layout.scan_layout);
        init();
    }

    private void init() {
        PickImageFragment fragment = new PickImageFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ScanConstants.OPEN_INTENT_PREFERENCE, getPreferenceContent());
        fragment.setArguments(bundle);
        // aLINK edit - Start
        // android.app.FragmentManager was deprecated in API level 28
        // Call the getSupportFragmentManager() API to get androidx.fragment.app.FragmentManager
        // instead of android.app.FragmentManager
        FragmentManager fragmentManager = getSupportFragmentManager();
        // aLINK edit - End
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.content, fragment);
        fragmentTransaction.commit();
    }

    protected int getPreferenceContent() {
        return getIntent().getIntExtra(ScanConstants.OPEN_INTENT_PREFERENCE, 0);
    }

    @Override
    public void onBitmapSelect(Uri uri) {
        ScanFragment fragment = new ScanFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ScanConstants.SELECTED_BITMAP, uri);
        fragment.setArguments(bundle);
        // aLINK edit - Start
        // android.app.FragmentManager was deprecated in API level 28
        // Call the getSupportFragmentManager() API to get androidx.fragment.app.FragmentManager
        // instead of android.app.FragmentManager
        FragmentManager fragmentManager = getSupportFragmentManager();
        // aLINK edit - End
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.content, fragment);
        // aLINK edit: do not add the pick image fragment to back stack
//        fragmentTransaction.addToBackStack(ScanFragment.class.toString());
        fragmentTransaction.commit();
    }

    @Override
    public void onScanFinish(Uri uri) {
        ResultFragment fragment = new ResultFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ScanConstants.SCANNED_RESULT, uri);
        fragment.setArguments(bundle);
        // aLINK edit - Start
        // android.app.FragmentManager was deprecated in API level 28
        // Call the getSupportFragmentManager() API to get androidx.fragment.app.FragmentManager
        // instead of android.app.FragmentManager
        FragmentManager fragmentManager = getSupportFragmentManager();
        // aLINK edit - End
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.content, fragment);
        fragmentTransaction.addToBackStack(ResultFragment.class.toString());
        fragmentTransaction.commit();
    }

    @Override
    public void onTrimMemory(int level) {
        switch (level) {
            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
                /*
                   Release any UI objects that currently hold memory.

                   The user interface has moved to the background.
                */
                break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:
                /*
                   Release any memory that your app doesn't need to run.

                   The device is running low on memory while the app is running.
                   The event raised indicates the severity of the memory-related event.
                   If the event is TRIM_MEMORY_RUNNING_CRITICAL, then the system will
                   begin killing background processes.
                */
                break;
            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
                /*
                   Release as much memory as the process can.

                   The app is on the LRU list and the system is running low on memory.
                   The event raised indicates where the app sits within the LRU list.
                   If the event is TRIM_MEMORY_COMPLETE, the process will be one of
                   the first to be terminated.
                */
                new AlertDialog.Builder(this)
                        .setTitle(R.string.low_memory)
                        .setMessage(R.string.low_memory_message)
                        .create()
                        .show();
                break;
            default:
                /*
                  Release any non-critical data structures.

                  The app received an unrecognized memory level value
                  from the system. Treat this as a generic low-memory message.
                */
                break;
        }
    }

    public native Bitmap getScannedBitmap(Bitmap bitmap, float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4);

    public native Bitmap getGrayBitmap(Bitmap bitmap);

    public native Bitmap getMagicColorBitmap(Bitmap bitmap);

    public native Bitmap getBWBitmap(Bitmap bitmap);

    public native float[] getPoints(Bitmap bitmap);

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("Scanner");
    }
}