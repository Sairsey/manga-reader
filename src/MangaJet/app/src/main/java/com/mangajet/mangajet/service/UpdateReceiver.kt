package com.mangajet.mangajet.service

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mangajet.mangajet.BuildConfig
import com.mangajet.mangajet.data.StorageManager
import com.mangajet.mangajet.MainActivity
import com.mangajet.mangajet.R
import com.mangajet.mangajet.data.Manga
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.data.MangaPage
import com.mangajet.mangajet.log.Logger
import java.util.Random

// class which will check mangas and draw notifications
class UpdateReceiver : BroadcastReceiver() {
    // Some useful constants
    companion object {
        const val SECOND = 1000 //in ms
        const val MINUTE = 60 * SECOND //in ms
        const val HOUR = 60 * MINUTE //in ms
        const val DAY = 24 * HOUR //in ms
        const val WEEK = 7 * HOUR //in ms
    }

    // our alarmManager which will call us once per time
    private lateinit var am : AlarmManager

    // function for drawing cool notification
    fun showNotification(
        context: Context,
        title : String,
        shortInnerText: String,
        bigInnerText: String = shortInnerText,
        url : String? = null) {
        // Create an explicit intent for an Activity in your app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        var builder = NotificationCompat.Builder(context, context.getString(R.string.channel_id))
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle(title)
            .setShowWhen(true)
            .setContentText(shortInnerText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigInnerText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (url != null)
        {
            var page = MangaPage(url)
            val bitmap = BitmapFactory.decodeFile(page.getFile().absolutePath)
            builder = builder.setLargeIcon(bitmap)
        }

        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            // for cleaning or
            var notificationId = Random().nextInt()
            notify(notificationId, builder.build())
        }
    }

    // function for checking new manga chapters
    fun checkForNewManga(context: Context?) {
        // in dev version we send notification before checking
        if (BuildConfig.VERSION_NAME.endsWith("dev")) {
            showNotification(context!!, "dev-notification",
                "We are starting to check if we have any notification")
        }

        // at first get all our mangas
        var paths = StorageManager.getAllPathsForType(StorageManager.FileType.MangaInfo)
        for (path in paths) {
            // load manga
            var manga : Manga
            try {
                manga = Manga(StorageManager.loadString(path, StorageManager.FileType.MangaInfo))
            }
            catch (ex : MangaJetException) {
                // skip it
                Logger.log(ex.message.toString())
                continue
            }
            // get previous amount
            var prevAmountOfChapters = manga.chapters.size

            // for dev we always show at least last 2
            if (BuildConfig.VERSION_NAME.endsWith("dev"))
                prevAmountOfChapters = manga.chapters.size - 2

            // get current amount
            manga.updateChapters()

            // if differs
            if (manga.chapters.size != prevAmountOfChapters) {
                // build notification string
                var str = manga.originalName + "\n\n"
                var shortStr = str

                for (chapterIndex in prevAmountOfChapters until manga.chapters.size)
                    str += manga.chapters[chapterIndex].fullName + "\n"

                // show notification
                showNotification(context!!, "New chapters", shortStr, str, manga.cover)
                // and save manga, so in history we can see our updated manga at top
                manga.saveToFile()
            }
        }
    }

    // function to set our clock
    fun setAlarm(context: Context) {
        am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val i = Intent(context, UpdateReceiver::class.java)
        val pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT)

        // do update in a minute
        am.set(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() +
                    MINUTE.toLong(),
            pi
        )

        if (BuildConfig.VERSION_NAME.endsWith("dev")) {
            // and once per minute if we in DEV
            am.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() +
                        MINUTE.toLong(),
                MINUTE.toLong(),
                pi
            )
        }
        else {
            // and once per day if we in RELEASE
            am.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() +
                        MINUTE.toLong(),
                DAY.toLong(),
                pi
            )
        }
    }

    // function for stopping alarm
    fun cancelAlarm(context: Context) {
        val intent = Intent(context, UpdateReceiver::class.java)
        val sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE)
        am.cancel(sender)
    }

    @SuppressLint("InvalidWakeLockTag")
    override fun onReceive(context: Context, intent: Intent) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "")
        wl.acquire()
        checkForNewManga(context)
        wl.release()
    }
}
