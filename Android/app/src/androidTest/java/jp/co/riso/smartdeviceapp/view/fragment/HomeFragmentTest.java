package jp.co.riso.smartdeviceapp.view.fragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.ContextCompat;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.view.BaseMainActivityTest;
import jp.co.riso.smartdeviceapp.view.PDFHandlerActivity;
import jp.co.riso.smartprint.R;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static androidx.test.espresso.intent.Intents.init;
import static androidx.test.espresso.intent.Intents.release;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

public class HomeFragmentTest extends BaseMainActivityTest {
    private HomeFragment mHomeFragment = null;
    private final String REQUEST_PERMISSIONS = "android.content.pm.action.REQUEST_PERMISSIONS";
    private final String REQUEST_PERMISSIONS_NAMES = "android.content.pm.extra.REQUEST_PERMISSIONS_NAMES";

    @Rule
    public GrantPermissionRule storagePermission = GrantPermissionRule.grant(WRITE_EXTERNAL_STORAGE);
    // TODO: CAMERA permission is hidden feature. uncomment when new features are restored
    //@Rule
    //public GrantPermissionRule cameraPermission = GrantPermissionRule.grant(CAMERA);

    @Before
    public void initEspresso() {
        init();
    }

    @After
    public void releaseEspresso() {
        release();
    }

    @Before
    public void initHomeFragment() {
        final FragmentManager fm = mActivity.getSupportFragmentManager();

        mActivity.runOnUiThread(() -> {
            fm.beginTransaction().add(R.id.mainLayout, new HomeFragment()).commit();
            fm.executePendingTransactions();
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        Fragment homeFragment = fm.findFragmentById(R.id.mainLayout);
        assertTrue(homeFragment instanceof HomeFragment);
        mHomeFragment = (HomeFragment) homeFragment;
    }

    @Test
    public void newInstance() {
        assertNotNull(mHomeFragment);
    }

    @Test
    public void onClickSelectDocument() {
        // stubs handling of start activity to prevent launching of OS picker
        // null result only since test is not about checking handling of result
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(FragmentActivity.RESULT_OK, null);
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result);

        testClick(R.id.fileButton);

        // if no permission yet, intent for permission request will be sent instead
        // TODO: add mocking of permission check
        if (ContextCompat.checkSelfPermission(mActivity,
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

        if (mHomeFragment.isChromeBook()) {
            assertNull(extraIntent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES));
        } else {
            assertEquals(extraIntent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES), AppConstants.DOC_TYPES);
        }
    }

    @Test
    public void onClickSelectPhotos() {
        // stubs handling of start activity to prevent launching of OS picker
        // null result only since test is not about checking handling of result
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(FragmentActivity.RESULT_OK, null);
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result);

        testClick(R.id.photosButton);

        // if no permission yet, intent for permission request will be sent instead
        // TODO: add mocking of permission check
        if (ContextCompat.checkSelfPermission(mActivity,
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

        if (mHomeFragment.isChromeBook()) {
            assertNull(extraIntent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES));
        } else {
            assertArrayEquals(extraIntent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES), AppConstants.IMAGE_TYPES);
        }
    }

    @Test
    public void onClickCapturePhoto() {
        // stubs handling of permission request in case of no permissions
        // null result only since test is not about checking handling of result
        Intents.intending(IntentMatchers.hasAction(REQUEST_PERMISSIONS))
                .respondWith(new Instrumentation.ActivityResult(FragmentActivity.RESULT_OK, null));

        // simulate return of ScanActivity
        Intent scanActivityResult = new Intent();
        Uri testImage = Uri.parse(PDFHandlerActivity.FILE_SCHEME+"://testPath");
        scanActivityResult.putExtra(ScanConstants.SCANNED_RESULT, testImage);
        Intents.intending(IntentMatchers.hasComponent(ScanActivity.class.getName()))
                .respondWith(new Instrumentation.ActivityResult(FragmentActivity.RESULT_OK, scanActivityResult));

        testClick(R.id.cameraButton);

        // if no permission yet, intent for permission request will be sent instead
        // TODO: add mocking of permission check
        if (ContextCompat.checkSelfPermission(mActivity,
                WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(mActivity,
                        CAMERA) != PERMISSION_GRANTED) {

            Intent permissionRequestIntent = Intents.getIntents().get(0);
            assertNotNull(permissionRequestIntent);
            assertEquals(permissionRequestIntent.getAction(), REQUEST_PERMISSIONS);

            String[] permissions = permissionRequestIntent.getStringArrayExtra(REQUEST_PERMISSIONS_NAMES);
            assertEquals(permissions.length, 2);
            assertEquals(permissions[0], CAMERA);
            assertEquals(permissions[1], WRITE_EXTERNAL_STORAGE);
            return;
        }

        // check that ScanActivity and PdfHandlerActivity intents were sent
        assertEquals(Intents.getIntents().size(), 2);

        // check that intent for ScanActivity was sent
        Intent scanActivityIntent = Intents.getIntents().get(0);
        assertNotNull(scanActivityIntent);
        assertEquals(scanActivityIntent.getComponent().getClassName(), ScanActivity.class.getName());
        assertEquals(scanActivityIntent.getIntExtra(ScanConstants.OPEN_INTENT_PREFERENCE, 0), ScanConstants.OPEN_CAMERA);

        // check that intent for PdfHandlerActivity was sent
        Intent pdfHandlerIntent = Intents.getIntents().get(1);
        assertNotNull(pdfHandlerIntent);
        assertEquals(pdfHandlerIntent.getComponent().getClassName(), PDFHandlerActivity.class.getName());
        assertEquals(pdfHandlerIntent.getIntExtra(AppConstants.EXTRA_FILE_FROM_PICKER, 0), HomeFragment.IMAGE_FROM_CAMERA);
        assertEquals(pdfHandlerIntent.getData(), testImage);
    }
}