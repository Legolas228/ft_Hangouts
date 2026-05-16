package com.pborrull.ft_hangouts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pborrull.ft_hangouts.models.Message

class MessageReceiver(private val onMessageReceived: (Message) -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val message = intent?.getParcelableExtra<Message>("message")
        if (message != null) {
            onMessageReceived(message)
        }
    }
}