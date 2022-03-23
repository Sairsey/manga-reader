package com.mangajet.mangajet.ui.settings.options

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.mangajet.mangajet.R

class AboutAppWebView : AppCompatActivity() {
    private lateinit var webView: WebView

    private class MyWebViewClient : WebViewClient() {
        @TargetApi(Build.VERSION_CODES.N)
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            view.loadUrl(request.url.toString())
            return true
        }

        // Для старых устройств
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_app_web_view)

        val urlAddress = intent.getStringExtra("URL").toString()
        webView = findViewById(R.id.abaoutAppWebView)
        webView.webViewClient = MyWebViewClient()
        // включаем поддержку JavaScript
        webView.getSettings().setJavaScriptEnabled(true)
        // указываем страницу загрузки
        webView.loadUrl(urlAddress)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
