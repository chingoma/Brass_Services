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
import com.lockminds.brass_services.Tools
import com.lockminds.brass_services.model.Accident
import com.lockminds.brass_services.model.Attendance
import com.lockminds.brass_services.utils.ItemAnimation

class AttendanceAdapter(context: Context, private val onClick: (Attendance) -> Unit) :
        ListAdapter<Attendance, AttendanceAdapter.AttendanceViewHolder>(AttendanceDiffCallback) {
    private val context: Context? = context


    /* ViewHolder for Accident, takes in the inflated view and the onClick behavior. */
    class AttendanceViewHolder(view: View, val onClick: (Attendance) -> Unit) :
            RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.name)
        private val actions: TextView = view.findViewById(R.id.actions)
        private val status: TextView = view.findViewById(R.id.status)
        private var currentAccident: Attendance? = null

        init {
            itemView.setOnClickListener {
                currentAccident?.let {
                    onClick(it)
                }
            }
        }

        /* Bind accident name and image. */
        fun bind(item: Attendance, context: Context?) {
            currentAccident = item
            name.text = item.name
            actions.text = Tools.fromHtml("Check IN: <b>${item.time_in}</b><br/> Check OUT:  <b>${item.time_out}</b>")
            status.text = Tools.fromHtml("Status: <b>${item.status}</b>  <br/>Hours: <b>${item.working_hours}</b>")
        }


    }

    /* Creates and inflates view and return FlowerViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_attendance, parent, false)
        return AttendanceViewHolder(view, onClick)
    }


    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val accident = getItem(position)
        holder.bind(accident, context)
    }

}

object AttendanceDiffCallback : DiffUtil.ItemCallback<Attendance>() {
    override fun areItemsTheSame(oldItem: Attendance, newItem: Attendance): Boolean {
        return oldItem.time_in == newItem.time_in
    }

    override fun areContentsTheSame(oldItem: Attendance, newItem: Attendance): Boolean {
        return oldItem == newItem
    }
}