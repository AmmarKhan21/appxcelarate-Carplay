package com.car.play.android.app.Fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.Adapters.DrivingScoreAdapter
import com.car.play.android.app.databinding.FragmentDrivingScoreBinding
import com.car.play.android.app.db.DrivingScoreEntity
import com.car.play.android.app.db.DrivingScoreViewModel
import com.google.android.gms.location.*
import java.text.SimpleDateFormat
import java.util.*

class DrivingScoreFragment : Fragment(), SensorEventListener {

    private lateinit var binding: FragmentDrivingScoreBinding
    private lateinit var googleAds: GoogleAds
    private lateinit var viewModel: DrivingScoreViewModel
    private lateinit var scoreAdapter: DrivingScoreAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sensorManager: SensorManager

    private var isAssessing = false
    private var startTime: Long = 0L
    private var totalDistance: Double = 0.0
    private var lastLocation: Location? = null
    private var hardBrakes: Int = 0
    private var rapidAccelerations: Int = 0
    private var maxSpeed: Double = 0.0
    private var speedViolations: Int = 0
    private var corneringEvents: Int = 0
    private var totalSamples: Int = 0
    private var lastAccelZ: Float = 0f
    private var lastAccelX: Float = 0f

    private val HARD_BRAKE_THRESHOLD = 12.0f
    private val RAPID_ACCEL_THRESHOLD = 11.0f
    private val CORNERING_THRESHOLD = 8.0f
    private val SPEED_LIMIT = 120.0

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            if (!isAssessing) return
            val location = result.lastLocation ?: return

            lastLocation?.let { prev ->
                val dist = prev.distanceTo(location) / 1000.0
                totalDistance += dist
            }

            val speed = location.speed * 3.6
            if (speed > maxSpeed) maxSpeed = speed
            if (speed > SPEED_LIMIT) speedViolations++
            totalSamples++

            lastLocation = location
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDrivingScoreBinding.inflate(inflater, container, false)
        googleAds = GoogleAds()
        googleAds.CheckNative(this, binding.nativeAd)
        viewModel = ViewModelProvider(requireActivity()).get(DrivingScoreViewModel::class.java)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        setupRecyclerView()
        setupButtons()
        observeData()

        return binding.root
    }

    private fun setupRecyclerView() {
        scoreAdapter = DrivingScoreAdapter(mutableListOf())
        binding.rvScores.layoutManager = LinearLayoutManager(requireContext())
        binding.rvScores.adapter = scoreAdapter
    }

    private fun setupButtons() {
        binding.icBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnStartAssessment.setOnClickListener {
            if (isAssessing) stopAssessment() else startAssessment()
        }
    }

    private fun observeData() {
        viewModel.latestScore.observe(viewLifecycleOwner, Observer { score ->
            if (score != null) {
                binding.tvScore.text = score.overallScore.toString()
                val color = when {
                    score.overallScore >= 80 -> 0xFF008000.toInt()
                    score.overallScore >= 60 -> 0xFFFF9800.toInt()
                    else -> 0xFFFF0000.toInt()
                }
                binding.tvScore.setTextColor(color)
                binding.tvScoreLabel.text = when {
                    score.overallScore >= 80 -> "Excellent Driver"
                    score.overallScore >= 60 -> "Good Driver"
                    else -> "Needs Improvement"
                }
                binding.tvAccelerationScore.text = "${score.accelerationScore}/100"
                binding.tvBrakingScore.text = "${score.brakingScore}/100"
                binding.tvSpeedScore.text = "${score.speedScore}/100"
                binding.tvCorneringScore.text = "${score.corneringScore}/100"
                binding.pbAcceleration.progress = score.accelerationScore
                binding.pbBraking.progress = score.brakingScore
                binding.pbSpeed.progress = score.speedScore
                binding.pbCornering.progress = score.corneringScore
                binding.progressScore.progress = score.overallScore
            }
        })

        viewModel.bestScore.observe(viewLifecycleOwner, Observer { score ->
            binding.tvBestScore.text = score?.overallScore?.toString() ?: "--"
        })

        viewModel.allScores.observe(viewLifecycleOwner, Observer { scores ->
            scoreAdapter.updateList(scores.take(10))
            val totalDist = scores.sumOf { it.distanceDriven }
            binding.tvTotalDistance.text = String.format("%.1f km", totalDist)
            val totalBrakes = scores.sumOf { it.hardBrakes }
            binding.tvHardBrakes.text = totalBrakes.toString()
        })
    }

    private fun startAssessment() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1002)
            return
        }

        isAssessing = true
        startTime = SystemClock.elapsedRealtime()
        totalDistance = 0.0
        lastLocation = null
        hardBrakes = 0
        rapidAccelerations = 0
        maxSpeed = 0.0
        speedViolations = 0
        corneringEvents = 0
        totalSamples = 0

        binding.btnStartAssessment.text = "Stop Assessment"
        binding.btnStartAssessment.setBackgroundColor(0xFFFF0000.toInt())

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
            .setMinUpdateIntervalMillis(1000)
            .build()

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        Toast.makeText(requireContext(), "Assessment started. Drive normally.", Toast.LENGTH_SHORT).show()
    }

    private fun stopAssessment() {
        isAssessing = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
        sensorManager.unregisterListener(this)

        val duration = SystemClock.elapsedRealtime() - startTime

        val accelerationScore = calculateAccelerationScore()
        val brakingScore = calculateBrakingScore()
        val speedScore = calculateSpeedScore()
        val corneringScore = calculateCorneringScore()
        val overallScore = (accelerationScore + brakingScore + speedScore + corneringScore) / 4

        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = dateFormat.format(Date())

        viewModel.addScore(
            date = date,
            overallScore = overallScore,
            accelerationScore = accelerationScore,
            brakingScore = brakingScore,
            speedScore = speedScore,
            corneringScore = corneringScore,
            distanceDriven = totalDistance,
            duration = duration,
            hardBrakes = hardBrakes,
            rapidAccelerations = rapidAccelerations
        )

        binding.btnStartAssessment.text = "Start Drive Assessment"
        binding.btnStartAssessment.setBackgroundColor(0xFFFF9800.toInt())

        Toast.makeText(requireContext(), "Assessment complete! Score: $overallScore", Toast.LENGTH_LONG).show()
    }

    private fun calculateAccelerationScore(): Int {
        val penalty = (rapidAccelerations * 10).coerceAtMost(60)
        return (100 - penalty).coerceIn(0, 100)
    }

    private fun calculateBrakingScore(): Int {
        val penalty = (hardBrakes * 15).coerceAtMost(70)
        return (100 - penalty).coerceIn(0, 100)
    }

    private fun calculateSpeedScore(): Int {
        if (totalSamples == 0) return 100
        val violationRate = speedViolations.toDouble() / totalSamples
        val penalty = (violationRate * 100).toInt().coerceAtMost(80)
        return (100 - penalty).coerceIn(0, 100)
    }

    private fun calculateCorneringScore(): Int {
        val penalty = (corneringEvents * 12).coerceAtMost(60)
        return (100 - penalty).coerceIn(0, 100)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!isAssessing || event == null) return
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val z = event.values[2]
            val x = event.values[0]

            if (Math.abs(z - lastAccelZ) > HARD_BRAKE_THRESHOLD) {
                hardBrakes++
            }
            if (Math.abs(z - lastAccelZ) > RAPID_ACCEL_THRESHOLD && z > lastAccelZ) {
                rapidAccelerations++
            }
            if (Math.abs(x - lastAccelX) > CORNERING_THRESHOLD) {
                corneringEvents++
            }

            lastAccelZ = z
            lastAccelX = x
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroyView() {
        super.onDestroyView()
        if (isAssessing) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            sensorManager.unregisterListener(this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1002 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startAssessment()
        }
    }
}
