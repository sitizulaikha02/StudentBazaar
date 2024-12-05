package com.cs407.studentbazaar.adapters

import com.cs407.studentbazaar.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(private val fragment: Fragment, val userList: ArrayList<User>):
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(fragment.requireContext()).inflate(R.layout.each_user_inbox, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userList[position]
        holder.inboxName.text = currentUser.name

        holder.itemView.setOnClickListener {
            val bundle = Bundle().apply {
                putString("name", currentUser.name)
                putString("uid", currentUser.uid)
            }

            fragment.findNavController().navigate(R.id.action_inboxFragment_to_chatFragment, bundle)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val inboxName = itemView.findViewById<TextView>(R.id.inbox_name)
    }
}