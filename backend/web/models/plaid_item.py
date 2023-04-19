from web import db

class PlaidItem(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    item_id = db.Column(db.String(100), unique=True)
    access_token = db.Column(db.String(100), unique=True)
    client_id = db.Column(db.String, db.ForeignKey('user.client_id'), nullable=False)

    def __init__(self, **kwargs):
        self.item_id = kwargs.get('item_id')
        self.access_token = kwargs.get('access_token')
        self.client_id = kwargs.get('client_id')
    
    def __repr__(self) -> str:
        return f'<PlaidItem {self.item_id}>'