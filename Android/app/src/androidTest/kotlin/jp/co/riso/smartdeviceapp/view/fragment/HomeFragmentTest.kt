package jp.co.riso.smartdeviceapp.view.fragment

import android.Manifest.permission.CAMERA
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkStatic
import jp.co.riso.android.dialog.ConfirmDialogFragment
import jp.co.riso.android.dialog.WaitingDialogFragment
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.MockTestUtil
import jp.co.riso.smartdeviceapp.ReflectionTestUtil
import jp.co.riso.smartdeviceapp.controller.print.ContentPrintManager
import jp.co.riso.smartdeviceapp.controller.print.ContentPrintManagerTest
import jp.co.riso.smartdeviceapp.model.ContentPrintFile
import jp.co.riso.smartdeviceapp.view.MainActivity
import jp.co.riso.smartprint.R
import org.junit.*

class HomeFragmentTest {
    // ================================================================================
    // Tests - initializeFragment
    // ================================================================================
    @Test
    fun testInitializeFragment_NullIntent() {
        ContentPrintManager.filenameFromNotification = null
        val fragment = spyk<HomeFragment>()
        val mockActivity = mockFragmentActivity()
        every { mockActivity.intent } returns null
        addActivity(fragment, mockActivity)
        val bundle = mockk<Bundle>()
        val view = mockView()
        fragment.initializeFragment(bundle)
        fragment.initializeView(view, bundle)
        fragment.initializeCustomActionBar(view, bundle)
        Assert.assertNotNull(fragment)
        Assert.assertEquals(fragment.viewLayout, R.layout.fragment_home)
    }

    @Test
    fun testInitializeFragment_NullExtras() {
        ContentPrintManager.filenameFromNotification = null
        val fragment = spyk<HomeFragment>()
        val mockActivity = mockFragmentActivity()
        val mockIntent = mockk<Intent>()
        every { mockIntent.extras } returns null
        every { mockActivity.intent } returns mockIntent
        addActivity(fragment, mockActivity)
        val bundle = mockk<Bundle>()
        val view = mockView()
        fragment.initializeFragment(bundle)
        fragment.initializeView(view, bundle)
        fragment.initializeCustomActionBar(view, bundle)
        Assert.assertNotNull(fragment)
        Assert.assertEquals(fragment.viewLayout, R.layout.fragment_home)
    }

    @Test
    fun testInitializeFragment_NullExtraString() {
        ContentPrintManager.filenameFromNotification = null
        val fragment = spyk<HomeFragment>()
        val mockActivity = mockFragmentActivity()
        val mockIntent = mockk<Intent>()
        val mockExtras = mockk<Bundle>(relaxed = true)
        every { mockExtras.getString(any()) } returns null
        every { mockIntent.extras } returns mockExtras
        every { mockActivity.intent } returns mockIntent
        addActivity(fragment, mockActivity)
        val bundle = mockk<Bundle>()
        val view = mockView()
        fragment.initializeFragment(bundle)
        fragment.initializeView(view, bundle)
        fragment.initializeCustomActionBar(view, bundle)
        Assert.assertNotNull(fragment)
        Assert.assertEquals(fragment.viewLayout, R.layout.fragment_home)
    }

    @Test
    fun testInitializeFragment_InvalidExtraString() {
        ContentPrintManager.filenameFromNotification = null
        val fragment = spyk<HomeFragment>()
        val mockActivity = mockFragmentActivity()
        val mockIntent = mockk<Intent>(relaxed = true)
        val mockExtras = mockk<Bundle>(relaxed = true)
        every { mockExtras.getString(any()) } returns AppConstants.ERR_KEY_INVALID_INTENT
        every { mockIntent.extras } returns mockExtras
        every { mockActivity.intent } returns mockIntent
        addActivity(fragment, mockActivity)
        val bundle = mockk<Bundle>()
        val view = mockView()
        fragment.initializeFragment(bundle)
        fragment.initializeView(view, bundle)
        fragment.initializeCustomActionBar(view, bundle)
        Assert.assertNotNull(fragment)
        Assert.assertEquals(fragment.viewLayout, R.layout.fragment_home)
    }

    @Test
    fun testInitializeFragment_OtherExtraString() {
        ContentPrintManager.filenameFromNotification = null
        val fragment = spyk<HomeFragment>()
        val mockActivity = mockFragmentActivity()
        val mockIntent = mockk<Intent>(relaxed = true)
        val mockExtras = mockk<Bundle>(relaxed = true)
        every { mockExtras.getString(any()) } returns "Test"
        every { mockIntent.extras } returns mockExtras
        every { mockActivity.intent } returns mockIntent
        addActivity(fragment, mockActivity)
        val bundle = mockk<Bundle>()
        val view = mockView()
        fragment.initializeFragment(bundle)
        fragment.initializeView(view, bundle)
        fragment.initializeCustomActionBar(view, bundle)
        Assert.assertNotNull(fragment)
        Assert.assertEquals(fragment.viewLayout, R.layout.fragment_home)
    }

    @Test
    fun testInitializeFragment_PushNotification_NotLoggedIn() {
        ContentPrintManager.filenameFromNotification = TEST_FILE_NAME
        ContentPrintManager.isLoggedIn = false
        ContentPrintManager.application = application
        val fragment = spyk<HomeFragment>()
        addActivity(fragment)
        val bundle = mockk<Bundle>()
        val view = mockView()
        fragment.initializeFragment(bundle)
        fragment.initializeView(view, bundle)
        fragment.initializeCustomActionBar(view, bundle)
        Assert.assertNotNull(fragment)
        Assert.assertEquals(fragment.viewLayout, R.layout.fragment_home)
    }

    @Test
    fun testInitializeFragment_PushNotification_IsLoggedIn() {
        ContentPrintManager.filenameFromNotification = TEST_FILE_NAME
        ContentPrintManager.isLoggedIn = true
        val fragment = spyk<HomeFragment>()
        addActivity(fragment)
        val bundle = mockk<Bundle>()
        val view = mockView()
        fragment.initializeFragment(bundle)
        fragment.initializeView(view, bundle)
        fragment.initializeCustomActionBar(view, bundle)
        Assert.assertNotNull(fragment)
        Assert.assertEquals(fragment.viewLayout, R.layout.fragment_home)
    }

    // ================================================================================
    // Tests - onClick
    // ================================================================================
    @Test
    fun testOnClick_FileButton() {
        val fragment = spyk<HomeFragment>()
        addActivity(fragment)
        val view = mockk<View>(relaxed = true)
        every { view.id } returns R.id.fileButton
        fragment.onClick(view)
        fragment.onClick(view)
        Assert.assertFalse(ContentPrintManager.isFileFromContentPrint)
        Assert.assertFalse(ContentPrintManager.isBoxRegistrationMode)
    }

    @Test
    fun testOnClick_PhotosButton() {
        val fragment = spyk<HomeFragment>()
        addActivity(fragment)
        val view = mockk<View>(relaxed = true)
        every { view.id } returns R.id.photosButton
        fragment.onClick(view)
        fragment.onClick(view)
        Assert.assertFalse(ContentPrintManager.isFileFromContentPrint)
        Assert.assertFalse(ContentPrintManager.isBoxRegistrationMode)
    }

    @Test
    fun testOnClick_ContentPrintButton() {
        val fragment = spyk<HomeFragment>()
        addActivity(fragment)
        addFragmentManager(fragment)
        val view = mockk<View>(relaxed = true)
        every { view.id } returns R.id.contentPrintButton
        fragment.onClick(view)
        fragment.onClick(view)
    }

    @Test
    fun testOnClick_Other() {
        val fragment = spyk<HomeFragment>()
        addActivity(fragment)
        val view = mockk<View>(relaxed = true)
        every { view.id } returns 0
        fragment.onClick(view)
    }

    // ================================================================================
    // Tests - onConfigurationChanged
    // ================================================================================
    @Test
    fun testOnClick_OnConfigurationChanged() {
        val fragment = spyk<HomeFragment>()
        addActivity(fragment)
        val mockView = mockView()
        every { fragment.view } returns mockView
        val mockLayout = mockk<LinearLayout>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_HOME_BUTTONS, mockLayout)
        val config = mockk<Configuration>()
        fragment.onConfigurationChanged(config)
    }

    // ================================================================================
    // Tests - onAuthenticationFinished
    // ================================================================================
    @Test
    fun testOnAuthenticationFinished_IsNotLoggedIn() {
        ContentPrintManager.isLoggedIn = false
        val fragment = spyk<HomeFragment>()
        addActivity(fragment)
        fragment.onAuthenticationStarted()
        fragment.onAuthenticationFinished()
    }

    @Test
    fun testOnAuthenticationFinished_NotPushNotification() {
        ContentPrintManager.isLoggedIn = true
        ContentPrintManager.filenameFromNotification = null
        val fragment = spyk<HomeFragment>()
        addActivity(fragment)
        fragment.onAuthenticationStarted()
        fragment.onAuthenticationFinished()
    }

    @Test
    fun testOnAuthenticationFinished_PushNotification() {
        ContentPrintManager.isLoggedIn = true
        ContentPrintManager.filenameFromNotification = TEST_FILE_NAME
        val fragment = spyk<HomeFragment>()
        addActivity(fragment)
        fragment.onAuthenticationStarted()
        fragment.onAuthenticationFinished()
    }

    @Test
    fun testOnAuthenticationFinished_InitPushNotification() {
        ContentPrintManager.isLoggedIn = true
        ContentPrintManager.filenameFromNotification = TEST_FILE_NAME
        val fragment = spyk<HomeFragment>()
        addActivity(fragment)
        val bundle = mockk<Bundle>(relaxed = true)
        fragment.initializeFragment(bundle)
        fragment.onAuthenticationStarted()
        fragment.onAuthenticationFinished()
    }

    // ================================================================================
    // Tests - onFileDownloaded
    // ================================================================================
    @Test
    fun testOnFileDownloaded_Success() {
        val fragment = spyk<HomeFragment>()
        addActivity(fragment)
        fragment.onFileListUpdated(true)
        val contentPrintFile = ContentPrintFile(0, TEST_FILE_NAME, null)
        fragment.onThumbnailDownloaded(contentPrintFile, TEST_FILE_NAME, true)
        fragment.onStartFileDownload()
        fragment.onFileDownloaded(contentPrintFile, TEST_FILE_NAME, true)
        Assert.assertTrue(ContentPrintManager.isFileFromContentPrint)
    }

    @Test
    fun testOnFileDownloaded_InitializedSuccess() {
        val fragment = spyk<HomeFragment>()
        addActivity(fragment)
        val bundle = mockk<Bundle>(relaxed = true)
        fragment.initializeFragment(bundle)
        fragment.onFileListUpdated(true)
        val contentPrintFile = ContentPrintFile(0, TEST_FILE_NAME, null)
        fragment.onThumbnailDownloaded(contentPrintFile, TEST_FILE_NAME, true)
        fragment.onStartFileDownload()
        fragment.onFileDownloaded(contentPrintFile, TEST_FILE_NAME, true)
        Assert.assertTrue(ContentPrintManager.isFileFromContentPrint)
    }

    @Test
    fun testOnFileDownloaded_Failed() {
        val fragment = spyk<HomeFragment>()
        addActivity(fragment)
        val bundle = mockk<Bundle>(relaxed = true)
        fragment.initializeFragment(bundle)
        fragment.onFileListUpdated(true)
        val contentPrintFile = ContentPrintFile(0, TEST_FILE_NAME, null)
        fragment.onThumbnailDownloaded(contentPrintFile, TEST_FILE_NAME, true)
        fragment.onStartFileDownload()
        fragment.onFileDownloaded(contentPrintFile, TEST_FILE_NAME, false)
        Assert.assertFalse(ContentPrintManager.isFileFromContentPrint)
    }

    // ================================================================================
    // Tests - onPrinterListUpdated
    // ================================================================================
    @Test
    fun testOnPrinterListUpdated_NotPushNotification() {
        ContentPrintManager.filenameFromNotification = null
        val fragment = spyk<HomeFragment>()
        addActivity(fragment)
        every { fragment.startActivity(any()) } just Runs
        fragment.onPrinterListUpdated(true)
    }

    @Test
    fun testOnPrinterListUpdated_Success() {
        ContentPrintManager.filePath = TEST_FILE_NAME
        val fragment = spyk<HomeFragment>()
        addActivity(fragment)
        every { fragment.startActivity(any()) } just Runs
        fragment.onPrinterListUpdated(true)
    }

    @Test
    fun testOnPrinterListUpdated_Failed() {
        ContentPrintManager.filenameFromNotification = TEST_FILE_NAME
        val fragment = spyk<HomeFragment>()
        addActivity(fragment)
        every { fragment.startActivity(any()) } just Runs
        fragment.onPrinterListUpdated(false)
    }

    // ================================================================================
    // Tests - onConfirm
    // ================================================================================
    @Test
    fun testOnConfirm() {
        val fragment = spyk<HomeFragment>()
        addActivity(fragment)
        fragment.onConfirm()
        fragment.onCancel()
    }

    // ================================================================================
    // Private Method Tests - downloadFileFromNotification
    // ================================================================================
    @Test
    fun testDownloadFileFromNotification_IsLoggedIn() {
        ContentPrintManager.isLoggedIn = true
        val fragment = spyk<HomeFragment>()
        addActivity(fragment)
        ReflectionTestUtil.callMethod(HomeFragment::class.java, fragment, METHOD_DOWNLOAD_FILE)
    }

    @Test
    fun testDownloadFileFromNotification_IsNotLoggedIn() {
        ContentPrintManager.isLoggedIn = false
        val fragment = spyk<HomeFragment>()
        addActivity(fragment)
        ReflectionTestUtil.callMethod(HomeFragment::class.java, fragment, METHOD_DOWNLOAD_FILE)
    }

    // ================================================================================
    // Private Method Tests - hideLoadingPreviewDialog
    // ================================================================================
    @Test
    fun testHideLoadingPreviewDialog_IsAdded() {
        val fragment = spyk<HomeFragment>()
        addActivity(fragment)
        every { fragment.isAdded } returns true
        val dialog = spyk<WaitingDialogFragment>()
        ReflectionTestUtil.setField(fragment, FIELD_DOWNLOAD_DIALOG, dialog)
        ReflectionTestUtil.callMethod(HomeFragment::class.java, fragment, METHOD_HIDE_LOADING_DIALOG)
    }

    @Test
    fun testHideLoadingPreviewDialog_IsNotAdded() {
        val fragment = spyk<HomeFragment>()
        addActivity(fragment)
        every { fragment.isAdded } returns false
        val dialog = spyk<WaitingDialogFragment>()
        ReflectionTestUtil.setField(fragment, FIELD_DOWNLOAD_DIALOG, dialog)
        ReflectionTestUtil.callMethod(HomeFragment::class.java, fragment, METHOD_HIDE_LOADING_DIALOG)
    }

    // ================================================================================
    // Private Method Tests - checkPermission
    // ================================================================================
    @Test
    fun testCheckPermission_IsNotStorageOnly_DeniedNoRequest() {
        val fragmentActivity = mockFragmentActivity()
        every {
            fragmentActivity.checkPermission(any(), any(), any())
        } returns PackageManager.PERMISSION_DENIED
        val fragment = spyk<HomeFragment>()
        every { fragment.requireActivity() } returns fragmentActivity
        every { fragment.shouldShowRequestPermissionRationale(any()) } returns false
        val isStorageOnly = false
        val result = ReflectionTestUtil.callMethod(HomeFragment::class.java, fragment,
            METHOD_CHECK_PERMISSION,
            ReflectionTestUtil.Param(Boolean::class.java, isStorageOnly)
        ) as Boolean
        Assert.assertFalse(result)
    }

    @Test
    fun testCheckPermission_IsNotStorageOnly_CameraDeniedWithCameraRequest_NoDialog() {
        val fragmentActivity = mockFragmentActivity()
        every {
            fragmentActivity.checkPermission(CAMERA, any(), any())
        } returns PackageManager.PERMISSION_GRANTED
        every {
            fragmentActivity.checkPermission(READ_MEDIA_IMAGES, any(), any())
        } returns PackageManager.PERMISSION_DENIED
        val fragment = spyk<HomeFragment>()
        every { fragment.requireActivity() } returns fragmentActivity
        every { fragment.shouldShowRequestPermissionRationale(CAMERA) } returns true
        every { fragment.shouldShowRequestPermissionRationale(READ_MEDIA_IMAGES) } returns false
        ReflectionTestUtil.setField(fragment, FIELD_CONFIRM_DIALOG, null)
        val isStorageOnly = false
        val result = ReflectionTestUtil.callMethod(HomeFragment::class.java, fragment,
            METHOD_CHECK_PERMISSION,
            ReflectionTestUtil.Param(Boolean::class.java, isStorageOnly)
        ) as Boolean
        Assert.assertFalse(result)
    }

    @Test
    fun testCheckPermission_IsNotStorageOnly_ReadDeniedWithReadRequest_WithDialog() {
        val fragmentActivity = mockFragmentActivity()
        every {
            fragmentActivity.checkPermission(CAMERA, any(), any())
        } returns PackageManager.PERMISSION_DENIED
        every {
            fragmentActivity.checkPermission(READ_MEDIA_IMAGES, any(), any())
        } returns PackageManager.PERMISSION_GRANTED
        val fragment = spyk<HomeFragment>()
        every { fragment.requireActivity() } returns fragmentActivity
        every { fragment.shouldShowRequestPermissionRationale(CAMERA) } returns false
        every { fragment.shouldShowRequestPermissionRationale(READ_MEDIA_IMAGES) } returns true
        val dialog = spyk<ConfirmDialogFragment>()
        ReflectionTestUtil.setField(fragment, FIELD_CONFIRM_DIALOG, dialog)
        val isStorageOnly = false
        val result = ReflectionTestUtil.callMethod(HomeFragment::class.java, fragment,
            METHOD_CHECK_PERMISSION,
            ReflectionTestUtil.Param(Boolean::class.java, isStorageOnly)
        ) as Boolean
        Assert.assertFalse(result)
    }

    @Test
    fun testCheckPermission_IsNotStorageOnly_Granted() {
        val fragmentActivity = mockFragmentActivity()
        every {
            fragmentActivity.checkPermission(any(), any(), any())
        } returns PackageManager.PERMISSION_GRANTED
        val fragment = spyk<HomeFragment>()
        every { fragment.requireActivity() } returns fragmentActivity
        val isStorageOnly = false
        val result = ReflectionTestUtil.callMethod(HomeFragment::class.java, fragment,
            METHOD_CHECK_PERMISSION,
            ReflectionTestUtil.Param(Boolean::class.java, isStorageOnly)
        ) as Boolean
        Assert.assertTrue(result)
    }

    @Test
    fun testCheckPermission_IsStorageOnly_Granted() {
        val fragmentActivity = mockFragmentActivity()
        every {
            fragmentActivity.checkPermission(any(), any(), any())
        } returns PackageManager.PERMISSION_GRANTED
        val fragment = spyk<HomeFragment>()
        every { fragment.requireActivity() } returns fragmentActivity
        val isStorageOnly = true
        val result = ReflectionTestUtil.callMethod(HomeFragment::class.java, fragment,
            METHOD_CHECK_PERMISSION,
            ReflectionTestUtil.Param(Boolean::class.java, isStorageOnly)
        ) as Boolean
        Assert.assertTrue(result)
    }

    @Test
    fun testCheckPermission_IsStorageOnly_DeniedWithRequest_NoDialog() {
        val fragmentActivity = mockFragmentActivity()
        every {
            fragmentActivity.checkPermission(any(), any(), any())
        } returns PackageManager.PERMISSION_DENIED
        val fragment = spyk<HomeFragment>()
        every { fragment.requireActivity() } returns fragmentActivity
        every { fragment.shouldShowRequestPermissionRationale(any()) } returns true
        ReflectionTestUtil.setField(fragment, FIELD_CONFIRM_DIALOG, null)
        val isStorageOnly = true
        val result = ReflectionTestUtil.callMethod(HomeFragment::class.java, fragment,
            METHOD_CHECK_PERMISSION,
            ReflectionTestUtil.Param(Boolean::class.java, isStorageOnly)
        ) as Boolean
        Assert.assertFalse(result)
    }

    @Test
    fun testCheckPermission_IsStorageOnly_DeniedWithRequest_WithDialog() {
        val fragmentActivity = mockFragmentActivity()
        every {
            fragmentActivity.checkPermission(any(), any(), any())
        } returns PackageManager.PERMISSION_DENIED
        val fragment = spyk<HomeFragment>()
        every { fragment.requireActivity() } returns fragmentActivity
        every { fragment.shouldShowRequestPermissionRationale(any()) } returns true
        val dialog = spyk<ConfirmDialogFragment>()
        ReflectionTestUtil.setField(fragment, FIELD_CONFIRM_DIALOG, dialog)
        val isStorageOnly = true
        val result = ReflectionTestUtil.callMethod(HomeFragment::class.java, fragment,
            METHOD_CHECK_PERMISSION,
            ReflectionTestUtil.Param(Boolean::class.java, isStorageOnly)
        ) as Boolean
        Assert.assertFalse(result)
    }

    @Test
    fun testCheckPermission_IsStorageOnly_DeniedNoRequest() {
        val fragmentActivity = mockFragmentActivity()
        every {
            fragmentActivity.checkPermission(any(), any(), any())
        } returns PackageManager.PERMISSION_DENIED
        val fragment = spyk<HomeFragment>()
        every { fragment.requireActivity() } returns fragmentActivity
        every { fragment.shouldShowRequestPermissionRationale(any()) } returns false
        val isStorageOnly = true
        val result = ReflectionTestUtil.callMethod(HomeFragment::class.java, fragment,
            METHOD_CHECK_PERMISSION,
            ReflectionTestUtil.Param(Boolean::class.java, isStorageOnly)
        ) as Boolean
        Assert.assertFalse(result)
    }

    // ================================================================================
    // Private Method Tests - openFile
    // ================================================================================
    @Test
    fun testOpenFile_ImagesFromPicker() {
        val fragment = spyk<HomeFragment>()
        addActivity(fragment)
        every { fragment.startActivity(any()) } just Runs
        val data: Uri? = null
        val clipData: ClipData? = null
        val fileType: Int = HomeFragment.IMAGES_FROM_PICKER
        ReflectionTestUtil.callMethod(HomeFragment::class.java, fragment,
            METHOD_OPEN_FILE,
            ReflectionTestUtil.Param(Uri::class.java, data),
            ReflectionTestUtil.Param(ClipData::class.java, clipData),
            ReflectionTestUtil.Param(Int::class.java, fileType)
        )
    }

    @Test
    fun testOpenFile_ImageFromPicker() {
        val fragment = spyk<HomeFragment>()
        addActivity(fragment)
        every { fragment.startActivity(any()) } just Runs
        val data: Uri? = null
        val clipData: ClipData? = null
        val fileType: Int = HomeFragment.IMAGE_FROM_PICKER
        ReflectionTestUtil.callMethod(HomeFragment::class.java, fragment,
            METHOD_OPEN_FILE,
            ReflectionTestUtil.Param(Uri::class.java, data),
            ReflectionTestUtil.Param(ClipData::class.java, clipData),
            ReflectionTestUtil.Param(Int::class.java, fileType)
        )
    }

    // ================================================================================
    // Private Functions
    // ================================================================================
    private fun addActivity(fragment: HomeFragment,
                            activity: FragmentActivity? = fragmentActivity) {
        every { fragment.activity } returns activity
        every { fragment.context } returns activity
    }

    private fun addFragmentManager(fragment: HomeFragment) {
        // Associate the fragment with a fragment manager
        val mockFragmentManager = mockk<FragmentManager>(relaxed = true)
        every { fragment.parentFragmentManager } returns mockFragmentManager
    }

    companion object {
        private const val TEST_FILE_NAME = "test.pdf"
        private const val FIELD_HOME_BUTTONS = "_homeButtons"
        private const val FIELD_CONFIRM_DIALOG = "_confirmDialogFragment"
        private const val FIELD_DOWNLOAD_DIALOG = "_downloadingDialog"
        private const val METHOD_DOWNLOAD_FILE = "downloadFileFromNotification"
        private const val METHOD_HIDE_LOADING_DIALOG = "hideLoadingPreviewDialog"
        private const val METHOD_CHECK_PERMISSION = "checkPermission"
        private const val METHOD_OPEN_FILE = "openFile"

        private var fragmentActivity: FragmentActivity? = null
        private var application: ISingleAccountPublicClientApplication? = null

        private fun mockView(): View {
            val mockView = mockk<View>(relaxed = true)
            val mockLayout = mockk<LinearLayout>(relaxed = true)
            every { mockView.findViewById<LinearLayout>(R.id.homeButtons) } returns mockLayout
            every { mockView.findViewById<LinearLayout>(R.id.fileButton) } returns mockLayout
            every { mockView.findViewById<LinearLayout>(R.id.photosButton) } returns mockLayout
            every { mockView.findViewById<LinearLayout>(R.id.cameraButton) } returns mockLayout
            every { mockView.findViewById<LinearLayout>(R.id.contentPrintButton) } returns mockLayout
            every { mockView.findViewById<LinearLayout>(R.id.contentView) } returns mockLayout
            val mockTextView = mockk<TextView>(relaxed = true)
            every { mockView.findViewById<TextView>(R.id.actionBarTitle) } returns mockTextView
            val mockViewGroup = mockk<ViewGroup>(relaxed = true)
            every { mockView.findViewById<ViewGroup>(R.id.leftActionLayout) } returns mockViewGroup
            every { mockView.context } returns fragmentActivity
            return mockView
        }

        private fun mockFragmentActivity(): FragmentActivity {
            val mockFragmentActivity = mockk<MainActivity>(relaxed = true)
            val mockInflater = mockk<LayoutInflater>(relaxed = true)
            val mockImageView = mockk<ImageView>(relaxed = true)
            every { mockInflater.inflate(R.layout.actionbar_button, null )} returns mockImageView
            val mockLayout = mockk<LinearLayout>(relaxed = true)
            every { mockLayout.findViewById<LinearLayout>(any()) } returns mockLayout
            val mockLayoutParams = mockk<LayoutParams>(relaxed = true)
            every { mockLayout.layoutParams } returns mockLayoutParams
            every { mockInflater.inflate(R.layout.home_buttons, any()) } returns mockLayout
            every { mockInflater.inflate(R.layout.home_buttons, any(), any()) } returns mockLayout
            every { mockFragmentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) } returns mockInflater
            return mockFragmentActivity
        }

        @JvmStatic
        @BeforeClass
        fun set() {
            fragmentActivity = mockFragmentActivity()
            MockTestUtil.mockConfiguration()
            MockTestUtil.mockUI()
            val mockActivity = MockTestUtil.mockActivity()
            ContentPrintManager.newInstance(mockActivity)
            application = ContentPrintManagerTest.mockApplication()
            ContentPrintManager.application = application
        }

        @JvmStatic
        @AfterClass
        fun tearDown() {
            MockTestUtil.unMockConfiguration()
            MockTestUtil.unMockUI()
        }
    }
}