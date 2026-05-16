package com.powergrid.exemployee.presentation.liveliness

import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.powergrid.exemployee.common.*
import com.powergrid.exemployee.databinding.FragmentLivelinessBinding
import com.powergrid.exemployee.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LivelinessFragment : BaseFragment() {
    private var _b: FragmentLivelinessBinding? = null
    private val b get() = _b!!
    private val vm: LivelinessViewModel by viewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentLivelinessBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)
        val token = (requireActivity() as MainActivity).authToken
        b.btnCapturePhoto.setOnClickListener { toast("TODO: Launch camera intent and attach photo") }
        b.btnSubmitLiveliness.setOnClickListener { vm.submit(token) }
        collectFlow(vm.submitState) { state ->
            when (state) {
                is UiState.Loading -> { b.progress.visible(); b.btnSubmitLiveliness.isEnabled = false }
                is UiState.Success -> {
                    b.progress.gone(); b.btnSubmitLiveliness.isEnabled = true
                    Snackbar.make(b.root, state.data, Snackbar.LENGTH_LONG).show()
                    vm.reset()
                }
                is UiState.Error -> { b.progress.gone(); b.btnSubmitLiveliness.isEnabled = true; toast(state.message) }
                UiState.Idle     -> b.progress.gone()
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
