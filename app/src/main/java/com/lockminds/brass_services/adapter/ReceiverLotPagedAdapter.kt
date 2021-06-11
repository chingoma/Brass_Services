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
import com.lockminds.brass_services.model.Office
import com.lockminds.brass_services.model.ReceiverLot


class ReceiverLotPagedAdapter(context: Context, private val onClick: (ReceiverLot) -> Unit) : PagingDataAdapter<ReceiverLot, ViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ReceiverLotViewHolder.create(parent,onClick)
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.item_attendance
        }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        (holder as ReceiverLotViewHolder).bind(item)
    }


    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<ReceiverLot>() {

            override fun areItemsTheSame(oldItem: ReceiverLot, newItem: ReceiverLot): Boolean {
                return (oldItem.id === newItem.id)
            }

            override fun areContentsTheSame(oldItem: ReceiverLot, newItem: ReceiverLot): Boolean =
                    oldItem == newItem
        }
    }

    class ReceiverLotViewHolder(view: View, val onClick: (ReceiverLot) -> Unit) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.name)
        private val actions: TextView = view.findViewById(R.id.actions)
        private val status: TextView = view.findViewById(R.id.status)
        private val checkpoint: TextView = view.findViewById(R.id.check_point)
        private var item: ReceiverLot? = null

        init {
            view.setOnClickListener {
                    item?.let {
                        onClick(it)
                    }
            }
        }

        fun bind(item: ReceiverLot?) {
            if (item == null) {
                val resources = itemView.resources
                name.text = resources.getString(R.string.app_name)
            } else {
                showRepoData(item)
            }
        }

        @SuppressLint("SetTextI18n")
        private fun showRepoData(item: ReceiverLot) {
            this.item = item
            name.text = item.lot_no
            actions.text = Tools.fromHtml("Escorter: <b>${item.escorter}</b> ")
            status.text = Tools.fromHtml("Driver: <b>${item.driver}</b> - <b>${item.driver_cell}</b>")
            checkpoint.text = Tools.fromHtml("Checkpoint: <b>${item.checkpoint}</b> - <b>${item.action}</b>")
            checkpoint.isVisible = true
        }

        companion object {
            fun create(parent: ViewGroup, onClick: (ReceiverLot) -> Unit): ReceiverLotViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_attendance, parent, false)
                return ReceiverLotViewHolder(view,onClick)
            }
        }

    }
    
    
}