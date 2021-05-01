package com.lockminds.brass_services.adapter
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lockminds.brass_services.R
import com.lockminds.brass_services.Tools
import com.lockminds.brass_services.model.AccidentGallery
import com.lockminds.brass_services.utils.ItemAnimation

class AccidentGalleryAdapter(context: Context, private val onClick: (AccidentGallery) -> Unit) :
        ListAdapter<AccidentGallery, AccidentGalleryAdapter.AccidentGalleryViewHolder>(AccidentGalleryDiffCallback) {
    private val context: Context? = context
    /* ViewHolder for Accident, takes in the inflated view and the onClick behavior. */
    class AccidentGalleryViewHolder(itemView: View, val onClick: (AccidentGallery) -> Unit) :
            RecyclerView.ViewHolder(itemView) {
        private val picture: ImageView = itemView.findViewById(R.id.image)
        private var currentAccident: AccidentGallery? = null

        init {
            itemView.setOnClickListener {
                currentAccident?.let {
                    onClick(it)
                }
            }
        }

        /* Bind accident name and image. */
        fun bind(accident: AccidentGallery, context: Context?) {
            currentAccident = accident
            if (context != null) {
                Tools.displayImage(context,picture,accident.file)
            }
        }


    }

    /* Creates and inflates view and return FlowerViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccidentGalleryViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_grid_image, parent, false)
        return AccidentGalleryViewHolder(view, onClick)
    }


    override fun onBindViewHolder(holder: AccidentGalleryViewHolder, position: Int) {
        val accident = getItem(position)
        holder.bind(accident, context)
    }

}

object AccidentGalleryDiffCallback : DiffUtil.ItemCallback<AccidentGallery>() {
    override fun areItemsTheSame(oldItem: AccidentGallery, newItem: AccidentGallery): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: AccidentGallery, newItem: AccidentGallery): Boolean {
        return oldItem.id == newItem.id
    }
}