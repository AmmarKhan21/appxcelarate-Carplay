package com.car.play.android.app.Fragments

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentDndDrivingBinding

class DndDrivingFragment : Fragment() {

    private val binding by lazy { FragmentDndDrivingBinding.inflate(layoutInflater) }
    private val mController by lazy {
        (requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
    }
    private lateinit var googleAds: GoogleAds
    private lateinit var prefs: SharedPreferences

    companion object {
        private const val PREFS_NAME = "dnd_driving_prefs"
        private const val KEY_DRIVING_MODE = "driving_mode"
        private const val KEY_AUTO_DETECT = "auto_detect"
        private const val KEY_AUTO_REPLY = "auto_reply"
        private const val KEY_AUTO_REPLY_MSG = "auto_reply_message"
        private const val KEY_SILENCE = "silence_all"
        private const val KEY_ALLOW_FAVORITES = "allow_favorites"
        private const val KEY_READ_ALOUD = "read_aloud"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        googleAds = GoogleAds()
        googleAds.CheckNative(this@DndDrivingFragment, binding.nativeAd)
        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadSettings()
        setupListeners()
        return binding.root
    }

    private fun loadSettings() {
        binding.switchDrivingMode.isChecked = prefs.getBoolean(KEY_DRIVING_MODE, false)
        binding.switchAutoDetect.isChecked = prefs.getBoolean(KEY_AUTO_DETECT, false)
        binding.switchAutoReply.isChecked = prefs.getBoolean(KEY_AUTO_REPLY, false)
        binding.etAutoReplyMessage.setText(
            prefs.getString(KEY_AUTO_REPLY_MSG, "I'm currently driving. I'll get back to you soon.")
        )
        binding.switchSilence.isChecked = prefs.getBoolean(KEY_SILENCE, true)
        binding.switchAllowFavorites.isChecked = prefs.getBoolean(KEY_ALLOW_FAVORITES, true)
        binding.switchReadAloud.isChecked = prefs.getBoolean(KEY_READ_ALOUD, false)
        updateStatusDisplay(prefs.getBoolean(KEY_DRIVING_MODE, false))
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener { mController.popBackStack() }

        binding.switchDrivingMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_DRIVING_MODE, isChecked).apply()
            if (isChecked) {
                enableDndMode()
            } else {
                disableDndMode()
            }
            updateStatusDisplay(isChecked)
        }

        binding.switchAutoDetect.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_AUTO_DETECT, isChecked).apply()
        }

        binding.switchAutoReply.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_AUTO_REPLY, isChecked).apply()
        }

        binding.switchSilence.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_SILENCE, isChecked).apply()
        }

        binding.switchAllowFavorites.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_ALLOW_FAVORITES, isChecked).apply()
        }

        binding.switchReadAloud.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_READ_ALOUD, isChecked).apply()
        }
    }

    private fun enableDndMode() {
        val notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (!notificationManager.isNotificationPolicyAccessGranted) {
            Toast.makeText(requireContext(), "Please grant DND access", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivity(intent)
            binding.switchDrivingMode.isChecked = false
            return
        }

        val filter = if (prefs.getBoolean(KEY_ALLOW_FAVORITES, true)) {
            NotificationManager.INTERRUPTION_FILTER_PRIORITY
        } else {
            NotificationManager.INTERRUPTION_FILTER_NONE
        }
        notificationManager.setInterruptionFilter(filter)

        val message = binding.etAutoReplyMessage.text.toString().trim()
        if (message.isNotEmpty()) {
            prefs.edit().putString(KEY_AUTO_REPLY_MSG, message).apply()
        }

        Toast.makeText(requireContext(), "Driving mode activated", Toast.LENGTH_SHORT).show()
    }

    private fun disableDndMode() {
        val notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (notificationManager.isNotificationPolicyAccessGranted) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }

        Toast.makeText(requireContext(), "Driving mode deactivated", Toast.LENGTH_SHORT).show()
    }

    private fun updateStatusDisplay(isActive: Boolean) {
        if (isActive) {
            binding.tvStatus.text = "Active"
            binding.tvStatus.setTextColor(0xFF4CAF50.toInt())
            val drawable = binding.statusIndicator.background
            if (drawable is GradientDrawable) {
                drawable.setColor(0xFF4CAF50.toInt())
            }
        } else {
            binding.tvStatus.text = "Inactive"
            binding.tvStatus.setTextColor(0xFFF44336.toInt())
            val drawable = binding.statusIndicator.background
            if (drawable is GradientDrawable) {
                drawable.setColor(0xFFF44336.toInt())
            }
        }
    }

    override fun onPause() {
        super.onPause()
        val message = binding.etAutoReplyMessage.text.toString().trim()
        if (message.isNotEmpty()) {
            prefs.edit().putString(KEY_AUTO_REPLY_MSG, message).apply()
        }
    }
}
