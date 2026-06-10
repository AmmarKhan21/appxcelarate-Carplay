package com.carplay.applecarplay.mirrorlink.autocarplay.carplayandroid.utils

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

    fun requestConsent(onComplete: (Boolean) -> Unit) {
        val params = ConsentRequestParameters.Builder()
            .setConsentDebugSettings(
                ConsentDebugSettings.Builder(activity)
                    .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                    .addTestDeviceHashedId("470B9066C28B143338BF3F608CBEC0BB") // Replace this with your test device hashed ID
                    .build()
            )
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                if (consentInformation.isConsentFormAvailable) {
                    loadAndShowConsentForm(onComplete)
                } else {
                    onComplete(true)
                }
            },
            { formError ->
                // Consent info update failed
                Log.e("GDPR", "Consent info update failed: ${formError.message}")
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
                            // After user responds, check consent status again
                            if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.OBTAINED) {
                                onComplete(true) // Consent given
                            } else {
                                onComplete(false) // Consent denied
                            }
                        } else {
                            Log.e("GDPR", "Consent form display failed: ${formError.message}")
                            onComplete(false)
                        }
                    }
                } else {
                    // Consent not required or already obtained
                    if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.OBTAINED) {
                        onComplete(true)
                    } else {
                        onComplete(false)
                    }
                }
            },
            { formLoadError ->
                Log.e("GDPR", "Consent form load failed: ${formLoadError.message}")
                onComplete(false)
            }
        )
    }

    fun initializeAdsWithTestDevice(testDeviceId: String) {
        MobileAds.initialize(activity) { initializationStatus ->
        }

        val testDeviceIds = listOf(testDeviceId)
        val configuration = RequestConfiguration.Builder()
            .setTestDeviceIds(testDeviceIds)
            .build()

        MobileAds.setRequestConfiguration(configuration)
    }
}


