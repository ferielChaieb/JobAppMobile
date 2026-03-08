package com.example.authentif.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.authentif.data.models.RecruiterCandidateItem
import com.example.authentif.databinding.ItemRecruiterCandidateBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecruiterCandidatesAdapter(
    private val onAccept: (RecruiterCandidateItem) -> Unit,
    private val onReject: (RecruiterCandidateItem) -> Unit,
    private val onOpenDetails: (RecruiterCandidateItem) -> Unit
) : RecyclerView.Adapter<RecruiterCandidatesAdapter.VH>() {

    private val allItems = mutableListOf<RecruiterCandidateItem>()
    private val shownItems = mutableListOf<RecruiterCandidateItem>()

    private var query: String = ""
    private var statusFilter: String = "all"

    fun submitList(list: List<RecruiterCandidateItem>) {
        allItems.clear()
        allItems.addAll(list)
        applyFilters()
    }

    fun setQuery(q: String) {
        query = q.trim().lowercase(Locale.getDefault())
        applyFilters()
    }

    fun setStatusFilter(status: String) {
        statusFilter = status
        applyFilters()
    }

    private fun applyFilters() {
        val filtered = allItems.asSequence()
            .filter { item -> statusFilter == "all" || item.status == statusFilter }
            .filter { item ->
                if (query.isBlank()) true
                else item.candidateName.lowercase(Locale.getDefault()).contains(query) ||
                        item.jobTitle.lowercase(Locale.getDefault()).contains(query)
            }
            .toList()

        shownItems.clear()
        shownItems.addAll(filtered)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemRecruiterCandidateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(shownItems[position])

    override fun getItemCount(): Int = shownItems.size

    inner class VH(private val b: ItemRecruiterCandidateBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(item: RecruiterCandidateItem) {
            val letter = item.candidateName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            b.tvAvatarLetter.text = letter

            b.tvName.text = item.candidateName
            b.tvMeta.text = "Applied for: ${item.jobTitle}"

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            b.tvApplied.text = "Applied ${sdf.format(Date(item.appliedAt))}"

            setStatusBadge(b.tvStatus, item.status)

            b.root.setOnClickListener { onOpenDetails(item) }

            b.rowActions.visibility = if (item.status == "pending") View.VISIBLE else View.GONE

            b.btnAccept.setOnClickListener { onAccept(item) }
            b.btnReject.setOnClickListener { onReject(item) }
        }

        private fun setStatusBadge(tv: TextView, status: String) {
            when (status) {
                "accepted" -> {
                    tv.text = "Accepted"
                    tv.setTextColor(Color.parseColor("#166534"))
                    tv.setBackgroundColor(Color.parseColor("#DCFCE7"))
                }
                "rejected" -> {
                    tv.text = "Rejected"
                    tv.setTextColor(Color.parseColor("#991B1B"))
                    tv.setBackgroundColor(Color.parseColor("#FEE2E2"))
                }
                else -> {
                    tv.text = "Pending"
                    tv.setTextColor(Color.parseColor("#92400E"))
                    tv.setBackgroundColor(Color.parseColor("#FEF3C7"))
                }
            }
        }
    }
}