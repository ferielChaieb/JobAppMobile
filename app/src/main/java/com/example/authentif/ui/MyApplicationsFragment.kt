package com.example.authentif.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.authentif.R
import com.example.authentif.ui.adapters.ApplicationsAdapter
import com.example.authentif.viewmodel.MyApplicationsViewModel
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class MyApplicationsFragment : Fragment(R.layout.fragment_my_applications) {

    private lateinit var tabLayout: TabLayout
    private lateinit var adapter: ApplicationsAdapter
    private val viewModel: MyApplicationsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabLayout = view.findViewById(R.id.tabLayout)

        val rv = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvApplications)
        rv.layoutManager = LinearLayoutManager(requireContext())

        adapter = ApplicationsAdapter(
            onDetails = { item ->
                Toast.makeText(requireContext(), "Job: ${item.jobTitle}", Toast.LENGTH_SHORT).show()
            },
            onMenu = { _, item ->
                Toast.makeText(requireContext(), "Status: ${item.status}", Toast.LENGTH_SHORT).show()
            }
        )

        rv.adapter = adapter

        setupTabs()
        observeViewModel()
        viewModel.loadMyApplications()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadMyApplications()
    }

    private fun setupTabs() {
        tabLayout.removeAllTabs()
        tabLayout.addTab(tabLayout.newTab().setText("All (0)"))
        tabLayout.addTab(tabLayout.newTab().setText("Pending (0)"))
        tabLayout.addTab(tabLayout.newTab().setText("Accepted (0)"))
        tabLayout.addTab(tabLayout.newTab().setText("Rejected (0)"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.selectTab(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.ui.collect { ui ->
                adapter.submitList(ui.filteredItems.toList())

                tabLayout.getTabAt(0)?.text = "All (${viewModel.totalCount()})"
                tabLayout.getTabAt(1)?.text = "Pending (${viewModel.pendingCount()})"
                tabLayout.getTabAt(2)?.text = "Accepted (${viewModel.acceptedCount()})"
                tabLayout.getTabAt(3)?.text = "Rejected (${viewModel.rejectedCount()})"

                ui.error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}