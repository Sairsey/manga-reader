package com.mangajet.mangajet.log

import android.content.Intent
import com.mangajet.mangajet.data.StorageManager
import java.io.File
import java.io.IOException
import java.io.FileNotFoundException
import java.io.BufferedReader
import java.io.FileReader

// Class for handling all uncaught exceptions
class UncaughtExceptionHandler : Thread.UncaughtExceptionHandler {

    private val defaultUEH : Thread.UncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
    private val stackTraceFileName = "stackTrace.txt"

    init{
        sendToMail()
    }

    // Function to handle exceptions
    override fun uncaughtException(t: Thread, e: Throwable) {
        Logger.log("Uncaught exception was thrown")
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
            val path = StorageManager.storageDirectory + Logger.fileLogPath + "/"
            val myDir = File(path)
            if(!myDir.exists())
                myDir.mkdirs()
            val file = File(myDir, stackTraceFileName)
            if(file.exists())
                file.delete()
            else
                file.createNewFile()
        }
        catch (ex : IOException){
            ex.hashCode()// It is okay if couldn't create
        }

        // Kill the app
        defaultUEH.uncaughtException(t, e)
    }

    // Function to send report to email
    private fun sendToMail(){
        var trace : String = ""
        // Add log and stack trace to mail
        val path = StorageManager.storageDirectory + Logger.fileLogPath + "/"
        try{
            if (!File( path, stackTraceFileName).exists())
                return
            val reader = BufferedReader(FileReader(File(path, Logger.fileLogName)))
            var line = reader.readLine()
            while(line != null){
                trace += line + "\n"
                line = reader.readLine()
            }
        }
        catch (e : IOException){
            e.hashCode()
            return// If couldn't read or open file => let it go
        }
        catch (e : FileNotFoundException){
            e.hashCode()
            return// If couldn't read or open file => let it go
        }
        println(trace)
        val sendIntent = Intent(Intent.ACTION_SEND)
        val subject = "Error report"
        val body = "Mail this to loko201195@gmail.com: \n$trace\n"

        sendIntent.putExtra(Intent.EXTRA_EMAIL,  "readerscope@altcanvas.com");
        sendIntent.putExtra(Intent.EXTRA_TEXT, body);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        sendIntent.setType("message/rfc822");

        val file = File(path,stackTraceFileName)
        if (file.exists())
            file.delete()
    }
}
