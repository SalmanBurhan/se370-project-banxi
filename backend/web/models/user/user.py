from web.utils.http_codes import HTTP_400_BAD_REQUEST, HTTP_401_UNAUTHORIZED, HTTP_201_CREATED
from web.models.user.validation import UserSignUpSchema, UserLoginSchema
from web.utils.common import generate_response
from flask_jwt_extended import create_access_token, create_refresh_token
from datetime import datetime, timedelta
from web import db, jwt, bcrypt
from uuid import uuid4
from web import app

@jwt.user_identity_loader
def user_identity_lookup(user):
    return user.client_id

@jwt.user_lookup_loader
def user_lookup_callback(_jwt_header, jwt_data):
    client_id = jwt_data.get('sub')
    return User.query.filter_by(client_id=client_id).one_or_none()

class User(db.Model):

    id = db.Column(db.Integer, primary_key = True)
    client_id = db.Column(db.String(50), unique = True, default=str(uuid4()), nullable=False)
    first_name = db.Column(db.String(50), nullable=False)
    last_name = db.Column(db.String(50), nullable=False)
    email = db.Column(db.String(100), unique = True, nullable=False)
    password = db.Column(db.String(500), nullable=False)
    created = db.Column(db.DateTime, default=datetime.utcnow, nullable=True)
    plaid_items = db.relationship('PlaidItem', backref='user', lazy=True)

    def __init__(self, **kwargs):
        self.first_name = kwargs.get('first_name')
        self.last_name = kwargs.get('last_name')
        self.email = kwargs.get('email')
        self.password = kwargs.get('password')

    def __repr__(self) -> str:
        return '<User {}>'.format(self.email)
    
    def hash_password(self):
        self.password = bcrypt.generate_password_hash(self.password).decode("utf8")
    
    def check_password(self, password):
        return bcrypt.check_password_hash(self.password, password)
    
    def refresh_token(self):
        access_token = create_access_token(identity=self)
        return generate_response(data={
            'access_token': access_token,
            'access_token_expires_in': int(app.config.get('JWT_ACCESS_TOKEN_EXPIRES').total_seconds())
        }, status=HTTP_201_CREATED)  
              
    @staticmethod
    def create_user(request, data):
        schema = UserSignUpSchema()
        
        if (errors := schema.validate(data)):
            return generate_response(message=errors, status=HTTP_400_BAD_REQUEST)
        if User.query.filter_by(email=data.get('email')).first():
            return generate_response(message="Email Address Already Registered", status=HTTP_400_BAD_REQUEST)
        
        user = User(**data)
        user.hash_password()
        db.session.add(user)
        db.session.commit()
        
        del data['password']
        return generate_response(message="User Created", status=HTTP_201_CREATED)
    
    @staticmethod
    def login(request, data):
        schema = UserLoginSchema()
        
        if (errors := schema.validate(data)):
            return generate_response(message=errors, status=HTTP_400_BAD_REQUEST)
        if not (user := db.session.execute(db.select(User).filter_by(email=data.get('email'))).scalar_one()):
            return generate_response(message='User Does Not Exist', status=HTTP_400_BAD_REQUEST)
        if not user.check_password(data.get('password')):
            return generate_response(message='Incorrect Username or Password', status=HTTP_401_UNAUTHORIZED)
        
        access_token = create_access_token(identity=user)
        refresh_token = create_refresh_token(identity=user)

        return generate_response(data={
            'client_id': user.client_id,
            'email': user.email,
            'first_name': user.first_name,
            'last_name': user.last_name,
            'refresh_token': refresh_token,
            'refresh_token_expires_in': int(app.config.get('JWT_REFRESH_TOKEN_EXPIRES').total_seconds()),
            'access_token': access_token,
            'access_token_expires_in': int(app.config.get('JWT_ACCESS_TOKEN_EXPIRES').total_seconds())
        }, status=HTTP_201_CREATED)        