package com.car.play.android.app.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.car.play.android.app.databinding.ItemFuelRecordBinding
import com.car.play.android.app.db.FuelEntity

class FuelAdapter(
    private var fuelList: List<FuelEntity>,
    private val onDeleteClick: (FuelEntity) -> Unit
) : RecyclerView.Adapter<FuelAdapter.FuelViewHolder>() {

    inner class FuelViewHolder(val binding: ItemFuelRecordBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FuelViewHolder {
        val binding = ItemFuelRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FuelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FuelViewHolder, position: Int) {
        val fuel = fuelList[position]
        holder.binding.apply {
            tvDate.text = fuel.date
            tvLiters.text = "${fuel.liters}L"
            tvCost.text = "$${String.format("%.2f", fuel.totalCost)}"
            tvStation.text = fuel.station
            tvFuelType.text = fuel.fuelType
            ivDelete.setOnClickListener { onDeleteClick(fuel) }
        }
    }

    override fun getItemCount() = fuelList.size

    fun updateList(newList: List<FuelEntity>) {
        fuelList = newList
        notifyDataSetChanged()
    }
}
