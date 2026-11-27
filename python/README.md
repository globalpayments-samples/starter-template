# Python Card Payment Example

This example demonstrates card payment processing using Flask and the Global Payments SDK.

## Requirements

- Python 3.10 or later
- pip (Python Package Installer)
- Global Payments account and API credentials

## Project Structure

- `server.py` - Main application file containing server setup and payment processing
- `index.html` - Client-side payment form
- `requirements.txt` - Project dependencies
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
4. Create and activate a virtual environment (recommended):
   ```bash
   python -m venv venv
   source venv/bin/activate  # On Windows, use: venv\Scripts\activate
   ```
5. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```
6. Run the application:
   ```bash
   ./run.sh
   ```
   Or manually:
   ```bash
   python server.py
   ```

## Implementation Details

### Server Setup
The application uses Flask to create a web server that:
- Serves static files
- Processes payment requests
- Provides configuration endpoint for client-side SDK
- Handles JSON responses

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
- Returns JSON responses for errors
- Includes appropriate HTTP status codes

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
- Configuring secure session handling
- Using a production-grade WSGI server
