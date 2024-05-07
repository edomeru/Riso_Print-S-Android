package jp.co.riso.smartdeviceapp

import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.microsoft.identity.client.Logger
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.PublicClientApplicationConfiguration
import com.microsoft.identity.client.PublicClientApplicationConfigurationFactory
import com.microsoft.identity.client.configuration.AccountMode
import com.microsoft.identity.client.configuration.LoggerConfiguration
import com.microsoft.identity.common.internal.telemetry.TelemetryConfiguration
import com.microsoft.identity.common.java.authorities.Authority
import com.microsoft.identity.common.java.authorities.Environment
import com.microsoft.identity.common.java.cache.MsalOAuth2TokenCache
import com.microsoft.identity.common.java.commands.parameters.CommandParameters
import com.microsoft.identity.common.java.interfaces.INameValueStorage
import com.microsoft.identity.common.java.interfaces.IPlatformComponents
import com.microsoft.identity.common.java.interfaces.IStorageSupplier
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
import jp.co.riso.smartprint.R
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.create
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MockUtil {
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

        private const val TEST_VERSION = "0.0.1"
        private const val TEST_URI = "http://test"
        private val TEST_EXPIRATION = tomorrow()
        const val SCOPE = "test scope"

        private fun tomorrow(): String {
            val formatter = DateTimeFormatter.ofPattern(ContentPrintManager.ISO_FORMAT)
            val nextDay = LocalDateTime.now().plusDays(1)
            return nextDay.format(formatter)
        }

        private fun cacheDir() : File {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            return context.cacheDir
        }

        private fun mockSharedPreferences(): SharedPreferences {
            val mockSharedPrefs = mockk<SharedPreferences>()
            every { mockSharedPrefs.getString(any(), any()) } returns TEST_EXPIRATION
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
            every { mockRes.getString(R.string.content_print_uri) } returns TEST_URI
            every { mockRes.getString(R.string.content_print_scope) } returns SCOPE
            every { mockRes.getString(R.string.content_print_scope) } returns SCOPE
            every { mockRes.openRawResource(any()) } answers {
                // Always return a new stream, since the calling function should close the old stream
                ByteArrayInputStream(TEST_CONFIG.toByteArray())
            }
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

        private fun mockContext(): Context {
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
            val mockInputStream = mockk<InputStream>()
            every { mockInputStream.read(any()) } returns -1
            every { mockResponseBody.byteStream() } returns mockInputStream
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

        fun unMockConfiguration() {
            unmockkConstructor(Retrofit.Builder::class)
            unmockkStatic(EncryptedSharedPreferences::class)
        }
    }
}