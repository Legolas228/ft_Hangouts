package com.pborrull.ft_hangouts

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import java.text.SimpleDateFormat
import java.util.*
import android.os.Handler
import android.os.Looper

class MyApplication : Application(), DefaultLifecycleObserver {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val backgroundDelayMs = 700L
    private val saveBackgroundRunnable = Runnable { saveBackgroundTime() }

    override fun onCreate() {
        super<Application>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        mainHandler.removeCallbacks(saveBackgroundRunnable)
        checkBackgroundTime()
    }

    override fun onStop(owner: LifecycleOwner) {
        mainHandler.postDelayed(saveBackgroundRunnable, backgroundDelayMs)
    }

    private fun saveBackgroundTime() {
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit().putLong("last_background_time", System.currentTimeMillis()).apply()
    }

    private fun checkBackgroundTime() {
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val backgroundTime = prefs.getLong("last_background_time", 0L)

        if (backgroundTime != 0L) {
            val date = Date(backgroundTime)
            val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val formattedTime = formatter.format(date)

            Toast.makeText(this, "App backgrounded at: $formattedTime", Toast.LENGTH_LONG).show()

            prefs.edit().remove("last_background_time").apply()
        }
    }
}