from flask.views import MethodView
from flask import make_response, render_template, redirect, url_for, request, Response
from flask_jwt_extended import jwt_required, get_jwt_identity

class Link(MethodView):

    @jwt_required(optional=True, locations='cookies')
    def get(self) -> Response:
        if (client_id := get_jwt_identity()):
            response = make_response(render_template('link.html', client_id=client_id), 200)
            response.headers['Content-Type'] = 'text/html'
            return response
        else:
            return redirect(url_for('link_login'))