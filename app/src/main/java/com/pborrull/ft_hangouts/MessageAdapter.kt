package com.pborrull.ft_hangouts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pborrull.ft_hangouts.models.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(private val messages: List<Message>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val layoutReceived: LinearLayout = view.findViewById(R.id.layoutReceived)
        private val layoutSent: LinearLayout = view.findViewById(R.id.layoutSent)
        private val messageReceived: TextView = view.findViewById(R.id.messageReceived)
        private val messageSent: TextView = view.findViewById(R.id.messageSent)
        private val timeReceived: TextView = view.findViewById(R.id.timeReceived)
        private val timeSent: TextView = view.findViewById(R.id.timeSent)

        private val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        fun bind(message: Message) {
            val timestamp = sdf.format(Date(message.timestamp))

            if (message.sentByMe) {
                layoutSent.visibility = View.VISIBLE
                layoutReceived.visibility = View.GONE
                messageSent.text = message.body
                timeSent.text = timestamp

            } else {
                layoutReceived.visibility = View.VISIBLE
                layoutSent.visibility = View.GONE
                messageReceived.text = message.body
                timeReceived.text = timestamp
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount() = messages.size
}
