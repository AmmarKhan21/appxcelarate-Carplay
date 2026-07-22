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
import com.android.billingclient.api.PendingPurchasesParams
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

        setupFeatureTile(binding.carplay, R.id.action_homeFragment_to_carselectfragment)
        setupFeatureTile(binding.ivDocuments, R.id.action_homeFragment_to_showdocumentfragment)
        setupFeatureTile(binding.ivMntExpense, R.id.action_homeFragment_to_carmaintenencefragment)
        setupFeatureTile(binding.ivCarparking, R.id.action_homeFragment_to_parkfragment)
        setupFeatureTile(binding.ivWeather, R.id.action_homeFragment_to_weatherfragment)
        setupFeatureTile(binding.ivEmergency, R.id.action_homeFragment_to_emergencyfragment)
        setupFeatureTile(binding.ivSpeedometer, R.id.action_homeFragment_to_speedometerfragment)
        setupFeatureTile(binding.ivFuel, R.id.action_homeFragment_to_fuelfragment)
        setupFeatureTile(binding.ivReminders, R.id.action_homeFragment_to_remindersfragment)
        setupFeatureTile(binding.ivTrip, R.id.action_homeFragment_to_tripfragment)
        setupFeatureTile(binding.ivDrivingScore, R.id.action_homeFragment_to_drivingscorefragment)
        setupFeatureTile(binding.ivNearby, R.id.action_homeFragment_to_nearbyfragment)
        setupFeatureTile(binding.ivDashcam, R.id.action_homeFragment_to_dashcamfragment)
        setupFeatureTile(binding.ivTire, R.id.action_homeFragment_to_tirefragment)
        setupFeatureTile(binding.ivCarProfile, R.id.action_homeFragment_to_carprofilefragment)
        setupFeatureTile(binding.ivFatigue, R.id.action_homeFragment_to_fatiguefragment)
        setupFeatureTile(binding.ivInsurance, R.id.action_homeFragment_to_insurancefragment)
        setupFeatureTile(binding.ivMileage, R.id.action_homeFragment_to_mileagefragment)
        setupFeatureTile(binding.ivAiAssistant, R.id.action_homeFragment_to_aiassistantfragment)
        setupFeatureTile(binding.ivDnd, R.id.action_homeFragment_to_dndfragment)
        setupFeatureTile(binding.ivExpenseManager, R.id.action_homeFragment_to_expensemanagerfragment)
        setupFeatureTile(binding.ivServiceHistory, R.id.action_homeFragment_to_servicetimelinefragment)
        setupFeatureTile(binding.ivVoice, R.id.action_homeFragment_to_voicefragment)
        setupFeatureTile(binding.ivPredictive, R.id.action_homeFragment_to_predictivefragment)

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

    private fun setupFeatureTile(view: android.view.View, destinationId: Int) {
        view.setOnClickListener {
            googleAds.CheckInterstitial(requireActivity(), object : onAdShowed {
                override fun onAdShow() {
                    mController.navigate(destinationId)
                }
            })
            it.isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({ it.isEnabled = true }, 2000)
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
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
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

        billingClient.queryProductDetailsAsync(params) { billingResult, queryProductDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val productDetailsList = queryProductDetailsResult.productDetailsList
                if (productDetailsList.isNotEmpty()) {
                    initiatePurchase(productDetailsList[0])
                } else {
                    Toast.makeText(
                        this.requireActivity(),
                        "Product not available. Please try again later.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this.requireActivity(),
                    "Unable to load products. Please try again later.",
                    Toast.LENGTH_SHORT
                ).show()
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
        if (!isAdded) return
        purchases?.forEach { purchase ->
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                when (purchase.products.firstOrNull()) {
                    "removeads" -> {
                        SharedPrefrence.saveSubscriptionState(this.requireActivity(), true)
                    }
                }

                if (!purchase.isAcknowledged) {
                    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()

                    billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                        if (!isAdded) return@acknowledgePurchase
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
}


