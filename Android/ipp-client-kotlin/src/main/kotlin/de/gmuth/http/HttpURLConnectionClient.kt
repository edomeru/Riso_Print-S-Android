package de.gmuth.http

/**
 * Copyright (c) 2020-2021 Gerhard Muth
 */

import de.gmuth.log.Logging
import de.gmuth.log.Logging.LogLevel.*
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URI
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection

class HttpURLConnectionClient(config: Http.Config = Http.Config()) : Http.Client(config) {

    companion object {
        val log = Logging.getLogger {}
        val julHandler = object : Handler() {
            // redirect java util logging message, ignore level
            override fun publish(record: LogRecord) = record.run {
                Logging.getLogger(loggerName, DEBUG).log(DEBUG) { message }
            }

            override fun flush() = Unit
            override fun close() = Unit
        }
    }

    init {
        log.debug { "HttpURLConnectionClient created" }
        if (config.debugLogging) // logger name depends on jvm implementation? omg!
            Logger.getLogger("sun.net.www.protocol.http.HttpURLConnection").run {
                if (!handlers.contains(julHandler)) addHandler(julHandler)
                level = Level.ALL
            }
    }

    override fun post(uri: URI, contentType: String, writeContent: (OutputStream) -> Unit, chunked: Boolean): Http.Response {
        with(uri.toURL().openConnection() as HttpURLConnection) {
            if (this is HttpsURLConnection && config.sslContext != null) {
                sslSocketFactory = config.sslContext!!.socketFactory
                if (!config.verifySSLHostname) hostnameVerifier = HostnameVerifier { _, _ -> true }
            }
            doOutput = true // trigger POST method
            config.run {
                connectTimeout = timeout
                readTimeout = timeout
                accept?.let { setRequestProperty("Accept", it) }
                acceptEncoding?.let { setRequestProperty("Accept-Encoding", it) }
                basicAuth?.let { setRequestProperty("Authorization", "Basic ${it.encodeBase64()}") }
                userAgent?.let { setRequestProperty("User-Agent", it) }
            }
            setRequestProperty("Content-Type", contentType)
            if (chunked) setChunkedStreamingMode(0)
            writeContent(outputStream)
            for ((key, values) in headerFields) {
                log.log(if (responseCode < 300) TRACE else ERROR) { "$key = $values" }
            }
            val responseStream = try {
                inputStream
            } catch (exception: Exception) {
                log.error { "http exception: $responseCode $responseMessage" }
                errorStream
            }
            return Http.Response(
                    responseCode,
                    getHeaderField("Server"),
                    getHeaderField("Content-Type"),
                    responseStream
            )
        }
    }
}
