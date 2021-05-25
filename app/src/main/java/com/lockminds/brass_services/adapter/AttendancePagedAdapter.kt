package com.lockminds.brass_services.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.lockminds.brass_services.R
import com.lockminds.brass_services.Tools
import com.lockminds.brass_services.model.Attendance


class AttendancePagedAdapter : PagingDataAdapter<Attendance, ViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return AttendanceViewHolder.create(parent)
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.item_attendance
        }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        (holder as AttendanceViewHolder).bind(item)
    }


    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<Attendance>() {

            override fun areItemsTheSame(oldItem: Attendance, newItem: Attendance): Boolean {
                return (oldItem.time_in === newItem.time_in)
            }

            override fun areContentsTheSame(oldItem: Attendance, newItem: Attendance): Boolean =
                    oldItem == newItem
        }
    }

    class AttendanceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.name)
        private val actions: TextView = view.findViewById(R.id.actions)
        private val status: TextView = view.findViewById(R.id.status)
        private var item: Attendance? = null

        init {
            view.setOnClickListener {

            }
        }

        fun bind(item: Attendance?) {
            if (item == null) {
                val resources = itemView.resources
                name.text = resources.getString(R.string.app_name)
            } else {
                showRepoData(item)
            }
        }

        @SuppressLint("SetTextI18n")
        private fun showRepoData(item: Attendance) {
            this.item = item
            name.text = item.name
            actions.text = Tools.fromHtml("Check IN: <b>${item.time_in}</b><br/> Check OUT:  <b>${item.time_out}</b>")
            status.text = Tools.fromHtml("Status: <b>${item.status}</b>  <br/>Hours: <b>${item.working_hours}</b><br/> Extra Hours: <b>${item.extra_hours}</b>")
        }

        companion object {
            fun create(parent: ViewGroup): AttendanceViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_attendance, parent, false)
                return AttendanceViewHolder(view)
            }
        }

    }
    
    
}