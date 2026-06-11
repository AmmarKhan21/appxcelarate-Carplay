package com.car.play.GoogleAds

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

class GDPR_Message(private val activity: Activity) {

    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(activity)

    private val isDebug = false

    fun requestConsent(onComplete: (Boolean) -> Unit) {
        val paramsBuilder = ConsentRequestParameters.Builder()

        if (isDebug) {
            paramsBuilder.setConsentDebugSettings(
                ConsentDebugSettings.Builder(activity)
                    .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                    .addTestDeviceHashedId("470B9066C28B143338BF3F608CBEC0BB")
                    .build()
            )
        }

        val params = paramsBuilder.build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                if (consentInformation.isConsentFormAvailable) {
                    loadAndShowConsentForm(onComplete)
                } else {
                    MobileAds.initialize(activity) {}
                    onComplete(true)
                }
            },
            { formError ->
                Log.e("GDPR", "Consent info update failed: ${formError.message}")
                MobileAds.initialize(activity) {}
                onComplete(false)
            }
        )
    }

    private fun loadAndShowConsentForm(onComplete: (Boolean) -> Unit) {
        UserMessagingPlatform.loadConsentForm(
            activity,
            { consentForm ->
                if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                    consentForm.show(activity) { formError ->
                        if (formError == null) {
                            MobileAds.initialize(activity) {}
                            if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.OBTAINED) {
                                onComplete(true)
                            } else {
                                onComplete(false)
                            }
                        } else {
                            Log.e("GDPR", "Consent form display failed: ${formError.message}")
                            MobileAds.initialize(activity) {}
                            onComplete(false)
                        }
                    }
                } else {
                    MobileAds.initialize(activity) {}
                    if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.OBTAINED) {
                        onComplete(true)
                    } else {
                        onComplete(false)
                    }
                }
            },
            { formLoadError ->
                Log.e("GDPR", "Consent form load failed: ${formLoadError.message}")
                MobileAds.initialize(activity) {}
                onComplete(false)
            }
        )
    }

    fun initializeAdsWithTestDevice(testDeviceId: String) {
        MobileAds.initialize(activity) {}

        val testDeviceIds = listOf(testDeviceId)
        val configuration = RequestConfiguration.Builder()
            .setTestDeviceIds(testDeviceIds)
            .build()

        MobileAds.setRequestConfiguration(configuration)
    }
}
