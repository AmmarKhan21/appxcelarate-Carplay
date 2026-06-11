package com.car.play.android.app.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.car.play.android.app.R
import com.car.play.android.app.databinding.ItemReminderBinding
import com.car.play.android.app.db.ReminderEntity

class ReminderAdapter(
    private var reminderList: List<ReminderEntity>,
    private val onDeleteClick: (ReminderEntity) -> Unit,
    private val onCompleteClick: (ReminderEntity, Boolean) -> Unit
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    inner class ReminderViewHolder(val binding: ItemReminderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val binding = ItemReminderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReminderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminderList[position]
        holder.binding.apply {
            tvTitle.text = reminder.title
            tvDescription.text = reminder.description
            tvDate.text = "${reminder.date} ${reminder.time}"
            tvCategory.text = reminder.category

            cbComplete.setOnCheckedChangeListener(null)
            cbComplete.isChecked = reminder.isCompleted

            val priorityColor = when (reminder.priority) {
                "High" -> ContextCompat.getColor(root.context, R.color.red)
                "Medium" -> ContextCompat.getColor(root.context, R.color.orange)
                else -> ContextCompat.getColor(root.context, R.color.green)
            }
            priorityIndicator.setBackgroundColor(priorityColor)

            root.alpha = if (reminder.isCompleted) 0.5f else 1.0f

            ivDelete.setOnClickListener { onDeleteClick(reminder) }
            cbComplete.setOnCheckedChangeListener { _, isChecked -> onCompleteClick(reminder, isChecked) }
        }
    }

    override fun getItemCount() = reminderList.size

    fun updateList(newList: List<ReminderEntity>) {
        reminderList = newList
        notifyDataSetChanged()
    }
}
