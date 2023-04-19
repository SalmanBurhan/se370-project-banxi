from flask_jwt_extended import jwt_required, current_user
from flask import request, make_response
from flask_restful import Resource
from flask import Response
from web.models import User

class RefreshToken(Resource):

    @jwt_required(refresh=True)
    def post(self) -> Response:
        """
        POST response method for refreshing a user token.
        :return: JSON object
        """
        response, status = current_user.refresh_token()
        return make_response(response, status)
