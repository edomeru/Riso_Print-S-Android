package jp.co.riso.smartdeviceapp.view.fragment;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import android.app.Instrumentation;
import android.content.ContentProvider;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

//import androidx.test.core.app.ApplicationProvider;
import androidx.core.content.ContextCompat;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Rule;
import org.junit.Test;

import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartprint.R;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;

public class HomeFragmentTest {
    //private final Context context = ApplicationProvider.getApplicationContext();
    private final String TAG = "HOME_FRAGMENT";
    private final String REQUEST_PERMISSIONS = "android.content.pm.action.REQUEST_PERMISSIONS";
    private final String REQUEST_PERMISSIONS_NAMES = "android.content.pm.extra.REQUEST_PERMISSIONS_NAMES";

    @Rule
    public IntentsTestRule<MainActivity> intentsTestRule = new IntentsTestRule<>(MainActivity.class);

    private HomeFragment initHomeFragment() {
        final FragmentManager fm = intentsTestRule.getActivity().getSupportFragmentManager();

        intentsTestRule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fm.beginTransaction().add(new HomeFragment(), TAG).commit();
                fm.executePendingTransactions();
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertTrue(fm.findFragmentByTag(TAG) instanceof HomeFragment);
        return (HomeFragment) fm.findFragmentByTag(TAG);
    }

    @Test
    public void newInstance() {
        HomeFragment homeFragment = initHomeFragment();
        assertNotNull(homeFragment);
    }

    @Test
    public void onClickSelectDocument() {
        HomeFragment homeFragment = initHomeFragment();

        // stubs handling of start activity to prevent launching of OS picker
        // null result only since test is not about checking handling of result
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(FragmentActivity.RESULT_OK, null);
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result);

        // prepare button to be clicked
        final View selectDocumentButton = new LinearLayout(intentsTestRule.getActivity());
        selectDocumentButton.setId(R.id.fileButton);

        homeFragment.onClick(selectDocumentButton);

        // if no permission yet, intent for permission request will be sent instead
        // TODO: add mocking of permission check
        if (ContextCompat.checkSelfPermission(intentsTestRule.getActivity(),
                WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            Intent permissionRequestIntent = Intents.getIntents().get(0);
            assertNotNull(permissionRequestIntent);
            assertEquals(permissionRequestIntent.getAction(), REQUEST_PERMISSIONS);

            String[] permissions = permissionRequestIntent.getStringArrayExtra(REQUEST_PERMISSIONS_NAMES);
            assertEquals(permissions.length, 1);
            assertEquals(permissions[0], WRITE_EXTERNAL_STORAGE);
            return;
        }

        // check intent that was sent
        Intent selectDocumentIntent = Intents.getIntents().get(0);
        assertNotNull(selectDocumentIntent);
        assertEquals(selectDocumentIntent.getAction(), Intent.ACTION_CHOOSER);

        Intent extraIntent = selectDocumentIntent.getParcelableExtra(Intent.EXTRA_INTENT);
        assertNotNull(extraIntent);
        assertEquals(extraIntent.getAction(), Intent.ACTION_GET_CONTENT);
        assertEquals(extraIntent.getType(), "*/*");

        if (homeFragment.isChromeBook()) {
            assertNull(extraIntent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES));
        } else {
            assertEquals(extraIntent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES), AppConstants.DOC_TYPES);
        }
    }

    @Test
    public void onClickSelectPhotos() {
        HomeFragment homeFragment = initHomeFragment();

        // stubs handling of start activity to prevent launching of OS picker
        // null result only since test is not about checking handling of result
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(FragmentActivity.RESULT_OK, null);
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result);

        // prepare button to be clicked
        final View selectPhotosButton = new LinearLayout(intentsTestRule.getActivity());
        selectPhotosButton.setId(R.id.photosButton);

        homeFragment.onClick(selectPhotosButton);

        // if no permission yet, intent for permission request will be sent instead
        // TODO: add mocking of permission check
        if (ContextCompat.checkSelfPermission(intentsTestRule.getActivity(),
                WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            Intent permissionRequestIntent = Intents.getIntents().get(0);
            assertNotNull(permissionRequestIntent);
            assertEquals(permissionRequestIntent.getAction(), REQUEST_PERMISSIONS);

            String[] permissions = permissionRequestIntent.getStringArrayExtra(REQUEST_PERMISSIONS_NAMES);
            assertEquals(permissions.length, 1);
            assertEquals(permissions[0], WRITE_EXTERNAL_STORAGE);
            return;
        }

        // check intent that was sent
        Intent selectPhotosIntent = Intents.getIntents().get(0);
        assertNotNull(selectPhotosIntent);
        assertEquals(selectPhotosIntent.getAction(), Intent.ACTION_CHOOSER);

        Intent extraIntent = selectPhotosIntent.getParcelableExtra(Intent.EXTRA_INTENT);
        assertNotNull(extraIntent);
        assertEquals(extraIntent.getAction(), Intent.ACTION_GET_CONTENT);
        assertEquals(extraIntent.getType(), "*/*");
        assertTrue(extraIntent.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false));

        if (homeFragment.isChromeBook()) {
            assertNull(extraIntent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES));
        } else {
            assertEquals(extraIntent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES), AppConstants.IMAGE_TYPES);
        }
    }
}