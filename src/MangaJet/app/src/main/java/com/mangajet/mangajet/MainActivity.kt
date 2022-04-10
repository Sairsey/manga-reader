package com.mangajet.mangajet

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.data.StorageManager
import com.mangajet.mangajet.databinding.ActivityMainBinding

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

        if (permissionsToAsk.size != 0)
            permissionsRequest.launch(permissionsToAsk.toTypedArray())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // you can re-run this function as many times as you want
        // It will show message-box only if permission is not granted
        handleStoragePermissions()

        // on start it is good idea to load all cookies and Authentication from Librarian
        try {
            Librarian.setLibrariesJSON(
                StorageManager.loadString(Librarian.path, StorageManager.FileType.LibraryInfo))
        }
        catch (ex: MangaJetException) {
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
}