package com.example.authentif.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.authentif.databinding.ItemRecentCandidateApplicationBinding
import com.example.authentif.data.models.RecentApplicationItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecentApplicationsAdapter(
    private val onClick: (RecentApplicationItem) -> Unit
) : RecyclerView.Adapter<RecentApplicationsAdapter.VH>() {

    private val items = mutableListOf<RecentApplicationItem>()

    fun submit(list: List<RecentApplicationItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(private val b: ItemRecentCandidateApplicationBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(item: RecentApplicationItem) {
            val letter = item.candidateName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            b.tvAvatarLetter.text = letter

            b.tvName.text = item.candidateName
            b.tvMeta.text = "Applied for: ${item.jobTitle}"

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            b.tvDate.text = "Applied ${if (item.appliedAt > 0) sdf.format(Date(item.appliedAt)) else "—"}"

            when (item.status.lowercase()) {
                "accepted" -> {
                    b.tvStatus.text = "Accepted"
                    b.tvStatus.setTextColor(Color.parseColor("#166534"))
                    b.tvStatus.setBackgroundColor(Color.parseColor("#DCFCE7"))
                }
                "rejected" -> {
                    b.tvStatus.text = "Rejected"
                    b.tvStatus.setTextColor(Color.parseColor("#991B1B"))
                    b.tvStatus.setBackgroundColor(Color.parseColor("#FEE2E2"))
                }
                else -> {
                    b.tvStatus.text = "Pending"
                    b.tvStatus.setTextColor(Color.parseColor("#92400E"))
                    b.tvStatus.setBackgroundColor(Color.parseColor("#FEF3C7"))
                }
            }

            b.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemRecentCandidateApplicationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size
}