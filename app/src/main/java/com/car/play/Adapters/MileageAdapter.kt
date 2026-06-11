package com.car.play.android.app.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.car.play.android.app.databinding.ItemMileageBinding
import com.car.play.android.app.db.MileageLogEntity

class MileageAdapter(
    private var mileageList: List<MileageLogEntity>,
    private val onDeleteClick: (MileageLogEntity) -> Unit
) : RecyclerView.Adapter<MileageAdapter.MileageViewHolder>() {

    inner class MileageViewHolder(val binding: ItemMileageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MileageViewHolder {
        val binding = ItemMileageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MileageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MileageViewHolder, position: Int) {
        val log = mileageList[position]
        holder.binding.apply {
            tvDate.text = log.date
            tvDistance.text = "${String.format("%.1f", log.distance)} mi"
            tvPurpose.text = log.purpose
            tvRoute.text = "${log.startLocation} → ${log.endLocation}"
            if (log.isBusinessTrip) {
                tvBusinessTag.visibility = View.VISIBLE
            } else {
                tvBusinessTag.visibility = View.GONE
            }
            ivDelete.setOnClickListener { onDeleteClick(log) }
        }
    }

    override fun getItemCount() = mileageList.size

    fun updateList(newList: List<MileageLogEntity>) {
        mileageList = newList
        notifyDataSetChanged()
    }
}
