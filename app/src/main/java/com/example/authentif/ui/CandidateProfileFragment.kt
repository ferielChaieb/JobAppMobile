package com.example.authentif.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.authentif.R
import com.example.authentif.data.models.CandidateProfile
import com.example.authentif.data.models.EducationItemModel
import com.example.authentif.data.models.ProInfo
import com.example.authentif.viewmodel.CandidateProfileSaveState
import com.example.authentif.viewmodel.CandidateProfileState
import com.example.authentif.viewmodel.CandidateProfileViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class CandidateProfileFragment : Fragment(R.layout.fragment_candidate_profile) {

    private val viewModel: CandidateProfileViewModel by viewModels()

    // header
    private lateinit var btnSave: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView

    // pro
    private lateinit var btnEditPro: TextView
    private lateinit var proView: View
    private lateinit var proEdit: View
    private lateinit var tvPosition: TextView
    private lateinit var tvExperienceYears: TextView
    private lateinit var tvLocation: TextView
    private lateinit var etPosition: TextInputEditText
    private lateinit var etExperienceYears: TextInputEditText
    private lateinit var etLocation: TextInputEditText

    // skills
    private lateinit var btnEditSkills: TextView
    private lateinit var chipGroupSkills: ChipGroup
    private lateinit var skillsEditContainer: LinearLayout
    private lateinit var etSkillInput: TextInputEditText
    private lateinit var btnAddSkill: MaterialButton
    private val skillsList = mutableListOf<String>()

    // education
    private lateinit var btnAddEducation: TextView
    private lateinit var educationContainer: LinearLayout

    private data class EducationItem(
        val degree: String,
        val school: String,
        val years: String
    )

    private val educationList = mutableListOf<EducationItem>()

    // experience
    private lateinit var btnEditExperience: TextView
    private lateinit var tvExperienceList: TextView
    private lateinit var experienceEditLayout: TextInputLayout
    private lateinit var etExperience: TextInputEditText

    // logout
    private lateinit var btnLogout: MaterialButton

    // state
    private var isEditingPro = false
    private var isEditingSkills = false
    private var isEditingExp = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.getCurrentUserId().isNullOrBlank()) {
            toast("Pas connecté")
            return
        }

        // header
        btnSave = view.findViewById(R.id.btnSave)
        tvName = view.findViewById(R.id.tvName)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvEmail.text = viewModel.getCurrentUserEmail() ?: ""

        // pro
        btnEditPro = view.findViewById(R.id.btnEditPro)
        proView = view.findViewById(R.id.proView)
        proEdit = view.findViewById(R.id.proEdit)
        tvPosition = view.findViewById(R.id.tvPosition)
        tvExperienceYears = view.findViewById(R.id.tvExperienceYears)
        tvLocation = view.findViewById(R.id.tvLocation)

        etPosition = view.findViewById(R.id.etPosition)
        etExperienceYears = view.findViewById(R.id.etExperienceYears)
        etLocation = view.findViewById(R.id.etLocation)

        // skills
        btnEditSkills = view.findViewById(R.id.btnEditSkills)
        chipGroupSkills = view.findViewById(R.id.chipGroupSkills)
        skillsEditContainer = view.findViewById(R.id.skillsEditContainer)
        etSkillInput = view.findViewById(R.id.etSkillInput)
        btnAddSkill = view.findViewById(R.id.btnAddSkill)

        // education
        btnAddEducation = view.findViewById(R.id.btnAddEducation)
        educationContainer = view.findViewById(R.id.educationContainer)

        // experience
        btnEditExperience = view.findViewById(R.id.btnEditExperience)
        tvExperienceList = view.findViewById(R.id.tvExperienceList)
        experienceEditLayout = view.findViewById(R.id.experienceEdit)
        etExperience = view.findViewById(R.id.etExperience)

        // logout
        btnLogout = view.findViewById(R.id.btnLogout)

        // actions
        btnEditPro.setOnClickListener { toggleProEdit() }
        btnEditSkills.setOnClickListener { toggleSkillsEdit() }
        btnEditExperience.setOnClickListener { toggleExperienceEdit() }

        btnAddSkill.setOnClickListener { addSkillFromInput() }
        etSkillInput.setOnEditorActionListener { _, _, _ ->
            addSkillFromInput()
            true
        }

        btnAddEducation.setOnClickListener { openAddEducationDialogCompact() }

        btnSave.setOnClickListener { saveAll() }

        btnLogout.setOnClickListener {
            viewModel.logout()
            toast("Déconnecté")
        }

        observeViewModel()
        viewModel.loadProfile()
    }

    // ------------------ PRO INFO ------------------

    private fun toggleProEdit() {
        isEditingPro = !isEditingPro
        if (isEditingPro) {
            etPosition.setText(if (tvPosition.text == "—") "" else tvPosition.text.toString())
            etExperienceYears.setText(if (tvExperienceYears.text == "—") "" else tvExperienceYears.text.toString())
            etLocation.setText(if (tvLocation.text == "—") "" else tvLocation.text.toString())

            proView.visibility = View.GONE
            proEdit.visibility = View.VISIBLE
            btnEditPro.text = "Cancel"
        } else {
            proEdit.visibility = View.GONE
            proView.visibility = View.VISIBLE
            btnEditPro.text = "Edit"
        }
    }

    // ------------------ SKILLS ------------------

    private fun toggleSkillsEdit() {
        isEditingSkills = !isEditingSkills
        skillsEditContainer.visibility = if (isEditingSkills) View.VISIBLE else View.GONE
        btnEditSkills.text = if (isEditingSkills) "Cancel" else "Edit"
    }

    private fun addSkillFromInput() {
        val text = etSkillInput.text?.toString()?.trim().orEmpty()
        if (text.isEmpty()) return

        if (skillsList.any { it.equals(text, ignoreCase = true) }) {
            etSkillInput.setText("")
            return
        }

        skillsList.add(text)
        etSkillInput.setText("")
        renderSkills(skillsList)
    }

    private fun renderSkills(list: List<String>) {
        chipGroupSkills.removeAllViews()

        if (list.isEmpty()) {
            addChip("No skills", removable = false)
            return
        }

        list.forEach { addChip(it, removable = true) }
    }

    private fun addChip(text: String, removable: Boolean) {
        val chip = Chip(requireContext()).apply {
            this.text = text
            isClickable = false
            isCheckable = false

            if (removable) {
                isCloseIconVisible = true
                setOnCloseIconClickListener {
                    skillsList.removeIf { it.equals(text, ignoreCase = true) }
                    renderSkills(skillsList)
                }
            } else {
                isCloseIconVisible = false
            }
        }
        chipGroupSkills.addView(chip)
    }

    // ------------------ EDUCATION ------------------

    private fun openAddEducationDialogCompact() {

        fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

        fun field(title: String, hint: String): Pair<View, TextInputEditText> {
            val root = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
            }

            val tvTitle = TextView(requireContext()).apply {
                text = title
                textSize = 18f
                setTextColor(android.graphics.Color.parseColor("#616161"))
            }

            val et = TextInputEditText(requireContext()).apply {
                background = null
                textSize = 16f
                setPadding(0, dp(3), 0, dp(6))
                setTextColor(android.graphics.Color.parseColor("#111111"))
            }

            val line = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(1)
                )
                setBackgroundColor(android.graphics.Color.parseColor("#BDBDBD"))
            }

            val tvHint = TextView(requireContext()).apply {
                text = hint
                textSize = 12f
                setTextColor(android.graphics.Color.parseColor("#808080"))
                setPadding(0, dp(6), 0, 0)
            }

            root.addView(tvTitle)
            root.addView(et)
            root.addView(line)
            root.addView(tvHint)

            return Pair(root, et)
        }

        val scroll = NestedScrollView(requireContext()).apply {
            isFillViewport = true
        }

        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(16), dp(20), dp(4))
        }

        val header = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val icon = TextView(requireContext()).apply {
            text = "🎓"
            textSize = 16f
            setPadding(0, 0, dp(10), 0)
        }

        val texts = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
        }

        val tvTitle = TextView(requireContext()).apply {
            text = "Add Education"
            textSize = 18f
            setTextColor(android.graphics.Color.parseColor("#111827"))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        val tvSub = TextView(requireContext()).apply {
            text = "Ajoute un diplôme, école, et années."
            textSize = 12f
            setTextColor(android.graphics.Color.parseColor("#6B7280"))
        }

        texts.addView(tvTitle)
        texts.addView(tvSub)
        header.addView(icon)
        header.addView(texts)

        val (degreeView, etDegree) = field("Degree", "Ex: Bachelor of Computer Science")
        val (schoolView, etSchool) = field("School", "Ex: University of Technology")
        val (yearsView, etYears) = field("Years", "Ex: 2019 - 2023")

        container.addView(header)
        container.addView(spaceView(10))
        container.addView(degreeView)
        container.addView(spaceView(10))
        container.addView(schoolView)
        container.addView(spaceView(10))
        container.addView(yearsView)

        scroll.addView(container)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(scroll)
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("ADD", null)
            .create()

        dialog.setOnShowListener {
            dialog.window?.setLayout(dp(320), ViewGroup.LayoutParams.WRAP_CONTENT)

            val addBtn = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            addBtn.setOnClickListener {
                val degree = etDegree.text?.toString()?.trim().orEmpty()
                val school = etSchool.text?.toString()?.trim().orEmpty()
                val years = etYears.text?.toString()?.trim().orEmpty()

                if (degree.isBlank() || school.isBlank() || years.isBlank()) {
                    toast("Remplir tous les champs")
                    return@setOnClickListener
                }

                educationList.add(EducationItem(degree, school, years))
                renderEducation()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun renderEducation() {
        educationContainer.removeAllViews()

        if (educationList.isEmpty()) {
            val tv = TextView(requireContext()).apply {
                text = "No education yet"
                setTextColor(android.graphics.Color.parseColor("#8A94A6"))
            }
            educationContainer.addView(tv)
            return
        }

        educationList.forEachIndexed { index, item ->
            educationContainer.addView(createEducationRow(item, index))
        }
    }

    private fun createEducationRow(item: EducationItem, index: Int): View {
        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 8.dp(), 0, 8.dp())
        }

        val line = View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(3.dp(), LinearLayout.LayoutParams.MATCH_PARENT).apply {
                marginStart = 2.dp()
                marginEnd = 12.dp()
            }
            setBackgroundColor(android.graphics.Color.parseColor("#2563EB"))
        }

        val col = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvDegree = TextView(requireContext()).apply {
            text = item.degree
            setTextColor(android.graphics.Color.parseColor("#111827"))
            textSize = 14f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        val tvSchool = TextView(requireContext()).apply {
            text = item.school
            setTextColor(android.graphics.Color.parseColor("#6B7280"))
            textSize = 13f
        }

        val tvYears = TextView(requireContext()).apply {
            text = item.years
            setTextColor(android.graphics.Color.parseColor("#9CA3AF"))
            textSize = 12f
        }

        col.addView(tvDegree)
        col.addView(tvSchool)
        col.addView(tvYears)

        val btnRemove = TextView(requireContext()).apply {
            text = "✕"
            textSize = 14f
            setTextColor(android.graphics.Color.parseColor("#9CA3AF"))
            setPadding(12.dp(), 6.dp(), 6.dp(), 6.dp())
            setOnClickListener {
                educationList.removeAt(index)
                renderEducation()
            }
        }

        root.addView(line)
        root.addView(col)
        root.addView(btnRemove)

        return root
    }

    private fun spaceView(hDp: Int): View =
        Space(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(1, hDp.dp())
        }

    private fun Int.dp(): Int =
        (this * resources.displayMetrics.density).toInt()

    // ------------------ EXPERIENCE ------------------

    private fun toggleExperienceEdit() {
        isEditingExp = !isEditingExp
        if (isEditingExp) {
            val list = getExperienceFromText(tvExperienceList.text.toString())
            etExperience.setText(list.joinToString("\n"))

            tvExperienceList.visibility = View.GONE
            experienceEditLayout.visibility = View.VISIBLE
            btnEditExperience.text = "Cancel"
        } else {
            experienceEditLayout.visibility = View.GONE
            tvExperienceList.visibility = View.VISIBLE
            btnEditExperience.text = "Edit"
        }
    }

    // ------------------ SAVE ------------------

    private fun saveAll() {
        if (isEditingPro) {
            tvPosition.text = etPosition.text?.toString()?.trim().orEmpty().ifBlank { "—" }
            tvExperienceYears.text = etExperienceYears.text?.toString()?.trim().orEmpty().ifBlank { "—" }
            tvLocation.text = etLocation.text?.toString()?.trim().orEmpty().ifBlank { "—" }
            toggleProEdit()
        }

        if (isEditingExp) {
            val list = etExperience.text?.toString().orEmpty()
                .split("\n")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            tvExperienceList.text =
                if (list.isEmpty()) "Coming soon..."
                else list.joinToString("\n• ", prefix = "• ")

            toggleExperienceEdit()
        }

        val expList = getExperienceFromText(tvExperienceList.text.toString())

        val profile = CandidateProfile(
            name = tvName.text.toString(),
            email = tvEmail.text.toString(),
            proInfo = ProInfo(
                position = tvPosition.text.toString().replace("—", "").trim(),
                experienceYears = tvExperienceYears.text.toString().replace("—", "").trim(),
                location = tvLocation.text.toString().replace("—", "").trim()
            ),
            skills = skillsList.toList(),
            experiences = expList,
            education = educationList.map {
                EducationItemModel(
                    degree = it.degree,
                    school = it.school,
                    years = it.years
                )
            }
        )

        viewModel.saveProfile(profile)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is CandidateProfileState.Loading -> Unit

                    is CandidateProfileState.Success -> {
                        val profile = state.profile

                        tvName.text = profile.name.ifBlank { "Candidate" }
                        tvEmail.text = profile.email

                        tvPosition.text = profile.proInfo.position.ifBlank { "—" }
                        tvExperienceYears.text = profile.proInfo.experienceYears.ifBlank { "—" }
                        tvLocation.text = profile.proInfo.location.ifBlank { "—" }

                        skillsList.clear()
                        skillsList.addAll(profile.skills)
                        renderSkills(skillsList)

                        tvExperienceList.text =
                            if (profile.experiences.isEmpty()) "Coming soon..."
                            else profile.experiences.joinToString("\n• ", prefix = "• ")

                        educationList.clear()
                        educationList.addAll(
                            profile.education.map {
                                EducationItem(
                                    degree = it.degree,
                                    school = it.school,
                                    years = it.years
                                )
                            }
                        )
                        renderEducation()
                    }

                    is CandidateProfileState.Error -> {
                        toast(state.message)
                    }

                    else -> Unit
                }
            }
        }

        lifecycleScope.launch {
            viewModel.saveState.collect { state ->
                when (state) {
                    is CandidateProfileSaveState.Success -> toast("Saved ✔")
                    is CandidateProfileSaveState.Error -> toast(state.message)
                    else -> Unit
                }
            }
        }
    }

    private fun getExperienceFromText(text: String): List<String> {
        return text.lines()
            .map { it.replace("•", "").trim() }
            .filter { it.isNotEmpty() && it != "Coming soon..." }
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}