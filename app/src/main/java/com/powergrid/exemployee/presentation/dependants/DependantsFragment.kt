package com.powergrid.exemployee.presentation.dependants

import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.powergrid.exemployee.common.*
import com.powergrid.exemployee.databinding.FragmentListBinding
import com.powergrid.exemployee.databinding.ItemDependantBinding
import com.powergrid.exemployee.domain.model.Dependant
import com.powergrid.exemployee.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DependantsFragment : BaseFragment() {
    private var _b: FragmentListBinding? = null
    private val b get() = _b!!
    private val vm: DependantsViewModel by viewModels()

    private val adapter = GenericListAdapter(
        inflate            = { inf, p, a -> ItemDependantBinding.inflate(inf, p, a) },
        areItemsTheSame    = { o: Dependant, n -> o.id == n.id },
        areContentsTheSame = { o: Dependant, n -> o == n },
    ) { binding, dep ->
        binding.tvDepName.text     = dep.name
        binding.tvDepRelation.text = dep.relation
        binding.tvDepAge.text      = "Age: ${dep.age}"
        binding.chipDepStatus.text = dep.status.replaceFirstChar { it.uppercase() }
        binding.chipDepStatus.setChipBackgroundColorResource(
            if (dep.status == "active") com.powergrid.exemployee.R.color.status_active
            else com.powergrid.exemployee.R.color.status_inactive
        )
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentListBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)
        b.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        b.recyclerView.adapter = adapter
        val token = (requireActivity() as MainActivity).authToken
        vm.load(token)
        collectFlow(vm.items) { state ->
            when (state) {
                is UiState.Loading -> { b.progress.visible(); b.recyclerView.gone() }
                is UiState.Success -> { b.progress.gone(); b.recyclerView.visible(); adapter.submitList(state.data) }
                is UiState.Error   -> { b.progress.gone(); toast(state.message) }
                UiState.Idle       -> Unit
            }
        }
        b.swipeRefresh.setOnRefreshListener { vm.load(token); b.swipeRefresh.isRefreshing = false }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
