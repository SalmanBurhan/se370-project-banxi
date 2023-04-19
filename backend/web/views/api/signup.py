from flask import request, make_response
from flask_restful import Resource
from flask import Response
from web.models import User

class SignUp(Resource):

    @staticmethod
    def post() -> Response:
        """
        POST response method for creating a new user.
        :return: JSON object
        """
        data = request.get_json()
        response, status = User.create_user(request, data)
        return make_response(response, status)
