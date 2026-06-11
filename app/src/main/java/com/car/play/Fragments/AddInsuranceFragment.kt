package com.car.play.android.app.Fragments

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.car.play.android.app.databinding.FragmentAddInsuranceBinding
import com.car.play.android.app.db.InsuranceViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddInsuranceFragment : Fragment() {

    private lateinit var binding: FragmentAddInsuranceBinding
    private lateinit var viewModel: InsuranceViewModel
    private val calendar = Calendar.getInstance()
    private var documentPath: String = ""

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                documentPath = uri.toString()
                binding.btnAttach.text = "Document Attached"
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddInsuranceBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(InsuranceViewModel::class.java)

        setupDatePickers()
        setupClickListeners()

        return binding.root
    }

    private fun setupDatePickers() {
        binding.tvStartDate.setOnClickListener {
            showDatePicker { date -> binding.tvStartDate.text = date }
        }
        binding.tvEndDate.setOnClickListener {
            showDatePicker { date -> binding.tvEndDate.text = date }
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                calendar.set(year, month, day)
                val format = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                onDateSelected(format.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setupClickListeners() {
        binding.icBack.setOnClickListener { findNavController().popBackStack() }

        binding.btnAttach.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            filePickerLauncher.launch(intent)
        }

        binding.btnSave.setOnClickListener { validateAndSave() }
    }

    private fun validateAndSave() {
        val policyNumber = binding.etPolicyNumber.text.toString().trim()
        val provider = binding.etProvider.text.toString().trim()
        val type = binding.etType.text.toString().trim()
        val premiumStr = binding.etPremium.text.toString().trim()
        val startDate = binding.tvStartDate.text.toString().trim()
        val endDate = binding.tvEndDate.text.toString().trim()
        val agentName = binding.etAgentName.text.toString().trim()
        val agentPhone = binding.etAgentPhone.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()

        if (policyNumber.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter policy number", Toast.LENGTH_SHORT).show()
            return
        }
        if (provider.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter provider", Toast.LENGTH_SHORT).show()
            return
        }
        if (startDate.isEmpty() || startDate == "Select start date") {
            Toast.makeText(requireContext(), "Please select start date", Toast.LENGTH_SHORT).show()
            return
        }
        if (endDate.isEmpty() || endDate == "Select end date") {
            Toast.makeText(requireContext(), "Please select end date", Toast.LENGTH_SHORT).show()
            return
        }

        val premium = premiumStr.toDoubleOrNull() ?: 0.0

        viewModel.addInsurance(
            policyNumber, provider, type, premium,
            startDate, endDate, agentName, agentPhone, documentPath, notes
        )
        Toast.makeText(requireContext(), "Insurance policy saved", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }
}
