package jp.co.riso.smartdeviceapp.view.fragment

import android.Manifest
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.Manifest.permission.CAMERA
import android.app.Instrumentation
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.scanlibrary.ScanActivity
import com.scanlibrary.ScanConstants
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import jp.co.riso.smartdeviceapp.view.MainActivity
import jp.co.riso.smartdeviceapp.view.PDFHandlerActivity
import jp.co.riso.smartprint.R
import org.junit.*

class HomeFragmentTest : BaseActivityTestUtil() {

    private var _homeFragment: HomeFragment? = null
    private val REQUEST_PERMISSIONS = "android.content.pm.action.REQUEST_PERMISSIONS"
    private val REQUEST_PERMISSIONS_NAMES = "android.content.pm.extra.REQUEST_PERMISSIONS_NAMES"

    @get:Rule
    var storagePermission: GrantPermissionRule = GrantPermissionRule.grant(WRITE_EXTERNAL_STORAGE)

    // HIDE_NEW_FEATURES comment when CAMERA permission is hidden feature. uncomment when new features are restored
    //@get:Rule
    //var cameraPermission: GrantPermissionRule = GrantPermissionRule.grant(CAMERA)

    @Before
    fun initEspresso() {
        Intents.init()
    }

    @After
    fun releaseEspresso() {
        Intents.release()
    }

    @Before
    fun initHomeFragment() {
        val fm = mainActivity!!.supportFragmentManager
        mainActivity!!.runOnUiThread {
            fm.beginTransaction().add(R.id.mainLayout, HomeFragment()).commit()
            fm.executePendingTransactions()
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        val homeFragment = fm.findFragmentById(R.id.mainLayout)
        Assert.assertTrue(homeFragment is HomeFragment)
        _homeFragment = homeFragment as HomeFragment?
    }

    @Test
    fun testNewInstance() {
        Assert.assertNotNull(_homeFragment)
    }

    @Test
    fun testOnClick_SelectDocument() {
        // stubs handling of start activity to prevent launching of OS picker
        // null result only since test is not about checking handling of result
        val result = Instrumentation.ActivityResult(FragmentActivity.RESULT_OK, null)
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result)
        testClick(R.id.fileButton)

        // if no permission yet, intent for permission request will be sent instead
        // TODO: add mocking of permission check
        if (ContextCompat.checkSelfPermission(
                mainActivity!!,
                WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val permissionRequestIntent = Intents.getIntents()[0]
            Assert.assertNotNull(permissionRequestIntent)
            Assert.assertEquals(permissionRequestIntent.action, REQUEST_PERMISSIONS)
            val permissions = permissionRequestIntent.getStringArrayExtra(REQUEST_PERMISSIONS_NAMES)
            Assert.assertEquals(permissions!!.size.toLong(), 1)
            Assert.assertEquals(permissions[0], WRITE_EXTERNAL_STORAGE)
            return
        }

        // check intent that was sent
        val selectDocumentIntent = Intents.getIntents()[0]
        Assert.assertNotNull(selectDocumentIntent)
        Assert.assertEquals(selectDocumentIntent.action, Intent.ACTION_CHOOSER)
        val extraIntent = selectDocumentIntent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
        Assert.assertNotNull(extraIntent)
        Assert.assertEquals(extraIntent!!.action, Intent.ACTION_GET_CONTENT)
        Assert.assertEquals(extraIntent.type, "*/*")
        if (_homeFragment!!.isChromeBook) {
            Assert.assertNull(extraIntent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES))
        } else {
            Assert.assertEquals(
                extraIntent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES),
                AppConstants.DOC_TYPES
            )
        }
    }

    @Test
    fun testOnClick_SelectPhotos() {
        // stubs handling of start activity to prevent launching of OS picker
        // null result only since test is not about checking handling of result
        val result = Instrumentation.ActivityResult(FragmentActivity.RESULT_OK, null)
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result)
        testClick(R.id.photosButton)

        // if no permission yet, intent for permission request will be sent instead
        // TODO: add mocking of permission check
        if (ContextCompat.checkSelfPermission(
                mainActivity!!,
                WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val permissionRequestIntent = Intents.getIntents()[0]
            Assert.assertNotNull(permissionRequestIntent)
            Assert.assertEquals(permissionRequestIntent.action, REQUEST_PERMISSIONS)
            val permissions = permissionRequestIntent.getStringArrayExtra(REQUEST_PERMISSIONS_NAMES)
            Assert.assertEquals(permissions!!.size.toLong(), 1)
            Assert.assertEquals(permissions[0], WRITE_EXTERNAL_STORAGE)
            return
        }

        // check intent that was sent
        val selectPhotosIntent = Intents.getIntents()[0]
        Assert.assertNotNull(selectPhotosIntent)
        Assert.assertEquals(selectPhotosIntent.action, Intent.ACTION_CHOOSER)
        val extraIntent = selectPhotosIntent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
        Assert.assertNotNull(extraIntent)
        Assert.assertEquals(extraIntent!!.action, Intent.ACTION_GET_CONTENT)
        Assert.assertEquals(extraIntent.type, "*/*")
        Assert.assertTrue(extraIntent.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false))
        if (_homeFragment!!.isChromeBook) {
            Assert.assertNull(extraIntent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES))
        } else {
            Assert.assertArrayEquals(
                extraIntent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES),
                AppConstants.IMAGE_TYPES
            )
        }
    }

    @Test
    fun testOnClick_CapturePhoto() {
        // stubs handling of permission request in case of no permissions
        // null result only since test is not about checking handling of result
        Intents.intending(IntentMatchers.hasAction(REQUEST_PERMISSIONS))
            .respondWith(Instrumentation.ActivityResult(FragmentActivity.RESULT_OK, null))

        // simulate return of ScanActivity
        val scanActivityResult = Intent()
        val testImage = Uri.parse(PDFHandlerActivity.FILE_SCHEME + "://testPath")
        scanActivityResult.putExtra(ScanConstants.SCANNED_RESULT, testImage)
        Intents.intending(IntentMatchers.hasComponent(ScanActivity::class.java.name))
            .respondWith(
                Instrumentation.ActivityResult(
                    FragmentActivity.RESULT_OK,
                    scanActivityResult
                )
            )
        testClick(R.id.cameraButton)

        // if no permission yet, intent for permission request will be sent instead
        // TODO: add mocking of permission check
        if (ContextCompat.checkSelfPermission(
                mainActivity!!,
                WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                mainActivity!!,
                CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val permissionRequestIntent = Intents.getIntents()[0]
            Assert.assertNotNull(permissionRequestIntent)
            Assert.assertEquals(permissionRequestIntent.action, REQUEST_PERMISSIONS)
            val permissions = permissionRequestIntent.getStringArrayExtra(REQUEST_PERMISSIONS_NAMES)
            Assert.assertEquals(permissions!!.size.toLong(), 2)
            Assert.assertEquals(permissions[0], CAMERA)
            Assert.assertEquals(permissions[1], WRITE_EXTERNAL_STORAGE)
            return
        }

        // check that ScanActivity, PdfHandlerActivity, MainActivity intents were sent
        Assert.assertEquals(Intents.getIntents().size.toLong(), 3)

        // check that intent for ScanActivity was sent
        val scanActivityIntent = Intents.getIntents()[0]
        Assert.assertNotNull(scanActivityIntent)
        Assert.assertEquals(scanActivityIntent.component!!.className, ScanActivity::class.java.name)
        Assert.assertEquals(
            scanActivityIntent.getIntExtra(ScanConstants.OPEN_INTENT_PREFERENCE, 0).toLong(),
            ScanConstants.OPEN_CAMERA.toLong()
        )

        // check that intent for PdfHandlerActivity was sent
        val pdfHandlerIntent = Intents.getIntents()[1]
        Assert.assertNotNull(pdfHandlerIntent)
        Assert.assertEquals(
            pdfHandlerIntent.component!!.className,
            PDFHandlerActivity::class.java.name
        )
        Assert.assertEquals(
            pdfHandlerIntent.getIntExtra(AppConstants.EXTRA_FILE_FROM_PICKER, 0).toLong(),
            HomeFragment.IMAGE_FROM_CAMERA.toLong()
        )
        Assert.assertEquals(pdfHandlerIntent.data, testImage)

        // check that intent for MainActivity was sent
        val mainActivityIntent = Intents.getIntents()[2]
        Assert.assertNotNull(mainActivityIntent)
        Assert.assertEquals(
            mainActivityIntent.component!!.className,
            MainActivity::class.java.name
        )
        Assert.assertEquals(
            pdfHandlerIntent.getIntExtra(AppConstants.EXTRA_FILE_FROM_PICKER, 0).toLong(),
            HomeFragment.IMAGE_FROM_CAMERA.toLong()
        )
        Assert.assertEquals(pdfHandlerIntent.data, testImage)
    }
}