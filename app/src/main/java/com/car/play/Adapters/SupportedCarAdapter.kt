package com.car.play.android.app.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.car.play.android.app.databinding.ItemSupportedCarBinding

data class SupportedCar(
    val name: String,
    val yearRange: String,
    val hasCarPlay: Boolean,
    val hasAndroidAuto: Boolean,
    val hasBluetooth: Boolean,
    val hasUSB: Boolean,
    val hasWiFi: Boolean,
    val wirelessCarPlay: Boolean = false
)

class SupportedCarAdapter(
    private val allCars: List<SupportedCar>
) : RecyclerView.Adapter<SupportedCarAdapter.ViewHolder>() {

    private var filteredList: List<SupportedCar> = allCars

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSupportedCarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredList[position])
    }

    override fun getItemCount(): Int = filteredList.size

    fun filterBySearch(query: String) {
        filteredList = if (query.isEmpty()) {
            allCars
        } else {
            allCars.filter { it.name.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }

    fun filterByType(type: String) {
        filteredList = when (type) {
            "All" -> allCars
            "CarPlay" -> allCars.filter { it.hasCarPlay }
            "Android Auto" -> allCars.filter { it.hasAndroidAuto }
            "Bluetooth" -> allCars.filter { it.hasBluetooth }
            "WiFi" -> allCars.filter { it.hasWiFi }
            else -> allCars
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemSupportedCarBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(car: SupportedCar) {
            binding.tvCarName.text = car.name
            binding.tvYear.text = car.yearRange

            val isFullSupport = car.hasCarPlay && car.hasAndroidAuto
            binding.ivStatus.setImageResource(
                if (isFullSupport) android.R.drawable.presence_online
                else android.R.drawable.presence_away
            )

            binding.badgeCarplay.visibility = if (car.hasCarPlay) View.VISIBLE else View.GONE
            if (car.wirelessCarPlay) binding.badgeCarplay.text = "CarPlay (W)"

            binding.badgeAndroidAuto.visibility = if (car.hasAndroidAuto) View.VISIBLE else View.GONE
            binding.badgeBluetooth.visibility = if (car.hasBluetooth) View.VISIBLE else View.GONE
            binding.badgeUsb.visibility = if (car.hasUSB) View.VISIBLE else View.GONE
            binding.badgeWifi.visibility = if (car.hasWiFi) View.VISIBLE else View.GONE
        }
    }
}
