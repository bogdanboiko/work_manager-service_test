package com.example.work_manager_test.ui.dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.work_manager_test.databinding.DialogTimePickerBinding
import com.example.work_manager_test.ui.ReminderViewModel
import java.util.Calendar

class TimePickerDialogFragment : DialogFragment() {
    private lateinit var binding: DialogTimePickerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogTimePickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.timePicker.setIs24HourView(true)
        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.submitButton.setOnClickListener {
            val result = Bundle().also {
                val hours = binding.timePicker.hour
                val minutes = binding.timePicker.minute
                Log.e(TAG, "$hours:$minutes")
                val time = Calendar.getInstance()
                time.set(Calendar.HOUR_OF_DAY, hours)
                time.set(Calendar.MINUTE, minutes)
                it.putString(TIME_KEY, ReminderViewModel.timeFormatter.format(time.time))

                val reminderId = arguments?.getString(TIME_REMINDER_ID_KEY)
                if (reminderId != null) {
                    it.putString(TIME_REMINDER_ID_KEY, reminderId)
                }
            }

            parentFragmentManager.setFragmentResult(TIME_PICKER_RESULT_KEY, result)
            dismiss()
        }
    }

    companion object {
        const val TAG = "TimePickerDialog"
        const val TIME_PICKER_RESULT_KEY = "time_picker_result_key"
        const val TIME_KEY = "time_key"
        const val TIME_REMINDER_ID_KEY = "time_reminder_id_key"

        fun createInstance(id: String): TimePickerDialogFragment {
            val dialog = TimePickerDialogFragment()
            dialog.arguments = Bundle().also {
                it.putString(TIME_REMINDER_ID_KEY, id)
            }

            return dialog
        }
    }
}