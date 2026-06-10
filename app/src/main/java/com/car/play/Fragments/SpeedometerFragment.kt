package com.car.play.android.app.Fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.databinding.FragmentSpeedometerBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.Locale

class SpeedometerFragment : Fragment() {

    private var _binding: FragmentSpeedometerBinding? = null
    private val binding get() = _binding!!

    private lateinit var googleAds: GoogleAds
    private lateinit var fusedClient: FusedLocationProviderClient

    private var useMph = false
    private var hasFix = false

    // Trip state
    private var isTracking = false
    private var tripMeters = 0.0
    private var maxSpeedMs = 0f
    private var lastLocation: Location? = null
    private var accumulatedMillis = 0L
    private var resumeElapsed = 0L

    private val uiHandler = Handler(Looper.getMainLooper())
    private val durationTicker = object : Runnable {
        override fun run() {
            if (isTracking) {
                updateDurationAndAvg()
                uiHandler.postDelayed(this, 1000)
            }
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            onNewLocation(location)
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startLocationUpdates()
        } else {
            binding.tvGps.text = getString(com.car.play.android.app.R.string.cp_enable_location)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpeedometerBinding.inflate(inflater, container, false)
        googleAds = GoogleAds()
        fusedClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        useMph = prefs().getBoolean(KEY_MPH, false)
        applyUnitLabels()
        binding.gauge.setMaxSpeed(if (useMph) 140f else 220f)

        binding.icBack.setOnClickListener { findNavController().popBackStack() }
        binding.unitToggle.setOnClickListener { toggleUnit() }
        binding.btnStart.setOnClickListener { toggleTracking() }
        binding.btnReset.setOnClickListener { resetTrip() }

        googleAds.CheckBanner(this, binding.root)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        ensureLocation()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun ensureLocation() {
        if (hasLocationPermission()) {
            startLocationUpdates()
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (!hasLocationPermission()) return
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setMinUpdateIntervalMillis(500L)
            .build()
        fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

    private fun stopLocationUpdates() {
        fusedClient.removeLocationUpdates(locationCallback)
    }

    private fun onNewLocation(location: Location) {
        _binding ?: return
        if (!hasFix) {
            hasFix = true
            binding.tvGps.text = ""
        }

        val speedMs = if (location.hasSpeed()) location.speed else 0f
        val display = toDisplaySpeed(speedMs)
        binding.tvSpeed.text = display.toInt().toString()
        binding.gauge.setSpeed(display)

        if (isTracking) {
            if (speedMs > maxSpeedMs) maxSpeedMs = speedMs
            val prev = lastLocation
            if (prev != null) {
                val delta = location.distanceTo(prev)
                // Ignore GPS jitter while essentially stationary or with poor accuracy
                if (delta > 1.5f && location.accuracy < 25f) {
                    tripMeters += delta
                }
            }
            lastLocation = location
            updateTripStats()
        }
    }

    private fun toggleTracking() {
        isTracking = !isTracking
        if (isTracking) {
            resumeElapsed = SystemClock.elapsedRealtime()
            lastLocation = null
            binding.btnStart.text = getString(com.car.play.android.app.R.string.cp_pause)
            binding.btnStart.setCompoundDrawablesRelativeWithIntrinsicBounds(
                com.car.play.android.app.R.drawable.ic_cp_pause, 0, 0, 0
            )
            uiHandler.post(durationTicker)
        } else {
            accumulatedMillis += SystemClock.elapsedRealtime() - resumeElapsed
            binding.btnStart.text = getString(com.car.play.android.app.R.string.cp_start)
            binding.btnStart.setCompoundDrawablesRelativeWithIntrinsicBounds(
                com.car.play.android.app.R.drawable.ic_cp_play, 0, 0, 0
            )
            uiHandler.removeCallbacks(durationTicker)
        }
    }

    private fun resetTrip() {
        isTracking = false
        tripMeters = 0.0
        maxSpeedMs = 0f
        lastLocation = null
        accumulatedMillis = 0L
        resumeElapsed = 0L
        uiHandler.removeCallbacks(durationTicker)
        binding.btnStart.text = getString(com.car.play.android.app.R.string.cp_start)
        binding.btnStart.setCompoundDrawablesRelativeWithIntrinsicBounds(
            com.car.play.android.app.R.drawable.ic_cp_play, 0, 0, 0
        )
        updateTripStats()
        binding.tvDuration.text = "00:00"
        binding.tvAvg.text = "0"
    }

    private fun currentDurationMillis(): Long {
        return if (isTracking) {
            accumulatedMillis + (SystemClock.elapsedRealtime() - resumeElapsed)
        } else {
            accumulatedMillis
        }
    }

    private fun updateTripStats() {
        _binding ?: return
        val km = tripMeters / 1000.0
        if (useMph) {
            binding.tvDistance.text = String.format(Locale.getDefault(), "%.1f mi", km * 0.621371)
        } else {
            binding.tvDistance.text = String.format(Locale.getDefault(), "%.1f km", km)
        }
        binding.tvMax.text = toDisplaySpeed(maxSpeedMs).toInt().toString()
    }

    private fun updateDurationAndAvg() {
        _binding ?: return
        val millis = currentDurationMillis()
        binding.tvDuration.text = formatDuration(millis)

        val seconds = millis / 1000.0
        val avgMs = if (seconds > 0) (tripMeters / seconds).toFloat() else 0f
        binding.tvAvg.text = toDisplaySpeed(avgMs).toInt().toString()
    }

    private fun toDisplaySpeed(speedMs: Float): Float {
        return if (useMph) speedMs * 2.2369363f else speedMs * 3.6f
    }

    private fun toggleUnit() {
        useMph = !useMph
        prefs().edit().putBoolean(KEY_MPH, useMph).apply()
        applyUnitLabels()
        binding.gauge.setMaxSpeed(if (useMph) 140f else 220f)
        updateTripStats()
        updateDurationAndAvg()
    }

    private fun applyUnitLabels() {
        val label = getString(
            if (useMph) com.car.play.android.app.R.string.cp_unit_mph
            else com.car.play.android.app.R.string.cp_unit_kmh
        )
        binding.unitToggle.text = label
        binding.tvUnit.text = label
    }

    private fun formatDuration(millis: Long): String {
        val totalSec = millis / 1000
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return if (h > 0) {
            String.format(Locale.getDefault(), "%d:%02d:%02d", h, m, s)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", m, s)
        }
    }

    private fun prefs() =
        requireContext().getSharedPreferences("cp_speedometer", Context.MODE_PRIVATE)

    override fun onDestroyView() {
        uiHandler.removeCallbacks(durationTicker)
        stopLocationUpdates()
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val KEY_MPH = "use_mph"
    }
}
