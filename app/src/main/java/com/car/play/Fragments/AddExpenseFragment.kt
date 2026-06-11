package com.car.play.android.app.Fragments

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.databinding.FragmentAddExpenseBinding
import com.car.play.android.app.db.ExpenseViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseFragment : Fragment() {

    private lateinit var binding: FragmentAddExpenseBinding
    private lateinit var googleAds: GoogleAds
    private lateinit var viewModel: ExpenseViewModel
    private var selectedDate: String = ""
    private var receiptPath: String = ""
    private var photoUri: Uri? = null

    private val categories = arrayOf("Fuel", "Maintenance", "Insurance", "Parking", "Tolls", "Fines", "Other")

    companion object {
        private const val REQUEST_CAMERA = 2001
        private const val REQUEST_GALLERY = 2002
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        googleAds = GoogleAds()
        viewModel = ViewModelProvider(requireActivity()).get(ExpenseViewModel::class.java)

        setupSpinner()
        setupDatePicker()
        setupButtons()

        return binding.root
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        selectedDate = dateFormat.format(calendar.time)
        binding.tvDate.text = selectedDate

        binding.tvDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDate = dateFormat.format(calendar.time)
                    binding.tvDate.text = selectedDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupButtons() {
        binding.icBack.setOnClickListener { findNavController().popBackStack() }

        binding.btnAttachReceipt.setOnClickListener {
            showImagePickerDialog()
        }

        binding.btnSave.setOnClickListener {
            saveExpense()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Attach Receipt")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openCamera() {
        try {
            val photoFile = createImageFile()
            photoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                photoFile
            )
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            startActivityForResult(intent, REQUEST_CAMERA)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Unable to open camera", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("RECEIPT_${timestamp}_", ".jpg", storageDir).also {
            receiptPath = it.absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CAMERA -> {
                    binding.ivReceiptPreview.visibility = View.VISIBLE
                    binding.ivReceiptPreview.setImageURI(photoUri)
                }
                REQUEST_GALLERY -> {
                    data?.data?.let { uri ->
                        receiptPath = uri.toString()
                        binding.ivReceiptPreview.visibility = View.VISIBLE
                        binding.ivReceiptPreview.setImageURI(uri)
                    }
                }
            }
        }
    }

    private fun saveExpense() {
        val title = binding.etTitle.text.toString().trim()
        val amountStr = binding.etAmount.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()
        val category = categories[binding.spinnerCategory.selectedItemPosition]

        if (title.isEmpty()) {
            binding.etTitle.error = "Title is required"
            return
        }
        if (amountStr.isEmpty()) {
            binding.etAmount.error = "Amount is required"
            return
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            binding.etAmount.error = "Enter a valid amount"
            return
        }

        viewModel.addExpense(
            title = title,
            amount = amount,
            category = category,
            date = selectedDate,
            notes = notes,
            receiptPath = receiptPath
        )

        Toast.makeText(requireContext(), "Expense saved!", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }
}
