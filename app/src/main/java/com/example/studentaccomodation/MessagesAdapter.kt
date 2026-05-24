package com.example.studentaccomodation

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studentaccomodation.R
import com.example.studentaccomodation.Message
import java.text.SimpleDateFormat
import java.util.*

class MessagesAdapter(private val currentUid: String) :
    ListAdapter<Message, RecyclerView.ViewHolder>(Diff()) {

    companion object {
        private const val SENT     = 1
        private const val RECEIVED = 2
    }

    override fun getItemViewType(position: Int) =
        if (getItem(position).senderId == currentUid) SENT else RECEIVED

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return if (viewType == SENT)
            SentVH(inf.inflate(R.layout.item_message_sent, parent, false))
        else
            ReceivedVH(inf.inflate(R.layout.item_message_received, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg  = getItem(position)
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp))
        when (holder) {
            is SentVH     -> holder.bind(msg, time)
            is ReceivedVH -> holder.bind(msg, time)
        }
    }

    class SentVH(v: android.view.View) : RecyclerView.ViewHolder(v) {
        private val tvText: TextView = v.findViewById(R.id.tvMessageText)
        private val tvTime: TextView = v.findViewById(R.id.tvTimestamp)
        fun bind(m: Message, time: String) { tvText.text = m.content; tvTime.text = time }
    }

    class ReceivedVH(v: android.view.View) : RecyclerView.ViewHolder(v) {
        private val tvText:   TextView = v.findViewById(R.id.tvMessageText)
        private val tvTime:   TextView = v.findViewById(R.id.tvTimestamp)
        private val tvSender: TextView = v.findViewById(R.id.tvSenderName)
        fun bind(m: Message, time: String) { tvText.text = m.content; tvTime.text = time; tvSender.text = m.senderName }
    }

    class Diff : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(a: Message, b: Message)    = a.id == b.id
        override fun areContentsTheSame(a: Message, b: Message) = a == b
    }
}
