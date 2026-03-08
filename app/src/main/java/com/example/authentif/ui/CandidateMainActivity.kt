package com.example.authentif.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.authentif.R
import com.example.authentif.viewmodel.CandidateMainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class CandidateMainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private val viewModel: CandidateMainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_candidate_main)

        if (!viewModel.isUserLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        bottomNav = findViewById(R.id.bottomNavCandidate)

        val navHost = supportFragmentManager
            .findFragmentById(R.id.navHostCandidate) as NavHostFragment
        val navController = navHost.navController

        bottomNav.setupWithNavController(navController)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    true
                }

                else -> {
                    navController.navigate(item.itemId)
                    true
                }
            }
        }
    }
}