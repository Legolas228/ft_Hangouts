package com.pborrull.ft_hangouts.db

import android.content.ContentValues
import android.content.Context
import com.pborrull.ft_hangouts.models.Contact
import com.pborrull.ft_hangouts.models.Message

class ContactRepository(context: Context) {

    private val dbHelper: ContactDatabaseHelper = ContactDatabaseHelper(context)

    fun addContact(contact: Contact): Long {
        val values = ContentValues().apply {
            put("name", contact.name)
            put("phone", contact.phone)
            put("email", contact.email)
            put("address", contact.address)
            put("note", contact.notes)
            put("photo_uri", contact.photo_uri)
        }
        return dbHelper.writableDatabase.insert("contacts", null, values)
    }

    fun getContact(id: Long): Contact? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "contacts",
            null,
            "id=?",
            arrayOf(id.toString()),
            null, null, null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                Contact(
                    id = it.getLong(it.getColumnIndexOrThrow("id")),
                    name = it.getString(it.getColumnIndexOrThrow("name")),
                    phone = it.getString(it.getColumnIndexOrThrow("phone")),
                    email = it.getString(it.getColumnIndexOrThrow("email")),
                    address = it.getString(it.getColumnIndexOrThrow("address")),
                    notes = it.getString(it.getColumnIndexOrThrow("note")),
                    photo_uri = it.getString(it.getColumnIndexOrThrow("photo_uri"))
                )
            } else {
                null
            }
        }
    }

    fun getAllContacts(): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val db = dbHelper.readableDatabase
        val cursor = db.query("contacts", null, null, null, null, null, "name ASC")

        cursor.use {
            while (it.moveToNext()) {
                val contact = Contact(
                    id = it.getLong(it.getColumnIndexOrThrow("id")),
                    name = it.getString(it.getColumnIndexOrThrow("name")),
                    phone = it.getString(it.getColumnIndexOrThrow("phone")),
                    email = it.getString(it.getColumnIndexOrThrow("email")),
                    address = it.getString(it.getColumnIndexOrThrow("address")),
                    notes = it.getString(it.getColumnIndexOrThrow("note")),
                    photo_uri = it.getString(it.getColumnIndexOrThrow("photo_uri"))
                )
                contacts.add(contact)
            }
        }
        return contacts
    }

    fun updateContact(contact: Contact): Int {
        val values = ContentValues().apply {
            put("name", contact.name)
            put("phone", contact.phone)
            put("email", contact.email)
            put("address", contact.address)
            put("note", contact.notes)
            put("photo_uri", contact.photo_uri)
        }
        return dbHelper.writableDatabase.update("contacts", values, "id=?", arrayOf(contact.id.toString()))
    }

    fun deleteContact(id: Long): Int {
        return dbHelper.writableDatabase.delete("contacts", "id=?", arrayOf(id.toString()))
    }

    fun insertMessage(message: Message): Long {
        val values = ContentValues().apply {
            put("contact_id", message.contactId)
            put("body", message.body)
            put("timestamp", message.timestamp)
            put("is_sent", if (message.sentByMe) 1 else 0)
        }
        return dbHelper.writableDatabase.insert("messages", null, values)
    }

    fun getMessagesForContact(contactId: Long): List<Message> {
        val messages = mutableListOf<Message>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "messages",
            null,
            "contact_id = ?",
            arrayOf(contactId.toString()),
            null,
            null,
            "timestamp ASC"
        )

        cursor.use {
            while (it.moveToNext()) {
                messages.add(Message(
                    id = it.getLong(it.getColumnIndexOrThrow("id")),
                    contactId = it.getLong(it.getColumnIndexOrThrow("contact_id")),
                    body = it.getString(it.getColumnIndexOrThrow("body")),
                    timestamp = it.getLong(it.getColumnIndexOrThrow("timestamp")),
                    sentByMe = it.getInt(it.getColumnIndexOrThrow("is_sent")) == 1
                ))
            }
        }
        return messages
    }

    fun getContactByPhone(phone: String): Contact? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "contacts",
            null,
            "phone = ?",
            arrayOf(phone),
            null, null, null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                Contact(
                    id = it.getLong(it.getColumnIndexOrThrow("id")),
                    name = it.getString(it.getColumnIndexOrThrow("name")),
                    phone = it.getString(it.getColumnIndexOrThrow("phone")),
                    email = it.getString(it.getColumnIndexOrThrow("email")),
                    address = it.getString(it.getColumnIndexOrThrow("address")),
                    notes = it.getString(it.getColumnIndexOrThrow("note")),
                    photo_uri = it.getString(it.getColumnIndexOrThrow("photo_uri"))
                )
            } else {
                null
            }
        }
    }
}