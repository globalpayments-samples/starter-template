# Ruby Card Payment Example

This example demonstrates card payment processing using Ruby and the Global Payments SDK.

## Requirements

- Ruby 2.7 or later
- Bundler
- Global Payments account and API credentials

## Project Structure

- `app.rb` - Main Sinatra app with payment and config routes
- `views/` - Contains HTML form (`index.erb`)
- `.env.sample` - Template for environment variables
- `run.sh` - Convenience script to run the application

## Setup

1. Clone this repository
2. Copy `.env.sample` to `.env`
3. Fill in your API credentials
4. Install dependencies:
   ```bash
   bundle install
   ```
5. Run the app:
   ```bash
   ./run.sh
   ```

## API Endpoints

### POST /process-payment
Processes a payment using the provided token and billing information.

### GET /config
Returns public API key for client-side SDK.

## Security Considerations

This demo is simplified. In production, add:
- Validation and sanitization
- Rate limiting
- HTTPS and secure headers
- CSRF protection
- Logging and monitoring
