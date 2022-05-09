package com.mangajet.mangajet.log

import android.util.Log
import com.mangajet.mangajet.BuildConfig
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.data.StorageManager
import java.io.IOException
import java.util.logging.FileHandler
import java.util.logging.Level
import java.util.logging.Logger
import java.util.logging.SimpleFormatter

// Our logger implementation
object Logger {
    //Create logger
    private var l = Logger.getLogger(javaClass.toString())

    // Enum class that level of logging data
    enum class Lvl{
        INFO(){
            override fun get() : Level = Level.INFO
              },
        WARNING(){
            override fun get() : Level = Level.WARNING
        },
        SEVERE(){
            override fun get() : Level = Level.SEVERE
        };

        abstract fun get() : Level

    }

    init{
        try {

            if(StorageManager.isExist(Librarian.settings.LOG_FILE_NAME, StorageManager.FileType.LibraryInfo))
               StorageManager.getFile(Librarian.settings.LOG_FILE_NAME, StorageManager.FileType.LibraryInfo).delete()
            StorageManager.saveString(Librarian.settings.LOG_FILE_NAME, "", StorageManager.FileType.LibraryInfo)
            val file = StorageManager.getFile(Librarian.settings.LOG_FILE_NAME, StorageManager.FileType.LibraryInfo)

            var fh = FileHandler(file.absolutePath)
            l.addHandler(fh)
            fh.formatter = SimpleFormatter()
            l.useParentHandlers = false
        }
        catch (e : MangaJetException){
            log("Could connect file to logger: " + e.message, Lvl.WARNING)
            e.hashCode()// Could not open file, so log to console at least
        }
        catch (e : SecurityException){
            log("Could connect file to logger: " + e.message, Lvl.WARNING)
            e.hashCode()// Could not open file, so log to console at least
        }
        catch (e : IOException){
            log("Could connect file to logger: " + e.message, Lvl.WARNING)
            e.hashCode()// Could not open file, so log to console at least
        }
        // Info about project version
        log(BuildConfig.BUILD_TYPE + " version: "+ BuildConfig.VERSION_NAME)
    }

    // Function to add info to the log
    fun log(msg : String, level : Lvl = Lvl.INFO){
        l.log(level.get(), msg)
        Log.d("MJA", msg)
    }

}
