package com.mangajet.mangajet.data

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Callback
import okhttp3.Call
import okio.IOException
import java.io.InputStream
import java.util.concurrent.CountDownLatch

// Singleton which will work with okHTTP to provide access to web resources
object WebAccessor {
    private val client = OkHttpClient()

    // Function to aquire things asyncroniously
    fun getAsync(url: String, callback: Callback,
                     headers: Map<String, String> = mapOf()) : Call {

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
        call = client.newCall(request)

        // Set callback
        call.enqueue(callback)

        return call
    }

    // Function to aquire text syncroniously
    // Note: Please use it if text are less then 1Mb
    fun getTextSync(url: String, headers: Map<String, String> = mapOf()) : String {
        // We have here another android crazy stuff
        // Problem is that Android blocks sockets from main thread
        // So to get text syncroniously we need to
        // do asyncronious request and wait for it.

        // Default value
        var str = ""

        // Atomic counter
        val countDownLatch = CountDownLatch(1)

        var isError = 0
        var retCode = 0

        // Simple Callback which will return string
        val callback = object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                countDownLatch.countDown()
                isError = 1
                println(e.message) // for debugging purposes
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        try {
                            str = response.body!!.string()
                        }
                        catch (e: NullPointerException) {
                            isError = 2
                            retCode = 404
                        }
                    }
                    else {
                        isError = 3
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
        if (isError == 1)
            throw MangaJetException("Connection lost. May be a problem with yours connectivity or timeout")
        else if (isError == 2 || isError == 3)
            throw MangaJetException("Server dismissed our request with return code $retCode")

        return str
    }

    // Function to aquire bytes via InputStream
    fun getBytesStream(url: String, headers: Map<String, String> = mapOf()) : InputStream {

        // We have here another android crazy stuff
        // Problem is that Android blocks sockets from main thread
        // So to get bytes syncroniously we need to
        // do asyncronious request and wait for it.

        // Default value
        var stream : InputStream = "".byteInputStream()

        // Atomic counter
        val countDownLatch = CountDownLatch(1)

        var isError = 0
        var retCode = 0


        // Simple Callback which will return string
        val callback = object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                countDownLatch.countDown()
                isError = 1
                println(e.message) // for debugging purposes
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        try {
                            stream = response.body!!.byteStream()
                        }
                        catch (e: NullPointerException) {
                            isError = 2
                            retCode = 404
                        }
                    }
                    else {
                        isError = 3
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
        if (isError == 1)
            throw MangaJetException("Connection lost. May be a problem with yours connectivity or timeout")
        else if (isError == 2 || isError == 3)
            throw MangaJetException("Server dismissed our request with return code $retCode")

        return stream
    }
}
