package com.car.play.android.app.Fragments

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.Adapters.ReminderAdapter
import com.car.play.android.app.R
import com.car.play.android.app.databinding.DialogAddReminderBinding
import com.car.play.android.app.databinding.FragmentRemindersBinding
import com.car.play.android.app.db.ReminderEntity
import com.car.play.android.app.db.ReminderViewModel
import com.car.play.android.app.services.ReminderScheduler
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RemindersFragment : Fragment() {

    private lateinit var binding: FragmentRemindersBinding
    private lateinit var googleAds: GoogleAds
    private lateinit var viewModel: ReminderViewModel
    private lateinit var reminderAdapter: ReminderAdapter
    private var currentFilter = "All"
    private var isObserving = false

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRemindersBinding.inflate(inflater, container, false)
        googleAds = GoogleAds()
        googleAds.CheckNative(this, binding.nativeAd)
        viewModel = ViewModelProvider(requireActivity()).get(ReminderViewModel::class.java)

        setupRecyclerView()
        setupClickListeners()
        setupFilterChips()
        observeData()
        requestNotificationPermissionIfNeeded()

        return binding.root
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun setupRecyclerView() {
        reminderAdapter = ReminderAdapter(
            emptyList(),
            onDeleteClick = { reminder -> showDeleteDialog(reminder) },
            onCompleteClick = { reminder, isChecked -> viewModel.markComplete(reminder.id, isChecked) }
        )
        binding.rvReminders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reminderAdapter
        }
    }

    private fun setupClickListeners() {
        binding.icBack.setOnClickListener { findNavController().popBackStack() }
        binding.fabAddReminder.setOnClickListener { showAddReminderDialog() }
    }

    private fun setupFilterChips() {
        val chips = listOf(binding.chipAll, binding.chipActive, binding.chipCompleted)
        binding.chipAll.setOnClickListener {
            currentFilter = "All"
            updateChipStyles(chips, 0)
            refreshList()
        }
        binding.chipActive.setOnClickListener {
            currentFilter = "Active"
            updateChipStyles(chips, 1)
            refreshList()
        }
        binding.chipCompleted.setOnClickListener {
            currentFilter = "Completed"
            updateChipStyles(chips, 2)
            refreshList()
        }
        updateChipStyles(chips, 0)
    }

    private fun updateChipStyles(chips: List<android.widget.TextView>, selectedIndex: Int) {
        chips.forEachIndexed { i, chip ->
            if (i == selectedIndex) {
                chip.setBackgroundResource(R.drawable.chip_bg_selected)
                chip.setTextColor(android.graphics.Color.WHITE)
            } else {
                chip.setBackgroundResource(R.drawable.chip_bg)
                chip.setTextColor(0xFFBBBBBB.toInt())
            }
        }
    }

    private fun observeData() {
        if (isObserving) return
        isObserving = true

        viewModel.allReminders.observe(viewLifecycleOwner) { reminders ->
            if (!isAdded) return@observe
            refreshList(reminders)
        }
        viewModel.activeReminders.observe(viewLifecycleOwner) { reminders ->
            if (!isAdded) return@observe
            if (currentFilter == "Active") {
                refreshList(reminders)
            }
        }
    }

    private fun refreshList(reminders: List<ReminderEntity>? = null) {
        val source = reminders ?: when (currentFilter) {
            "Active" -> viewModel.activeReminders.value
            else -> viewModel.allReminders.value
        } ?: emptyList()

        val filteredList = when (currentFilter) {
            "Active" -> source.filter { !it.isCompleted }
            "Completed" -> source.filter { it.isCompleted }
            else -> source
        }

        if (filteredList.isEmpty()) {
            binding.emptyImg.visibility = View.VISIBLE
            binding.rvReminders.visibility = View.GONE
        } else {
            binding.emptyImg.visibility = View.GONE
            binding.rvReminders.visibility = View.VISIBLE
            reminderAdapter.updateList(filteredList)
        }
    }

    private fun showDeleteDialog(reminder: ReminderEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Reminder")
            .setMessage("Are you sure you want to delete this reminder?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteReminder(reminder)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddReminderDialog() {
        val dialogBinding = DialogAddReminderBinding.inflate(LayoutInflater.from(requireContext()))
        val calendar = Calendar.getInstance()

        val categories = arrayOf("Oil Change", "Tire Rotation", "Insurance", "Inspection", "Car Wash", "Other")
        val categoryAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item_dark, categories)
        categoryAdapter.setDropDownViewResource(R.layout.spinner_dropdown_dark)
        dialogBinding.spinnerCategory.adapter = categoryAdapter

        val priorities = arrayOf("High", "Medium", "Low")
        val priorityAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item_dark, priorities)
        priorityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_dark)
        dialogBinding.spinnerPriority.adapter = priorityAdapter

        val intervals = arrayOf("Weekly", "Monthly", "Yearly")
        val intervalAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item_dark, intervals)
        intervalAdapter.setDropDownViewResource(R.layout.spinner_dropdown_dark)
        dialogBinding.spinnerRecurringInterval.adapter = intervalAdapter

        dialogBinding.switchRecurring.setOnCheckedChangeListener { _, isChecked ->
            dialogBinding.spinnerRecurringInterval.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        dialogBinding.etDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, day)
                    val format = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    dialogBinding.etDate.setText(format.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        dialogBinding.etTime.setOnClickListener {
            TimePickerDialog(
                requireContext(),
                { _, hour, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                    dialogBinding.etTime.setText(format.format(calendar.time))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        val dialog = AlertDialog.Builder(requireContext(), R.style.Theme_NewCarplay)
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnSave.setOnClickListener {
            val title = dialogBinding.etTitle.text.toString().trim()
            val description = dialogBinding.etDescription.text.toString().trim()
            val date = dialogBinding.etDate.text.toString().trim()
            val time = dialogBinding.etTime.text.toString().trim()
            val category = dialogBinding.spinnerCategory.selectedItem?.toString() ?: ""
            val priority = dialogBinding.spinnerPriority.selectedItem?.toString() ?: ""
            val isRecurring = dialogBinding.switchRecurring.isChecked
            val recurringInterval = if (isRecurring) {
                dialogBinding.spinnerRecurringInterval.selectedItem?.toString() ?: ""
            } else ""

            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (date.isEmpty()) {
                Toast.makeText(requireContext(), "Please select a date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (time.isEmpty()) {
                Toast.makeText(requireContext(), "Please select a time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val triggerAt = ReminderScheduler.parseTriggerTime(date, time)
            if (triggerAt == null) {
                Toast.makeText(requireContext(), "Invalid date or time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (triggerAt <= System.currentTimeMillis()) {
                Toast.makeText(requireContext(), "Please choose a future date and time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.addReminder(
                title, description, date, time, isRecurring, recurringInterval, category, priority
            )
            Toast.makeText(requireContext(), "Reminder saved", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}
