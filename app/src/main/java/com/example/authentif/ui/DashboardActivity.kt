package com.example.authentif.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.example.authentif.*
import com.example.authentif.data.models.JobPublic
import com.example.authentif.viewmodel.DashboardViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class DashboardActivity : AppCompatActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val viewModel: DashboardViewModel by viewModels()

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var ivMenu: ImageButton

    private lateinit var tvCandidateName: TextView
    private lateinit var tvAppliedCount: TextView
    private lateinit var tvAcceptedCount: TextView
    private lateinit var tvProfilePercent: TextView

    private lateinit var btnViewAllJobs: TextView
    private lateinit var tvJobsEmpty: TextView
    private lateinit var jobsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Drawer (inchangé)
        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.nav_view)
        ivMenu = findViewById(R.id.ivMenu)

        ivMenu.setOnClickListener { drawerLayout.openDrawer(GravityCompat.END) }

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_candidate -> startActivity(Intent(this, CandidateMainActivity::class.java))
                R.id.menu_logout -> {
                    auth.signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.END)
            true
        }

        // Bind stats (inchangé)
        tvCandidateName = findViewById(R.id.tvCandidateName)
        tvAppliedCount = findViewById(R.id.tvAppliedCount)
        tvAcceptedCount = findViewById(R.id.tvAcceptedCount)
        tvProfilePercent = findViewById(R.id.tvProfilePercent)

        // Bind jobs section (inchangé)
        btnViewAllJobs = findViewById(R.id.btnViewAllJobs)
        tvJobsEmpty = findViewById(R.id.tvJobsEmpty)
        jobsContainer = findViewById(R.id.jobsContainer)

        // Candidate name (inchangé)
        val email = auth.currentUser?.email ?: "candidate@mail.com"
        tvCandidateName.text = email.substringBefore("@")

        // Profile percent (inchangé)
        tvProfilePercent.text = "0%"

        btnViewAllJobs.setOnClickListener {
            Toast.makeText(this, "Voir tous les jobs (bientôt)", Toast.LENGTH_SHORT).show()
        }

        observeUi()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh(auth.currentUser?.uid)
    }

    private fun observeUi() {
        lifecycleScope.launch {
            viewModel.ui.collect { ui ->
                tvAppliedCount.text = ui.appliedCount
                tvAcceptedCount.text = ui.acceptedCount

                if (!ui.jobsError.isNullOrBlank()) {
                    tvJobsEmpty.visibility = View.VISIBLE
                    tvJobsEmpty.text = "Impossible de charger les jobs."
                    Toast.makeText(this@DashboardActivity, "Erreur: ${ui.jobsError}", Toast.LENGTH_LONG).show()
                    jobsContainer.removeAllViews()
                } else {
                    renderJobs(ui.jobs)
                }
            }
        }
    }

    // ✅ Ton rendu UI (presque identique à ton code)
    private fun renderJobs(jobs: List<JobPublic>) {
        jobsContainer.removeAllViews()

        if (jobs.isEmpty()) {
            tvJobsEmpty.visibility = View.VISIBLE
            tvJobsEmpty.text = "Aucun job disponible."
            return
        }

        tvJobsEmpty.visibility = View.GONE

        jobs.forEach { job ->
            val jobId = job.id

            // ✅ même fallback company comme ton code (sans changer ton model)
            val company = "Company" // si tu veux, on peut le récupérer depuis Firestore plus tard

            val row = layoutInflater.inflate(R.layout.item_job_post, jobsContainer, false)

            row.findViewById<TextView>(R.id.tvJobTitle).text =
                if (job.title.isBlank()) "Offre sans titre" else job.title

            row.findViewById<TextView>(R.id.tvCompany).text = company

            row.findViewById<Chip>(R.id.chipType).text = job.type.ifBlank { "—" }
            row.findViewById<Chip>(R.id.chipLocation).text = job.location.ifBlank { "—" }

            row.findViewById<TextView>(R.id.tvSalary).text = "${job.minSalary} - ${job.maxSalary}"
            row.findViewById<TextView>(R.id.tvTime).text = timeAgo(job.createdAt)

            row.findViewById<ImageView>(R.id.btnFav).setOnClickListener {
                Toast.makeText(this, "Saved ✅", Toast.LENGTH_SHORT).show()
            }

            row.setOnClickListener {
                val i = Intent(this, JobDetailsActivity::class.java)
                i.putExtra("jobId", jobId)
                startActivity(i)
            }

            jobsContainer.addView(row)
        }
    }

    private fun timeAgo(createdAt: Long): String {
        if (createdAt <= 0L) return "Nouveau"
        val diff = System.currentTimeMillis() - createdAt
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            minutes < 1 -> "À l’instant"
            minutes < 60 -> "$minutes min"
            hours < 24 -> "$hours h"
            days < 7 -> "$days j"
            else -> "Nouveau"
        }
    }
}