from web import plaid_client, plaid_config, db

from flask_jwt_extended import jwt_required, get_jwt_identity, current_user
from flask_restful import Resource
from flask import jsonify, request, Response
from flask.views import MethodView

from plaid.model.item_public_token_exchange_request import (
    ItemPublicTokenExchangeRequest,
)
from web.models.plaid_item import PlaidItem


class TokenExchange(MethodView):
    @jwt_required(locations="cookies")
    def post(self) -> Response:
        data = request.get_json()
        public_token = data.get("public_token")

        exchange_request = ItemPublicTokenExchangeRequest(public_token=public_token)
        exchange_response = plaid_client.item_public_token_exchange(exchange_request)

        # print(f'Client ID : {client_id} | Access Token : {access_token}')
        access_token = exchange_response["access_token"]
        item_id = exchange_response["item_id"]

        db.session.add(
            PlaidItem(item_id=item_id, access_token=access_token, user=current_user)
        )
        db.session.commit()

        return "", 201
