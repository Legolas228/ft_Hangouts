@file:Suppress("DEPRECATION")

package com.pborrull.ft_hangouts

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pborrull.ft_hangouts.db.ContactRepository
import com.pborrull.ft_hangouts.models.Message

class ConversationActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var editMessage: EditText
    private lateinit var buttonSend: ImageButton
    private lateinit var buttonCall: ImageButton
    private lateinit var repository: ContactRepository
    private lateinit var messages: MutableList<Message>
    private lateinit var adapter: MessageAdapter
    private var contactId: Long = 0
    private lateinit var contactName: String
    private var contactPhone: String = ""
    private val REQUEST_CALL_PERMISSION = 1

    private var messageReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val currentThemeId = prefs.getInt("app_theme_id", R.style.Theme_Hangouts_Green)
        setTheme(currentThemeId)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        repository = ContactRepository(this)
        contactId = intent.getLongExtra("contact_id", 0)

        if (contactId == 0L) {
            finish()
            return
        }

        val contact = repository.getContact(contactId)
        if (contact == null) {
            finish()
            return
        }
        contactName = contact.name
        contactPhone = contact.phone

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val toolbarTitle = findViewById<TextView>(R.id.toolbarTitle)
        toolbarTitle.text = contactName

        buttonCall = findViewById(R.id.buttonCall)
        buttonCall.setOnClickListener {
            makePhoneCall()
        }

        recyclerView = findViewById(R.id.recyclerViewMessages)
        recyclerView.layoutManager = LinearLayoutManager(this)

        editMessage = findViewById(R.id.editMessage)
        buttonSend = findViewById(R.id.buttonSend)

        loadInitialMessages()

        buttonSend.setOnClickListener {
            val text = editMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                val message = Message(
                    contactId = contactId,
                    body = text,
                    timestamp = System.currentTimeMillis(),
                    sentByMe = true
                )
                repository.insertMessage(message)
                messages.add(message)
                adapter.notifyItemInserted(messages.size - 1)
                recyclerView.scrollToPosition(messages.size - 1)
                editMessage.text.clear()

                sendSMS(contactPhone, text)
            }
        }
    }

    private fun sendSMS(phoneNumber: String, message: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), REQUEST_CALL_PERMISSION)
            return
        }

        try {
            val smsManager = SmsManager.getDefault()
            val sentIntent = PendingIntent.getBroadcast(this, 0, Intent("SMS_SENT"), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val deliveredIntent = PendingIntent.getBroadcast(this, 0, Intent("SMS_DELIVERED"), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            smsManager.sendTextMessage(phoneNumber, null, message, sentIntent, deliveredIntent)
            Toast.makeText(this, "SMS sent", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error sending SMS: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun makePhoneCall() {
        if (contactPhone.isNotEmpty()) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PERMISSION)
            } else {
                try {
                    val cleanNumber = contactPhone.replace(Regex("[^0-9+]"), "")

                    val intent = Intent(Intent.ACTION_CALL)


                    intent.data = Uri.parse("tel:$cleanNumber")
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, getString(R.string.error_call), Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, getString(R.string.no_phone_number), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall()
            } else {
                Toast.makeText(this, getString(R.string.permissions_denied), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadInitialMessages() {
        messages = repository.getMessagesForContact(contactId).toMutableList()
        adapter = MessageAdapter(messages)
        recyclerView.adapter = adapter
        if (messages.isNotEmpty()) {
            recyclerView.scrollToPosition(messages.size - 1)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        messages.clear()
        messages.addAll(repository.getMessagesForContact(contactId))
        adapter.notifyDataSetChanged()
        if (messages.isNotEmpty()) {
            recyclerView.scrollToPosition(messages.size - 1)
        }

        messageReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.getParcelableExtra<Message>("message_data")?.let { message ->
                    if (message.contactId == contactId) {
                        messages.add(message)
                        adapter.notifyItemInserted(messages.size - 1)
                        recyclerView.scrollToPosition(messages.size - 1)
                    }
                }
            }
        }

        val filter = IntentFilter("com.pborrull.ft_hangouts.NEW_MESSAGE")
        messageReceiver?.let {
            LocalBroadcastManager.getInstance(this).registerReceiver(it, filter)
        }
    }

    override fun onPause() {
        super.onPause()
        messageReceiver?.let {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(it)
        }
        messageReceiver = null
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}