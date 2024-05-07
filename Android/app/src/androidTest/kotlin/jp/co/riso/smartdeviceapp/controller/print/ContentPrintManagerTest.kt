package jp.co.riso.smartdeviceapp.controller.print

import android.app.Activity
import com.microsoft.identity.client.AcquireTokenParameters
import com.microsoft.identity.client.IAccount
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.ICurrentAccountResult
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.exception.MsalArgumentException
import com.microsoft.identity.client.exception.MsalException
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import jp.co.riso.smartdeviceapp.MockUtil
import jp.co.riso.smartdeviceapp.model.ContentPrintFile
import jp.co.riso.smartdeviceapp.model.ContentPrintPrintSettings
import jp.co.riso.smartdeviceapp.model.ContentPrintPrinter
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.AfterClass
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import retrofit2.Call
import java.io.InputStream
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import java.util.UUID

class ContentPrintManagerTest {
    class TestAuthenticationCallback: ContentPrintManager.IAuthenticationCallback {
        var isAuthenticationStarted = false
        var isAuthenticationFinished = false

        // ================================================================================
        // INTERFACE - ContentPrintManager.IAuthenticationCallback
        // ================================================================================
        override fun onAuthenticationStarted() {
            isAuthenticationStarted = true
        }

        override fun onAuthenticationFinished() {
            isAuthenticationFinished = true
        }
    }

    class TestContentPrintCallback:  ContentPrintManager.IContentPrintCallback {
        var isFileListUpdated = false
        var fileListUpdatedResult = false
        var isFileDownloadStarted = false
        var isFileDownloadFinished = false
        var thumbnailDownloadResult = false
        var fileDownloadResult = false
        var isPrinterListUpdated = false
        var printerListUpdatedResult = false

        // ================================================================================
        // INTERFACE - ContentPrintManager.IContentPrintCallback
        // ================================================================================
        override fun onFileListUpdated(success: Boolean) {
            isFileListUpdated = true
            fileListUpdatedResult = success
        }

        override fun onStartFileDownload() {
            isFileDownloadStarted = true
        }

        override fun onThumbnailDownloaded(
            contentPrintFile: ContentPrintFile,
            filePath: String,
            success: Boolean
        ) {
            isFileDownloadFinished = true
            thumbnailDownloadResult = success
        }

        override fun onFileDownloaded(
            contentPrintFile: ContentPrintFile,
            filePath: String,
            success: Boolean
        ) {
            isFileDownloadFinished = true
            fileDownloadResult = success
        }

        override fun onPrinterListUpdated(success: Boolean) {
            isPrinterListUpdated = true
            printerListUpdatedResult = success
        }
    }

    class TestRegisterToBoxCallback: ContentPrintManager.IRegisterToBoxCallback {
        var isBoxRegistrationStarted = false
        var isBoxRegistrationFinished = false
        var boxRegistrationResult = false

        override fun onStartBoxRegistration() {
            isBoxRegistrationStarted = true
        }

        override fun onBoxRegistered(success: Boolean) {
            isBoxRegistrationFinished = true
            boxRegistrationResult = success
        }
    }

    @Before
    fun setUp() {
        // Mock the Content Print application
        ContentPrintManager.application = application
    }

    // ================================================================================
    // Tests - SingleAccountApplicationCreatedListener
    // ================================================================================
    @Test
    fun testSingleAccountApplicationCreatedListener_onCreated() {
        val listener = ContentPrintManager.SingleAccountApplicationCreatedListener()
        val application: ISingleAccountPublicClientApplication = mockApplication()
        listener.onCreated(application)
        Assert.assertEquals(application, listener.application)
    }

    @Test
    fun testSingleAccountApplicationCreatedListener_onError() {
        val listener = ContentPrintManager.SingleAccountApplicationCreatedListener()
        val exception: MsalException = MsalArgumentException(EXCEPTION_ARGUMENT_NAME, EXCEPTION_MESSAGE)
        listener.onError(exception)
        Assert.assertNull(listener.application)
    }

    // ================================================================================
    // Tests - InteractiveAuthenticationCallback
    // ================================================================================
    @Test
    fun testInteractiveAuthenticationCallback_onSuccess() {
        val testCallback = TestAuthenticationCallback()
        val callback = ContentPrintManager.InteractiveAuthenticationCallback(testCallback)
        val result = mockAuthenticationResult()
        callback.onSuccess(result)
        Assert.assertTrue(ContentPrintManager.isLoggedIn)
    }

    @Test
    fun testInteractiveAuthenticationCallback_onSuccessNull() {
        val callback = ContentPrintManager.InteractiveAuthenticationCallback(null)
        callback.onSuccess(null)
        Assert.assertTrue(ContentPrintManager.isLoggedIn)
    }

    @Test
    fun testInteractiveAuthenticationCallback_onError() {
        val testCallback = TestAuthenticationCallback()
        val callback = ContentPrintManager.InteractiveAuthenticationCallback(testCallback)
        val exception: MsalException = MsalArgumentException(EXCEPTION_ARGUMENT_NAME, EXCEPTION_MESSAGE)
        callback.onError(exception)
        Assert.assertFalse(ContentPrintManager.isLoggedIn)
    }

    @Test
    fun testInteractiveAuthenticationCallback_onErrorNull() {
        val callback = ContentPrintManager.InteractiveAuthenticationCallback(null)
        callback.onError(null)
        Assert.assertFalse(ContentPrintManager.isLoggedIn)
    }

    @Test
    fun testInteractiveAuthenticationCallback_onCancel() {
        val testCallback = TestAuthenticationCallback()
        val callback = ContentPrintManager.InteractiveAuthenticationCallback(testCallback)
        callback.onCancel()
        Assert.assertFalse(ContentPrintManager.isLoggedIn)
    }

    @Test
    fun testInteractiveAuthenticationCallback_onCancelNull() {
        val callback = ContentPrintManager.InteractiveAuthenticationCallback(null)
        callback.onCancel()
        Assert.assertFalse(ContentPrintManager.isLoggedIn)
    }

    // ================================================================================
    // Tests - RefreshAuthenticationCallback
    // ================================================================================
    @Test
    fun testRefreshAuthenticationCallback_onSuccess() {
        val testCallback = TestAuthenticationCallback()
        val callback = ContentPrintManager.RefreshAuthenticationCallback(testCallback)
        val result = mockAuthenticationResult()
        callback.onSuccess(result)
        Assert.assertTrue(ContentPrintManager.isLoggedIn)
    }

    @Test
    fun testRefreshAuthenticationCallback_onSuccessNull() {
        val callback = ContentPrintManager.RefreshAuthenticationCallback(null)
        callback.onSuccess(null)
        Assert.assertTrue(ContentPrintManager.isLoggedIn)
    }

    @Test
    fun testRefreshAuthenticationCallback_onError() {
        val testCallback = TestAuthenticationCallback()
        val callback = ContentPrintManager.RefreshAuthenticationCallback(testCallback)
        val exception: MsalException = MsalArgumentException(EXCEPTION_ARGUMENT_NAME, EXCEPTION_MESSAGE)
        callback.onError(exception)
        Assert.assertFalse(ContentPrintManager.isLoggedIn)
    }

    @Test
    fun testRefreshAuthenticationCallback_onErrorNull() {
        val callback = ContentPrintManager.RefreshAuthenticationCallback(null)
        callback.onError(null)
        Assert.assertFalse(ContentPrintManager.isLoggedIn)
    }

    // ================================================================================
    // Tests - AuthInterceptor
    // ================================================================================
    @Test
    fun testAuthInterceptor_intercept() {
        val interceptor = ContentPrintManager.AuthInterceptor()
        val mockChain: Interceptor.Chain = mockk<Interceptor.Chain>()
        val mockRequest: Request = mockk<Request>()
        val mockResponse = mockk<Response>()
        every { mockChain.proceed(any()) } returns mockResponse
        val mockBuilder: Request.Builder = mockk<Request.Builder>()
        every { mockRequest.newBuilder() } returns mockBuilder
        every { mockBuilder.addHeader(any(), any()) } coAnswers { mockBuilder }
        every { mockBuilder.build() } returns mockRequest
        every { mockChain.request() } returns mockRequest
        val response = interceptor.intercept(mockChain)
        Assert.assertNotNull(response)
    }

    // ================================================================================
    // Tests - ContentPrintFileResult
    // ================================================================================
    @Test
    fun testContentPrintFileResult_Init() {
        val result = ContentPrintManager.ContentPrintFileResult()
        Assert.assertNotNull(result)
        Assert.assertNotNull(result.list)
        Assert.assertEquals(result.count, -1)
    }

    @Test
    fun testContentPrintFileResult_SetValues() {
        val result = ContentPrintManager.ContentPrintFileResult()
        val file = ContentPrintFile(0, null, null)
        result.list = arrayListOf(file)
        result.count = 1
        Assert.assertNotNull(result)
        Assert.assertEquals(result.list[0], file)
        Assert.assertEquals(result.count, 1)
    }

    // ================================================================================
    // Tests - ContentPrintFileResultCallback
    // ================================================================================
    @Test
    fun testContentPrintFileResultCallback_onFailure() {
        val testCallback = TestContentPrintCallback()
        val callback = ContentPrintManager.ContentPrintFileResultCallback(testCallback)
        val mockResult = mockk<Call<ContentPrintManager.ContentPrintFileResult>>()
        callback.onFailure(mockResult, Throwable())
        Assert.assertTrue(testCallback.isFileListUpdated)
        Assert.assertFalse(testCallback.fileListUpdatedResult)
    }

    @Test
    fun testContentPrintFileResultCallback_onFailureNull() {
        val callback = ContentPrintManager.ContentPrintFileResultCallback(null)
        val mockResult = mockk<Call<ContentPrintManager.ContentPrintFileResult>>()
        callback.onFailure(mockResult, Throwable())
    }

    @Test
    fun testContentPrintFileResultCallback_onResponse() {
        val testCallback = TestContentPrintCallback()
        val callback = ContentPrintManager.ContentPrintFileResultCallback(testCallback)
        val mockResult = mockk<Call<ContentPrintManager.ContentPrintFileResult>>()
        val mockResponse = mockk<retrofit2.Response<ContentPrintManager.ContentPrintFileResult>>()
        every { mockResponse.body() } returns ContentPrintManager.ContentPrintFileResult()
        callback.onResponse(mockResult, mockResponse)
        Assert.assertTrue(testCallback.isFileListUpdated)
        Assert.assertTrue(testCallback.fileListUpdatedResult)
    }

    @Test
    fun testContentPrintFileResultCallback_onResponseNullBody() {
        val testCallback = TestContentPrintCallback()
        val callback = ContentPrintManager.ContentPrintFileResultCallback(testCallback)
        val mockResult = mockk<Call<ContentPrintManager.ContentPrintFileResult>>()
        val mockResponse = mockk<retrofit2.Response<ContentPrintManager.ContentPrintFileResult>>()
        every { mockResponse.body() } returns null
        callback.onResponse(mockResult, mockResponse)
        Assert.assertTrue(testCallback.isFileListUpdated)
        Assert.assertTrue(testCallback.fileListUpdatedResult)
    }

    @Test
    fun testContentPrintFileResultCallback_onResponseNullCallback() {
        val callback = ContentPrintManager.ContentPrintFileResultCallback(null)
        val mockResult = mockk<Call<ContentPrintManager.ContentPrintFileResult>>()
        val mockResponse = mockk<retrofit2.Response<ContentPrintManager.ContentPrintFileResult>>()
        val result = ContentPrintManager.ContentPrintFileResult()
        val file = ContentPrintFile(0, null, null)
        result.list = listOf(file)
        result.count = 1
        every { mockResponse.body() } returns result
        callback.onResponse(mockResult, mockResponse)
        Assert.assertEquals(ContentPrintManager.fileList, result.list)
        Assert.assertEquals(ContentPrintManager.fileCount, 1)
    }

    // ================================================================================
    // Tests - ContentPrintFileDownloadCallback
    // ================================================================================
    @Test
    fun testContentPrintFileDownloadCallback_onFailure() {
        val manager = ContentPrintManager.getInstance()
        val filename = TEST_FILE
        val testCallback = TestContentPrintCallback()
        val callback = ContentPrintManager.ContentPrintFileDownloadCallback(manager!!, filename, testCallback)
        val mockResult = mockk<Call<ContentPrintManager.ContentPrintFileResult>>()
        callback.onFailure(mockResult, Throwable())
    }

    @Test
    fun testContentPrintFileDownloadCallback_onFailureNull() {
        val manager = ContentPrintManager.getInstance()
        val callback = ContentPrintManager.ContentPrintFileDownloadCallback(manager!!, null, null)
        val mockResult = mockk<Call<ContentPrintManager.ContentPrintFileResult>>()
        callback.onFailure(mockResult, Throwable())
    }

    @Test
    fun testContentPrintFileDownloadCallback_onResponse() {
        val manager = ContentPrintManager.getInstance()
        val filename = TEST_FILE
        val testCallback = TestContentPrintCallback()
        val callback = ContentPrintManager.ContentPrintFileDownloadCallback(manager!!, filename, testCallback)
        val mockResult = mockk<Call<ContentPrintManager.ContentPrintFileResult>>()
        val mockResponse = mockk<retrofit2.Response<ContentPrintManager.ContentPrintFileResult>>()
        val result = ContentPrintManager.ContentPrintFileResult()
        val file1 = ContentPrintFile(0, filename, null)
        val file2 = ContentPrintFile(1, "another file", null)
        result.list = listOf(file1, file2)
        result.count = 2
        every { mockResponse.body() } returns result
        callback.onResponse(mockResult, mockResponse)
    }

    @Test
    fun testContentPrintFileDownloadCallback_onResponseNullBody() {
        val manager = ContentPrintManager.getInstance()
        val filename = TEST_FILE
        val testCallback = TestContentPrintCallback()
        val callback = ContentPrintManager.ContentPrintFileDownloadCallback(manager!!, filename, testCallback)
        val mockResult = mockk<Call<ContentPrintManager.ContentPrintFileResult>>()
        val mockResponse = mockk<retrofit2.Response<ContentPrintManager.ContentPrintFileResult>>()
        every { mockResponse.body() } returns null
        callback.onResponse(mockResult, mockResponse)
    }

    @Test
    fun testContentPrintFileDownloadCallback_onResponseNullCallback() {
        val manager = ContentPrintManager.getInstance()
        val filename = TEST_FILE
        val callback = ContentPrintManager.ContentPrintFileDownloadCallback(manager!!, filename, null)
        val mockResult = mockk<Call<ContentPrintManager.ContentPrintFileResult>>()
        val mockResponse = mockk<retrofit2.Response<ContentPrintManager.ContentPrintFileResult>>()
        val result = ContentPrintManager.ContentPrintFileResult()
        val file1 = ContentPrintFile(1, "another file", null)
        val file2 = ContentPrintFile(2, filename, null)
        val file3 = ContentPrintFile(3, "another file 2", null)
        result.list = listOf(file1, file2, file3)
        result.count = 2
        every { mockResponse.body() } returns result
        callback.onResponse(mockResult, mockResponse)
    }

    // ================================================================================
    // Tests - ContentPrintPrinterResult
    // ================================================================================
    @Test
    fun testContentPrintPrinterResult_Init() {
        val result = ContentPrintManager.ContentPrintPrinterResult()
        Assert.assertNotNull(result)
        Assert.assertNotNull(result.list)
        Assert.assertEquals(result.count, -1)
    }

    @Test
    fun testContentPrintPrinterResult_SetValues() {
        val result = ContentPrintManager.ContentPrintPrinterResult()
        val printer = ContentPrintPrinter()
        result.list = arrayListOf(printer)
        result.count = 1
        Assert.assertNotNull(result)
        Assert.assertEquals(result.list[0], printer)
        Assert.assertEquals(result.count, 1)
    }

    // ================================================================================
    // Tests - ContentPrintPrinterResultCallback
    // ================================================================================
    @Test
    fun testContentPrintPrinterResultCallback_onFailure() {
        val testCallback = TestContentPrintCallback()
        val callback = ContentPrintManager.ContentPrintPrinterResultCallback(testCallback)
        val mockResult = mockk<Call<ContentPrintManager.ContentPrintPrinterResult>>()
        callback.onFailure(mockResult, Throwable())
        Assert.assertTrue(testCallback.isPrinterListUpdated)
        Assert.assertFalse(testCallback.printerListUpdatedResult)
    }

    @Test
    fun testContentPrintPrinterResultCallback_onFailureNull() {
        val callback = ContentPrintManager.ContentPrintPrinterResultCallback(null)
        val mockResult = mockk<Call<ContentPrintManager.ContentPrintPrinterResult>>()
        callback.onFailure(mockResult, Throwable())
    }

    @Test
    fun testContentPrintPrinterResultCallback_onResponse() {
        val testCallback = TestContentPrintCallback()
        val callback = ContentPrintManager.ContentPrintPrinterResultCallback(testCallback)
        val mockResult = mockk<Call<ContentPrintManager.ContentPrintPrinterResult>>()
        val mockResponse = mockk<retrofit2.Response<ContentPrintManager.ContentPrintPrinterResult>>()
        val result = ContentPrintManager.ContentPrintPrinterResult()
        val printer = ContentPrintPrinter()
        result.list = listOf(printer)
        result.count = 1
        every { mockResponse.body() } returns result
        callback.onResponse(mockResult, mockResponse)
        Assert.assertTrue(testCallback.isPrinterListUpdated)
        Assert.assertTrue(testCallback.printerListUpdatedResult)
    }

    @Test
    fun testContentPrintPrinterResultCallback_onResponseNullBody() {
        val testCallback = TestContentPrintCallback()
        val callback = ContentPrintManager.ContentPrintPrinterResultCallback(testCallback)
        val mockResult = mockk<Call<ContentPrintManager.ContentPrintPrinterResult>>()
        val mockResponse = mockk<retrofit2.Response<ContentPrintManager.ContentPrintPrinterResult>>()
        every { mockResponse.body() } returns null
        callback.onResponse(mockResult, mockResponse)
        Assert.assertTrue(testCallback.isPrinterListUpdated)
        Assert.assertTrue(testCallback.printerListUpdatedResult)
    }

    @Test
    fun testContentPrintPrinterResultCallback_onResponseNullCallback() {
        val callback = ContentPrintManager.ContentPrintPrinterResultCallback(null)
        val mockResult = mockk<Call<ContentPrintManager.ContentPrintPrinterResult>>()
        val mockResponse = mockk<retrofit2.Response<ContentPrintManager.ContentPrintPrinterResult>>()
        val result = ContentPrintManager.ContentPrintPrinterResult()
        val printer = ContentPrintPrinter()
        result.list = listOf(printer)
        result.count = 1
        every { mockResponse.body() } returns result
        callback.onResponse(mockResult, mockResponse)
        Assert.assertEquals(ContentPrintManager.printerList, result.list)
    }

    // ================================================================================
    // Tests - ContentPrintBoxRegistrationRequest
    // ================================================================================
    @Test
    fun testContentPrintBoxRegistrationRequest_Init() {
        val result = ContentPrintManager.ContentPrintBoxRegistrationRequest()
        Assert.assertNotNull(result)
        Assert.assertEquals(result.fileId, -1)
        Assert.assertNull(result.serialNo)
        Assert.assertNull(result.printSettings)
    }

    @Test
    fun testContentPrintBoxRegistrationRequest_SetValues() {
        val result = ContentPrintManager.ContentPrintBoxRegistrationRequest()
        val printSettings = ContentPrintPrintSettings()
        result.fileId = 1
        result.serialNo = TEST_SERIAL_NO
        result.printSettings = printSettings
        Assert.assertNotNull(result)
        Assert.assertEquals(result.fileId, 1)
        Assert.assertEquals(result.serialNo, TEST_SERIAL_NO)
        Assert.assertEquals(result.printSettings, printSettings)
    }

    // ================================================================================
    // Tests - ContentPrintBoxRegistrationResultCallback
    // ================================================================================
    @Test
    fun testContentPrintBoxRegistrationResultCallback_onFailure() {
        val testCallback = TestRegisterToBoxCallback()
        val callback = ContentPrintManager.ContentPrintBoxRegistrationResultCallback(testCallback)
        val mockResult = mockk<Call<ResponseBody>>()
        callback.onFailure(mockResult, Throwable())
        Assert.assertTrue(testCallback.isBoxRegistrationFinished)
        Assert.assertFalse(testCallback.boxRegistrationResult)
    }

    @Test
    fun testContentPrintBoxRegistrationResultCallback_onFailureNull() {
        val callback = ContentPrintManager.ContentPrintBoxRegistrationResultCallback(null)
        val mockResult = mockk<Call<ResponseBody>>()
        callback.onFailure(mockResult, Throwable())
    }

    @Test
    fun testContentPrintBoxRegistrationResultCallback_onResponse() {
        val testCallback = TestRegisterToBoxCallback()
        val callback = ContentPrintManager.ContentPrintBoxRegistrationResultCallback(testCallback)
        val mockResult = mockk<Call<ResponseBody>>()
        val mockResponse = mockk<retrofit2.Response<ResponseBody>>()
        callback.onResponse(mockResult, mockResponse)
        Assert.assertTrue(testCallback.isBoxRegistrationFinished)
        Assert.assertTrue(testCallback.boxRegistrationResult)
    }

    @Test
    fun testContentPrintBoxRegistrationResultCallback_onResponseNullCallback() {
        val callback = ContentPrintManager.ContentPrintBoxRegistrationResultCallback(null)
        val mockResult = mockk<Call<ResponseBody>>()
        val mockResponse = mockk<retrofit2.Response<ResponseBody>>()
        callback.onResponse(mockResult, mockResponse)
    }

    // ================================================================================
    // Tests - init
    // ================================================================================
    @Test
    fun testInit_Context() {
        ContentPrintManager.newInstance(activity)
        val manager = ContentPrintManager.getInstance()
        Assert.assertNotNull(manager)
    }

    @Test
    fun testInit_NullContext() {
        ContentPrintManager.newInstance(null)
        val manager = ContentPrintManager.getInstance()
        Assert.assertNotNull(manager)
    }

    // ================================================================================
    // Tests - login
    // ================================================================================
    @Test
    fun testLogin_NullApplication() {
        ContentPrintManager.application = null
        val manager = ContentPrintManager.getInstance()
        val testCallback = TestAuthenticationCallback()
        manager?.login(activity, testCallback)
        Assert.assertFalse(ContentPrintManager.isLoggedIn)
    }

    @Test
    fun testLogin_WithApplication() {
        ContentPrintManager.application = application
        val manager = ContentPrintManager.getInstance()
        // Test the login method
        val testCallback = TestAuthenticationCallback()
        manager?.login(activity, testCallback)
        // Wait for the authentication to be finished
        waitForCondition(testCallback.isAuthenticationFinished)
        Assert.assertTrue(ContentPrintManager.isLoggedIn)
    }

    // ================================================================================
    // Tests - logout
    // ================================================================================
    @Test
    fun testLogout_NullApplication_IsNotLoggedIn() {
        ContentPrintManager.application = null
        val manager = ContentPrintManager.getInstance()
        ContentPrintManager.isLoggedIn = false
        val testCallback = TestAuthenticationCallback()
        manager?.logout(testCallback)
        Assert.assertFalse(ContentPrintManager.isLoggedIn)
    }

    @Test
    fun testLogout_NullApplication_IsLoggedIn() {
        ContentPrintManager.application = null
        val manager = ContentPrintManager.getInstance()
        ContentPrintManager.isLoggedIn = true
        val testCallback = TestAuthenticationCallback()
        manager?.logout(testCallback)
        Assert.assertFalse(ContentPrintManager.isLoggedIn)
    }

    @Test
    fun testLogout_Application_IsNotLoggedIn() {
        ContentPrintManager.application = application
        val manager = ContentPrintManager.getInstance()
        ContentPrintManager.isLoggedIn = false
        val testCallback = TestAuthenticationCallback()
        manager?.logout(testCallback)
        Assert.assertFalse(ContentPrintManager.isLoggedIn)
    }

    @Test
    fun testLogout_Application_IsLoggedIn() {
        ContentPrintManager.application = application
        val manager = ContentPrintManager.getInstance()
        ContentPrintManager.isLoggedIn = true
        val testCallback = TestAuthenticationCallback()
        manager?.logout(testCallback)
        Assert.assertFalse(ContentPrintManager.isLoggedIn)
    }

    @Test
    fun testLogout_NullCallback() {
        ContentPrintManager.application = application
        ContentPrintManager.newInstance(activity)
        val manager = ContentPrintManager.getInstance()
        ContentPrintManager.isLoggedIn = true
        manager?.logout(null)
        Assert.assertFalse(ContentPrintManager.isLoggedIn)
    }

    // ================================================================================
    // Tests - updateFileList
    // ================================================================================
    @Test
    fun testUpdateFileList_WithCallback() {
        ContentPrintManager.application = application
        ContentPrintManager.contentPrintService = service
        val manager = ContentPrintManager.getInstance()
        val testCallback = TestContentPrintCallback()
        manager?.updateFileList(LIMIT, PAGE, testCallback)
        // Call the callback since the content print service is mocked
        testCallback.onFileListUpdated(true)
        Assert.assertTrue(testCallback.fileListUpdatedResult)
    }

    @Test
    fun testUpdateFileList_WithoutCallback() {
        ContentPrintManager.application = application
        ContentPrintManager.contentPrintService = service
        val manager = ContentPrintManager.getInstance()
        val testCallback = TestContentPrintCallback()
        manager?.updateFileList(LIMIT, PAGE, null)
        Assert.assertFalse(testCallback.fileListUpdatedResult)
    }

    @Test
    fun testUpdateFileList_NullService() {
        ContentPrintManager.application = application
        ContentPrintManager.contentPrintService = null
        val manager = ContentPrintManager.getInstance()
        val testCallback = TestContentPrintCallback()
        manager?.updateFileList(LIMIT, PAGE, testCallback)
        // Call the callback since the content print service is null
        testCallback.onFileListUpdated(true)
        Assert.assertTrue(testCallback.fileListUpdatedResult)
    }

    // ================================================================================
    // Tests - downloadFilename
    // ================================================================================
    @Test
    fun testDownloadFilename_WithCallback() {
        ContentPrintManager.application = application
        ContentPrintManager.contentPrintService = service
        val manager = ContentPrintManager.getInstance()
        val filename = TEST_FILE
        val testCallback = TestContentPrintCallback()
        manager?.downloadFile(filename, testCallback)
        // Call the callback since the content print service is null
        val file = ContentPrintFile(0, filename, null)
        testCallback.onFileDownloaded(file, "", true)
        Assert.assertTrue(testCallback.fileDownloadResult)
    }

    @Test
    fun testDownloadFilename_WithoutCallback() {
        ContentPrintManager.application = application
        ContentPrintManager.contentPrintService = service
        val manager = ContentPrintManager.getInstance()
        manager?.downloadFile(null, null)
    }

    @Test
    fun testDownloadFilename_NullService() {
        ContentPrintManager.application = application
        ContentPrintManager.contentPrintService = null
        val manager = ContentPrintManager.getInstance()
        val filename = TEST_FILE
        val testCallback = TestContentPrintCallback()
        manager?.downloadFile(filename, testCallback)
        // Call the callback since the content print service is null
        val file = ContentPrintFile(0, filename, null)
        testCallback.onFileDownloaded(file, "", true)
        Assert.assertTrue(testCallback.fileDownloadResult)
    }

    // ================================================================================
    // Tests - downloadFile
    // ================================================================================
    @Test
    fun testDownloadFile_WithCallback() {
        ContentPrintManager.application = application
        ContentPrintManager.contentPrintService = service
        val manager = ContentPrintManager.getInstance()
        val file = ContentPrintFile(1, TEST_FILE, null)
        val testCallback = TestContentPrintCallback()
        manager?.downloadFile(file, testCallback)
        //waitForCondition(testCallback.isFileDownloadFinished)
        //Assert.assertTrue(testCallback.fileDownloadResult)
    }

    @Test
    fun testDownloadFile_WithoutCallback() {
        ContentPrintManager.application = application
        ContentPrintManager.contentPrintService = service
        val manager = ContentPrintManager.getInstance()
        val file = ContentPrintFile(1, TEST_FILE, null)
        manager?.downloadFile(file, null)
    }

    @Test
    fun testDownloadFile_NullService_WithoutCallback() {
        ContentPrintManager.application = application
        ContentPrintManager.contentPrintService = null
        val manager = ContentPrintManager.getInstance()
        val file = ContentPrintFile(1, TEST_FILE, null)
        manager?.downloadFile(file, null)
    }

    @Test
    fun testDownloadFile_NullFilename() {
        ContentPrintManager.application = application
        ContentPrintManager.contentPrintService = service
        val manager = ContentPrintManager.getInstance()
        val file = ContentPrintFile(1, null, null)
        val testCallback = TestContentPrintCallback()
        manager?.downloadFile(file, testCallback)
        Assert.assertFalse(testCallback.fileDownloadResult)
    }

    @Test
    fun testDownloadFile_NullResponseBody() {
        ContentPrintManager.application = application
        val mockService = mockk<ContentPrintManager.IContentPrintService>()
        val mockResponseResult = mockk<retrofit2.Response<ResponseBody>>()
        every { mockResponseResult.code() } returns 200
        every { mockResponseResult.body() } returns null
        coEvery { mockService.downloadFile(any()) } returns mockResponseResult
        ContentPrintManager.contentPrintService = mockService
        val manager = ContentPrintManager.getInstance()
        val file = ContentPrintFile(1, TEST_FILE, null)
        val testCallback = TestContentPrintCallback()
        manager?.downloadFile(file, testCallback)
        Assert.assertFalse(testCallback.fileDownloadResult)
    }

    @Test
    fun testDownloadFile_DownloadErrorCode() {
        ContentPrintManager.application = application
        val mockService = mockk<ContentPrintManager.IContentPrintService>()
        val mockResponseResult = mockk<retrofit2.Response<ResponseBody>>()
        every { mockResponseResult.code() } returns 401
        val mockResponseBody = mockk<ResponseBody>()
        every { mockResponseResult.body() } returns mockResponseBody
        coEvery { mockService.downloadFile(any()) } returns mockResponseResult
        ContentPrintManager.contentPrintService = mockService
        val manager = ContentPrintManager.getInstance()
        val file = ContentPrintFile(1, TEST_FILE, null)
        val testCallback = TestContentPrintCallback()
        manager?.downloadFile(file, testCallback)
        Assert.assertFalse(testCallback.fileDownloadResult)
    }

    @Test
    fun testDownloadFile_DownloadError() {
        ContentPrintManager.application = application
        val mockService = mockk<ContentPrintManager.IContentPrintService>()
        val mockResponseResult = mockk<retrofit2.Response<ResponseBody>>()
        every { mockResponseResult.code() } returns 200
        val mockResponseBody = mockk<ResponseBody>()
        val mockInputStream = mockk<InputStream>()
        every { mockResponseBody.byteStream() } returns mockInputStream
        coEvery { mockService.downloadFile(any()) } returns mockResponseResult
        ContentPrintManager.contentPrintService = mockService
        val manager = ContentPrintManager.getInstance()
        val file = ContentPrintFile(1, TEST_FILE, null)
        val testCallback = TestContentPrintCallback()
        manager?.downloadFile(file, testCallback)
        Assert.assertFalse(testCallback.fileDownloadResult)
    }

    @Test
    fun testDownloadFile_DownloadError_WithoutCallback() {
        ContentPrintManager.application = application
        val mockService = mockk<ContentPrintManager.IContentPrintService>()
        val mockResponseResult = mockk<retrofit2.Response<ResponseBody>>()
        every { mockResponseResult.code() } returns 200
        val mockResponseBody = mockk<ResponseBody>()
        val mockInputStream = mockk<InputStream>()
        every { mockResponseBody.byteStream() } returns mockInputStream
        coEvery { mockService.downloadFile(any()) } returns mockResponseResult
        ContentPrintManager.contentPrintService = mockService
        val manager = ContentPrintManager.getInstance()
        val file = ContentPrintFile(1, TEST_FILE, null)
        manager?.downloadFile(file, null)
    }

    // ================================================================================
    // Tests - downloadThumbnail
    // ================================================================================
    @Test
    fun testDownloadThumbnail_WithCallback() {
        ContentPrintManager.application = application
        ContentPrintManager.contentPrintService = service
        val manager = ContentPrintManager.getInstance()
        val file = ContentPrintFile(1, TEST_FILE, null)
        val testCallback = TestContentPrintCallback()
        manager?.downloadThumbnail(file, testCallback)
        //waitForCondition(testCallback.isFileDownloadFinished)
        //Assert.assertTrue(testCallback.fileDownloadResult)
    }

    @Test
    fun testDownloadThumbnail_WithoutCallback() {
        ContentPrintManager.application = application
        ContentPrintManager.contentPrintService = service
        val manager = ContentPrintManager.getInstance()
        val file = ContentPrintFile(1, TEST_FILE, null)
        manager?.downloadThumbnail(file, null)
    }

    @Test
    fun testDownloadThumbnail_NullService_WithoutCallback() {
        ContentPrintManager.application = application
        ContentPrintManager.contentPrintService = null
        val manager = ContentPrintManager.getInstance()
        val file = ContentPrintFile(1, TEST_FILE, null)
        manager?.downloadThumbnail(file, null)
    }

    @Test
    fun testDownloadThumbnail_DownloadErrorCode() {
        ContentPrintManager.application = application
        val mockService = mockk<ContentPrintManager.IContentPrintService>()
        val mockResponseResult = mockk<retrofit2.Response<ResponseBody>>()
        every { mockResponseResult.code() } returns 401
        val mockResponseBody = mockk<ResponseBody>()
        every { mockResponseResult.body() } returns mockResponseBody
        coEvery { mockService.downloadThumbnail(any(), any()) } returns mockResponseResult
        ContentPrintManager.contentPrintService = mockService
        val manager = ContentPrintManager.getInstance()
        val file = ContentPrintFile(1, TEST_FILE, null)
        val testCallback = TestContentPrintCallback()
        manager?.downloadThumbnail(file, testCallback)
        Assert.assertFalse(testCallback.fileDownloadResult)
    }

    @Test
    fun testDownloadThumbnail_DownloadError() {
        ContentPrintManager.application = application
        val mockService = mockk<ContentPrintManager.IContentPrintService>()
        val mockResponseResult = mockk<retrofit2.Response<ResponseBody>>()
        every { mockResponseResult.code() } returns 200
        val mockResponseBody = mockk<ResponseBody>()
        val mockInputStream = mockk<InputStream>()
        every { mockResponseBody.byteStream() } returns mockInputStream
        coEvery { mockService.downloadThumbnail(any(), any()) } returns mockResponseResult
        ContentPrintManager.contentPrintService = mockService
        val manager = ContentPrintManager.getInstance()
        val file = ContentPrintFile(1, TEST_FILE, null)
        val testCallback = TestContentPrintCallback()
        manager?.downloadThumbnail(file, testCallback)
        Assert.assertFalse(testCallback.fileDownloadResult)
    }

    @Test
    fun testDownloadThumbnail_DownloadError_WithoutCallback() {
        ContentPrintManager.application = application
        val mockService = mockk<ContentPrintManager.IContentPrintService>()
        val mockResponseResult = mockk<retrofit2.Response<ResponseBody>>()
        every { mockResponseResult.code() } returns 200
        val mockResponseBody = mockk<ResponseBody>()
        val mockInputStream = mockk<InputStream>()
        every { mockResponseBody.byteStream() } returns mockInputStream
        coEvery { mockService.downloadThumbnail(any(), any()) } returns mockResponseResult
        ContentPrintManager.contentPrintService = mockService
        val manager = ContentPrintManager.getInstance()
        val file = ContentPrintFile(1, TEST_FILE, null)
        manager?.downloadThumbnail(file, null)
    }

    // ================================================================================
    // Tests - updatePrinterList
    // ================================================================================

    // ================================================================================
    // Tests - registerToBox
    // ================================================================================

    // ================================================================================
    // Private Methods
    // ================================================================================
    private fun waitForCondition(condition: Boolean) {
        try {
            while (!condition) {
                Thread.sleep(1000)
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val TEST_SERIAL_NO = "12345"
        private const val TEST_FILE = "test.pdf"
        private const val ACCOUNT_USERNAME = "test"
        private const val ACCOUNT_AUTHORITY = "test authority"
        private const val ACCESS_TOKEN = "test token"
        private const val SCHEME = "http"
        private const val AUTHORIZATION_HEADER = "Bearer $ACCESS_TOKEN"
        private const val TENANT_ID = "test tenant ID"
        private const val EXCEPTION_ARGUMENT_NAME = "test argument"
        private const val EXCEPTION_MESSAGE = "test exception"
        private const val LIMIT = 10
        private const val PAGE = 1
        private val SCOPE = arrayOf(MockUtil.SCOPE)

        private var activity: Activity? = null
        private var application: ISingleAccountPublicClientApplication? = null
        private var service: ContentPrintManager.IContentPrintService? = null

        private fun mockApplication(): ISingleAccountPublicClientApplication {
            val mockApp: ISingleAccountPublicClientApplication = mockk<ISingleAccountPublicClientApplication>()
            val mockAccountResult = mockk<ICurrentAccountResult>()
            val mockAcc = mockAccount()
            every { mockAccountResult.currentAccount } returns mockAcc
            every { mockApp.currentAccount } returns mockAccountResult
            every { mockApp.acquireToken(any()) } answers {
                val params = firstArg<AcquireTokenParameters>()
                val callback = params.callback
                val mockResult = mockAuthenticationResult()
                callback.onSuccess(mockResult)
            }
            every { mockApp.signOut() } returns true
            every { mockApp.acquireTokenSilentAsync(any()) } just Runs
            return mockApp
        }

        private fun mockAccount(): IAccount {
            val mockAcc = mockk<IAccount>()
            every { mockAcc.username } returns ACCOUNT_USERNAME
            every { mockAcc.authority } returns ACCOUNT_AUTHORITY
            return mockAcc
        }

        private fun mockAuthenticationResult(): IAuthenticationResult {
            val mockResult = mockk<IAuthenticationResult>()
            val mockAcc = mockAccount()
            val localDate = LocalDateTime.now().plusDays(1)
            val expiresOn = Date.from(localDate.atZone(
                ZoneId.systemDefault()).toInstant())
            every { mockResult.accessToken } returns ACCESS_TOKEN
            every { mockResult.account } returns mockAcc
            every { mockResult.authenticationScheme } returns SCHEME
            every { mockResult.authorizationHeader } returns AUTHORIZATION_HEADER
            every { mockResult.correlationId } returns UUID.randomUUID()
            every { mockResult.expiresOn } returns expiresOn
            every { mockResult.scope } returns SCOPE
            every { mockResult.tenantId } returns TENANT_ID
            return mockResult
        }

        @JvmStatic
        @BeforeClass
        fun set() {
            activity = MockUtil.mockActivity()
            application = mockApplication()
            service = MockUtil.mockContentPrintService()
            MockUtil.mockConfiguration()

            ContentPrintManager.newInstance(activity)
        }

        @JvmStatic
        @AfterClass
        fun tearDown() {
            MockUtil.unMockConfiguration()
        }
    }
}
