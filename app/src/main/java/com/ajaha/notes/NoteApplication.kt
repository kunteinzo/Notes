package com.ajaha.notes

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Process
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import java.lang.Thread.UncaughtExceptionHandler
import java.lang.Thread.getDefaultUncaughtExceptionHandler

/**
 * From Sketchware
 * Credit to Sketchware*/
class NoteApplication : Application() {
    private lateinit var mContext: Context
    private lateinit var uncaughtExceptionHandler: UncaughtExceptionHandler

    override fun onCreate() {
        super.onCreate()

        mContext = applicationContext
        uncaughtExceptionHandler = getDefaultUncaughtExceptionHandler() as UncaughtExceptionHandler

        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            val intent = Intent(applicationContext, ErrorActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("error", Log.getStackTraceString(e))

            val pendingIntent = PendingIntent.getActivity(
                applicationContext, 1111, intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )

            val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, pendingIntent)

            Process.killProcess(Process.myPid())
            System.exit(1)

            uncaughtExceptionHandler.uncaughtException(t, e)
        }

        when (PreferenceManager.getDefaultSharedPreferences(applicationContext)
            .getString("app_theme", "light")) {
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }
}