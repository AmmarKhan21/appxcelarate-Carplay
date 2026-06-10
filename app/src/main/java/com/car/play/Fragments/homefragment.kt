package com.car.play.android.app.Fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.car.play.GoogleAds.GoogleAds
import com.car.play.GoogleAds.onAdShowed
import com.car.play.Utils.SharedPrefrence
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentHomefragmentBinding
import com.car.play.android.app.dialogs.RatingDialog
import com.car.play.android.app.dialogs.SharePrivacyDialog.showSharePrivacyDialog

class homefragment : Fragment() {
    private lateinit var billingClient: BillingClient
    private lateinit var googleAds: GoogleAds
    private val REQUEST_CAMERA_PERMISSION = 100
    private val binding by lazy { FragmentHomefragmentBinding.inflate(layoutInflater) }
    private val mController by lazy {
        (requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        googleAds = GoogleAds()
        setupSubscribeButton()
        setupBillingClient()
        googleAds.CheckNative(this@homefragment,binding.nativeAd)
        setupClickListeners()
        requestCameraPermission()
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
    private fun checkPermissions(): Boolean {
        val permission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        return permission == PackageManager.PERMISSION_GRANTED
    }

    // Request camera permission
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        binding.ivNav.setOnClickListener { binding.nav.open() }
        binding.carplay.setOnClickListener {
            googleAds.CheckInterstitial(requireActivity(),object : onAdShowed {
                override fun onAdShow() {
                    mController.navigate(R.id.action_homeFragment_to_carselectfragment)
                }
            })
            it.isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({
                it.isEnabled = true
            }, 2000)

        }

//        binding.ivEmergrncy.setOnClickListener {
//            mController.navigate(R.id.action_homeFragment_to_emergencyfragment)
//        }
        binding.ivCarparking.setOnClickListener {
            googleAds.CheckInterstitial(requireActivity(),object : onAdShowed {
                override fun onAdShow() {
                    mController.navigate(R.id.action_homeFragment_to_parkfragment)
                }
            })
            it.isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({
                it.isEnabled = true
            }, 2000)


        }
        binding.ivDocuments.setOnClickListener {
            googleAds.CheckInterstitial(requireActivity(),object : onAdShowed {
                override fun onAdShow() {
                    mController.navigate(R.id.action_homeFragment_to_showdocumentfragment)
                }
            })
            it.isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({
                it.isEnabled = true
            }, 2000)

        }
        binding.ivMntExpense.setOnClickListener {
            googleAds.CheckInterstitial(requireActivity(),object : onAdShowed {
                override fun onAdShow() {
                    mController.navigate(R.id.action_homeFragment_to_carmaintenencefragment)
                }
            })
            it.isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({
                it.isEnabled = true
            }, 2000)

        }
        binding.ivWeather.setOnClickListener {
            googleAds.CheckInterstitial(requireActivity(),object : onAdShowed {
                override fun onAdShow() {
                    mController.navigate(R.id.action_homeFragment_to_weatherfragment)
                }
            })
            it.isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({
                it.isEnabled = true
            }, 2000)

        }
        binding.ivSpeedometer.setOnClickListener {
            googleAds.CheckInterstitial(requireActivity(), object : onAdShowed {
                override fun onAdShow() {
                    mController.navigate(R.id.action_homeFragment_to_speedometerfragment)
                }
            })
            it.isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({ it.isEnabled = true }, 2000)
        }
        binding.ivFuel.setOnClickListener {
            googleAds.CheckInterstitial(requireActivity(), object : onAdShowed {
                override fun onAdShow() {
                    mController.navigate(R.id.action_homeFragment_to_fuelfragment)
                }
            })
            it.isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({ it.isEnabled = true }, 2000)
        }
        binding.ivReminders.setOnClickListener {
            googleAds.CheckInterstitial(requireActivity(), object : onAdShowed {
                override fun onAdShow() {
                    mController.navigate(R.id.action_homeFragment_to_remindersfragment)
                }
            })
            it.isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({ it.isEnabled = true }, 2000)
        }
        binding.ivPrivacy.setOnClickListener {
            showSharePrivacyDialog(requireContext(), "privacy")
        }
        binding.ivShare.setOnClickListener {
            showSharePrivacyDialog(requireContext(), "share")
        }
        binding.ivRatus.setOnClickListener {
            binding.nav.close()
            RatingDialog.ratingDialog(this, requireActivity())
        }
    }
    private fun setupSubscribeButton() {
        binding.ivRemoveAds.setOnClickListener {
            queryAvailableProducts("removeads")
        }
        binding.rmvAds.setOnClickListener {
            queryAvailableProducts("removeads")
        }

    }
    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(this.requireActivity())
            .setListener { billingResult, purchases ->
                handlePurchase(purchases)
            }
            .enablePendingPurchases()  // Required for API level 30+
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Billing client is ready
                    setupSubscribeButton()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Handle the disconnection, you may want to reconnect here
            }
        })
    }

    private fun queryAvailableProducts(productId: String) {
        if (!this::billingClient.isInitialized || !billingClient.isReady) {
            Toast.makeText(this.requireActivity(), "Billing client is not ready yet. Please try again later.", Toast.LENGTH_SHORT).show()
            return
        }
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsList[0]
                initiatePurchase(productDetails)
            }
        }
    }
    private fun initiatePurchase(productDetails: ProductDetails) {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(this.requireActivity(), billingFlowParams)
    }

    private fun handlePurchase(purchases: List<Purchase>?) {
        purchases?.forEach { purchase ->
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                when (purchase.products[0]) {
                    "removeads" -> {
                        SharedPrefrence.saveSubscriptionState(this.requireActivity(), true)
                    }
                }

                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Toast.makeText(
                            this.requireActivity(),
                            "Purchase successful!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}


