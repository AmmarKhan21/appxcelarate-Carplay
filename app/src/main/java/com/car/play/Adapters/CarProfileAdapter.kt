package com.car.play.android.app.Adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.car.play.android.app.databinding.ItemCarProfileBinding
import com.car.play.android.app.db.CarProfileEntity
import java.io.File

class CarProfileAdapter(
    private var carList: List<CarProfileEntity>,
    private val onSetActiveClick: (CarProfileEntity) -> Unit,
    private val onDeleteClick: (CarProfileEntity) -> Unit
) : RecyclerView.Adapter<CarProfileAdapter.CarProfileViewHolder>() {

    inner class CarProfileViewHolder(val binding: ItemCarProfileBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarProfileViewHolder {
        val binding = ItemCarProfileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarProfileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarProfileViewHolder, position: Int) {
        val car = carList[position]
        holder.binding.apply {
            tvName.text = car.name
            tvDetails.text = "${car.make} ${car.model} ${car.year}"
            tvPlate.text = if (car.licensePlate.isNotEmpty()) car.licensePlate else "No plate"

            if (car.imagePath.isNotEmpty()) {
                val file = File(car.imagePath)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(car.imagePath)
                    ivCarImage.setImageBitmap(bitmap)
                }
            }

            if (car.isActive) {
                btnSetActive.text = "Active"
                btnSetActive.setBackgroundResource(com.car.play.android.app.R.drawable.submit_btn_bg)
                btnSetActive.isEnabled = false
            } else {
                btnSetActive.text = "Set Active"
                btnSetActive.setBackgroundResource(com.car.play.android.app.R.drawable.card_bg)
                btnSetActive.isEnabled = true
                btnSetActive.setOnClickListener { onSetActiveClick(car) }
            }

            ivDelete.setOnClickListener { onDeleteClick(car) }
        }
    }

    override fun getItemCount() = carList.size

    fun updateList(newList: List<CarProfileEntity>) {
        carList = newList
        notifyDataSetChanged()
    }
}
