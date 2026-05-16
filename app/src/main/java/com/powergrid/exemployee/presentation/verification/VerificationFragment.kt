package com.powergrid.exemployee.presentation.verification

import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.powergrid.exemployee.common.*
import com.powergrid.exemployee.databinding.FragmentListBinding
import com.powergrid.exemployee.databinding.ItemVerificationBinding
import com.powergrid.exemployee.domain.model.VerificationDoc
import com.powergrid.exemployee.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VerificationFragment : BaseFragment() {
    private var _b: FragmentListBinding? = null
    private val b get() = _b!!
    private val vm: VerificationViewModel by viewModels()

    private val adapter = GenericListAdapter(
        inflate            = { inf, p, a -> ItemVerificationBinding.inflate(inf, p, a) },
        areItemsTheSame    = { o: VerificationDoc, n -> o.id == n.id },
        areContentsTheSame = { o: VerificationDoc, n -> o == n },
    ) { binding, doc ->
        binding.tvDocType.text    = doc.docType
        binding.tvVerifiedOn.text = doc.verifiedOn?.let { "Verified: $it" } ?: ""
        binding.tvRemarks.text    = doc.remarks ?: ""
        binding.tvRemarks.visibility = if (doc.remarks != null) View.VISIBLE else View.GONE
        val (label, color) = when (doc.status) {
            "verified" -> "✓ Verified" to com.powergrid.exemployee.R.color.status_verified
            "rejected" -> "✗ Rejected" to com.powergrid.exemployee.R.color.status_rejected
            else       -> "⏳ Pending"  to com.powergrid.exemployee.R.color.status_pending
        }
        binding.chipDocStatus.text = label
        binding.chipDocStatus.setChipBackgroundColorResource(color)
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
