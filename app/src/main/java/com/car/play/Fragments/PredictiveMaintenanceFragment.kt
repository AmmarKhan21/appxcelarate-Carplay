package com.car.play.android.app.Fragments

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentPredictiveMaintenanceBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class PredictiveMaintenanceFragment : Fragment() {

    private val binding by lazy { FragmentPredictiveMaintenanceBinding.inflate(layoutInflater) }
    private val mController by lazy {
        (requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
    }
    private lateinit var googleAds: GoogleAds
    private lateinit var prefs: SharedPreferences

    companion object {
        private const val PREFS_NAME = "predictive_maintenance_prefs"
        private const val KEY_CURRENT_MILEAGE = "current_mileage"
        private const val KEY_OIL_LAST_KM = "oil_last_km"
        private const val KEY_OIL_LAST_DATE = "oil_last_date"
        private const val KEY_TIRE_LAST_KM = "tire_last_km"
        private const val KEY_TIRE_LAST_DATE = "tire_last_date"
        private const val KEY_BRAKE_LAST_KM = "brake_last_km"
        private const val KEY_BRAKE_LAST_DATE = "brake_last_date"
        private const val KEY_AIR_LAST_KM = "air_last_km"
        private const val KEY_AIR_LAST_DATE = "air_last_date"
        private const val KEY_BATTERY_LAST_KM = "battery_last_km"
        private const val KEY_BATTERY_LAST_DATE = "battery_last_date"

        private const val OIL_INTERVAL_KM = 8000
        private const val OIL_INTERVAL_DAYS = 180
        private const val TIRE_INTERVAL_KM = 12000
        private const val TIRE_INTERVAL_DAYS = 365
        private const val BRAKE_INTERVAL_KM = 30000
        private const val BRAKE_INTERVAL_DAYS = 730
        private const val AIR_INTERVAL_KM = 20000
        private const val AIR_INTERVAL_DAYS = 365
        private const val BATTERY_INTERVAL_KM = 50000
        private const val BATTERY_INTERVAL_DAYS = 1095
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        googleAds = GoogleAds()
        googleAds.CheckNative(this@PredictiveMaintenanceFragment, binding.nativeAd)
        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        setupClickListeners()
        updateUI()
        return binding.root
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { mController.popBackStack() }
        binding.btnUpdateMileage.setOnClickListener { showUpdateMileageDialog() }
    }

    private fun updateUI() {
        val currentKm = prefs.getFloat(KEY_CURRENT_MILEAGE, 0f).toInt()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val now = Date()

        val oilRemaining = calculateRemaining(
            currentKm, prefs.getFloat(KEY_OIL_LAST_KM, 0f).toInt(), OIL_INTERVAL_KM,
            prefs.getString(KEY_OIL_LAST_DATE, "") ?: "", OIL_INTERVAL_DAYS, dateFormat, now
        )
        updateMaintenanceItem(
            binding.tvOilDue, binding.pbOil, binding.tvOilLast,
            oilRemaining, OIL_INTERVAL_KM,
            prefs.getString(KEY_OIL_LAST_DATE, "Not recorded") ?: "Not recorded"
        )

        val tireRemaining = calculateRemaining(
            currentKm, prefs.getFloat(KEY_TIRE_LAST_KM, 0f).toInt(), TIRE_INTERVAL_KM,
            prefs.getString(KEY_TIRE_LAST_DATE, "") ?: "", TIRE_INTERVAL_DAYS, dateFormat, now
        )
        updateMaintenanceItem(
            binding.tvTireDue, binding.pbTire, binding.tvTireLast,
            tireRemaining, TIRE_INTERVAL_KM,
            prefs.getString(KEY_TIRE_LAST_DATE, "Not recorded") ?: "Not recorded"
        )

        val brakeRemaining = calculateRemaining(
            currentKm, prefs.getFloat(KEY_BRAKE_LAST_KM, 0f).toInt(), BRAKE_INTERVAL_KM,
            prefs.getString(KEY_BRAKE_LAST_DATE, "") ?: "", BRAKE_INTERVAL_DAYS, dateFormat, now
        )
        updateMaintenanceItem(
            binding.tvBrakeDue, binding.pbBrake, binding.tvBrakeLast,
            brakeRemaining, BRAKE_INTERVAL_KM,
            prefs.getString(KEY_BRAKE_LAST_DATE, "Not recorded") ?: "Not recorded"
        )

        val airRemaining = calculateRemaining(
            currentKm, prefs.getFloat(KEY_AIR_LAST_KM, 0f).toInt(), AIR_INTERVAL_KM,
            prefs.getString(KEY_AIR_LAST_DATE, "") ?: "", AIR_INTERVAL_DAYS, dateFormat, now
        )
        updateMaintenanceItem(
            binding.tvAirDue, binding.pbAir, binding.tvAirLast,
            airRemaining, AIR_INTERVAL_KM,
            prefs.getString(KEY_AIR_LAST_DATE, "Not recorded") ?: "Not recorded"
        )

        val batteryRemaining = calculateRemaining(
            currentKm, prefs.getFloat(KEY_BATTERY_LAST_KM, 0f).toInt(), BATTERY_INTERVAL_KM,
            prefs.getString(KEY_BATTERY_LAST_DATE, "") ?: "", BATTERY_INTERVAL_DAYS, dateFormat, now
        )
        updateMaintenanceItem(
            binding.tvBatteryDue, binding.pbBattery, binding.tvBatteryLast,
            batteryRemaining, BATTERY_INTERVAL_KM,
            prefs.getString(KEY_BATTERY_LAST_DATE, "Not recorded") ?: "Not recorded"
        )

        val healthScore = calculateHealthScore(
            oilRemaining, OIL_INTERVAL_KM,
            tireRemaining, TIRE_INTERVAL_KM,
            brakeRemaining, BRAKE_INTERVAL_KM,
            airRemaining, AIR_INTERVAL_KM,
            batteryRemaining, BATTERY_INTERVAL_KM
        )
        binding.tvHealthScore.text = "$healthScore"
        binding.tvHealthStatus.text = when {
            healthScore >= 80 -> "Excellent"
            healthScore >= 60 -> "Good"
            healthScore >= 40 -> "Fair"
            healthScore >= 20 -> "Needs Attention"
            else -> "Service Required"
        }
        binding.tvHealthStatus.setTextColor(when {
            healthScore >= 80 -> 0xFF4CAF50.toInt()
            healthScore >= 60 -> 0xFF8BC34A.toInt()
            healthScore >= 40 -> 0xFFFFC107.toInt()
            healthScore >= 20 -> 0xFFFF9800.toInt()
            else -> 0xFFF44336.toInt()
        })
    }

    private fun calculateRemaining(
        currentKm: Int, lastKm: Int, intervalKm: Int,
        lastDateStr: String, intervalDays: Int,
        dateFormat: SimpleDateFormat, now: Date
    ): Int {
        val kmRemaining = if (lastKm > 0) {
            intervalKm - (currentKm - lastKm)
        } else {
            intervalKm
        }

        val daysRemaining = if (lastDateStr.isNotEmpty()) {
            try {
                val lastDate = dateFormat.parse(lastDateStr)
                if (lastDate != null) {
                    val daysSince = TimeUnit.MILLISECONDS.toDays(now.time - lastDate.time).toInt()
                    intervalDays - daysSince
                } else intervalDays
            } catch (e: Exception) {
                intervalDays
            }
        } else {
            intervalDays
        }

        return minOf(kmRemaining, daysRemaining * (intervalKm / intervalDays))
    }

    private fun updateMaintenanceItem(
        tvDue: android.widget.TextView,
        progressBar: android.widget.ProgressBar,
        tvLast: android.widget.TextView,
        remaining: Int,
        interval: Int,
        lastDate: String
    ) {
        val remainingClamped = remaining.coerceAtLeast(0)
        val daysApprox = if (interval > 0) (remainingClamped * 30 / (interval / 12)).coerceAtLeast(0) else 0
        tvDue.text = "Due in $remainingClamped km / ~$daysApprox days"

        val usedPercent = ((interval - remainingClamped).toFloat() / interval * 100).toInt().coerceIn(0, 100)
        progressBar.progress = usedPercent

        tvLast.text = "Last: $lastDate"

        tvDue.setTextColor(when {
            remainingClamped > interval * 0.3 -> 0xFF4CAF50.toInt()
            remainingClamped > interval * 0.1 -> 0xFFFFC107.toInt()
            else -> 0xFFF44336.toInt()
        })
    }

    private fun calculateHealthScore(vararg pairs: Int): Int {
        var totalPercent = 0f
        var count = 0
        var i = 0
        while (i < pairs.size) {
            val remaining = pairs[i].coerceAtLeast(0)
            val interval = pairs[i + 1]
            totalPercent += (remaining.toFloat() / interval * 100).coerceIn(0f, 100f)
            count++
            i += 2
        }
        return if (count > 0) (totalPercent / count).toInt() else 100
    }

    private fun showUpdateMileageDialog() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 20)
        }

        val currentKm = prefs.getFloat(KEY_CURRENT_MILEAGE, 0f)
        val mileageInput = EditText(requireContext()).apply {
            hint = "Current odometer (km)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            if (currentKm > 0) setText(currentKm.toInt().toString())
            setSingleLine()
        }
        layout.addView(mileageInput)

        AlertDialog.Builder(requireContext())
            .setTitle("Update Mileage")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val km = mileageInput.text.toString().toFloatOrNull()
                if (km != null && km > 0) {
                    prefs.edit().putFloat(KEY_CURRENT_MILEAGE, km).apply()
                    updateUI()
                    Toast.makeText(requireContext(), "Mileage updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Enter a valid number", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
