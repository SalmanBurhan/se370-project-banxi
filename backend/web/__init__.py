import flask, flask_restful, flask_sqlalchemy, flask_jwt_extended, flask_bcrypt
from web import plaid_config

app = flask.Flask(__name__)
app.config.from_pyfile("config.flask.py")
from web.utils import error_handers

rest_api = flask_restful.Api(app)
db = flask_sqlalchemy.SQLAlchemy(app)
bcrypt = flask_bcrypt.Bcrypt(app)
jwt = flask_jwt_extended.JWTManager(app)
plaid_client = plaid_config.create_plaid_client()

with app.app_context():
    db.create_all()
    db.session.commit()  # <- Here commit changes to database


def register_url(view, endpoint, url, methods=["GET"]):
    view_func = view.as_view(endpoint)
    app.add_url_rule(url, view_func=view_func, methods=methods)


from web.views import *

rest_api.add_resource(APIViews.SignUp, "/banxi/api/auth/signup")
rest_api.add_resource(APIViews.Login, "/banxi/api/auth/login")
rest_api.add_resource(APIViews.RefreshToken, "/banxi/api/auth/refresh")
rest_api.add_resource(APIViews.Institutions, "/banxi/api/user/institutions")
rest_api.add_resource(APIViews.Accounts, "/banxi/api/user/accounts")
rest_api.add_resource(APIViews.Transactions, "/banxi/api/user/transactions")
rest_api.add_resource(
    APIViews.TransactionCategories, "/banxi/api/transactions/categories"
)

register_url(
    WebViews.PlaidLink.Link,
    "link",
    "/banxi/link",
    methods=[
        "GET",
    ],
)
register_url(
    WebViews.PlaidLink.Login,
    "link_login",
    "/banxi/link/login",
    methods=[
        "GET",
        "POST",
    ],
)
register_url(
    WebViews.PlaidLink.LinkToken,
    "link_create",
    "/banxi/link/create",
    methods=[
        "POST",
    ],
)
register_url(
    WebViews.PlaidLink.TokenExchange,
    "link_exchange",
    "/banxi/link/exchange",
    methods=[
        "POST",
    ],
)
