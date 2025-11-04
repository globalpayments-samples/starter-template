package com.globalpayments.android.sample

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.globalpayments.android.sample.databinding.ActivityMainBinding
import com.globalpayments.android.sample.model.Transaction
import com.globalpayments.android.sample.service.PaymentService
import com.globalpayments.android.sample.service.TransactionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val paymentService = PaymentService()
    private lateinit var transactionManager: TransactionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        transactionManager = TransactionManager(this)
        
        initializeUI()
        setupWebView()
        setupButton()
        setupAuthorizeButton()
        setupTokenizeButton()
        setupHistoryButton()
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
    
    private fun setupTokenizeButton() {
        binding.tokenizeCardButton.setOnClickListener {
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
                if (cardData == null || cardData.has("error")) {
                    Toast.makeText(this, cardData?.optString("error") ?: "Please fill all card fields.", Toast.LENGTH_SHORT).show()
                    return@evaluateJavascript
                }
                
                // Call tokenize endpoint
                lifecycleScope.launch {
                    try {
                        val result = paymentService.tokenizeCard(
                            cardNumber = cardData.optString("cardNumber"),
                            expiry = cardData.optString("expiry"),
                            cvv = cardData.optString("cvv")
                        )
                        withContext(Dispatchers.Main) {
                            showTokenResultDialog(result)
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            showTokenResultDialog("❌ TOKENIZATION ERROR\n\nError: ${e.message}")
                        }
                    }
                }
            }
        }
    }
    
    private fun setupAuthorizeButton() {
        binding.authorizeButton.setOnClickListener {
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
                    null
                }
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
                
                lifecycleScope.launch {
                    try {
                        val result = paymentService.authorizeCard(
                            cardNumber = cardData.optString("cardNumber"),
                            expiry = cardData.optString("expiry"),
                            cvv = cardData.optString("cvv"),
                            amount = amount,
                            billingZip = zip
                        )
                        withContext(Dispatchers.Main) {
                            showAuthResultDialog(result)
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            showResultDialog("Authorization Error", "Exception: ${e.message}")
                        }
                    }
                }
            }
        }
    }
    
    private fun setupHistoryButton() {
        binding.historyButton.setOnClickListener {
            val intent = Intent(this, TransactionHistoryActivity::class.java)
            startActivity(intent)
        }
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
                            // Save transaction to history
                            saveTransaction(cardData, amount, result)
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
    
    private fun saveTransaction(cardData: org.json.JSONObject, amount: Double, result: String) {
        try {
            val cardNumber = cardData.optString("cardNumber", "")
            val lastFour = cardNumber.takeLast(4).padStart(4, '*')
            val status = if (result.contains("SUCCESSFUL")) "success" else "failed"
            val message = if (result.contains("SUCCESSFUL")) "" else result.substring(result.lastIndexOf('\n')).trim()
            
            // Extract card type based on first digit
            val cardType = when {
                cardNumber.startsWith("4") -> "Visa"
                cardNumber.startsWith("5") -> "Mastercard"
                cardNumber.startsWith("3") && !cardNumber.startsWith("36") -> "American Express"
                cardNumber.startsWith("6") -> "Discover"
                else -> "Card"
            }
            
            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                amount = amount,
                cardLastFour = lastFour,
                cardType = cardType,
                status = status,
                timestamp = System.currentTimeMillis(),
                billingZip = binding.billingZipEditText.text.toString(),
                message = message
            )
            
            transactionManager.addTransaction(transaction)
        } catch (e: Exception) {
            android.util.Log.e("TransactionSave", "Failed to save transaction", e)
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
                    * {
                        box-sizing: border-box;
                    }
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        margin: 0;
                        padding: 12px;
                        background-color: #F8F8F8;
                    }
                    .form-container {
                        background: white;
                        padding: 16px;
                        border-radius: 8px;
                        box-shadow: 0 1px 3px rgba(0,0,0,0.08);
                    }
                    .form-group {
                        margin-bottom: 14px;
                    }
                    label {
                        display: block;
                        margin-bottom: 6px;
                        font-weight: 600;
                        color: #0C0C0C;
                        font-size: 14px;
                    }
                    input, select {
                        width: 100%;
                        padding: 11px 12px;
                        border: 1.5px solid #C4C4C4;
                        border-radius: 6px;
                        font-size: 16px;
                        background-color: white;
                        font-family: inherit;
                    }
                    input:focus, select:focus {
                        outline: none;
                        border-color: #262AFF;
                        box-shadow: 0 0 0 3px rgba(38,42,255,0.1);
                    }
                    input::placeholder {
                        color: #999999;
                    }
                    .status {
                        margin-top: 14px;
                        padding: 10px 12px;
                        background-color: #E6F2FF;
                        color: #262AFF;
                        border-radius: 6px;
                        text-align: center;
                        font-size: 13px;
                        font-weight: 500;
                    }
                    .card-badge {
                        display: inline-block;
                        padding: 3px 8px;
                        background-color: #E6F2FF;
                        color: #262AFF;
                        border-radius: 3px;
                        font-size: 11px;
                        margin-left: 8px;
                        font-weight: 600;
                    }
                </style>
                <script>
                    const testCards = {
                        'visa': { number: '4111111111111111', expiry: '12/25', cvv: '123' },
                        'mastercard': { number: '5425233010103442', expiry: '12/25', cvv: '123' },
                        'amex': { number: '374245455400126', expiry: '12/25', cvv: '1234' },
                        'discover': { number: '6011111111111117', expiry: '12/25', cvv: '123' }
                    };
                    
                    function onCardSelect(value) {
                        if (value && testCards[value]) {
                            const card = testCards[value];
                            document.getElementById('cardNumber').value = card.number;
                            document.getElementById('expiry').value = card.expiry;
                            document.getElementById('cvv').value = card.cvv;
                            
                            // Reset field styles
                            [document.getElementById('cardNumber'), document.getElementById('expiry'), document.getElementById('cvv')].forEach(field => {
                                field.style.borderColor = '#ddd';
                                field.style.backgroundColor = '#fff';
                            });
                        }
                    }
                </script>
            </head>
            <body>
                <div class='form-container'>
                    <div class='form-group'>
                        <label for='cardSelect'>Sample Test Cards <span class='card-badge'>Quick Fill</span></label>
                        <select id='cardSelect' onchange='onCardSelect(this.value)'>
                            <option value=''>-- Select a test card --</option>
                            <option value='visa'>Visa (4111 1111 1111 1111)</option>
                            <option value='mastercard'>Mastercard (5425 2330 1010 3442)</option>
                            <option value='amex'>American Express (3742 454554 00126)</option>
                            <option value='discover'>Discover (6011 1111 1111 1117)</option>
                        </select>
                    </div>
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
    
    private fun showTokenResultDialog(message: String) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Token Generation Result")
            .setMessage(message)
            .setPositiveButton("Copy Token") { _, _ ->
                // Extract token from message if present
                val tokenMatch = Regex("""Token: ([^\n]+)""").find(message)
                if (tokenMatch != null) {
                    val token = tokenMatch.groupValues[1].trim()
                    val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("Token", token)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(this, "Token copied to clipboard!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "No token found in response", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Dismiss") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .create()
        
        dialog.apply {
            show()
            val messageView = findViewById<android.widget.TextView>(android.R.id.message)
            messageView?.setTextIsSelectable(true)
        }
    }
    
    private fun showAuthResultDialog(message: String) {
        val transactionIdMatch = Regex("""Transaction ID: ([^\n]+)""").find(message)
        val transactionId = transactionIdMatch?.groupValues?.get(1)?.trim() ?: ""
        
        val amountText = binding.amountEditText.text.toString()
        val amount = amountText.toDoubleOrNull() ?: 0.0
        
        val dialog = AlertDialog.Builder(this)
            .setTitle("Authorization Result")
            .setMessage(message)
            .setPositiveButton("Capture Later") { d, _ ->
                d.dismiss()
            }
            .setNegativeButton("Capture Now") { _, _ ->
                if (transactionId.isNotEmpty()) {
                    lifecycleScope.launch {
                        try {
                            val result = paymentService.captureTransaction(transactionId, amount)
                            withContext(Dispatchers.Main) {
                                showResultDialog("Capture Result", result)
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                showResultDialog("Capture Error", "Error: ${e.message}")
                            }
                        }
                    }
                }
            }
            .setCancelable(true)
            .create()
        
        dialog.apply {
            show()
            val messageView = findViewById<android.widget.TextView>(android.R.id.message)
            messageView?.setTextIsSelectable(true)
        }
    }
}
