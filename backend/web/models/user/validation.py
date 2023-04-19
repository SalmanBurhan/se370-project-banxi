from marshmallow import Schema, validates, ValidationError
from marshmallow import fields as Field
from marshmallow.validate import Length
import string

class UserSignUpSchema(Schema):
    
    first_name = Field.Str(required=True, validate=Length(min=2, max=50))
    last_name = Field.Str(required=True, validate=Length(min=2, max=50))
    email = Field.Email(required=True)
    password = Field.Str(required=True)

    @validates('first_name')
    def validate_first_name(self, value):
        if len([c for c in string.digits if c in value]) > 0:
            raise ValidationError('First Name May Not Contain Numeric Characters')
        if len([c for c in string.punctuation if c in value and c not in ['\'', '-']]) > 0:
            raise ValidationError('First Name May Not Contain Special Characters')

    @validates('last_name')
    def validate_first_name(self, value):
        if len([c for c in string.digits if c in value]) > 0:
            raise ValidationError('Last Name May Not Contain Numeric Characters')
        if len([c for c in string.punctuation if c in value and c not in ['\'', '-']]) > 0:
            raise ValidationError('Last Name May Not Contain Special Characters')

    @validates('password')
    def validate_password(self, value):
        length_range = range(8,17) # Min=8, Max=16
        if len(value) < 8:
            raise ValidationError('Password Must Be At Least 8 Characters')
        if len(value) > 17:
            raise ValidationError('Password Must Be At Most 16 Characters')
        if len([c for c in string.punctuation if c in value]) < 1:
            raise ValidationError('Password Must Contain At Least One Special Character')
        if len([c for c in string.ascii_uppercase if c in value]) < 1:
            raise ValidationError('Password Must Contain At Least One Uppercase Letter')
        if len([c for c in string.ascii_lowercase if c in value]) < 1:
            raise ValidationError('Password Must Contain At Least One Lowercase Letter')
        if len([c for c in string.digits if c in value]) < 1:
            raise ValidationError('Password Must Contain At Least One Numeric Character')

class UserLoginSchema(Schema):

    email = Field.Email(required=True)
    password = Field.Str(required=True, validate=Length(min=8, max=16))