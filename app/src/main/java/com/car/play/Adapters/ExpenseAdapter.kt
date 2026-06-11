package com.car.play.android.app.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.car.play.android.app.databinding.ItemExpenseBinding
import com.car.play.android.app.db.ExpenseEntity

class ExpenseAdapter(
    private val items: MutableList<ExpenseEntity>,
    private val deleteAction: (ExpenseEntity) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<ExpenseEntity>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class ExpenseViewHolder(private val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ExpenseEntity) {
            binding.tvTitle.text = item.title
            binding.tvAmount.text = String.format("$%.2f", item.amount)
            binding.tvDate.text = item.date
            binding.tvCategoryBadge.text = item.category

            val categoryColor = when (item.category) {
                "Fuel" -> 0xFFFF9800.toInt()
                "Maintenance" -> 0xFF2196F3.toInt()
                "Insurance" -> 0xFF4CAF50.toInt()
                "Parking" -> 0xFF9C27B0.toInt()
                "Tolls" -> 0xFFE91E63.toInt()
                "Fines" -> 0xFFFF0000.toInt()
                else -> 0xFF607D8B.toInt()
            }
            binding.tvCategoryBadge.setTextColor(categoryColor)

            binding.ivDelete.setOnClickListener { deleteAction(item) }
        }
    }
}
