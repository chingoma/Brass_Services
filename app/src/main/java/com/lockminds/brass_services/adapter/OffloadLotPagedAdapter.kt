package com.lockminds.brass_services.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.lockminds.brass_services.R
import com.lockminds.brass_services.Tools
import com.lockminds.brass_services.model.OffloadLot


class OffloadLotPagedAdapter(context: Context, private val onClick: (OffloadLot) -> Unit) : PagingDataAdapter<OffloadLot, ViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return OffloadLotViewHolder.create(parent,onClick)
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.item_attendance
        }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        (holder as OffloadLotViewHolder).bind(item)
    }


    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<OffloadLot>() {

            override fun areItemsTheSame(oldItem: OffloadLot, newItem: OffloadLot): Boolean {
                return (oldItem.id === newItem.id)
            }

            override fun areContentsTheSame(oldItem: OffloadLot, newItem: OffloadLot): Boolean =
                    oldItem == newItem
        }
    }

    class OffloadLotViewHolder(view: View, val onClick: (OffloadLot) -> Unit) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.name)
        private val actions: TextView = view.findViewById(R.id.actions)
        private val status: TextView = view.findViewById(R.id.status)
        private val checkpoint: TextView = view.findViewById(R.id.check_point)
        private var item: OffloadLot? = null

        init {
            view.setOnClickListener {
                    item?.let {
                        onClick(it)
                    }
            }
        }

        fun bind(item: OffloadLot?) {
            if (item == null) {
                val resources = itemView.resources
                name.text = resources.getString(R.string.app_name)
            } else {
                showRepoData(item)
            }
        }

        @SuppressLint("SetTextI18n")
        private fun showRepoData(item: OffloadLot) {
            this.item = item
            name.text = item.lot_no
            actions.text = Tools.fromHtml("Escorter: <b>${item.escorter}</b> ")
            status.text = Tools.fromHtml("Driver: <b>${item.driver}</b> - <b>${item.driver_cell}</b>")
            checkpoint.text = Tools.fromHtml("Received: <b>${item.receiving_date}</b>")
            checkpoint.isVisible = true
        }

        companion object {
            fun create(parent: ViewGroup, onClick: (OffloadLot) -> Unit): OffloadLotViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_attendance, parent, false)
                return OffloadLotViewHolder(view,onClick)
            }
        }

    }
    
    
}