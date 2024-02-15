package de.gmuth.http

/**
 * Copyright (c) 2020-2021 Gerhard Muth
 */

import de.gmuth.http.Http.Implementation.JavaHttpURLConnection
import java.io.InputStream
import java.io.OutputStream
import java.net.URI
import java.util.*
import javax.net.ssl.SSLContext

interface Http {

    class Config(
            var timeout: Int = 30000, // milli seconds
            var userAgent: String? = null,
            var basicAuth: BasicAuth? = null,
            var sslContext: SSLContext? = null,
            // trust any certificate: sslContextForAnyCertificate()
            // use individual certificate: sslContext(loadCertificate(FileInputStream("printer.pem")))
            // use truststore: sslContext(loadKeyStore(FileInputStream("printer.jks"), "changeit"))
            var verifySSLHostname: Boolean = true,
            var accept: String? = null,
            var acceptEncoding: String? = null,
            var debugLogging: Boolean = false
    ) {
        fun trustAnyCertificate() {
            sslContext = SSLHelper.sslContextForAnyCertificate()
        }
    }

    class BasicAuth(val user: String, val password: String) {
        fun encodeBase64(): String = Base64.getEncoder().encodeToString("$user:$password".toByteArray())
    }

    class Response(
            val status: Int,
            val server: String?,
            val contentType: String?,
            val contentStream: InputStream?
    ) {
        fun isOK() = status == 200
        fun hasContent() = contentStream != null
        fun hasContentType() = contentType != null
        fun readTextContent() = contentStream!!.bufferedReader().use { it.readText() }
        fun contentTypeIsText() = hasContentType() && contentType!!.startsWith("text")
        fun textContent() = if (hasContent() && contentTypeIsText()) "\n" + readTextContent() else ""
    }

    abstract class Client(val config: Config) {
        abstract fun post(
                uri: URI,
                contentType: String,
                writeContent: (OutputStream) -> Unit,
                chunked: Boolean = false
        ): Response
    }

    enum class Implementation(val createClient: (config: Config) -> Client) {
        JavaHttpURLConnection({ config -> HttpURLConnectionClient(config) }),
        Java11HttpClient({ config -> JavaHttpClient(config) });
    }

    companion object {
        var defaultImplementation: Implementation = JavaHttpURLConnection
    }

}