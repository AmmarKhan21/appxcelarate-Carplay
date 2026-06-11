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
import com.car.play.android.app.Adapters.ExpenseAdapter
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentExpenseManagerBinding
import com.car.play.android.app.db.ExpenseEntity
import com.car.play.android.app.db.ExpenseViewModel

class ExpenseManagerFragment : Fragment() {

    private lateinit var binding: FragmentExpenseManagerBinding
    private lateinit var googleAds: GoogleAds
    private lateinit var viewModel: ExpenseViewModel
    private lateinit var expenseAdapter: ExpenseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExpenseManagerBinding.inflate(inflater, container, false)
        googleAds = GoogleAds()
        googleAds.CheckNative(this, binding.nativeAd)
        viewModel = ViewModelProvider(requireActivity()).get(ExpenseViewModel::class.java)

        setupRecyclerView()
        setupButtons()
        observeData()

        return binding.root
    }

    private fun setupRecyclerView() {
        expenseAdapter = ExpenseAdapter(mutableListOf()) { expense ->
            showDeleteConfirmation(expense)
        }
        binding.rvExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExpenses.adapter = expenseAdapter
    }

    private fun setupButtons() {
        binding.icBack.setOnClickListener { findNavController().popBackStack() }
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_expenseManager_to_addExpense)
        }
    }

    private fun observeData() {
        viewModel.allExpenses.observe(viewLifecycleOwner, Observer { expenses ->
            expenseAdapter.updateList(expenses)
        })

        viewModel.totalExpenses.observe(viewLifecycleOwner, Observer { total ->
            binding.tvTotalExpenses.text = String.format("$%.2f", total ?: 0.0)
        })

        viewModel.expenseSummary.observe(viewLifecycleOwner, Observer { summaryList ->
            summaryList.forEach { summary ->
                when (summary.category) {
                    "Fuel" -> binding.tvFuelTotal.text = String.format("$%.0f", summary.total)
                    "Maintenance" -> binding.tvMaintenanceTotal.text = String.format("$%.0f", summary.total)
                    "Insurance" -> binding.tvInsuranceTotal.text = String.format("$%.0f", summary.total)
                    "Parking" -> binding.tvParkingTotal.text = String.format("$%.0f", summary.total)
                    "Tolls" -> binding.tvTollsTotal.text = String.format("$%.0f", summary.total)
                    else -> binding.tvOtherTotal.text = String.format("$%.0f", summary.total)
                }
            }
        })
    }

    private fun showDeleteConfirmation(expense: ExpenseEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Expense")
            .setMessage("Are you sure you want to delete \"${expense.title}\"?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteExpense(expense)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
