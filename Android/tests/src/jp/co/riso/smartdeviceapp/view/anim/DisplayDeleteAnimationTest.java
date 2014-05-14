
package jp.co.riso.smartdeviceapp.view.anim;

import jp.co.riso.smartdeviceapp.view.MainActivity;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DisplayDeleteAnimationTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity mActivity;

    public DisplayDeleteAnimationTest() {
        super(MainActivity.class);
    }

    public DisplayDeleteAnimationTest(Class<MainActivity> activityClass) {
        super(activityClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        wakeUpScreen();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testBeginDeleteModeOnView_WithAnimation() {
        DisplayDeleteAnimation mDeleteAnimation = new DisplayDeleteAnimation();
        LinearLayout ll = new LinearLayout(mActivity);
        Button deleteButton = new Button(mActivity);
        deleteButton.setText("button");
        deleteButton.setId(1);
        ll.addView(deleteButton);
        deleteButton.setVisibility(View.INVISIBLE);
        assertEquals(View.INVISIBLE, deleteButton.getVisibility());
        mDeleteAnimation.beginDeleteModeOnView(ll, true, 1);
        waitInMilliseconds(1000);
        assertEquals(View.VISIBLE, deleteButton.getVisibility());
    }

    public void testBeginDeleteModeOnView_WithoutAnimation() {
        DisplayDeleteAnimation mDeleteAnimation = new DisplayDeleteAnimation();
        LinearLayout ll = new LinearLayout(mActivity);
        Button deleteButton = new Button(mActivity);
        deleteButton.setText("button");
        deleteButton.setId(1);
        ll.addView(deleteButton);
        deleteButton.setVisibility(View.INVISIBLE);
        assertEquals(View.INVISIBLE, deleteButton.getVisibility());
        mDeleteAnimation.beginDeleteModeOnView(ll, false, 1);
        waitInMilliseconds(100);
        assertEquals(View.VISIBLE, deleteButton.getVisibility());
    }

    public void testBeginDeleteModeOnView_WithOutAnimationHideOtherViews() {
        DisplayDeleteAnimation mDeleteAnimation = new DisplayDeleteAnimation();
        LinearLayout ll = new LinearLayout(mActivity);
        Button deleteButton = new Button(mActivity);
        deleteButton.setText("button");
        deleteButton.setId(1);
        ll.addView(deleteButton);

        Button otherButton = new Button(mActivity);
        otherButton.setText("other button");
        otherButton.setId(2);
        ll.addView(otherButton);

        TextView tv = new TextView(mActivity);
        tv.setText("textview");
        tv.setId(3);
        ll.addView(tv);

        deleteButton.setVisibility(View.INVISIBLE);
        otherButton.setVisibility(View.VISIBLE);
        tv.setVisibility(View.VISIBLE);

        assertEquals(View.INVISIBLE, deleteButton.getVisibility());
        assertEquals(View.VISIBLE, otherButton.getVisibility());

        assertEquals(View.VISIBLE, tv.getVisibility());

        mDeleteAnimation.beginDeleteModeOnView(ll, false, 1, 2, 3);
        waitInMilliseconds(100);
        assertEquals(View.VISIBLE, deleteButton.getVisibility());
        assertEquals(View.INVISIBLE, otherButton.getVisibility());
        assertEquals(View.INVISIBLE, tv.getVisibility());

    }

    public void testBeginDeleteModeOnView_WithAnimationHideOtherViews() {
        // DisplayDeleteAnimation mDeleteAnimation = new
        // DisplayDeleteAnimation();
        final LinearLayout ll = new LinearLayout(mActivity);
        Button deleteButton = new Button(mActivity);
        deleteButton.setText("button");
        deleteButton.setId(1);
        ll.addView(deleteButton);

        Button otherButton = new Button(mActivity);
        otherButton.setText("other button");
        otherButton.setId(2);
        ll.addView(otherButton);

        TextView tv = new TextView(mActivity);
        tv.setText("textview");
        tv.setId(3);
        ll.addView(tv);

        deleteButton.setVisibility(View.INVISIBLE);
        otherButton.setVisibility(View.VISIBLE);
        tv.setVisibility(View.VISIBLE);

        assertEquals(View.INVISIBLE, deleteButton.getVisibility());
        assertEquals(View.VISIBLE, otherButton.getVisibility());
        assertEquals(View.VISIBLE, tv.getVisibility());
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActivity.getWindow().addContentView(
                        ll, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                                LayoutParams.MATCH_PARENT));
                DisplayDeleteAnimation mDeleteAnimation = new DisplayDeleteAnimation();
                mDeleteAnimation.beginDeleteModeOnView(ll, true, 1, 2, 3);
            }
        });
        waitInMilliseconds(1000);
        assertEquals(View.VISIBLE, deleteButton.getVisibility());
        assertEquals(View.INVISIBLE, otherButton.getVisibility());
        assertEquals(View.INVISIBLE, tv.getVisibility());
    }

    
    public void testBeginDeleteModeOnView_WithAnimationHideOtherViewsTwice() {
        // DisplayDeleteAnimation mDeleteAnimation = new
        // DisplayDeleteAnimation();
        final LinearLayout ll = new LinearLayout(mActivity);
        Button deleteButton = new Button(mActivity);
        deleteButton.setText("button");
        deleteButton.setId(1);
        ll.addView(deleteButton);

        Button otherButton = new Button(mActivity);
        otherButton.setText("other button");
        otherButton.setId(2);
        ll.addView(otherButton);

        TextView tv = new TextView(mActivity);
        tv.setText("textview");
        tv.setId(3);
        ll.addView(tv);

        deleteButton.setVisibility(View.INVISIBLE);
        otherButton.setVisibility(View.VISIBLE);
        tv.setVisibility(View.VISIBLE);

        assertEquals(View.INVISIBLE, deleteButton.getVisibility());
        assertEquals(View.VISIBLE, otherButton.getVisibility());
        assertEquals(View.VISIBLE, tv.getVisibility());
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActivity.getWindow().addContentView(
                        ll, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                                LayoutParams.MATCH_PARENT));
                DisplayDeleteAnimation mDeleteAnimation = new DisplayDeleteAnimation();
                mDeleteAnimation.beginDeleteModeOnView(ll, true, 1, 2, 3);
                mDeleteAnimation.beginDeleteModeOnView(ll, true, 1, 2, 3);
            }
        });

        waitInMilliseconds(1000);

        assertEquals(View.VISIBLE, deleteButton.getVisibility());
        assertEquals(View.INVISIBLE, otherButton.getVisibility());
        assertEquals(View.INVISIBLE, tv.getVisibility());
    }

    public void testEndDeleteModeOnView_WithAnimation() {
        // setActivityInitialTouchMode(true);
        final LinearLayout ll = new LinearLayout(mActivity);
        final Button deleteButton = new Button(mActivity);
        deleteButton.setText("button");
        deleteButton.setId(1);

        ll.addView(deleteButton);
        assertEquals(View.VISIBLE, deleteButton.getVisibility());
        mActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                // ll.addView(deleteButton);
                mActivity.getWindow().addContentView(
                        ll,
                        new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                                LayoutParams.MATCH_PARENT));
                // assertEquals(View.VISIBLE, deleteButton.getVisibility());
                DisplayDeleteAnimation mDeleteAnimation = new DisplayDeleteAnimation();
                mDeleteAnimation.endDeleteMode(ll, true, 1);
            }
        });

        waitInMilliseconds(1000);
        assertEquals(View.INVISIBLE, deleteButton.getVisibility());
    }

    public void testEndDeleteModeOnView_WithOutAnimation() {
        final LinearLayout ll = new LinearLayout(mActivity);
        final Button deleteButton = new Button(mActivity);
        deleteButton.setText("button");
        deleteButton.setId(1);

        ll.addView(deleteButton);
        assertEquals(View.VISIBLE, deleteButton.getVisibility());
        DisplayDeleteAnimation mDeleteAnimation = new DisplayDeleteAnimation();
        mDeleteAnimation.endDeleteMode(ll, false, 1);

        waitInMilliseconds(100);
        assertEquals(View.INVISIBLE, deleteButton.getVisibility());
    }
    
    public void testEndDeleteModeOnView_WithOutAnimationShowOtherViews() {
        final LinearLayout ll = new LinearLayout(mActivity);
        final Button deleteButton = new Button(mActivity);
        deleteButton.setText("button");
        deleteButton.setId(1);

        ll.addView(deleteButton);

        Button otherButton = new Button(mActivity);
        otherButton.setText("other button");
        otherButton.setId(2);
        ll.addView(otherButton);

        TextView tv = new TextView(mActivity);
        tv.setText("textview");
        tv.setId(3);
        ll.addView(tv);

        deleteButton.setVisibility(View.VISIBLE);
        otherButton.setVisibility(View.INVISIBLE);
        tv.setVisibility(View.INVISIBLE);

        assertEquals(View.VISIBLE, deleteButton.getVisibility());
        assertEquals(View.INVISIBLE, otherButton.getVisibility());

        assertEquals(View.INVISIBLE, tv.getVisibility());
        DisplayDeleteAnimation mDeleteAnimation = new DisplayDeleteAnimation();
        mDeleteAnimation.endDeleteMode(ll, false, 1, 2, 3);

        waitInMilliseconds(100);
        assertEquals(View.INVISIBLE, deleteButton.getVisibility());
        assertEquals(View.VISIBLE, otherButton.getVisibility());
        assertEquals(View.VISIBLE, tv.getVisibility());
    }
    
    public void testEndDeleteModeOnView_WithAnimationShowOtherViews() {
        final LinearLayout ll = new LinearLayout(mActivity);
        final Button deleteButton = new Button(mActivity);
        deleteButton.setText("button");
        deleteButton.setId(1);

        ll.addView(deleteButton);

        Button otherButton = new Button(mActivity);
        otherButton.setText("other button");
        otherButton.setId(2);
        ll.addView(otherButton);

        TextView tv = new TextView(mActivity);
        tv.setText("textview");
        tv.setId(3);
        ll.addView(tv);

        deleteButton.setVisibility(View.VISIBLE);
        otherButton.setVisibility(View.INVISIBLE);
        tv.setVisibility(View.INVISIBLE);

        assertEquals(View.VISIBLE, deleteButton.getVisibility());
        assertEquals(View.INVISIBLE, otherButton.getVisibility());

        assertEquals(View.INVISIBLE, tv.getVisibility());


        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActivity.getWindow().addContentView(
                        ll,
                        new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                                LayoutParams.MATCH_PARENT));
                DisplayDeleteAnimation mDeleteAnimation = new DisplayDeleteAnimation();
                mDeleteAnimation.endDeleteMode(ll, true, 1, 2, 3);
            }
        });

        waitInMilliseconds(1000);
        
        assertEquals(View.INVISIBLE, deleteButton.getVisibility());
        assertEquals(View.VISIBLE, otherButton.getVisibility());
        assertEquals(View.VISIBLE, tv.getVisibility());
    }
    
    public void testEndDeleteModeOnView_WithAnimationShowOtherViewsTwice() {
        final LinearLayout ll = new LinearLayout(mActivity);
        final Button deleteButton = new Button(mActivity);
        deleteButton.setText("button");
        deleteButton.setId(1);

        ll.addView(deleteButton);

        Button otherButton = new Button(mActivity);
        otherButton.setText("other button");
        otherButton.setId(2);
        ll.addView(otherButton);

        TextView tv = new TextView(mActivity);
        tv.setText("textview");
        tv.setId(3);
        ll.addView(tv);

        deleteButton.setVisibility(View.VISIBLE);
        otherButton.setVisibility(View.INVISIBLE);
        tv.setVisibility(View.INVISIBLE);

        assertEquals(View.VISIBLE, deleteButton.getVisibility());
        assertEquals(View.INVISIBLE, otherButton.getVisibility());

        assertEquals(View.INVISIBLE, tv.getVisibility());


        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActivity.getWindow().addContentView(
                        ll,
                        new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                                LayoutParams.MATCH_PARENT));
                DisplayDeleteAnimation mDeleteAnimation = new DisplayDeleteAnimation();
                mDeleteAnimation.endDeleteMode(ll, true, 1, 2, 3);
                mDeleteAnimation.endDeleteMode(ll, true, 1, 2, 3);
            }
        });

        waitInMilliseconds(1000);
        
        assertEquals(View.INVISIBLE, deleteButton.getVisibility());
        assertEquals(View.VISIBLE, otherButton.getVisibility());
        assertEquals(View.VISIBLE, tv.getVisibility());
    }
    
    
    
    //================================================================================
    // Private methods
    //================================================================================

    // wait some milliseconds so that you can see the change on emulator/device.
    private void waitInMilliseconds(final int ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private void wakeUpScreen() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActivity.getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            }
        });

        waitInMilliseconds(2000);
    }
}
