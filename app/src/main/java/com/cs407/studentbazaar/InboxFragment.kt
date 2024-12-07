package com.cs407.studentbazaar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.studentbazaar.adapters.User
import com.cs407.studentbazaar.adapters.UserAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InboxFragment : Fragment() {
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var adapter: UserAdapter
    private lateinit var backButton: ImageView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_inbox, container, false)

        // Initialize RecyclerView and Adapter
        userList = ArrayList()
        adapter = UserAdapter(this, userList)

        backButton = view.findViewById(R.id.button_back)

        userRecyclerView = view.findViewById(R.id.userRecyclerView)
        userRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        userRecyclerView.adapter = adapter

        Log.d("InboxFragment", "I'm here" )

        fetchUsersFromFirestore()

        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }

    private fun fetchUsersFromFirestore() {
        db.collection("users")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val currentUser = FirebaseAuth.getInstance().currentUser

                userList.clear()

                for (document in querySnapshot.documents) {
                    val uid = document.id // The document ID is the UID
                    if (uid != currentUser?.uid) {
                        userList.add(
                            User(
                                name = document.getString("name") ?: "No Name",
                                email = document.getString("email") ?: "No Email",
                                uid = uid
                            )
                        )
                    }
                }
                Log.d("InboxFragment", "Users: ${userList}" )

                adapter.notifyDataSetChanged()

            }
            .addOnFailureListener { e ->
                Log.d("FetchUsers", "${e.message}" )
            }
    }
}