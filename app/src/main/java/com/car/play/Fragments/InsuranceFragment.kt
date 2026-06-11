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
import com.car.play.android.app.Adapters.InsuranceAdapter
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentInsuranceBinding
import com.car.play.android.app.db.InsuranceEntity
import com.car.play.android.app.db.InsuranceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class InsuranceFragment : Fragment() {

    private lateinit var binding: FragmentInsuranceBinding
    private lateinit var googleAds: GoogleAds
    private lateinit var viewModel: InsuranceViewModel
    private lateinit var insuranceAdapter: InsuranceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInsuranceBinding.inflate(inflater, container, false)
        googleAds = GoogleAds()
        googleAds.CheckNative(this, binding.nativeAd)
        viewModel = ViewModelProvider(requireActivity()).get(InsuranceViewModel::class.java)

        setupRecyclerView()
        setupClickListeners()
        observeData()

        return binding.root
    }

    private fun setupRecyclerView() {
        insuranceAdapter = InsuranceAdapter(emptyList()) { insurance -> showDeleteDialog(insurance) }
        binding.rvInsurance.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = insuranceAdapter
        }
    }

    private fun setupClickListeners() {
        binding.icBack.setOnClickListener { findNavController().popBackStack() }
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_insurance_to_addInsurance)
        }
    }

    private fun observeData() {
        viewModel.nextExpiring.observe(viewLifecycleOwner, Observer { insurance ->
            if (insurance != null) {
                binding.activeCard.visibility = View.VISIBLE
                binding.tvNoActive.visibility = View.GONE
                binding.tvProvider.text = insurance.provider
                binding.tvPolicyNumber.text = "Policy: ${insurance.policyNumber}"
                binding.tvType.text = insurance.type
                binding.tvExpiry.text = "Expires: ${insurance.endDate}"
                binding.tvCountdown.text = "${calculateDaysUntil(insurance.endDate)} days left"
            } else {
                binding.activeCard.visibility = View.GONE
                binding.tvNoActive.visibility = View.VISIBLE
            }
        })

        viewModel.allInsurance.observe(viewLifecycleOwner, Observer { policies ->
            if (policies.isNullOrEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvInsurance.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.rvInsurance.visibility = View.VISIBLE
                insuranceAdapter.updateList(policies)
            }
        })
    }

    private fun calculateDaysUntil(dateString: String): Long {
        return try {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val endDate = sdf.parse(dateString)
            val today = Date()
            val diff = (endDate?.time ?: 0L) - today.time
            TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            0L
        }
    }

    private fun showDeleteDialog(insurance: InsuranceEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Policy")
            .setMessage("Are you sure you want to delete this insurance policy?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteInsurance(insurance)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
