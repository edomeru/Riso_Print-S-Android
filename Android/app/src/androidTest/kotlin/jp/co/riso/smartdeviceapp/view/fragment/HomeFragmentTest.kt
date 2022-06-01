package jp.co.riso.smartdeviceapp.view.fragment

import android.Manifest.permission.CAMERA
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.AlertDialog
import android.app.Instrumentation
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.scanlibrary.ScanActivity
import com.scanlibrary.ScanConstants
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import jp.co.riso.smartdeviceapp.view.MainActivity
import jp.co.riso.smartdeviceapp.view.PDFHandlerActivity
import jp.co.riso.smartdeviceapp.view.fragment.HomeFragment.Companion.FRAGMENT_TAG_DIALOG
import jp.co.riso.smartprint.R
import org.junit.*
import java.io.File
import java.io.IOException


class HomeFragmentTest : BaseActivityTestUtil() {

    private var _homeFragment: HomeFragment? = null
    private val REQUEST_PERMISSIONS = "android.content.pm.action.REQUEST_PERMISSIONS"
    private val REQUEST_PERMISSIONS_NAMES = "android.content.pm.extra.REQUEST_PERMISSIONS_NAMES"

    @get:Rule
    var storagePermission: GrantPermissionRule = GrantPermissionRule.grant(WRITE_EXTERNAL_STORAGE)

    // HIDE_NEW_FEATURES comment when CAMERA permission is hidden feature. uncomment when new features are restored
    @get:Rule
    var cameraPermission: GrantPermissionRule = GrantPermissionRule.grant(CAMERA)

    @Before
    fun initEspresso() {
        Intents.init()
    }

    @After
    fun releaseEspresso() {
        Intents.release()
    }

    @Before
    fun setup() {
        wakeUpScreen()
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
            Assert.assertArrayEquals(
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
            mainActivityIntent.getIntExtra(AppConstants.EXTRA_FILE_FROM_PICKER, 0).toLong(),
            HomeFragment.IMAGE_FROM_CAMERA.toLong()
        )
        Assert.assertEquals(mainActivityIntent.data, testImage)
    }

    @Test
    fun testOrientationChange_SelectDocument() {
        switchOrientation()
        waitForAnimation()
        testOnClick_SelectDocument()
    }

    @Test
    fun testOrientationChange_SelectPhotos() {
        switchOrientation()
        waitForAnimation()
        testOnClick_SelectPhotos()
    }

    @Test
    fun testOrientationChange_CapturePhoto() {
        switchOrientation()
        waitForAnimation()
        testOnClick_CapturePhoto()
    }

    @Test
    fun testOpenFile_SelectDocument() {
        val intent = Intent()
        intent.putExtra("select-document", true)
        val testFile = File(mainActivity!!.getExternalFilesDir(null), "tmp.pdf")
        try {
            testFile.createNewFile()
        } catch (e: IOException) {
            Log.e("HomeFragmentTest", "createNewFile", e)
        }
        intent.data = Uri.fromFile(testFile)

        val result = Instrumentation.ActivityResult(FragmentActivity.RESULT_OK, intent)
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result)
        testClick(R.id.fileButton)

        // check that File Picker, PdfHandlerActivity intents were sent
        Assert.assertEquals(Intents.getIntents().size.toLong(), 2)

        // File Picker intent is checked in testOnClick_SelectDocument

        val pdfHandlerIntent = Intents.getIntents()[1]
        Assert.assertNotNull(pdfHandlerIntent)
        Assert.assertEquals(
            pdfHandlerIntent.component!!.className,
            PDFHandlerActivity::class.java.name
        )
        Assert.assertEquals(
            pdfHandlerIntent.getIntExtra(AppConstants.EXTRA_FILE_FROM_PICKER, 0).toLong(),
            HomeFragment.PDF_FROM_PICKER.toLong()
        )
        Assert.assertEquals(pdfHandlerIntent.data!!.path, testFile.path)
    }

    @Test
    fun testSelectPhotos_Valid() {
        val intent = Intent()
        intent.putExtra("select-photos", true)
        val testFile = File(mainActivity!!.getExternalFilesDir(null), "tmp.jpg")
        try {
            testFile.createNewFile()
        } catch (e: IOException) {
            Log.e("HomeFragmentTest", "createNewFile", e)
        }
        intent.data = Uri.fromFile(testFile)

        val result = Instrumentation.ActivityResult(FragmentActivity.RESULT_OK, intent)
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result)
        testClick(R.id.photosButton)

        // check that File Picker, PdfHandlerActivity intents were sent
        Assert.assertEquals(Intents.getIntents().size.toLong(), 2)

        // File Picker intent is checked in testOnClick_SelectPhotos

        val pdfHandlerIntent = Intents.getIntents()[1]
        Assert.assertNotNull(pdfHandlerIntent)
        Assert.assertEquals(
            pdfHandlerIntent.component!!.className,
            PDFHandlerActivity::class.java.name
        )
        Assert.assertEquals(
            pdfHandlerIntent.getIntExtra(AppConstants.EXTRA_FILE_FROM_PICKER, 0).toLong(),
            HomeFragment.IMAGE_FROM_PICKER.toLong()
        )
        Assert.assertEquals(pdfHandlerIntent.data!!.path, testFile.path)
    }

    @Test
    fun testSelectPhotos_Invalid() {
        val intent = Intent()
        intent.putExtra("select-photos", true)
        val testFile = File(mainActivity!!.getExternalFilesDir(null), "tmp.dat")
        try {
            testFile.createNewFile()
        } catch (e: IOException) {
            Log.e("HomeFragmentTest", "createNewFile", e)
        }
        intent.data = Uri.fromFile(testFile)

        val result = Instrumentation.ActivityResult(FragmentActivity.RESULT_OK, intent)
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result)
        testClick(R.id.photosButton)

        // check that only File Picker intent was sent
        Assert.assertEquals(Intents.getIntents().size.toLong(), 1)

        // File Picker intent is checked in testOnClick_SelectPhotos

        val fragment = mainActivity!!.supportFragmentManager.findFragmentByTag(
            FRAGMENT_TAG_DIALOG
        )
        Assert.assertTrue(fragment is DialogFragment)
        Assert.assertTrue((fragment as DialogFragment?)!!.showsDialog)
        val dialog = fragment!!.dialog as AlertDialog?
        val msg = dialog!!.findViewById<View>(android.R.id.message)
        Assert.assertEquals(
            SmartDeviceApp.appContext!!.resources.getString(R.string.ids_err_msg_invalid_file_selection),
            (msg as TextView).text
        )
        val b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        Assert.assertEquals(
            SmartDeviceApp.appContext!!.resources.getString(R.string.ids_lbl_ok),
            b.text
        )
    }

/* TODO:
    @Test
    fun testOpenFile_SelectPhotosMultipleValid() {
 */

    @Test
    fun testSelectPhotos_MultipleInvalid() {
        val bundle = Bundle()
        val parcels: ArrayList<Parcelable> = ArrayList()
        val intent = Intent()
        intent.putExtra("select-photos-multiple", true)
        for (i in 1..5) {
            val testFile = File(mainActivity!!.getExternalFilesDir(null), "tmp$i.dat")
            try {
                testFile.createNewFile()
            } catch (e: IOException) {
                Log.e("HomeFragmentTest", "createNewFile", e)
            }
            val uri = Uri.fromFile(testFile)
            parcels.add(uri)
        }
        bundle.putParcelableArrayList(Intent.EXTRA_STREAM, parcels)
        intent.putExtras(bundle)

        val result = Instrumentation.ActivityResult(FragmentActivity.RESULT_OK, intent)
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result)
        testClick(R.id.photosButton)

        // check that only File Picker intent was sent
        Assert.assertEquals(1, Intents.getIntents().size.toLong())

        // File Picker intent is already checked in testOnClick_SelectPhotos

        val fragment = mainActivity!!.supportFragmentManager.findFragmentByTag(
            FRAGMENT_TAG_DIALOG
        )
        Assert.assertTrue(fragment is DialogFragment)
        Assert.assertTrue((fragment as DialogFragment?)!!.showsDialog)
        val dialog = fragment!!.dialog as AlertDialog?
        val msg = dialog!!.findViewById<View>(android.R.id.message)
        Assert.assertEquals(
            SmartDeviceApp.appContext!!.resources.getString(R.string.ids_err_msg_invalid_file_selection),
            (msg as TextView).text
        )
        val b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        Assert.assertEquals(
            SmartDeviceApp.appContext!!.resources.getString(R.string.ids_lbl_ok),
            b.text
        )
    }

    @Test
    fun testCapturePhoto() {
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
        Intents.intending(IntentMatchers.hasAction(REQUEST_PERMISSIONS))
            .respondWith(
                Instrumentation.ActivityResult(
                    FragmentActivity.RESULT_OK,
                    scanActivityResult))
        testClick(R.id.cameraButton)

        // check that ScanActivity, PdfHandlerActivity, MainActivity intents were sent
        Assert.assertEquals(3, Intents.getIntents().size.toLong())

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

    }

    // TODO: testIntent_Invalid
    // TODO: testPermission
}