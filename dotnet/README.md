# .NET Card Payment Example

This example demonstrates card payment processing using ASP.NET Core and the Global Payments SDK.

## Requirements

- .NET 10.0 SDK or later
- Global Payments account and API credentials

## Project Structure

- `Program.cs` - Main application file containing server setup and payment processing
- `wwwroot/index.html` - Client-side payment form
- `.env.sample` - Template for environment variables
- `run.sh` - Convenience script to run the application
- `appsettings.json` - Application configuration file

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
   dotnet restore
   ```
5. Run the application:
   ```bash
   ./run.sh
   ```
   Or manually:
   ```bash
   dotnet run
   ```

## Implementation Details

### Server Setup
The application uses ASP.NET Core's minimal API approach to create a lightweight web server that:
- Serves static files from wwwroot directory
- Processes payment requests
- Provides configuration endpoint for client-side SDK

### SDK Configuration
The Global Payments SDK is configured using environment variables and the PorticoConfig class:
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
- Returns appropriate HTTP status codes
- Provides meaningful error messages

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
