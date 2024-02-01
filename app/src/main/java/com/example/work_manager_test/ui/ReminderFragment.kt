package com.example.work_manager_test.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.work_manager_test.data.ReminderEntity
import com.example.work_manager_test.databinding.FragmentReminderBinding
import com.example.work_manager_test.ui.adapter.ReminderListAdapter
import com.example.work_manager_test.ui.adapter.ReminderListCallback
import com.example.work_manager_test.ui.dialog.DatePickerDialogFragment
import com.example.work_manager_test.ui.dialog.DatePickerDialogFragment.Companion.DATE_KEY
import com.example.work_manager_test.ui.dialog.DatePickerDialogFragment.Companion.DATE_PICKER_RESULT_KEY
import com.example.work_manager_test.ui.dialog.DatePickerDialogFragment.Companion.DATE_REMINDER_ID_KEY
import com.example.work_manager_test.ui.dialog.TimePickerDialogFragment
import com.example.work_manager_test.ui.dialog.TimePickerDialogFragment.Companion.TIME_KEY
import com.example.work_manager_test.ui.dialog.TimePickerDialogFragment.Companion.TIME_PICKER_RESULT_KEY
import com.example.work_manager_test.ui.dialog.TimePickerDialogFragment.Companion.TIME_REMINDER_ID_KEY
import com.example.work_manager_test.ui.service.BackgroundTimerService
import com.example.work_manager_test.ui.service.BackgroundTimerService.Companion.COUNTDOWN_RATE
import com.example.work_manager_test.ui.service.BackgroundTimerService.Companion.MOVE_TO_BACKGROUND
import com.example.work_manager_test.ui.service.BackgroundTimerService.Companion.MOVE_TO_FOREGROUND
import com.example.work_manager_test.ui.service.BackgroundTimerService.Companion.START
import com.example.work_manager_test.ui.service.BackgroundTimerService.Companion.STOPWATCH_ACTION
import com.example.work_manager_test.utils.ReminderWorkManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class ReminderFragment : Fragment(), ReminderListCallback {
    private lateinit var binding: FragmentReminderBinding
    private val viewModel: ReminderViewModel by viewModels()
    private lateinit var reminderAdapter: ReminderListAdapter
    private lateinit var statusReceiver: BroadcastReceiver
    private lateinit var timeReceiver: BroadcastReceiver

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        moveToBackground()
        timeReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                val countdown = p1?.getLongExtra(BackgroundTimerService.TIME_ELAPSED, 0)

                if (countdown != null) {
                    val hours: Long = countdown.div(1000).div(60).div(60)
                    val minutes: Long = countdown.div(1000).div(60)
                    val seconds: Long = countdown.div(1000).rem(60)
                    binding.countdown.text = "${"%02d".format(hours)}:${"%02d".format(minutes)}:${"%02d".format(seconds)}"
                }
            }
        }

        requireActivity().registerReceiver(timeReceiver, IntentFilter(BackgroundTimerService.STOPWATCH_TICK))

        statusReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {

            }
        }

        requireActivity().registerReceiver(statusReceiver, IntentFilter(BackgroundTimerService.STOPWATCH_STATUS))
    }

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(statusReceiver)
        requireActivity().unregisterReceiver(timeReceiver)
        moveToForeground()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configDatePickButton()
        configTimePickButton()
        configDatePickDialogResultListener()
        configTimePickDialogResultListener()
        configSubmitButton()
        configReminderEventListener()
        configReminderListAdapter()
    }

    private fun configReminderListAdapter() {
        reminderAdapter = ReminderListAdapter(this)
        binding.reminderList.adapter = reminderAdapter
        binding.reminderList.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.reminders.collect {
                if (it.isNotEmpty()) {
                    val reminder = it.first()
                    val todayDate = Calendar.getInstance()
                    val date = Calendar.getInstance()
                    date.time = ReminderViewModel.fullDateFormatter.parse("${reminder.date} ${reminder.time}")
                    Log.e("this", (date.timeInMillis - todayDate.timeInMillis).toString())
                    val countdownService = Intent(requireContext(), BackgroundTimerService::class.java)
                    countdownService.putExtra(STOPWATCH_ACTION, START)
                    countdownService.putExtra(COUNTDOWN_RATE, date.timeInMillis - todayDate.timeInMillis)
                    requireActivity().startService(countdownService)
                }

                reminderAdapter.submitList(it)
            }
        }
    }

    private fun moveToForeground() {
        val stopwatchService = Intent(requireContext(), BackgroundTimerService::class.java)
        stopwatchService.putExtra(
            STOPWATCH_ACTION,
            MOVE_TO_FOREGROUND
        )
        requireActivity().startService(stopwatchService)
    }

    private fun moveToBackground() {
        val stopwatchService = Intent(requireContext(), BackgroundTimerService::class.java)
        stopwatchService.putExtra(
            STOPWATCH_ACTION,
            MOVE_TO_BACKGROUND
        )
        requireActivity().startService(stopwatchService)
    }

    private fun configReminderEventListener() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.reminderEvents.collect {
                when (it) {
                    is ReminderEvents.ReminderAddedSuccessfully -> {
                        createReminderNotification(it.reminderEntity, false)
                    }

                    is ReminderEvents.CancelReminder -> cancelReminder(it.id)
                    is ReminderEvents.UpdateReminder -> {
                        createReminderNotification(it.updatedReminder, true)
                    }
                }
            }
        }
    }

    private fun cancelReminder(id: String) {
        WorkManager.getInstance(requireContext()).cancelWorkById(UUID.fromString(id)).state.observe(
            viewLifecycleOwner
        ) {
            when (it) {
                is Operation.State.SUCCESS -> {
                    viewModel.cancelReminder(id)
                }
            }
        }
    }

    private fun configDatePickDialogResultListener() {
        childFragmentManager.setFragmentResultListener(
            DATE_PICKER_RESULT_KEY,
            viewLifecycleOwner
        ) { key, bundle ->
            val date = bundle.getLong(DATE_KEY)
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = date

            val reminderId = bundle.getString(DATE_REMINDER_ID_KEY)

            if (reminderId != null) {
                viewModel.updateReminderDate(calendar, reminderId)
            } else {
                binding.dateText.text = ReminderViewModel.dateFormatter.format(calendar.time)
                viewModel.date = calendar
            }
        }
    }

    private fun configTimePickDialogResultListener() {
        childFragmentManager.setFragmentResultListener(
            TIME_PICKER_RESULT_KEY,
            viewLifecycleOwner
        ) { key, bundle ->
            val timeString = bundle.getString(TIME_KEY)
            val time = Calendar.getInstance()
            time.time = ReminderViewModel.timeFormatter.parse(timeString)

            val reminderId = bundle.getString(TIME_REMINDER_ID_KEY)

            if (reminderId != null) {
                val nowTime = ReminderViewModel.timeFormatter.parse(
                    ReminderViewModel.timeFormatter.format(Calendar.getInstance().time)
                )

                if (nowTime.before(time.time)) {
                    viewModel.updateReminderTime(time, reminderId)
                }
            } else {
                binding.timeText.text = timeString
                viewModel.time = time
            }
        }
    }

    private fun configDatePickButton() {
        binding.pickDateButton.setOnClickListener {
            DatePickerDialogFragment().show(childFragmentManager, DatePickerDialogFragment.TAG)
        }
    }

    private fun configTimePickButton() {
        binding.pickTimeButton.setOnClickListener {
            TimePickerDialogFragment().show(childFragmentManager, TimePickerDialogFragment.TAG)
        }
    }

    private fun configSubmitButton() {
        binding.submit.setOnClickListener {
            val message = binding.messageBox.text.toString()
            if (message.isNotEmpty()) {
                viewModel.createReminder(message)
            }
        }
    }

    private fun createReminderNotification(reminder: ReminderEntity, isUpdate: Boolean) {
        val todayDate = Calendar.getInstance()
        val date = Calendar.getInstance()
        date.time = ReminderViewModel.fullDateFormatter.parse("${reminder.date} ${reminder.time}")
        Log.e("this", date.toString())
        Log.e("this", todayDate.toString())
        Log.e("this", (date.timeInMillis - todayDate.timeInMillis).toString())
        val myWorkRequest = OneTimeWorkRequestBuilder<ReminderWorkManager>()
            .setInitialDelay(date.timeInMillis - todayDate.timeInMillis, TimeUnit.MILLISECONDS)
            .setId(UUID.fromString(reminder.id))
            .setInputData(
                workDataOf(
                    "message" to reminder.message,
                )
            )
            .build()

        if (isUpdate) {
            WorkManager.getInstance(requireContext()).updateWork(myWorkRequest)
        } else {
            WorkManager.getInstance(requireContext()).enqueue(myWorkRequest)
        }
    }

    override fun onDeleteItemClick(item: ReminderEntity) {
        viewModel.sendReminderEvent(ReminderEvents.CancelReminder(item.id))
    }

    override fun onEditDateClick(item: ReminderEntity) {
        DatePickerDialogFragment.createInstance(item.id)
            .show(childFragmentManager, DatePickerDialogFragment.TAG)
    }

    override fun onEditTimeClick(item: ReminderEntity) {
        TimePickerDialogFragment.createInstance(item.id)
            .show(childFragmentManager, TimePickerDialogFragment.TAG)
    }
}