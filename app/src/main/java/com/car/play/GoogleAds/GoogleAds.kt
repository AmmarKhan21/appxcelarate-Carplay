package com.car.play.GoogleAds

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.net.ConnectivityManager
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.car.play.Utils.SharedPrefrence
import com.car.play.android.app.BuildConfig
import com.car.play.android.app.R
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.firebase.remoteconfig.FirebaseRemoteConfig


class GoogleAds {

    private var progressDialog: ProgressDialog? = null
    private val mNativeAd: NativeAd? = null
    private var adView: AdView? = null
    private var remoteConfig = FirebaseRemoteConfig.getInstance()


    private var maximum_count = 1

    companion object {
        private var interstitialAd: InterstitialAd? = null
        private var count = 0
    }


    fun CheckBanner(fragment: Fragment, view: View) {
        val activity = fragment.requireActivity()
        val bannerContainer = view.findViewById<ConstraintLayout>(R.id.banner_ad)
        if (bannerContainer != null) {
//            val check = MyPref().getBannerOn(activity)
            val isPurchase: Boolean = SharedPrefrence.checkSubscriptionState(activity)
            val check = remoteConfig.getBoolean("isBannerOn")
            Log.d("check121", "$check")
//            Log.d("check", "$check")
            if (isNetworkAvailable(activity) && check && !isPurchase) {
                bannerContainer.visibility = View.VISIBLE
                LoadBanner(bannerContainer, fragment)
            } else {
                bannerContainer.visibility = View.GONE
            }
        } else {
            Log.e("MyAdmobAds", "CardView with ID banner_ad not found in the view hierarchy.")
        }
    }

    fun CheckNativesmall(fragment: Fragment, view: View) {
        val activity = fragment.requireActivity()
        val nativeContainer = view.findViewById<CardView>(R.id.native_ad)
        val isPurchase: Boolean = SharedPrefrence.checkSubscriptionState(activity)
        val check = remoteConfig.getBoolean("isNativeOn")
        if (isNetworkAvailable(activity) && check && !isPurchase) {
            nativeContainer.visibility = View.VISIBLE
            LoadNativeAdsmall(fragment, nativeContainer)
        } else {
            nativeContainer.visibility = View.GONE
        }
    }

    fun CheckNative(fragment: Fragment, view: View) {
        val activity = fragment.requireActivity()
        val nativeContainer = view.findViewById<CardView>(R.id.native_ad)
        val isPurchase: Boolean = SharedPrefrence.checkSubscriptionState(activity)
        val check = remoteConfig.getBoolean("isNativeOn")
        if (isNetworkAvailable(activity) && check && !isPurchase) {
            nativeContainer.visibility = View.VISIBLE
            LoadNativeAd(fragment, nativeContainer)
        } else {
            nativeContainer.visibility = View.GONE
        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun CheckInterstitial(fragment: Activity, onAdShowed: onAdShowed) {
        val activity: Activity = fragment
        val remote_count = remoteConfig.getString("remote_count")
        Log.d("remotecount", "$remote_count: ")
        if (remote_count.isNullOrEmpty()) {
            maximum_count = 0
        } else
            maximum_count = remote_count.toInt()

        val check = remoteConfig.getBoolean("isInterstitialOn")
        val isPurchase: Boolean = SharedPrefrence.checkSubscriptionState(activity)
        Log.d("check1", "$check")
        if (isNetworkAvailable(activity) && count == maximum_count && check && !isPurchase) {
            count = 0
            // Show loading dialog for at least 1 second before showing ad
            showLoadingDialog(activity)
            // Delay the ad loading by 1 second to show the progress dialog
            Handler().postDelayed({
                progressDialog?.dismiss()
                showInit(activity, onAdShowed)
            }, 1000)
        } else {
            count++
            onAdShowed.onAdShow()
        }
    }

    private fun isNetworkAvailable(context: Activity): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
        return false
    }


    private fun getAdSize(mActivity: Activity): AdSize {
        val display = mActivity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val widthPixels = outMetrics.widthPixels.toFloat()
        val density = outMetrics.density
        val adWidth = (widthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(mActivity, adWidth)
    }

    fun LoadBanner(adContainer: ConstraintLayout, fragment: Fragment) {
        val mActivity: Activity = fragment.requireActivity()
        val bannerremote = remoteConfig.getString("banner_ad")

        val bannertest = "ca-app-pub-3940256099942544/6300978111"
        val bannerid = getAdUnitId(bannerremote, bannertest)
        Log.d("bannerremote", "$bannerremote")
        Log.d("bannnerid", "$bannerid")
        if (adView != null) {
            adView!!.destroy()
        }
        try {
            adView = AdView(mActivity)
//            adView!!.adUnitId =remoteConfig.getString("banner_ad")
            adView!!.adUnitId = bannerid
            adContainer.addView(adView)
            val adSize = getAdSize(mActivity)
            // Set the adaptive ad size on the ad view.
            adView!!.setAdSize(adSize)
            adView!!.loadAd(AdRequest.Builder().build())
            adView!!.adListener = object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    adView = null
                }

                override fun onAdLoaded() {
                    adContainer.visibility = View.VISIBLE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("ResourceType")
    fun LoadNativeAd(fragment: Fragment, frameLayout: FrameLayout) {
        val mActivity = fragment.requireActivity()
        val nativeremote = remoteConfig.getString("native_ad")
        Log.d("nativeremote", "$nativeremote")
        val nativetest = "ca-app-pub-3940256099942544/2247696110"
        val nativeid = getAdUnitId(nativeremote, nativetest)
        Log.d("nativeid", "$nativeid")
        val adLoader = AdLoader.Builder(mActivity, nativeid)
            .forNativeAd { nativeAd: NativeAd ->
                val adView =
                    mActivity.layoutInflater.inflate(R.layout.native_admob_ad, null) as NativeAdView
                if (mNativeAd != null) {
                    mNativeAd.destroy()
                }
                populateNativeAdView(nativeAd, adView)
                frameLayout.removeAllViews()
                frameLayout.addView(adView)
            }.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    frameLayout.visibility = View.VISIBLE
                }
            }).build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun LoadNativeAdsmall(fragment: Fragment, frameLayout: FrameLayout) {
        val mActivity = fragment.requireActivity()
        val nativeremote = remoteConfig.getString("native_ad")
        Log.d("nativeremote", "$nativeremote")
        val nativetest = "ca-app-pub-3940256099942544/2247696110"
        val nativeid = getAdUnitId(nativeremote, nativetest)
        Log.d("nativeid", "$nativeid")
        val adLoader = AdLoader.Builder(mActivity, nativeid)
            .forNativeAd { nativeAd: NativeAd ->
                val adView = mActivity.layoutInflater.inflate(
                    R.layout.item_small_native,
                    null
                ) as NativeAdView
                if (mNativeAd != null) {
                    mNativeAd.destroy()
                }
                populateNativeAdViewsmall(nativeAd, adView)
                frameLayout.removeAllViews()
                frameLayout.addView(adView)
            }.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    frameLayout.visibility = View.VISIBLE
                }
            }).build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {

        val mediaView = adView.findViewById<MediaView>(R.id.ad_media)
        val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
        val bodyView = adView.findViewById<TextView>(R.id.ad_body)
        val ctaView = adView.findViewById<TextView>(R.id.ad_call_to_action)

        // Register ONLY valid assets
        adView.mediaView = mediaView
        adView.headlineView = headlineView
        adView.bodyView = bodyView
        adView.callToActionView = ctaView

        // Populate data
        headlineView.text = nativeAd.headline

        if (nativeAd.body.isNullOrEmpty()) {
            bodyView.visibility = View.GONE
        } else {
            bodyView.text = nativeAd.body
            bodyView.visibility = View.VISIBLE
        }

        if (nativeAd.callToAction.isNullOrEmpty()) {
            ctaView.visibility = View.GONE
        } else {
            ctaView.text = nativeAd.callToAction
            ctaView.visibility = View.VISIBLE
        }

        // IMPORTANT: No advertiserView registration
        // IMPORTANT: No fake "AD" advertiser text

        adView.setNativeAd(nativeAd)
    }

    /*    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
            adView.mediaView = adView.findViewById<MediaView>(R.id.ad_media)
            adView.headlineView = adView.findViewById<View>(R.id.ad_headline)
            adView.bodyView = adView.findViewById<View>(R.id.ad_body)
            adView.callToActionView = adView.findViewById<View>(R.id.ad_call_to_action)
    //        adView.iconView = adView.findViewById<View>(R.id.ad_app_icon)
            adView.advertiserView = adView.findViewById<View>(R.id.ad_advertiser)

            // Populate the views with native ad data
            (adView.headlineView as TextView?)!!.text = nativeAd.headline
            (adView.bodyView as TextView?)!!.text = nativeAd.body
            (adView.callToActionView as TextView?)!!.text = nativeAd.callToAction

    //        // Load the icon
    //        if (nativeAd.icon != null) {
    //            (adView.iconView as ImageView?)!!.setImageDrawable(
    //                nativeAd.icon!!.drawable
    //            )
    //        }

            // Load the advertiser
            // Load the advertiser safely
            val advertiserText = nativeAd.advertiser ?: "AD"
            (adView.advertiserView as TextView?)?.text = advertiserText


            // Set click listener for the ad
            adView.isClickable = true
            adView.setOnClickListener {
            }

            // Set native ad
            adView.setNativeAd(nativeAd)
        }*/
    private fun populateNativeAdViewsmall(nativeAd: NativeAd, adView: NativeAdView) {
        adView.mediaView = adView.findViewById<MediaView>(R.id.ad_media)
        adView.headlineView = adView.findViewById<View>(R.id.ad_headline)
        adView.bodyView = adView.findViewById<View>(R.id.ad_body)
        adView.callToActionView = adView.findViewById<View>(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById<View>(R.id.ad_app_icon)
        adView.advertiserView = adView.findViewById<View>(R.id.ad_advertiser)

        // Populate the views with native ad data
        (adView.headlineView as TextView?)!!.text = nativeAd.headline
        (adView.bodyView as TextView?)!!.text = nativeAd.body
        (adView.callToActionView as TextView?)!!.text = nativeAd.callToAction

        // Load the icon
        if (nativeAd.icon != null) {
            (adView.iconView as ImageView?)!!.setImageDrawable(
                nativeAd.icon!!.drawable
            )
        }

        // Load the advertiser
        // Load the advertiser safely
        val advertiserText = nativeAd.advertiser ?: "AD"
        (adView.advertiserView as TextView?)?.text = advertiserText


        // Set click listener for the ad
        adView.isClickable = true
        adView.setOnClickListener {
            // Handle ad click
        }

        // Set native ad
        adView.setNativeAd(nativeAd)
    }

    fun loadBannerAd(context: Context, adContainer: ConstraintLayout) {
        val adView = AdView(context)
        adView.adUnitId = "your-ad-unit-id"  // Set your AdMob banner ID here
        adContainer.addView(adView)

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                adContainer.visibility = View.VISIBLE  // Show the banner once it's loaded
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                super.onAdFailedToLoad(error)
                adContainer.visibility = View.GONE  // Hide the banner if it fails to load
            }
        }
    }


    private fun getAdUnitId(remoteConfigKey: String, testAdId: String): String {
        return if (BuildConfig.DEBUG) {
            Log.d("debug", "debug")
            testAdId // Use Google test Ad IDs in debug mode
        } else {
            Log.d("debug", "release")
            remoteConfigKey
        }
    }


    // Show the loading dialog for 1 second
    private fun showLoadingDialog(activity: Activity) {
        progressDialog = ProgressDialog(activity)
        progressDialog?.setMessage("Loading Ad please wait...")
        progressDialog?.setCancelable(false)  // Dialog is not cancellable
        progressDialog?.show()  // Show the dialog
    }

    // Load the interstitial ad
    fun loadInter(activity: Activity) {
        val interstitialremote = remoteConfig.getString("interstitial_ad")
        Log.d("interstitialremote", "$interstitialremote")
        val interstitialtest = "ca-app-pub-3940256099942544/1033173712"
        val interstitialid = getAdUnitId(interstitialremote, interstitialtest)
        Log.d("interstitialid", "$interstitialid")
        if (interstitialAd == null) {
            val adRequest = AdRequest.Builder().build()
            val interstitialAdUnitId = interstitialid

            InterstitialAd.load(
                activity,
                interstitialAdUnitId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(loadedInterstitialAd: InterstitialAd) {
                        Log.d("test22", "interstitialAd is loaded successfully")
                        interstitialAd = loadedInterstitialAd
                        // Dismiss the loading dialog when the ad is loaded
//                    progressDialog?.dismiss()
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.d("test22", "interstitialAd faild")
                        interstitialAd = null
                        // Dismiss the loading dialog if ad fails to load
                        progressDialog?.dismiss()
                    }
                })
        }
    }

    // Show the ad if available
    private fun showInit(activity: Activity, onAdShowed: onAdShowed) {
        if (activity == null) {
            Log.d("test22", "activitynull")
            onAdShowed.onAdShow()
            return
        }

        // Show the ad if available, otherwise proceed to the next flow
        if (interstitialAd != null) {
            Log.d("test22", "interstitialAd is not null")
            interstitialAd!!.show(activity)
            interstitialAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    Log.d("test22", "Ad dismissed")
                    interstitialAd = null
                    Handler(activity.mainLooper).postDelayed({
                        onAdShowed.onAdShow()
                    }, 100)
                    loadInter(activity)  // Load the next ad
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    super.onAdFailedToShowFullScreenContent(adError)
                    Log.d("test22", "Ad failed")
                    interstitialAd = null
                    onAdShowed.onAdShow()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d("test22", "Ad showing")
                    interstitialAd = null
                }
            }
        } else {
            Log.d("test22", "load inter")
            // If ad is not ready, continue the flow and try to load an ad
            onAdShowed.onAdShow()
            loadInter(activity)
        }
    }

}