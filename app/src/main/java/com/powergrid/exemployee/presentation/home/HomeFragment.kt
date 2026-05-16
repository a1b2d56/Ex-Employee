package com.powergrid.exemployee.presentation.home

import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import coil.load
import coil.transform.CircleCropTransformation
import com.powergrid.exemployee.R
import com.powergrid.exemployee.common.*
import com.powergrid.exemployee.databinding.FragmentHomeBinding
import com.powergrid.exemployee.domain.model.Employee
import com.powergrid.exemployee.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment() {
    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!
    private val vm: HomeViewModel by viewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentHomeBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)
        val token = (requireActivity() as MainActivity).authToken
        vm.loadEmployee(token)
        if (FontPrefs.isBold(requireContext())) applyBold()

        collectFlow(vm.employee) { state ->
            when (state) {
                is UiState.Loading -> { b.progressHome.visible(); b.cardEmployee.gone(); b.tvError.gone() }
                is UiState.Success -> { b.progressHome.gone(); b.cardEmployee.visible(); bindEmployee(state.data) }
                is UiState.Error   -> { b.progressHome.gone(); b.tvError.visible(); b.tvError.text = state.message }
                UiState.Idle       -> Unit
            }
        }
    }

    private fun bindEmployee(e: Employee) {
        b.tvName.text        = e.name
        b.tvDesignation.text = e.designation
        b.tvDepartment.text  = e.department
        b.tvEmployeeId.text  = e.employeeId
        b.tvAge.text         = "${e.age} yrs"
        b.tvDob.text         = e.dob
        b.tvEmail.text       = e.email
        b.tvPhone.text       = e.phone
        if (!e.photoUrl.isNullOrBlank()) {
            b.ivProfile.load(e.photoUrl) {
                placeholder(R.drawable.ic_person_placeholder)
                error(R.drawable.ic_person_placeholder)
                transformations(CircleCropTransformation())
            }
        } else b.ivProfile.setImageResource(R.drawable.ic_person_placeholder)
    }

    private fun applyBold() {
        listOf(b.tvName, b.tvDesignation, b.tvDepartment, b.tvEmployeeId, b.tvAge, b.tvDob, b.tvEmail, b.tvPhone)
            .forEach { it.setTypeface(it.typeface, Typeface.BOLD) }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
