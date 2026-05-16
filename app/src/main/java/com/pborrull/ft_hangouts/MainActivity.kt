package com.pborrull.ft_hangouts

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pborrull.ft_hangouts.db.ContactRepository
import com.pborrull.ft_hangouts.models.Contact
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*
import androidx.core.content.edit

class MainActivity : AppCompatActivity() {

    private lateinit var repository: ContactRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var addButton: FloatingActionButton
    private var contacts: MutableList<Contact> = mutableListOf()
    private lateinit var adapter: ContactAdapter
    private lateinit var prefs: SharedPreferences
    private lateinit var contactReceiver: BroadcastReceiver

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        prefs = getSharedPreferences("settings", MODE_PRIVATE)

        val language = prefs.getString("app_language", Locale.getDefault().language) ?: "en"
        applyLocale(language)

        val currentThemeId = prefs.getInt("app_theme_id", R.style.Theme_Hangouts_Green)
        setTheme(currentThemeId)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestSmsPermissions()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val btnBlue = findViewById<View>(R.id.btnThemeBlue)
        val btnGreen = findViewById<View>(R.id.btnThemeGreen)

        btnBlue.setOnClickListener { saveAndApplyTheme(R.style.Theme_Hangouts_Blue) }
        btnGreen.setOnClickListener { saveAndApplyTheme(R.style.Theme_Hangouts_Green) }

        recyclerView = findViewById(R.id.recyclerViewContacts)
        recyclerView.layoutManager = LinearLayoutManager(this)

        repository = ContactRepository(this)
        contacts = repository.getAllContacts().toMutableList()

        adapter = ContactAdapter(
            contacts,
            onEditClick = { contact ->
                val intent = Intent(this, ContactDetailActivity::class.java)
                intent.putExtra("contact_id", contact.id)
                startActivity(intent)
            },
            onChatClick = { contact ->
                val intent = Intent(this, ConversationActivity::class.java)
                intent.putExtra("contact_id", contact.id)
                startActivity(intent)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val btnEnglish = findViewById<ImageButton>(R.id.btnEnglish)
        val btnSpanish = findViewById<ImageButton>(R.id.btnSpanish)
        btnEnglish.setOnClickListener { changeLanguage("en") }
        btnSpanish.setOnClickListener { changeLanguage("es") }

        addButton = findViewById(R.id.btnAddContact)
        addButton.setOnClickListener {
            startActivity(Intent(this, AddContactActivity::class.java))
        }

        contactReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "com.pborrull.ft_hangouts.NEW_CONTACT") {
                    reloadContacts()
                }
            }
        }

        val filter = IntentFilter("com.pborrull.ft_hangouts.NEW_CONTACT")
        LocalBroadcastManager.getInstance(this).registerReceiver(contactReceiver, filter)
    }

    override fun onStart() {
        super.onStart()
    }

    private fun reloadContacts() {
        contacts.clear()
        contacts.addAll(repository.getAllContacts())
        adapter.notifyDataSetChanged()
        Toast.makeText(this, "New contact received!", Toast.LENGTH_SHORT).show()
    }

    private fun requestSmsPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add("android.permission.POST_NOTIFICATIONS")
        }

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val allGranted = grantResults.all { it == android.content.pm.PackageManager.PERMISSION_GRANTED }
            if (allGranted) {
                Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Some permissions were denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun changeLanguage(languageCode: String) {
        prefs.edit().putString("app_language", languageCode).apply()
        applyLocale(languageCode)
        recreate()
    }

    private fun applyLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    override fun onResume() {
        super.onResume()
        contacts.clear()
        contacts.addAll(repository.getAllContacts())
        adapter.notifyDataSetChanged()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(contactReceiver)
    }

    private fun saveAndApplyTheme(themeId: Int) {
        prefs.edit().putInt("app_theme_id", themeId).apply()
        recreate()
    }
}