from flask.views import MethodView
from web.models import PlaidItem # Not Needed Here, Just Here In Order For DB Models To Function
from flask import make_response, render_template, redirect, url_for, request, Response
from web.models import User
from web.utils.http_codes import HTTP_201_CREATED, HTTP_401_UNAUTHORIZED
from flask_jwt_extended import jwt_required, set_access_cookies, get_jwt_identity

class LinkLogin(MethodView):

    @jwt_required(optional=True, locations='cookies')
    def get(self) -> Response:
        if get_jwt_identity(): return redirect(url_for('link'))
        else: response = make_response(render_template('login.html'))
        response.headers['Content-Type'] = 'text/html'
        return response
    
    def post(self) -> Response:
        login_response, status = User.login(request, request.form)
        if status == HTTP_201_CREATED:
            access_token = login_response['data']['access_token']
            response = make_response(redirect(url_for('link_login')))
            set_access_cookies(response, access_token)
            return response
        else:
            response = make_response(render_template('login.html', login_failed=True), 401)
            response.headers['Content-Type'] = 'text/html'
            return response