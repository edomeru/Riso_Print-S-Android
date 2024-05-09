package jp.co.riso.smartdeviceapp

import android.accounts.AccountManager
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.util.DisplayMetrics
import android.widget.ArrayAdapter
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.test.platform.app.InstrumentationRegistry
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkConstructor
import io.mockk.unmockkStatic
import jp.co.riso.smartdeviceapp.controller.print.ContentPrintManager
import jp.co.riso.smartdeviceapp.model.ContentPrintFile
import jp.co.riso.smartdeviceapp.view.contentprint.ContentPrintFileAdapter
import jp.co.riso.smartprint.R
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.create
import java.io.ByteArrayInputStream
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MockTestUtil {
    companion object {
        private const val PACKAGE_NAME = "jp.co.riso.smartprint"
        private const val CLIENT_ID = "00000000-0000-0000-0000-000000000000"
        private const val REDIRECT_URI = "msauth://$PACKAGE_NAME/test"

        // MockK generates an ArrayIndexOutOfBoundsException when static mocking a class
        // that contains a class.getSimpleName() call
        // As a workaround for not being able to mockk PublicClientApplication,
        // create a valid config file for the MSAL libraries
        private const val TEST_CONFIG = "{ \"client_id\" : \"$CLIENT_ID\"," +
                "\"authorization_user_agent\" : \"WEBVIEW\"," +
                "\"redirect_uri\" : \"$REDIRECT_URI\"," +
                "\"authorization_in_current_task\": false," +
                "\"power_opt_check_for_network_req_enabled\": false," +
                "\"broker_redirect_uri_registered\": false," +
                "\"environment\": \"PreProduction\"," +
                "\"authorities\" : [ { \"type\": \"AAD\", \"audience\": {" +
                "\"type\": \"PersonalMicrosoftAccount\" }," +
                "\"default\": true } ] }"

        private const val TEST_URI = "http://test"
        private const val PIXEL_SIZE = 100
        private val TEST_EXPIRATION = tomorrow()
        const val SCOPE = "test scope"

        private fun tomorrow(): String {
            val formatter = DateTimeFormatter.ofPattern(ContentPrintManager.ISO_FORMAT)
            val nextDay = LocalDateTime.now().plusDays(1)
            return nextDay.format(formatter)
        }

        fun cacheDir() : File {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            return context.cacheDir
        }

        fun mockSharedPreferences(): SharedPreferences {
            val mockSharedPrefs = mockk<SharedPreferences>()
            every { mockSharedPrefs.getString(any(), any()) } returns TEST_EXPIRATION
            val mockEditor = mockk<SharedPreferences.Editor>()
            every { mockSharedPrefs.edit() } returns mockEditor
            every { mockEditor.putString(any(), any()) } returns mockEditor
            every { mockEditor.remove(any()) } returns mockEditor
            every { mockEditor.commit() } returns true
            every { mockEditor.apply() } just Runs
            return mockSharedPrefs
        }

        private fun mockResources(): Resources {
            val mockRes = mockk<Resources>()
            every { mockRes.getString(any()) } answers {
                val id = firstArg<Int>()
                if (id == R.string.content_print_uri) {
                    TEST_URI
                } else if (id == R.string.content_print_scope) {
                    SCOPE
                } else {
                    ""
                }
            }
            every { mockRes.openRawResource(any()) } answers {
                // Always return a new stream, since the calling function should close the old stream
                ByteArrayInputStream(TEST_CONFIG.toByteArray())
            }
            every { mockRes.getValue(any<Int>(), any(), any()) } just Runs
            every { mockRes.getInteger(any()) } returns 0
            every { mockRes.getBoolean(any()) } returns false
            every { mockRes.getColor(any()) } returns 0
            every { mockRes.displayMetrics } returns DisplayMetrics()
            every { mockRes.configuration } returns Configuration()
            every { mockRes.getDimensionPixelSize(any()) } returns PIXEL_SIZE
            return mockRes
        }

        private fun mockPackageManager() : PackageManager {
            val mockPM = mockk<PackageManager>()
            every { mockPM.checkPermission(any(), any()) } returns 0
            val mockPackageInfo = mockk<PackageInfo>()
            every { mockPackageInfo.longVersionCode } returns 0
            every {
                mockPM.getPackageInfo(
                    any<String>(),
                    any<Int>()
                )
            } returns mockPackageInfo
            val mockApplicationInfo = mockk<ApplicationInfo>()
            every {
                mockPM.getApplicationInfo(
                    any(),
                    any<Int>()
                )
            } returns mockApplicationInfo

            return mockPM
        }

        fun mockActivity(): Activity {
            val mockAct = mockk<Activity>()
            every { mockAct.applicationContext } returns mockContext()
            every { mockAct.cacheDir } returns cacheDir()
            every { mockAct.packageName } returns PACKAGE_NAME
            val mockSharedPrefs = mockSharedPreferences()
            every { mockAct.getSharedPreferences(any(), any()) } returns mockSharedPrefs
            val mockRes = mockResources()
            every { mockAct.resources } returns mockRes
            val mockPM = mockPackageManager()
            every { mockAct.packageManager } returns mockPM
            val mockAccountManager = mockk<AccountManager>()
            every { mockAccountManager.authenticatorTypes } returns arrayOf()
            every { mockAct.getSystemService(Context.ACCOUNT_SERVICE) } returns mockAccountManager
            return mockAct
        }

        fun mockContext(): Context {
            val mockCtx = mockk<Context>()
            every { mockCtx.applicationContext } returns mockCtx
            every { mockCtx.packageName } returns PACKAGE_NAME
            val mockSharedPrefs = mockSharedPreferences()
            every { mockCtx.getSharedPreferences(any(), any()) } returns mockSharedPrefs
            val mockPM = mockPackageManager()
            every { mockCtx.packageManager } returns mockPM
            val mockRes = mockResources()
            every { mockCtx.resources } returns mockRes
            val mockAccountManager = mockk<AccountManager>()
            every { mockAccountManager.authenticatorTypes } returns arrayOf()
            every { mockCtx.getSystemService(Context.ACCOUNT_SERVICE) } returns mockAccountManager
            val mockApplicationInfo = mockk<ApplicationInfo>()
            every { mockCtx.applicationInfo } returns mockApplicationInfo
            every { mockCtx.cacheDir } returns cacheDir()
            return mockCtx
        }

        fun mockContentPrintService(): ContentPrintManager.IContentPrintService {
            val mockService = mockk<ContentPrintManager.IContentPrintService>()
            val mockFileResult = mockk<Call<ContentPrintManager.ContentPrintFileResult>>()
            every { mockFileResult.enqueue(any()) } just Runs
            val mockResponseResult = mockk<retrofit2.Response<ResponseBody>>()
            every { mockResponseResult.code() } returns 200
            val mockResponseBody = mockk<ResponseBody>()
            val inputStream = ByteArrayInputStream(TEST_CONFIG.toByteArray())
            every { mockResponseBody.byteStream() } returns inputStream
            every { mockResponseResult.body() } returns mockResponseBody
            every { mockService.listFiles(any(), any(), any()) } returns mockFileResult
            coEvery { mockService.downloadFile(any()) } returns mockResponseResult
            coEvery { mockService.downloadThumbnail(any(), any()) } returns mockResponseResult
            val mockPrinterResult = mockk<Call<ContentPrintManager.ContentPrintPrinterResult>>()
            every { mockPrinterResult.enqueue(any()) } just Runs
            every { mockService.listPrinters() } returns mockPrinterResult
            val mockRegisterResult = mockk<Call<ResponseBody>>()
            every { mockRegisterResult.enqueue(any()) } just Runs
            every { mockService.registerToBox(any()) } returns mockRegisterResult
            return mockService
        }

        /**
         * MockK UI components that can cause errors during testing
         */
        fun mockUI() {
            // The ValueAnimator class can only run on a looper thread
            mockkStatic(ValueAnimator::class)
            val mockAnimator = mockk<ValueAnimator>(relaxed = true)
            every { mockAnimator.animatedValue } returns 1
            every { mockAnimator.addUpdateListener(any()) } answers {
                val listener = firstArg<ValueAnimator.AnimatorUpdateListener>()
                listener.onAnimationUpdate(mockAnimator)
            }
            every { ValueAnimator.ofInt(any(), any()) } returns mockAnimator

            mockkConstructor(ContentPrintFileAdapter::class)
            // ArrayAdapter.clear calls AbstractList.remove, which generates an UnsupportedOperationException
            every { anyConstructed<ContentPrintFileAdapter>().clear() } just Runs
            // ArrayAdapter.addAll calls AbstractList.add, which generates an UnsupportedOperationException
            every { anyConstructed<ContentPrintFileAdapter>().addAll(any<Collection<ContentPrintFile>>()) } just Runs
        }

        /**
         * UnMockK UI components that can cause errors during testing
         */
        fun unMockUI() {
            unmockkStatic(ValueAnimator::class)
            unmockkConstructor(ContentPrintFileAdapter::class)
        }

        /**
         * MockK the Configuration for MSAL libraries
         */
        fun mockConfiguration() {
            mockkConstructor(Retrofit.Builder::class)
            val mockRetrofit = mockk<Retrofit>()
            every { anyConstructed<Retrofit.Builder>().build() } returns mockRetrofit
            val mockService = mockContentPrintService()
            every { mockRetrofit.create<ContentPrintManager.IContentPrintService>() } returns mockService

            val mockSharedPrefs = mockSharedPreferences()
            mockkStatic(EncryptedSharedPreferences::class)
            every {
                EncryptedSharedPreferences.create(
                    any<Context>(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns mockSharedPrefs
        }

        /**
         * UnMockK the Configuration for MSAL libraries
         */
        fun unMockConfiguration() {
            unmockkConstructor(Retrofit.Builder::class)
            unmockkStatic(EncryptedSharedPreferences::class)
        }
    }
}