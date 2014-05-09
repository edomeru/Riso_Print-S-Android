package jp.co.riso.smartdeviceapp.view;

import android.app.Instrumentation;
import android.content.pm.ActivityInfo;
import android.test.ActivityInstrumentationTestCase2;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
    
    private Instrumentation mInstrumentation;
    private MainActivity mActivity; // MyActivity is the class name of the app under test
    
    public MainActivityTest() {
        super(MainActivity.class);
    }
    
    public MainActivityTest(Class<MainActivity> activityClass) {
        super(activityClass);
    }

    protected void setUp() throws Exception {
        super.setUp();
        
        mInstrumentation = getInstrumentation();
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testLifecycle_Rotation() {
        mActivity = getActivity();
        
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mInstrumentation.waitForIdleSync();
        
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mInstrumentation.waitForIdleSync();
        
        mActivity.finish();
    }
}
