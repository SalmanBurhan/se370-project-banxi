from flask_restful import Resource
from flask import Response
from web import plaid_client


class TransactionCategories(Resource):
    @staticmethod
    def get() -> Response:
        plaid_response = plaid_client.categories_get({})
        return list(map(lambda category: category.to_dict(), plaid_response.categories))
