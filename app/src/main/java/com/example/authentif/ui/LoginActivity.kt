package com.example.authentif.ui

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.authentif.R
import com.example.authentif.viewmodel.LoginState
import com.example.authentif.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvGoSignup: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        email = findViewById(R.id.etEmailLogin)
        password = findViewById(R.id.etPasswordLogin)
        btnLogin = findViewById(R.id.btnLogin)
        tvGoSignup = findViewById(R.id.tvGoSignup)

        btnLogin.setOnClickListener {
            viewModel.login(
                email.text.toString(),
                password.text.toString()
            )
        }

        tvGoSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        observeState()
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is LoginState.Idle -> {
                        btnLogin.isEnabled = true
                    }

                    is LoginState.Loading -> {
                        btnLogin.isEnabled = false
                    }

                    is LoginState.Success -> {
                        btnLogin.isEnabled = true

                        val role = state.role
                        val nextIntent = if (role == "recruiter") {
                            Intent(this@LoginActivity, DashboardActivityRecruteur::class.java)
                        } else {
                            Intent(this@LoginActivity, DashboardActivity::class.java)
                        }

                        Toast.makeText(this@LoginActivity, "Connexion réussie ✔ ($role)", Toast.LENGTH_SHORT).show()
                        startActivity(nextIntent)
                        finish()
                    }

                    is LoginState.Error -> {
                        btnLogin.isEnabled = true
                        Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}