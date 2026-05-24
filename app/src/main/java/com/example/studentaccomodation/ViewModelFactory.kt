package com.example.studentaccomodation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.studentaccomodation.FirebaseRepository

class ViewModelFactory : ViewModelProvider.Factory {
    private val repo = FirebaseRepository()
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(AuthViewModel::class.java)    -> AuthViewModel(repo) as T
        modelClass.isAssignableFrom(ListingViewModel::class.java) -> ListingViewModel(repo) as T
        modelClass.isAssignableFrom(BookingViewModel::class.java) -> BookingViewModel(repo) as T
        modelClass.isAssignableFrom(ChatViewModel::class.java)    -> ChatViewModel(repo) as T
        else -> throw IllegalArgumentException("Unknown ViewModel")
    }
}