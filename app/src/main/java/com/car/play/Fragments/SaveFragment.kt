package com.car.play.android.app.Fragments

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentSaveBinding
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
class SaveFragment : Fragment(R.layout.fragment_save) {

    private lateinit var binding: FragmentSaveBinding
    private lateinit var googleAds: GoogleAds
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentSaveBinding.bind(view)
        binding.icBack.setOnClickListener{
            parentFragmentManager.popBackStack()
        }
        googleAds = GoogleAds()

        googleAds.CheckNativesmall(this@SaveFragment,binding.nativeAd)
        // Retrieve the image URI passed via the NavController
        val imageUriString = arguments?.getString("image_uri")
        val imageUri: Uri? = Uri.parse(imageUriString)

        // Display the image if the URI is valid
        imageUri?.let {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                binding.imageView.setImageBitmap(bitmap)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Image not found", Toast.LENGTH_SHORT).show()
            }
        }

        // Save Button click listener
        binding.saveButton.setOnClickListener {
            val title = binding.titleEditText.text.toString().trim()
            if (title.isNotEmpty()) {
                saveImageWithTitle(imageUri, title)
            } else {
                Toast.makeText(requireContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImageWithTitle(uri: Uri?, title: String) {
        uri?.let {
            try {
                // Open an InputStream from the URI
                val inputStream: InputStream = requireContext().contentResolver.openInputStream(it)!!

                // Sanitize the title to remove invalid characters
                val sanitizedTitle = title.replace(Regex("[^a-zA-Z0-9_]"), "_")

                // Create the target directory and file (force .jpg extension)
                val directory = File(requireContext().getExternalFilesDir(null), "MyApp")
                if (!directory.exists()) {
                    directory.mkdirs()  // Create directory if it doesn't exist
                }

                // Save the file as .jpg
                val file = File(directory, "$sanitizedTitle.jpg")
                val outputStream: OutputStream = FileOutputStream(file)

                // Copy the image to the new location
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()

                // Show a success message
                Toast.makeText(requireContext(), "Image saved as $sanitizedTitle.jpg", Toast.LENGTH_SHORT).show()

                // After saving the image, navigate back or close the fragment
                parentFragmentManager.popBackStack()

            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error saving image", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // Function to extract file extension (JPEG, PNG, etc.)
    private fun getFileExtension(uri: Uri): String? {
        val mimeType = requireContext().contentResolver.getType(uri)
        return mimeType?.split("/")?.get(1)  // Extract extension from MIME type (e.g., image/png -> png)
    }
}
