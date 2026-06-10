package com.car.play.GoogleAds


import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.annotation.NonNull
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.car.play.Utils.SharedPrefrence
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.car.play.android.app.BuildConfig

class AppOpenAdManager(private val application: Application) : LifecycleObserver,
    Application.ActivityLifecycleCallbacks {
    private  val remoteConfig = FirebaseRemoteConfig.getInstance()
    companion object {
        private const val LOG_TAG = "AppOpenAdManager"
    }

    private var appOpenAd: AppOpenAd? = null
    private var isShowingAds = false
    //private var AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"

    private var loadCallback: AppOpenAd.AppOpenAdLoadCallback? = null

    private var currentActivity: Activity? = null

    init {
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        Log.d(LOG_TAG, "onStart: ")
        try {
            showAdIfAvailable()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error in showAdIfAvailable: ${e.message}", e)
        }
    }
    fun fetchAd() {
        if (isAdAvailable()) {
            return
        }
        Log.e("loadappopen", "loadingadsmanager")
        loadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(@NonNull appOpenAd: AppOpenAd) {
                super.onAdLoaded(appOpenAd)
                this@AppOpenAdManager.appOpenAd = appOpenAd
                Log.d(LOG_TAG, "onAdLoaded")
            }

            override fun onAdFailedToLoad(@NonNull loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                Log.d(LOG_TAG, "onAdFailedToLoad: ${loadAdError.message}")
            }
        }
        val adRequest = getAdRequest()
        val appOpenremote = remoteConfig.getString("appopen_ad")
        Log.d("appopenremote", "$appOpenremote: ")
        val appOpentest = "ca-app-pub-3940256099942544/9257395921"
        val appOpenid = getAdUnitId(appOpenremote, appOpentest)
        AppOpenAd.load(
            application,
            appOpenid,
            adRequest,
            loadCallback as AppOpenAd.AppOpenAdLoadCallback
        )
    }

    private fun showAdIfAvailable() {


        val check = remoteConfig.getBoolean("isAppOpenOn")

        // Launch a coroutine to read subscription state from DataStore
        CoroutineScope(Dispatchers.Main).launch {
            val isPurchase: Boolean = SharedPrefrence.checkSubscriptionState(application)


            if (!isShowingAds && isAdAvailable() && check && !isPurchase) {
                Log.d("apopen", "resumeapopen")
                val fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        super.onAdFailedToShowFullScreenContent(adError)
                    }

                    override fun onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent()
                        isShowingAds = true
                    }

                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        this@AppOpenAdManager.appOpenAd = null
                        isShowingAds = false
                        fetchAd()
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                    }
                }
                appOpenAd?.fullScreenContentCallback = fullScreenContentCallback
                currentActivity?.let { appOpenAd?.show(it) }
            } else {
                fetchAd()
            }
        }
    }

/*    private fun showAdIfAvailable() {
        Log.d(LOG_TAG, "showAdIfAvailable")
        val check = remoteConfig.getBoolean("appopen_resume")
        val isPurchase: Boolean = SharedPrefrence.checkSubscriptionState(application)
//        val isRemoveAdsWithEmergency = SharedPref.getRemoveEmergency(application)

        if (!isShowingAds && isAdAvailable() *//*&& check*//* && !isPurchase *//*&& !isRemoveAdsWithEmergency*//*)  {
            val fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    super.onAdFailedToShowFullScreenContent(adError)
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                    isShowingAds = true
                }

                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    this@AppOpenAdManager.appOpenAd = null
                    isShowingAds = false
                    fetchAd()
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                }
            }
            appOpenAd?.fullScreenContentCallback = fullScreenContentCallback
            currentActivity?.let { appOpenAd?.show(it) }
        } else {
            fetchAd()
        }
    }*/


    private fun getAdUnitId(remoteConfigKey: String, testAdId: String): String {
        return if (BuildConfig.DEBUG) {
            Log.d("debug", "debug")
            testAdId // Use Google test Ad IDs in debug mode
        } else {
            Log.d("debug", "release")
            remoteConfigKey
        }
    }

    /**
     * Creates and returns ad request.
     */
    private fun getAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }

    /**
     * Utility method that checks if ad exists and can be shown.
     */
    private fun isAdAvailable(): Boolean {
        return appOpenAd != null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        currentActivity = null
    }
}