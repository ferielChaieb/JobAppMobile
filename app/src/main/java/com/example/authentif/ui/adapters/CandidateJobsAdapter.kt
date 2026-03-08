package com.example.authentif.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.authentif.R
import com.example.authentif.data.models.JobPublic
import com.google.android.material.chip.Chip

class CandidateJobsAdapter(
    private val items: MutableList<JobPublic>,
    private val onFavClick: (JobPublic) -> Unit,
    private val onItemClick: (JobPublic) -> Unit
) : RecyclerView.Adapter<CandidateJobsAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvJobTitle: TextView = v.findViewById(R.id.tvJobTitle)
        val tvCompany: TextView = v.findViewById(R.id.tvCompany)
        val chipType: Chip = v.findViewById(R.id.chipType)
        val chipLocation: Chip = v.findViewById(R.id.chipLocation)
        val tvSalary: TextView = v.findViewById(R.id.tvSalary)
        val tvTime: TextView = v.findViewById(R.id.tvTime)
        val btnFav: ImageView = v.findViewById(R.id.btnFav)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_job_post, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val job = items[position]

        h.tvJobTitle.text = job.title.ifBlank { "—" }
        h.tvCompany.text = job.companyName.ifBlank { "Company" }
        h.chipType.text = job.type.ifBlank { "—" }
        h.chipLocation.text = job.location.ifBlank { "—" }

        val salaryText =
            if (job.minSalary != "—" || job.maxSalary != "—") {
                "${job.minSalary} - ${job.maxSalary}"
            } else {
                "—"
            }

        h.tvSalary.text = salaryText
        h.tvTime.text = timeAgo(job.createdAt)

        h.btnFav.setImageResource(android.R.drawable.btn_star_big_off)
        h.btnFav.setOnClickListener { onFavClick(job) }

        h.itemView.setOnClickListener { onItemClick(job) }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<JobPublic>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    private fun timeAgo(createdAt: Long): String {
        if (createdAt <= 0L) return "—"

        val diff = System.currentTimeMillis() - createdAt
        val min = diff / 1000 / 60
        val hour = min / 60
        val day = hour / 24

        return when {
            day > 0 -> "$day day(s) ago"
            hour > 0 -> "$hour hour(s) ago"
            min > 0 -> "$min min ago"
            else -> "Just now"
        }
    }
}