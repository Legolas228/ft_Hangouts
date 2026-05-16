package com.pborrull.ft_hangouts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.pborrull.ft_hangouts.db.ContactRepository
import com.pborrull.ft_hangouts.models.Contact


class AddContactActivity : AppCompatActivity() {
    private lateinit var repository: ContactRepository
    private lateinit var ivProfileImage: ImageView
    private var currentPhotoUri: String? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            currentPhotoUri = it.toString()
            ivProfileImage.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val currentThemeId = prefs.getInt("app_theme_id", R.style.Theme_Hangouts_Green)
        setTheme(currentThemeId)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contact)

        repository = ContactRepository(this)

        val name = findViewById<EditText>(R.id.inputName)
        val phone = findViewById<EditText>(R.id.inputPhone)
        val email = findViewById<EditText>(R.id.inputEmail)
        val address = findViewById<EditText>(R.id.inputAddress)
        val notes = findViewById<EditText>(R.id.inputNotes)

        ivProfileImage = findViewById(R.id.ivProfileImage)

        val btnSelectPhoto = findViewById<Button>(R.id.btnSelectPhoto)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val buttonCancel = findViewById<Button>(R.id.buttonCancel)

        buttonCancel.setOnClickListener {
            finish()
        }

        btnSelectPhoto.setOnClickListener {
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        btnSave.setOnClickListener {

            val nameText = name.text.toString().trim()
            val phoneText = phone.text.toString().trim()

            if (nameText.isEmpty()) {
                name.error = getString(R.string.name_required)
                name.requestFocus()
                return@setOnClickListener
            }

            val phoneRegex = Regex("^\\+?[0-9]{3,15}$")

            if (phoneText.isEmpty()) {
                phone.error = getString(R.string.phone_required)
                phone.requestFocus()
                return@setOnClickListener
            }

            if (!phoneText.matches(phoneRegex)) {
                phone.error = getString(R.string.invalid_phone_number_error)
                phone.requestFocus()
                return@setOnClickListener
            }

            val contact = Contact(
                id = 0L,
                name = nameText,
                phone = phoneText,
                email = email.text.toString(),
                address = address.text.toString(),
                notes = notes.text.toString(),
                photo_uri = currentPhotoUri
            )
            val insertedId = repository.addContact(contact)
            if (insertedId > 0) {
                Toast.makeText(this, getString(R.string.contact_added), Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, getString(R.string.contact_add_error), Toast.LENGTH_SHORT).show()
            }
        }
    }
}