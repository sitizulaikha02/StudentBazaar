package com.cs407.studentbazaar.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cs407.studentbazaar.R
import com.cs407.studentbazaar.data.PublishedItem

class PublishedItemAdapter(private val items: List<PublishedItem>) : RecyclerView.Adapter<PublishedItemAdapter.PublishedItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublishedItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_published, parent, false)
        return PublishedItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: PublishedItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    inner class PublishedItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemTitle: TextView = itemView.findViewById(R.id.itemTitle)
        private val itemPrice: TextView = itemView.findViewById(R.id.itemPrice)
        private val itemLabel: TextView = itemView.findViewById(R.id.itemLabel) // New field
        private val itemCondition: TextView = itemView.findViewById(R.id.itemCondition) // New field
        private val itemDescription: TextView = itemView.findViewById(R.id.itemDescription) // New field
        private val itemImage: ImageView = itemView.findViewById(R.id.itemImage)

        fun bind(item: PublishedItem) {
            itemTitle.text = item.title
            itemPrice.text = "$${item.price}"
            itemLabel.text = item.label // Binding the new label field
            itemCondition.text = item.condition // Binding the new condition field
            itemDescription.text = item.description // Binding the new description field

            // Load the image using Glide if available
            if (item.imageUrl != null) {
                Glide.with(itemView.context).load(item.imageUrl).into(itemImage)
            } else {
                itemImage.setImageDrawable(null) // Placeholder if no image
            }
        }
    }
}
