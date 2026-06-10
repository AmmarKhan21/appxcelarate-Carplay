package com.car.play.android.app.Adapters


import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.car.android.app.carplay.carconnect.interfaces.CarAdapterListener
import com.car.play.android.app.data_classes.CarsData
import com.car.play.android.app.databinding.ItemCarsBinding

class CarAdapter(private val mList: List<CarsData>, private val clickEvent: CarAdapterListener) :
    RecyclerView.Adapter<CarAdapter.ViewHolder>() {
    private var filteredList: List<CarsData> = mList
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCarsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val items = filteredList[position]
        holder.binding.ivCar.setImageResource(items.icon)
        holder.binding.txt.setText(items.name)
        holder.itemView.setOnClickListener {
            clickEvent.onCarClick(position)
        }
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    class ViewHolder(val binding: ItemCarsBinding) : RecyclerView.ViewHolder(binding.root)

    fun filter(query: String) {
        Log.d("queryyy", "Filtering with: $query")
        filteredList = if (query.isEmpty()) {
            Log.d("queryyy", "Empty search, showing all")
            mList
        } else {
            val filtered = mList.filter {
                it.name.contains(query, ignoreCase = true)
            }
            Log.d("queryyy", "Filtered list: ${filtered.size} items")
            filtered
        }
        notifyDataSetChanged()
    }
}
