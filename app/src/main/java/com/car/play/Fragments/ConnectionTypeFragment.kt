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
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentConnectionTypeBinding

class ConnectionTypeFragment : Fragment() {
    private lateinit var googleAds: GoogleAds
    private val mController by lazy { (requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController }
    private val binding by lazy { FragmentConnectionTypeBinding.inflate(layoutInflater) }
    private var connectionType: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        googleAds= GoogleAds()
        googleAds.CheckNative(this@ConnectionTypeFragment,binding.nativeAd)
        initView()
        setupClickListeners()
        return binding.root
    }



    private fun initView() {
        connectionType = arguments?.getString("connectionType") ?: "Unknown"
        val intentIcon = arguments?.getInt("icon", 0) ?: 0
        val intentImage = arguments?.getInt("image", 0) ?: 0
        binding.ivIcon.setImageResource(intentIcon)
        binding.ivCar.setImageResource(intentImage)
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            mController.popBackStack()
        }

        binding.ivConnect.setOnClickListener {
            when (connectionType) {
                "wifi" -> openSettings(Settings.ACTION_WIFI_SETTINGS)
                "bluetooth" -> openSettings(Settings.ACTION_BLUETOOTH_SETTINGS)
                "cast" -> openSettings(Settings.ACTION_CAST_SETTINGS)
                "usb" -> handleUsbConnection()
            }
        }
    }

    private fun openSettings(action: String) {
        try {
            startActivity(Intent(action))
        } catch (e: SecurityException) {
            Toast.makeText(
                requireContext(),
                "Permission denied to open settings: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()  // Log the exception for debugging purposes
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "An error occurred: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
        }
    }

    /*  private fun openSettings(action: String) {
          startActivity(Intent(action))
      }*/

    private fun handleUsbConnection() {
        if (isDeveloperOptionsEnabled()) {
            openSettings(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.txt_developer),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun isDeveloperOptionsEnabled(): Boolean {
        return try {
            Settings.Global.getInt(
                requireContext().contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                0
            ) != 0
        } catch (e: Settings.SettingNotFoundException) {
            false
        }
    }
}