package com.car.newcarplay.Fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.car.play.WeatherData.GPS_REQUEST
import com.car.play.WeatherData.GeoLocationService
import com.car.play.WeatherData.GpsHelper
import com.car.play.WeatherData.LOCATION_REQUEST
import com.car.play.WeatherData.MyApp
import com.car.play.WeatherData.RequestStatus
import com.car.play.WeatherData.ResponseWeather
import com.car.play.WeatherData.WeatherDataRepository
import com.car.play.WeatherData.WeatherViewModel
import com.car.play.WeatherData.showToast
import com.car.play.WeatherData.unixTimestampToTimeString
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentWeatherBinding

class WeatherFragment : Fragment() {
    private val binding by lazy { FragmentWeatherBinding.inflate(layoutInflater) }
    private lateinit var viewModel: WeatherViewModel
    private lateinit var model: GeoLocationService
    private lateinit var weatherRepo: WeatherDataRepository
    private var isGPSEnabled = false
    private var lat: String? = null
    private var lon: String? = null
    private var city: String? = null

    private val mController by lazy {
        (requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setupClickEvent()
        handleBackPress()
        return binding.root
    }


    private fun setupClickEvent() {
        binding.icBack.setOnClickListener {onbackpressedEvent() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model = GeoLocationService(requireContext())
        viewModel = ViewModelProvider(this).get(WeatherViewModel::class.java)
        weatherRepo = WeatherDataRepository()
        //checking GPS status
        GpsHelper(requireContext()).turnGPSOn(object : GpsHelper.OnGpsListener {
            override fun gpsStatus(isGPSEnable: Boolean) {
                isGPSEnabled = isGPSEnable
            }
        })

        setUpObservers()


    }override fun onStart() {
        super.onStart()
        invokeLocationAction()
    }

    private fun setUpObservers() {
        viewModel.locationLiveData.observe(viewLifecycleOwner) {
            viewModel.getWeatherByLocation(
                weatherRepo,
                it.latitude.toString(),
                it.longitude.toString()
            )
        }

        viewModel.weatherByLocation.observe(viewLifecycleOwner) {
            it?.let { resource ->
                when (resource.status) {
                    RequestStatus.SUCCESS -> {
                        binding.incInfoWeather.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.GONE
                        binding.animFailed.visibility = View.GONE
                        binding.animNetwork.visibility = View.GONE
                        setUpUI(it.data)
                    }
                    RequestStatus.ERROR -> {
                        showFailedView(it.message)
                    }
                    RequestStatus.LOADING -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.animFailed.visibility = View.GONE
                        binding.animNetwork.visibility = View.GONE
                    }
                }
            }
        }



    }

    private fun showFailedView(message: String?) {
        binding.progressBar.visibility=View.GONE
        binding.incInfoWeather.visibility=View.GONE
        Log.d("message", "$message")
        when(message){
            "Network Failure" -> {
                binding.animFailed.visibility=View.GONE
                binding.animNetwork.visibility=View.VISIBLE
            }
            else ->{
                binding.animNetwork.visibility=View.GONE
                binding.animFailed.visibility=View.VISIBLE
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUpUI(data: ResponseWeather?) {
        binding.tvTemp.text = data?.main?.temp.toString()
        binding.tvCityName.text = data?.name
        binding.tvWeatherCondition.text = data?.weather!![0].main
        binding.tvSunsetTime.text = data.sys.sunrise.unixTimestampToTimeString()
        binding.tvSunsetTime.text = data.sys.sunset.unixTimestampToTimeString()
        binding.tvRealFeelText.text = "${data.main.feelsLike}${getString(R.string.degree_celsius_symbol)}"
        binding.tvCloudinessText.text = "${data.clouds.all}%"
        binding.tvWindSpeedText.text = "${data.wind.speed}m/s"
        binding.tvHumidityText.text = "${data.main.humidity}%"
        binding.tvPressureText.text = "${data.main.pressure}hPa"
        binding.tvVisibilityText.text = "${data.visibility}M"
        lat = data.coord.lat.toString()
        lon = data.coord.lon.toString()
        city = data.name
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GPS_REQUEST) {
                isGPSEnabled = true
                invokeLocationAction()
            }
        }
    }

    private fun invokeLocationAction() {
        when {
            !isGPSEnabled -> showToast(
                requireContext(),
                "Enable GPS",
                1
            )
            isPermissionsGranted() -> startLocationUpdate()
            shouldShowRequestPermissionRationale() -> requestLocationPermission()
            else -> requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_REQUEST
        )
    }

    private fun startLocationUpdate() {
        viewModel.getCurrentLocation(model)
    }

    private fun isPermissionsGranted() =
        ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

    private fun shouldShowRequestPermissionRationale() =
        ActivityCompat.shouldShowRequestPermissionRationale(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) && ActivityCompat.shouldShowRequestPermissionRationale(
            requireActivity(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_REQUEST -> {
                invokeLocationAction()
            }
        }
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
        mController.popBackStack()
    }
}
