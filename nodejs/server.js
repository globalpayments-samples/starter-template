/**
 * Global Payments SDK Template - Node.js
 * 
 * This Express application provides a starting template for Global Payments SDK integration.
 * Customize the endpoints and logic below for your specific use case.
 */

import express from 'express';
import * as dotenv from 'dotenv';
import path from 'path';
import { fileURLToPath } from 'url';
import { readFileSync } from 'fs';
import {
    ServicesContainer,
    PorticoConfig,
    Address,
    CreditCardData
} from 'globalpayments-api';

// Load environment variables from .env file
dotenv.config();

/**
 * Initialize Express application with necessary middleware
 */
const app = express();
const port = process.env.PORT || 8000;

// Serve only the frontend page, never the server's own files
const __dirname = path.dirname(fileURLToPath(import.meta.url));
const indexHtml = readFileSync(path.join(__dirname, 'index.html'), 'utf8');
app.get(['/', '/index.html'], (req, res) => {
    res.type('html').send(indexHtml);
});
app.use(express.urlencoded({ extended: true })); // Parse form data
app.use(express.json()); // Parse JSON requests

// Configure Global Payments SDK with credentials and settings
const config = new PorticoConfig();
config.secretApiKey = process.env.SECRET_API_KEY;
config.serviceUrl = 'https://cert.api2.heartlandportico.com'; // Use production URL for live transactions
ServicesContainer.configureService(config);

/**
 * Utility function to sanitize postal code
 * Customize validation logic as needed for your use case
 */
const sanitizePostalCode = (postalCode) => {
    return postalCode.replace(/[^a-zA-Z0-9-]/g, '').slice(0, 10);
};

/**
 * Config endpoint - provides public API key for client-side use
 * Customize response data as needed
 */
app.get('/config', (req, res) => {
    res.json({
        success: true,
        data: {
            publicApiKey: process.env.PUBLIC_API_KEY
            // Add other configuration data as needed
        }
    });
});

/**
 * Example payment processing endpoint
 * Customize this endpoint for your specific payment flow
 */
app.post('/process-payment', async (req, res) => {
    // Send the same error shape as the other implementations
    const fail = (status, code, details) => res.status(status).json({
        success: false,
        message: 'Payment processing failed',
        error: { code, details }
    });

    // TODO: Add your payment processing logic here
    // Example implementation for basic charge:
    const { payment_token: paymentToken, billing_zip: billingZip, amount: amountStr } = req.body;
    if (!paymentToken || !billingZip || !amountStr) {
        return fail(400, 'VALIDATION_ERROR', 'Missing required fields');
    }

    // Customize amount and other parameters as needed
    const amount = Number(amountStr);
    if (!Number.isFinite(amount) || amount <= 0) {
        return fail(400, 'VALIDATION_ERROR', 'Amount must be a positive number');
    }

    try {
        const card = new CreditCardData();
        card.token = paymentToken;

        // Add billing address for AVS verification
        const address = new Address();
        address.postalCode = sanitizePostalCode(billingZip);

        const response = await card.charge(amount.toFixed(2))
            .withAllowDuplicates(true)
            .withCurrency('USD')
            .withAddress(address)
            .execute();

        // A declined card comes back as a normal response, not an error
        if (response.responseCode !== '00') {
            return fail(400, 'PAYMENT_DECLINED', response.responseMessage);
        }

        res.json({
            success: true,
            message: `Payment successful! Transaction ID: ${response.transactionId}`,
            data: { transactionId: response.transactionId }
        });
    } catch (error) {
        fail(400, 'API_ERROR', error.message);
    }
});

/**
 * Add your custom endpoints here
 * Examples:
 * - app.post('/authorize', ...) // Authorization only
 * - app.post('/capture', ...)   // Capture authorized payment
 * - app.post('/refund', ...)    // Process refund
 * - app.get('/transaction/:id', ...) // Get transaction details
 */

// Start the server
app.listen(port, '0.0.0.0', () => {
    console.log(`Server running at http://localhost:${port}`);
    console.log(`Customize this template for your use case!`);
});