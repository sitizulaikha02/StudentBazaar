package com.cs407.studentbazaar.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cs407.studentbazaar.R

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

            // If imageUrl is provided, load it using Glide with resizing
            if (item.imageUri != "") {
                Glide.with(itemView.context)
                    .load(item.imageUri)
                    .apply(RequestOptions()
                        .override(200, 200)
                        .fitCenter()) // Resize to 800x800 pixels (you can adjust this size)
                        .error(R.drawable.default_image) // Fallback if the image cannot be loaded
                    .into(itemImage)
            } else {
                // If imageUrl is null, select a random default image from local resources
                val defaultImageResId = getDefaultImageResource()
                itemImage.setImageResource(defaultImageResId)
            }
        }

        // Function to randomly pick a local image from a predefined set of images
        private fun getDefaultImageResource(): Int {
            // Replace with your actual drawable resource IDs for default images
//            val imageResources = listOf(
//                R.drawable.book, // Local images for fallback
//                R.drawable.desk,
//                R.drawable.fan,
//                R.drawable.mirror,
//                R.drawable.tv
//            )

            return R.drawable.default_image // Randomly select one of the images
        }
    }

    fun updateItems(newItems: List<PublishedItem>) {
        (items as MutableList).clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

}
