package com.car.play.android.app.Adapters

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.car.play.android.app.databinding.ItemTroubleshootBinding

data class TroubleshootItem(
    val category: String,
    val question: String,
    val answer: String,
    var isExpanded: Boolean = false
)

class TroubleshootAdapter(
    private val items: List<TroubleshootItem>
) : RecyclerView.Adapter<TroubleshootAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTroubleshootBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemTroubleshootBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TroubleshootItem) {
            binding.tvCategory.text = item.category
            binding.tvQuestion.text = item.question
            binding.tvAnswer.text = item.answer
            binding.tvAnswer.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
            binding.ivExpand.rotation = if (item.isExpanded) 180f else 0f

            binding.root.setOnClickListener {
                item.isExpanded = !item.isExpanded

                val targetRotation = if (item.isExpanded) 180f else 0f
                ObjectAnimator.ofFloat(binding.ivExpand, "rotation", targetRotation).apply {
                    duration = 200
                    start()
                }

                binding.tvAnswer.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
            }
        }
    }
}
