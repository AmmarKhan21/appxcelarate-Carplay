package com.car.play.android.app.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.car.play.android.app.databinding.ItemTireRecordBinding
import com.car.play.android.app.db.TirePressureEntity

class TirePressureAdapter(
    private var records: List<TirePressureEntity>,
    private val onDeleteClick: (TirePressureEntity) -> Unit
) : RecyclerView.Adapter<TirePressureAdapter.TireViewHolder>() {

    inner class TireViewHolder(val binding: ItemTireRecordBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TireViewHolder {
        val binding = ItemTireRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TireViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TireViewHolder, position: Int) {
        val record = records[position]
        holder.binding.apply {
            tvDate.text = record.date
            tvFL.text = String.format("%.1f", record.frontLeft)
            tvFR.text = String.format("%.1f", record.frontRight)
            tvRL.text = String.format("%.1f", record.rearLeft)
            tvRR.text = String.format("%.1f", record.rearRight)

            val worstDeviation = maxOf(
                kotlin.math.abs(record.frontLeft - record.recommendedPressure) / record.recommendedPressure,
                kotlin.math.abs(record.frontRight - record.recommendedPressure) / record.recommendedPressure,
                kotlin.math.abs(record.rearLeft - record.recommendedPressure) / record.recommendedPressure,
                kotlin.math.abs(record.rearRight - record.recommendedPressure) / record.recommendedPressure
            )

            when {
                worstDeviation <= 0.10 -> {
                    tvStatus.text = "OK - Within Range"
                    tvStatus.setTextColor(0xFF4CAF50.toInt())
                }
                worstDeviation <= 0.20 -> {
                    tvStatus.text = "Warning - Check Tires"
                    tvStatus.setTextColor(0xFFFFC107.toInt())
                }
                else -> {
                    tvStatus.text = "Critical - Adjust Now"
                    tvStatus.setTextColor(0xFFF44336.toInt())
                }
            }

            ivDelete.setOnClickListener { onDeleteClick(record) }
        }
    }

    override fun getItemCount() = records.size

    fun updateList(newList: List<TirePressureEntity>) {
        records = newList
        notifyDataSetChanged()
    }
}
