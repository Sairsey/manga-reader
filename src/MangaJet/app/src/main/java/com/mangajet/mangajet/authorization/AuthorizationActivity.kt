package com.mangajet.mangajet.authorization

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import com.mangajet.mangajet.R
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.mangareader.AuthorizationViewModel
import com.mangajet.mangajet.mangareader.MangaReaderActivity

// Class which represents Authorization Activity
class AuthorizationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authorization_second)

        // Call viewmodel to init all elements
        val authorizationViewmodel = ViewModelProvider(this)[AuthorizationViewModel::class.java]
        authorizationViewmodel.initAuthScreen(intent)

        // start webView
        val webViewElement = findViewById<WebView>(R.id.AuthWebView)
        webViewElement.settings.javaScriptEnabled = true
        webViewElement.loadUrl(authorizationViewmodel.url)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webViewElement, true)

        // add button callback
        val buttonDone = findViewById<Button>(R.id.DoneButton)
        buttonDone.setOnClickListener{
            authorizationViewmodel.library?.setCookies(
                CookieManager.getInstance().getCookie(authorizationViewmodel.url))
            finish()
        }
    }
}
