package com.car.play.android.app.Fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.databinding.FragmentFatigueAlertBinding

class FatigueAlertFragment : Fragment() {

    private lateinit var binding: FragmentFatigueAlertBinding
    private lateinit var googleAds: GoogleAds
    private lateinit var prefs: SharedPreferences

    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    private var isDriving = false
    private var sessionStartTime = 0L
    private var totalDrivingTimeToday = 0L
    private var breaksTaken = 0
    private var hasAlerted = false
    private var mediaPlayer: MediaPlayer? = null

    companion object {
        private const val PREFS_NAME = "fatigue_alert_prefs"
        private const val KEY_THRESHOLD = "threshold"
        private const val KEY_ENABLE = "enable_alerts"
        private const val KEY_VIBRATE = "vibrate"
        private const val KEY_SOUND = "sound"
        private const val KEY_TOTAL_DRIVING = "total_driving_today"
        private const val KEY_BREAKS = "breaks_today"
        private const val KEY_LAST_DATE = "last_date"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFatigueAlertBinding.inflate(inflater, container, false)
        googleAds = GoogleAds()
        googleAds.CheckNative(this@FatigueAlertFragment, binding.nativeAd)
        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        loadSettings()
        setupClickListeners()
        updateStatsDisplay()

        return binding.root
    }

    private fun loadSettings() {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val lastDate = prefs.getString(KEY_LAST_DATE, "") ?: ""
        if (lastDate != today) {
            prefs.edit()
                .putLong(KEY_TOTAL_DRIVING, 0)
                .putInt(KEY_BREAKS, 0)
                .putString(KEY_LAST_DATE, today)
                .apply()
        }

        totalDrivingTimeToday = prefs.getLong(KEY_TOTAL_DRIVING, 0)
        breaksTaken = prefs.getInt(KEY_BREAKS, 0)

        val threshold = prefs.getFloat(KEY_THRESHOLD, 2.0f)
        binding.etThreshold.setText(String.format("%.1f", threshold))
        binding.switchEnable.isChecked = prefs.getBoolean(KEY_ENABLE, true)
        binding.switchVibrate.isChecked = prefs.getBoolean(KEY_VIBRATE, true)
        binding.switchSound.isChecked = prefs.getBoolean(KEY_SOUND, true)
    }

    private fun saveSettings() {
        val threshold = binding.etThreshold.text.toString().toFloatOrNull() ?: 2.0f
        prefs.edit()
            .putFloat(KEY_THRESHOLD, threshold)
            .putBoolean(KEY_ENABLE, binding.switchEnable.isChecked)
            .putBoolean(KEY_VIBRATE, binding.switchVibrate.isChecked)
            .putBoolean(KEY_SOUND, binding.switchSound.isChecked)
            .putLong(KEY_TOTAL_DRIVING, totalDrivingTimeToday)
            .putInt(KEY_BREAKS, breaksTaken)
            .apply()
    }

    private fun setupClickListeners() {
        binding.icBack.setOnClickListener { findNavController().popBackStack() }

        binding.btnStart.setOnClickListener { startDrivingSession() }
        binding.btnStop.setOnClickListener { stopDrivingSession() }

        binding.switchEnable.setOnCheckedChangeListener { _, _ -> saveSettings() }
        binding.switchVibrate.setOnCheckedChangeListener { _, _ -> saveSettings() }
        binding.switchSound.setOnCheckedChangeListener { _, _ -> saveSettings() }

        binding.btnRestStops.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=rest+stop+near+me"))
            intent.setPackage("com.google.android.apps.maps")
            try {
                startActivity(intent)
            } catch (e: Exception) {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=rest+stop+near+me"))
                startActivity(webIntent)
            }
        }
    }

    private fun startDrivingSession() {
        if (isDriving) return

        isDriving = true
        sessionStartTime = System.currentTimeMillis()
        hasAlerted = false
        binding.btnStart.alpha = 0.5f
        binding.btnStop.alpha = 1.0f

        timerRunnable = object : Runnable {
            override fun run() {
                if (!isDriving) return
                val elapsed = System.currentTimeMillis() - sessionStartTime
                updateTimerDisplay(elapsed)
                updateFatigueStatus(elapsed)
                checkThreshold(elapsed)
                binding.tvCurrentSession.text = formatTime(elapsed)
                binding.tvTimeToday.text = formatTime(totalDrivingTimeToday + elapsed)
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(timerRunnable!!)

        Toast.makeText(requireContext(), "Driving session started", Toast.LENGTH_SHORT).show()
    }

    private fun stopDrivingSession() {
        if (!isDriving) return

        isDriving = false
        val sessionDuration = System.currentTimeMillis() - sessionStartTime
        totalDrivingTimeToday += sessionDuration
        breaksTaken++

        timerRunnable?.let { handler.removeCallbacks(it) }
        stopAlarm()

        binding.btnStart.alpha = 1.0f
        binding.btnStop.alpha = 0.5f

        updateStatsDisplay()
        saveSettings()

        Toast.makeText(requireContext(), "Session stopped. Take a break!", Toast.LENGTH_SHORT).show()
    }

    private fun updateTimerDisplay(elapsedMs: Long) {
        binding.tvTimer.text = formatTime(elapsedMs)
    }

    private fun updateFatigueStatus(elapsedMs: Long) {
        val hours = elapsedMs / 3600000.0

        when {
            hours < 1.0 -> {
                binding.tvStatus.text = getString(com.car.play.android.app.R.string.status_fresh)
                binding.tvStatus.setTextColor(0xFF4CAF50.toInt())
            }
            hours < 2.0 -> {
                binding.tvStatus.text = getString(com.car.play.android.app.R.string.status_moderate)
                binding.tvStatus.setTextColor(0xFFFFC107.toInt())
            }
            hours < 3.0 -> {
                binding.tvStatus.text = getString(com.car.play.android.app.R.string.status_tired)
                binding.tvStatus.setTextColor(0xFFFF9800.toInt())
            }
            else -> {
                binding.tvStatus.text = getString(com.car.play.android.app.R.string.status_dangerous)
                binding.tvStatus.setTextColor(0xFFFF1744.toInt())
            }
        }
    }

    private fun checkThreshold(elapsedMs: Long) {
        if (!binding.switchEnable.isChecked || hasAlerted) return

        val thresholdHours = binding.etThreshold.text.toString().toFloatOrNull() ?: 2.0f
        val elapsedHours = elapsedMs / 3600000.0f

        if (elapsedHours >= thresholdHours) {
            hasAlerted = true
            triggerAlert()
        }
    }

    private fun triggerAlert() {
        Toast.makeText(requireContext(), getString(com.car.play.android.app.R.string.take_a_break), Toast.LENGTH_LONG).show()

        if (binding.switchVibrate.isChecked) {
            triggerVibration()
        }

        if (binding.switchSound.isChecked) {
            playAlarm()
        }
    }

    private fun triggerVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = requireContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500, 200, 500), -1))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500, 200, 500), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 500, 200, 500, 200, 500), -1)
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun playAlarm() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(requireContext(), alarmUri)
                prepare()
                isLooping = false
                start()
            }
        } catch (_: Exception) {
        }
    }

    private fun stopAlarm() {
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
    }

    private fun updateStatsDisplay() {
        binding.tvTimeToday.text = formatTime(totalDrivingTimeToday)
        binding.tvBreaks.text = breaksTaken.toString()
        binding.tvCurrentSession.text = "00:00:00"
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timerRunnable?.let { handler.removeCallbacks(it) }
        stopAlarm()
        if (isDriving) {
            val sessionDuration = System.currentTimeMillis() - sessionStartTime
            totalDrivingTimeToday += sessionDuration
        }
        saveSettings()
    }
}
