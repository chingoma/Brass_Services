package com.lockminds.brass_services.adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lockminds.brass_services.R
import com.lockminds.brass_services.model.CheckPointHistory
import com.lockminds.brass_services.utils.ItemAnimation

class LotHistoryAdapter(context: Context, private val onClick: (CheckPointHistory) -> Unit) :
        ListAdapter<CheckPointHistory, LotHistoryAdapter.LotHistoryViewHolder>(LotHistoryDiffCallback) {
    private val context: Context? = context
    /* ViewHolder for Lot, takes in the inflated view and the onClick behavior. */
    class LotHistoryViewHolder(itemView: View, val onClick: (CheckPointHistory) -> Unit) :
            RecyclerView.ViewHolder(itemView) {
        private val checkPoint: TextView = itemView.findViewById(R.id.check_point)
        private val action: TextView = itemView.findViewById(R.id.action)
        private var currentLot: CheckPointHistory? = null

        init {
            itemView.setOnClickListener {
                currentLot?.let {
                    onClick(it)
                }
            }
        }

        /* Bind lot name and image. */
        fun bind(lot: CheckPointHistory, context: Context?) {
            currentLot = lot
            checkPoint.text = lot.check_point.toString()
            action.text = lot.action.toString()
        }
    }

    /* Creates and inflates view and return FlowerViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LotHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_lot_destination, parent, false)
        return LotHistoryViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: LotHistoryViewHolder, position: Int) {
        val lot = getItem(position)
        holder.bind(lot, context)
        // setAnimation(holder.itemView, position)
    }

    private var lastPosition = -1
    private val onAttach = true

    private fun setAnimation(view: View, position: Int) {
        if (position > lastPosition) {
            ItemAnimation.animate(view, if (onAttach) position else -1, ItemAnimation.FADE_IN)
            lastPosition = position
        }
    }
}

object LotHistoryDiffCallback : DiffUtil.ItemCallback<CheckPointHistory>() {
    override fun areItemsTheSame(oldItem: CheckPointHistory, newItem: CheckPointHistory): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: CheckPointHistory, newItem: CheckPointHistory): Boolean {
        return oldItem.id == newItem.id
    }
}