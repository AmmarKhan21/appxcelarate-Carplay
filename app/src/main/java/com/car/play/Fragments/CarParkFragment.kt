package com.car.play.android.app.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.NavHostFragment
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentCarParkBinding

class CarParkFragment : Fragment() {
    private lateinit var googleAds: GoogleAds
    private val binding by lazy { FragmentCarParkBinding.inflate(layoutInflater) }
    private val mController by lazy {
        (requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        googleAds = GoogleAds()
        googleAds.CheckNative(this, binding.nativeAd)
        setupClickEvent()
        handleBackPress()
        return binding.root
    }


    private fun setupClickEvent() {
        binding.icBack.setOnClickListener { mController.popBackStack() }
        binding.lvParkCar.setOnClickListener { mController.navigate(R.id.action_carParkFragment_to_parkCarFragment) }
        binding.lvFindCar.setOnClickListener { mController.navigate(R.id.action_carParkFragment_to_findParkCarFragment) }
    }



    private fun handleBackPress() {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onbackpressedEvent()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )
    }

    private fun onbackpressedEvent() {
        mController.navigate(R.id.action_carParkFragment_to_homeFragment)
    }

}