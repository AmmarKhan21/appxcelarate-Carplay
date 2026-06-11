package com.car.play.android.app.Fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentFeedbackBinding

class FeedbackFragment : Fragment() {

    private val binding by lazy { FragmentFeedbackBinding.inflate(layoutInflater) }
    private val mController by lazy {
        (requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
    }
    private lateinit var googleAds: GoogleAds

    private val categories = arrayOf("Bug Report", "Feature Request", "General", "Other")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        googleAds = GoogleAds()
        googleAds.CheckNative(this@FeedbackFragment, binding.nativeAd)
        setupSpinner()
        setupClickListeners()
        return binding.root
    }

    private fun setupSpinner() {
        val adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, categories) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).setTextColor(resources.getColor(R.color.white, null))
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as TextView).setTextColor(resources.getColor(R.color.white, null))
                view.setBackgroundColor(resources.getColor(R.color.bg_dark, null))
                return view
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { mController.popBackStack() }

        binding.btnSubmit.setOnClickListener { submitFeedback() }
    }

    private fun submitFeedback() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val rating = binding.ratingBar.rating.toInt()
        val category = binding.spinnerCategory.selectedItem?.toString() ?: "General"
        val message = binding.etMessage.text.toString().trim()

        if (name.isEmpty()) {
            binding.etName.error = "Name is required"
            binding.etName.requestFocus()
            return
        }
        if (email.isEmpty()) {
            binding.etEmail.error = "Email is required"
            binding.etEmail.requestFocus()
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Enter a valid email"
            binding.etEmail.requestFocus()
            return
        }
        if (message.isEmpty()) {
            binding.etMessage.error = "Message is required"
            binding.etMessage.requestFocus()
            return
        }

        val subject = "App Feedback - $category - $rating stars"
        val body = """
            |Feedback from: $name
            |Email: $email
            |Rating: $rating / 5 stars
            |Category: $category
            |
            |Message:
            |$message
        """.trimMargin()

        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("10xdigitalagency313@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            startActivity(emailIntent)
            Toast.makeText(requireContext(), "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT).show()
        }
    }
}
