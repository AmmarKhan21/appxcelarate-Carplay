package com.car.play.android.app.Fragments

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.Adapters.CarProfileAdapter
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentCarProfileBinding
import com.car.play.android.app.db.CarProfileEntity
import com.car.play.android.app.db.CarProfileViewModel
import java.io.File

class CarProfileFragment : Fragment() {

    private val binding by lazy { FragmentCarProfileBinding.inflate(layoutInflater) }
    private val mController by lazy {
        (requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
    }
    private lateinit var googleAds: GoogleAds
    private lateinit var viewModel: CarProfileViewModel
    private lateinit var carAdapter: CarProfileAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        googleAds = GoogleAds()
        googleAds.CheckNative(this@CarProfileFragment, binding.nativeAd)
        viewModel = ViewModelProvider(requireActivity())[CarProfileViewModel::class.java]

        setupRecyclerView()
        setupClickListeners()
        observeData()

        return binding.root
    }

    private fun setupRecyclerView() {
        carAdapter = CarProfileAdapter(
            emptyList(),
            onSetActiveClick = { car -> viewModel.setActive(car.id) },
            onDeleteClick = { car -> showDeleteDialog(car) }
        )
        binding.rvCars.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = carAdapter
        }
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { mController.popBackStack() }
        binding.fabAdd.setOnClickListener {
            mController.navigate(R.id.action_carProfile_to_addCar)
        }
    }

    private fun observeData() {
        viewModel.activeCar.observe(viewLifecycleOwner) { car ->
            if (car != null) {
                binding.cardActiveCar.visibility = View.VISIBLE
                binding.tvNoActive.visibility = View.GONE
                binding.tvActiveName.text = car.name
                binding.tvActiveDetails.text = "${car.make} ${car.model} ${car.year}"
                binding.tvActivePlate.text = if (car.licensePlate.isNotEmpty()) car.licensePlate else "No plate"

                if (car.imagePath.isNotEmpty()) {
                    val file = File(car.imagePath)
                    if (file.exists()) {
                        val bitmap = BitmapFactory.decodeFile(car.imagePath)
                        binding.ivActiveCarImage.setImageBitmap(bitmap)
                    }
                }
            } else {
                binding.cardActiveCar.visibility = View.GONE
                binding.tvNoActive.visibility = View.VISIBLE
            }
        }

        viewModel.allCars.observe(viewLifecycleOwner) { cars ->
            if (cars.isNullOrEmpty()) {
                binding.rvCars.visibility = View.GONE
                binding.tvEmpty.visibility = View.VISIBLE
            } else {
                binding.rvCars.visibility = View.VISIBLE
                binding.tvEmpty.visibility = View.GONE
                carAdapter.updateList(cars)
            }
        }
    }

    private fun showDeleteDialog(car: CarProfileEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Car")
            .setMessage("Are you sure you want to delete \"${car.name}\"?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteCar(car)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
