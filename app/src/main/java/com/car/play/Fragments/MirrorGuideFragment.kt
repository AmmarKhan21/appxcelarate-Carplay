package com.car.play.android.app.Fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentMirrorGuideBinding

class MirrorGuideFragment : Fragment() {

    private lateinit var googleAds: GoogleAds
    private val binding by lazy { FragmentMirrorGuideBinding.inflate(layoutInflater) }
    private val mController by lazy {
        (requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
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
            mController.popBackStack()
        }

        binding.btnMiracastLearn.setOnClickListener {
            toggleDetails(binding.miracastDetails)
        }

        binding.btnCastLearn.setOnClickListener {
            toggleDetails(binding.castDetails)
        }

        binding.btnUsbLearn.setOnClickListener {
            toggleDetails(binding.usbDetails)
        }

        binding.btnAdapterLearn.setOnClickListener {
            toggleDetails(binding.adapterDetails)
        }

        binding.btnAppScreenCast.setOnClickListener {
            openPlayStore("com.screencast.mirror")
        }

        binding.btnAppMiracast.setOnClickListener {
            openPlayStore("com.miracast.wifi.display")
        }

        binding.btnAppGoogleHome.setOnClickListener {
            openPlayStore("com.google.android.apps.chromecast.app")
        }
    }

    private fun toggleDetails(detailsView: View) {
        detailsView.visibility = if (detailsView.visibility == View.VISIBLE) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun openPlayStore(packageName: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }
}
