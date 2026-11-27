# PHP Card Payment Example

This example demonstrates card payment processing using PHP and the Global Payments SDK.

## Requirements

- PHP 8.2.35+, 8.3.36+, 8.4.27+, or 8.5.12+
- Composer
- Global Payments account and API credentials

## Project Structure

- `process-payment.php` - Payment processing script
- `index.html` - Client-side payment form
- `config.php` - Returns the public API key for the hosted fields
- `router.php` - Router for the PHP built-in server
- `composer.json` - Project dependencies
- `.env.sample` - Template for environment variables
- `run.sh` - Convenience script to run the application

## Setup

1. Clone this repository
2. Copy `.env.sample` to `.env`
3. Update `.env` with your Global Payments credentials:
   ```
   PUBLIC_API_KEY=pk_test_xxx
   SECRET_API_KEY=sk_test_xxx
   ```
4. Install dependencies:
   ```bash
   composer install
   ```
5. Run the application:
   ```bash
   ./run.sh
   ```
   Or manually:
   ```bash
   php -S 127.0.0.1:8000 router.php
   ```

## Implementation Details

### Application Structure
The application uses a simple PHP structure:
- Static HTML form for payment collection
- Separate PHP script for payment processing
- Composer for dependency management

### SDK Configuration
Global Payments SDK configuration using environment variables:
- Loads credentials from .env file
- Sets up service URL for API communication
- Configures developer identification

### Payment Processing
Payment processing flow:
1. Client submits payment token and billing zip
2. Server creates CreditCardData with token
3. Creates Address with postal code
4. Charges the amount from the form in USD
5. Returns success/error response

### Error Handling
Implements comprehensive error handling:
- Catches and processes API exceptions
- Returns appropriate error messages
- Handles edge cases gracefully

## API Endpoints

### GET /config
Returns the public API key the page uses to set up the hosted fields.

Response:
```json
{
    "success": true,
    "data": {
        "publicApiKey": "pkapi_cert_xxx"
    }
}
```

### POST /process-payment
Charges the tokenized card in USD. The page posts it as a form (`application/x-www-form-urlencoded`).

Request Parameters:
- `payment_token` (string, required) - Single use token from the hosted fields
- `amount` (string, required) - Amount to charge, for example `10.00`
- `billing_zip` (string, required) - Billing postal code, sent for AVS

Response (Success):
```json
{
    "success": true,
    "message": "Payment successful! Transaction ID: xxx",
    "data": {
        "transactionId": "xxx"
    }
}
```

Response (Error, HTTP 400):
```json
{
    "success": false,
    "message": "Payment processing failed",
    "error": {
        "code": "VALIDATION_ERROR | PAYMENT_DECLINED | API_ERROR",
        "details": "Error message"
    }
}
```

## Security Considerations

This example demonstrates basic implementation. For production use, consider:
- Implementing additional input validation
- Adding request rate limiting
- Including security headers
- Implementing proper logging
- Adding payment fraud prevention measures
- Using HTTPS in production
- Implementing CSRF protection
- Configuring proper session handling
- Setting appropriate PHP security directives
