from plaid.model.institutions_get_by_id_request_options import (
    InstitutionsGetByIdRequestOptions,
)
from plaid.model.institutions_get_by_id_request import (
    InstitutionsGetByIdRequest,
)
from plaid.model.item_get_request import ItemGetRequest
from plaid.model.country_code import CountryCode

from flask_jwt_extended import jwt_required, current_user
from flask import make_response
from flask_restful import Resource
from flask import Response
from web.utils.common import generate_response
from web import plaid_client, plaid_config


class Institutions(Resource):
    @jwt_required()
    def get(self) -> Response:
        institutions = []
        for item in current_user.plaid_items:
            plaid_item_response = plaid_client.item_get(
                ItemGetRequest(access_token=item.access_token)
            )
            plaid_institution_response = plaid_client.institutions_get_by_id(
                InstitutionsGetByIdRequest(
                    institution_id=plaid_item_response.item.institution_id,
                    country_codes=list(
                        map(lambda x: CountryCode(x), plaid_config.PLAID_COUNTRY_CODES)
                    ),
                    options=InstitutionsGetByIdRequestOptions(
                        include_optional_metadata=True
                    ),
                )
            )
            plaid_institution_response.institution.institution_id = item.item_id
            institutions.append(plaid_institution_response.institution.to_dict())
        return make_response(generate_response(data=institutions, status=200))
