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
import com.lockminds.brass_services.model.Office
import com.lockminds.brass_services.utils.ItemAnimation

class OfficeAdapter(context: Context, private val onClick: (Office) -> Unit) :
        ListAdapter<Office, OfficeAdapter.OfficesViewHolder>(OfficeDiffCallback) {
    private val context: Context? = context


    /* ViewHolder for Office, takes in the inflated view and the onClick behavior. */
    class OfficesViewHolder(itemView: View, val onClick: (Office) -> Unit) :
            RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.name)
        private var currentOffice: Office? = null

        init {
            itemView.setOnClickListener {
                currentOffice?.let {
                    onClick(it)
                }
            }
        }

        /* Bind Office name and image. */
        fun bind(Office: Office, context: Context?) {
            currentOffice = Office
            name.text = Office.name
        }


    }

    /* Creates and inflates view and return FlowerViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfficesViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_office, parent, false)
        return OfficesViewHolder(view, onClick)
    }


    override fun onBindViewHolder(holder: OfficesViewHolder, position: Int) {
        val office = getItem(position)
        holder.bind(office, context)
    }

}

object OfficeDiffCallback : DiffUtil.ItemCallback<Office>() {
    override fun areItemsTheSame(oldItem: Office, newItem: Office): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Office, newItem: Office): Boolean {
        return oldItem.id == newItem.id
    }
}