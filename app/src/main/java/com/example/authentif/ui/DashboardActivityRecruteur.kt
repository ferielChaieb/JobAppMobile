package com.example.authentif.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.authentif.R
import com.example.authentif.ui.adapters.RecentApplicationsAdapter
import com.example.authentif.viewmodel.RecruiterDashboardViewModel
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch

class DashboardActivityRecruteur : AppCompatActivity() {

    private val viewModel: RecruiterDashboardViewModel by viewModels()

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var ivMenu: ImageButton

    private lateinit var tvRecruiterName: TextView
    private lateinit var tvActiveJobsCount: TextView
    private lateinit var tvApplicantsCount: TextView
    private lateinit var tvHiredCount: TextView

    private lateinit var tvViewAll: TextView
    private lateinit var tvEmptyRecent: TextView
    private lateinit var rvRecent: RecyclerView

    private lateinit var recentAdapter: RecentApplicationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!viewModel.isUserLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_dashboard_recruteur)

        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.nav_view)
        ivMenu = findViewById(R.id.ivMenu)

        tvRecruiterName = findViewById(R.id.tvRecruiterName)
        tvActiveJobsCount = findViewById(R.id.tvActiveJobsCount)
        tvApplicantsCount = findViewById(R.id.tvApplicantsCount)
        tvHiredCount = findViewById(R.id.tvHiredCount)

        tvViewAll = findViewById(R.id.tvViewAll)
        tvEmptyRecent = findViewById(R.id.tvEmptyRecent)
        rvRecent = findViewById(R.id.rvRecent)

        ivMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_recruiter -> {
                    startActivity(Intent(this, RecruiterMainActivity::class.java))
                }
                R.id.menu_logout -> {
                    viewModel.logout()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.END)
            true
        }

        recentAdapter = RecentApplicationsAdapter { item ->
            if (item.candidateId.isBlank()) return@RecentApplicationsAdapter
            val i = Intent(this, CandidateProfileDetailsActivity::class.java)
            i.putExtra("candidateId", item.candidateId)
            startActivity(i)
        }

        rvRecent.layoutManager = LinearLayoutManager(this)
        rvRecent.adapter = recentAdapter

        tvViewAll.setOnClickListener {
            startActivity(Intent(this, RecruiterMainActivity::class.java))
        }

        observeViewModel()

        viewModel.loadHeaderName()
        viewModel.loadStatsAndRecent()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadStatsAndRecent()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.ui.collect { ui ->
                tvRecruiterName.text = ui.recruiterName
                tvActiveJobsCount.text = ui.activeJobsCount
                tvApplicantsCount.text = ui.applicantsCount
                tvHiredCount.text = ui.hiredCount

                tvEmptyRecent.text = ui.recentApplicationsMessage
                tvEmptyRecent.visibility = if (ui.recentApplicationsEmpty) TextView.VISIBLE else TextView.GONE
                rvRecent.visibility = if (ui.recentApplicationsEmpty) RecyclerView.GONE else RecyclerView.VISIBLE
            }
        }

        lifecycleScope.launch {
            viewModel.recent.collect { list ->
                recentAdapter.submit(list)
            }
        }
    }
}