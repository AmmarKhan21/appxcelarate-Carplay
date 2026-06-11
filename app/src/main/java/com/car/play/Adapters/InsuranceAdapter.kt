package com.car.play.android.app.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.car.play.android.app.databinding.ItemInsuranceBinding
import com.car.play.android.app.db.InsuranceEntity

class InsuranceAdapter(
    private var insuranceList: List<InsuranceEntity>,
    private val onDeleteClick: (InsuranceEntity) -> Unit
) : RecyclerView.Adapter<InsuranceAdapter.InsuranceViewHolder>() {

    inner class InsuranceViewHolder(val binding: ItemInsuranceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InsuranceViewHolder {
        val binding = ItemInsuranceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InsuranceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InsuranceViewHolder, position: Int) {
        val insurance = insuranceList[position]
        holder.binding.apply {
            tvProvider.text = insurance.provider
            tvPolicyNumber.text = "Policy: ${insurance.policyNumber}"
            tvType.text = insurance.type
            tvDates.text = "${insurance.startDate} - ${insurance.endDate}"
            tvPremium.text = "$${String.format("%.2f", insurance.premium)}"
            ivDelete.setOnClickListener { onDeleteClick(insurance) }
        }
    }

    override fun getItemCount() = insuranceList.size

    fun updateList(newList: List<InsuranceEntity>) {
        insuranceList = newList
        notifyDataSetChanged()
    }
}
