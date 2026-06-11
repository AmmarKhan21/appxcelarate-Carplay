package com.car.play.android.app.Fragments

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentConnectionGuideBinding
import com.intuit.sdp.R as sdpR

class ConnectionGuideFragment : Fragment() {

    private lateinit var googleAds: GoogleAds
    private val binding by lazy { FragmentConnectionGuideBinding.inflate(layoutInflater) }
    private val mController by lazy {
        (requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
    }
    private var connectionType: String = "wifi"

    data class GuideStep(val number: Int, val title: String, val description: String)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        googleAds = GoogleAds()
        googleAds.CheckNative(this, binding.nativeAd)

        connectionType = arguments?.getString("connectionType") ?: "wifi"
        setupHeader()
        populateSteps()
        setupClickListeners()
        return binding.root
    }

    private fun setupHeader() {
        when (connectionType) {
            "wifi" -> {
                binding.tvConnectionType.text = "WiFi Connection"
                binding.ivConnectionIcon.setImageResource(R.drawable.ic_wifi)
            }
            "bluetooth" -> {
                binding.tvConnectionType.text = "Bluetooth Connection"
                binding.ivConnectionIcon.setImageResource(R.drawable.bluetooth1)
            }
            "usb" -> {
                binding.tvConnectionType.text = "USB Connection"
                binding.ivConnectionIcon.setImageResource(R.drawable.usb1)
            }
            "cast" -> {
                binding.tvConnectionType.text = "Screen Cast"
                binding.ivConnectionIcon.setImageResource(R.drawable.cast1)
            }
        }
    }

    private fun getSteps(): List<GuideStep> {
        return when (connectionType) {
            "wifi" -> listOf(
                GuideStep(1, "Open WiFi Settings", "Go to your phone's WiFi settings to begin the connection process."),
                GuideStep(2, "Find Your Car's WiFi", "Look for your car's WiFi network in the available networks list. It usually starts with the car brand name."),
                GuideStep(3, "Enter Password", "Enter the WiFi password shown on your car's infotainment display. Check your car's manual if needed."),
                GuideStep(4, "Connect & Enjoy", "Once connected, return to the app and enjoy wireless connectivity with your car.")
            )
            "bluetooth" -> listOf(
                GuideStep(1, "Enable Bluetooth", "Make sure Bluetooth is turned on in your phone's settings."),
                GuideStep(2, "Put Car in Pairing Mode", "Go to your car's infotainment system and enable Bluetooth pairing/discovery mode."),
                GuideStep(3, "Search for Devices", "On your phone, tap 'Scan' or 'Search for devices' to find available Bluetooth devices."),
                GuideStep(4, "Select Your Car", "Find your car's name in the list of available devices and tap on it to pair."),
                GuideStep(5, "Confirm Pairing", "Confirm the pairing code on both your phone and car display to complete the connection.")
            )
            "usb" -> listOf(
                GuideStep(1, "Get a Compatible Cable", "Use a high-quality USB data cable (not a charge-only cable). USB-C or Lightning depending on your phone."),
                GuideStep(2, "Connect to Car USB Port", "Plug one end into your phone and the other into your car's USB port. Use the media/data port, not the charging port."),
                GuideStep(3, "Allow USB Debugging", "If prompted, allow USB debugging or data transfer on your phone. You may need to enable Developer Options first."),
                GuideStep(4, "Select Connection Mode", "Choose the appropriate connection mode on your phone — select 'File Transfer' or 'MTP' for best compatibility.")
            )
            "cast" -> listOf(
                GuideStep(1, "Check Car Compatibility", "Ensure your car's infotainment system supports screen casting (Miracast, Google Cast, or similar)."),
                GuideStep(2, "Open Cast Settings", "Go to your phone's Settings > Connected Devices > Cast, or pull down the notification shade and tap Cast."),
                GuideStep(3, "Select Your Car Display", "Choose your car's display from the list of available cast devices."),
                GuideStep(4, "Start Casting", "Once connected, your phone screen will be mirrored to your car's display. Open the app to use it on the big screen.")
            )
            else -> emptyList()
        }
    }

    private fun populateSteps() {
        val steps = getSteps()
        binding.stepsContainer.removeAllViews()

        for (step in steps) {
            val stepView = createStepView(step)
            binding.stepsContainer.addView(stepView)
        }
    }

    private fun createStepView(step: GuideStep): View {
        val context = requireContext()

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = resources.getDimensionPixelSize(sdpR.dimen._10sdp)
            }
            background = ContextCompat.getDrawable(context, R.drawable.card_bg)
            setPadding(
                resources.getDimensionPixelSize(sdpR.dimen._12sdp),
                resources.getDimensionPixelSize(sdpR.dimen._12sdp),
                resources.getDimensionPixelSize(sdpR.dimen._12sdp),
                resources.getDimensionPixelSize(sdpR.dimen._12sdp)
            )
        }

        val numberCircle = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                resources.getDimensionPixelSize(sdpR.dimen._30sdp),
                resources.getDimensionPixelSize(sdpR.dimen._30sdp)
            ).apply {
                marginEnd = resources.getDimensionPixelSize(sdpR.dimen._10sdp)
            }
            text = step.number.toString()
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, R.color.white))
            gravity = android.view.Gravity.CENTER
            background = ContextCompat.getDrawable(context, R.drawable.edittext_bg)
        }

        val textContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val titleTv = TextView(context).apply {
            text = step.title
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, R.color.white))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        val descTv = TextView(context).apply {
            text = step.description
            textSize = 12f
            setTextColor(0xFFBBBBBB.toInt())
            setPadding(0, resources.getDimensionPixelSize(sdpR.dimen._3sdp), 0, 0)
        }

        textContainer.addView(titleTv)
        textContainer.addView(descTv)
        container.addView(numberCircle)
        container.addView(textContainer)

        return container
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            mController.popBackStack()
        }

        binding.btnOpenSettings.setOnClickListener {
            when (connectionType) {
                "wifi" -> openSettings(Settings.ACTION_WIFI_SETTINGS)
                "bluetooth" -> openSettings(Settings.ACTION_BLUETOOTH_SETTINGS)
                "usb" -> openSettings(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                "cast" -> openSettings(Settings.ACTION_CAST_SETTINGS)
            }
        }
    }

    private fun openSettings(action: String) {
        try {
            startActivity(Intent(action))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Unable to open settings: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
