package com.cs407.studentbazaar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import com.google.android.material.snackbar.Snackbar
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

        val rg_payment_options = view.findViewById<RadioGroup>(R.id.rg_payment_options)
        val credit_card_details = view.findViewById<View>(R.id.credit_card_details)
        val btn_confirm_payment = view.findViewById<View>(R.id.btn_confirm_payment)

        // Initialize Google Pay API client
        paymentsClient = Wallet.getPaymentsClient(
            requireActivity(),
            Wallet.WalletOptions.Builder()
                .setEnvironment(googlePayEnvironment)
                .build()
        )

        // Handle payment option selection
        rg_payment_options.setOnCheckedChangeListener { _, checkedId ->
            val radioButton = view.findViewById<RadioButton>(checkedId)
            if (radioButton?.id == R.id.rb_credit_card) {
                credit_card_details.visibility = View.VISIBLE
            } else {
                credit_card_details.visibility = View.GONE
            }
        }

        // Handle confirm payment button click
        btn_confirm_payment.setOnClickListener {
            val selectedOption = rg_payment_options.checkedRadioButtonId
            when (selectedOption) {
                R.id.rb_cash_meetup -> handleCashMeetUp()
                R.id.rb_credit_card -> processGooglePay()
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
    }

    private fun processGooglePay() {
        val paymentDataRequestJson = GooglePayUtil.createPaymentDataRequest()
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
        }
    }

    private fun handlePaymentFailure(exception: Exception) {
        Log.e("PaymentFragment", "Payment Failed: ${exception.message}", exception)
        Snackbar.make(requireView(), "Payment failed!", Snackbar.LENGTH_SHORT).show()
    }
}

object GooglePayUtil {

    fun createPaymentDataRequest(): JSONObject? {
        return try {
            val paymentDataRequest = JSONObject()
                .put("apiVersion", 2)
                .put("apiVersionMinor", 0)
                .put("allowedPaymentMethods", getAllowedPaymentMethods())
                .put("transactionInfo", getTransactionInfo())
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

    private fun getTransactionInfo(): JSONObject {
        return JSONObject()
            //TODO: Change "10.00" to obtain from added item to Cart
            .put("totalPrice", "10.00")
            .put("totalPriceStatus", "FINAL")
            .put("currencyCode", "USD")
    }

    private fun getMerchantInfo(): JSONObject {
        return JSONObject()
            //TODO: Change "Example Merchant" to obtain from seller of the item
            .put("merchantName", "Example Merchant")
    }
}