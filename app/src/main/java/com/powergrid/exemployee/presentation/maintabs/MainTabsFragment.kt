package com.powergrid.exemployee.presentation.maintabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.powergrid.exemployee.R
import com.powergrid.exemployee.common.BaseFragment
import com.powergrid.exemployee.databinding.FragmentMainTabsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainTabsFragment : BaseFragment() {

    private var _binding: FragmentMainTabsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMainTabsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Apply edge-to-edge window insets padding to the bottom navigation
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNav) { v, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, sysBars.bottom)
            insets
        }

        binding.viewPager.adapter = MainTabsAdapter(this)

        // Prevent ViewPager overscroll glow and configure animation
        binding.viewPager.getChildAt(0).overScrollMode = View.OVER_SCROLL_NEVER

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val menuId = when (position) {
                    0 -> R.id.homeFragment
                    1 -> R.id.noticeboardFragment
                    2 -> R.id.dependantsFragment
                    3 -> R.id.livelinessFragment
                    else -> return
                }
                if (binding.bottomNav.selectedItemId != menuId) {
                    binding.bottomNav.selectedItemId = menuId
                }

                val titleRes = when (position) {
                    0 -> R.string.title_home
                    1 -> R.string.title_noticeboard
                    2 -> R.string.title_dependants
                    3 -> R.string.title_liveliness
                    else -> R.string.title_home
                }
                (requireActivity() as androidx.appcompat.app.AppCompatActivity).supportActionBar?.setTitle(titleRes)
            }
        })

        binding.bottomNav.setOnItemSelectedListener { item ->
            val position = when (item.itemId) {
                R.id.homeFragment -> 0
                R.id.noticeboardFragment -> 1
                R.id.dependantsFragment -> 2
                R.id.livelinessFragment -> 3
                R.id.nav_drawer_toggle -> {
                    (requireActivity() as com.powergrid.exemployee.presentation.MainActivity).openDrawer()
                    return@setOnItemSelectedListener false
                }
                else -> return@setOnItemSelectedListener false
            }
            if (binding.viewPager.currentItem != position) {
                binding.viewPager.setCurrentItem(position, true)
            }
            true
        }
    }

    fun switchToTab(index: Int) {
        if (binding.viewPager.currentItem != index) {
            binding.viewPager.setCurrentItem(index, true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
