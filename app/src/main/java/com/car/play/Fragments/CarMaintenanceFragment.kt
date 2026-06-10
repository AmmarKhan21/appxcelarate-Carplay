package com.car.play.android.app.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.Adapters.CarMaintenanceAdapter
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentCarMaintenanceBinding
import com.car.play.android.app.db.CarMaintenanceEntity
import com.car.play.android.app.db.CarMaintenanceViewModel

class CarMaintenanceFragment : Fragment() {
    private lateinit var googleAds: GoogleAds
    private lateinit var binding: FragmentCarMaintenanceBinding
    private lateinit var viewModel: CarMaintenanceViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCarMaintenanceBinding.inflate(inflater, container, false)
        googleAds = GoogleAds()
        googleAds.CheckNative(this, binding.nativeAd)
        viewModel = ViewModelProvider(requireActivity()).get(CarMaintenanceViewModel::class.java)

        setupRecyclerView()
        setupAddButton()

        // Observe LiveData for updates
        viewModel.allServices.observe(viewLifecycleOwner, Observer { services ->
            if (services.isEmpty()) {
                binding.emptyImg.visibility = View.VISIBLE
                binding.documentrecyclerview.visibility = View.GONE
            } else {
                binding.emptyImg.visibility = View.GONE
                binding.documentrecyclerview.visibility = View.VISIBLE

                // Pass the delete action to the adapter
                val adapter = CarMaintenanceAdapter(services.toMutableList()) { service ->
                    deleteService(service) // Trigger deletion when delete icon is clicked
                }

                binding.documentrecyclerview.adapter = adapter
            }
        })

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.documentrecyclerview.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupAddButton() {
        binding.icBack.setOnClickListener { findNavController().popBackStack() }
        binding.addBtn.setOnClickListener {
            // Navigate to the AddCarmaintenance fragment
            findNavController().navigate(R.id.action_CarMaintenanceFragment_to_AddCarmaintenance)
        }
    }

    private fun deleteService(service: CarMaintenanceEntity) {
        // Call ViewModel to delete service from database
        viewModel.deleteService(service)
    }
}

