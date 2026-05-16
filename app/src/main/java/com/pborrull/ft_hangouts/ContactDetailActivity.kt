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

class ContactDetailActivity : AppCompatActivity() {

    private lateinit var repository: ContactRepository
    private var contactId: Long = 0
    private lateinit var contact: Contact
    private lateinit var ivProfileImage: ImageView
    private var currentPhotoUri: String? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                currentPhotoUri = it.toString()
                ivProfileImage.setImageURI(it)
            }
        }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("unsaved_photo_uri", currentPhotoUri)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val unsavedUri = savedInstanceState.getString("unsaved_photo_uri")
        if (unsavedUri != null) {
            currentPhotoUri = unsavedUri
            try {
                ivProfileImage.setImageURI(Uri.parse(currentPhotoUri))
            } catch (e: Exception) {
                ivProfileImage.setImageResource(R.mipmap.ic_channel)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val currentThemeId = prefs.getInt("app_theme_id", R.style.Theme_Hangouts_Green)
        setTheme(currentThemeId)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_detail)

        repository = ContactRepository(this)
        contactId = intent.getLongExtra("contact_id", 0)

        if (contactId == 0L) {
            Toast.makeText(this, R.string.error_contact_id_missing, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        contact = repository.getContact(contactId) ?: run {
            Toast.makeText(this, getString(R.string.error_contact_not_found), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        ivProfileImage = findViewById(R.id.ivProfileImage)
        val btnChangePhoto = findViewById<Button>(R.id.btnChangePhoto)
        val editName = findViewById<EditText>(R.id.editName)
        val editPhone = findViewById<EditText>(R.id.editPhone)
        val editEmail = findViewById<EditText>(R.id.editEmail)
        val editAddress = findViewById<EditText>(R.id.editAddress)
        val editNote = findViewById<EditText>(R.id.editNote)

        val buttonSave = findViewById<Button>(R.id.buttonSave)
        val buttonCancel = findViewById<Button>(R.id.buttonCancel)
        val buttonDelete = findViewById<Button>(R.id.buttonDelete)

        editName.setText(contact.name)
        editPhone.setText(contact.phone)
        editEmail.setText(contact.email)
        editAddress.setText(contact.address)
        editNote.setText(contact.notes)

        currentPhotoUri = contact.photo_uri
        if (!currentPhotoUri.isNullOrEmpty()) {
            try {
                ivProfileImage.setImageURI(Uri.parse(currentPhotoUri))
            } catch (e: Exception) {
                ivProfileImage.setImageResource(R.mipmap.ic_channel)
            }
        } else {
            ivProfileImage.setImageResource(R.mipmap.ic_channel)
        }

        btnChangePhoto.setOnClickListener {
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        buttonCancel.setOnClickListener {
            finish()
        }

        buttonDelete.setOnClickListener {
            val rowsDeleted = repository.deleteContact(contactId)
            if (rowsDeleted > 0) {
                Toast.makeText(this, getString(R.string.contact_deleted), Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, getString(R.string.contact_delete_error), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        buttonSave.setOnClickListener {
            val nameText = editName.text.toString().trim()
            val phoneText = editPhone.text.toString().trim()

            if (nameText.isEmpty()) {
                editName.error = getString(R.string.name_required)
                editName.requestFocus()
                return@setOnClickListener
            }

            val phoneRegex = Regex("^\\+?[0-9]{3,15}$")

            if (phoneText.isEmpty()) {
                editPhone.error = getString(R.string.phone_required)
                editPhone.requestFocus()
                return@setOnClickListener
            }

            if (!phoneText.matches(phoneRegex)) {
                editPhone.error = getString(R.string.invalid_phone_number_error)
                editPhone.requestFocus()
                return@setOnClickListener
            }

            val updatedContact = Contact(
                id = contactId,
                name = nameText,
                phone = phoneText,
                email = editEmail.text.toString(),
                address = editAddress.text.toString(),
                notes = editNote.text.toString(),
                photo_uri = currentPhotoUri
            )

            val rowsUpdated = repository.updateContact(updatedContact)

            if (rowsUpdated > 0) {
                Toast.makeText(this, getString(R.string.contact_updated), Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, getString(R.string.error_updating_contact), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}