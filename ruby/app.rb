require 'sinatra'
require 'dotenv/load'
require 'json'
require 'httparty'

# Configuration
set :bind, '0.0.0.0'
set :port, ENV.fetch('PORT', 4567)

# Constants
GATEWAY_URL = 'https://cert.api2.heartlandportico.com/Hps.Exchange.PosGateway/PosGatewayService.asmx'

helpers do
  def sanitize_postal_code(code)
    return '' if code.nil?
    code.gsub(/[^a-zA-Z0-9-]/, '')[0, 10]
  end

# https://github.com/hps/heartland-example-code/blob/master/perl/PorticoExample.pm - Portico XML namespaces

  def build_transaction_xml(token, amount, postal_code)
    <<~XML
      <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:hps="http://Hps.Exchange.PosGateway">
        <soapenv:Header/>
        <soapenv:Body>
          <hps:PosRequest clientType="" clientVer="">
            <hps:Ver1.0>
              <hps:Header>
                <hps:SecretAPIKey>#{ENV['SECRET_API_KEY']}</hps:SecretAPIKey>
              </hps:Header>
              <hps:Transaction>
                <hps:CreditSale>
                  <hps:Block1>
                    <hps:CardData>
                      <hps:TokenData>
                        <hps:TokenValue>#{token}</hps:TokenValue>
                      </hps:TokenData>
                    </hps:CardData>
                    <hps:Amt>#{'%.2f' % amount}</hps:Amt>
                    <hps:CardHolderData>
                      <hps:CardHolderZip>#{postal_code}</hps:CardHolderZip>
                    </hps:CardHolderData>
                    <hps:AllowDup>Y</hps:AllowDup>
                  </hps:Block1>
                </hps:CreditSale>
              </hps:Transaction>
            </hps:Ver1.0>
          </hps:PosRequest>
        </soapenv:Body>
      </soapenv:Envelope>
    XML
  end
end

get '/' do
  erb :index
end

get '/config' do
  content_type :json
  {
    success: true,
    data: { publicApiKey: ENV['PUBLIC_API_KEY'] }
  }.to_json
end

post '/process-payment' do
  content_type :json

  begin
    token = params['payment_token']
    billing_zip = params['billing_zip']
    amount = params['amount'].to_f

    raise 'Missing required fields' if token.nil? || billing_zip.nil? || amount <= 0

    xml_request = build_transaction_xml(token, amount, sanitize_postal_code(billing_zip))

    response = HTTParty.post(GATEWAY_URL,
      headers: { 
        'Content-Type' => 'text/xml; charset=utf-8',
        'SOAPAction' => ''
      },
      body: xml_request
    )

    if response.success?
      # Quick check for success XML response
      if response.body.include?('<RspCode>00</RspCode>')
        transaction_id = response.body.match(/<GatewayTxnId>([^<]+)<\/GatewayTxnId>/)&.[](1)
        {
          success: true,
          message: "Payment successful! Transaction ID: #{transaction_id}",
          data: { transactionId: transaction_id }
        }.to_json
      else
        halt 400, {
          success: false,
          message: 'Payment processing failed',
          error: {
            code: 'GATEWAY_ERROR',
            details: response.body
          }
        }.to_json
      end
    else
      halt 500, {
        success: false,
        message: 'Gateway error',
        error: {
          code: 'HTTP_ERROR',
          details: response.body
        }
      }.to_json
    end
  rescue => e
    halt 400, {
      success: false,
      message: 'Payment processing failed',
      error: {
        code: 'API_ERROR',
        details: e.message
      }
    }.to_json
  end
end
