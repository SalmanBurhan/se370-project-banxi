from plaid.model.accounts_get_request import AccountsGetRequest

from flask_jwt_extended import jwt_required, current_user
from flask import request, make_response
from flask_restful import Resource
from flask import Response
from web.utils.common import generate_response
from web import plaid_client
from web.utils.validators import AccountsRequestSchema
from marshmallow import ValidationError
from web import plaid_client


class Accounts(Resource):
    @jwt_required()
    def post(self) -> Response:
        try:
            schema = AccountsRequestSchema().load(request.get_json())
            plaid_item = next(
                filter(
                    lambda o: o.item_id == schema["institution_id"],
                    current_user.plaid_items,
                )
            )
            plaid_response = plaid_client.accounts_get(
                AccountsGetRequest(access_token=plaid_item.access_token)
            )
            accounts = []
            for account in plaid_response.accounts:
                setattr(account, "institution_id", plaid_item.item_id)
                accounts.append(account.to_dict())

            return make_response(generate_response(data=accounts, status=200))
        except ValidationError as validation_error:
            return make_response(
                generate_response(data=validation_error.messages, status=400)
            )
