# Android Global Payments Integration# Android Card Payment Example



A production-ready Android application that integrates with Global Payments using a client/server architecture.This example demonstrates card payment processing using Android and the Global Payments SDK with hosted fields for secure tokenization.



## Architecture## Requirements



This implementation uses the **recommended production approach**:- Android Studio Arctic Fox (2020.3.1) or later

- Android SDK API level 24 (Android 7.0) or higher  

- **Android App**: Handles UI, user input, and secure communication with backend- Java JDK 17 or later (compatible with Java 21)

- **Backend Server**: Processes payments using Global Payments SDK- Kotlin 1.9.20 or later

- **Communication**: HTTPS API calls between client and server- Global Payments account and API credentials



## Why This Architecture?## Project Structure



The Android SDK 1.0 has compatibility issues with current Global Payments infrastructure:```

- X-GP-Version header incompatibilityandroid/

- Missing PorticoConfig class (available in other SDKs)├── app/

- API endpoint connectivity issues│   ├── src/main/

│   │   ├── java/com/globalpayments/android/sample/

The client/server approach provides:│   │   │   ├── MainActivity.kt              # Main activity with hosted fields

- ✅ **Security**: Payment processing happens on secure backend│   │   │   ├── model/

- ✅ **PCI Compliance**: Android app only handles UI, never raw card data│   │   │   │   └── PaymentModels.kt         # Data models for requests/responses

- ✅ **Reliability**: Uses proven Global Payments SDK on backend│   │   │   ├── service/

- ✅ **Scalability**: Single backend can serve multiple mobile apps│   │   │   │   └── PaymentService.kt        # Payment processing service

│   │   │   └── utils/

## Backend Server│   │   │       └── ValidationUtils.kt       # Input validation utilities

│   │   ├── res/

This Android app requires a backend server for payment processing. The repository includes working implementations in:│   │   │   ├── layout/

- Node.js (recommended for quick setup)│   │   │   │   └── activity_main.xml        # Main activity layout

- Java│   │   │   ├── values/

- Python│   │   │   │   ├── strings.xml             # String resources

- PHP│   │   │   │   ├── colors.xml              # Color definitions

- Go│   │   │   │   └── themes.xml              # App themes and styles

- C#│   │   │   └── drawable/

│   │   │       └── card_background.xml     # Card background drawable

## Quick Start│   │   └── AndroidManifest.xml             # App manifest

│   ├── build.gradle                        # App-level Gradle configuration

### 1. Set up Backend Server│   └── proguard-rules.pro                  # ProGuard rules

├── build.gradle                            # Project-level Gradle configuration

```bash├── settings.gradle                         # Gradle settings

# Navigate to Node.js backend├── local.properties.sample                 # Environment variables template

cd ../nodejs├── run.sh                                  # Build and run script

└── README.md                              # This file

# Install dependencies```

npm install

## Setup

# Configure credentials in .env file

PUBLIC_API_KEY=your_public_key### 1. Environment Configuration

SECRET_API_KEY=your_secret_key

1. Copy `local.properties.sample` to `local.properties`

# Start server2. Update `local.properties` with your Global Payments credentials:

npm start   ```properties

```   PUBLIC_API_KEY=pk_test_your_public_key_here

   SECRET_API_KEY=sk_test_your_secret_key_here

### 2. Configure Android App   ```



```bash### 2. Build and Run

# Configure credentials in local.properties

SECRET_API_KEY=your_secret_key#### Using Android Studio

PUBLIC_API_KEY=your_public_key1. Open the project in Android Studio

```2. Sync the project with Gradle files

3. Connect an Android device or start an emulator

### 3. Build and Run4. Run the app using the Run button or `Ctrl+R` (Cmd+R on Mac)



```bash#### Using Command Line

# Build the app1. Make sure you have Android SDK and tools in your PATH

./gradlew assembleDebug2. Run the convenience script:

   ```bash

# Install on device/emulator   ./run.sh

./gradlew installDebug   ```

```   Or manually:

   ```bash

## Payment Flow   ./gradlew assembleDebug

   ./gradlew installDebug

1. **User Input**: User enters payment amount and billing information   ```

2. **Token Generation**: Hosted fields generate secure payment token

3. **API Call**: Android app sends payment request to backend server## Implementation Details

4. **Payment Processing**: Backend processes payment using Global Payments SDK

5. **Response**: Success/failure result returned to Android app### Hosted Fields Integration



## ConfigurationThe application uses Global Payments hosted fields for secure payment data collection:



### Global Payments Credentials- **WebView Integration**: Embeds hosted fields in a WebView for PCI compliance

- **JavaScript Bridge**: Uses `@JavascriptInterface` to communicate between native code and hosted fields

Configure your Global Payments credentials in `local.properties`:- **Tokenization**: Card data is tokenized client-side without touching your servers

- **Security**: No raw card data is handled by the application

```properties

SECRET_API_KEY=your_secret_api_key### Payment Processing Flow

PUBLIC_API_KEY=your_public_api_key

```1. **Form Display**: User sees amount input and billing zip field

2. **Hosted Fields Loading**: WebView loads the Global Payments hosted fields

### Backend Server URL3. **Card Data Entry**: User enters card information in secure hosted fields

4. **Token Generation**: Hosted fields generate a secure payment token

The app is configured to communicate with backend server at:5. **Payment Processing**: App uses token to process payment via Global Payments SDK

- **Emulator**: `http://10.0.2.2:8000` (maps to localhost:8000)6. **Result Display**: Success or error message shown to user

- **Device**: Update to your server's IP address

### SDK Configuration

## Security Features

The Global Payments SDK is configured in `PaymentService.kt`:

- **PCI Compliant**: No raw card data handled by Android app

- **HTTPS Communication**: Secure API calls to backend```kotlin

- **Server-Side Processing**: All sensitive operations on backendval config = PorticoConfig().apply {

- **Token-Based**: Uses secure tokens instead of card data    secretApiKey = BuildConfig.SECRET_API_KEY

    serviceUrl = BuildConfig.SERVICE_URL  // Sandbox or production URL

## API Integration    developerId = "000000"

    versionNumber = "0000"

### GP API Configuration}

```

The backend server uses GP API with CNP (Card-Not-Present) channel:

### Input Validation

```javascript

const config = new GpApiConfig();Comprehensive input validation is implemented:

config.appId = process.env.PUBLIC_API_KEY;

config.appKey = process.env.SECRET_API_KEY;- **Amount Validation**: Ensures positive numeric values

config.channel = 'CNP'; // Card-Not-Present for online/mobile- **Postal Code Sanitization**: Removes invalid characters, limits length

config.serviceUrl = 'https://apis.sandbox.globalpay.com/ucp';- **Real-time Feedback**: Shows validation errors immediately

```

### Error Handling

## Testing

The application implements robust error handling:

Use the test card number `4111111111111111` for testing payments.

- **API Errors**: Catches and displays Global Payments API errors

## Production Deployment- **Network Errors**: Handles connectivity issues gracefully  

- **Validation Errors**: Shows user-friendly validation messages

1. **Backend**: Deploy backend server to production environment- **Token Errors**: Handles hosted fields tokenization failures

2. **Android**: Update backend URL to production server

3. **Credentials**: Use production Global Payments credentials## API Integration

4. **Security**: Ensure HTTPS for all API communication

### Payment Processing

## Support

The app processes payments using the same pattern as other language implementations:

This implementation follows Global Payments best practices for mobile applications using the client/server architecture recommended for production environments.
```kotlin
suspend fun processPayment(request: PaymentRequest): PaymentResponse {
    val card = CreditCardData().apply {
        token = request.paymentToken
    }
    
    val chargeBuilder = card.charge(BigDecimal.valueOf(request.amount))
        .withAllowDuplicates(true)
        .withCurrency("USD")
    
    if (request.billingZip.isNotEmpty()) {
        val address = Address().apply {
            postalCode = ValidationUtils.sanitizePostalCode(request.billingZip)
        }
        chargeBuilder.withAddress(address)
    }
    
    val response = chargeBuilder.execute()
    // Handle response...
}
```

## Customization Options

### Adding New Payment Features

The template can be extended for various payment scenarios:

1. **Authorization/Capture**: Modify `PaymentService` to support auth-only transactions
2. **Refunds**: Add refund functionality using transaction references
3. **Recurring Payments**: Implement tokenization for stored payment methods
4. **Multiple Payment Methods**: Add support for ACH, digital wallets, etc.

### UI Customization

- **Themes**: Modify `themes.xml` for custom branding
- **Colors**: Update `colors.xml` with your brand colors
- **Layouts**: Customize `activity_main.xml` for different form layouts
- **Hosted Fields Styling**: Modify the CSS in `MainActivity.kt` hosted fields HTML

### Build Variants

The app supports different build variants:

- **Debug**: Uses sandbox environment with debugging enabled
- **Release**: Uses production environment with optimizations

## Security Considerations

### PCI Compliance

- **Hosted Fields**: No raw card data touches your application
- **Tokenization**: All card data is tokenized before processing
- **HTTPS**: All API communication uses HTTPS
- **Local Storage**: No sensitive data stored locally

### Production Deployment

For production use, consider:

1. **Certificate Pinning**: Pin SSL certificates for API endpoints
2. **Root Detection**: Detect and handle rooted devices
3. **Debugging Protection**: Disable debugging in release builds
4. **Code Obfuscation**: Use ProGuard/R8 for code protection
5. **API Key Security**: Consider using Android Keystore for API keys

## Testing

### Unit Tests

Run unit tests:
```bash
./gradlew test
```

### Integration Tests

Run instrumented tests:
```bash
./gradlew connectedAndroidTest
```

### Manual Testing

Test scenarios:
- Valid payment processing
- Invalid card data handling
- Network error scenarios
- Form validation
- Payment success/failure flows

## Troubleshooting

### Common Issues

1. **Build Failures**
   - Ensure Android SDK and build tools are installed
   - Check Gradle version compatibility
   - Verify internet connection for dependency downloads

2. **API Key Issues**
   - Verify `local.properties` file exists and contains valid keys
   - Check API key format (pk_test_ for public, sk_test_ for secret)
   - Ensure keys match your Global Payments environment

3. **Hosted Fields Not Loading**
   - Check internet connectivity
   - Verify public API key is correct
   - Check browser console in WebView for JavaScript errors

4. **Payment Processing Failures**
   - Verify secret API key configuration
   - Check SDK configuration matches your environment
   - Review payment request data format

### Debug Logging

Enable debug logging by setting log level in `PaymentService`:

```kotlin
Log.setLevel(Log.VERBOSE)
```

## Dependencies

- **Global Payments SDK**: Card payment processing
- **AndroidX**: Modern Android support libraries
- **Material Design**: UI components and theming
- **WebKit**: WebView for hosted fields integration
- **Kotlin Coroutines**: Asynchronous programming

## Support

For Global Payments SDK support:
- [Developer Documentation](https://developer.globalpay.com/)
- [API Reference](https://developer.globalpay.com/api)
- [Android SDK Documentation](https://developer.globalpay.com/sdks/android)

## License

This sample application is provided as-is for demonstration purposes.