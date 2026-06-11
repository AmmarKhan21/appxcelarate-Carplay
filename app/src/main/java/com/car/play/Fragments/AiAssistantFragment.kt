package com.car.play.android.app.Fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.Adapters.ChatAdapter
import com.car.play.android.app.Adapters.ChatMessage
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentAiAssistantBinding

class AiAssistantFragment : Fragment() {

    private val binding by lazy { FragmentAiAssistantBinding.inflate(layoutInflater) }
    private val mController by lazy {
        (requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
    }
    private lateinit var googleAds: GoogleAds
    private lateinit var chatAdapter: ChatAdapter
    private val handler = Handler(Looper.getMainLooper())

    private val qaDatabase = mapOf(
        listOf("check engine", "engine light", "cel") to "The Check Engine Light (CEL) can indicate many issues:\n\n• Loose gas cap - tighten it and drive for a day\n• Oxygen sensor failure - affects fuel economy\n• Catalytic converter issue - may need replacement\n• Mass airflow sensor - clean or replace\n• Spark plugs/wires - replace if old\n\nGet an OBD-II scan to read the exact error code. Don't ignore this light for long.",

        listOf("oil light", "oil pressure", "oil warning") to "The Oil Warning Light means:\n\n• Low oil pressure - stop driving immediately\n• Check oil level with dipstick\n• Top up if low, use correct weight oil\n• If level is fine but light stays on, could be oil pump or sensor issue\n• Driving with low oil can destroy your engine\n\nPull over safely and check ASAP.",

        listOf("battery light", "battery warning", "charging") to "Battery/Charging Light indicates:\n\n• Alternator may be failing\n• Battery connections loose or corroded\n• Serpentine belt broken or slipping\n• Battery may be old (3-5 year lifespan)\n\nYou may have 30-60 minutes before battery dies. Drive to nearest mechanic or safe location.",

        listOf("temperature", "overheating", "coolant", "temp light") to "Temperature Warning / Overheating:\n\n• Stop driving and pull over safely\n• Turn off AC, turn on heater to help cool engine\n• Wait 15-30 minutes before opening hood\n• Check coolant level (when cool)\n• Look for leaks under the car\n• Never open radiator cap when hot\n\nDriving while overheating can warp the head gasket.",

        listOf("brake light", "brake warning", "brakes") to "Brake Warning Light:\n\n• Check if parking brake is released\n• Low brake fluid - check reservoir\n• Worn brake pads - squealing noise\n• ABS system issue if ABS light is on too\n• Brake line leak - very dangerous\n\nBrakes are critical safety. Get checked immediately.",

        listOf("abs", "anti-lock") to "ABS Light (Anti-lock Braking System):\n\n• Regular brakes still work, but ABS is disabled\n• Wheel speed sensor may be faulty\n• ABS module could need repair\n• Wiring issue possible\n\nSafe to drive carefully, but get it checked soon.",

        listOf("airbag", "srs") to "Airbag/SRS Light:\n\n• Airbag system has a fault\n• Airbags may not deploy in a crash\n• Could be sensor, wiring, or clock spring\n• Seat belt pretensioner issue\n\nGet diagnosed at a dealer or qualified mechanic.",

        listOf("tire pressure", "tpms", "tire light") to "Tire Pressure Light (TPMS):\n\n• One or more tires are low on pressure\n• Check all tires with a gauge\n• Inflate to recommended PSI (door jamb sticker)\n• If flashing, TPMS sensor may be faulty\n• Temperature changes affect pressure\n\nDriving on low tires wastes fuel and is unsafe.",

        listOf("oil change", "when to change oil", "oil interval") to "Oil Change Schedule:\n\n• Conventional oil: every 5,000-7,500 km\n• Synthetic oil: every 10,000-15,000 km\n• Check owner's manual for your car's interval\n• Severe driving (city, towing): change more often\n• Always use the correct oil weight\n\nRegular oil changes are the single best thing for engine longevity.",

        listOf("tire rotation", "rotate tires") to "Tire Rotation:\n\n• Every 8,000-12,000 km\n• Ensures even tire wear\n• Front tires wear faster (FWD cars)\n• Extends tire life significantly\n• Check alignment at the same time\n\nRotation pattern depends on drivetrain type.",

        listOf("brake pad", "brake replacement", "when replace brakes") to "Brake Pad Replacement:\n\n• Typically every 40,000-70,000 km\n• Listen for squealing or grinding\n• Check thickness - replace under 3mm\n• Rotors may need resurfacing or replacement\n• Don't delay - brakes are critical safety\n\nCity driving wears brakes faster.",

        listOf("air filter", "cabin filter", "filter change") to "Air Filter Replacement:\n\n• Engine air filter: every 20,000-30,000 km\n• Cabin air filter: every 15,000-25,000 km\n• Dirty filter reduces power and fuel economy\n• Easy DIY replacement on most cars\n• Check more often in dusty conditions",

        listOf("fuel efficiency", "gas mileage", "save fuel", "mpg", "fuel tips") to "Fuel Efficiency Tips:\n\n• Keep tires properly inflated (+3% efficiency)\n• Remove excess weight from trunk\n• Use cruise control on highways\n• Avoid aggressive acceleration and braking\n• Keep windows up at highway speeds\n• Regular maintenance (air filter, spark plugs)\n• Use recommended fuel grade\n• Combine short trips\n• Turn off engine if idling >60 seconds\n• Keep speed under 100 km/h when possible",

        listOf("won't start", "not starting", "dead", "no crank") to "Car Won't Start Troubleshooting:\n\n• Dead battery - try jump start\n• Corroded battery terminals - clean them\n• Bad starter motor - clicking sound\n• Fuel pump failure - no sound when turning key\n• Ignition switch issue\n• Check if steering wheel is locked\n• Try different key or key fob battery\n\nIf it cranks but won't fire, could be fuel or spark issue.",

        listOf("noise", "strange sound", "rattle", "squeak", "knock") to "Strange Noises Diagnosis:\n\n• Squealing on startup: serpentine belt\n• Grinding when braking: worn brake pads\n• Knocking from engine: low octane fuel or rod bearing\n• Clicking when turning: CV joint\n• Humming that changes with speed: wheel bearing\n• Rattling underneath: loose heat shield\n• Whining from engine bay: power steering pump\n\nNote when the noise occurs (speed, turning, braking) to help diagnosis.",

        listOf("vibration", "shaking", "shimmy") to "Vibration/Shaking Diagnosis:\n\n• At highway speed: wheel balance issue\n• When braking: warped brake rotors\n• At idle: engine misfire or mount issue\n• When turning: CV joint or tie rod\n• Steering wheel shake: alignment or tire issue\n\nGet wheels balanced and aligned first - most common fix.",

        listOf("smoke", "exhaust smoke", "white smoke", "blue smoke", "black smoke") to "Exhaust Smoke Colors:\n\n• White smoke: coolant leak into combustion (head gasket)\n• Blue smoke: burning oil (worn rings or valve seals)\n• Black smoke: too much fuel (rich mixture, injector issue)\n• Light white on cold morning: normal condensation\n\nPersistent smoke of any color needs mechanic attention.",

        listOf("service schedule", "maintenance schedule", "when service", "next service") to "General Service Schedule:\n\n• Oil change: every 5,000-15,000 km\n• Tire rotation: every 8,000-12,000 km\n• Brake inspection: every 20,000 km\n• Air filter: every 20,000-30,000 km\n• Transmission fluid: every 50,000-100,000 km\n• Coolant flush: every 50,000 km\n• Spark plugs: every 50,000-100,000 km\n• Timing belt: every 100,000-150,000 km\n\nCheck your owner's manual for specific intervals.",

        listOf("gas station", "fuel station", "nearest gas", "petrol") to "I'll help you find a gas station nearby!",

        listOf("hello", "hi", "hey", "help") to "Hello! I'm your AI Car Assistant. I can help with:\n\n• Warning light meanings\n• Maintenance schedules\n• Fuel efficiency tips\n• Troubleshooting car problems\n• Service recommendations\n\nJust ask me anything about your car!"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        googleAds = GoogleAds()
        googleAds.CheckNative(this@AiAssistantFragment, binding.nativeAd)
        setupChat()
        setupClickListeners()
        return binding.root
    }

    private fun setupChat() {
        chatAdapter = ChatAdapter()
        binding.rvChat.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChat.adapter = chatAdapter

        chatAdapter.addMessage(ChatMessage(
            "Hi! I'm your AI Car Assistant. Ask me about warning lights, maintenance, fuel tips, or any car problems. You can also tap the suggestion chips below!",
            false
        ))
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { mController.popBackStack() }

        binding.btnSend.setOnClickListener { sendMessage() }

        binding.chipEngine.setOnClickListener {
            processUserInput("What does the check engine light mean?")
        }

        binding.chipService.setOnClickListener {
            processUserInput("When is my next service due?")
        }

        binding.chipGas.setOnClickListener {
            processUserInput("Find nearest gas station")
        }

        binding.chipFuel.setOnClickListener {
            processUserInput("How to improve fuel efficiency?")
        }
    }

    private fun sendMessage() {
        val message = binding.etMessage.text.toString().trim()
        if (message.isEmpty()) return
        binding.etMessage.text.clear()
        processUserInput(message)
    }

    private fun processUserInput(input: String) {
        chatAdapter.addMessage(ChatMessage(input, true))
        scrollToBottom()

        handler.postDelayed({
            val response = generateResponse(input)
            chatAdapter.addMessage(ChatMessage(response, false))
            scrollToBottom()

            if (input.lowercase().contains("gas station") || input.lowercase().contains("fuel station") || input.lowercase().contains("petrol")) {
                handler.postDelayed({
                    try {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("geo:0,0?q=gas+station+near+me")
                            setPackage("com.google.android.apps.maps")
                        }
                        startActivity(intent)
                    } catch (e: Exception) {
                        val webIntent = Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com/maps/search/gas+station+near+me"))
                        startActivity(webIntent)
                    }
                }, 500)
            }
        }, 800)
    }

    private fun generateResponse(input: String): String {
        val lowerInput = input.lowercase()

        for ((keywords, response) in qaDatabase) {
            if (keywords.any { lowerInput.contains(it) }) {
                return response
            }
        }

        return "I'm not sure about that specific topic. Here are some things I can help with:\n\n• Ask about any dashboard warning light\n• Maintenance schedules and tips\n• Fuel efficiency advice\n• Car trouble diagnosis (noises, vibrations, smoke)\n• When to replace parts (brakes, oil, filters)\n\nTry rephrasing your question or ask about one of these topics!"
    }

    private fun scrollToBottom() {
        binding.rvChat.post {
            val itemCount = chatAdapter.itemCount
            if (itemCount > 0) {
                binding.rvChat.smoothScrollToPosition(itemCount - 1)
            }
        }
    }
}
