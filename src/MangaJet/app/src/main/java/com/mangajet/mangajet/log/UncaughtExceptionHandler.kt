package com.mangajet.mangajet.log

import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.data.StorageManager
import java.io.IOException
import java.io.BufferedReader
import java.io.FileReader

// Class for handling all uncaught exceptions
class UncaughtExceptionHandler : Thread.UncaughtExceptionHandler {

    private val defaultUEH : Thread.UncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
    private val stackTraceFileName = "stackTrace.txt"

    // Function to handle exceptions
    override fun uncaughtException(t: Thread, e: Throwable) {
        Logger.log("Uncaught exception was thrown", Logger.Lvl.SEVERE)
        var report = e.toString() + "\n\n"
        report += "------------------ Stack trace ------------------\n\n"
        for (elem in e.stackTrace)
            report += "    $elem\n"
        report += "-------------------------------------------------\n\n"
        // Sometimes exception can be found in cause
        val cause = e.cause
        if(cause != null){
            report += "------------------ Cause ------------------\n\n"
            report += cause.toString() + "\n\n"
            for(elem in cause.stackTrace)
                report += "    $elem\n"
            report += "-------------------------------------------------\n\n"
        }
        Logger.log(report)

        // Create flag app
        try {
            if(!StorageManager.isExist(stackTraceFileName, StorageManager.FileType.LibraryInfo))
                StorageManager.saveString(stackTraceFileName, "", StorageManager.FileType.LibraryInfo)
        }
        catch (ex : MangaJetException){
            // It is okay if couldn't create
        }

        // Kill the app
        defaultUEH.uncaughtException(t, e)
    }

    // Function to send get crash report as String
    // If no crash => return empty String
    fun getCrashReport() : String {
        var trace : String = ""
        // Add log and stack trace to mail
        try{
            if (!StorageManager.isExist(stackTraceFileName, StorageManager.FileType.LibraryInfo))
                return ""
            else
                StorageManager.getFile(stackTraceFileName, StorageManager.FileType.LibraryInfo).delete()
            var file = StorageManager.getFile(Logger.fileLogName, StorageManager.FileType.LibraryInfo)
            val reader = BufferedReader(FileReader(file))
            var line = reader.readLine()
            while(line != null){
                trace += line + "\n"
                line = reader.readLine()
            }
        }
        catch (e : IOException){
            e.hashCode()
            // If couldn't read or open file => let it go
        }
        catch (e : MangaJetException){
            // If couldn't read or open file => let it go
        }

        return trace
    }
}
