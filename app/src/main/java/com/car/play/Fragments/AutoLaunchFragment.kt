package com.car.play.android.app.Fragments

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentAutoLaunchBinding
import com.intuit.sdp.R as sdpR

class AutoLaunchFragment : Fragment() {

    private lateinit var googleAds: GoogleAds
    private val binding by lazy { FragmentAutoLaunchBinding.inflate(layoutInflater) }
    private val mController by lazy {
        (requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
    }
    private lateinit var prefs: SharedPreferences
    private var selectedRadio: RadioButton? = null

    companion object {
        const val PREFS_NAME = "auto_launch_prefs"
        const val KEY_ENABLED = "auto_launch_enabled"
        const val KEY_DEVICE_MAC = "auto_launch_device_mac"
        const val KEY_DEVICE_NAME = "auto_launch_device_name"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        googleAds = GoogleAds()
        googleAds.CheckNative(this, binding.nativeAd)
        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        setupToggle()
        loadPairedDevices()
        setupClickListeners()
        return binding.root
    }

    private fun setupToggle() {
        binding.switchAutoLaunch.isChecked = prefs.getBoolean(KEY_ENABLED, false)
        binding.switchAutoLaunch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_ENABLED, isChecked).apply()
            if (isChecked && prefs.getString(KEY_DEVICE_MAC, null) == null) {
                Toast.makeText(requireContext(), "Please select a Bluetooth device below", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadPairedDevices() {
        if (!hasBluetoothPermission()) {
            binding.tvNoDevices.visibility = View.VISIBLE
            binding.tvNoDevices.text = "Bluetooth permission required.\nGrant permission in app settings."
            return
        }

        val bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val bluetoothAdapter = bluetoothManager?.adapter

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            binding.tvNoDevices.visibility = View.VISIBLE
            binding.tvNoDevices.text = "Bluetooth is disabled.\nPlease enable Bluetooth first."
            return
        }

        val pairedDevices: Set<BluetoothDevice>? = try {
            bluetoothAdapter.bondedDevices
        } catch (e: SecurityException) {
            null
        }

        if (pairedDevices.isNullOrEmpty()) {
            binding.tvNoDevices.visibility = View.VISIBLE
            return
        }

        binding.tvNoDevices.visibility = View.GONE
        val savedMac = prefs.getString(KEY_DEVICE_MAC, null)

        for (device in pairedDevices) {
            val deviceName = try { device.name ?: "Unknown Device" } catch (e: SecurityException) { "Unknown Device" }
            val deviceMac = device.address
            addDeviceRow(deviceName, deviceMac, deviceMac == savedMac)
        }
    }

    private fun addDeviceRow(name: String, mac: String, isSelected: Boolean) {
        val context = requireContext()

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = resources.getDimensionPixelSize(sdpR.dimen._6sdp)
            }
            background = ContextCompat.getDrawable(context, R.drawable.card_bg)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                resources.getDimensionPixelSize(sdpR.dimen._12sdp),
                resources.getDimensionPixelSize(sdpR.dimen._10sdp),
                resources.getDimensionPixelSize(sdpR.dimen._12sdp),
                resources.getDimensionPixelSize(sdpR.dimen._10sdp)
            )
        }

        val textContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val nameTv = TextView(context).apply {
            text = name
            textSize = 13f
            setTextColor(ContextCompat.getColor(context, R.color.white))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        val macTv = TextView(context).apply {
            text = mac
            textSize = 10f
            setTextColor(0xFFBBBBBB.toInt())
        }

        textContainer.addView(nameTv)
        textContainer.addView(macTv)

        val radio = RadioButton(context).apply {
            isChecked = isSelected
            if (isSelected) selectedRadio = this
            setOnClickListener {
                selectedRadio?.isChecked = false
                selectedRadio = this
                this.isChecked = true
                prefs.edit()
                    .putString(KEY_DEVICE_MAC, mac)
                    .putString(KEY_DEVICE_NAME, name)
                    .apply()
                Toast.makeText(context, "Selected: $name", Toast.LENGTH_SHORT).show()
            }
        }

        container.addView(textContainer)
        container.addView(radio)
        binding.devicesContainer.addView(container)
    }

    private fun hasBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            mController.popBackStack()
        }
    }
}
