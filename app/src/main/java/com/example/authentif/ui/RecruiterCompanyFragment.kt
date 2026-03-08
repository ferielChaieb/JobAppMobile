package com.example.authentif.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.authentif.R
import com.example.authentif.data.models.RecruiterCompanyProfile
import com.example.authentif.databinding.FragmentRecruiterCompanyBinding
import com.example.authentif.viewmodel.RecruiterCompanySaveState
import com.example.authentif.viewmodel.RecruiterCompanyState
import com.example.authentif.viewmodel.RecruiterCompanyViewModel
import kotlinx.coroutines.launch

class RecruiterCompanyFragment : Fragment(R.layout.fragment_recruiter_company) {

    private var _binding: FragmentRecruiterCompanyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RecruiterCompanyViewModel by viewModels()

    private var isEditing = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRecruiterCompanyBinding.bind(view)

        setEditMode(false)

        observeViewModel()
        viewModel.loadRecruiterProfile()

        binding.btnEditInfo.setOnClickListener {
            setEditMode(true)
            fillEditFieldsFromView()
        }

        binding.btnEditAbout.setOnClickListener {
            setEditMode(true)
            fillEditFieldsFromView()
        }

        binding.btnTopAction.setOnClickListener {
            if (isEditing) {
                saveToViewModelAndBackToView()
            } else {
                Toast.makeText(requireContext(), "Settings", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSignOut.setOnClickListener {
            viewModel.logout()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun setEditMode(enable: Boolean) {
        isEditing = enable

        binding.infoViewBlock.visibility = if (enable) View.GONE else View.VISIBLE
        binding.infoEditBlock.visibility = if (enable) View.VISIBLE else View.GONE

        binding.tvAbout.visibility = if (enable) View.GONE else View.VISIBLE
        binding.etAbout.visibility = if (enable) View.VISIBLE else View.GONE

        binding.btnEditInfo.text = if (enable) "Editing..." else "Edit"
        binding.btnEditAbout.text = if (enable) "Editing..." else "Edit"
        binding.btnEditInfo.isEnabled = !enable
        binding.btnEditAbout.isEnabled = !enable

        binding.btnTopAction.setImageResource(
            if (enable) android.R.drawable.ic_menu_save
            else android.R.drawable.ic_menu_manage
        )
    }

    private fun fillEditFieldsFromView() {
        binding.etIndustry.setText(binding.tvIndustry.text.toString().replace("—", ""))
        binding.etSize.setText(binding.tvSize.text.toString().replace("—", ""))
        binding.etLocation.setText(binding.tvLocation.text.toString().replace("—", ""))
        binding.etWebsite.setText(binding.tvWebsite.text.toString().replace("—", ""))
        binding.etAbout.setText(binding.tvAbout.text.toString().replace("—", ""))
    }

    private fun saveToViewModelAndBackToView() {
        val profile = RecruiterCompanyProfile(
            industry = binding.etIndustry.text?.toString()?.trim().orEmpty(),
            size = binding.etSize.text?.toString()?.trim().orEmpty(),
            location = binding.etLocation.text?.toString()?.trim().orEmpty(),
            website = binding.etWebsite.text?.toString()?.trim().orEmpty(),
            about = binding.etAbout.text?.toString()?.trim().orEmpty()
        )

        viewModel.saveRecruiterProfile(profile)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is RecruiterCompanyState.Loading -> Unit

                    is RecruiterCompanyState.Success -> {
                        val profile = state.profile

                        binding.tvIndustry.text = profile.industry.ifBlank { "—" }
                        binding.tvSize.text = profile.size.ifBlank { "—" }
                        binding.tvLocation.text = profile.location.ifBlank { "—" }
                        binding.tvWebsite.text = profile.website.ifBlank { "—" }
                        binding.tvAbout.text = profile.about.ifBlank { "—" }
                    }

                    is RecruiterCompanyState.Error -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }
        }

        lifecycleScope.launch {
            viewModel.saveState.collect { state ->
                when (state) {
                    is RecruiterCompanySaveState.Loading -> {
                        binding.btnTopAction.isEnabled = false
                    }

                    is RecruiterCompanySaveState.Success -> {
                        binding.btnTopAction.isEnabled = true

                        binding.tvIndustry.text = binding.etIndustry.text?.toString()?.trim().orEmpty().ifBlank { "—" }
                        binding.tvSize.text = binding.etSize.text?.toString()?.trim().orEmpty().ifBlank { "—" }
                        binding.tvLocation.text = binding.etLocation.text?.toString()?.trim().orEmpty().ifBlank { "—" }
                        binding.tvWebsite.text = binding.etWebsite.text?.toString()?.trim().orEmpty().ifBlank { "—" }
                        binding.tvAbout.text = binding.etAbout.text?.toString()?.trim().orEmpty().ifBlank { "—" }

                        setEditMode(false)
                        Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
                    }

                    is RecruiterCompanySaveState.Error -> {
                        binding.btnTopAction.isEnabled = true
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> {
                        binding.btnTopAction.isEnabled = true
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}