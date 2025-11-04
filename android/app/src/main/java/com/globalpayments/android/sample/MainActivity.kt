package com.globalpayments.android.sample

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.globalpayments.android.sample.databinding.ActivityMainBinding
import com.globalpayments.android.sample.service.PaymentService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val paymentService = PaymentService()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initializeUI()
        setupWebView()
        setupButton()
    }
    
    private fun initializeUI() {
        // Update SDK status
        binding.sdkStatusTextView.text = "Global Payments SDK Ready"
        
        // Hide the loading spinner initially
        binding.loadingProgressBar.visibility = android.view.View.GONE
        
        // Enable the payment button
        binding.processPaymentButton.isEnabled = true
        
        // Pre-populate test data for quick testing
        binding.amountEditText.setText("10.00")
        binding.billingZipEditText.setText("12345")
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.hostedFieldsWebView.settings.javaScriptEnabled = true
        
        // Set up WebView client to handle page loading
        binding.hostedFieldsWebView.webViewClient = object : android.webkit.WebViewClient() {
            override fun onPageStarted(view: android.webkit.WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.loadingProgressBar.visibility = android.view.View.VISIBLE
            }
            
            override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.loadingProgressBar.visibility = android.view.View.GONE
            }
        }
        
        loadPaymentForm()
    }
    
    private fun setupButton() {
        binding.processPaymentButton.setOnClickListener {
            binding.hostedFieldsWebView.evaluateJavascript(
                """
                (function() {
                    var card = document.getElementById('cardNumber');
                    var expiry = document.getElementById('expiry');
                    var cvv = document.getElementById('cvv');
                    var missing = [];
                    if (!card.value.trim()) missing.push(card);
                    if (!expiry.value.trim()) missing.push(expiry);
                    if (!cvv.value.trim()) missing.push(cvv);
                    missing.forEach(function(field) {
                        field.style.borderColor = '#dc3545';
                        field.style.backgroundColor = '#f8d7da';
                    });
                    if (missing.length > 0) {
                        return JSON.stringify({ error: 'Please fill all card fields.' });
                    }
                    // Reset styles if all fields are filled
                    [card, expiry, cvv].forEach(function(field) {
                        field.style.borderColor = '#ddd';
                        field.style.backgroundColor = '#fff';
                    });
                    return JSON.stringify({
                        cardNumber: card.value.trim(),
                        expiry: expiry.value.trim(),
                        cvv: cvv.value.trim()
                    });
                })();
                """
            ) { cardDataJson ->
                val cleanJson = cardDataJson.trim().let {
                    if (it.startsWith("\"") && it.endsWith("\"")) {
                        it.substring(1, it.length - 1).replace("\\\"", "\"")
                    } else it
                }
                val cardData = try {
                    org.json.JSONObject(cleanJson)
                } catch (e: Exception) {
                    android.util.Log.e("GP_DEBUG", "Failed to parse cardDataJson: $cleanJson", e)
                    null
                }
                android.util.Log.d("GP_DEBUG", "Card Data from WebView: $cardDataJson")
                if (cardData == null || cardData.has("error")) {
                    Toast.makeText(this, cardData?.optString("error") ?: "Please fill all card fields.", Toast.LENGTH_SHORT).show()
                    return@evaluateJavascript
                }
                val amountText = binding.amountEditText.text.toString()
                val zip = binding.billingZipEditText.text.toString()
                if (amountText.isEmpty() || zip.length < 3) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@evaluateJavascript
                }
                val amount = amountText.toDoubleOrNull() ?: 0.0
                // val paymentToken = cardData.optString("cardNumber")
                // Send card data to backend so it generates token
                lifecycleScope.launch {
                    try {
                        val result = paymentService.processPaymentWithCardData(
                            cardNumber = cardData.optString("cardNumber"),
                            expiry = cardData.optString("expiry"),
                            cvv = cardData.optString("cvv"),
                            amount = amount,
                            billingZip = zip
                        )
                        withContext(Dispatchers.Main) {
                            showResultDialog("Payment Result", result)
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            showResultDialog("Payment Error", "Exception: ${e.message}\nStack: ${e.stackTrace.take(3).joinToString("\n")}")
                        }
                    }
                }
            }
        }
    }
    
    private fun loadPaymentForm() {
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset='utf-8'>
                <meta name='viewport' content='width=device-width, initial-scale=1'>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        margin: 0;
                        padding: 16px;
                        background-color: #f8f9fa;
                    }
                    .form-container {
                        background: white;
                        padding: 20px;
                        border-radius: 8px;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                    }
                    .form-group {
                        margin-bottom: 16px;
                    }
                    label {
                        display: block;
                        margin-bottom: 4px;
                        font-weight: bold;
                        color: #333;
                    }
                    input {
                        width: 100%;
                        padding: 12px;
                        border: 1px solid #ddd;
                        border-radius: 4px;
                        font-size: 16px;
                        box-sizing: border-box;
                    }
                    input:focus {
                        outline: none;
                        border-color: #007bff;
                        box-shadow: 0 0 0 2px rgba(0,123,255,0.25);
                    }
                    .status {
                        margin-top: 16px;
                        padding: 8px;
                        background-color: #d4edda;
                        color: #155724;
                        border-radius: 4px;
                        text-align: center;
                    }
                </style>
            </head>
            <body>
                <div class='form-container'>
                    <div class='form-group'>
                        <label for='cardNumber'>Card Number</label>
                        <input type='text' id='cardNumber' placeholder='Card Number' maxlength='19'>
                    </div>
                    <div class='form-group'>
                        <label for='expiry'>Expiry Date</label>
                        <input type='text' id='expiry' placeholder='MM/YY' maxlength='5'>
                    </div>
                    <div class='form-group'>
                        <label for='cvv'>CVV</label>
                        <input type='text' id='cvv' placeholder='CVV' maxlength='4'>
                    </div>
                    <div class='status'>
                        ✓ Hosted Fields Ready for Secure Payment
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
        
        binding.hostedFieldsWebView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }
    
    private fun processPayment() {
        val amountText = binding.amountEditText.text.toString()
        val zip = binding.billingZipEditText.text.toString()
        
        // Show immediate feedback that button was clicked
        Toast.makeText(this, "Processing payment...", Toast.LENGTH_SHORT).show()
        
        if (amountText.isEmpty() || zip.length < 3) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        val amount = amountText.toDoubleOrNull() ?: 0.0
        
        // Process payment on background thread
        // Get card data from hosted fields WebView
        binding.hostedFieldsWebView.evaluateJavascript(
            """
            (function() {
                return JSON.stringify({
                    cardNumber: document.getElementById('cardNumber').value.trim(),
                    expiry: document.getElementById('expiry').value.trim(),
                    cvv: document.getElementById('cvv').value.trim()
                });
            })();
            """
        ) { cardDataJson ->
            val cardData = try {
                org.json.JSONObject(cardDataJson)
            } catch (e: Exception) {
                null
            }
            if (cardData == null ||
                cardData.optString("cardNumber").isEmpty() ||
                cardData.optString("expiry").isEmpty() ||
                cardData.optString("cvv").isEmpty()) {
                Toast.makeText(this, "Please fill all card fields.", Toast.LENGTH_SHORT).show()
                return@evaluateJavascript
            }
            // Tokenize card data here (call your tokenization API)
            // For demo, just pass card number as token (replace with real tokenization)
            val paymentToken = cardData.optString("cardNumber")
            lifecycleScope.launch {
                try {
                    val result = paymentService.processPaymentWithToken(paymentToken, amount)
                    withContext(Dispatchers.Main) {
                        showResultDialog("Payment Result", result)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        showResultDialog("Payment Error", "Exception: ${e.message}\nStack: ${e.stackTrace.take(3).joinToString("\n")}")
                    }
                }
            }
        }
    }
    
    private fun showResultDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> 
                dialog.dismiss() 
            }
            .setCancelable(true)
            .create()
            .apply {
                // Make the dialog scrollable for long messages
                show()
                // Allow text selection in the dialog
                val messageView = findViewById<android.widget.TextView>(android.R.id.message)
                messageView?.setTextIsSelectable(true)
            }
    }
}
