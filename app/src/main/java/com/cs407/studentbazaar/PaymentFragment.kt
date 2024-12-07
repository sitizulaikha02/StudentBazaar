package com.cs407.studentbazaar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.cs407.studentbazaar.adapters.PublishedItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class PaymentFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_payment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve total price and cart items JSON from arguments
        val totalPrice = arguments?.getString("totalPrice") ?: "0.00"
        val cartItemsJson = arguments?.getString("cartItemsJson") ?: "[]"

        // Deserialize JSON string back to ArrayList<PublishedItem>
        val cartItems: ArrayList<PublishedItem> = Gson().fromJson(
            cartItemsJson,
            object : TypeToken<ArrayList<PublishedItem>>() {}.type
        )

        Log.d("PaymentFragment", "Total Price: $totalPrice")
        Log.d("PaymentFragment", "Cart Items: $cartItems")

        val rgPaymentOptions = view.findViewById<RadioGroup>(R.id.rg_payment_options)
        val creditCardDetails = view.findViewById<View>(R.id.credit_card_details)
        val btnConfirmPayment = view.findViewById<Button>(R.id.btn_confirm_payment)
        val tvTotalPrice = view.findViewById<TextView>(R.id.tv_total_price)

        tvTotalPrice.text = "Total: $$totalPrice"

        // Handle payment option selection
        rgPaymentOptions.setOnCheckedChangeListener { _, checkedId ->
            val radioButton = view.findViewById<RadioButton>(checkedId)
            if (radioButton?.id == R.id.rb_credit_card) {
                creditCardDetails.visibility = View.VISIBLE
            } else {
                creditCardDetails.visibility = View.GONE
            }
        }

        // Handle confirm payment button click
        btnConfirmPayment.setOnClickListener {
            val selectedOption = rgPaymentOptions.checkedRadioButtonId
            when (selectedOption) {
                R.id.rb_cash_meetup -> handleCashMeetUp()
                R.id.rb_credit_card -> {
                    if (validateCreditCardDetails()) {
                        handleCreditCardPayment()
                    }
                }
                else -> Snackbar.make(
                    view,
                    "Please select a payment method",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun handleCashMeetUp() {
        Snackbar.make(requireView(), "Cash Meet Up selected!", Snackbar.LENGTH_SHORT).show()
        Log.d("PaymentFragment", "Cash Meet Up Selected")
        findNavController().navigate(R.id.action_paymentFragment_to_homepageFragment)
    }

    private fun validateCreditCardDetails(): Boolean {
        val cardNumber = view?.findViewById<TextInputEditText>(R.id.et_card_number)?.text.toString()
        val expirationDate = view?.findViewById<TextInputEditText>(R.id.et_expiration_date)?.text.toString()
        val cvv = view?.findViewById<TextInputEditText>(R.id.et_cvv)?.text.toString()

        if (cardNumber.length != 16 || !cardNumber.all { it.isDigit() }) {
            Snackbar.make(requireView(), "Invalid Card Number", Snackbar.LENGTH_SHORT).show()
            return false
        }

        if (!expirationDate.matches(Regex("(0[1-9]|1[0-2])/[0-9]{2}"))) {
            Snackbar.make(requireView(), "Invalid Expiration Date", Snackbar.LENGTH_SHORT).show()
            return false
        }

        if (cvv.length != 3 || !cvv.all { it.isDigit() }) {
            Snackbar.make(requireView(), "Invalid CVV", Snackbar.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun handleCreditCardPayment() {
        // Simulate credit card payment
        Log.d("PaymentFragment", "Processing credit card payment...")
        Snackbar.make(requireView(), "Payment successful!", Snackbar.LENGTH_SHORT).show()

        // Navigate to homepage or display a success screen
        findNavController().navigate(R.id.action_paymentFragment_to_homepageFragment)
    }

    private fun handlePaymentSuccess() {
        // Log success and show a confirmation to the user
        Log.d("PaymentFragment", "Payment successful!")
        Snackbar.make(requireView(), "Payment successful!", Snackbar.LENGTH_SHORT).show()

        // Remove purchased items from the listing
        removePurchasedItems()

        // Navigate to homepage
        findNavController().navigate(R.id.action_paymentFragment_to_homepageFragment)
    }

    private fun removePurchasedItems() {
        val cartItemsJson = arguments?.getString("cartItemsJson") ?: "[]"
        val cartItems: ArrayList<PublishedItem> = Gson().fromJson(
            cartItemsJson,
            object : TypeToken<ArrayList<PublishedItem>>() {}.type
        )

        val sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        sharedViewModel.removeItems(cartItems)

        Log.d("PaymentFragment", "Items removed from listing: $cartItems")
    }

}
