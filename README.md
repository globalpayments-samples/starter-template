```markdown
# Ruby Card Payment Example

This example demonstrates how to process card payments using Ruby and the Global Payments SDK.

---

## Requirements

- Ruby 2.7 or later  
- [Bundler](https://bundler.io/)  
- Global Payments account and API credentials  

---

## Project Structure

```

.
├── app.rb          # Main Sinatra app with payment and config routes
├── views/
│   └── index.erb   # HTML form for payment
├── .env.sample     # Template for environment variables
└── run.sh          # Convenience script to run the application

````

---

## Setup

1. Clone this repository:
   ```bash
   git clone https://github.com/your-org/your-repo.git
   cd your-repo
````

2. Copy the environment template:

   ```bash
   cp .env.sample .env
   ```

3. Fill in your API credentials in `.env`.

4. Install dependencies:

   ```bash
   bundle install
   ```

5. Run the application:

   ```bash
   ./run.sh
   ```

---

## API Endpoints

### `POST /process-payment`

Processes a card payment using the provided token and billing information.

### `GET /config`

Returns the public API key used by the client-side SDK.

---

## Security Considerations

This is a demo and intentionally simplified. For production use, implement:

* Input validation and sanitization
* Rate limiting
* HTTPS with secure headers
* CSRF protection
* Logging and monitoring

```

Let me know if you'd like badges, license section, or example responses added.
```
