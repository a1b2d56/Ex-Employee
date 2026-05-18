package com.powergrid.exemployee.presentation.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.powergrid.exemployee.ExEmployeeApp
import com.powergrid.exemployee.R
import com.powergrid.exemployee.common.BaseFragment
import com.powergrid.exemployee.common.FontPrefs
import com.powergrid.exemployee.common.ThemePrefs
import com.powergrid.exemployee.databinding.FragmentSettingsBinding
import com.powergrid.exemployee.presentation.MainActivity
import com.powergrid.exemployee.presentation.auth.LoginActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : BaseFragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAccount()
        setupAppearance()
    }

    private fun setupAccount() {
        binding.layoutSignOut.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.sign_out_confirm_title)
                .setMessage(R.string.sign_out_confirm_message)
                .setPositiveButton(R.string.btn_sign_out) { _, _ ->
                    startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    private fun setupAppearance() {
        // Current theme display
        val currentTheme = ThemePrefs.getTheme(requireContext())
        binding.tvCurrentTheme.text = currentTheme.label

        // Current font size
        val currentScale = FontPrefs.getScale(requireContext())
        val scaleIdx = MainActivity.FONT_SCALES.indexOfFirst { it == currentScale }.takeIf { it >= 0 } ?: 1
        binding.tvCurrentFontSize.text = MainActivity.FONT_LABELS[scaleIdx]

        // Bold toggle
        binding.switchBold.isChecked = FontPrefs.isBold(requireContext())
        binding.switchBold.setOnCheckedChangeListener { _, isChecked ->
            FontPrefs.setBold(requireContext(), isChecked)
            requireActivity().recreate()
        }

        // Theme picker
        binding.layoutThemePicker.setOnClickListener { showThemePicker() }

        // Font size cycle
        binding.layoutFontSize.setOnClickListener {
            val nextIdx = (scaleIdx + 1) % MainActivity.FONT_SCALES.size
            FontPrefs.setScale(requireContext(), MainActivity.FONT_SCALES[nextIdx])
            requireActivity().recreate()
        }
    }

    private fun showThemePicker() {
        val themes = ThemePrefs.AppTheme.entries.toTypedArray()
        val labels = themes.map { it.label }.toTypedArray()
        val currentIdx = themes.indexOf(ThemePrefs.getTheme(requireContext()))

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.settings_choose_theme)
            .setSingleChoiceItems(labels, currentIdx) { dialog, which ->
                dialog.dismiss()
                val chosen = themes[which]
                ThemePrefs.setTheme(requireContext(), chosen)
                (requireActivity().application as ExEmployeeApp).applyTheme()
                requireActivity().recreate()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
