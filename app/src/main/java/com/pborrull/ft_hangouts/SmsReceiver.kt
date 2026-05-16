package com.pborrull.ft_hangouts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.telephony.SmsMessage
import androidx.core.app.NotificationCompat
import com.pborrull.ft_hangouts.db.ContactRepository
import com.pborrull.ft_hangouts.models.Contact
import com.pborrull.ft_hangouts.models.Message

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {

            val bundle = intent.extras
            val pdus = bundle?.get("pdus") as Array<Any>?

            pdus?.forEach { pdu ->
                val sms = SmsMessage.createFromPdu(pdu as ByteArray)
                val sender = sms.originatingAddress ?: ""
                val body = sms.messageBody ?: ""

                val repo = ContactRepository(context.applicationContext)

                var contact = repo.getContactByPhone(sender)

                if (contact == null) {
                    contact = Contact(
                        id = 0,
                        name = sender,
                        phone = sender,
                        email = "",
                        address = "",
                        notes = context.getString(R.string.auto_created_note)
                    )
                    val newContactId = repo.addContact(contact)
                    contact.id = newContactId

                    val contactIntent = Intent("com.pborrull.ft_hangouts.NEW_CONTACT")
                    androidx.localbroadcastmanager.content.LocalBroadcastManager
                        .getInstance(context).sendBroadcast(contactIntent)
                }

                val message = Message(
                    contactId = contact.id,
                    body = body,
                    timestamp = System.currentTimeMillis(),
                    sentByMe = false
                )
                repo.insertMessage(message)

                val broadcastIntent = Intent("com.pborrull.ft_hangouts.NEW_MESSAGE")
                broadcastIntent.putExtra("message_data", message)

                androidx.localbroadcastmanager.content.LocalBroadcastManager
                    .getInstance(context).sendBroadcast(broadcastIntent)


                showNotification(context, contact.name, body)
            }
            abortBroadcast()
        }
    }

    private fun showNotification(context: Context, contactName: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "sms_channel",
                context.getString(R.string.channel_sms_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "sms_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(contactName)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(contactName.hashCode(), notification)
    }
}
