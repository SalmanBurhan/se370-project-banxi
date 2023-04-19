import os, dotenv
from datetime import timedelta

dotenv.load_dotenv()

# Flask Constants & Secrets
SECRET_KEY                          = os.getenv('FLASK_SECRET_KEY')
''' The secret key used to encode and decode JWTs when using a symmetric signing algorithm. '''

SQLALCHEMY_DATABASE_URI             = os.getenv('FLASK_SQLALCHEMY_DATABASE_URI')
''' The filepath of the SQLite3 database file which will be used to handle permanent storage. '''

SQLALCHEMY_TRACK_MODIFICATIONS      = os.getenv('FLASK_SQLALCHEMY_TRACK_MODIFICATIONS')
''' If set to True, Flask-SQLAlchemy will track modifications of objects and emit signals.
    The default is None, which enables tracking but issues a warning that it will be disabled
    by default in the future. This requires extra memory and should be disabled if not needed. '''

JWT_ACCESS_TOKEN_EXPIRES            = timedelta(hours = float(os.getenv('JWT_ACCESS_TOKEN_EXPIRES_HOURS', 0.5)))
''' The number of hours after which a JWT access token expires. default = 30 minutes. '''

JWT_REFRESH_TOKEN_EXPIRES           = timedelta(days = float(os.getenv('JWT_REFRESH_TOKEN_EXPIRES_DAYS', 7)))
''' The number of hours after which a JWT refresh token expires. default = 1 week. '''

# SESSION_PERMANENT =                 os.getenv('FLASK_SESSION_PERMANENT')
# SESSION_TYPE =                      os.getenv('FLASK_SESSION_TYPE')