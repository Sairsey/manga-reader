package com.mangajet.mangajet.authorization

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar
import com.mangajet.mangajet.R
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.data.StorageManager
import com.mangajet.mangajet.mangareader.AuthorizationViewModel
import com.mangajet.mangajet.log.Logger
import kotlin.system.exitProcess


// Class which represents Authorization Activity
class AuthorizationActivity : AppCompatActivity() {

    // this function will create blocking dialog with instruction
    fun showInstruction() {
        val builder = AlertDialog.Builder(this)
        builder
            .setTitle("Instruction")
            .setMessage("To authenticate follow these simple steps:\n" +
                    "1)Close this dialog by pressing option you like\n" +
                    "2)In opened in-app browser authenticate like you do in on website.\n" +
                    "3)After authentication finished press big \"Done\" button at the bottom\n")
            .setPositiveButton("Got it!") { dialog, id ->
            }
            .setNegativeButton("Do not show it again"
            ) { dialog, id ->
                val prefs = getSharedPreferences("auth_pref", MODE_PRIVATE).edit()
                prefs.putBoolean("dontshow", true)
                prefs.commit()
            }
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.authorization_activity)

        setSupportActionBar(findViewById<MaterialToolbar>(R.id.authorizationWVToolbar))
        setTitle(R.string.title_authorization)

        val prefs = getSharedPreferences("auth_pref", MODE_PRIVATE)
        val show = prefs.getBoolean("dontshow", false)
        if (show == false)
        {
            showInstruction()
        }

        // Call viewmodel to init all elements
        val authorizationViewmodel = ViewModelProvider(this)[AuthorizationViewModel::class.java]
        authorizationViewmodel.initAuthScreen(intent)

        // start webView
        val webViewElement = findViewById<WebView>(R.id.AuthWebView)
        webViewElement.setWebViewClient(object : WebViewClient() {
            @TargetApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                view.loadUrl(request.url.toString())
                return true
            }

            // For old devices
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

        })
        webViewElement.settings.javaScriptEnabled = true
        webViewElement.settings.javaScriptCanOpenWindowsAutomatically = true
        webViewElement.settings.domStorageEnabled = true
        webViewElement.settings.userAgentString =
            "Mozilla/5.0 (Linux; Android 8.0; Pixel 2 Build/OPD3.170816.012) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/93.0.4577.82 Mobile Safari/537.36"
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
                Logger.log("Catch MJE while trying to update json with library data: " + ex.message, Logger.Lvl.WARNING)
                var builder = AlertDialog.Builder(this)
                builder.setTitle("ERROR")
                builder.setMessage(ex.message)
                builder.setNeutralButton("ok") { dialog, which ->
                    exitProcess(-1)
                }
                builder.setCancelable(false)
                builder.show()
            }
        }
    }
}
