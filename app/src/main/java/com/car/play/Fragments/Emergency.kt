package com.car.play.android.app.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentConnectBinding
import com.car.play.android.app.databinding.FragmentEmergencyBinding


class Emergency : Fragment() {

    private val binding by lazy { FragmentEmergencyBinding.inflate(layoutInflater) }
    private val mController by lazy { (requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        return binding.root
    }

}