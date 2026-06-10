package com.car.play.android.app.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.Adapters.ImagePagerAdapter
import com.car.play.android.app.databinding.FragmentMediaBinding

/*
class MediaFragment : Fragment() {

    private lateinit var googleAds: GoogleAds

    private val binding by lazy { FragmentMediaBinding.inflate(layoutInflater) }
    private lateinit var viewPagerAdapter: ImagePagerAdapter
    private lateinit var imageList: List<String>  // List of image paths

    // Use Safe Args to retrieve the arguments passed from ShowDocumentFragment
    private val args: MediaFragmentArgs by navArgs() // Safe Args generated class

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

////        binding = FragmentMediaBinding.inflate(inflater, container, false)
        googleAds = GoogleAds()



        // Get the list of image paths passed from ShowDocumentFragment (via Safe Args)
        imageList = args.imagePathList.toList() // Convert the array to a List

        // Set up the ImagePagerAdapter with the list of image paths
        viewPagerAdapter = ImagePagerAdapter(imageList)
        binding.viewPager.adapter = viewPagerAdapter

        // Get the clicked image index and set it as the initial position in ViewPager2
        val initialImagePath = args.initialImagePath
        val clickedImageIndex = args.clickedImageIndex

        // Find the position of the clicked image in the list
        val initialPosition = imageList.indexOf(initialImagePath)

        // If we have a valid initial image, set the initial position in ViewPager2
        if (initialPosition >= 0) {
            binding.viewPager.setCurrentItem(initialPosition, false)
        }

        loadNative()

        return binding.root
    }

    private fun loadNative() {
        googleAds.CheckBanner(this, binding.bannerAd)
    }
}
*/



class MediaFragment : Fragment() {

    private lateinit var googleAds: GoogleAds
    private val binding by lazy { FragmentMediaBinding.inflate(layoutInflater) }
    private lateinit var viewPagerAdapter: ImagePagerAdapter
    private lateinit var imageList: List<String>  // List of image paths
    private lateinit var imageTitles: List<String> // List of image titles
    private lateinit var imageDates: List<String>  // List of image save dates

    // Use Safe Args to retrieve the arguments passed from ShowDocumentFragment
    private val args: MediaFragmentArgs by navArgs() // Safe Args generated class

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        googleAds = GoogleAds()

        // Get the list of image paths passed from ShowDocumentFragment (via Safe Args)
        imageList = args.imagePathList.toList() // Convert the array to a List

        // Get the list of image titles passed from ShowDocumentFragment (via Safe Args)
        imageTitles = args.imageTitleList.toList() // Convert the array of titles to a List

        // Get the list of image save dates passed from ShowDocumentFragment (via Safe Args)
        imageDates = args.imageDateList.toList() // Convert the array of dates to a List

        // Set up the ImagePagerAdapter with the list of image paths
        viewPagerAdapter = ImagePagerAdapter(imageList)
        binding.viewPager.adapter = viewPagerAdapter

        // Get the clicked image index and set it as the initial position in ViewPager2
        val initialImagePath = args.initialImagePath
        val clickedImageIndex = args.clickedImageIndex

        // Find the position of the clicked image in the list
        val initialPosition = imageList.indexOf(initialImagePath)

        // If we have a valid initial image, set the initial position in ViewPager2
        if (initialPosition >= 0) {
            binding.viewPager.setCurrentItem(initialPosition, false)
        }

        // Update the image title and save date for the current image
        updateImageInfo(initialPosition)

        // Set up a page change callback to update the title and save date as the user swipes
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateImageInfo(position)
            }
        })

        loadNative()

        return binding.root
    }

    // Function to update the image title and save date based on the current position
    private fun updateImageInfo(position: Int) {
        val title = imageTitles.getOrElse(position) { "Unknown Title" }
        val saveDate = imageDates.getOrElse(position) { "Unknown Date" }

        // Update the title in TextView
        binding.tvImageTitle.text = title

        // Update the save date in the TextView (with id `date`)
        binding.date.text = "$saveDate"  // Shows save date
    }

    private fun loadNative() {
        googleAds.CheckBanner(this, binding.bannerAd)
    }
}
