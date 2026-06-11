package com.car.play.android.app.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.car.play.android.app.databinding.ItemTimelineBinding

data class ServiceRecord(
    val id: Long = System.currentTimeMillis(),
    val date: String,
    val serviceName: String,
    val cost: Double,
    val mechanic: String,
    val notes: String
)

class TimelineAdapter(
    private var records: List<ServiceRecord>,
    private val onDeleteClick: (ServiceRecord) -> Unit
) : RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder>() {

    inner class TimelineViewHolder(val binding: ItemTimelineBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val binding = ItemTimelineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TimelineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        val record = records[position]
        holder.binding.apply {
            tvDate.text = record.date
            tvServiceName.text = record.serviceName
            tvCost.text = "$${String.format("%.2f", record.cost)}"
            tvMechanic.text = record.mechanic

            if (record.notes.isNotBlank()) {
                tvNotes.text = record.notes
                tvNotes.visibility = View.VISIBLE
            } else {
                tvNotes.visibility = View.GONE
            }

            timelineConnector.visibility = if (position == records.lastIndex) View.INVISIBLE else View.VISIBLE

            ivDelete.setOnClickListener { onDeleteClick(record) }
        }
    }

    override fun getItemCount() = records.size

    fun updateList(newList: List<ServiceRecord>) {
        records = newList
        notifyDataSetChanged()
    }
}
