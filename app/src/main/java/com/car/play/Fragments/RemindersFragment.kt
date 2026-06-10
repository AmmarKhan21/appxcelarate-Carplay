package com.car.play.android.app.Fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.Adapters.ReminderAdapter
import com.car.play.android.app.databinding.DialogAddReminderBinding
import com.car.play.android.app.databinding.FragmentRemindersBinding
import com.car.play.android.app.db.ReminderViewModel
import com.car.play.android.app.reminders.ReminderScheduler
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RemindersFragment : Fragment() {

    private var _binding: FragmentRemindersBinding? = null
    private val binding get() = _binding!!

    private lateinit var googleAds: GoogleAds
    private lateinit var viewModel: ReminderViewModel

    private val notifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* result handled silently; reminders still saved */ }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRemindersBinding.inflate(inflater, container, false)
        googleAds = GoogleAds()
        viewModel = ViewModelProvider(this).get(ReminderViewModel::class.java)

        binding.remindersRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.icBack.setOnClickListener { findNavController().popBackStack() }
        binding.fab.setOnClickListener { showAddDialog() }

        googleAds.CheckNative(this, binding.nativeAd)
        maybeRequestNotificationPermission()

        viewModel.allReminders.observe(viewLifecycleOwner) { reminders ->
            _binding ?: return@observe
            if (reminders.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.remindersRecycler.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.remindersRecycler.visibility = View.VISIBLE
            }
            binding.remindersRecycler.adapter =
                ReminderAdapter(reminders) { reminder -> viewModel.deleteReminder(reminder) }
        }
        return binding.root
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun showAddDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val db = DialogAddReminderBinding.inflate(layoutInflater)
        dialog.setContentView(db.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.92).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Defaults
        db.chipInsurance.isChecked = true
        db.lead7.isChecked = true

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val dueCalendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 30) }
        db.btnDate.text = dateFormat.format(dueCalendar.time)

        db.btnDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    dueCalendar.set(Calendar.YEAR, year)
                    dueCalendar.set(Calendar.MONTH, month)
                    dueCalendar.set(Calendar.DAY_OF_MONTH, day)
                    db.btnDate.text = dateFormat.format(dueCalendar.time)
                },
                dueCalendar.get(Calendar.YEAR),
                dueCalendar.get(Calendar.MONTH),
                dueCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        db.btnCancel.setOnClickListener { dialog.dismiss() }
        db.btnSave.setOnClickListener {
            val title = db.etTitle.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Enter a title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val type = when (db.cgType.checkedChipId) {
                db.chipInsurance.id -> "Insurance"
                db.chipRegistration.id -> "Registration"
                db.chipLicense.id -> "License"
                db.chipService.id -> "Service"
                else -> "Other"
            }

            val leadDays = when (db.cgLead.checkedChipId) {
                db.lead1.id -> 1
                db.lead3.id -> 3
                db.lead14.id -> 14
                db.lead30.id -> 30
                else -> 7
            }

            // Normalize the due time to 9am so notifications fire at a sensible hour
            dueCalendar.set(Calendar.HOUR_OF_DAY, 9)
            dueCalendar.set(Calendar.MINUTE, 0)
            dueCalendar.set(Calendar.SECOND, 0)

            viewModel.addReminder(
                title = title,
                type = type,
                dueDateMillis = dueCalendar.timeInMillis,
                note = db.etNote.text.toString().trim(),
                leadDays = leadDays
            )
            ReminderScheduler.runNow(requireContext())
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
