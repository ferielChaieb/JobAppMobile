package com.example.authentif.ui

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.authentif.R
import com.example.authentif.viewmodel.SignupState
import com.example.authentif.viewmodel.SignupViewModel
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {

    private val viewModel: SignupViewModel by viewModels()

    private lateinit var name: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var btnSignup: Button
    private lateinit var tvGoLogin: TextView

    private lateinit var rgRole: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        name = findViewById(R.id.etName)
        email = findViewById(R.id.etEmail)
        password = findViewById(R.id.etPassword)
        btnSignup = findViewById(R.id.btnSignup)
        tvGoLogin = findViewById(R.id.tvGoLogin)
        rgRole = findViewById(R.id.rgRole)

        btnSignup.setOnClickListener {
            val checkedId = rgRole.checkedRadioButtonId
            if (checkedId == -1) {
                Toast.makeText(this, "Choisissez un rôle (Candidate ou Recruteur)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val role = when (checkedId) {
                R.id.rbCandidate -> "candidate"
                R.id.rbRecruiter -> "recruiter"
                else -> "candidate"
            }

            viewModel.signup(
                name = name.text.toString(),
                email = email.text.toString(),
                password = password.text.toString(),
                role = role
            )
        }

        tvGoLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        observeState()
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is SignupState.Idle -> btnSignup.isEnabled = true

                    is SignupState.Loading -> btnSignup.isEnabled = false

                    is SignupState.Success -> {
                        btnSignup.isEnabled = true
                        Toast.makeText(this@SignupActivity, "Compte créé avec succès ✔", Toast.LENGTH_SHORT).show()

                        val next = if (state.role == "candidate") {
                            Intent(this@SignupActivity, DashboardActivity::class.java)
                        } else {
                            Intent(this@SignupActivity, DashboardActivityRecruteur::class.java)
                        }
                        startActivity(next)
                        finish()
                    }

                    is SignupState.Error -> {
                        btnSignup.isEnabled = true
                        // petit bonus : si c'est le nom, on met error sur le champ
                        if (state.message == "Nom requis") name.error = "Nom requis"
                        Toast.makeText(this@SignupActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}