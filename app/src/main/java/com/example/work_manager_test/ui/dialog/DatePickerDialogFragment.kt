package com.example.work_manager_test.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.work_manager_test.databinding.DialogDatePickerBinding
import java.util.Calendar


class DatePickerDialogFragment : DialogFragment() {
    private lateinit var binding: DialogDatePickerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogDatePickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.datePicker.minDate = Calendar.getInstance().timeInMillis

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.submitButton.setOnClickListener {
            val result = Bundle().also {
                val day = binding.datePicker.dayOfMonth
                val month = binding.datePicker.month
                val year = binding.datePicker.year
                val date = Calendar.getInstance()
                date.set(year, month, day)
                it.putLong(DATE_KEY, date.timeInMillis)

                val reminderId = arguments?.getString(DATE_REMINDER_ID_KEY)
                if (reminderId != null) {
                    it.putString(DATE_REMINDER_ID_KEY, reminderId)
                }
            }

            parentFragmentManager.setFragmentResult(DATE_PICKER_RESULT_KEY, result)
            dismiss()
        }
    }

    companion object {
        const val TAG = "DatePickerDialog"
        const val DATE_PICKER_RESULT_KEY = "date_picker_result_key"
        const val DATE_KEY = "date_key"
        const val DATE_REMINDER_ID_KEY = "date_reminder_id_key"

        fun createInstance(id: String): DatePickerDialogFragment {
            val dialog = DatePickerDialogFragment()
            dialog.arguments = Bundle().also {
                it.putString(DATE_REMINDER_ID_KEY, id)
            }

            return dialog
        }
    }
}