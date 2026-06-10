package com.car.play.android.app.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.navigation.fragment.NavHostFragment
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.R
import com.car.play.android.app.Utils.ImageModel
import com.car.play.android.app.databinding.FragmentShowDocumentBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ShowDocument : Fragment() {
    private lateinit var googleAds: GoogleAds
    private val binding by lazy { FragmentShowDocumentBinding.inflate(layoutInflater) }
    private val mController by lazy { (requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController }
    private lateinit var documentAdapter: DocumentAdapter
    private val imageFiles = mutableListOf<ImageModel>()  // Mutable list to allow deletion

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        googleAds = GoogleAds()
        googleAds.CheckNative(this, binding.nativeAd)
        binding.documentrecyclerview.layoutManager = GridLayoutManager(requireContext(), 1)

        // Create adapter with a delete lambda
        documentAdapter = DocumentAdapter(imageFiles, onItemClick = { imageModel ->
            // Handle item click to navigate to MediaFragment
            val clickedImageIndex = imageFiles.indexOf(imageModel)
            val imagePaths = imageFiles.map { it.imagePath }.toTypedArray()
            val imageTitles = imageFiles.map { it.title }.toTypedArray() // List of titles
            val imageDates = imageFiles.map { it.date }.toTypedArray()
            val action = ShowDocumentDirections.actionShowdocumentfragmentToMediafragment(
                imagePathList = imagePaths,
                initialImagePath = imageModel.imagePath,
                clickedImageIndex = clickedImageIndex,
                imageTitleList = imageTitles, // Add the image titles here
                imageDateList = imageDates
            )
            mController.navigate(action)

        }, onDeleteClick = { position ->
            // Handle delete click
            deleteImage(position)
        })

        binding.documentrecyclerview.adapter = documentAdapter

        loadImages()
        setupClickListeners()
        return binding.root
    }

    private fun loadImages() {
        val mediaStorageDir = File(requireContext().getExternalFilesDir(null), "MyApp")
        if (mediaStorageDir.exists()) {
            val imageFilesList = mediaStorageDir.listFiles { file -> file.isFile && file.name.endsWith(".jpg") }
            if (imageFilesList != null && imageFilesList.isNotEmpty()) {
                imageFiles.clear()
                imageFilesList.forEach { file ->
                    val title = file.nameWithoutExtension // Use the file name (without extension) as the title
                    val lastModifiedDate = file.lastModified()
                    val dateSaved = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(lastModifiedDate))
                    imageFiles.add(ImageModel(file.absolutePath, title,dateSaved))
                }
                documentAdapter.notifyDataSetChanged()
                toggleRecyclerViewVisibility(true)
            } else {
                toggleRecyclerViewVisibility(false)
            }
        } else {
            toggleRecyclerViewVisibility(false)
        }
    }

    private fun toggleRecyclerViewVisibility(hasImages: Boolean) {
        if (hasImages) {
            binding.documentrecyclerview.visibility = View.VISIBLE
            binding.emptyImg.visibility = View.GONE
        } else {
            binding.documentrecyclerview.visibility = View.GONE
            binding.emptyImg.visibility = View.VISIBLE
        }
    }

    private fun deleteImage(position: Int) {
        // Remove the image from the data source
        val deletedImage = imageFiles[position]

        // Optionally, delete the file from storage
        val file = File(deletedImage.imagePath)
        if (file.exists()) {
            file.delete()  // Delete the file from storage
        }

        // Remove from the list and notify adapter
        imageFiles.removeAt(position)
        documentAdapter.notifyItemRemoved(position)

        // If no images left, hide the RecyclerView
        if (imageFiles.isEmpty()) {
            toggleRecyclerViewVisibility(false)
        }
    }

    private fun setupClickListeners() {
        binding.addBtn.setOnClickListener {
            mController.navigate(R.id.action_showdocumentfragment_to_documentfragment)
        }
        binding.icBack.setOnClickListener {
            mController.popBackStack()
        }
    }

    private fun handleBackPress() {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onbackpressedEvent()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )
    }

    private fun onbackpressedEvent() {
        mController.navigate(R.id.action_showdocumentfragment_to_homefragment)
    }
}
