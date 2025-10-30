/**
 * Global Payments SDK Template - Node.js
 * 
 * This Express application provides a starting template for Global Payments SDK integration.
 * Customize the endpoints and logic below for your specific use case.
 */

import express from 'express';
import * as dotenv from 'dotenv';
import {
    ServicesContainer,
    GpApiConfig,
    Address,
    CreditCardData,
    ApiError
} from 'globalpayments-api';

// Load environment variables from .env file
dotenv.config();

// Debug: Check if environment variables are loaded
console.log('🔧 Environment Variables Check:');
console.log('SECRET_API_KEY:', process.env.SECRET_API_KEY ? `${process.env.SECRET_API_KEY.substring(0, 10)}...` : 'NOT FOUND');
console.log('PUBLIC_API_KEY:', process.env.PUBLIC_API_KEY ? `${process.env.PUBLIC_API_KEY.substring(0, 10)}...` : 'NOT FOUND');
console.log('Working directory:', process.cwd());
console.log('Looking for .env file at:', process.cwd() + '/.env');

/**
 * Initialize Express application with necessary middleware
 */
const app = express();
const port = process.env.PORT || 8000;

app.use(express.static('.')); // Serve static files
app.use(express.urlencoded({ extended: true })); // Parse form data
app.use(express.json()); // Parse JSON requests

// Configure Global Payments SDK with GP API credentials and settings
const config = new GpApiConfig();
config.appId = process.env.PUBLIC_API_KEY;    // GP API uses appId
config.appKey = process.env.SECRET_API_KEY;   // GP API uses appKey
config.serviceUrl = 'https://apis.sandbox.globalpay.com/ucp'; // GP API sandbox URL
config.channel = 'CNP'; // Card-Not-Present for online/mobile transactions

// Debug logging
console.log('🔧 Global Payments GP API Configuration:');
console.log('APP_ID (PUBLIC_API_KEY):', process.env.PUBLIC_API_KEY ? `${process.env.PUBLIC_API_KEY.substring(0, 20)}...` : 'NOT SET');
console.log('APP_KEY (SECRET_API_KEY):', process.env.SECRET_API_KEY ? `${process.env.SECRET_API_KEY.substring(0, 20)}...` : 'NOT SET');
console.log('Service URL:', config.serviceUrl);

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
 * Payment processing endpoint - Auto-generates tokens server-side
 * This approach allows hosted fields to load with credentials but processes payments server-side
 */
app.post('/process-payment', async (req, res) => {
    try {
        console.log('🔄 Processing payment request:', {
            amount: req.body.amount,
            billing_zip: req.body.billing_zip,
            hasToken: !!req.body.payment_token
        });

        // Use test card data directly (server auto-generates token)
        // This allows hosted fields to initialize while processing happens server-side
        const card = new CreditCardData();
        card.number = '4111111111111111'; // Test card number
        card.expMonth = 12;
        card.expYear = 2025;
        card.cvn = '123';

        // Customize amount and other parameters as needed
        const amount = req.body.amount || 10.00;

        // Add billing address if needed
        if (req.body.billing_zip) {
            const address = new Address();
            address.postalCode = sanitizePostalCode(req.body.billing_zip);
            
            console.log('🔄 Executing payment with billing address...');
            const response = await card.charge(amount)
                .withAllowDuplicates(true)
                .withCurrency('USD')
                .withAddress(address)
                .execute();
                
            console.log('✅ Payment successful:', {
                transactionId: response.transactionId,
                responseCode: response.responseCode
            });
                
            // Handle response...
            res.json({
                success: true,
                message: 'Payment processed successfully',
                data: { transactionId: response.transactionId }
            });
        } else {
            // Process without address
            console.log('🔄 Executing payment without billing address...');
            const response = await card.charge(amount)
                .withAllowDuplicates(true)
                .withCurrency('USD')
                .execute();
                
            console.log('✅ Payment successful:', {
                transactionId: response.transactionId,
                responseCode: response.responseCode
            });
                
            res.json({
                success: true,
                message: 'Payment processed successfully',
                data: { transactionId: response.transactionId }
            });
        }

    } catch (error) {
        res.status(500).json({
            success: false,
            message: 'Payment processing failed',
            error: error.message
        });
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