package com.car.play.android.app.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.NavHostFragment
import com.car.android.app.carplay.carconnect.interfaces.CarAdapterListener
import com.car.android.app.carplay.carconnect.utils.lists.CarsList.vehicalList
import com.car.play.android.app.Adapters.CarAdapter
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentSelectCarBinding

class SelectCar : Fragment(), CarAdapterListener {

    private val binding by lazy { FragmentSelectCarBinding.inflate(layoutInflater) }

    private val mController by lazy { (requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController }
    private lateinit var adapter: CarAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setupRecyclerView()
        setupSearch()
        setupClickListeners()
        handleBackPress()
        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = CarAdapter(vehicalList, this)
        binding.recyclerview.adapter = adapter
    }

    private fun setupSearch() {
        binding.tvSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            mController.popBackStack()
        }
    }



        override fun onCarClick(position: Int) {
            mController.navigate(R.id.action_carSelectionFragment_to_connectfragment)
        }
        private fun handleBackPress() {
            val onBackPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onbackpressedEvent()
                }
            }
            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                onBackPressedCallback
            )
        }

        private fun onbackpressedEvent(){
            mController.navigate(R.id.action_carSelectionFragment_to_homeFragment)
        }
}