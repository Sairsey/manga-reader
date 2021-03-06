package com.mangajet.mangajet.data

import com.mangajet.mangajet.log.Logger
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Callback
import okhttp3.Call
import okio.IOException
import java.io.OutputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

// Singleton which will work with okHTTP to provide access to web resources
object WebAccessor {
    private val client = OkHttpClient()
        .newBuilder()
        .connectTimeout(1, TimeUnit.MINUTES)
        .readTimeout(1, TimeUnit.MINUTES)
        .build()
    private val fastClient = OkHttpClient()
        .newBuilder()
        .connectTimeout(2, TimeUnit.MILLISECONDS)
        .readTimeout(2, TimeUnit.MILLISECONDS)
        .build()

    const val NOT_FOUND = 404

    const val NO_ERROR = 0
    const val IO_ERROR = 1
    const val BODY_NULL_ERROR = 2
    const val RET_CODE_ERROR = 3

    // Function to retrieve length of file by url
    fun getLength(url: String, headers: Map<String, String> = mapOf()) : Long{

        // Atomic counter
        val countDownLatch = CountDownLatch(1)

        var size : Long = -1
        // Simple Callback which will return string
        val callback = object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Logger.log("OnFailure response in getLength in WebAcc: " + e.message, Logger.Lvl.WARNING)
                countDownLatch.countDown()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        try {
                            size = response.body!!.contentLength()
                        }
                        catch (expected: NullPointerException) {
                            Logger.log("Null Pointer exception in getLength", Logger.Lvl.WARNING)
                            // That's sad, but return -1 work as flag for us
                        }
                    }
                    countDownLatch.countDown()
                }
            }
        }

        // Make async call
        val call = getAsync(url, callback, headers, fast = true)

        // And wait for it
        countDownLatch.await()

        return size
    }

    // Function to acquire things asynchronously
    private fun getAsync(url: String, callback: Callback,
                         headers: Map<String, String> = mapOf(), fast: Boolean = false ) : Call {

        var preRequest = Request.Builder()
            .url(url)

        var isFirst = true

        // Generate headers
        for ((key, value) in headers) {
            if (isFirst) {
                preRequest = preRequest.header(key, value)
                isFirst = false
            }
            else {
                preRequest = preRequest.addHeader(key, value)
            }
        }

        // Build request
        val request = preRequest.build()

        // Make async call
        val call: Call

        if (fast)
            call = fastClient.newCall(request)
        else
            call = client.newCall(request)

        // Set callback
        call.enqueue(callback)

        return call
    }

    // Function to acquire text synchronously
    // Note: Please use it if text are less then 1Mb
    fun getTextSync(url: String, headers: Map<String, String> = mapOf()) : String {
        // We have here another android crazy stuff
        // Problem is that Android blocks sockets from main thread
        // So to get text synchronously we need to
        // do asynchronous request and wait for it.

        // Default value
        var str = ""

        // Atomic counter
        val countDownLatch = CountDownLatch(1)

        var isError = NO_ERROR
        var retCode = 0

        // Simple Callback which will return string
        val callback = object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                isError = IO_ERROR
                Logger.log("OnFailure response in getTextSync in WebAccessor: " + e.message, Logger.Lvl.WARNING)
                println(e.message) // for debugging purposes
                countDownLatch.countDown()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        try {
                            str = response.body!!.string()
                        }
                        catch (expected: NullPointerException) {
                            Logger.log("Null Pointer exception in getTextSync", Logger.Lvl.WARNING)
                            isError = BODY_NULL_ERROR
                            retCode = NOT_FOUND
                        }
                    }
                    else {
                        isError = RET_CODE_ERROR
                        retCode = response.code
                    }
                    countDownLatch.countDown()
                }
            }
        }

        // Make async call
        val call = getAsync(url, callback, headers)

        // And wait for it
        countDownLatch.await()

        // Throw our exceptions in case we have any errors
        if (isError == IO_ERROR)
            throw MangaJetException("Connection lost. May be a problem with yours connectivity or timeout")
        else if (isError == BODY_NULL_ERROR || isError == RET_CODE_ERROR)
            throw MangaJetException("Server dismissed our request with return code $retCode")

        return str
    }

    // Class which represent async handler of streaming web data to some file
    class Promise(outputStream: OutputStream) {
        // Atomic counter
        private var countDownLatch = CountDownLatch(1)
        // Error state
        private var isError = NO_ERROR
        // HTTP return code
        private var retCode = 0
        // Stream in which we want to write
        // private var outputStream : OutputStream

        // Simple Callback which will fill outputStream
        val callback = object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                isError = IO_ERROR
                Logger.log("OnFailure response in WebAccessor: " + e.message, Logger.Lvl.WARNING)
                println(e.message) // for debugging purposes
                countDownLatch.countDown()
            }

            @Suppress("NestedBlockDepth")
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        try {
                            response.body!!.byteStream().use { input ->
                                outputStream.use { output ->
                                    input.copyTo(output)
                                }
                            }
                        }
                        catch (expected: NullPointerException) {
                            Logger.log("Null Pointer exception in Promise in WebAccessor", Logger.Lvl.WARNING)
                            isError = BODY_NULL_ERROR
                            retCode = NOT_FOUND
                        }
                    }
                    else {
                        isError = RET_CODE_ERROR
                        retCode = response.code
                    }
                    countDownLatch.countDown()
                }
            }
        }

        // Function for synchronization
        // MAY THROW MangaJetException
        fun join() {
            // wait for this promise
            countDownLatch.await()

            // Throw our exceptions in case we have any errors
            if (isError == IO_ERROR)
                throw MangaJetException("Connection lost. May be a problem with yours connectivity or timeout")
            else if (isError == BODY_NULL_ERROR || isError == RET_CODE_ERROR)
                throw MangaJetException("Server dismissed our request with return code $retCode")
        }
    }

    // Function to acquire bytes via InputStream
    fun writeBytesStream(url: String, outputStream: OutputStream, headers: Map<String, String> = mapOf()) : Promise {
        // Create new promise
        val myPromise = Promise(outputStream)

        // Make async call
        getAsync(url, myPromise.callback, headers)

        // Return promise
        return myPromise
    }
}
