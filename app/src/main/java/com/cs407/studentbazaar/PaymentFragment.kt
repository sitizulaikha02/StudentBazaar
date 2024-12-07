package com.cs407.studentbazaar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cs407.studentbazaar.adapters.PublishedItem
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject

class PaymentFragment : Fragment() {

    private lateinit var paymentsClient: PaymentsClient
    private val googlePayEnvironment = WalletConstants.ENVIRONMENT_TEST // Sandbox mode

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_payment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        // Initialize Google Pay API client
        paymentsClient = Wallet.getPaymentsClient(
            requireActivity(),
            Wallet.WalletOptions.Builder()
                .setEnvironment(googlePayEnvironment)
                .build()
        )

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
                        processGooglePay(totalPrice)
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

        if (cardNumber.length != 16) {
            Snackbar.make(requireView(), "Invalid Card Number", Snackbar.LENGTH_SHORT).show()
            return false
        }

        if (!expirationDate.matches(Regex("(0[1-9]|1[0-2])/[0-9]{2}"))) {
            Snackbar.make(requireView(), "Invalid Expiration Date", Snackbar.LENGTH_SHORT).show()
            return false
        }

        if (cvv.length != 3) {
            Snackbar.make(requireView(), "Invalid CVV", Snackbar.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun processGooglePay(totalPrice: String) {
        val paymentDataRequestJson = GooglePayUtil.createPaymentDataRequest(totalPrice)
        if (paymentDataRequestJson == null) {
            Log.e("PaymentFragment", "Invalid Payment Data Request")
            return
        }

        val request = PaymentDataRequest.fromJson(paymentDataRequestJson.toString())
        val task = paymentsClient.loadPaymentData(request)

        task.addOnCompleteListener { completedTask ->
            try {
                val paymentData = completedTask.result
                handlePaymentSuccess(paymentData)
            } catch (e: Exception) {
                handlePaymentFailure(e)
            }
        }
    }

    private fun handlePaymentSuccess(paymentData: PaymentData?) {
        paymentData?.let {
            val paymentInfo = it.toJson()
            Log.d("PaymentFragment", "Payment Success: $paymentInfo")
            Snackbar.make(requireView(), "Payment successful!", Snackbar.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_paymentFragment_to_homepageFragment)
        }
    }

    private fun handlePaymentFailure(exception: Exception) {
        Log.e("PaymentFragment", "Payment Failed: ${exception.message}", exception)
        Snackbar.make(requireView(), "Payment failed! Please try again.", Snackbar.LENGTH_SHORT).show()
    }
}

object GooglePayUtil {

    fun createPaymentDataRequest(totalPrice: String): JSONObject? {
        return try {
            val paymentDataRequest = JSONObject()
                .put("apiVersion", 2)
                .put("apiVersionMinor", 0)
                .put("allowedPaymentMethods", getAllowedPaymentMethods())
                .put("transactionInfo", getTransactionInfo(totalPrice))
                .put("merchantInfo", getMerchantInfo())

            paymentDataRequest
        } catch (e: Exception) {
            null
        }
    }

    private fun getAllowedPaymentMethods(): JSONArray {
        val cardPaymentMethod = JSONObject()
            .put("type", "CARD")
            .put(
                "parameters", JSONObject()
                    .put("allowedAuthMethods", JSONArray(listOf("PAN_ONLY", "CRYPTOGRAM_3DS")))
                    .put("allowedCardNetworks", JSONArray(listOf("VISA", "MASTERCARD")))
            )
            .put(
                "tokenizationSpecification", JSONObject()
                    .put("type", "PAYMENT_GATEWAY")
                    .put(
                        "parameters", JSONObject()
                            .put("gateway", "example")
                            .put("gatewayMerchantId", "exampleMerchantId")
                    )
            )
        return JSONArray(listOf(cardPaymentMethod))
    }

    private fun getTransactionInfo(totalPrice: String): JSONObject {
        return JSONObject()
            .put("totalPrice", totalPrice)
            .put("totalPriceStatus", "FINAL")
            .put("currencyCode", "USD")
    }

    private fun getMerchantInfo(): JSONObject {
        return JSONObject()
            .put("merchantName", "Example Merchant")
    }
}
