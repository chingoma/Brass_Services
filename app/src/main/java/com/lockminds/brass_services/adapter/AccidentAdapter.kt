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
import com.lockminds.brass_services.model.Accident
import com.lockminds.brass_services.utils.ItemAnimation

class AccidentAdapter(context: Context, private val onClick: (Accident) -> Unit) :
        ListAdapter<Accident, AccidentAdapter.AccidentViewHolder>(AccidentDiffCallback) {
    private val context: Context? = context


    /* ViewHolder for Accident, takes in the inflated view and the onClick behavior. */
    class AccidentViewHolder(itemView: View, val onClick: (Accident) -> Unit) :
            RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.name)
        private val description: TextView = itemView.findViewById(R.id.description)
        private var currentAccident: Accident? = null

        init {
            itemView.setOnClickListener {
                currentAccident?.let {
                    onClick(it)
                }
            }
        }

        /* Bind accident name and image. */
        fun bind(accident: Accident, context: Context?) {
            currentAccident = accident
            name.text = accident.lot_no
            description.text = accident.location+" "+accident.accident_date
        }


    }

    /* Creates and inflates view and return FlowerViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccidentViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_accident, parent, false)
        return AccidentViewHolder(view, onClick)
    }


    override fun onBindViewHolder(holder: AccidentViewHolder, position: Int) {
        val accident = getItem(position)
        holder.bind(accident, context)
    }

}

object AccidentDiffCallback : DiffUtil.ItemCallback<Accident>() {
    override fun areItemsTheSame(oldItem: Accident, newItem: Accident): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Accident, newItem: Accident): Boolean {
        return oldItem.id == newItem.id
    }
}