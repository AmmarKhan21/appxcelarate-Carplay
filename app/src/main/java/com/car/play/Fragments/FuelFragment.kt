package com.car.play.android.app.Fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.Adapters.FuelAdapter
import com.car.play.android.app.Adapters.FuelDisplay
import com.car.play.android.app.databinding.DialogAddFuelBinding
import com.car.play.android.app.databinding.FragmentFuelBinding
import com.car.play.android.app.db.FuelEntryEntity
import com.car.play.android.app.db.FuelViewModel
import java.util.Locale

class FuelFragment : Fragment() {

    private var _binding: FragmentFuelBinding? = null
    private val binding get() = _binding!!

    private lateinit var googleAds: GoogleAds
    private lateinit var viewModel: FuelViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFuelBinding.inflate(inflater, container, false)
        googleAds = GoogleAds()
        viewModel = ViewModelProvider(this).get(FuelViewModel::class.java)

        binding.fuelRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.icBack.setOnClickListener { findNavController().popBackStack() }
        binding.fab.setOnClickListener { showAddDialog() }

        googleAds.CheckNative(this, binding.nativeAd)

        viewModel.allEntries.observe(viewLifecycleOwner) { entries ->
            render(entries)
        }
        return binding.root
    }

    private fun render(entries: List<FuelEntryEntity>) {
        _binding ?: return
        if (entries.isEmpty()) {
            binding.emptyView.visibility = View.VISIBLE
            binding.fuelRecycler.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.GONE
            binding.fuelRecycler.visibility = View.VISIBLE
        }

        val ascending = entries.sortedBy { it.odometer }
        val economyMap = HashMap<Long, String>()

        var prevFullOdo: Double? = null
        var litersSincePrevFull = 0.0
        var firstFullOdo: Double? = null
        var consumedSinceFirst = 0.0
        var overallEconomy: Double? = null

        for (e in ascending) {
            if (prevFullOdo != null) litersSincePrevFull += e.liters
            if (firstFullOdo != null) consumedSinceFirst += e.liters

            if (e.fullTank) {
                val pf = prevFullOdo
                if (pf != null && litersSincePrevFull > 0) {
                    val dist = e.odometer - pf
                    if (dist > 0) {
                        economyMap[e.id] =
                            String.format(Locale.getDefault(), "%.1f km/L", dist / litersSincePrevFull)
                    }
                }
                prevFullOdo = e.odometer
                litersSincePrevFull = 0.0

                if (firstFullOdo == null) {
                    firstFullOdo = e.odometer
                    consumedSinceFirst = 0.0
                } else if (consumedSinceFirst > 0) {
                    overallEconomy = (e.odometer - firstFullOdo) / consumedSinceFirst
                }
            }
        }

        val displays = entries.map { FuelDisplay(it, economyMap[it.id]) }
        binding.fuelRecycler.adapter = FuelAdapter(displays) { entry -> viewModel.deleteEntry(entry) }

        binding.tvEconomy.text =
            overallEconomy?.let { String.format(Locale.getDefault(), "%.1f", it) } ?: "--"
        binding.tvSpent.text =
            String.format(Locale.getDefault(), "%,.0f", entries.sumOf { it.totalCost })
        binding.tvCount.text = entries.size.toString()
    }

    private fun showAddDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogAddFuelBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnSave.setOnClickListener {
            val odo = dialogBinding.etOdometer.text.toString().toDoubleOrNull()
            val liters = dialogBinding.etLiters.text.toString().toDoubleOrNull()
            val cost = dialogBinding.etCost.text.toString().toDoubleOrNull()

            if (odo == null || liters == null || liters <= 0.0) {
                Toast.makeText(
                    requireContext(),
                    "Enter odometer and litres",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            viewModel.addEntry(
                dateMillis = System.currentTimeMillis(),
                odometer = odo,
                liters = liters,
                totalCost = cost ?: 0.0,
                fullTank = dialogBinding.swFullTank.isChecked
            )
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
