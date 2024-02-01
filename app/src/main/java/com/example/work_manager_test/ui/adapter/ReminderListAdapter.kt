package com.example.work_manager_test.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.work_manager_test.data.ReminderEntity
import com.example.work_manager_test.databinding.ItemReminderBinding

interface ReminderListCallback {
    fun onDeleteItemClick(item: ReminderEntity)
    fun onEditDateClick(item: ReminderEntity)
    fun onEditTimeClick(item: ReminderEntity)
}

class ReminderListAdapter(private val callback: ReminderListCallback) :
    ListAdapter<ReminderEntity, ReminderListAdapter.ReminderViewHolder>(diffUtils) {
    inner class ReminderViewHolder(private val binding: ItemReminderBinding) :
        ViewHolder(binding.root) {
        fun bind(item: ReminderEntity) {
            binding.dateInfo.text = item.date
            binding.timeInfo.text = item.time
            binding.cancelReminder.setOnClickListener {
                callback.onDeleteItemClick(item)
            }

            binding.editDate.setOnClickListener {
                callback.onEditDateClick(item)
            }

            binding.editTime.setOnClickListener {
                callback.onEditTimeClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        return ReminderViewHolder(
            ItemReminderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtils = object : DiffUtil.ItemCallback<ReminderEntity>() {
            override fun areItemsTheSame(
                oldItem: ReminderEntity,
                newItem: ReminderEntity
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: ReminderEntity,
                newItem: ReminderEntity
            ): Boolean {
                return oldItem.id == newItem.id
                        && oldItem.date == newItem.date
                        && oldItem.time == newItem.time
                        && oldItem.message == newItem.message
            }
        }
    }
}