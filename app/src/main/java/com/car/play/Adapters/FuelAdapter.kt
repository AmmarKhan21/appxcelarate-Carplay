package com.car.play.android.app.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.car.play.android.app.databinding.ItemFuelBinding
import com.car.play.android.app.db.FuelEntryEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FuelDisplay(
    val entry: FuelEntryEntity,
    val economyText: String?
)

class FuelAdapter(
    private val items: List<FuelDisplay>,
    private val onDelete: (FuelEntryEntity) -> Unit
) : RecyclerView.Adapter<FuelAdapter.VH>() {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    inner class VH(val binding: ItemFuelBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemFuelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val display = items[position]
        val entry = display.entry
        val b = holder.binding

        b.tvOdo.text = String.format(Locale.getDefault(), "%,d km", entry.odometer.toLong())
        b.tvDetails.text = String.format(
            Locale.getDefault(),
            "%.1f L%s",
            entry.liters,
            if (entry.fullTank) " · Full" else ""
        )
        b.tvDate.text = dateFormat.format(Date(entry.dateMillis))
        b.tvCost.text = String.format(Locale.getDefault(), "%.2f", entry.totalCost)

        if (display.economyText != null) {
            b.tvEconomy.visibility = View.VISIBLE
            b.tvEconomy.text = display.economyText
        } else {
            b.tvEconomy.visibility = View.GONE
        }

        b.ivDelete.setOnClickListener { onDelete(entry) }
    }
}
