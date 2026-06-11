package com.car.play.android.app.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.Adapters.SupportedCar
import com.car.play.android.app.Adapters.SupportedCarAdapter
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentCarsDatabaseBinding

class CarsDatabaseFragment : Fragment() {

    private lateinit var googleAds: GoogleAds
    private val binding by lazy { FragmentCarsDatabaseBinding.inflate(layoutInflater) }
    private val mController by lazy {
        (requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
    }
    private lateinit var adapter: SupportedCarAdapter
    private var activeFilter = "All"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        googleAds = GoogleAds()
        googleAds.CheckNative(this, binding.nativeAd)
        setupRecyclerView()
        setupSearch()
        setupChips()
        setupClickListeners()
        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = SupportedCarAdapter(buildCarDatabase())
        binding.rvCars.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCars.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filterBySearch(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupChips() {
        val chips = listOf(
            binding.chipAll to "All",
            binding.chipCarplay to "CarPlay",
            binding.chipAndroidAuto to "Android Auto",
            binding.chipBluetooth to "Bluetooth",
            binding.chipWifi to "WiFi"
        )

        for ((chip, type) in chips) {
            chip.setOnClickListener {
                activeFilter = type
                adapter.filterByType(type)
                updateChipStates(chips.map { it.first }, chip)
            }
        }
    }

    private fun updateChipStates(allChips: List<TextView>, selected: TextView) {
        for (chip in allChips) {
            chip.setBackgroundResource(
                if (chip == selected) R.drawable.edittext_bg else R.drawable.card_bg
            )
        }
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            mController.popBackStack()
        }
    }

    private fun buildCarDatabase(): List<SupportedCar> {
        return listOf(
            SupportedCar("Toyota Camry", "2018+", true, true, true, true, false),
            SupportedCar("Toyota RAV4", "2019+", true, true, true, true, false),
            SupportedCar("Toyota Corolla", "2020+", true, true, true, true, false),
            SupportedCar("Honda Civic", "2019+", true, true, true, true, false),
            SupportedCar("Honda Accord", "2018+", true, true, true, true, false),
            SupportedCar("Honda CR-V", "2019+", true, true, true, true, false),
            SupportedCar("BMW 3 Series", "2019+", true, true, true, true, true, wirelessCarPlay = true),
            SupportedCar("BMW 5 Series", "2019+", true, true, true, true, true, wirelessCarPlay = true),
            SupportedCar("BMW X3", "2020+", true, true, true, true, true, wirelessCarPlay = true),
            SupportedCar("Mercedes C-Class", "2020+", true, true, true, true, true, wirelessCarPlay = true),
            SupportedCar("Mercedes E-Class", "2021+", true, true, true, true, true, wirelessCarPlay = true),
            SupportedCar("Mercedes GLC", "2020+", true, true, true, true, true, wirelessCarPlay = true),
            SupportedCar("Tesla Model 3", "2017+", false, false, true, false, true),
            SupportedCar("Tesla Model Y", "2020+", false, false, true, false, true),
            SupportedCar("Tesla Model S", "2021+", false, false, true, false, true),
            SupportedCar("Ford F-150", "2020+", true, true, true, true, false),
            SupportedCar("Ford Mustang", "2020+", true, true, true, true, false),
            SupportedCar("Ford Explorer", "2020+", true, true, true, true, false),
            SupportedCar("Chevrolet Malibu", "2019+", true, true, true, true, false),
            SupportedCar("Chevrolet Silverado", "2020+", true, true, true, true, false),
            SupportedCar("Chevrolet Equinox", "2018+", true, true, true, true, false),
            SupportedCar("Audi A4", "2020+", true, true, true, true, true, wirelessCarPlay = true),
            SupportedCar("Audi Q5", "2020+", true, true, true, true, true, wirelessCarPlay = true),
            SupportedCar("Audi A6", "2019+", true, true, true, true, true, wirelessCarPlay = true),
            SupportedCar("Hyundai Sonata", "2020+", true, true, true, true, false),
            SupportedCar("Hyundai Tucson", "2021+", true, true, true, true, false),
            SupportedCar("Hyundai Elantra", "2021+", true, true, true, true, false),
            SupportedCar("Kia Sportage", "2020+", true, true, true, true, false),
            SupportedCar("Kia Seltos", "2021+", true, true, true, true, false),
            SupportedCar("Kia K5", "2021+", true, true, true, true, false),
            SupportedCar("Volkswagen Golf", "2020+", true, true, true, true, false),
            SupportedCar("Volkswagen Tiguan", "2019+", true, true, true, true, false),
            SupportedCar("Nissan Altima", "2019+", true, true, true, true, false),
            SupportedCar("Nissan Rogue", "2020+", true, true, true, true, false),
            SupportedCar("Mazda CX-5", "2018+", true, true, true, true, false),
            SupportedCar("Mazda 3", "2019+", true, true, true, true, false),
            SupportedCar("Subaru Outback", "2020+", true, true, true, true, false),
            SupportedCar("Subaru Forester", "2019+", true, true, true, true, false),
            SupportedCar("Lexus RX", "2020+", true, true, true, true, false),
            SupportedCar("Lexus ES", "2019+", true, true, true, true, false),
            SupportedCar("Volvo XC60", "2020+", true, true, true, true, true, wirelessCarPlay = true),
            SupportedCar("Volvo XC90", "2019+", true, true, true, true, true, wirelessCarPlay = true),
            SupportedCar("Jeep Grand Cherokee", "2020+", true, true, true, true, false),
            SupportedCar("Jeep Wrangler", "2018+", true, true, true, true, false),
            SupportedCar("Ram 1500", "2019+", true, true, true, true, false),
            SupportedCar("Genesis G70", "2020+", true, true, true, true, true, wirelessCarPlay = true),
            SupportedCar("Porsche Cayenne", "2019+", true, true, true, true, true, wirelessCarPlay = true),
            SupportedCar("Acura TLX", "2021+", true, true, true, true, false)
        )
    }
}
