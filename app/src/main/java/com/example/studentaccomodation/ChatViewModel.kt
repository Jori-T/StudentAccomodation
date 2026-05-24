package com.example.studentaccomodation

import androidx.lifecycle.*
import com.example.studentaccomodation.Message
import com.example.studentaccomodation.FirebaseRepository

class ChatViewModel(private val repo: FirebaseRepository) : ViewModel() {

    fun getConversation(listingId: String, userId: String, providerId: String): LiveData<List<Message>> =
        repo.getConversation(listingId, userId, providerId)

    fun sendMessage(msg: Message, listingId: String, userId: String, providerId: String) =
        repo.sendMessage(msg, listingId, userId, providerId)
}