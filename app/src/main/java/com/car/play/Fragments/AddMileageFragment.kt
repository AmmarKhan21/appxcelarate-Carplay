package com.car.play.android.app.Fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.car.play.android.app.databinding.FragmentAddMileageBinding
import com.car.play.android.app.db.MileageLogViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddMileageFragment : Fragment() {

    private lateinit var binding: FragmentAddMileageBinding
    private lateinit var viewModel: MileageLogViewModel
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddMileageBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(MileageLogViewModel::class.java)

        setupDatePicker()
        setupClickListeners()

        return binding.root
    }

    private fun setupDatePicker() {
        binding.tvDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    val format = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    binding.tvDate.text = format.format(calendar.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupClickListeners() {
        binding.icBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnSave.setOnClickListener { validateAndSave() }
    }

    private fun validateAndSave() {
        val date = binding.tvDate.text.toString().trim()
        val startOdometerStr = binding.etStartOdometer.text.toString().trim()
        val endOdometerStr = binding.etEndOdometer.text.toString().trim()
        val purpose = binding.etPurpose.text.toString().trim()
        val startLocation = binding.etStartLocation.text.toString().trim()
        val endLocation = binding.etEndLocation.text.toString().trim()
        val isBusinessTrip = binding.switchBusiness.isChecked
        val notes = binding.etNotes.text.toString().trim()

        if (date.isEmpty() || date == "Select date") {
            Toast.makeText(requireContext(), "Please select a date", Toast.LENGTH_SHORT).show()
            return
        }
        if (startOdometerStr.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter start odometer", Toast.LENGTH_SHORT).show()
            return
        }
        if (endOdometerStr.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter end odometer", Toast.LENGTH_SHORT).show()
            return
        }

        val startOdometer = startOdometerStr.toDoubleOrNull() ?: 0.0
        val endOdometer = endOdometerStr.toDoubleOrNull() ?: 0.0

        if (endOdometer <= startOdometer) {
            Toast.makeText(requireContext(), "End odometer must be greater than start", Toast.LENGTH_SHORT).show()
            return
        }

        val distance = endOdometer - startOdometer

        viewModel.addLog(
            date, startOdometer, endOdometer, distance,
            purpose, startLocation, endLocation, isBusinessTrip, notes
        )
        Toast.makeText(requireContext(), "Mileage log saved", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }
}
