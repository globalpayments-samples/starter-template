"""
Global Payments SDK Template - Python Flask

This Flask application provides a starting template for Global Payments SDK integration.
Customize the endpoints and logic below for your specific use case.
"""

import math
import os
import re
from flask import Flask, request, jsonify, send_from_directory
from dotenv import load_dotenv
from globalpayments.api import PorticoConfig, ServicesContainer
from globalpayments.api.payment_methods import CreditCardData
from globalpayments.api.entities import Address
from globalpayments.api.entities.exceptions import ApiException

# Load environment variables
load_dotenv()

# Initialize application
# Serve only the frontend files, never the server's own files
FRONTEND_DIR = os.path.dirname(os.path.abspath(__file__))
app = Flask(__name__, static_folder=None)

def configure_sdk():
    """
    Configure the Global Payments SDK with necessary credentials and settings.
    Customize these settings for your environment.
    """
    config = PorticoConfig()
    config.secret_api_key = os.getenv('SECRET_API_KEY')
    config.service_url = 'https://cert.api2.heartlandportico.com'  # Use production URL for live transactions
    config.developer_id = '000000'  # Your developer ID
    config.version_number = '0000'  # Your version number
    
    ServicesContainer.configure(config)

# Configure SDK on startup
configure_sdk()

def sanitize_postal_code(postal_code: str) -> str:
    """
    Utility function to sanitize postal code.
    Customize validation logic as needed for your use case.
    """
    sanitized = re.sub(r'[^a-zA-Z0-9-]', '', postal_code or '')
    return sanitized[:10]

@app.route('/')
@app.route('/index.html')
def index():
    """Serve the main HTML page."""
    return send_from_directory(FRONTEND_DIR, 'index.html')

@app.route('/config')
def get_config():
    """
    Config endpoint - provides public API key for client-side use.
    Customize response data as needed.
    """
    return jsonify({
        'success': True,
        'data': {
            'publicApiKey': os.getenv('PUBLIC_API_KEY')
            # Add other configuration data as needed
        }
    })

def payment_error(code, details, status):
    """Build the error response for a failed payment request."""
    return jsonify({
        'success': False,
        'message': 'Payment processing failed',
        'error': {'code': code, 'details': details}
    }), status

@app.route('/process-payment', methods=['POST'])
def process_payment():
    """
    Example payment processing endpoint.
    Customize this endpoint for your specific payment flow.
    """
    # TODO: Add your payment processing logic here
    # Example implementation for basic charge:
    payment_token = request.form.get('payment_token')
    billing_zip = request.form.get('billing_zip')
    amount_str = request.form.get('amount')
    if not payment_token or not billing_zip or not amount_str:
        return payment_error('VALIDATION_ERROR', 'Missing required fields', 400)

    # Customize amount and other parameters as needed
    try:
        amount = round(float(amount_str), 2)
    except ValueError:
        amount = 0
    if not math.isfinite(amount) or amount <= 0:
        return payment_error('VALIDATION_ERROR', 'Amount must be a positive number', 400)

    try:
        card = CreditCardData()
        card.token = payment_token

        # Add billing address for AVS verification
        address = Address()
        address.postal_code = sanitize_postal_code(billing_zip)

        response = card.charge(amount)\
            .with_allow_duplicates(True)\
            .with_currency('USD')\
            .with_address(address)\
            .execute()

        # A declined card comes back as a normal response, not an exception
        if response.response_code != '00':
            return payment_error('PAYMENT_DECLINED', response.response_message, 400)

        return jsonify({
            'success': True,
            'message': f'Payment successful! Transaction ID: {response.transaction_id}',
            'data': {'transactionId': response.transaction_id}
        })

    except ApiException as e:
        # Gateway errors are passed back so the cause of the failure is visible
        return payment_error('API_ERROR', str(e), 400)
    except Exception:
        # Log unexpected errors on the server and return a generic message
        app.logger.exception('Unexpected error while processing payment')
        return payment_error('SERVER_ERROR', 'An unexpected error occurred', 500)

# Add your custom endpoints here
# Examples:
# @app.route('/authorize', methods=['POST'])
# def authorize_payment():
#     # Authorization only logic
#     pass
#
# @app.route('/capture', methods=['POST'])  
# def capture_payment():
#     # Capture authorized payment logic
#     pass
#
# @app.route('/refund', methods=['POST'])
# def refund_payment():
#     # Process refund logic
#     pass
#
# @app.route('/transaction/<transaction_id>')
# def get_transaction(transaction_id):
#     # Get transaction details logic
#     pass

# Start the server if this file is run directly
if __name__ == '__main__':
    port = int(os.getenv('PORT', 8000))
    print(f"Server running at http://localhost:{port}")
    print("Customize this template for your use case!")
    app.run(host='0.0.0.0', port=port, debug=os.environ.get("FLASK_DEBUG") == "1")