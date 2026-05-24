package com.example.studentaccomodation

import androidx.lifecycle.*
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class AuthViewModel(private val repo: FirebaseRepository) : ViewModel() {

    private val _loginResult  = MutableLiveData<Result<User>>()
    val loginResult: LiveData<Result<User>> = _loginResult

    private val _registerResult = MutableLiveData<Result<User>>()
    val registerResult: LiveData<Result<User>> = _registerResult

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    fun login(email: String, password: String) = viewModelScope.launch {
        _loading.value = true
        try {
            val uid  = repo.loginWithEmail(email, password)
            val user = repo.getUser(uid) ?: throw Exception("User profile not found")
            
            // Update FCM token
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                repo.updateFcmToken(uid, token)
            } catch (_: Exception) {}
            _loginResult.value = Result.success(user)
        } catch (e: Exception) {
            _loginResult.value = Result.failure(e)
        } finally { _loading.value = false }
    }

    fun register(
        studentId: String, fullName: String, email: String,
        phone: String, password: String, role: String, institution: String
    ) = viewModelScope.launch {
        _loading.value = true
        try {
            val uid = repo.registerWithEmail(email, password)
            val user = User(
                uid = uid, studentId = studentId, fullName = fullName,
                email = email, phone = phone, role = role, institution = institution
            )
            repo.createUser(user)
            
            // Seed listings on first run (any new account triggers it)
            repo.seedListingsIfEmpty()

            _registerResult.value = Result.success(user)
        } catch (e: Exception) {
            _registerResult.value = Result.failure(e)
        } finally { _loading.value = false }
    }

    fun logout() = repo.logout()
}
