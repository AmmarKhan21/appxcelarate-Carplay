package com.car.play.android.app.Fragments

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentVoiceCommandsBinding
import org.json.JSONArray
import java.util.Locale

class VoiceCommandsFragment : Fragment() {

    private val binding by lazy { FragmentVoiceCommandsBinding.inflate(layoutInflater) }
    private val mController by lazy {
        (requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
    }
    private lateinit var googleAds: GoogleAds
    private lateinit var prefs: SharedPreferences

    companion object {
        private const val PREFS_NAME = "voice_commands_prefs"
        private const val KEY_PARKING_LAT = "parking_lat"
        private const val KEY_PARKING_LNG = "parking_lng"
        private const val KEY_RECENT_COMMANDS = "recent_commands"
        private const val AUDIO_PERMISSION_REQUEST = 300
    }

    private val speechLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                val command = matches[0]
                binding.tvStatus.text = "Processing..."
                processCommand(command)
            } else {
                binding.tvStatus.text = "Tap to speak"
            }
        } else {
            binding.tvStatus.text = "Tap to speak"
        }
        stopMicAnimation()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        googleAds = GoogleAds()
        googleAds.CheckNative(this@VoiceCommandsFragment, binding.nativeAd)
        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        setupClickListeners()
        loadRecentCommands()
        return binding.root
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { mController.popBackStack() }

        binding.btnMic.setOnClickListener {
            if (!SpeechRecognizer.isRecognitionAvailable(requireContext())) {
                Toast.makeText(requireContext(), "Speech recognition not available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), AUDIO_PERMISSION_REQUEST)
                return@setOnClickListener
            }

            startListening()
        }
    }

    private fun startListening() {
        binding.tvStatus.text = "Listening..."
        startMicAnimation()

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a command...")
        }

        try {
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Speech recognition not available", Toast.LENGTH_SHORT).show()
            binding.tvStatus.text = "Tap to speak"
            stopMicAnimation()
        }
    }

    private fun processCommand(command: String) {
        val lower = command.lowercase()
        saveRecentCommand(command)

        when {
            lower.contains("save parking") || lower.contains("park here") || lower.contains("remember parking") -> {
                saveParking()
            }
            lower.contains("find my car") || lower.contains("find car") || lower.contains("where did i park") -> {
                findCar()
            }
            lower.contains("weather") -> {
                openWeather()
            }
            lower.contains("gas station") || lower.contains("fuel") || lower.contains("petrol") -> {
                findGasStation()
            }
            lower.contains("speed") || lower.contains("how fast") -> {
                checkSpeed()
            }
            lower.contains("navigate") || lower.contains("direction") -> {
                Toast.makeText(requireContext(), "Opening navigation...", Toast.LENGTH_SHORT).show()
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q="))
                intent.setPackage("com.google.android.apps.maps")
                try { startActivity(intent) } catch (_: Exception) { }
            }
            else -> {
                Toast.makeText(requireContext(), "Command not recognized: $command", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvStatus.text = "Tap to speak"
    }

    @SuppressLint("MissingPermission")
    private fun saveParking() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 400)
            return
        }

        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        if (location != null) {
            prefs.edit()
                .putFloat(KEY_PARKING_LAT, location.latitude.toFloat())
                .putFloat(KEY_PARKING_LNG, location.longitude.toFloat())
                .apply()
            Toast.makeText(requireContext(), "Parking location saved!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Unable to get location", Toast.LENGTH_SHORT).show()
        }
    }

    private fun findCar() {
        val lat = prefs.getFloat(KEY_PARKING_LAT, 0f)
        val lng = prefs.getFloat(KEY_PARKING_LNG, 0f)

        if (lat == 0f && lng == 0f) {
            Toast.makeText(requireContext(), "No parking location saved", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("google.navigation:q=$lat,$lng&mode=w")
            setPackage("com.google.android.apps.maps")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng&travelmode=walking"))
            startActivity(webIntent)
        }
    }

    private fun openWeather() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://weather.google.com"))
        startActivity(intent)
    }

    private fun findGasStation() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("geo:0,0?q=gas+station+near+me")
            setPackage("com.google.android.apps.maps")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/search/gas+station+near+me"))
            startActivity(webIntent)
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkSpeed() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 401)
            return
        }

        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        if (location != null && location.hasSpeed()) {
            val speedKmh = (location.speed * 3.6).toInt()
            Toast.makeText(requireContext(), "Current speed: $speedKmh km/h", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(requireContext(), "Speed not available. Start moving with GPS enabled.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveRecentCommand(command: String) {
        val json = prefs.getString(KEY_RECENT_COMMANDS, "[]") ?: "[]"
        val array = try { JSONArray(json) } catch (_: Exception) { JSONArray() }

        val newArray = JSONArray()
        newArray.put(command)
        for (i in 0 until minOf(array.length(), 9)) {
            newArray.put(array.getString(i))
        }

        prefs.edit().putString(KEY_RECENT_COMMANDS, newArray.toString()).apply()
        loadRecentCommands()
    }

    private fun loadRecentCommands() {
        val json = prefs.getString(KEY_RECENT_COMMANDS, "[]") ?: "[]"
        val array = try { JSONArray(json) } catch (_: Exception) { JSONArray() }

        binding.llRecentCommands.removeAllViews()

        if (array.length() == 0) {
            binding.tvRecentTitle.visibility = View.GONE
            return
        }

        binding.tvRecentTitle.visibility = View.VISIBLE
        for (i in 0 until array.length()) {
            val tv = TextView(requireContext()).apply {
                text = "• ${array.getString(i)}"
                setTextColor(0xFFBBBBBB.toInt())
                textSize = 12f
                setPadding(0, 4, 0, 4)
            }
            binding.llRecentCommands.addView(tv)
        }
    }

    private fun startMicAnimation() {
        ObjectAnimator.ofFloat(binding.btnMic, "scaleX", 1f, 1.2f, 1f).apply {
            duration = 1000
            repeatCount = ObjectAnimator.INFINITE
            start()
        }
        ObjectAnimator.ofFloat(binding.btnMic, "scaleY", 1f, 1.2f, 1f).apply {
            duration = 1000
            repeatCount = ObjectAnimator.INFINITE
            start()
        }
    }

    private fun stopMicAnimation() {
        binding.btnMic.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                AUDIO_PERMISSION_REQUEST -> startListening()
                400 -> saveParking()
                401 -> checkSpeed()
            }
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}
