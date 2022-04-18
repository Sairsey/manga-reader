package com.mangajet.mangajet.log

import com.mangajet.mangajet.data.StorageManager
import java.io.File
import java.io.IOException
import java.util.*
import java.util.logging.FileHandler
import java.util.logging.Level
import java.util.logging.Logger
import java.util.logging.SimpleFormatter

// Our logger implementation
object Logger {
    //Create logger
    private var l = Logger.getLogger(javaClass.toString())
    const val fileLogPath = "/LogFiles"
    const val fileLogName = "log.txt"

    // Enum class that level of logging data
    enum class Lvl(){
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
            val path = StorageManager.storageDirectory + fileLogPath + "/"
            val myDir = File(path)
            if(!myDir.exists())
                myDir.mkdirs()
            val file = File(myDir, fileLogName)
            if(file.exists())
                file.delete()
            else
                file.createNewFile()
            var fh = FileHandler(path + fileLogName)
            l.addHandler(fh)
            fh.formatter = SimpleFormatter()
            l.useParentHandlers = false
        }
        catch (e : SecurityException){
            e.hashCode()// Could not open file, so log to console at least
        }
        catch (e : IOException){
            e.hashCode()// Could not open file, so log to console at least
        }
    }

    // Function to add info to the log
    fun log(msg : String, level : Lvl = Lvl.INFO){
        l.log(level.get(), msg)
    }

}
