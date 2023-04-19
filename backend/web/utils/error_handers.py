from werkzeug.exceptions import HTTPException
from flask import jsonify
from web import app

# @app.errorhandler(HTTPException)
# def handle_exception(e):
#     return jsonify({
#         'message': e.description
#     }), e.code
