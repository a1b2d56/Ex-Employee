package com.powergrid.exemployee.presentation.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.powergrid.exemployee.BuildConfig
import com.powergrid.exemployee.common.BaseFragment
import com.powergrid.exemployee.databinding.FragmentAboutBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutFragment : BaseFragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvVersion.text = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
