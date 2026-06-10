package com.car.play.android.app.Fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentFeedbackBinding

class FeedbackFragment : Fragment() {

    private val binding by lazy { FragmentFeedbackBinding.inflate(layoutInflater) }

    private val mController by lazy {
        (requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        setupClickListeners()
        return binding.root
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            mController.popBackStack()
        }

        binding.ivSubmit.setOnClickListener {
            submitFeedback()
        }
    }

    private fun submitFeedback() {
        val message = binding.txtMessage.text.toString()
        if (message.isEmpty()) {
            binding.txtMessage.error = getString(R.string.error_empty_message)
        } else {
            sendEmail(message)
        }
    }

    private fun sendEmail(message: String) {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.txtEmail)))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            putExtra(Intent.EXTRA_TEXT, message)
        }
        startActivity(emailIntent)
    }
}