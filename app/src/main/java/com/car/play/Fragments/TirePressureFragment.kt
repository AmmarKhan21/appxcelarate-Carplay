package com.car.play.android.app.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.Adapters.TirePressureAdapter
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentTirePressureBinding
import com.car.play.android.app.db.TirePressureEntity
import com.car.play.android.app.db.TirePressureViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TirePressureFragment : Fragment() {

    private lateinit var binding: FragmentTirePressureBinding
    private lateinit var googleAds: GoogleAds
    private lateinit var viewModel: TirePressureViewModel
    private lateinit var adapter: TirePressureAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTirePressureBinding.inflate(inflater, container, false)
        googleAds = GoogleAds()
        googleAds.CheckNative(this@TirePressureFragment, binding.nativeAd)
        viewModel = ViewModelProvider(requireActivity())[TirePressureViewModel::class.java]

        setupRecyclerView()
        setupClickListeners()
        observeData()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = TirePressureAdapter(emptyList()) { record -> showDeleteDialog(record) }
        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TirePressureFragment.adapter
        }
    }

    private fun setupClickListeners() {
        binding.icBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnUpdatePressure.setOnClickListener { showUpdateDialog() }
    }

    private fun observeData() {
        viewModel.latestRecord.observe(viewLifecycleOwner, Observer { record ->
            if (record != null) {
                updateCarView(record)
            } else {
                resetCarView()
            }
        })

        viewModel.allRecords.observe(viewLifecycleOwner, Observer { records ->
            if (records.isNullOrEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvHistory.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.rvHistory.visibility = View.VISIBLE
                adapter.updateList(records)
            }
        })
    }

    private fun updateCarView(record: TirePressureEntity) {
        binding.tvFl.text = String.format("%.1f", record.frontLeft)
        binding.tvFr.text = String.format("%.1f", record.frontRight)
        binding.tvRl.text = String.format("%.1f", record.rearLeft)
        binding.tvRr.text = String.format("%.1f", record.rearRight)
        binding.tvRecommended.text = "${String.format("%.1f", record.recommendedPressure)} PSI"

        setTireColor(binding.tvFl, record.frontLeft, record.recommendedPressure)
        setTireColor(binding.tvFr, record.frontRight, record.recommendedPressure)
        setTireColor(binding.tvRl, record.rearLeft, record.recommendedPressure)
        setTireColor(binding.tvRr, record.rearRight, record.recommendedPressure)
    }

    private fun setTireColor(textView: TextView, actual: Double, recommended: Double) {
        val deviation = kotlin.math.abs(actual - recommended) / recommended
        val color = when {
            deviation <= 0.10 -> 0xFF4CAF50.toInt()
            deviation <= 0.20 -> 0xFFFFC107.toInt()
            else -> 0xFFF44336.toInt()
        }
        textView.setTextColor(color)
    }

    private fun resetCarView() {
        binding.tvFl.text = "--"
        binding.tvFr.text = "--"
        binding.tvRl.text = "--"
        binding.tvRr.text = "--"
        binding.tvRecommended.text = "-- PSI"

        val defaultColor = 0xFFFFFFFF.toInt()
        binding.tvFl.setTextColor(defaultColor)
        binding.tvFr.setTextColor(defaultColor)
        binding.tvRl.setTextColor(defaultColor)
        binding.tvRr.setTextColor(defaultColor)
    }

    private fun showUpdateDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_tire_pressure, null)
        val etFrontLeft = dialogView.findViewById<EditText>(R.id.etFrontLeft)
        val etFrontRight = dialogView.findViewById<EditText>(R.id.etFrontRight)
        val etRearLeft = dialogView.findViewById<EditText>(R.id.etRearLeft)
        val etRearRight = dialogView.findViewById<EditText>(R.id.etRearRight)
        val etRecommended = dialogView.findViewById<EditText>(R.id.etRecommended)
        val etNotes = dialogView.findViewById<EditText>(R.id.etNotes)
        val btnSave = dialogView.findViewById<TextView>(R.id.btnSave)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)

        viewModel.latestRecord.value?.let { record ->
            etFrontLeft.setText(String.format("%.1f", record.frontLeft))
            etFrontRight.setText(String.format("%.1f", record.frontRight))
            etRearLeft.setText(String.format("%.1f", record.rearLeft))
            etRearRight.setText(String.format("%.1f", record.rearRight))
            etRecommended.setText(String.format("%.1f", record.recommendedPressure))
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Update Tire Pressure")
            .setView(dialogView)
            .create()

        btnSave.setOnClickListener {
            val fl = etFrontLeft.text.toString().toDoubleOrNull()
            val fr = etFrontRight.text.toString().toDoubleOrNull()
            val rl = etRearLeft.text.toString().toDoubleOrNull()
            val rr = etRearRight.text.toString().toDoubleOrNull()
            val rec = etRecommended.text.toString().toDoubleOrNull()
            val notes = etNotes.text.toString().trim()

            if (fl == null || fr == null || rl == null || rr == null || rec == null) {
                Toast.makeText(requireContext(), "Please fill all pressure fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val date = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
            viewModel.addRecord(fl, fr, rl, rr, rec, date, notes)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showDeleteDialog(record: TirePressureEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Record")
            .setMessage("Are you sure you want to delete this tire pressure record?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteRecord(record)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
