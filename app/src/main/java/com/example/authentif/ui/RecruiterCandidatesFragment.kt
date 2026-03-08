package com.example.authentif.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.authentif.R
import com.example.authentif.ui.adapters.RecruiterCandidatesAdapter
import com.example.authentif.ui.extensions.addTextChangedListenerSimple
import com.example.authentif.viewmodel.RecruiterCandidatesViewModel
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class RecruiterCandidatesFragment : Fragment(R.layout.fragment_recruiter_candidates) {

    private lateinit var etSearch: EditText
    private lateinit var rvCandidates: RecyclerView
    private lateinit var tvEmpty: TextView

    private lateinit var chipAll: Chip
    private lateinit var chipPending: Chip
    private lateinit var chipAccepted: Chip
    private lateinit var chipRejected: Chip

    private lateinit var adapter: RecruiterCandidatesAdapter

    private val viewModel: RecruiterCandidatesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.tvTitle).text = "Candidates"

        etSearch = view.findViewById(R.id.etSearch)
        rvCandidates = view.findViewById(R.id.rvCandidates)
        tvEmpty = view.findViewById(R.id.tvEmpty)

        chipAll = view.findViewById(R.id.chipAll)
        chipPending = view.findViewById(R.id.chipPending)
        chipAccepted = view.findViewById(R.id.chipAccepted)
        chipRejected = view.findViewById(R.id.chipRejected)

        adapter = RecruiterCandidatesAdapter(
            onAccept = { viewModel.updateStatus(it.id, "accepted") },
            onReject = { viewModel.updateStatus(it.id, "rejected") },
            onOpenDetails = { item ->
                if (item.candidateId.isBlank()) return@RecruiterCandidatesAdapter

                val i = Intent(requireContext(), CandidateProfileDetailsActivity::class.java)
                i.putExtra("candidateId", item.candidateId)
                startActivity(i)
            }
        )

        rvCandidates.layoutManager = LinearLayoutManager(requireContext())
        rvCandidates.adapter = adapter

        chipAll.isChecked = true
        adapter.setStatusFilter("all")

        chipAll.setOnClickListener {
            adapter.setStatusFilter("all")
            toggleEmpty()
        }

        chipPending.setOnClickListener {
            adapter.setStatusFilter("pending")
            toggleEmpty()
        }

        chipAccepted.setOnClickListener {
            adapter.setStatusFilter("accepted")
            toggleEmpty()
        }

        chipRejected.setOnClickListener {
            adapter.setStatusFilter("rejected")
            toggleEmpty()
        }

        etSearch.addTextChangedListenerSimple {
            adapter.setQuery(etSearch.text?.toString().orEmpty())
            toggleEmpty()
        }

        observeViewModel()
        viewModel.loadApplicants()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadApplicants()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.ui.collect { ui ->
                adapter.submitList(ui.items)

                updateChipCounts()

                toggleEmpty()

                ui.error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateChipCounts() {
        chipAll.text = "All (${viewModel.allCount()})"
        chipPending.text = "Pending (${viewModel.pendingCount()})"
        chipAccepted.text = "Accepted (${viewModel.acceptedCount()})"
        chipRejected.text = "Rejected (${viewModel.rejectedCount()})"
    }

    private fun toggleEmpty() {
        tvEmpty.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
    }
}