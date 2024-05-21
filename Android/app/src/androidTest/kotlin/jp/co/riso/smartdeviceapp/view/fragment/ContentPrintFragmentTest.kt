package jp.co.riso.smartdeviceapp.view.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import eu.erikw.PullToRefreshListView
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import jp.co.riso.smartdeviceapp.MockTestUtil
import jp.co.riso.smartdeviceapp.controller.print.ContentPrintManager
import jp.co.riso.smartdeviceapp.controller.print.ContentPrintManagerTest
import jp.co.riso.smartdeviceapp.model.ContentPrintFile
import jp.co.riso.smartdeviceapp.view.base.BaseActivity
import jp.co.riso.smartdeviceapp.view.contentprint.ContentPrintFileAdapterTest
import jp.co.riso.smartprint.R
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

class ContentPrintFragmentTest {
    // ================================================================================
    // Tests - InitializeView
    // ================================================================================
    @Test
    fun testInitializeView_WithoutPushNotification() {
        ContentPrintManager.filenameFromNotification = null
        ContentPrintManager.isLoggedIn = true

        val fragment = spyk<ContentPrintFragment>()
        addActivity(fragment)
        val bundle = mockk<Bundle>()
        val view = mockView_WithoutButtons()
        fragment.initializeFragment(bundle)
        fragment.initializeView(view, bundle)
        Assert.assertNotNull(fragment)
        Assert.assertEquals(fragment.viewLayout, R.layout.fragment_contentprint)
    }

    @Test
    fun testInitializeView_WithPushNotification() {
        ContentPrintManager.filenameFromNotification = TEST_FILE_NAME
        ContentPrintManager.isLoggedIn = true

        val fragment = spyk<ContentPrintFragment>()
        initializeComponents(fragment)
        Assert.assertNotNull(fragment)
        Assert.assertEquals(fragment.viewLayout, R.layout.fragment_contentprint)
    }

    // ================================================================================
    // Tests - InitializeCustomActionBar
    // ================================================================================
    @Test
    fun testInitializeCustomActionBar_IsLoggedOut() {
        ContentPrintManager.isLoggedIn = false

        val fragment = spyk<ContentPrintFragment>()
        addActivity(fragment)
        val view = mockView_WithButtons()
        val savedInstanceState = mockk<Bundle>()
        fragment.initializeCustomActionBar(view, savedInstanceState)
        initializeComponents(fragment, true)
        fragment.initializeCustomActionBar(view, savedInstanceState)
        fragment.onStart()
    }

    @Test
    fun testInitializeCustomActionBar_IsLoggedIn_WithContext() {
        ContentPrintManager.isLoggedIn = true

        val fragment = spyk<ContentPrintFragment>()
        addActivity(fragment)
        val view = mockView_WithButtons()
        val savedInstanceState = mockk<Bundle>()
        fragment.initializeCustomActionBar(view, savedInstanceState)
        initializeComponents(fragment, true)
        fragment.initializeCustomActionBar(view, savedInstanceState)
        fragment.onStart()
    }

    @Test
    fun testInitializeCustomActionBar_IsLoggedIn_WithoutContext() {
        ContentPrintManager.isLoggedIn = true

        val fragment = spyk<ContentPrintFragment>()
        addActivity(fragment, true)
        val view = mockView()
        val savedInstanceState = mockk<Bundle>()
        fragment.initializeCustomActionBar(view, savedInstanceState)
        fragment.onStart()
    }

    // ================================================================================
    // Tests - onClick
    // ================================================================================
    @Test
    fun testOnClick_BackButton_CanGoBack() {
        val fragment = spyk<ContentPrintFragment>()
        addFragmentManager(fragment, 1)
        val view = mockViewWithId(R.id.menu_id_back_button)
        fragment.onClick(view)
    }

    @Test
    fun testOnClick_BackButton_CannotGoBack() {
        val fragment = spyk<ContentPrintFragment>()
        addFragmentManager(fragment, 0)
        val view = mockViewWithId(R.id.menu_id_back_button)
        fragment.onClick(view)
    }

    @Test
    fun testOnClick_LoginButton() {
        ContentPrintManager.isLoggedIn = false
        val fragment = ContentPrintFragment()
        val view = mockViewWithId( R.id.menu_id_action_login_button)
        fragment.onClick(view)
        fragment.initializeFragment(null)
        ContentPrintManager.application = ContentPrintManagerTest.mockApplication()
        fragment.onClick(view)
    }

    @Test
    fun testOnClick_LogoutButton() {
        ContentPrintManager.isLoggedIn = true
        val fragment = spyk<ContentPrintFragment>()
        addActivity(fragment)
        val view = mockViewWithId(R.id.menu_id_action_logout_button)
        fragment.onClick(view)
        initializeComponents(fragment)
        fragment.onClick(view)
    }

    @Test
    fun testOnClick_RefreshButton() {
        val fragment = ContentPrintFragment()
        val view = mockViewWithId(R.id.menu_id_action_refresh_button)
        fragment.onClick(view)
    }

    @Test
    fun testOnClick_PreviousNextButtons() {
        val fragment = spyk<ContentPrintFragment>()
        val previous = mockViewWithId(R.id.previousButton)
        val next = mockViewWithId(R.id.nextButton)
        // Set 1 page
        ContentPrintManager.fileCount = 1
        // No previous page (on page 1)
        fragment.onClick(previous)
        // Go to next page (no next page)
        fragment.onClick(next)
        // Set 3 pages
        ContentPrintManager.fileCount = ContentPrintFragment.ITEMS_PER_PAGE * 3
        // Go to next page
        fragment.onClick(next)
        // Go to previous page
        fragment.onClick(previous)
        initializeComponents(fragment)
        // Go to next page
        fragment.onClick(next)
        // Go to previous page
        fragment.onClick(previous)
        ContentPrintManager.fileCount = 0
    }

    @Test
    fun testOnClick_Outside() {
        val fragment = ContentPrintFragment()
        val view = mockViewWithId(0)
        fragment.onClick(view)
    }

    // ================================================================================
    // Tests - OnRefreshListener
    // ================================================================================
    @Test
    fun testOnRefreshListener() {
        val fragment = spyk<ContentPrintFragment>()
        ContentPrintManager.isLoggedIn = false
        fragment.onRefresh()
        ContentPrintManager.isLoggedIn = true
        fragment.onRefresh()
        fragment.onHeaderAdjusted(0)
        fragment.onBounceBackHeader(0)
        initializeComponents(fragment)
        ContentPrintManager.isLoggedIn = false
        fragment.onRefresh()
        ContentPrintManager.isLoggedIn = true
        fragment.onRefresh()
        fragment.onHeaderAdjusted(0)
        fragment.onBounceBackHeader(0)
    }

    // ================================================================================
    // Tests - onAuthenticationStarted
    // ================================================================================
    @Test
    fun testOnAuthenticationStarted() {
        val fragment = spyk<ContentPrintFragment>()
        fragment.onAuthenticationStarted()
        initializeComponents(fragment)
        fragment.onAuthenticationStarted()
    }

    // ================================================================================
    // Tests - onAuthenticationFinished
    // ================================================================================
    @Test
    fun testOnAuthenticationFinished_NotLoggedIn() {
        ContentPrintManager.isLoggedIn = false
        val fragment = spyk<ContentPrintFragment>()
        addActivity(fragment)
        ContentPrintFragment.lastAuthentication = ContentPrintFragment.Authentication.LOGIN
        fragment.onAuthenticationFinished()
        val mockView = mockView()
        fragment.initializeCustomActionBar(mockView, null)
        fragment.onAuthenticationFinished()
    }

    @Test
    fun testOnAuthenticationFinished_LoggedIn() {
        ContentPrintManager.isLoggedIn = true
        val fragment = spyk<ContentPrintFragment>()
        addActivity(fragment)
        ContentPrintFragment.lastAuthentication = ContentPrintFragment.Authentication.LOGOUT
        ContentPrintManager.filenameFromNotification = TEST_FILE_NAME
        fragment.onAuthenticationFinished()
        fragment.initializeFragment(null)
        fragment.onAuthenticationFinished()
        ContentPrintFragment.lastAuthentication = ContentPrintFragment.Authentication.LOGIN
        ContentPrintManager.filenameFromNotification = null
        fragment.onAuthenticationFinished()
    }

    // ================================================================================
    // Tests - onFileListUpdated
    // ================================================================================
    @Test
    fun testOnFileListUpdated() {
        val fragment = spyk<ContentPrintFragment>()
        ContentPrintManager.isLoggedIn = true
        ContentPrintManager.fileList = ArrayList()
        fragment.onFileListUpdated(true)
        val contentPrintFile = ContentPrintFile(0, TEST_FILE_NAME, null)
        val list = mutableListOf<ContentPrintFile>()
        for (i in 0..ContentPrintFragment.ITEMS_PER_PAGE) {
            list.add(contentPrintFile)
        }
        list.add(contentPrintFile)
        ContentPrintManager.fileList = list
        fragment.onFileListUpdated(true)
        ContentPrintManager.filenameFromNotification = TEST_FILE_NAME
        initializeComponents(fragment)
        fragment.onFileListUpdated(true)
        ContentPrintManager.fileList = ArrayList()
        fragment.onFileListUpdated(true)
        ContentPrintManager.isLoggedIn = false
        fragment.onFileListUpdated(true)
        fragment.onFileListUpdated(false)
        fragment.onStartFileDownload()
        fragment.onFileListUpdated(false)
    }

    // ================================================================================
    // Tests - onThumbnailDownloaded
    // ================================================================================
    @Test
    fun testOnThumbnailDownloaded_Success() {
        val fragment = spyk<ContentPrintFragment>()
        val contentPrintFile = ContentPrintFile(0, TEST_FILE_NAME, null)
        val filePath = TEST_FILE_NAME
        val success = true
        fragment.onThumbnailDownloaded(contentPrintFile, filePath, success)
        initializeComponents(fragment)
        fragment.onThumbnailDownloaded(contentPrintFile, filePath, success)
        Assert.assertNotNull(contentPrintFile.thumbnailImagePath)
        Assert.assertEquals(contentPrintFile.thumbnailImagePath, filePath)
    }

    @Test
    fun testOnThumbnailDownloaded_Fail() {
        val fragment = spyk<ContentPrintFragment>()
        val contentPrintFile = ContentPrintFile(0, TEST_FILE_NAME, null)
        val filePath = TEST_FILE_NAME
        val success = false
        fragment.onThumbnailDownloaded(contentPrintFile, filePath, success)
        Assert.assertNull(contentPrintFile.thumbnailImagePath)
    }

    // ================================================================================
    // Tests - onStartFileDownload
    // ================================================================================
    @Test
    fun testOnStartFileDownload() {
        ContentPrintManager.isLoggedIn = false
        val fragment = spyk<ContentPrintFragment>()
        addActivity(fragment, true)
        val context = MockTestUtil.mockContext()
        every { fragment.context } returns context
        fragment.onStartFileDownload()
        ContentPrintManager.isLoggedIn = false
        fragment.onFileListUpdated(false)
        fragment.onFileListUpdated(false)
        fragment.onStartFileDownload()
        fragment.onStartFileDownload()
    }

    // ================================================================================
    // Tests - onFileDownloaded
    // ================================================================================
    @Test
    fun testOnFileDownloaded_Success() {
        val fragment = spyk<ContentPrintFragment>()
        val contentPrintFile = ContentPrintFile(0, TEST_FILE_NAME, null)
        val filePath = TEST_FILE_NAME
        val success = true
        fragment.onFileDownloaded(contentPrintFile, filePath, success)
        initializeComponents(fragment)
        fragment.onFileDownloaded(contentPrintFile, filePath, success)
        Assert.assertTrue(ContentPrintManager.isFileFromContentPrint)
        Assert.assertEquals(ContentPrintManager.filePath, filePath)
    }

    @Test
    fun testOnFileDownloaded_Fail() {
        val fragment = spyk<ContentPrintFragment>()
        addActivity(fragment)
        val contentPrintFile = ContentPrintFile(0, TEST_FILE_NAME, null)
        val filePath = TEST_FILE_NAME
        val success = false
        fragment.onFileDownloaded(contentPrintFile, filePath, success)
        Assert.assertFalse(ContentPrintManager.isFileFromContentPrint)
        Assert.assertNull(ContentPrintManager.filePath)
    }

    // ================================================================================
    // Tests - onPrinterListUpdated
    // ================================================================================
    @Test
    fun testOnPrinterListUpdated() {
        val fragment = spyk<ContentPrintFragment>()
        addActivity(fragment)
        every { fragment.startActivity(any()) } just Runs
        ContentPrintManager.filePath = null
        fragment.onPrinterListUpdated(true)
        fragment.onPrinterListUpdated(false)
        ContentPrintManager.filePath = TEST_FILE_NAME
        fragment.onPrinterListUpdated(true)
        fragment.onPrinterListUpdated(false)
    }

    // ================================================================================
    // Tests - onFileSelect
    // ================================================================================
    @Test
    fun testOnFileSelect_FileExists() {
        val fragment = spyk<ContentPrintFragment>()
        addActivity(fragment)
        val contentPrintFile = ContentPrintFile(0, TEST_FILE_NAME, null)
        val result = fragment.onFileSelect(contentPrintFile)
        Assert.assertEquals(1, result)
    }

    @Test
    fun testOnFileSelect_Null() {
        val fragment = spyk<ContentPrintFragment>()
        val result = fragment.onFileSelect(null)
        Assert.assertEquals(-1, result)
    }

    // ================================================================================
    // Tests - onConfirm
    // ================================================================================
    @Test
    fun testOnConfirm_Logout() {
        val fragment = spyk<ContentPrintFragment>()
        addActivity(fragment)
        fragment.onCancel()
        // Set last confirmation as logout
        val view = mockViewWithId(R.id.menu_id_action_logout_button)
        fragment.onClick(view)
        fragment.onConfirm()
        fragment.initializeFragment(null)
        fragment.onConfirm()
    }

    @Test
    fun testOnConfirm_Preview() {
        val fragment = spyk<ContentPrintFragment>()
        addActivity(fragment)
        fragment.onCancel()
        // Set last confirmation as preview
        val contentPrintFile = ContentPrintFile(0, TEST_FILE_NAME, null)
        fragment.onFileSelect(contentPrintFile)
        fragment.onConfirm()
        fragment.initializeFragment(null)
        fragment.onConfirm()
        ContentPrintManager.selectedFile = null
        fragment.onConfirm()
    }

    // ================================================================================
    // Private Functions
    // ================================================================================
    private fun initializeComponents(fragment: ContentPrintFragment, noActivity: Boolean = false) {
        if (!noActivity) {
            addActivity(fragment)
        }
        val savedInstanceState = mockk<Bundle>()
        fragment.initializeFragment(savedInstanceState)
        val view = mockView_WithButtons()
        fragment.initializeView(view, savedInstanceState)
    }

    private fun addActivity(fragment: ContentPrintFragment, noContext: Boolean = false) {
        // Set the fragment activity to prevent NullPointerException during the initialization
        // of the ContentPrintFileAdapter, which requires a non-null activity
        every { fragment.activity } returns fragmentActivity
        if (!noContext) {
            every { fragment.context } returns fragmentActivity
        }
    }

    private fun addFragmentManager(fragment: ContentPrintFragment, stackCount: Int) {
        // Associate the fragment with a fragment manager
        val mockFragmentManager = mockk<FragmentManager>(relaxed = true)
        every { mockFragmentManager.backStackEntryCount } returns stackCount
        every { fragment.parentFragmentManager } returns mockFragmentManager
    }

    companion object {
        private const val TEST_FILE_NAME = "test.pdf"

        private var fragmentActivity: FragmentActivity? = null
        private var activity: Activity? = null

        private fun mockContext(): Context {
            val mockInflater = mockk<LayoutInflater>()
            val mockImageView = mockk<ImageView>(relaxed = true)
            every { mockInflater.inflate(R.layout.actionbar_button, null )} returns mockImageView
            val mockLayout = mockk<LinearLayout>(relaxed = true)
            val mockTextView = mockk<TextView>(relaxed = true)
            every { mockLayout.findViewById<TextView>(R.id.buttonLabelText) } returns mockTextView
            every { mockLayout.findViewById<ImageView>(R.id.buttonImage) } returns mockImageView
            every { mockInflater.inflate(R.layout.actionbar_button_with_label, null )} returns mockLayout
            return MockTestUtil.mockUiContext(mockInflater)
        }

        private fun mockView(): View {
            val mockView = mockk<View>()
            val mockTextView = mockk<TextView>(relaxed = true)
            every { mockTextView.text = any() } just Runs
            val mockLayoutParams1 = mockk<FrameLayout.LayoutParams>(relaxed = true)
            every { mockTextView.layoutParams } returns mockLayoutParams1
            every { mockView.findViewById<TextView>(R.id.loggedOutText) } returns mockTextView
            every { mockView.findViewById<TextView>(R.id.emptyListText) } returns mockTextView
            every { mockView.findViewById<TextView>(R.id.pageLabel) } returns mockTextView
            every { mockView.findViewById<TextView>(R.id.actionBarTitle) } returns mockTextView
            every { mockView.findViewById<TextView>(R.id.buttonLabelText) } returns mockTextView
            val mockButtonImageView = mockk<ImageView>(relaxed = true)
            every { mockView.findViewById<ImageView>(R.id.buttonImage) } returns mockButtonImageView
            val mockListView = mockk<PullToRefreshListView>(relaxed = true)
            val mockImageView = mockk<ImageView>()
            val mockLayoutParams2 = mockk<RelativeLayout.LayoutParams>(relaxed = true)
            every { mockImageView.visibility = any() } just Runs
            every { mockImageView.layoutParams } returns mockLayoutParams2
            every { mockListView.findViewById<ImageView>(R.id.ptr_id_image) } returns mockImageView
            val mockProgressBar = mockk<ProgressBar>()
            every { mockProgressBar.visibility = any() } just Runs
            every { mockProgressBar.layoutParams } returns mockLayoutParams2
            every { mockListView.findViewById<ProgressBar>(R.id.ptr_id_spinner) } returns mockProgressBar
            every { mockView.findViewById<View>(R.id.content_list) } returns mockListView
            val mockLayout = mockk<ViewGroup>(relaxed = true)
            every { mockView.findViewById<ViewGroup>(R.id.leftActionLayout) } returns mockLayout
            every { mockView.findViewById<ViewGroup>(R.id.rightActionLayout) } returns mockLayout
            val mockLinearLayout = mockk<LinearLayout>(relaxed = true)
            every { mockView.findViewById<View>(R.layout.actionbar_button_with_label) } returns mockLinearLayout
            every { mockView.context } returns mockContext()
            return mockView
        }

        private fun mockView_WithButtons(): View {
            val mockView = mockView()
            val mockButton = mockk<Button>(relaxed = true)
            every { mockButton.setOnClickListener(any()) } just Runs
            every { mockView.findViewById<Button>(R.id.previousButton) } returns mockButton
            every { mockView.findViewById<Button>(R.id.nextButton) } returns mockButton
            return mockView
        }

        private fun mockView_WithoutButtons(): View {
            val mockView = mockView()
            every { mockView.findViewById<Button>(R.id.previousButton) } returns null
            every { mockView.findViewById<Button>(R.id.nextButton) } returns null
            return mockView
        }

        private fun mockViewWithId(id: Int): View {
            val view = mockk<View>(relaxed = true)
            every { view.id } returns id
            return view
        }

        private fun mockFragmentActivity(): FragmentActivity {
            val mockFragmentActivity = mockk<BaseActivity>(relaxed = true)
            // Inflater for ContentPrintFileAdapter
            val mockInflater = mockk<LayoutInflater>()
            val mockView = ContentPrintFileAdapterTest.mockView()
            every { mockInflater.inflate(any<Int>(), any(), any() )} returns mockView
            every { mockFragmentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) } returns mockInflater
            return mockFragmentActivity
        }

        @JvmStatic
        @BeforeClass
        fun set() {
            fragmentActivity = mockFragmentActivity()
            activity = MockTestUtil.mockActivity()
            MockTestUtil.mockConfiguration()
            MockTestUtil.mockUI()
            ContentPrintManager.newInstance(activity)
            ContentPrintManager.application = ContentPrintManagerTest.mockApplication()
        }

        @JvmStatic
        @AfterClass
        fun tearDown() {
            MockTestUtil.unMockConfiguration()
            MockTestUtil.unMockUI()
        }
    }
}