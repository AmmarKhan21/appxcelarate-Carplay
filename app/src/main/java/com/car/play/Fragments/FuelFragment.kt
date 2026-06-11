package com.car.play.android.app.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.Adapters.FuelAdapter
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentFuelBinding
import com.car.play.android.app.db.FuelEntity
import com.car.play.android.app.db.FuelViewModel

class FuelFragment : Fragment() {

    private lateinit var binding: FragmentFuelBinding
    private lateinit var googleAds: GoogleAds
    private lateinit var viewModel: FuelViewModel
    private lateinit var fuelAdapter: FuelAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFuelBinding.inflate(inflater, container, false)
        googleAds = GoogleAds()
        googleAds.CheckNative(this, binding.nativeAd)
        viewModel = ViewModelProvider(requireActivity()).get(FuelViewModel::class.java)

        setupRecyclerView()
        setupClickListeners()
        observeData()

        return binding.root
    }

    private fun setupRecyclerView() {
        fuelAdapter = FuelAdapter(emptyList()) { fuel -> showDeleteDialog(fuel) }
        binding.rvFuelRecords.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = fuelAdapter
        }
    }

    private fun setupClickListeners() {
        binding.icBack.setOnClickListener { findNavController().popBackStack() }
        binding.fabAddFuel.setOnClickListener {
            findNavController().navigate(R.id.action_fuelFragment_to_addFuelFragment)
        }
    }

    private fun observeData() {
        viewModel.allFuelRecords.observe(viewLifecycleOwner, Observer { records ->
            if (records.isNullOrEmpty()) {
                binding.emptyImg.visibility = View.VISIBLE
                binding.rvFuelRecords.visibility = View.GONE
            } else {
                binding.emptyImg.visibility = View.GONE
                binding.rvFuelRecords.visibility = View.VISIBLE
                fuelAdapter.updateList(records)
            }
        })

        viewModel.totalFuelCost.observe(viewLifecycleOwner, Observer { total ->
            binding.tvTotalSpent.text = "$${String.format("%.2f", total ?: 0.0)}"
        })

        viewModel.averageCostPerLiter.observe(viewLifecycleOwner, Observer { avg ->
            binding.tvAvgCost.text = "$${String.format("%.2f", avg ?: 0.0)}"
        })

        viewModel.lastFuelRecord.observe(viewLifecycleOwner, Observer { record ->
            binding.tvLastFill.text = record?.date ?: "N/A"
        })
    }

    private fun showDeleteDialog(fuel: FuelEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Record")
            .setMessage("Are you sure you want to delete this fuel record?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteFuelRecord(fuel)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
