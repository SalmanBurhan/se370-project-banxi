from web import plaid_client, plaid_config

from flask_jwt_extended import jwt_required, current_user
from flask_restful import Resource
from flask import jsonify, request, Response
from flask.views import MethodView

from plaid.model.link_token_create_request_user import LinkTokenCreateRequestUser
from plaid.model.link_token_create_request import LinkTokenCreateRequest
from plaid.model.country_code import CountryCode
from web.models import User
from typing import NamedTuple


class LinkTokenSettings(NamedTuple):
    request: LinkTokenCreateRequest
    mode_update: bool


class LinkToken(MethodView):
    @jwt_required(locations="cookies")
    def post(self) -> Response:
        data = request.get_json()
        request_settings = LinkToken.create_link_token_request(current_user, **data)
        response = plaid_client.link_token_create(request_settings.request).to_dict()
        response["mode_update"] = request_settings.mode_update
        return jsonify(response)

    @staticmethod
    def create_link_token_request(
        current_user: User, item_id: str = None
    ) -> LinkTokenSettings:
        request = LinkTokenCreateRequest(
            client_name="Banxi",
            country_codes=list(
                map(lambda x: CountryCode(x), plaid_config.PLAID_COUNTRY_CODES)
            ),
            language="en",
            user=LinkTokenCreateRequestUser(client_user_id=current_user.client_id),
        )

        if plaid_config.PLAID_REDIRECT_URI is not None:
            request["redirect_uri"] = plaid_config.PLAID_REDIRECT_URI

        # Launching Link in Update Mode
        if plaid_item := db.session.execute(
            db.select(PlaidItem).filter_by(item_id=item_id, user=current_user)
        ).scalar():
            request["access_token"] = plaid_item.access_token
            mode_update = True
        else:
            request["products"] = plaid_config.PLAID_PRODUCTS
            mode_update = False

        return LinkTokenSettings(request, mode_update)
