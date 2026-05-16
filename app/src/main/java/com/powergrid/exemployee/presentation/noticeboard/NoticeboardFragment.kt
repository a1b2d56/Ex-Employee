package com.powergrid.exemployee.presentation.noticeboard

import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.powergrid.exemployee.common.*
import com.powergrid.exemployee.databinding.FragmentListBinding
import com.powergrid.exemployee.databinding.ItemNoticeBinding
import com.powergrid.exemployee.domain.model.Notice
import com.powergrid.exemployee.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoticeboardFragment : BaseFragment() {
    private var _b: FragmentListBinding? = null
    private val b get() = _b!!
    private val vm: NoticeboardViewModel by viewModels()

    private val adapter = GenericListAdapter(
        inflate            = { inf, p, a -> ItemNoticeBinding.inflate(inf, p, a) },
        areItemsTheSame    = { o: Notice, n -> o.id == n.id },
        areContentsTheSame = { o: Notice, n -> o == n },
    ) { binding, notice ->
        binding.tvNoticeTitle.text   = notice.title
        binding.tvNoticeDate.text    = notice.date
        binding.tvNoticeContent.text = notice.content
        binding.chipUrgent.visibility = if (notice.urgent) View.VISIBLE else View.GONE
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
        collectFlow(vm.notices) { state ->
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
