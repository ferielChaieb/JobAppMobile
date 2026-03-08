package com.example.authentif.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.authentif.data.models.JobR
import com.example.authentif.databinding.ItemRecruiterJobBinding

class RecruiterJobsAdapter(
    private val onDetail: (JobR) -> Unit,
    private val onEdit: (JobR) -> Unit
) : RecyclerView.Adapter<RecruiterJobsAdapter.VH>() {

    private val items = mutableListOf<JobR>()

    fun submit(list: List<JobR>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    class VH(val b: ItemRecruiterJobBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemRecruiterJobBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val job = items[position]

        holder.b.tvJobTitle.text = job.title
        holder.b.tvMeta.text = "${job.type} • ${job.location}"
        holder.b.tvStatus.text = job.status.replaceFirstChar { it.uppercase() }

        holder.b.btnDetail.text = "Detail Job"
        holder.b.btnDetail.setOnClickListener { onDetail(job) }
        holder.b.btnEdit.setOnClickListener { onEdit(job) }
    }

    override fun getItemCount() = items.size
}