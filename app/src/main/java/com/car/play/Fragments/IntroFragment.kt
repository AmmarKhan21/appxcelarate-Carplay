package com.car.play.android.app.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.car.play.GoogleAds.GoogleAds
import com.google.android.material.tabs.TabLayout
import com.car.play.android.app.Adapters.IntroAdapter
import com.car.play.android.app.R
import com.car.play.android.app.data_classes.Intro
import com.car.play.android.app.databinding.FragmentIntroBinding

class IntroFragment : Fragment() {
    private val binding by lazy { FragmentIntroBinding.inflate(layoutInflater) }
    private val mController by lazy {
        (requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
    }
    private lateinit var mList: List<Intro>
    private lateinit var introViewPagerAdapter: IntroAdapter
    private var position: Int = 0
    private lateinit var googleAds: GoogleAds

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setupView()
        setupClickListeners()
        googleAds = GoogleAds()
        googleAds.CheckNative(this@IntroFragment, binding.nativeAd)
        return binding.root
    }

    private fun setupView() {
        mList = listOf(
            Intro(
                "Connect Your Car",
                "Seamlessly connect your phone to your car via WiFi, Bluetooth, USB, or screen casting.",
                R.drawable.iv_intro1
            ),
            Intro(
                "Track Everything",
                "Monitor fuel, expenses, maintenance, trips, and driving score all in one place.",
                R.drawable.iv_intro2
            ),
            Intro(
                "Drive Smarter",
                "Get AI assistance, voice commands, emergency SOS, and predictive maintenance alerts.",
                R.drawable.iv_intro1
            ),
        )

        introViewPagerAdapter = IntroAdapter(requireContext(), mList)
        binding.screenViewpager.adapter = introViewPagerAdapter
        binding.tabIndicator.setupWithViewPager(binding.screenViewpager)
        binding.dotsIndicator.setViewPager(binding.screenViewpager)

        binding.btnNext.setOnClickListener {
            position = binding.screenViewpager.currentItem
            if (position < mList.size) {
                position++
                binding.screenViewpager.currentItem = position
            }
            if (position == mList.size - 1) {
                loadLastScreen()
            }
        }

        binding.tabIndicator.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == mList.size - 1) {
                    loadLastScreen()
                } else {
                    loadFirstScreen()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupClickListeners() {
        binding.txtSkip.setOnClickListener { startHome() }
        binding.btnGetStarted.setOnClickListener { startHome() }
    }

    private fun loadLastScreen() {
        binding.btnNext.visibility = View.INVISIBLE
        binding.btnGetStarted.visibility = View.VISIBLE
        binding.txtSkip.visibility = View.INVISIBLE
    }

    private fun loadFirstScreen() {
        binding.btnNext.visibility = View.VISIBLE
        binding.btnGetStarted.visibility = View.INVISIBLE
        binding.txtSkip.visibility = View.VISIBLE
    }

    private fun startHome() {
        mController.navigate(R.id.action_introFragmetn_to_homeFragment)
    }
}
