package com.car.play.android.app.Adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.car.play.android.app.R
import com.car.play.android.app.databinding.ItemReminderBinding
import com.car.play.android.app.db.ReminderEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

class ReminderAdapter(
    private val items: List<ReminderEntity>,
    private val onDelete: (ReminderEntity) -> Unit
) : RecyclerView.Adapter<ReminderAdapter.VH>() {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    inner class VH(val binding: ItemReminderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemReminderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val reminder = items[position]
        val b = holder.binding

        b.tvTitle.text = reminder.title
        b.tvDue.text = dateFormat.format(Date(reminder.dueDateMillis))
        b.ivType.setImageResource(iconForType(reminder.type))

        if (reminder.note.isBlank()) {
            b.tvNote.visibility = View.GONE
        } else {
            b.tvNote.visibility = View.VISIBLE
            b.tvNote.text = reminder.note
        }

        val now = System.currentTimeMillis()
        val daysLeft = ceil(
            (reminder.dueDateMillis - now).toDouble() / TimeUnit.DAYS.toMillis(1)
        ).toInt()

        when {
            daysLeft < 0 -> {
                b.tvStatus.text = "Overdue"
                b.tvStatus.setTextColor(Color.parseColor("#FF5A5F"))
            }
            daysLeft == 0 -> {
                b.tvStatus.text = "Due today"
                b.tvStatus.setTextColor(Color.parseColor("#FFB020"))
            }
            else -> {
                b.tvStatus.text = "${daysLeft}d left"
                b.tvStatus.setTextColor(Color.parseColor("#2ECC71"))
            }
        }

        b.ivDelete.setOnClickListener { onDelete(reminder) }
    }

    private fun iconForType(type: String): Int {
        return when (type.lowercase(Locale.getDefault())) {
            "insurance", "registration", "license" -> R.drawable.ic_cp_shield
            "service" -> R.drawable.ic_cp_economy
            else -> R.drawable.ic_cp_bell
        }
    }
}
