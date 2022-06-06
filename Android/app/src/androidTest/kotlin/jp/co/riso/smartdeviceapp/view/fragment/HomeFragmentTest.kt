package jp.co.riso.smartdeviceapp.view.fragment

import android.Manifest.permission.CAMERA
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.AlertDialog
import android.app.Instrumentation
import android.content.ClipData
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.TextView
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

    private fun grantPermissions() {
        /** Use if default state, permissions are not granted
         * Not yet working. Clearing of permissions is needed at the end of each test, however, this also clears coverage report generated in app storage
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("pm grant ${mainActivity!!.packageName} android.permission.WRITE_EXTERNAL_STORAGE")
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("pm grant ${mainActivity!!.packageName} android.permission.CAMERA")
        waitFewSeconds()
         */
    }
    /** Use if default state, permissions are not granted
     * Not yet working. Clearing of permissions is needed at the end of each test, however, this also clears coverage report generated in app storage
    private fun grantStoragePermission() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        if (Build.VERSION.SDK_INT >= 23) {
            val allowPermission = UiDevice.getInstance(instrumentation).findObject(
                UiSelector().text(
                    "Allow"
                )
            )
            if (allowPermission.exists()) {
                allowPermission.click()
            }
        }
    }

    private fun grantCameraPermission() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        if (Build.VERSION.SDK_INT >= 23) {
            val allowPermission = UiDevice.getInstance(instrumentation).findObject(
                UiSelector().text(
                    when {
                        Build.VERSION.SDK_INT == 23 -> "Allow"
                        Build.VERSION.SDK_INT <= 28 -> "ALLOW"
                        Build.VERSION.SDK_INT == 29 -> "Allow only while using the app"
                        else -> "While using the app"
                    }
                )
            )
            if (allowPermission.exists()) {
                allowPermission.click()
            }
        }
    }

    fun denyPermission() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        if (Build.VERSION.SDK_INT >= 23) {
            val denyPermission = UiDevice.getInstance(instrumentation).findObject(UiSelector().text(
                when {
                    Build.VERSION.SDK_INT in 24..28 -> "DENY"
                    else -> "Deny"
                }
            ))
            if (denyPermission.exists()) {
                denyPermission.click()
            }
        }
    }

    private fun checkIntent_StoragePermission(intent: Intent) {
        Assert.assertNotNull(intent)
        Assert.assertEquals(intent.action, REQUEST_PERMISSIONS)
        val permissions = intent.getStringArrayExtra(REQUEST_PERMISSIONS_NAMES)
        Assert.assertEquals(1, permissions!!.size.toLong())
        Assert.assertEquals(WRITE_EXTERNAL_STORAGE, permissions[0])
    }

    private fun checkIntent_CameraPermission(intent: Intent) {
        Assert.assertNotNull(intent)
        Assert.assertEquals(REQUEST_PERMISSIONS, intent.action)
        val permissions = intent.getStringArrayExtra(REQUEST_PERMISSIONS_NAMES)
        Assert.assertEquals(2, permissions!!.size.toLong())
        Assert.assertEquals(CAMERA, permissions[0])
        Assert.assertEquals(WRITE_EXTERNAL_STORAGE, permissions[1])
    }
     */

    private fun checkIntent_DocumentPicker(intent: Intent) {
        Assert.assertNotNull(intent)
        Assert.assertEquals(Intent.ACTION_CHOOSER, intent.action)
        val extraIntent = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
        Assert.assertNotNull(extraIntent)
        Assert.assertEquals(Intent.ACTION_GET_CONTENT, extraIntent!!.action)
        Assert.assertEquals("*/*", extraIntent.type)
        if (_homeFragment!!.isChromeBook) {
            Assert.assertNull(extraIntent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES))
        } else {
            Assert.assertArrayEquals(
                AppConstants.DOC_TYPES,
                extraIntent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES)
            )
        }
    }

    private fun checkIntent_ImagePicker(intent: Intent) {
        Assert.assertNotNull(intent)
        Assert.assertEquals(Intent.ACTION_CHOOSER, intent.action)
        val extraIntent = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
        Assert.assertNotNull(extraIntent)
        Assert.assertEquals(Intent.ACTION_GET_CONTENT, extraIntent!!.action)
        Assert.assertEquals("*/*", extraIntent.type)
        Assert.assertTrue(extraIntent.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false))
        if (_homeFragment!!.isChromeBook) {
            Assert.assertNull(extraIntent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES))
        } else {
            Assert.assertArrayEquals(
                AppConstants.IMAGE_TYPES,
                extraIntent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES)
            )
        }
    }

    private fun checkIntent_ScanActivity(intent: Intent) {
        Assert.assertNotNull(intent)
        Assert.assertEquals(
            ScanActivity::class.java.name,
            intent.component!!.className)
        Assert.assertEquals(
            ScanConstants.OPEN_CAMERA.toLong(),
            intent.getIntExtra(ScanConstants.OPEN_INTENT_PREFERENCE, 0).toLong())
    }

    @Test
    fun testNewInstance() {
        Assert.assertNotNull(_homeFragment)
    }

    /** Not yet working. Clearing of permissions is needed at the end of each test, however, this also clears coverage report
    @Test
    fun testPermissionsAllow_SelectDocument() {
        val result = Instrumentation.ActivityResult(FragmentActivity.RESULT_OK, null)
        Intents.intending(IntentMatchers.hasAction(Intent.ACTION_CHOOSER)).respondWith(result)

        testClick(R.id.fileButton)

        // Check permission not yet granted
        Assert.assertTrue(
            ContextCompat.checkSelfPermission(
                mainActivity!!,
                WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        )
        checkIntent_StoragePermission(Intents.getIntents()[0])
        grantStoragePermission()

        // check that picker intent was sent
        checkIntent_DocumentPicker(Intents.getIntents()[1])
    }

    @Test
    fun testPermissionsAllow_SelectPhotos() {
        val result = Instrumentation.ActivityResult(FragmentActivity.RESULT_OK, null)
        Intents.intending(IntentMatchers.hasAction(Intent.ACTION_CHOOSER)).respondWith(result)

        testClick(R.id.photosButton)

        // Check permission not yet granted
        Assert.assertTrue(
            ContextCompat.checkSelfPermission(
                mainActivity!!,
                WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        )
        checkIntent_StoragePermission(Intents.getIntents()[0])
        grantStoragePermission()

        // check that picker intent was sent
        checkIntent_ImagePicker(Intents.getIntents()[1])
    }

    @Test
    fun testPermissionsAllow_CapturePhoto() {
        val result = Intent()
        val testFile = Uri.fromFile(File(getPath(IMG_BMP)))
        result.putExtra(ScanConstants.SCANNED_RESULT, testFile)
        Intents.intending(IntentMatchers.hasComponent(ScanActivity::class.java.name))
            .respondWith(
                Instrumentation.ActivityResult(
                    FragmentActivity.RESULT_OK,
                    result
                )
            )

        testClick(R.id.cameraButton)

        // Check permission not yet granted
        Assert.assertTrue(
            ContextCompat.checkSelfPermission(
                mainActivity!!,
                WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                mainActivity!!,
                CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        )
        checkIntent_CameraPermission(Intents.getIntents()[0])
        grantStoragePermission()
        grantCameraPermission()

        // check that picker intent was sent
        checkIntent_ScanActivity(Intents.getIntents()[1])
    }
     */



    @Test
    fun testOnClick_SelectDocument() {
        grantPermissions()
        // stubs handling of start activity to prevent launching of OS picker
        // null result only since test is not about checking handling of result
        val result = Instrumentation.ActivityResult(FragmentActivity.RESULT_OK, null)
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result)
        testClick(R.id.fileButton)

        // check intent that was sent
        checkIntent_DocumentPicker(Intents.getIntents()[0])
    }

    @Test
    fun testOnClick_SelectPhotos() {
        grantPermissions()

        // stubs handling of start activity to prevent launching of OS picker
        // null result only since test is not about checking handling of result
        val result = Instrumentation.ActivityResult(FragmentActivity.RESULT_OK, null)
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result)
        testClick(R.id.photosButton)

        // check intent that was sent
        checkIntent_ImagePicker(Intents.getIntents()[0])
    }

    @Test
    fun testOnClick_CapturePhoto() {
        grantPermissions()
        // stubs handling of permission request in case of no permissions
        // null result only since test is not about checking handling of result
        Intents.intending(IntentMatchers.hasAction(REQUEST_PERMISSIONS))
            .respondWith(Instrumentation.ActivityResult(FragmentActivity.RESULT_OK, null))

        // simulate return of ScanActivity
        val scanActivityResult = Intent()
        val testFile = Uri.fromFile(File(getPath(IMG_BMP)))
        scanActivityResult.putExtra(ScanConstants.SCANNED_RESULT, testFile)
        Intents.intending(IntentMatchers.hasComponent(ScanActivity::class.java.name))
            .respondWith(
                Instrumentation.ActivityResult(
                    FragmentActivity.RESULT_OK,
                    scanActivityResult
                )
            )
        testClick(R.id.cameraButton)

        // check that ScanActivity, PdfHandlerActivity, MainActivity intents were sent
        Assert.assertEquals(3, Intents.getIntents().size.toLong())

        // check that intent for ScanActivity was sent
        checkIntent_ScanActivity(Intents.getIntents()[0])

        // check that intent for PdfHandlerActivity was sent
        val pdfHandlerIntent = Intents.getIntents()[1]
        Assert.assertNotNull(pdfHandlerIntent)
        Assert.assertEquals(
            PDFHandlerActivity::class.java.name,
            pdfHandlerIntent.component!!.className
        )
        Assert.assertEquals(
            HomeFragment.IMAGE_FROM_CAMERA.toLong(),
            pdfHandlerIntent.getIntExtra(AppConstants.EXTRA_FILE_FROM_PICKER, 0).toLong(),
        )
        Assert.assertEquals(testFile, pdfHandlerIntent.data)

        // check that intent for MainActivity was sent
        val mainActivityIntent = Intents.getIntents()[2]
        Assert.assertNotNull(mainActivityIntent)
        Assert.assertEquals(
            MainActivity::class.java.name,
            mainActivityIntent.component!!.className
        )
        Assert.assertEquals(
            HomeFragment.IMAGE_FROM_CAMERA.toLong(),
            mainActivityIntent.getIntExtra(AppConstants.EXTRA_FILE_FROM_PICKER, 0).toLong()
        )
        Assert.assertEquals(testFile, mainActivityIntent.data)
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
    fun testSelectDocument() {
        grantPermissions()
        val intent = Intent()
        val testFile = File(getPath(DOC_PDF))
        intent.data = Uri.fromFile(testFile)
        val result = Instrumentation.ActivityResult(FragmentActivity.RESULT_OK, intent)
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result)
        testClick(R.id.fileButton)

        // check that File Picker, PdfHandlerActivity intents were sent
        Assert.assertEquals(Intents.getIntents().size.toLong(), 2)

        checkIntent_DocumentPicker(Intents.getIntents()[0])

        val pdfHandlerIntent = Intents.getIntents()[1]
        Assert.assertNotNull(pdfHandlerIntent)
        Assert.assertEquals(
            PDFHandlerActivity::class.java.name,
            pdfHandlerIntent.component!!.className
        )
        Assert.assertEquals(
            HomeFragment.PDF_FROM_PICKER.toLong(),
            pdfHandlerIntent.getIntExtra(AppConstants.EXTRA_FILE_FROM_PICKER, 0).toLong()
        )
        Assert.assertEquals(getPath(DOC_PDF), pdfHandlerIntent.data!!.path)
    }

    @Test
    fun testSelectDocument_Invalid() {
        grantPermissions()
        val intent = Intent()
        val testFile = File(getPath(IMG_PNG))
        intent.data = Uri.fromFile(testFile)
        val result = Instrumentation.ActivityResult(FragmentActivity.RESULT_OK, intent)
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result)
        testClick(R.id.fileButton)

        // check that only File Picker intent was sent
        Assert.assertEquals(1, Intents.getIntents().size.toLong())

        val fragment = mainActivity!!.supportFragmentManager.findFragmentByTag(
            FRAGMENT_TAG_DIALOG
        )
        Assert.assertTrue(fragment is DialogFragment)
        Assert.assertTrue((fragment as DialogFragment?)!!.showsDialog)
        val dialog = fragment!!.dialog as AlertDialog?
        val msg = dialog!!.findViewById<View>(android.R.id.message)
        Assert.assertEquals(
            SmartDeviceApp.appContext!!.resources.getString(R.string.ids_err_msg_open_failed),
            (msg as TextView).text
        )
        val b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        Assert.assertEquals(
            SmartDeviceApp.appContext!!.resources.getString(R.string.ids_lbl_ok),
            b.text
        )
    }

    @Test
    fun testSelectDocument_TextFile() {
        grantPermissions()
        val intent = Intent()
        val testFile = File(getPath(DOC_TXT))
        intent.data = Uri.fromFile(testFile)
        val result = Instrumentation.ActivityResult(FragmentActivity.RESULT_OK, intent)
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result)
        testClick(R.id.fileButton)

        // check that File Picker, PdfHandlerActivity intents were sent
        Assert.assertEquals(Intents.getIntents().size.toLong(), 2)

        checkIntent_DocumentPicker(Intents.getIntents()[0])

        val pdfHandlerIntent = Intents.getIntents()[1]
        Assert.assertNotNull(pdfHandlerIntent)
        Assert.assertEquals(
            PDFHandlerActivity::class.java.name,
            pdfHandlerIntent.component!!.className
        )
        Assert.assertEquals(
            HomeFragment.TEXT_FROM_PICKER.toLong(),
            pdfHandlerIntent.getIntExtra(AppConstants.EXTRA_FILE_FROM_PICKER, 0).toLong()
        )
        Assert.assertEquals(getPath(DOC_TXT), pdfHandlerIntent.data!!.path)
    }

    @Test
    fun testSelectPhotos() {
        grantPermissions()
        val intent = Intent()
        val testFile = File(getPath(IMG_PNG))
        intent.data = Uri.fromFile(testFile)
        val result = Instrumentation.ActivityResult(FragmentActivity.RESULT_OK, intent)
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result)
        testClick(R.id.photosButton)

        // check that File Picker, PdfHandlerActivity intents were sent
        Assert.assertEquals(2, Intents.getIntents().size.toLong())

        checkIntent_ImagePicker(Intents.getIntents()[0])

        val pdfHandlerIntent = Intents.getIntents()[1]
        Assert.assertNotNull(pdfHandlerIntent)
        Assert.assertEquals(
            PDFHandlerActivity::class.java.name,
            pdfHandlerIntent.component!!.className
        )
        Assert.assertEquals(
            HomeFragment.IMAGE_FROM_PICKER.toLong(),
            pdfHandlerIntent.getIntExtra(AppConstants.EXTRA_FILE_FROM_PICKER, 0).toLong()
        )
        Assert.assertEquals(getPath(IMG_PNG), pdfHandlerIntent.data!!.path)
    }

    @Test
    fun testSelectPhotos_Invalid() {
        grantPermissions()
        val intent = Intent()
        val testFile = File(getPath(DOC_PDF))
        intent.data = Uri.fromFile(testFile)
        val result = Instrumentation.ActivityResult(FragmentActivity.RESULT_OK, intent)
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result)
        testClick(R.id.photosButton)

        // check that only File Picker intent was sent
        Assert.assertEquals(1, Intents.getIntents().size.toLong())

        checkIntent_ImagePicker(Intents.getIntents()[0])

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
    fun testSelectPhotosMultiple() {
        grantPermissions()
        val testFiles = ClipData.newUri(
            SmartDeviceApp.appContext!!.contentResolver,
            getPath(IMG_BMP),
            Uri.fromFile(File(getPath(IMG_BMP))))
        testFiles.addItem(ClipData.Item(Uri.fromFile(File(getPath(IMG_GIF)))))
        testFiles.addItem(ClipData.Item(Uri.fromFile(File(getPath(IMG_PNG)))))
        val intent = Intent()
        intent.clipData = testFiles
        val result = Instrumentation.ActivityResult(FragmentActivity.RESULT_OK, intent)
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result)
        testClick(R.id.photosButton)

        // check that File Picker, PdfHandlerActivity intents were sent
        Assert.assertEquals(2, Intents.getIntents().size.toLong())

        checkIntent_ImagePicker(Intents.getIntents()[0])

        val pdfHandlerIntent = Intents.getIntents()[1]
        Assert.assertNotNull(pdfHandlerIntent)
        Assert.assertEquals(
            PDFHandlerActivity::class.java.name,
            pdfHandlerIntent.component!!.className
        )
        Assert.assertEquals(
            HomeFragment.IMAGES_FROM_PICKER.toLong(),
            pdfHandlerIntent.getIntExtra(AppConstants.EXTRA_FILE_FROM_PICKER, 0).toLong()
        )
        Assert.assertEquals(testFiles.itemCount, pdfHandlerIntent.clipData!!.itemCount)
        for (i in 0 until testFiles.itemCount) {
            Assert.assertEquals(testFiles.getItemAt(i), pdfHandlerIntent.clipData!!.getItemAt(i))
        }
    }


    @Test
    fun testSelectPhotosMultiple_Invalid() {
        grantPermissions()
        val testFiles = ClipData.newUri(
                SmartDeviceApp.appContext!!.contentResolver,
                getPath(IMG_BMP),
                Uri.fromFile(File(getPath(IMG_BMP))))
        testFiles.addItem(ClipData.Item(Uri.fromFile(File(getPath(IMG_GIF)))))
        testFiles.addItem(ClipData.Item(Uri.fromFile(File(getPath(DOC_PDF)))))
        val intent = Intent()
        intent.clipData = testFiles
        val result = Instrumentation.ActivityResult(FragmentActivity.RESULT_OK, intent)
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result)
        testClick(R.id.photosButton)

        // check that only File Picker intent was sent
        Assert.assertEquals(1, Intents.getIntents().size.toLong())

        checkIntent_ImagePicker(Intents.getIntents()[0])

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
        grantPermissions()

        // stubs handling of permission request in case of no permissions
        // null result only since test is not about checking handling of result
        Intents.intending(IntentMatchers.hasAction(REQUEST_PERMISSIONS))
            .respondWith(Instrumentation.ActivityResult(FragmentActivity.RESULT_OK, null))

        // simulate return of ScanActivity
        val scanActivityResult = Intent()
        val testFile = Uri.fromFile(File(getPath(IMG_BMP)))
        scanActivityResult.putExtra(ScanConstants.SCANNED_RESULT, testFile)
        Intents.intending(IntentMatchers.hasComponent(ScanActivity::class.java.name))
            .respondWith(
                Instrumentation.ActivityResult(
                    FragmentActivity.RESULT_OK,
                    scanActivityResult
                )
            )

        testClick(R.id.cameraButton)

        // check that ScanActivity, PdfHandlerActivity, MainActivity intents were sent
        Assert.assertEquals(3, Intents.getIntents().size.toLong())

        checkIntent_ScanActivity(Intents.getIntents()[0])

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
        Assert.assertEquals(pdfHandlerIntent.data, testFile)

    }

    // TODO: testIntent_Invalid
    // TODO: testPermission
}