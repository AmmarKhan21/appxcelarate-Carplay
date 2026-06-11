package com.car.play.android.app.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.car.play.android.app.databinding.ItemDrivingScoreBinding
import com.car.play.android.app.db.DrivingScoreEntity

class DrivingScoreAdapter(
    private val items: MutableList<DrivingScoreEntity>
) : RecyclerView.Adapter<DrivingScoreAdapter.ScoreViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreViewHolder {
        val binding = ItemDrivingScoreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScoreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScoreViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<DrivingScoreEntity>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class ScoreViewHolder(private val binding: ItemDrivingScoreBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DrivingScoreEntity) {
            binding.tvDate.text = item.date
            binding.tvScore.text = item.overallScore.toString()

            val scoreColor = when {
                item.overallScore >= 80 -> 0xFF008000.toInt()
                item.overallScore >= 60 -> 0xFFFF9800.toInt()
                else -> 0xFFFF0000.toInt()
            }
            binding.tvScore.setTextColor(scoreColor)

            binding.tvDistance.text = String.format("%.1f km", item.distanceDriven)

            val minutes = item.duration / 60000
            binding.tvDuration.text = "${minutes} min"
        }
    }
}
