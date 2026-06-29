
package com.car.play.android.app.Fragments
import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.app.lock.hide.apps.secure.utils.dialogs.PhotoAccessPermissionDialog1
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentDocumentBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File

class DocumentFragment : Fragment() {
    private lateinit var googleAds: GoogleAds
    private val binding by lazy { FragmentDocumentBinding.inflate(layoutInflater) }

    // Image picker result launcher for single image selection from gallery
    private val selectImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                // Show a dialog for the user to enter a title for the image
                openSaveFragment(uri)
            }
        }

    // Camera result launcher
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                // Image is captured successfully, show title input dialog
                openSaveFragment(imageUri)
            }
        }

    private lateinit var imageUri: Uri


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        googleAds = GoogleAds()
        googleAds.CheckNative(this, binding.nativeAd)
        setupClickEvent()
        return binding.root
    }

    private fun setupClickEvent() {
        binding.lvCamera.setOnClickListener {
            checkCameraPermission()
        }

        // Gallery Button Click
        binding.lvGallery.setOnClickListener {
            // Open the image picker for a single image
            openGallery()
            disableButtonTemporarily(binding.lvGallery)
        }

        // Back Button Click
        binding.icBack.setOnClickListener {
            // Navigate back using NavController
            findNavController().popBackStack()
        }
    }

    private fun openGallery() {
        // Launch the image picker for single image selection
        selectImageLauncher.launch("image/*")
    }

    private fun openCamera() {
        try {
            val photoFile = File(
                requireContext().cacheDir,
                "document_${System.currentTimeMillis()}.jpg"
            )
            imageUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                photoFile
            )
            takePictureLauncher.launch(imageUri)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Unable to open camera", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to disable button for a short period to prevent multiple clicks
    private fun disableButtonTemporarily(button: View) {
        button.isEnabled = false
        Handler(Looper.getMainLooper()).postDelayed({
            button.isEnabled = true
        }, 1000)
    }

    // Function to open SaveFragment to input title and save image
    private fun openSaveFragment(uri: Uri) {
        // Create a Bundle to pass the URI as an argument
        val bundle = Bundle().apply {
            putString("image_uri", uri.toString())  // Passing the image URI to SaveFragment
        }

        // Navigate to SaveFragment using the NavController and pass the arguments
        findNavController().navigate(R.id.action_document_to_savefragment, bundle)
    }


    private fun checkCameraPermission() {
        Dexter.withContext(this.requireActivity()).withPermissions(Manifest.permission.CAMERA)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        openCamera()
                    } else if (report.isAnyPermissionPermanentlyDenied) {
                        showPermissionDeniedDialog()
                    } else {
                        handleDeniedPermissions(report.deniedPermissionResponses)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }
    private fun handleDeniedPermissions(deniedResponses: Collection<PermissionDeniedResponse>) {
        for (response in deniedResponses) {
            if (response.permissionName == Manifest.permission.CAMERA) {
                showPermissionDeniedDialog()
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        val dialog = PhotoAccessPermissionDialog1(this.requireActivity())
        dialog.show()
    }
}
