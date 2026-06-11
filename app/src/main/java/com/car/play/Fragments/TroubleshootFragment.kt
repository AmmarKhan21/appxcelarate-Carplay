package com.car.play.android.app.Fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.Adapters.TroubleshootAdapter
import com.car.play.android.app.Adapters.TroubleshootItem
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentTroubleshootBinding

class TroubleshootFragment : Fragment() {

    private lateinit var googleAds: GoogleAds
    private val binding by lazy { FragmentTroubleshootBinding.inflate(layoutInflater) }
    private val mController by lazy {
        (requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        googleAds = GoogleAds()
        googleAds.CheckNative(this, binding.nativeAd)
        setupRecyclerView()
        setupClickListeners()
        return binding.root
    }

    private fun setupRecyclerView() {
        val faqItems = buildFaqList()
        val adapter = TroubleshootAdapter(faqItems)
        binding.rvTroubleshoot.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTroubleshoot.adapter = adapter
    }

    private fun buildFaqList(): List<TroubleshootItem> {
        return listOf(
            // WiFi Issues
            TroubleshootItem(
                "WiFi",
                "Car WiFi not showing up?",
                "Make sure car infotainment is powered on. Go to car settings and enable WiFi hotspot. Restart both phone and car system."
            ),
            TroubleshootItem(
                "WiFi",
                "Connected but no internet?",
                "Car WiFi is for connection only, not internet. Your phone data will still work separately."
            ),
            TroubleshootItem(
                "WiFi",
                "Connection drops frequently?",
                "Move phone closer to car unit. Check for interference from other devices. Try forgetting the network and reconnecting."
            ),

            // Bluetooth Issues
            TroubleshootItem(
                "Bluetooth",
                "Phone not finding car?",
                "Put car in pairing/discovery mode. Make sure Bluetooth is enabled on both devices. Restart Bluetooth on phone."
            ),
            TroubleshootItem(
                "Bluetooth",
                "Paired but no audio?",
                "Check audio output settings. Ensure media audio is enabled in Bluetooth settings for this device."
            ),
            TroubleshootItem(
                "Bluetooth",
                "Bluetooth keeps disconnecting?",
                "Clear Bluetooth cache in phone settings. Remove device and re-pair. Update car firmware if available."
            ),

            // USB Issues
            TroubleshootItem(
                "USB",
                "Car not recognizing phone?",
                "Try a different USB cable (data cable, not charge-only). Try different USB port on car."
            ),
            TroubleshootItem(
                "USB",
                "Developer options needed?",
                "Go to Settings > About Phone > Tap Build Number 7 times to enable developer options."
            ),

            // Cast Issues
            TroubleshootItem(
                "Cast",
                "Cast device not found?",
                "Ensure both phone and car are on same WiFi network. Restart both devices."
            ),
            TroubleshootItem(
                "Cast",
                "Poor cast quality?",
                "Check WiFi signal strength. Reduce distance between phone and car display."
            ),

            // General
            TroubleshootItem(
                "General",
                "App not working with car?",
                "Make sure app has all required permissions. Update the app to latest version."
            ),
            TroubleshootItem(
                "General",
                "How to check compatibility?",
                "Visit our supported cars database in the app to check if your car model is compatible."
            )
        )
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            mController.popBackStack()
        }

        binding.btnContactSupport.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.txtEmail)))
                putExtra(Intent.EXTRA_SUBJECT, "Support Request - ${getString(R.string.app_name)}")
                putExtra(Intent.EXTRA_TEXT, "Hi, I need help with the following issue:\n\n")
            }
            try {
                startActivity(emailIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
