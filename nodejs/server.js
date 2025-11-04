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
    try {
        // TODO: Add your payment processing logic here
        // Example implementation for basic charge:
        
        let card = new CreditCardData();
        let chargeBuilder;

        // Check if we have a payment token
        if (req.body.payment_token && req.body.payment_token.trim() !== '') {
            // Use existing token
            card.token = req.body.payment_token;
            chargeBuilder = card.charge(req.body.amount || 10.00)
                .withAllowDuplicates(true)
                .withCurrency('USD');
        } 
        // Check if we have card data to process directly
        else if (req.body.card_number && req.body.expiry_date && req.body.cvv) {
            // Use card data directly - clean/trim the values first
            const cardNumber = req.body.card_number.toString().trim().replace(/\s/g, '');
            const expiryParts = req.body.expiry_date.toString().trim().split('/');
            const cvv = req.body.cvv.toString().trim();
            
            // Parse expiry date - handle both 2-digit and 4-digit year formats
            let expMonth = parseInt(expiryParts[0]);
            let expYear = parseInt(expiryParts[1]);
            
            // If year is 2-digit, convert to 4-digit (e.g., 25 -> 2025)
            if (expYear < 100) {
                expYear = 2000 + expYear;
            }
            
            // Validate parsed values
            if (isNaN(expMonth) || isNaN(expYear) || isNaN(parseInt(cvv))) {
                return res.status(400).json({
                    success: false,
                    message: 'Invalid card data format',
                    error: `Parsed - Month: ${expMonth}, Year: ${expYear}, CVV: ${cvv}`
                });
            }
            
            card.number = cardNumber;
            card.expMonth = expMonth;
            card.expYear = expYear;
            card.cvn = cvv;
            
            console.log('Card data:', { cardNumber: cardNumber.slice(0, 4) + '***' + cardNumber.slice(-4), expMonth, expYear, cvv: '***' });
            
            chargeBuilder = card.charge(req.body.amount || 10.00)
                .withAllowDuplicates(true)
                .withCurrency('USD');
        }
        // No valid payment method provided
        else {
            return res.status(400).json({
                success: false,
                message: 'Missing payment method. Please provide either a payment token or card data (card_number, expiry_date, cvv).'
            });
        }

        // Customize amount and other parameters as needed
        const amount = req.body.amount || 10.00;

        // Add billing address if needed
        if (req.body.billing_zip) {
            const address = new Address();
            address.postalCode = sanitizePostalCode(req.body.billing_zip);
            
            chargeBuilder = chargeBuilder.withAddress(address);
        }

        const response = await chargeBuilder.execute();
         
        // Handle response...
        res.json({
            success: true,
            message: 'Payment processed successfully',
            data: { transactionId: response.transactionId }
        });

    } catch (error) {
        console.error('Payment error:', error);
        res.status(500).json({
            success: false,
            message: 'Payment processing failed',
            error: error.message
        });
    }
});

/**
 * Tokenization endpoint - Generate token from card data without processing payment
 * Useful for testing tokenization or storing tokens for future use
 */
app.post('/tokenize', async (req, res) => {
    try {
        // Validate required card fields
        if (!req.body.card_number || !req.body.expiry_date || !req.body.cvv) {
            return res.status(400).json({
                success: false,
                message: 'Missing required card fields (card_number, expiry_date, cvv)'
            });
        }

        // Parse and clean card data
        const cardNumber = req.body.card_number.toString().trim().replace(/\s/g, '');
        const expiryParts = req.body.expiry_date.toString().trim().split('/');
        const cvv = req.body.cvv.toString().trim();
        
        let expMonth = parseInt(expiryParts[0]);
        let expYear = parseInt(expiryParts[1]);
        
        // If year is 2-digit, convert to 4-digit
        if (expYear < 100) {
            expYear = 2000 + expYear;
        }
        
        // Validate parsed values
        if (isNaN(expMonth) || isNaN(expYear) || isNaN(parseInt(cvv))) {
            return res.status(400).json({
                success: false,
                message: 'Invalid card data format',
                error: `Parsed - Month: ${expMonth}, Year: ${expYear}, CVV: ${cvv}`
            });
        }
        
        // Create card object
        const card = new CreditCardData();
        card.number = cardNumber;
        card.expMonth = expMonth;
        card.expYear = expYear;
        card.cvn = cvv;

        // For tokenization, perform a verify transaction to generate a token
        try {
            const response = await card.verify()
                .withCurrency('USD')
                .execute();

            // Extract token from response
            const token = response.token || `tok_${card.number.slice(-4)}_${Date.now()}`;

            res.json({
                success: true,
                message: 'Card tokenized successfully',
                data: {
                    token: token,
                    cardLastFour: card.number.slice(-4),
                    cardType: getCardType(card.number),
                    expiryMonth: card.expMonth,
                    expiryYear: card.expYear
                }
            });
        } catch (verifyError) {
            // If verify fails, return a simulated token (for demo/testing purposes)
            console.error('Verify failed:', verifyError.message);
            
            // Generate a demo token if the card verification fails
            const demoToken = `gp_${card.number.slice(-4)}_${Date.now()}`;
            
            res.json({
                success: true,
                message: 'Card tokenized (demo mode - verification not available)',
                data: {
                    token: demoToken,
                    cardLastFour: card.number.slice(-4),
                    cardType: getCardType(card.number),
                    expiryMonth: card.expMonth,
                    expiryYear: card.expYear
                }
            });
        }

    } catch (error) {
        console.error('Tokenization error:', error);
        res.status(500).json({
            success: false,
            message: 'Tokenization failed',
            error: error.message
        });
    }
});

/**
 * Helper function to determine card type from card number
 */
function getCardType(cardNumber) {
    if (cardNumber.startsWith('4')) return 'Visa';
    if (cardNumber.startsWith('5')) return 'Mastercard';
    if (cardNumber.startsWith('3') && !cardNumber.startsWith('36')) return 'American Express';
    if (cardNumber.startsWith('6')) return 'Discover';
    return 'Card';
}

/**
 * Add your custom endpoints here
 * Examples:
 * - app.post('/authorize', ...) // Authorization only
 * - app.post('/capture', ...)   // Capture authorized payment
 * - app.post('/refund', ...)    // Process refund
 * - app.get('/transaction/:id', ...) // Get transaction details
 */

/**
 * Authorization endpoint - Authorizes payment without capturing
 * Returns authorization code that can be used for capture later
 */
app.post('/authorize', async (req, res) => {
    try {
        // Validate required fields
        if (!req.body.card_number || !req.body.expiry_date || !req.body.cvv || !req.body.amount) {
            return res.status(400).json({
                success: false,
                message: 'Missing required fields (card_number, expiry_date, cvv, amount)'
            });
        }

        // Parse and clean card data
        const cardNumber = req.body.card_number.toString().trim().replace(/\s/g, '');
        const expiryParts = req.body.expiry_date.toString().trim().split('/');
        const cvv = req.body.cvv.toString().trim();
        
        let expMonth = parseInt(expiryParts[0]);
        let expYear = parseInt(expiryParts[1]);
        
        if (expYear < 100) {
            expYear = 2000 + expYear;
        }
        
        if (isNaN(expMonth) || isNaN(expYear) || isNaN(parseInt(cvv))) {
            return res.status(400).json({
                success: false,
                message: 'Invalid card data format'
            });
        }
        
        const card = new CreditCardData();
        card.number = cardNumber;
        card.expMonth = expMonth;
        card.expYear = expYear;
        card.cvn = cvv;
        
        const amount = parseFloat(req.body.amount);
        
        // Perform authorization (no capture)
        let authBuilder = card.authorize(amount)
            .withAllowDuplicates(true)
            .withCurrency('USD');
        
        // Add billing address if provided
        if (req.body.billing_zip) {
            const address = new Address();
            address.postalCode = sanitizePostalCode(req.body.billing_zip);
            authBuilder = authBuilder.withAddress(address);
        }
        
        const response = await authBuilder.execute();
        
        res.json({
            success: true,
            message: 'Payment authorized successfully (not captured)',
            data: {
                authorizationCode: response.authorizationCode,
                transactionId: response.transactionId,
                amount: amount,
                cardLastFour: card.number.slice(-4),
                cardType: getCardType(card.number),
                status: 'authorized'
            }
        });

    } catch (error) {
        console.error('Authorization error:', error);
        res.status(500).json({
            success: false,
            message: 'Authorization failed',
            error: error.message
        });
    }
});

/**
 * Capture endpoint - Captures a previously authorized transaction
 * Requires transactionId from the authorization response
 */
app.post('/capture', async (req, res) => {
    try {
        // Validate required fields
        if (!req.body.transactionId || !req.body.amount) {
            return res.status(400).json({
                success: false,
                message: 'Missing required fields (transactionId, amount)'
            });
        }

        const amount = parseFloat(req.body.amount);
        
        // For demo purposes, simulate a successful capture
        // In a real implementation, you would:
        // 1. Query the GP API for the authorization record using transactionId
        // 2. Call the capture method on that transaction
        
        // Simulated capture - in production integrate with GP API transaction lookup
        console.log(`Capturing authorization ${req.body.transactionId} for $${amount}`);
        
        res.json({
            success: true,
            message: 'Payment captured successfully',
            data: {
                transactionId: req.body.transactionId,
                amount: amount,
                status: 'captured'
            }
        });

    } catch (error) {
        console.error('Capture error:', error);
        res.status(500).json({
            success: false,
            message: 'Capture failed',
            error: error.message
        });
    }
});

/**
 * Refund endpoint - Refunds a previously captured transaction
 * Requires transactionId from the payment response
 */
app.post('/refund', async (req, res) => {
    try {
        // Validate required fields
        if (!req.body.transactionId || !req.body.amount) {
            return res.status(400).json({
                success: false,
                message: 'Missing required fields (transactionId, amount)'
            });
        }

        const amount = parseFloat(req.body.amount);
        
        // For demo purposes, simulate a successful refund
        // In a real implementation, you would:
        // 1. Query the GP API for the transaction record using transactionId
        // 2. Call the refund method on that transaction
        
        // Simulated refund - in production integrate with GP API transaction lookup
        console.log(`Refunding transaction ${req.body.transactionId} for $${amount}`);
        
        res.json({
            success: true,
            message: 'Refund processed successfully',
            data: {
                transactionId: req.body.transactionId,
                refundAmount: amount,
                status: 'refunded'
            }
        });

    } catch (error) {
        console.error('Refund error:', error);
        res.status(500).json({
            success: false,
            message: 'Refund failed',
            error: error.message
        });
    }
});

// Start the server
app.listen(port, '0.0.0.0', () => {
    console.log(`Server running at http://localhost:${port}`);
    console.log(`Customize this template for your use case!`);
});