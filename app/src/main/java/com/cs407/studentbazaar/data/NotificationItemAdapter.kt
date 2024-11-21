package com.cs407.studentbazaar.data

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cs407.studentbazaar.R

class NotificationItemAdapter(private val itemList: ArrayList<NotificationItem>) :
    RecyclerView.Adapter<NotificationItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.notificationIcon)
        val message: TextView = itemView.findViewById(R.id.notificationMessage)
        val timestamp: TextView = itemView.findViewById(R.id.notificationTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.each_notification_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int = itemList.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.icon.setImageResource(item.iconResId)
        holder.message.text = item.activity

        // Format the timestamp to a readable date string
        val date = java.util.Date(item.timestamp.toLong()) // Convert from Int to Long
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()) // Custom format (you can change it)
        val formattedDate = formatter.format(date)

        holder.timestamp.text = formattedDate // Set formatted timestamp
    }
}