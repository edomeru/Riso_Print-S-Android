/*
 * Copyright (c) 2024 RISO, Inc. All rights reserved.
 *
 * ContentPrintManager.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.controller.print

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.annotations.SerializedName
import com.microsoft.identity.client.AcquireTokenParameters
import com.microsoft.identity.client.AcquireTokenSilentParameters
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.IPublicClientApplication.ISingleAccountApplicationCreatedListener
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.SilentAuthenticationCallback
import com.microsoft.identity.client.exception.MsalException
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.model.ContentPrintFile
import jp.co.riso.smartdeviceapp.model.ContentPrintPrintSettings
import jp.co.riso.smartdeviceapp.model.ContentPrintPrinter
import jp.co.riso.smartprint.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection.HTTP_OK
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


/**
 * @class ContentPrintManager
 *
 * @brief Manager responsible for Content Print management
 */
class ContentPrintManager(context: Context?) {
    /**
     *  MSAL interface and classes
     */
    open class SingleAccountApplicationCreatedListener: ISingleAccountApplicationCreatedListener {
        // This variable is saved for testing purposes
        var application: ISingleAccountPublicClientApplication? = null

        override fun onCreated(application: ISingleAccountPublicClientApplication) {
            // This requires "account_mode" : "SINGLE" in the config json file.
            this.application = application
            ContentPrintManager.application = application
        }

        override fun onError(exception: MsalException) {
            Log.e(TAG, "${exception.errorCode}: ${exception.message}")
            this.application = null
            ContentPrintManager.application = null
        }
    }

    interface IAuthenticationCallback {
        fun onAuthenticationStarted()
        fun onAuthenticationFinished()
    }

    class InteractiveAuthenticationCallback(private var authenticationCallback: IAuthenticationCallback?) : AuthenticationCallback {
        override fun onSuccess(authenticationResult: IAuthenticationResult?) {
            isLoggedIn = true
            receiveAuthenticationResult(authenticationResult, this.authenticationCallback)
        }

        override fun onError(exception: MsalException?) {
            Log.e(TAG, "===== [MSAL][Login] ${exception?.errorCode}: ${exception?.message} =====")
            isLoggedIn = false
            this.authenticationCallback?.onAuthenticationFinished()
        }

        override fun onCancel() {
            Log.e(TAG, "===== [MSAL][Login] Login was cancelled by the user. =====")
            isLoggedIn = false
            this.authenticationCallback?.onAuthenticationFinished()
        }
    }

    class RefreshAuthenticationCallback(private var authenticationCallback: IAuthenticationCallback?) : SilentAuthenticationCallback {
        override fun onSuccess(authenticationResult: IAuthenticationResult?) {
            isLoggedIn = true
            receiveAuthenticationResult(authenticationResult, this.authenticationCallback)
        }

        override fun onError(exception: MsalException?) {
            Log.e(TAG, "===== [MSAL][Refresh] ${exception?.errorCode}: ${exception?.message} =====")
            isLoggedIn = false
            this.authenticationCallback?.onAuthenticationFinished()
        }
    }

    /**
     * HTTP REST interface and classes for Retrofit
     */
    interface IContentPrintService {
        @GET(API_LIST_FILES)
        fun listFiles(@Query("limit") limit: Int, @Query("page") page: Int, @Query("printer_type") printerType: Int): Call<ContentPrintFileResult>

        @Streaming
        @GET(API_DOWNLOAD_FILE)
        suspend fun downloadThumbnail(@Path("file_id") fileId: Int, @Query("thumbnail") thumbnail: Int): Response<ResponseBody>

        @Streaming
        @GET(API_DOWNLOAD_FILE)
        suspend fun downloadFile(@Path("file_id") fileId: Int): Response<ResponseBody>

        @GET(API_LIST_PRINTERS)
        fun listPrinters(): Call<ContentPrintPrinterResult>

        @POST(API_REGISTER)
        fun registerToBox(@Body request: ContentPrintBoxRegistrationRequest): Call<ResponseBody>
    }

    interface IContentPrintCallback {
        fun onFileListUpdated(success: Boolean)

        fun onThumbnailDownloaded(contentPrintFile: ContentPrintFile, filePath: String, success: Boolean)

        fun onStartFileDownload()

        fun onFileDownloaded(contentPrintFile: ContentPrintFile, filePath: String, success: Boolean)

        fun onPrinterListUpdated(success: Boolean)
    }

    interface IRegisterToBoxCallback {
        fun onStartBoxRegistration()
        fun onBoxRegistered(success: Boolean)
    }

    class AuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val requestBuilder = chain.request().newBuilder()
            requestBuilder.addHeader("Authorization", "Bearer $_accessToken")
            return chain.proceed(requestBuilder.build())
        }
    }

    class ContentPrintFileResult {
        @SerializedName("list")
        var list: List<ContentPrintFile> = ArrayList()

        @SerializedName("count")
        var count: Int = -1
    }

    class ContentPrintFileResultCallback(var callback: IContentPrintCallback?) : Callback<ContentPrintFileResult> {
        override fun onFailure(call: Call<ContentPrintFileResult>, t: Throwable) {
            Log.e(TAG, "updateFileList - Error ${t.message}")
            callback?.onFileListUpdated(false)
        }

        override fun onResponse(
            call: Call<ContentPrintFileResult>,
            response: Response<ContentPrintFileResult>
        ) {
            fileCount = response.body()?.count ?: 0
            fileList = response.body()?.list ?: ArrayList()
            callback?.onFileListUpdated(true)
            Log.d(TAG, "updateFileList - END")
        }
    }

    class ContentPrintFileDownloadCallback(var manager: ContentPrintManager, var filename: String?, var callback: IContentPrintCallback?) : Callback<ContentPrintFileResult> {
        override fun onFailure(call: Call<ContentPrintFileResult>, t: Throwable) {
            Log.e(TAG, "listFiles - Error ${t.message}")
            callback?.onFileListUpdated(false)
        }

        override fun onResponse(
            call: Call<ContentPrintFileResult>,
            response: Response<ContentPrintFileResult>
        ) {
            fileCount = response.body()?.count ?: 0
            fileList = response.body()?.list ?: ArrayList()
            Log.d(TAG, "updateFileList - END")

            for (file in fileList) {
                if (file.filename == filename) {
                    selectedFile = file
                    manager.downloadFile(file, callback)
                    break
                }
            }
        }
    }

    class ContentPrintPrinterResult {
        @SerializedName("list")
        var list: List<ContentPrintPrinter> = ArrayList()

        @SerializedName("count")
        var count: Int = -1
    }

    class ContentPrintPrinterResultCallback(var callback: IContentPrintCallback?): Callback<ContentPrintPrinterResult> {
        override fun onFailure(call: Call<ContentPrintPrinterResult>, t: Throwable) {
            Log.e(TAG, "updatePrinterList - Error ${t.message}")
            callback?.onPrinterListUpdated(false)
        }

        override fun onResponse(
            call: Call<ContentPrintPrinterResult>,
            response: Response<ContentPrintPrinterResult>
        ) {
            printerList = response.body()?.list ?: ArrayList()
            selectedPrinter = if (printerList.isNotEmpty()) printerList.first() else null
            for (printer in printerList) {
                Log.d(TAG, "${printer.serialNo}, ${printer.printerName}")
            }
            callback?.onPrinterListUpdated(true)
            Log.d(TAG, "updatePrinterList - END")
        }
    }

    class ContentPrintBoxRegistrationRequest {
        @SerializedName("file_id")
        var fileId: Int = -1

        @SerializedName("serial_no")
        var serialNo: String? = null

        @SerializedName("print_settings")
        var printSettings: ContentPrintPrintSettings? = null
    }

    class ContentPrintBoxRegistrationResultCallback(var callback: IRegisterToBoxCallback?) : Callback<ResponseBody> {
        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            callback?.onBoxRegistered(false)
            Log.e(TAG, "registerToBox - Error ${t.message}")
        }

        override fun onResponse(
            call: Call<ResponseBody>,
            response: Response<ResponseBody>
        ) {
            callback?.onBoxRegistered(true)
            Log.d(TAG, "registerToBox - END")
        }
    }

    /**
     * Initialization
     */
    init {
        scopes = ArrayList()
        if (context != null) {
            // Initialize the application
            PublicClientApplication.createSingleAccountPublicClientApplication(
                context,
                R.raw.msal_content_print_config,
                SingleAccountApplicationCreatedListener()
            )

            val masterKey: MasterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            preferences = EncryptedSharedPreferences.create(
                context,
                KEY_STORE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            val res = context.resources
            _cacheDir = context.cacheDir?.absolutePath
            _uri = res.getString(R.string.content_print_uri)
            scopes!!.add(res.getString(R.string.content_print_scope))
            initializeHttpClient()

            // Get the access token from the key store
            _accessToken = getKeyValue(KEY_TOKEN)
            _authority = getKeyValue(KEY_AUTHORITY)
            checkAccessToken(context as? IAuthenticationCallback)
        } else {
            // Reset variables to null
            application = null
            _cacheDir = null
            _uri = null
        }
    }

    fun login(activity: Activity?, authenticationCallback: IAuthenticationCallback) {
        if (application != null) {
            authenticationCallback.onAuthenticationStarted()

            val params = AcquireTokenParameters.Builder()
                .startAuthorizationFromActivity(activity)
                .withScopes(scopes)
                .withCallback(InteractiveAuthenticationCallback(authenticationCallback))
                .build()
            application!!.acquireToken(params)
        }
    }

    fun logout(authenticationCallback: IAuthenticationCallback?) {
        if (application != null && isLoggedIn) {
            CoroutineScope(Dispatchers.IO).launch {
                application!!.signOut()
            }
        }

        isLoggedIn = false

        removeKey(KEY_TOKEN)
        removeKey(KEY_AUTHORITY)
        removeKey(KEY_EXPIRES_ON)
        authenticationCallback?.onAuthenticationFinished()
    }

    fun updateFileList(limit: Int, page: Int, callback: IContentPrintCallback?) {
        Log.d(TAG, "updateFileList - START")
        CoroutineScope(Dispatchers.IO).launch {
            contentPrintService?.listFiles(limit, page, PRINTER_TYPE)
                ?.enqueue(ContentPrintFileResultCallback(callback))
        }
    }

    fun downloadFile(filename: String?, callback: IContentPrintCallback?) {
        CoroutineScope(Dispatchers.IO).launch {
            callback?.onStartFileDownload()

            // Get the file from the file list
            contentPrintService?.listFiles(0, 0, PRINTER_TYPE)
                ?.enqueue(ContentPrintFileDownloadCallback(this@ContentPrintManager, filename, callback))
        }
    }

    fun downloadThumbnail(contentPrintFile: ContentPrintFile, callback: IContentPrintCallback?) {
        CoroutineScope(Dispatchers.IO).launch {
            downloadFile(contentPrintFile, callback, true)
        }
    }

    fun downloadFile(contentPrintFile: ContentPrintFile, callback: IContentPrintCallback?) {
        CoroutineScope(Dispatchers.IO).launch {
            downloadFile(contentPrintFile, callback, false)
        }
    }

    private suspend fun downloadFile(contentPrintFile: ContentPrintFile,
                             callback: IContentPrintCallback?,
                             isThumbnail: Boolean) {
        try {
            if (!isThumbnail) {
                callback?.onStartFileDownload()
            }

            val response: Response<ResponseBody>? = if (isThumbnail) {
                Log.d(TAG, "downloadThumbnail: ${contentPrintFile.filename} - START")
                contentPrintService?.downloadThumbnail(contentPrintFile.fileId, 1)
            } else {
                Log.d(TAG, "downloadFile: ${contentPrintFile.filename} - START")
                contentPrintService?.downloadFile(contentPrintFile.fileId)
            }

            val responseCode = response?.code()
            Log.d(TAG, "download ${contentPrintFile.filename} returned $responseCode")

            val responseBody = response?.body()
            if (responseCode == HTTP_OK && responseBody != null) {
                val filename = contentPrintFile.filename?.substringBeforeLast(".")
                var filePath = "$_cacheDir/$filename"
                if (isThumbnail) {
                    filePath += "-${contentPrintFile.fileId}-thumbnail"
                }
                filePath += "." + contentPrintFile.filename?.substringAfterLast(".")

                val inputStream = responseBody.byteStream()
                saveFileToPath(inputStream, filePath)

                if (isThumbnail) {
                    callback?.onThumbnailDownloaded(contentPrintFile, filePath, true)
                } else {
                    callback?.onFileDownloaded(contentPrintFile, filePath, true)

                    // Clear the file name
                    filenameFromNotification = null
                }

                if (isThumbnail) {
                    Log.d(TAG, "downloadThumbnail: ${contentPrintFile.filename} - END")
                } else {
                    Log.d(TAG, "downloadFile: ${contentPrintFile.filename} - END")
                }
            } else {
                if (isThumbnail) {
                    callback?.onThumbnailDownloaded(contentPrintFile, "", false)
                } else {
                    callback?.onFileDownloaded(contentPrintFile, "", false)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in downloading file ${contentPrintFile.filename}: $e")
            if (isThumbnail) {
               callback?.onThumbnailDownloaded(contentPrintFile, "", false)
            } else {
               callback?.onFileDownloaded(contentPrintFile, "", false)
            }
        }
    }

    fun updatePrinterList(callback: IContentPrintCallback?) {
        Log.d(TAG, "updatePrinterList - START")
        CoroutineScope(Dispatchers.IO).launch {
            contentPrintService?.listPrinters()
                ?.enqueue(ContentPrintPrinterResultCallback(callback))
        }
    }

    fun registerToBox(fileId: Int, serialNo: String, printSettings: ContentPrintPrintSettings, callback: IRegisterToBoxCallback?) {
        Log.d(TAG, "registerToBox - START")
        callback?.onStartBoxRegistration()

        CoroutineScope(Dispatchers.IO).launch {
            val request = ContentPrintBoxRegistrationRequest()
            request.fileId = fileId
            request.serialNo = serialNo
            request.printSettings = printSettings

            contentPrintService?.registerToBox(request)
                ?.enqueue(ContentPrintBoxRegistrationResultCallback(callback))
        }
    }

    companion object {
        fun newInstance(activity: Activity?) {
            _manager = ContentPrintManager(activity)
        }

        fun getInstance(): ContentPrintManager? {
            return _manager
        }

        fun getFilename(intent: Intent): String? {
            val extras = intent.extras
            if (extras != null) {
                val message = extras.getString(AppConstants.VAL_KEY_CONTENT_PRINT)
                val filename = message?.substringAfter("「")?.substringBefore("」")
                return filename?.substringAfter("\"")?.substringBefore("\"")
            }
            return null
        }

        private fun refreshToken(authenticationCallback: IAuthenticationCallback?) {
            if (_authority != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    authenticationCallback?.onAuthenticationStarted()

                    val params = AcquireTokenSilentParameters.Builder()
                        .withScopes(scopes)
                        .forAccount(application?.currentAccount?.currentAccount)
                        .fromAuthority(_authority)
                        .withCallback(RefreshAuthenticationCallback(authenticationCallback))
                        .build()
                    application?.acquireTokenSilentAsync(params)
                }
            }
        }

        private fun initializeHttpClient() {
            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor())
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl(_uri!!)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            contentPrintService = retrofit.create(IContentPrintService::class.java)
        }

        private fun receiveAuthenticationResult(
            authenticationResult: IAuthenticationResult?,
            authenticationCallback: IAuthenticationCallback?) {
            _authority = authenticationResult?.account?.authority
            _accessToken = authenticationResult?.accessToken
            val expiresOn = authenticationResult?.expiresOn?.toInstant()?.atZone(ZoneId.of(TIME_ZONE))

            // Save the access token in the key store
            saveKeyValue(KEY_TOKEN, _accessToken)
            saveKeyValue(KEY_AUTHORITY, _authority)
            if (expiresOn != null) {
                saveKeyValue(KEY_EXPIRES_ON, dateToString(expiresOn))
            } else {
                removeKey(KEY_EXPIRES_ON)
            }
            Log.d(TAG, "===== Save the access token in the key store $_accessToken =====")
            Log.d(TAG, "===== Expires On: $expiresOn =====")

            initializeHttpClient()
            authenticationCallback?.onAuthenticationFinished()

            if (expiresOn != null) {
                val duration = Duration.between(expiresOn.toLocalDateTime(), LocalDateTime.now())
                CoroutineScope(Dispatchers.IO).launch {
                    delay(duration.toMillis())
                    refreshToken(authenticationCallback)
                }
            }
        }

        fun checkAccessToken(authenticationCallback: IAuthenticationCallback?) {
            if (_accessToken != null) {
                // Check if the access token is expired
                val expireString = getKeyValue(KEY_EXPIRES_ON)
                if (expireString != null) {
                    val expiresOn = stringToDate(expireString)
                    isLoggedIn = !isExpired(expiresOn)

                    if (!isLoggedIn) {
                        refreshToken(authenticationCallback)
                    }
                }
            }
        }

        private fun dateToString(date: ZonedDateTime?): String {
            val formatter =  DateTimeFormatter.ofPattern(ISO_FORMAT)
            Log.d("TEST", "dateToString: ${formatter.format(date)}")
            return formatter.format(date)
        }

        private fun stringToDate(dateStr: String?): ZonedDateTime? {
            val formatter =  DateTimeFormatter.ofPattern(ISO_FORMAT)
            val dateTime = LocalDateTime.parse(dateStr, formatter)
            Log.d("TEST", "stringToDate: $dateStr")
            return dateTime.atZone(ZoneId.of(TIME_ZONE))
        }

        fun saveKeyValue(key: String, value: String?) {
            val editor = preferences?.edit()
            editor?.putString(key, value)
            editor?.commit()
            Log.d("TEST", "saveKeyValue $key = $value")
        }

        fun getKeyValue(key: String): String? {
            val value = preferences?.getString(key, null)
            Log.d("TEST", "getKeyValue $key = $value")
            return value
        }

        fun removeKey(key: String?) {
            val editor = preferences?.edit()
            editor?.remove(key)
            editor?.commit()
        }

        fun isExpired(dateTime: ZonedDateTime?): Boolean {
            if (dateTime != null) {
                val localZone = dateTime.withZoneSameInstant(ZoneId.systemDefault())
                val expiresOn = localZone.toLocalDateTime()
                val now = LocalDateTime.now()
                return expiresOn < now
            }
            return true
        }

        private fun saveFileToPath(inputStream: InputStream?, filePath: String) {
            val outputStream = FileOutputStream(filePath)
            outputStream.use { output ->
                val buffer = ByteArray(BUFFER_SIZE)
                var read = 0
                while (inputStream?.read(buffer).also {
                        if (it != null) {
                            read = it
                        }
                    } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
            }
        }

        const val KEY_STORE = "ContentPrintKeyStore"
        const val KEY_TOKEN = "ContentPrintKeyToken"
        const val KEY_AUTHORITY = "ContentPrintKeyAuthority"
        const val KEY_EXPIRES_ON = "ContentPrintKeyExpiresOn"
        const val ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'"
        const val TIME_ZONE = "UTC"

        private const val BUFFER_SIZE = 1024 * 4

        private const val PRINTER_TYPE = 1 // IJ printers only
        private const val API_LIST_FILES = "api/content/list"
        private const val API_DOWNLOAD_FILE = "api/content/preview/{file_id}"
        private const val API_LIST_PRINTERS = "api/device/printerList"
        private const val API_REGISTER = "api/content/registered"

        val TAG: String = ContentPrintManager::class.java.simpleName ?: "Content Print"

        private var _manager: ContentPrintManager? = null
        private var _cacheDir: String? = null
        private var _uri: String? = null
        private var _authority: String? = null
        private var _accessToken: String? = null

        var preferences: SharedPreferences? = null
        var application: ISingleAccountPublicClientApplication? = null
        var contentPrintService: IContentPrintService? = null
        var scopes: ArrayList<String>? = null
        var isLoggedIn: Boolean = false
        var filenameFromNotification: String? = null
        var selectedFile: ContentPrintFile? = null
        var selectedPrinter: ContentPrintPrinter? = null
        var filePath: String? = null
        var isFileFromContentPrint: Boolean = false
        var isBoxRegistrationMode: Boolean = false
        var fileCount: Int = 0
        var fileList: List<ContentPrintFile> = ArrayList()
        var printerList: List<ContentPrintPrinter> = ArrayList()
    }
}