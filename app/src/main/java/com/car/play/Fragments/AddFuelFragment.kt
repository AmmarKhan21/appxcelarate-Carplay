package com.car.play.android.app.Fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentAddFuelBinding
import com.car.play.android.app.db.FuelViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddFuelFragment : Fragment() {

    private lateinit var binding: FragmentAddFuelBinding
    private lateinit var googleAds: GoogleAds
    private lateinit var viewModel: FuelViewModel
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddFuelBinding.inflate(inflater, container, false)
        googleAds = GoogleAds()
        googleAds.CheckBanner(this, binding.bannerAd)
        viewModel = ViewModelProvider(requireActivity()).get(FuelViewModel::class.java)

        setupDatePicker()
        setupFuelTypeSpinner()
        setupAutoCalculation()
        setupClickListeners()

        return binding.root
    }

    private fun setupDatePicker() {
        binding.etDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    val format = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    binding.etDate.setText(format.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupFuelTypeSpinner() {
        val fuelTypes = arrayOf("Petrol", "Diesel", "CNG", "Electric")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, fuelTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFuelType.adapter = adapter
    }

    private fun setupAutoCalculation() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calculateTotal()
            }
        }
        binding.etLiters.addTextChangedListener(watcher)
        binding.etCostPerLiter.addTextChangedListener(watcher)
    }

    private fun calculateTotal() {
        val liters = binding.etLiters.text.toString().toDoubleOrNull() ?: 0.0
        val costPerLiter = binding.etCostPerLiter.text.toString().toDoubleOrNull() ?: 0.0
        val total = liters * costPerLiter
        if (total > 0) {
            binding.etTotalCost.setText(String.format("%.2f", total))
        }
    }

    private fun setupClickListeners() {
        binding.icBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnSave.setOnClickListener { validateAndSave() }
    }

    private fun validateAndSave() {
        val date = binding.etDate.text.toString().trim()
        val litersStr = binding.etLiters.text.toString().trim()
        val costPerLiterStr = binding.etCostPerLiter.text.toString().trim()
        val totalCostStr = binding.etTotalCost.text.toString().trim()
        val odometerStr = binding.etOdometer.text.toString().trim()
        val fuelType = binding.spinnerFuelType.selectedItem?.toString() ?: ""
        val station = binding.etStation.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()

        if (date.isEmpty()) {
            Toast.makeText(requireContext(), "Please select a date", Toast.LENGTH_SHORT).show()
            return
        }
        if (litersStr.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter liters", Toast.LENGTH_SHORT).show()
            return
        }
        if (costPerLiterStr.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter cost per liter", Toast.LENGTH_SHORT).show()
            return
        }

        val liters = litersStr.toDoubleOrNull() ?: 0.0
        val costPerLiter = costPerLiterStr.toDoubleOrNull() ?: 0.0
        val totalCost = totalCostStr.toDoubleOrNull() ?: (liters * costPerLiter)
        val odometer = odometerStr.toDoubleOrNull() ?: 0.0

        viewModel.addFuelRecord(date, liters, costPerLiter, totalCost, odometer, fuelType, station, notes)
        Toast.makeText(requireContext(), "Fuel record saved", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }
}
