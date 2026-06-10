package com.car.play.android.app.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.databinding.FragmentAddCarmaintenanceBinding
import com.car.play.android.app.db.CarMaintenanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AddCarmaintenance : Fragment() {
    private lateinit var googleAds: GoogleAds
    private val binding by lazy { FragmentAddCarmaintenanceBinding.inflate(layoutInflater) }
    private val viewModel: CarMaintenanceViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        googleAds = GoogleAds()
        googleAds.CheckNative(this, binding.nativeAd)
        binding.icBack.setOnClickListener { findNavController().popBackStack() }
        binding.save.setOnClickListener {
            val serviceName = binding.sName.text.toString()
            val cost = binding.cost.text.toString()
            val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
            if (serviceName.isNotEmpty() && cost.isNotEmpty()) {
                viewModel.addService(serviceName, cost,currentDate)
                findNavController().popBackStack() // Close fragment and go back
            }
        }
        return binding.root
    }
}
