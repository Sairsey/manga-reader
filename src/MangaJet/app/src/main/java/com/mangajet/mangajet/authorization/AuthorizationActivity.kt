package com.mangajet.mangajet.authorization

import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.mangajet.mangajet.R
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.data.StorageManager
import com.mangajet.mangajet.mangareader.AuthorizationViewModel
import kotlin.system.exitProcess


// Class which represents Authorization Activity
class AuthorizationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.authorization_activity)

        setTitle(R.string.title_authorization)

        // Call viewmodel to init all elements
        val authorizationViewmodel = ViewModelProvider(this)[AuthorizationViewModel::class.java]
        authorizationViewmodel.initAuthScreen(intent)

        // start webView
        val webViewElement = findViewById<WebView>(R.id.AuthWebView)
        webViewElement.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return false
            }
        })
        webViewElement.settings.javaScriptEnabled = true
        webViewElement.loadUrl(authorizationViewmodel.url)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webViewElement, true)

        // add button callback
        val buttonDone = findViewById<Button>(R.id.DoneButton)
        buttonDone.setOnClickListener{
            authorizationViewmodel.library?.setCookies(
                CookieManager.getInstance().getCookie(authorizationViewmodel.url))
            try {
                // after we changed Librarian we must update JSON with new data
                StorageManager.saveString(
                    Librarian.path,
                    Librarian.getLibrariesJSON(),
                    StorageManager.FileType.LibraryInfo
                )
                finish()
            }
            catch (ex: MangaJetException) {
                var builder = AlertDialog.Builder(this)
                builder.setTitle("ERROR")
                builder.setMessage(ex.message)
                builder.setNeutralButton("ok") { dialog, which ->
                    exitProcess(-1)
                }
                builder.show()
            }
        }
    }
}
