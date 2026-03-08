package com.example.authentif.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.authentif.R
import com.example.authentif.data.models.ApplicationItem
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

class ApplicationsAdapter(
    private val onDetails: (ApplicationItem) -> Unit,
    private val onMenu: (View, ApplicationItem) -> Unit
) : ListAdapter<ApplicationItem, ApplicationsAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<ApplicationItem>() {
            override fun areItemsTheSame(oldItem: ApplicationItem, newItem: ApplicationItem) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ApplicationItem, newItem: ApplicationItem) =
                oldItem == newItem
        }
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvJobTitle: TextView = itemView.findViewById(R.id.tvJobTitle)
        val tvCompany: TextView = itemView.findViewById(R.id.tvCompany)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val chipStatus: Chip = itemView.findViewById(R.id.chipStatus)
        val btnDetails: MaterialButton = itemView.findViewById(R.id.btnDetails)
        val btnMenu: ImageView = itemView.findViewById(R.id.btnMenu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_application, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)

        holder.tvJobTitle.text = item.jobTitle
        holder.tvCompany.text = item.company
        holder.tvDate.text = "Applied on ${item.appliedDate}"

        when (item.status.lowercase()) {
            "accepted" -> {
                holder.chipStatus.text = "Accepted"
                holder.chipStatus.setChipBackgroundColorResource(android.R.color.holo_green_light)
                holder.chipStatus.setTextColor(Color.parseColor("#065F46"))
            }
            "rejected" -> {
                holder.chipStatus.text = "Rejected"
                holder.chipStatus.setChipBackgroundColorResource(android.R.color.holo_red_light)
                holder.chipStatus.setTextColor(Color.parseColor("#7F1D1D"))
            }
            else -> {
                holder.chipStatus.text = "Pending"
                holder.chipStatus.setChipBackgroundColorResource(android.R.color.holo_orange_light)
                holder.chipStatus.setTextColor(Color.parseColor("#92400E"))
            }
        }

        holder.btnDetails.setOnClickListener { onDetails(item) }
        holder.btnMenu.setOnClickListener { v -> onMenu(v, item) }
    }
}