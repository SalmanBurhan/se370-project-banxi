import os, plaid, plaid.api.plaid_api, plaid.model.products, dotenv

dotenv.load_dotenv()

def empty_to_none(field):
    return None if (not (value := os.getenv(field)) or len(value) == 0) else value

# PLAID CONSTANTS & SECRETS
PLAID_CLIENT_ID =       os.getenv('PLAID_CLIENT_ID')
PLAID_SECRET =          os.getenv('PLAID_SECRET')
PLAID_ENV =             os.getenv('PLAID_ENV', 'sandbox')
PLAID_COUNTRY_CODES =   os.getenv('PLAID_COUNTRY_CODES', 'US').split(',')
PLAID_REDIRECT_URI =    empty_to_none('PLAID_REDIRECT_URI')
PLAID_HOST =            {'sandbox': plaid.Environment.Sandbox,
                         'development': plaid.Environment.Development,
                         'production': plaid.Environment.Production}.get(PLAID_ENV)
PLAID_PRODUCTS =        [plaid.model.products.Products(product)
                         for product in
                         os.getenv('PLAID_PRODUCTS', 'transactions').split(',')]

def create_plaid_client():
    configuration = plaid.Configuration(host = PLAID_HOST, api_key = {
        'clientId': PLAID_CLIENT_ID,
        'secret': PLAID_SECRET,
        'plaidVersion': '2020-09-14'
    })
    api_client = plaid.ApiClient(configuration)
    client = plaid.api.plaid_api.PlaidApi(api_client)
    return client