package com.car.play.android.app.Fragments

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentPredictiveMaintenanceBinding
import com.intuit.sdp.R as sdpR
import java.text.NumberFormat
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
    private var currentFilter = FilterType.ALL

    enum class FilterType { ALL, DUE_SOON, OVERDUE, GOOD }

    data class MaintenanceItem(
        val key: String,
        val title: String,
        val emoji: String,
        val intervalKm: Int,
        val intervalDays: Int
    )

    enum class ItemStatus { GOOD, DUE_SOON, OVERDUE }

    companion object {
        private const val PREFS_NAME = "predictive_maintenance_prefs"
        private const val KEY_CURRENT_MILEAGE = "current_mileage"

        private val ITEMS = listOf(
            MaintenanceItem("oil", "Oil Change", "\uD83D\uDEE2\uFE0F", 8000, 180),
            MaintenanceItem("tire", "Tire Rotation", "\uD83D\uDD04", 12000, 365),
            MaintenanceItem("brake", "Brake Inspection", "\uD83D\uDED1", 30000, 730),
            MaintenanceItem("air", "Air Filter", "\uD83D\uDCA8", 20000, 365),
            MaintenanceItem("battery", "Battery Check", "\uD83D\uDD0B", 50000, 1095),
            MaintenanceItem("coolant", "Coolant Flush", "\u2744\uFE0F", 40000, 730),
            MaintenanceItem("spark", "Spark Plugs", "\u26A1", 45000, 730),
            MaintenanceItem("trans", "Transmission Fluid", "\u2699\uFE0F", 60000, 1095)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        googleAds = GoogleAds()
        googleAds.CheckNative(this@PredictiveMaintenanceFragment, binding.nativeAd)
        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        setupClickListeners()
        setupFilterChips()
        updateUI()
        return binding.root
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { mController.popBackStack() }
        binding.btnUpdateMileage.setOnClickListener { showUpdateMileageDialog() }
        binding.btnSetup.setOnClickListener { showUpdateMileageDialog() }
    }

    private fun setupFilterChips() {
        val chips = listOf(binding.chipAll, binding.chipDueSoon, binding.chipOverdue, binding.chipGood)
        val filters = listOf(FilterType.ALL, FilterType.DUE_SOON, FilterType.OVERDUE, FilterType.GOOD)

        chips.forEachIndexed { index, chip ->
            chip.setOnClickListener {
                currentFilter = filters[index]
                updateChipSelection(chips, index)
                rebuildCards()
            }
        }
    }

    private fun updateChipSelection(chips: List<TextView>, selectedIndex: Int) {
        chips.forEachIndexed { i, chip ->
            if (i == selectedIndex) {
                chip.setBackgroundResource(R.drawable.chip_bg_selected)
                chip.setTextColor(Color.WHITE)
            } else {
                chip.setBackgroundResource(R.drawable.chip_bg)
                chip.setTextColor(0xFFBBBBBB.toInt())
            }
        }
    }

    private fun updateUI() {
        val currentKm = prefs.getFloat(KEY_CURRENT_MILEAGE, 0f).toInt()

        if (currentKm <= 0) {
            binding.setupCard.visibility = View.VISIBLE
            binding.healthCard.visibility = View.GONE
            binding.maintenanceContainer.removeAllViews()
            binding.tvCurrentMileage.visibility = View.GONE
            return
        }

        binding.setupCard.visibility = View.GONE
        binding.healthCard.visibility = View.VISIBLE

        val nf = NumberFormat.getNumberInstance(Locale.getDefault())
        binding.tvCurrentMileage.visibility = View.VISIBLE
        binding.tvCurrentMileage.text = "Odometer: ${nf.format(currentKm)} km"

        val healthScore = calculateOverallHealth()
        binding.tvHealthScore.text = "$healthScore"
        binding.tvHealthStatus.text = when {
            healthScore >= 80 -> "Excellent Condition"
            healthScore >= 60 -> "Good Condition"
            healthScore >= 40 -> "Fair - Service Soon"
            healthScore >= 20 -> "Needs Attention"
            else -> "Service Required!"
        }
        binding.tvHealthStatus.setTextColor(getStatusColor(healthScore))

        rebuildCards()
    }

    private fun rebuildCards() {
        binding.maintenanceContainer.removeAllViews()
        val currentKm = prefs.getFloat(KEY_CURRENT_MILEAGE, 0f).toInt()
        if (currentKm <= 0) return

        for (item in ITEMS) {
            val remaining = calculateRemainingKm(item)
            val status = getItemStatus(remaining, item.intervalKm)

            if (currentFilter != FilterType.ALL) {
                val matchesFilter = when (currentFilter) {
                    FilterType.DUE_SOON -> status == ItemStatus.DUE_SOON
                    FilterType.OVERDUE -> status == ItemStatus.OVERDUE
                    FilterType.GOOD -> status == ItemStatus.GOOD
                    else -> true
                }
                if (!matchesFilter) continue
            }

            addMaintenanceCard(item, remaining, status)
        }

        if (binding.maintenanceContainer.childCount == 0) {
            val emptyTv = TextView(requireContext()).apply {
                text = when (currentFilter) {
                    FilterType.OVERDUE -> "No overdue items - great job!"
                    FilterType.DUE_SOON -> "Nothing due soon"
                    FilterType.GOOD -> "No items in good standing yet"
                    else -> "No maintenance items"
                }
                textSize = 14f
                setTextColor(0xFF888888.toInt())
                gravity = Gravity.CENTER
                setPadding(0, resources.getDimensionPixelSize(sdpR.dimen._30sdp), 0, resources.getDimensionPixelSize(sdpR.dimen._30sdp))
            }
            binding.maintenanceContainer.addView(emptyTv)
        }
    }

    private fun addMaintenanceCard(item: MaintenanceItem, remainingKm: Int, status: ItemStatus) {
        val ctx = requireContext()
        val clamped = remainingKm.coerceAtLeast(0)
        val usedPercent = ((item.intervalKm - clamped).toFloat() / item.intervalKm * 100).toInt().coerceIn(0, 100)
        val daysRemaining = calculateRemainingDays(item)
        val daysText = if (daysRemaining > 0) "$daysRemaining days" else "overdue"
        val kmText = if (clamped > 0) "${NumberFormat.getNumberInstance(Locale.getDefault()).format(clamped)} km" else "overdue"
        val lastDate = prefs.getString("${item.key}_last_date", null)
        val lastKm = prefs.getFloat("${item.key}_last_km", 0f).toInt()

        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = resources.getDimensionPixelSize(sdpR.dimen._6sdp)
            }
            background = ContextCompat.getDrawable(ctx, R.drawable.card_bg)
            setPadding(
                resources.getDimensionPixelSize(sdpR.dimen._12sdp),
                resources.getDimensionPixelSize(sdpR.dimen._12sdp),
                resources.getDimensionPixelSize(sdpR.dimen._12sdp),
                resources.getDimensionPixelSize(sdpR.dimen._12sdp)
            )
            isClickable = true
            isFocusable = true
        }

        val headerRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val titleTv = TextView(ctx).apply {
            text = "${item.emoji} ${item.title}"
            textSize = 14f
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val statusBadge = TextView(ctx).apply {
            text = when (status) {
                ItemStatus.GOOD -> "OK"
                ItemStatus.DUE_SOON -> "DUE SOON"
                ItemStatus.OVERDUE -> "OVERDUE"
            }
            textSize = 10f
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(
                resources.getDimensionPixelSize(sdpR.dimen._8sdp), resources.getDimensionPixelSize(sdpR.dimen._3sdp),
                resources.getDimensionPixelSize(sdpR.dimen._8sdp), resources.getDimensionPixelSize(sdpR.dimen._3sdp)
            )
            background = when (status) {
                ItemStatus.GOOD -> ContextCompat.getDrawable(ctx, R.drawable.chip_bg)?.also {
                    setTextColor(0xFF4CAF50.toInt())
                }
                ItemStatus.DUE_SOON -> ContextCompat.getDrawable(ctx, R.drawable.chip_bg)?.also {
                    setTextColor(0xFFFFC107.toInt())
                }
                ItemStatus.OVERDUE -> ContextCompat.getDrawable(ctx, R.drawable.chip_bg)?.also {
                    setTextColor(0xFFF44336.toInt())
                }
            }
        }

        headerRow.addView(titleTv)
        headerRow.addView(statusBadge)

        val dueTv = TextView(ctx).apply {
            text = if (remainingKm <= 0) "Overdue - service needed now!"
            else "Due in $kmText / ~$daysText"
            textSize = 12f
            setTextColor(when (status) {
                ItemStatus.GOOD -> 0xFF4CAF50.toInt()
                ItemStatus.DUE_SOON -> 0xFFFFC107.toInt()
                ItemStatus.OVERDUE -> 0xFFF44336.toInt()
            })
            setPadding(0, resources.getDimensionPixelSize(sdpR.dimen._5sdp), 0, 0)
        }

        val pb = ProgressBar(ctx, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                resources.getDimensionPixelSize(sdpR.dimen._5sdp)
            ).apply {
                topMargin = resources.getDimensionPixelSize(sdpR.dimen._8sdp)
            }
            max = 100
            progress = usedPercent
            progressBackgroundTintList = android.content.res.ColorStateList.valueOf(0xFF333333.toInt())
            progressTintList = android.content.res.ColorStateList.valueOf(when (status) {
                ItemStatus.GOOD -> 0xFF4CAF50.toInt()
                ItemStatus.DUE_SOON -> 0xFFFFC107.toInt()
                ItemStatus.OVERDUE -> 0xFFF44336.toInt()
            })
        }

        val bottomRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, resources.getDimensionPixelSize(sdpR.dimen._5sdp), 0, 0)
        }

        val lastTv = TextView(ctx).apply {
            text = if (lastDate != null) {
                val nf = NumberFormat.getNumberInstance(Locale.getDefault())
                "Last: $lastDate at ${nf.format(lastKm)} km"
            } else "No service recorded"
            textSize = 10f
            setTextColor(0xFF888888.toInt())
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val serviceTv = TextView(ctx).apply {
            text = "Mark Done"
            textSize = 11f
            setTextColor(0xFF3E73BF.toInt())
            setTypeface(typeface, Typeface.BOLD)
            setPadding(
                resources.getDimensionPixelSize(sdpR.dimen._8sdp), resources.getDimensionPixelSize(sdpR.dimen._3sdp),
                resources.getDimensionPixelSize(sdpR.dimen._8sdp), resources.getDimensionPixelSize(sdpR.dimen._3sdp)
            )
            setOnClickListener { showMarkServiceDoneDialog(item) }
        }

        bottomRow.addView(lastTv)
        bottomRow.addView(serviceTv)

        container.addView(headerRow)
        container.addView(dueTv)
        container.addView(pb)
        container.addView(bottomRow)

        container.setOnClickListener { showMarkServiceDoneDialog(item) }

        binding.maintenanceContainer.addView(container)
    }

    private fun calculateRemainingKm(item: MaintenanceItem): Int {
        val currentKm = prefs.getFloat(KEY_CURRENT_MILEAGE, 0f).toInt()
        val lastKm = prefs.getFloat("${item.key}_last_km", 0f).toInt()

        return if (lastKm > 0) {
            item.intervalKm - (currentKm - lastKm)
        } else {
            item.intervalKm
        }
    }

    private fun calculateRemainingDays(item: MaintenanceItem): Int {
        val lastDateStr = prefs.getString("${item.key}_last_date", null) ?: return item.intervalDays
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val lastDate = dateFormat.parse(lastDateStr) ?: return item.intervalDays
            val daysSince = TimeUnit.MILLISECONDS.toDays(Date().time - lastDate.time).toInt()
            (item.intervalDays - daysSince).coerceAtLeast(0)
        } catch (e: Exception) {
            item.intervalDays
        }
    }

    private fun getItemStatus(remainingKm: Int, intervalKm: Int): ItemStatus {
        return when {
            remainingKm <= 0 -> ItemStatus.OVERDUE
            remainingKm <= intervalKm * 0.2 -> ItemStatus.DUE_SOON
            else -> ItemStatus.GOOD
        }
    }

    private fun calculateOverallHealth(): Int {
        var totalPercent = 0f
        var count = 0
        for (item in ITEMS) {
            val remaining = calculateRemainingKm(item).coerceAtLeast(0)
            val daysRemaining = calculateRemainingDays(item)
            val kmPercent = (remaining.toFloat() / item.intervalKm * 100).coerceIn(0f, 100f)
            val daysPercent = (daysRemaining.toFloat() / item.intervalDays * 100).coerceIn(0f, 100f)
            totalPercent += minOf(kmPercent, daysPercent)
            count++
        }
        return if (count > 0) (totalPercent / count).toInt() else 100
    }

    private fun getStatusColor(score: Int): Int {
        return when {
            score >= 80 -> 0xFF4CAF50.toInt()
            score >= 60 -> 0xFF8BC34A.toInt()
            score >= 40 -> 0xFFFFC107.toInt()
            score >= 20 -> 0xFFFF9800.toInt()
            else -> 0xFFF44336.toInt()
        }
    }

    private fun showUpdateMileageDialog() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 20)
        }

        val currentKm = prefs.getFloat(KEY_CURRENT_MILEAGE, 0f)
        val mileageInput = EditText(requireContext()).apply {
            hint = "Current odometer reading (km)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            if (currentKm > 0) setText(currentKm.toInt().toString())
            setTextColor(Color.WHITE)
            setHintTextColor(0xFF888888.toInt())
            setSingleLine()
        }
        layout.addView(mileageInput)

        AlertDialog.Builder(requireContext())
            .setTitle("Update Mileage")
            .setMessage("Enter your current odometer reading so predictions stay accurate.")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val km = mileageInput.text.toString().toFloatOrNull()
                if (km != null && km > 0) {
                    prefs.edit().putFloat(KEY_CURRENT_MILEAGE, km).apply()
                    updateUI()
                    Toast.makeText(requireContext(), "Mileage updated to ${NumberFormat.getNumberInstance(Locale.getDefault()).format(km.toInt())} km", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showMarkServiceDoneDialog(item: MaintenanceItem) {
        val currentKm = prefs.getFloat(KEY_CURRENT_MILEAGE, 0f)
        val nf = NumberFormat.getNumberInstance(Locale.getDefault())

        AlertDialog.Builder(requireContext())
            .setTitle("${item.emoji} ${item.title}")
            .setMessage("Mark this service as completed today at ${nf.format(currentKm.toInt())} km?\n\nThis will reset the prediction timer for this service.")
            .setPositiveButton("Mark Done") { _, _ ->
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                prefs.edit()
                    .putFloat("${item.key}_last_km", currentKm)
                    .putString("${item.key}_last_date", dateFormat.format(Date()))
                    .apply()
                updateUI()
                Toast.makeText(requireContext(), "${item.title} marked as done", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
