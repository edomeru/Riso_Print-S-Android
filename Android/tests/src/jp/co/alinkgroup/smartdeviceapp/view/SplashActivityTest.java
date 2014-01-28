
package jp.co.alinkgroup.smartdeviceapp.view;

import android.app.Instrumentation;
import android.content.pm.ActivityInfo;
import android.test.ActivityInstrumentationTestCase2;

public class SplashActivityTest extends ActivityInstrumentationTestCase2<SplashActivity> {
    private Instrumentation mInstrumentation;
    private SplashActivity mActivity; // MyActivity is the class name of the app under test
    
    public SplashActivityTest() {
        super(SplashActivity.class);
    }
    
    public SplashActivityTest(Class<SplashActivity> activityClass) {
        super(activityClass);
    }

    protected void setUp() throws Exception {
        super.setUp();
        
        mInstrumentation = getInstrumentation();
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        
    }
    
    
    public void testLifecyle_PauseResume() {
        mActivity = getActivity();
        
        try {
            mActivity.onPause();
            mActivity.onResume();
        } catch (Exception e){
            fail(e.getMessage());
        }
        
        mInstrumentation.waitForIdleSync();
        
        mActivity.finish();
    }
    
    public void testLifecycle_Rotation() {
        mActivity = getActivity();
        
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mInstrumentation.waitForIdleSync();
        
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mInstrumentation.waitForIdleSync();
        
        mActivity.finish();
    }
    
    /*
    public void testLifecycle_MainActivityShouldDisplay() {
        mActivity = getActivity();
        
        mInstrumentation.waitForIdleSync();

        // assume AsyncTask will be finished in 6 seconds.
        try {
          Thread.sleep(AppConstants.APP_SPLASH_DURATION);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        
        mInstrumentation.waitForIdleSync();
        
        assertTrue(mActivity.isFinishing());
    }
    */
}
