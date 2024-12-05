package com.cs407.studentbazaar.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cs407.studentbazaar.R

class UserItemAdapter(private val imageUris: List<String>) :
    RecyclerView.Adapter<UserItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.each_user_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int = imageUris.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val imageUri = imageUris[position]

        // Use Glide to load the image from the URI
        Glide.with(holder.itemView.context)
            .load(imageUri) // Load the URI
            .apply(
                RequestOptions()
                    .error(R.drawable.default_image) // Fallback if loading fails
                    .override(300, 300)
                    .centerCrop() // Adjust image scaling
            )
            .into(holder.imageView) // Load into the ImageView
    }
}
