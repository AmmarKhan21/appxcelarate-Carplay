package com.car.play.android.app.Fragments

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentAddCarBinding
import com.car.play.android.app.db.CarProfileViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class AddCarFragment : Fragment() {

    private val binding by lazy { FragmentAddCarBinding.inflate(layoutInflater) }
    private val mController by lazy {
        (requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
    }
    private lateinit var googleAds: GoogleAds
    private lateinit var viewModel: CarProfileViewModel

    private var selectedImagePath: String = ""

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { handleImageUri(it) }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        bitmap?.let { handleCameraBitmap(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        googleAds = GoogleAds()
        viewModel = ViewModelProvider(requireActivity())[CarProfileViewModel::class.java]

        setupClickListeners()

        return binding.root
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { mController.popBackStack() }

        binding.flPhoto.setOnClickListener { showImagePickerDialog() }

        binding.tvPurchaseDate.setOnClickListener { showDatePicker() }

        binding.btnSave.setOnClickListener { validateAndSave() }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Camera", "Gallery")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> cameraLauncher.launch(null)
                    1 -> galleryLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun handleImageUri(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (bitmap != null) {
                binding.ivCarPhoto.setImageBitmap(bitmap)
                binding.tvPhotoHint.visibility = View.GONE
                selectedImagePath = saveImageToInternal(bitmap)
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleCameraBitmap(bitmap: Bitmap) {
        binding.ivCarPhoto.setImageBitmap(bitmap)
        binding.tvPhotoHint.visibility = View.GONE
        selectedImagePath = saveImageToInternal(bitmap)
    }

    private fun saveImageToInternal(bitmap: Bitmap): String {
        val dir = File(requireContext().filesDir, "car_photos")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "car_${UUID.randomUUID()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        return file.absolutePath
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                calendar.set(year, month, day)
                binding.tvPurchaseDate.text = sdf.format(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun validateAndSave() {
        val name = binding.etName.text.toString().trim()
        val make = binding.etMake.text.toString().trim()
        val model = binding.etModel.text.toString().trim()
        val year = binding.etYear.text.toString().trim()
        val color = binding.etColor.text.toString().trim()
        val license = binding.etLicense.text.toString().trim()
        val vin = binding.etVin.text.toString().trim()
        val purchaseDate = binding.tvPurchaseDate.text.toString().trim()

        if (name.isEmpty()) {
            binding.etName.error = "Name is required"
            binding.etName.requestFocus()
            return
        }
        if (make.isEmpty()) {
            binding.etMake.error = "Make is required"
            binding.etMake.requestFocus()
            return
        }
        if (model.isEmpty()) {
            binding.etModel.error = "Model is required"
            binding.etModel.requestFocus()
            return
        }

        viewModel.addCar(
            name = name,
            make = make,
            model = model,
            year = year,
            color = color,
            licensePlate = license,
            vin = vin,
            purchaseDate = if (purchaseDate == "Select date") "" else purchaseDate,
            imagePath = selectedImagePath
        )

        Toast.makeText(requireContext(), "Car saved successfully", Toast.LENGTH_SHORT).show()
        mController.popBackStack()
    }
}
