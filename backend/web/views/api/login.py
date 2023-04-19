from flask import request, make_response
from flask_restful import Resource
from flask import Response
from web.models import User

class Login(Resource):

    @staticmethod
    def post() -> Response:
        """
        POST response method for logging in a user.
        :return: JSON object
        """
        data = request.get_json()
        response, status = User.login(request, data)
        return make_response(response, status)
