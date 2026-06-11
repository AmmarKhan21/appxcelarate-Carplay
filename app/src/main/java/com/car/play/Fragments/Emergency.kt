package com.car.play.android.app.Fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentEmergencyBinding
import org.json.JSONArray
import org.json.JSONObject

class Emergency : Fragment() {

    private val binding by lazy { FragmentEmergencyBinding.inflate(layoutInflater) }
    private val mController by lazy {
        (requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
    }
    private lateinit var googleAds: GoogleAds
    private val contactsList = mutableListOf<EmergencyContact>()
    private lateinit var contactsAdapter: EmergencyContactsAdapter

    companion object {
        private const val PREFS_NAME = "emergency_prefs"
        private const val KEY_CONTACTS = "emergency_contacts"
        private const val LOCATION_PERMISSION_REQUEST = 200
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        googleAds = GoogleAds()
        googleAds.CheckNative(this@Emergency, binding.nativeAd)
        setupContactsList()
        loadContacts()
        setupClickListeners()
        return binding.root
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { mController.popBackStack() }

        binding.btnSos.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:911")
            }
            startActivity(intent)
        }

        binding.btnCall911.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:911")
            }
            startActivity(intent)
        }

        binding.btnHospital.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("geo:0,0?q=hospital+near+me")
                setPackage("com.google.android.apps.maps")
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                val webIntent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/search/hospital+near+me"))
                startActivity(webIntent)
            }
        }

        binding.btnFire.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:911")
            }
            startActivity(intent)
        }

        binding.btnTow.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("geo:0,0?q=tow+truck+near+me")
                setPackage("com.google.android.apps.maps")
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                val webIntent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/search/tow+truck+near+me"))
                startActivity(webIntent)
            }
        }

        binding.btnAddContact.setOnClickListener { showAddContactDialog() }

        binding.btnShareLocation.setOnClickListener { shareLocation() }
    }

    private fun setupContactsList() {
        contactsAdapter = EmergencyContactsAdapter(
            contactsList,
            onCallClick = { contact ->
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${contact.phone}")
                }
                startActivity(intent)
            },
            onLongPress = { position ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Contact")
                    .setMessage("Remove ${contactsList[position].name}?")
                    .setPositiveButton("Delete") { _, _ ->
                        contactsList.removeAt(position)
                        contactsAdapter.notifyItemRemoved(position)
                        saveContacts()
                        updateContactsVisibility()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        binding.rvContacts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvContacts.adapter = contactsAdapter
    }

    private fun showAddContactDialog() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 20)
        }

        val nameInput = EditText(requireContext()).apply {
            hint = "Contact Name"
            setTextColor(android.graphics.Color.WHITE)
            setHintTextColor(0xFF888888.toInt())
            setSingleLine()
        }

        val phoneInput = EditText(requireContext()).apply {
            hint = "Phone Number"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            setTextColor(android.graphics.Color.WHITE)
            setHintTextColor(0xFF888888.toInt())
            setSingleLine()
        }

        layout.addView(nameInput)
        layout.addView(phoneInput)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Emergency Contact")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val name = nameInput.text.toString().trim()
                val phone = phoneInput.text.toString().trim()
                if (name.isNotEmpty() && phone.isNotEmpty()) {
                    contactsList.add(EmergencyContact(name, phone))
                    contactsAdapter.notifyItemInserted(contactsList.size - 1)
                    saveContacts()
                    updateContactsVisibility()
                } else {
                    Toast.makeText(requireContext(), "Please fill both fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveContacts() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        contactsList.forEach { contact ->
            val obj = JSONObject().apply {
                put("name", contact.name)
                put("phone", contact.phone)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_CONTACTS, jsonArray.toString()).apply()
    }

    private fun loadContacts() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_CONTACTS, null) ?: return
        try {
            val jsonArray = JSONArray(json)
            contactsList.clear()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                contactsList.add(EmergencyContact(obj.getString("name"), obj.getString("phone")))
            }
            contactsAdapter.notifyDataSetChanged()
            updateContactsVisibility()
        } catch (_: Exception) { }
    }

    private fun updateContactsVisibility() {
        if (contactsList.isEmpty()) {
            binding.tvEmptyContacts.visibility = View.VISIBLE
            binding.rvContacts.visibility = View.GONE
        } else {
            binding.tvEmptyContacts.visibility = View.GONE
            binding.rvContacts.visibility = View.VISIBLE
        }
    }

    @SuppressLint("MissingPermission")
    private fun shareLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST)
            return
        }

        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        if (location != null) {
            val shareText = "Emergency! I need help. My location: https://maps.google.com/?q=${location.latitude},${location.longitude}"
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
                putExtra(Intent.EXTRA_SUBJECT, "Emergency Location")
            }
            startActivity(Intent.createChooser(shareIntent, "Share Location"))
        } else {
            Toast.makeText(requireContext(), "Unable to get location. Enable GPS and try again.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            shareLocation()
        } else {
            Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    data class EmergencyContact(val name: String, val phone: String)

    class EmergencyContactsAdapter(
        private val contacts: List<EmergencyContact>,
        private val onCallClick: (EmergencyContact) -> Unit,
        private val onLongPress: (Int) -> Unit
    ) : RecyclerView.Adapter<EmergencyContactsAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val nameText: android.widget.TextView = itemView.findViewById(R.id.tv_contact_name)
            val phoneText: android.widget.TextView = itemView.findViewById(R.id.tv_contact_phone)
            val callBtn: View = itemView.findViewById(R.id.btn_call_contact)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_emergency_contact, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val contact = contacts[position]
            holder.nameText.text = contact.name
            holder.phoneText.text = contact.phone
            holder.callBtn.setOnClickListener { onCallClick(contact) }
            holder.itemView.setOnLongClickListener {
                onLongPress(position)
                true
            }
        }

        override fun getItemCount() = contacts.size
    }
}
