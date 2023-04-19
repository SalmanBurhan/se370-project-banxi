from plaid.model.transactions_get_request import TransactionsGetRequest
from plaid.model.transactions_get_request_options import TransactionsGetRequestOptions

from flask_jwt_extended import jwt_required, current_user
from web.utils.common import generate_response
from flask import make_response, request
from flask_restful import Resource
from web import plaid_client
from web.utils.validators import TransactionsRequestSchema
from marshmallow import ValidationError


class Transactions(Resource):
    @jwt_required()
    def post(self):
        try:
            schema = TransactionsRequestSchema().load(request.get_json())
            plaid_item = next(
                filter(
                    lambda o: o.item_id == schema["institution_id"],
                    current_user.plaid_items,
                )
            )
            plaid_response = plaid_client.transactions_get(
                TransactionsGetRequest(
                    access_token=plaid_item.access_token,
                    start_date=schema["start_date"],
                    end_date=schema["end_date"],
                    options=TransactionsGetRequestOptions(
                        account_ids=[schema["account_id"]],
                        count=schema["count"],
                        offset=schema["offset"],
                    ),
                )
            ).to_dict()

            return make_response(
                generate_response(data=plaid_response.get("transactions"), status=200)
            )
        except ValidationError as validation_error:
            return make_response(
                generate_response(data=validation_error.messages, status=400)
            )
