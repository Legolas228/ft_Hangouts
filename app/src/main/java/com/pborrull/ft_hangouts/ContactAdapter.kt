package com.pborrull.ft_hangouts

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pborrull.ft_hangouts.models.Contact

class ContactAdapter(
    private val contacts: List<Contact>,
    private val onEditClick: (Contact) -> Unit,
    private val onChatClick: (Contact) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val ivPhoto: ImageView = view.findViewById(R.id.ivContactPhoto)
        val textName: TextView = view.findViewById(R.id.textName)
        val textPhone: TextView = view.findViewById(R.id.textPhone)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnChat: ImageButton = view.findViewById(R.id.btnChat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]

        var imageSet = false
        if (!contact.photo_uri.isNullOrEmpty()) {
            try {
                holder.ivPhoto.setImageURI(Uri.parse(contact.photo_uri))
                imageSet = true
            } catch (e: Exception) {
            }
        }

        if (!imageSet) {
            holder.ivPhoto.setImageResource(R.mipmap.ic_channel)
        }

        holder.textName.text = contact.name
        holder.textPhone.text = contact.phone

        holder.btnEdit.setOnClickListener { onEditClick(contact) }
        holder.btnChat.setOnClickListener { onChatClick(contact) }

        holder.itemView.setOnClickListener { onEditClick(contact) }
    }

    override fun getItemCount(): Int = contacts.size
}
