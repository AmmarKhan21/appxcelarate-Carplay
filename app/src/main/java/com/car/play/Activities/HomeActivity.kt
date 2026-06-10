package com.car.play.android.app.Activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.car.play.android.app.Fragments.IntroFragment
import com.car.play.android.app.Fragments.homefragment
import com.car.play.android.app.R
import com.car.play.android.app.databinding.ActivityHomeBinding
import com.car.play.android.app.dialogs.ExitDialog

class HomeActivity : AppCompatActivity() {
    private val binding by lazy { ActivityHomeBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(binding.root)

    }
    override fun onBackPressed() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host)
        val currentFragment = navHostFragment?.childFragmentManager?.fragments?.get(0)
        if (currentFragment is homefragment) {
            ExitDialog.exitDialog(this)
        } else if (currentFragment is IntroFragment) {
            finishAffinity()
        }else{
            super.onBackPressed()
        }
    }
}