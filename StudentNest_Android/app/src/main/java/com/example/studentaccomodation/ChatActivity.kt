package com.example.studentaccomodation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentaccomodation.Message
import com.example.studentaccomodation.SessionManager
import com.example.studentaccomodation.ChatViewModel
import com.example.studentaccomodation.ViewModelFactory
import com.example.studentaccomodation.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {

    private lateinit var b: ActivityChatBinding
    private val vm: ChatViewModel by viewModels { ViewModelFactory() }
    private lateinit var adapter: MessagesAdapter

    private var listingId    = ""
    private var providerId   = ""
    private var landlordName = ""
    private var landlordPhone = ""
    private var listingTitle = ""
    private var currentUid   = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityChatBinding.inflate(layoutInflater)
        setContentView(b.root)

        listingId     = intent.getStringExtra("listing_id")     ?: ""
        providerId    = intent.getStringExtra("provider_id")    ?: ""
        landlordName  = intent.getStringExtra("landlord_name")  ?: "Landlord"
        landlordPhone = intent.getStringExtra("landlord_phone") ?: ""
        listingTitle  = intent.getStringExtra("listing_title")  ?: ""
        currentUid    = SessionManager.uid(this)

        setupUI()
        setupMessages()
        setupSend()
    }

    private fun setupUI() {
        b.btnBack.setOnClickListener { finish() }
        b.tvLandlordName.text    = landlordName
        b.tvListingTitle.text    = "Re: $listingTitle"
        b.tvLandlordInitial.text = landlordName.firstOrNull()?.uppercase() ?: "L"

        b.btnCallLandlord.setOnClickListener {
            if (landlordPhone.isNotEmpty())
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:+267$landlordPhone")))
        }
    }

    private fun setupMessages() {
        adapter = MessagesAdapter(currentUid)
        b.rvMessages.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        b.rvMessages.adapter = adapter

        vm.getConversation(listingId, currentUid, providerId).observe(this) { msgs ->
            adapter.submitList(msgs)
            if (msgs.isNotEmpty()) b.rvMessages.scrollToPosition(msgs.size - 1)
        }
    }

    private fun setupSend() {
        b.btnSend.setOnClickListener { sendMessage() }
        b.etMessage.setOnEditorActionListener { _, _, _ -> sendMessage(); true }
    }

    private fun sendMessage() {
        val text = b.etMessage.text.toString().trim()
        if (text.isEmpty()) return

        val msg = Message(
            listingId  = listingId,
            senderId   = currentUid,
            receiverId = providerId,
            senderName = SessionManager.name(this),
            content    = text
        )
        vm.sendMessage(msg, listingId, currentUid, providerId)
        b.etMessage.text?.clear()

        // Auto-reply simulation (demo only)
        val replies = listOf(
            "Hi! Thanks for your interest. The room is still available.",
            "Yes, you can schedule a viewing anytime this week.",
            "The deposit is payable via the app when you reserve.",
            "All utilities are included in the monthly price.",
            "Security is 24/7 with a gated entrance.",
            "The nearest bus route is about 200 metres away.",
            "Please feel free to visit between 9am – 5pm on weekdays.",
            "Yes, we welcome students from all institutions."
        )
        b.rvMessages.postDelayed({
            if (!isFinishing) {
                val reply = Message(
                    listingId  = listingId,
                    senderId   = providerId,
                    receiverId = currentUid,
                    senderName = landlordName,
                    content    = replies[(System.currentTimeMillis() % replies.size).toInt()]
                )
                vm.sendMessage(reply, listingId, currentUid, providerId)
            }
        }, 1600)
    }
}
