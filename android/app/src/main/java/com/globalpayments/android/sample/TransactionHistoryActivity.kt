package com.globalpayments.android.sample

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.globalpayments.android.sample.model.Transaction
import com.globalpayments.android.sample.service.TransactionManager

class TransactionHistoryActivity : AppCompatActivity() {
    
    private lateinit var transactionManager: TransactionManager
    private lateinit var transactionContainer: LinearLayout
    private lateinit var emptyStateText: TextView
    private lateinit var statsContainer: LinearLayout
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_history)
        
        transactionManager = TransactionManager(this)
        
        // Set up toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Transaction History"
        
        // Initialize views
        transactionContainer = findViewById(R.id.transactionContainer)
        emptyStateText = findViewById(R.id.emptyStateText)
        statsContainer = findViewById(R.id.statsContainer)
        
        val clearButton: Button = findViewById(R.id.clearButton)
        clearButton.setOnClickListener { showClearConfirmation() }
        
        loadTransactions()
    }
    
    private fun loadTransactions() {
        val transactions = transactionManager.getTransactions()
        
        transactionContainer.removeAllViews()
        
        if (transactions.isEmpty()) {
            emptyStateText.visibility = View.VISIBLE
            statsContainer.visibility = View.GONE
        } else {
            emptyStateText.visibility = View.GONE
            statsContainer.visibility = View.VISIBLE
            
            // Display statistics
            displayStatistics()
            
            // Display each transaction
            for (transaction in transactions) {
                val transactionView = createTransactionView(transaction)
                transactionContainer.addView(transactionView)
            }
        }
    }
    
    private fun displayStatistics() {
        val stats = transactionManager.getStatistics()
        val statsText = findViewById<TextView>(R.id.statsText)
        
        statsText.text = String.format(
            "Total Transactions: %d | Successful: %d | Failed: %d\nTotal Amount: $%.2f | Avg: $%.2f",
            stats.totalTransactions,
            stats.successfulTransactions,
            stats.failedTransactions,
            stats.totalAmount,
            stats.averageAmount
        )
    }
    
    private fun createTransactionView(transaction: Transaction): View {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 8, 16, 8)
        }
        
        // Header row
        val headerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        // Status icon and date
        val dateStatusText = TextView(this).apply {
            text = "${transaction.getStatusIcon()} ${transaction.getFormattedDate()}"
            textSize = 14f
            setTextColor(transaction.getStatusColor())
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            maxLines = 1
        }
        
        // Amount
        val amountText = TextView(this).apply {
            text = transaction.getFormattedAmount()
            textSize = 16f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setTextColor(android.graphics.Color.parseColor("#333333"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        headerRow.addView(dateStatusText)
        headerRow.addView(amountText)
        
        // Details row
        val detailsText = TextView(this).apply {
            text = "${transaction.cardType} ••••${transaction.cardLastFour} | Status: ${transaction.status} | Zip: ${transaction.billingZip}"
            textSize = 12f
            setTextColor(android.graphics.Color.parseColor("#666666"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        // Click to see details
        val clickableArea = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(android.graphics.Color.parseColor("#f5f5f5"))
            setPadding(12, 12, 12, 12)
            isClickable = true
            isFocusable = true
            setOnClickListener { showTransactionDetails(transaction) }
        }
        
        clickableArea.addView(headerRow)
        clickableArea.addView(detailsText)
        
        // Divider
        val divider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            )
            setBackgroundColor(android.graphics.Color.parseColor("#e0e0e0"))
        }
        
        container.addView(clickableArea)
        container.addView(divider)
        
        return container
    }
    
    private fun showTransactionDetails(transaction: Transaction) {
        val message = """
            Transaction ID: ${transaction.id}
            Amount: ${transaction.getFormattedAmount()}
            Card: ${transaction.cardType} ••••${transaction.cardLastFour}
            Status: ${transaction.status}
            Date: ${transaction.getFormattedDate()}
            Billing Zip: ${transaction.billingZip}
            
            ${if (transaction.message.isNotEmpty()) "Message: ${transaction.message}" else ""}
        """.trimIndent()
        
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("Transaction Details")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setCancelable(true)
        
        // Add refund button for successful transactions
        if (transaction.status.lowercase() == "success" && transaction.id.isNotEmpty()) {
            dialogBuilder.setNegativeButton("Refund") { _, _ ->
                showRefundConfirmation(transaction)
            }
        }
        
        dialogBuilder.show()
    }
    
    private fun showRefundConfirmation(transaction: Transaction) {
        AlertDialog.Builder(this)
            .setTitle("Refund Transaction")
            .setMessage("Refund ${transaction.getFormattedAmount()} for ${transaction.cardType} ••••${transaction.cardLastFour}?")
            .setPositiveButton("Confirm") { _, _ ->
                performRefund(transaction)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun performRefund(transaction: Transaction) {
        // Import PaymentService and call refund
        val paymentService = com.globalpayments.android.sample.service.PaymentService()
        
        lifecycleScope.launch {
            try {
                val result = paymentService.refundTransaction(
                    transactionId = transaction.id,
                    amount = transaction.amount
                )
                withContext(Dispatchers.Main) {
                    AlertDialog.Builder(this@TransactionHistoryActivity)
                        .setTitle("Refund Result")
                        .setMessage(result)
                        .setPositiveButton("OK") { _, _ ->
                            loadTransactions() // Reload to show updated status
                        }
                        .show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    AlertDialog.Builder(this@TransactionHistoryActivity)
                        .setTitle("Refund Error")
                        .setMessage("Error: ${e.message}")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        }
    }
    
    private fun showClearConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Clear History?")
            .setMessage("Are you sure you want to delete all transactions? This cannot be undone.")
            .setPositiveButton("Yes") { _, _ ->
                transactionManager.clearTransactions()
                loadTransactions()
            }
            .setNegativeButton("No", null)
            .show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
