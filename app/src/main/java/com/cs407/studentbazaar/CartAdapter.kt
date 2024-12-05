package com.cs407.studentbazaar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cs407.studentbazaar.R
import com.cs407.studentbazaar.data.PublishedItem

class CartAdapter(
    private var itemList: List<PublishedItem> = emptyList(),
    private val onRemoveClicked: (PublishedItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    // ViewHolder setup
    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemTitleTextView: TextView = itemView.findViewById(R.id.itemTitleTextView)
        val itemPriceTextView: TextView = itemView.findViewById(R.id.itemPriceTextView)
        val removeItemButton: Button = itemView.findViewById(R.id.removeItemButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = itemList[position]

        holder.itemTitleTextView.text = item.title
        holder.itemPriceTextView.text = "$${item.price}"

        // Set up the remove button click listener
        holder.removeItemButton.setOnClickListener {
            onRemoveClicked(item)
        }
    }

    override fun getItemCount(): Int = itemList.size

    fun updateItems(newItems: List<PublishedItem>) {
        itemList = newItems
        notifyDataSetChanged()  // Notify the adapter that the data has changed
    }
}
