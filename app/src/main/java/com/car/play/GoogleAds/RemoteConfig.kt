package com.car.play.GoogleAds

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.car.play.android.app.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
object RemoteConfig {
    @SuppressLint("StaticFieldLeak")
    lateinit var remoteConfig: FirebaseRemoteConfig

    // Accept Context instead of Activity
    fun setConfig(context: Context) {
        remoteConfig = Firebase.remoteConfig

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = (3600)

        }

        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.config_default1)

        // Fetch remote config, passing context
        fetchRemoteConfig(context)
    }

    // Fetch remote config, using context
    private fun fetchRemoteConfig(context: Context) {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Fetch succeeded
                    updateListen(context)
                    Log.d("TAG", "fetchRemoteConfig: ${task.isSuccessful}")
                } else {
                    // Handle fetch failure
                    Log.d("TAG", "fetchRemoteConfig: ${task.isSuccessful}")
                    if (context is Activity) {
//                        Toast.makeText(context, "Failed to fetch config", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    fun updateListen(context: Context) {
        remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                Log.e("TAG", "Updated keys: " + configUpdate.updatedKeys)

                updatePreferences(context)

//                Toast.makeText(context,"Success to update keys",Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                Log.w("TAG", "Config update error with code: " + error.code, error)
//                Toast.makeText(context,"failed to update keys",Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updatePreferences(context: Context) {
        remoteConfig.getBoolean("isBannerOn")
        remoteConfig.getBoolean("isInterstitialOn")
        remoteConfig.getBoolean("isNativeOn")
        remoteConfig.getBoolean("isAppOpenOn")

        remoteConfig.getBoolean("isReplaceInterOn")
        remoteConfig.getBoolean("isRewardedInterOn")
        remoteConfig.getBoolean("isRewardedOn")

        remoteConfig.getString("banner_ad")
        remoteConfig.getString("interstitial_ad")
        remoteConfig.getString("native_ad")
        remoteConfig.getString("app_open")

        remoteConfig.getString("replace_inter")
        remoteConfig.getString("rewarded_inter")
        remoteConfig.getString("rewarded_ad")
    }
}
