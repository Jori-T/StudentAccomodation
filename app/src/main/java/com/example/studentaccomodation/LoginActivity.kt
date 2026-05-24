package com.example.studentaccomodation

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.studentaccomodation.databinding.ActivityLoginBinding
import kotlin.getValue

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val vm: AuthViewModel by viewModels { ViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observe()
        clicks()
    }

    private fun observe() {
        vm.loading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled    = !loading
            binding.btnRegister.isEnabled = !loading
        }

        vm.loginResult.observe(this) { result ->
            result.onSuccess { user ->
                SessionManager.save(this, user.uid, user.fullName, user.email, user.role)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            result.onFailure { e ->
                binding.tvError.visibility = View.VISIBLE
                binding.tvError.text = friendlyError(e.message)
                binding.tilEmail.error    = " "
                binding.tilPassword.error = " "
            }
        }
    }

    private fun clicks() {
        binding.btnLogin.setOnClickListener {
            binding.tilEmail.error    = null
            binding.tilPassword.error = null
            binding.tvError.visibility = View.GONE

            val email = binding.etEmail.text.toString().trim()
            val pass  = binding.etPassword.text.toString()

            if (email.isEmpty()) { binding.tilEmail.error = "Email required"; return@setOnClickListener }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.error = "Invalid email"; return@setOnClickListener
            }
            if (pass.length < 6) { binding.tilPassword.error = "Min 6 characters"; return@setOnClickListener }

            vm.login(email, pass)
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun friendlyError(msg: String?): String = when {
        msg == null -> "Login failed. Try again."
        "password" in msg.lowercase() -> "Incorrect password."
        "user" in msg.lowercase()     -> "No account found for this email."
        "network" in msg.lowercase()  -> "No internet connection."
        else -> msg
    }
}
