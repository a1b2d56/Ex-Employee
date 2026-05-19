package com.powergrid.exemployee.presentation.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.powergrid.exemployee.ExEmployeeApp
import com.powergrid.exemployee.R
import com.powergrid.exemployee.common.BaseFragment
import com.powergrid.exemployee.common.FontPrefs
import com.powergrid.exemployee.common.ThemePrefs
import com.powergrid.exemployee.databinding.FragmentSettingsBinding
import com.powergrid.exemployee.presentation.MainActivity
import com.powergrid.exemployee.presentation.auth.LoginActivity
import com.powergrid.exemployee.security.BiometricHelper
import com.powergrid.exemployee.security.BiometricResult
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : BaseFragment() {

    @Inject lateinit var biometric: BiometricHelper

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
        val hasSecret = biometric.hasStoredSecret()
        binding.switchBiometric.isChecked = hasSecret
        binding.switchBiometric.isEnabled = biometric.isAvailable()

        binding.switchBiometric.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked && !hasSecret) {
                val token = (requireActivity() as MainActivity).authToken
                if (token.isEmpty()) {
                    buttonView.isChecked = false
                    return@setOnCheckedChangeListener
                }
                biometric.promptToEncryptAndStore(requireActivity(), token) { result ->
                    if (result !is BiometricResult.Success) {
                        buttonView.isChecked = false
                    }
                }
            } else if (!isChecked && hasSecret) {
                biometric.clearSecret()
            }
        }

        binding.layoutSignOut.setOnClickListener {
            val dialog = BottomSheetDialog(requireContext())
            val view = layoutInflater.inflate(R.layout.layout_bottom_sheet_signout, null)
            dialog.setContentView(view)

            view.findViewById<Button>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
            view.findViewById<Button>(R.id.btnSignOut).setOnClickListener {
                dialog.dismiss()
                startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
            dialog.show()
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

        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.layout_bottom_sheet_theme, null)
        dialog.setContentView(view)

        val container = view.findViewById<LinearLayout>(R.id.layoutThemesContainer)
        themes.forEachIndexed { index, theme ->
            val tv = layoutInflater.inflate(android.R.layout.simple_list_item_single_choice, container, false) as android.widget.CheckedTextView
            tv.text = theme.label
            tv.isChecked = (index == currentIdx)
            // Style it to match Material You
            tv.setPadding(64, 48, 64, 48)
            tv.setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Subhead)
            tv.setOnClickListener {
                dialog.dismiss()
                ThemePrefs.setTheme(requireContext(), theme)
                (requireActivity().application as ExEmployeeApp).applyTheme()
                requireActivity().recreate()
            }
            container.addView(tv)
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
