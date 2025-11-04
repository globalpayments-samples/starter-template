package com.globalpayments.android.sample.service

import android.content.Context
import com.globalpayments.android.sample.model.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TransactionManager(private val context: Context) {
    
    private val sharedPreferences = context.getSharedPreferences("transactions", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val transactionListKey = "transaction_list"
    
    /**
     * Save a new transaction to history
     */
    fun addTransaction(transaction: Transaction) {
        val transactions = getTransactions().toMutableList()
        transactions.add(0, transaction)  // Add to front (newest first)
        
        // Keep only last 50 transactions to avoid excessive storage
        if (transactions.size > 50) {
            val trimmedList = transactions.take(50)
            val json = gson.toJson(trimmedList)
            sharedPreferences.edit().putString(transactionListKey, json).apply()
            return
        }
        
        val json = gson.toJson(transactions)
        sharedPreferences.edit().putString(transactionListKey, json).apply()
    }
    
    /**
     * Get all transactions sorted by newest first
     */
    fun getTransactions(): List<Transaction> {
        val json = sharedPreferences.getString(transactionListKey, "[]") ?: "[]"
        return try {
            val type = object : TypeToken<List<Transaction>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Get a specific transaction by ID
     */
    fun getTransactionById(id: String): Transaction? {
        return getTransactions().find { it.id == id }
    }
    
    /**
     * Get transactions by status (success, failed, pending)
     */
    fun getTransactionsByStatus(status: String): List<Transaction> {
        return getTransactions().filter { it.status.equals(status, ignoreCase = true) }
    }
    
    /**
     * Clear all transactions
     */
    fun clearTransactions() {
        sharedPreferences.edit().remove(transactionListKey).apply()
    }
    
    /**
     * Get transaction statistics
     */
    fun getStatistics(): TransactionStats {
        val transactions = getTransactions()
        val successTransactions = transactions.filter { it.status.equals("success", ignoreCase = true) }
        val totalAmount = successTransactions.sumOf { it.amount }
        
        return TransactionStats(
            totalTransactions = transactions.size,
            successfulTransactions = successTransactions.size,
            failedTransactions = transactions.count { it.status.equals("failed", ignoreCase = true) },
            totalAmount = totalAmount,
            averageAmount = if (successTransactions.isNotEmpty()) totalAmount / successTransactions.size else 0.0
        )
    }
}

data class TransactionStats(
    val totalTransactions: Int,
    val successfulTransactions: Int,
    val failedTransactions: Int,
    val totalAmount: Double,
    val averageAmount: Double
)
