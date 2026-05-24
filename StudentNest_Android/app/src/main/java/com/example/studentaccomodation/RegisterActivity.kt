package com.example.studentaccomodation

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.studentaccomodation.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import kotlin.getValue

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val vm: AuthViewModel by viewModels { ViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observe()
        clicks()
    }

    private fun observe() {
        vm.loading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnRegister.isEnabled  = !loading
        }

        vm.registerResult.observe(this) { result ->
            result.onSuccess { user ->
                // Sign out immediately so they have to log in manually
                FirebaseAuth.getInstance().signOut()
                
                Toast.makeText(this, "Registration successful! Please login with your details.", Toast.LENGTH_LONG).show()
                
                // Return to LoginActivity
                finish() 
            }
            result.onFailure { e ->
                binding.tvError.visibility = View.VISIBLE
                binding.tvError.text = when {
                    e.message?.contains("already in use", true) == true ->
                        "This email is already registered. Please login."
                    e.message?.contains("network", true) == true ->
                        "No internet connection. Please check and retry."
                    else -> e.message ?: "Registration failed."
                }
            }
        }
    }

    private fun clicks() {
        binding.btnBack.setOnClickListener { finish() }
        binding.tvLogin.setOnClickListener { finish() }

        binding.btnRegister.setOnClickListener {
            listOf(binding.tilName, binding.tilStudentId, binding.tilEmail,
                binding.tilPhone, binding.tilPassword, binding.tilConfirmPassword)
                .forEach { it.error = null }
            binding.tvError.visibility = View.GONE

            val name    = binding.etName.text.toString().trim()
            val sid     = binding.etStudentId.text.toString().trim()
            val inst    = binding.etInstitution.text.toString().trim()
            val email   = binding.etEmail.text.toString().trim()
            val phone   = binding.etPhone.text.toString().trim()
            val pass    = binding.etPassword.text.toString()
            val confirm = binding.etConfirmPassword.text.toString()
            val role    = if (binding.rbProvider.isChecked) "PROVIDER" else "STUDENT"

            if (name.isEmpty())  { binding.tilName.error = "Name required"; return@setOnClickListener }
            if (sid.isEmpty())   { binding.tilStudentId.error = "Student ID required"; return@setOnClickListener }
            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.error = "Valid email required"; return@setOnClickListener
            }
            if (phone.length < 7) { binding.tilPhone.error = "Valid phone required"; return@setOnClickListener }
            if (pass.length < 6)  { binding.tilPassword.error = "Min 6 characters"; return@setOnClickListener }
            if (pass != confirm)  { binding.tilConfirmPassword.error = "Passwords do not match"; return@setOnClickListener }

            vm.register(sid, name, email, phone, pass, role, inst)
        }
    }
}
