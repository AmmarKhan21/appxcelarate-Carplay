package com.car.play.android.app.Fragments

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentConnectBinding

class ConnectFragment : Fragment() {
    private lateinit var googleAds: GoogleAds
    private val binding by lazy { FragmentConnectBinding.inflate(layoutInflater) }
    private val mController by lazy { (requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        googleAds = GoogleAds()
        googleAds.CheckNative(this, binding.nativeAd)
        setupClickListeners()
        return binding.root
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.ivWifi.setOnClickListener {
            navigateToConnectingFragment("wifi", R.drawable.ic_wifi, R.drawable.img_wifi)
        }

        binding.ivBluetooth.setOnClickListener {
            navigateToConnectingFragment("bluetooth", R.drawable.bluetooth1, R.drawable.img_bluetooth)
        }

        binding.ivCast.setOnClickListener {
            navigateToConnectingFragment("cast", R.drawable.cast1, R.drawable.img_cast)
        }

        binding.ivUsb.setOnClickListener {
            navigateToConnectingFragment("usb", R.drawable.usb1, R.drawable.img_bluetooth)
        }
    }

    private fun navigateToConnectingFragment(connectionType: String, iconResId: Int, imageResId: Int) {
        val bundle = Bundle().apply {
            putString("connectionType", connectionType)
            putInt("icon", iconResId)
            putInt("image", imageResId)
        }
        mController.navigate(R.id.action_connectfragment_to_connectiontype, bundle)
    }
}