package com.mangajet.mangajet

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.data.StorageManager
import com.mangajet.mangajet.databinding.ActivityMainBinding
import com.mangajet.mangajet.log.Logger
import com.mangajet.mangajet.log.UncaughtExceptionHandler
import kotlin.system.exitProcess


// Class which represents Main Activity which user will see then he opens application
class MainActivity : AppCompatActivity(), ActivityResultCallback<Map<String, Boolean>> {

    private lateinit var binding: ActivityMainBinding

    // Permission request
    private val permissionsRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
        this
    )

    // Callback for permission request
    override fun onActivityResult(result: Map<String, Boolean>) {
        StorageManager.readPermission = result[Manifest.permission.READ_EXTERNAL_STORAGE] == true
        StorageManager.writePermission = result[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true

        if (!StorageManager.readPermission || !StorageManager.writePermission)
        {
            val builder = AlertDialog.Builder(this)
            builder
                .setTitle("Error")
                .setMessage("This application cannot work without storage permission." +
                        "Please open your settings and give this application" +
                        "external storage permission")
                .setPositiveButton("Open settings"
                ) { dialog, id ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    val uri: Uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
                .setNegativeButton("Exit"
                ) { dialog, id ->
                    exitProcess(-1)
                }
            val dialog = builder.create()
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
        }
    }

    // Function to check permission.
    private fun checkPermission(permission: String) : Boolean {
        return ContextCompat.checkSelfPermission(this@MainActivity, permission) == PackageManager.PERMISSION_GRANTED
    }

    // Function which will handle updating StorageManager permissions
    private fun handleStoragePermissions() {
        StorageManager.writePermission =
            checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        StorageManager.readPermission =
            checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)

        val permissionsToAsk = ArrayList<String>()
        if (!StorageManager.readPermission)
            permissionsToAsk.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (!StorageManager.writePermission)
            permissionsToAsk.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permissionsToAsk.size != 0) {
            permissionsRequest.launch(permissionsToAsk.toTypedArray())
        }
    }

    override fun onRestart() {
        super.onRestart()
        // you can re-run this function as many times as you want
        // It will show message-box only if permission is not granted
        handleStoragePermissions()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        // you can re-run this function as many times as you want
        // It will show message-box only if permission is not granted
        handleStoragePermissions()

        // Set logger and UEH
        Thread.setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler())
        checkForCrash(UncaughtExceptionHandler().getCrashReport())
        Librarian.hashCode()
        Logger.log("Logger initialized")

        // on start it is good idea to load all cookies and Authentication from Librarian
        try {
            Librarian.setLibrariesJSON(
                StorageManager.loadString(Librarian.path, StorageManager.FileType.LibraryInfo))
        }
        catch (ex: MangaJetException) {
            Logger.log("Could not set libraries json: " + ex.message, Logger.Lvl.WARNING)
            // in this case we can just skip, because if file not found it isnt a big deal.
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.mainToolbar)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_history,
                R.id.navigation_favourite,
                R.id.navigation_search,
                R.id.navigation_for_you,
                R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    // Functions that checks report for crash => sends it to email
    private fun checkForCrash(report : String){

        if(report.isEmpty())
            return
        val email = "mangajetmailbot@gmail.com"

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Ooopsie..")
            .setMessage("App crashed. Can you send info about it via email?")
            .setPositiveButton("Send") {
                    dialog, id ->
                val sendIntent = Intent(Intent.ACTION_SEND)
                val subject = "Error report"
                sendIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                sendIntent.putExtra(Intent.EXTRA_TEXT, report)
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
                sendIntent.type = "message/rfc822"
                this.startActivity(Intent.createChooser(sendIntent, "Title:"))
            }
            .setNegativeButton("Cancel") {
                    dialog, _ ->
                dialog.cancel()
            }
        val alert = builder.create()
        alert.show()
        alert.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        
    }
}
