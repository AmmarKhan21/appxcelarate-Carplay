package com.car.play.android.app.Fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentDashcamBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashCamFragment : Fragment() {

    private val binding by lazy { FragmentDashcamBinding.inflate(layoutInflater) }
    private val mController by lazy {
        (requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
    }

    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null
    private var isRecording = false

    private val timerHandler = Handler(Looper.getMainLooper())
    private var recordingSeconds = 0L
    private val timerRunnable = object : Runnable {
        override fun run() {
            recordingSeconds++
            val hours = recordingSeconds / 3600
            val minutes = (recordingSeconds % 3600) / 60
            val seconds = recordingSeconds % 60
            binding.tvTimer.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            timerHandler.postDelayed(this, 1000)
        }
    }

    private val dateTimeHandler = Handler(Looper.getMainLooper())
    private val dateTimeRunnable = object : Runnable {
        override fun run() {
            binding.tvDatetime.text =
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            dateTimeHandler.postDelayed(this, 1000)
        }
    }

    private var locationManager: LocationManager? = null
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val speedKmh = (location.speed * 3.6f).toInt()
            binding.tvSpeed.text = "$speedKmh km/h"
        }

        @Deprecated("Deprecated in API level 29")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private const val PREFS_NAME = "dashcam_prefs"
        private const val KEY_AUTO_RECORD = "auto_record"
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setupClickListeners()
        loadPreferences()
        dateTimeHandler.post(dateTimeRunnable)

        if (allPermissionsGranted()) {
            startCamera()
            startLocationUpdates()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE)
        }

        return binding.root
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            if (isRecording) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Recording in progress")
                    .setMessage("Stop recording and go back?")
                    .setPositiveButton("Stop & Go Back") { _, _ ->
                        stopRecording()
                        mController.popBackStack()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                mController.popBackStack()
            }
        }

        binding.btnRecord.setOnClickListener {
            if (!isRecording) startRecording()
        }

        binding.btnStop.setOnClickListener {
            if (isRecording) stopRecording()
        }

        binding.btnSettings.setOnClickListener {
            Toast.makeText(requireContext(), "Settings coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                type = "video/*"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "No gallery app found", Toast.LENGTH_SHORT).show()
            }
        }

        binding.switchAutoRecord.setOnCheckedChangeListener { _, isChecked ->
            requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_AUTO_RECORD, isChecked).apply()
        }
    }

    private fun loadPreferences() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        binding.switchAutoRecord.isChecked = prefs.getBoolean(KEY_AUTO_RECORD, false)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    videoCapture
                )
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Camera initialization failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @SuppressLint("MissingPermission")
    private fun startRecording() {
        val capture = videoCapture ?: return

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "DashCam_${System.currentTimeMillis()}")
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
        }

        val outputOptions = MediaStoreOutputOptions.Builder(
            requireContext().contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues).build()

        activeRecording = capture.output
            .prepareRecording(requireContext(), outputOptions)
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(requireContext())) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> {
                        isRecording = true
                        binding.recordingIndicator.visibility = View.VISIBLE
                        recordingSeconds = 0
                        timerHandler.post(timerRunnable)
                        requireActivity().window.addFlags(
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        )
                    }

                    is VideoRecordEvent.Finalize -> {
                        isRecording = false
                        binding.recordingIndicator.visibility = View.GONE
                        timerHandler.removeCallbacks(timerRunnable)
                        requireActivity().window.clearFlags(
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        )
                        if (event.error != VideoRecordEvent.Finalize.ERROR_NONE) {
                            Toast.makeText(
                                requireContext(), "Recording error", Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                requireContext(), "Video saved", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
    }

    private fun stopRecording() {
        activeRecording?.stop()
        activeRecording = null
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 1000L, 0f, locationListener
            )
        } catch (_: Exception) {
        }
    }

    private fun allPermissionsGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                requireContext(), it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (allPermissionsGranted()) {
                startCamera()
                startLocationUpdates()
            } else {
                Toast.makeText(
                    requireContext(), "Permissions required for dash cam", Toast.LENGTH_SHORT
                ).show()
                mController.popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timerHandler.removeCallbacks(timerRunnable)
        dateTimeHandler.removeCallbacks(dateTimeRunnable)
        locationManager?.removeUpdates(locationListener)
        if (isRecording) stopRecording()
    }
}
