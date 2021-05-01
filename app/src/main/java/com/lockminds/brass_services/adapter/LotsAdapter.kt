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
import com.lockminds.brass_services.model.Lot
import com.lockminds.brass_services.utils.ItemAnimation

class LotsAdapter(context: Context, private val onClick: (Lot) -> Unit) :
        ListAdapter<Lot, LotsAdapter.LotsViewHolder>(LotsDiffCallback) {
    private val context: Context? = context
    /* ViewHolder for Lot, takes in the inflated view and the onClick behavior. */
    class LotsViewHolder(itemView: View, val onClick: (Lot) -> Unit) :
            RecyclerView.ViewHolder(itemView) {
        private val lotnum: TextView = itemView.findViewById(R.id.lot_no)
        private val source: TextView = itemView.findViewById(R.id.lot_source)
        private var currentLot: Lot? = null

        init {
            itemView.setOnClickListener {
                currentLot?.let {
                    onClick(it)
                }
            }
        }

        /* Bind lot name and image. */
        fun bind(lot: Lot, context: Context?) {
            currentLot = lot
            lotnum.text = lot.lot_no
            source.text = lot.source
        }


    }

    /* Creates and inflates view and return FlowerViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LotsViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_lot, parent, false)
        return LotsViewHolder(view, onClick)
    }


    override fun onBindViewHolder(holder: LotsViewHolder, position: Int) {
        val lot = getItem(position)
        holder.bind(lot, context)
        // setAnimation(holder.itemView, position)
    }

    private var lastPosition = -1
    private val on_attach = true

    private fun setAnimation(view: View, position: Int) {
        if (position > lastPosition) {
            ItemAnimation.animate(view, if (on_attach) position else -1, ItemAnimation.FADE_IN)
            lastPosition = position
        }
    }
}

object LotsDiffCallback : DiffUtil.ItemCallback<Lot>() {
    override fun areItemsTheSame(oldItem: Lot, newItem: Lot): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Lot, newItem: Lot): Boolean {
        return oldItem.id == newItem.id
    }
}