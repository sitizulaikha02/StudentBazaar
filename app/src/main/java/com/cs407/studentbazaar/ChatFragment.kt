package com.cs407.studentbazaar

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.studentbazaar.adapters.Message
import com.cs407.studentbazaar.adapters.MessageAdapter
import com.cs407.studentbazaar.notifications.NotificationSender
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class ChatFragment : Fragment() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: Button
    private lateinit var backButton: ImageButton
    private lateinit var userButton: ImageButton
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var db: DatabaseReference
    private lateinit var chatTitleBar: TextView
    private lateinit var messageId: String

    private var receiverRoom: String? = null
    private var senderRoom: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        // Retrieve arguments passed from previous fragment
        val name = arguments?.getString("name")
        val receiverUid = arguments?.getString("uid")
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid

        val sharedPref = requireContext().getSharedPreferences("WhoAmI", Context.MODE_PRIVATE)
        val username = sharedPref.getString("USERNAME", "null").toString() // Default value is null if not found

        chatTitleBar = view.findViewById(R.id.chat_title)
        chatTitleBar.text = name

//        (requireActivity() as AppCompatActivity).supportActionBar?.title = name

        db = FirebaseDatabase.getInstance().getReference()

        // Unique room for every chat
        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        Log.d("senderUid", "$senderUid")
        Log.d("receiverUid", "$receiverUid")
        Log.d("senderRoom", "$senderRoom")
        Log.d("receiverRoom", "$receiverRoom")

        loadChatMessages()


        // Set up RecyclerView and other UI components
        chatRecyclerView = view.findViewById(R.id.recyclerView_chat)
        messageBox = view.findViewById(R.id.editText_message)
        sendButton = view.findViewById(R.id.button_send)
        backButton = view.findViewById(R.id.button_back)
        userButton = view.findViewById(R.id.userButton)
        messageList = ArrayList()
        messageAdapter = MessageAdapter(requireContext(), messageList)

        chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        chatRecyclerView.adapter = messageAdapter

        // Fetch chat messages
        db.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(Message::class.java)
                        message?.let { messageList.add(it) }
                        Log.d("onDataChange", "Message added: ${message?.message}, Sender: ${message?.senderId}")
                    }
                    messageAdapter.notifyDataSetChanged()

                    // Scroll to the last message
                    if (messageList.isNotEmpty()) {
                        chatRecyclerView.scrollToPosition(messageList.size - 1)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("onDataChangeError", "Error: ${error.message}")
                }
            })

        // Send button logic
        sendButton.setOnClickListener {
            val message = messageBox.text.toString()
            // Generate a unique messageId using Firebase's push().key
            val messageId = db.child("chats").child(senderRoom!!).child("messages").push().key

            if (messageId != null) {
                // Create the message object with messageId
                val messageObject = Message(messageId, message, senderUid)

                // Save the message in the sender's chat room
                db.child("chats").child(senderRoom!!).child("messages").child(messageId)
                    .setValue(messageObject)
                    .addOnSuccessListener {
                        // Save the message in the receiver's chat room
                        db.child("chats").child(receiverRoom!!).child("messages").child(messageId)
                            .setValue(messageObject)
                            .addOnFailureListener { exception ->
                                // Handle failure for receiver's messages
                                Log.e("FirebaseError", "Failed to add message to receiverRoom: ${exception.message}")
                            }
                    }
                    .addOnFailureListener { exception ->
                        // Handle failure for sender's messages
                        Log.e("FirebaseError", "Failed to add message to senderRoom: ${exception.message}")
                    }

                // Clear the message box
                messageBox.setText("")
            } else {
                Log.e("FirebaseError", "Failed to generate messageId")
            }

            val db = FirebaseFirestore.getInstance()

            // Fetch recipient's FCM token
            db.collection("users").document(receiverUid.toString()).get()
                .addOnSuccessListener { document ->
                    val fcmToken = document.getString("fcmToken")
                    if (!fcmToken.isNullOrEmpty()) {
                        val notificationSender = NotificationSender(requireContext())
                        val notificationType = "message"

                        // Send notification to the recipient
                        notificationSender.sendNotificationToUser(fcmToken, username, message, senderUid, notificationType)
                    } else {
                        Log.e("SendingNotification", "FCM token is missing for user: $receiverUid")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("SendingNotification", "Error fetching user token: ${e.message}")
                }

            messageBox.setText("")
        }

        val buttonMeetup = view.findViewById<Button>(R.id.button_meetup)
        buttonMeetup.setOnClickListener {
            findNavController().navigate(R.id.action_chatFragment_to_meetupFragment)
        }

        backButton.setOnClickListener {
            // Use Navigation Component to navigate to UserProfileFragment
            findNavController().navigate(R.id.action_chatFragment_to_inboxFragment)
        }

        userButton.setOnClickListener {
            val bundle = Bundle().apply {
                putString("uid", receiverUid)
            }
            // Use Navigation Component to navigate to UserProfileFragment
            findNavController().navigate(R.id.action_chatFragment_to_userProfileFragment, bundle)
        }

        return view
    }

    private fun loadChatMessages() {
        // Fetch all messages in the senderRoom
        db.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()

                    for (postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(Message::class.java)
                        message?.let {
                            messageList.add(it)
                        }
                    }

                    // Notify the adapter of data changes
                    messageAdapter.notifyDataSetChanged()

                    // Log the messageId for debugging
                    if (messageList.isNotEmpty()) {
                        Log.d("ChatFragment", "Navigated from notification to chat.")
                        chatRecyclerView.scrollToPosition(messageList.size - 1)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatFragment", "Error: ${error.message}")
                }
            })
    }


}