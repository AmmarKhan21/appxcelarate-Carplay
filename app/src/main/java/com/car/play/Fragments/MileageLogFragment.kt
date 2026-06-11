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
import com.car.play.android.app.Adapters.MileageAdapter
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentMileageLogBinding
import com.car.play.android.app.db.MileageLogEntity
import com.car.play.android.app.db.MileageLogViewModel

class MileageLogFragment : Fragment() {

    private lateinit var binding: FragmentMileageLogBinding
    private lateinit var googleAds: GoogleAds
    private lateinit var viewModel: MileageLogViewModel
    private lateinit var mileageAdapter: MileageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMileageLogBinding.inflate(inflater, container, false)
        googleAds = GoogleAds()
        googleAds.CheckNative(this, binding.nativeAd)
        viewModel = ViewModelProvider(requireActivity()).get(MileageLogViewModel::class.java)

        setupRecyclerView()
        setupClickListeners()
        observeData()

        return binding.root
    }

    private fun setupRecyclerView() {
        mileageAdapter = MileageAdapter(emptyList()) { log -> showDeleteDialog(log) }
        binding.rvMileage.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mileageAdapter
        }
    }

    private fun setupClickListeners() {
        binding.icBack.setOnClickListener { findNavController().popBackStack() }
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_mileage_to_addMileage)
        }
    }

    private fun observeData() {
        viewModel.allLogs.observe(viewLifecycleOwner, Observer { logs ->
            if (logs.isNullOrEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvMileage.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.rvMileage.visibility = View.VISIBLE
                mileageAdapter.updateList(logs)
            }
        })

        viewModel.totalMileage.observe(viewLifecycleOwner, Observer { total ->
            binding.tvTotalMileage.text = "${String.format("%.1f", total ?: 0.0)} mi"
        })

        viewModel.totalBusinessMileage.observe(viewLifecycleOwner, Observer { business ->
            val businessVal = business ?: 0.0
            binding.tvBusinessMileage.text = "${String.format("%.1f", businessVal)} mi"

            val totalVal = viewModel.totalMileage.value ?: 0.0
            val personal = totalVal - businessVal
            binding.tvPersonalMileage.text = "${String.format("%.1f", personal)} mi"
        })
    }

    private fun showDeleteDialog(log: MileageLogEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Log")
            .setMessage("Are you sure you want to delete this mileage log?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteLog(log)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
