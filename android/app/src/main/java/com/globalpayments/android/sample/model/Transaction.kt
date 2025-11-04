package com.globalpayments.android.sample.model

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

data class Transaction(
    val id: String,                 // Transaction ID from backend
    val amount: Double,             // Payment amount
    val cardLastFour: String,       // Last 4 digits of card
    val cardType: String,           // Visa, Mastercard, etc.
    val status: String,             // "success", "failed", "pending", "authorized", "refunded"
    val timestamp: Long,            // Unix timestamp
    val billingZip: String,         // Billing zip code
    val message: String = "",       // Error message or additional info
    val transactionId: String = "", // Backend transaction ID for capture/refund
    val authCode: String = ""       // Authorization code for authorized transactions
) : Serializable {
    
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    fun getFormattedAmount(): String {
        return String.format(Locale.US, "$%.2f", amount)
    }
    
    fun getStatusColor(): Int {
        return when (status.lowercase()) {
            "success" -> android.graphics.Color.parseColor("#4CAF50")  // Green
            "failed" -> android.graphics.Color.parseColor("#F44336")   // Red
            "pending" -> android.graphics.Color.parseColor("#FF9800")  // Orange
            else -> android.graphics.Color.parseColor("#999999")       // Gray
        }
    }
    
    fun getStatusIcon(): String {
        return when (status.lowercase()) {
            "success" -> "✓"
            "failed" -> "✗"
            "pending" -> "⏳"
            else -> "?"
        }
    }
}
