package com.example.authentif

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.authentif.ui.LoginActivity
import com.example.authentif.ui.SignupActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var btnCreateAccount: Button
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)   // ton écran avec les 2 boutons

        //  les ids doivent exister dans activity_splash.xml
        btnCreateAccount = findViewById(R.id.btnCreateAccount)
        btnLogin = findViewById(R.id.btnLogin)

        //  Aller vers la page d’inscription
        btnCreateAccount.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        //  Aller vers la page de connexion
        btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
