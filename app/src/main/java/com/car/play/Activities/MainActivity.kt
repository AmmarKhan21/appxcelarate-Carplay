package com.car.play.android.app.Activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.car.play.GoogleAds.GoogleAds
import com.car.play.GoogleAds.onAdShowed
import com.car.play.Utils.SharedPrefrence
import com.car.play.android.app.R
import com.car.play.android.app.databinding.ActivityCarSplashBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarSplashBinding
    private lateinit var googleAds: GoogleAds
    private val handler = Handler(Looper.getMainLooper())
    private var hasNavigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        com.car.play.android.app.design.DesignTheme.apply(this)
        super.onCreate(savedInstanceState)
        binding = ActivityCarSplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        googleAds = GoogleAds()
        googleAds.loadInter(this@MainActivity)
        // Wait 5 seconds and then call Interstitial
        handler.postDelayed({
            showInterstitial()
        }, 5000)
    }

    private fun showInterstitial() {
        googleAds.CheckInterstitial(this@MainActivity,object : onAdShowed {
            override fun onAdShow() {
                navigateToHome()
            }
        })
    }

    private fun navigateToHome() {
        if (hasNavigated) return
        hasNavigated = true

        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
