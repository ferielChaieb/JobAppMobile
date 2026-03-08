package com.example.authentif.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.authentif.R
import com.example.authentif.viewmodel.RecruiterMainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class RecruiterMainActivity : AppCompatActivity() {

    private val viewModel: RecruiterMainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // sécurité auth via ViewModel
        if (!viewModel.isUserLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_recruiter_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavRecruiter)

        val navHost = supportFragmentManager
            .findFragmentById(R.id.navHostRecruiter) as NavHostFragment

        val navController = navHost.navController

        // Relie bottom nav au navController
        bottomNav.setupWithNavController(navController)

        // Intercepte juste HOME
        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.nav_recruiter_home) {
                startActivity(Intent(this, DashboardActivityRecruteur::class.java))
                true
            } else {
                androidx.navigation.ui.NavigationUI.onNavDestinationSelected(item, navController)
                true
            }
        }

        // Optionnel : au démarrage, sélectionne Jobs
        bottomNav.selectedItemId = R.id.nav_recruiter_jobs
    }
}