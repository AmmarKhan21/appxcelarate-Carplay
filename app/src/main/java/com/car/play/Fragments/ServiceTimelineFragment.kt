package com.car.play.android.app.Fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.Adapters.ServiceRecord
import com.car.play.android.app.Adapters.TimelineAdapter
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentServiceTimelineBinding
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ServiceTimelineFragment : Fragment() {

    private val binding by lazy { FragmentServiceTimelineBinding.inflate(layoutInflater) }
    private lateinit var googleAds: GoogleAds
    private val mController by lazy {
        (requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
    }
    private lateinit var timelineAdapter: TimelineAdapter
    private val records = mutableListOf<ServiceRecord>()

    private val prefs by lazy {
        requireContext().getSharedPreferences("service_timeline", android.content.Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        googleAds = GoogleAds()
        googleAds.CheckNative(this, binding.nativeAd)
        setupRecyclerView()
        setupClickListeners()
        loadRecords()
        return binding.root
    }

    private fun setupRecyclerView() {
        timelineAdapter = TimelineAdapter(emptyList()) { record ->
            showDeleteDialog(record)
        }
        binding.rvTimeline.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = timelineAdapter
        }
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { mController.popBackStack() }
        binding.fabAdd.setOnClickListener { showAddServiceDialog() }
    }

    private fun loadRecords() {
        records.clear()
        val json = prefs.getString("records", "[]") ?: "[]"
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                records.add(
                    ServiceRecord(
                        id = obj.getLong("id"),
                        date = obj.getString("date"),
                        serviceName = obj.getString("serviceName"),
                        cost = obj.getDouble("cost"),
                        mechanic = obj.getString("mechanic"),
                        notes = obj.getString("notes")
                    )
                )
            }
        } catch (_: Exception) { }

        records.sortByDescending { it.id }
        updateUI()
    }

    private fun saveRecords() {
        val arr = JSONArray()
        records.forEach { r ->
            arr.put(JSONObject().apply {
                put("id", r.id)
                put("date", r.date)
                put("serviceName", r.serviceName)
                put("cost", r.cost)
                put("mechanic", r.mechanic)
                put("notes", r.notes)
            })
        }
        prefs.edit().putString("records", arr.toString()).apply()
    }

    private fun updateUI() {
        if (records.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.rvTimeline.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.rvTimeline.visibility = View.VISIBLE
            timelineAdapter.updateList(records)
        }
    }

    private fun showAddServiceDialog() {
        val ctx = requireContext()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        val layout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 20)
        }

        val etDate = EditText(ctx).apply {
            hint = "Date (tap to pick)"
            isFocusable = false
            isClickable = true
            setTextColor(android.graphics.Color.WHITE)
            setHintTextColor(android.graphics.Color.GRAY)
        }
        etDate.setOnClickListener {
            DatePickerDialog(ctx, { _, y, m, d ->
                calendar.set(y, m, d)
                etDate.setText(dateFormat.format(calendar.time))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        val etName = EditText(ctx).apply {
            hint = "Service Name (e.g. Oil Change)"
            setTextColor(android.graphics.Color.WHITE)
            setHintTextColor(android.graphics.Color.GRAY)
        }

        val etCost = EditText(ctx).apply {
            hint = "Cost ($)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setTextColor(android.graphics.Color.WHITE)
            setHintTextColor(android.graphics.Color.GRAY)
        }

        val etMechanic = EditText(ctx).apply {
            hint = "Mechanic / Shop"
            setTextColor(android.graphics.Color.WHITE)
            setHintTextColor(android.graphics.Color.GRAY)
        }

        val etNotes = EditText(ctx).apply {
            hint = "Notes (optional)"
            setTextColor(android.graphics.Color.WHITE)
            setHintTextColor(android.graphics.Color.GRAY)
        }

        layout.addView(etDate)
        layout.addView(etName)
        layout.addView(etCost)
        layout.addView(etMechanic)
        layout.addView(etNotes)

        AlertDialog.Builder(ctx)
            .setTitle("Add Service Record")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val date = etDate.text.toString().trim()
                val name = etName.text.toString().trim()
                val costStr = etCost.text.toString().trim()
                val mechanic = etMechanic.text.toString().trim()
                val notes = etNotes.text.toString().trim()

                if (date.isEmpty() || name.isEmpty()) {
                    Toast.makeText(ctx, "Date and service name are required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val cost = costStr.toDoubleOrNull() ?: 0.0
                val record = ServiceRecord(
                    id = System.currentTimeMillis(),
                    date = date,
                    serviceName = name,
                    cost = cost,
                    mechanic = mechanic,
                    notes = notes
                )
                records.add(0, record)
                saveRecords()
                updateUI()
                Toast.makeText(ctx, "Service record added", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteDialog(record: ServiceRecord) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Record")
            .setMessage("Delete \"${record.serviceName}\" on ${record.date}?")
            .setPositiveButton("Delete") { _, _ ->
                records.remove(record)
                saveRecords()
                updateUI()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
