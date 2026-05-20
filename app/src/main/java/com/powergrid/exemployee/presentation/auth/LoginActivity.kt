package com.powergrid.exemployee.presentation.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.powergrid.exemployee.common.BaseActivity
import com.powergrid.exemployee.common.UiState
import com.powergrid.exemployee.common.collectFlow
import com.powergrid.exemployee.databinding.ActivityLoginBinding
import com.powergrid.exemployee.domain.model.CaptchaData
import com.powergrid.exemployee.presentation.MainActivity
import com.powergrid.exemployee.security.BiometricHelper
import com.powergrid.exemployee.security.BiometricResult
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val vm: LoginViewModel by viewModels()
    @Inject lateinit var biometric: BiometricHelper

    private var currentCaptcha: CaptchaData? = null
    private var currentTab = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupTabs()
        setupPasswordTab()
        setupOtpTab()
        autoLoginCheck()
        setupBypassButton()
        observeViewModels()
        vm.loadCaptcha()
    }

    private fun setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Password Login"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("OTP Login"))
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentTab = tab.position
                binding.layoutPasswordTab.visibility = if (currentTab == 0) View.VISIBLE else View.GONE
                binding.layoutOtpTab.visibility      = if (currentTab == 1) View.VISIBLE else View.GONE
                vm.loadCaptcha()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })
    }

    private fun setupPasswordTab() {
        binding.btnRefreshCaptchaPassword.setOnClickListener { vm.loadCaptcha() }
        binding.btnLoginPassword.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val answer   = binding.etCaptchaPassword.text.toString().toIntOrNull() ?: -1
            val captcha  = currentCaptcha ?: run { snack("Captcha not loaded"); return@setOnClickListener }
            var valid = true
            if (username.isBlank()) { binding.tilUsername.error = "Required"; valid = false } else binding.tilUsername.error = null
            if (password.isBlank()) { binding.tilPassword.error = "Required"; valid = false } else binding.tilPassword.error = null
            if (answer != captcha.answer) { binding.tilCaptchaPassword.error = "Incorrect answer"; valid = false } else binding.tilCaptchaPassword.error = null
            if (!valid) return@setOnClickListener
            vm.loginPassword(username, password, captcha.token, answer)
        }
    }

    private fun setupOtpTab() {
        binding.btnRefreshCaptchaOtp.setOnClickListener { vm.loadCaptcha() }
        binding.btnSendOtp.setOnClickListener {
            val username = binding.etOtpUsername.text.toString().trim()
            val answer   = binding.etCaptchaOtp.text.toString().toIntOrNull() ?: -1
            val captcha  = currentCaptcha ?: run { snack("Captcha not loaded"); return@setOnClickListener }
            var valid = true
            if (username.isBlank()) { binding.tilOtpUsername.error = "Required"; valid = false } else binding.tilOtpUsername.error = null
            if (answer != captcha.answer) { binding.tilCaptchaOtp.error = "Incorrect answer"; valid = false } else binding.tilCaptchaOtp.error = null
            if (!valid) return@setOnClickListener
            vm.sendOtp(username, captcha.token, answer)
        }
        binding.btnVerifyOtp.setOnClickListener {
            val username = binding.etOtpUsername.text.toString().trim()
            val otp      = binding.etOtpCode.text.toString().trim()
            if (otp.length < 4) { binding.tilOtpCode.error = "Enter OTP"; return@setOnClickListener }
            binding.tilOtpCode.error = null
            vm.verifyOtp(username, otp)
        }
    }

    private fun autoLoginCheck() {
        if (biometric.isAvailable() && biometric.isEnabled() && biometric.hasStoredSecret()) {
            biometric.promptToDecrypt(this) { result ->
                when (result) {
                    is BiometricResult.Success  -> navigateToMain(String(result.data, Charsets.UTF_8))
                    is BiometricResult.Error    -> snack(result.message)
                    BiometricResult.Cancelled   -> Unit
                    BiometricResult.NotAvailable -> Unit
                }
            }
        } else {
            val savedToken = com.powergrid.exemployee.common.AuthPrefs.getToken(this)
            if (!savedToken.isNullOrEmpty()) {
                navigateToMain(savedToken)
            }
        }
    }

    // TODO: Remove this button before production build
    private fun setupBypassButton() {
        binding.btnDevBypass.setOnClickListener { navigateToMain("dev_bypass_token") }
    }

    private fun observeViewModels() {
        collectFlow(vm.captcha) { state ->
            when (state) {
                is UiState.Loading -> { binding.tvCaptchaQuestion.text = "Loading…"; binding.tvCaptchaQuestionOtp.text = "Loading…" }
                is UiState.Success -> {
                    currentCaptcha = state.data
                    binding.tvCaptchaQuestion.text    = state.data.question
                    binding.tvCaptchaQuestionOtp.text = state.data.question
                    binding.etCaptchaPassword.text?.clear()
                    binding.etCaptchaOtp.text?.clear()
                }
                is UiState.Error -> { binding.tvCaptchaQuestion.text = "Error"; snack(state.message) }
                UiState.Idle     -> Unit
            }
        }
        collectFlow(vm.loginState) { state ->
            when (state) {
                is UiState.Loading -> showProgress(true)
                is UiState.Success -> {
                    showProgress(false)
                    if (!biometric.hasStoredSecret()) {
                        com.powergrid.exemployee.common.AuthPrefs.saveToken(this, state.data)
                    }
                    navigateToMain(state.data)
                }
                is UiState.Error   -> { showProgress(false); snack(state.message); vm.resetLogin(); vm.loadCaptcha() }
                UiState.Idle       -> showProgress(false)
            }
        }
        collectFlow(vm.otpSent) { state ->
            when (state) {
                is UiState.Loading -> showProgress(true)
                is UiState.Success -> {
                    showProgress(false)
                    snack("OTP sent successfully")
                    binding.layoutOtpEntry.visibility = View.VISIBLE
                    binding.btnSendOtp.isEnabled      = false
                    vm.resetOtpSent(); vm.loadCaptcha()
                }
                is UiState.Error -> { showProgress(false); snack(state.message); vm.resetOtpSent(); vm.loadCaptcha() }
                UiState.Idle     -> showProgress(false)
            }
        }
        collectFlow(vm.otpVerify) { state ->
            when (state) {
                is UiState.Loading -> showProgress(true)
                is UiState.Success -> {
                    showProgress(false)
                    if (!biometric.hasStoredSecret()) {
                        com.powergrid.exemployee.common.AuthPrefs.saveToken(this, state.data)
                    }
                    navigateToMain(state.data)
                }
                is UiState.Error   -> { showProgress(false); snack(state.message); vm.resetOtpVerify() }
                UiState.Idle       -> showProgress(false)
            }
        }
    }

    private fun navigateToMain(token: String) {
        startActivity(Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_TOKEN, token)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }
    private fun showProgress(show: Boolean) { binding.progressLogin.visibility = if (show) View.VISIBLE else View.GONE }
    private fun snack(msg: String) = Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
}
