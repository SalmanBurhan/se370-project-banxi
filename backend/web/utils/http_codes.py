def is_informational(code): return 100 <= code <= 199
def is_success(code): return 200 <= code <= 299
def is_redirect(code): return 300 <= code <= 399
def is_client_error(code): return 400 <= code <= 499
def is_server_error(code): return 500 <= code <= 599

HTTP_200_OK                         = 200
HTTP_201_CREATED                    = 201
HTTP_400_BAD_REQUEST                = 400
HTTP_401_UNAUTHORIZED               = 401
HTTP_403_FORBIDDEN                  = 403
HTTP_404_NOT_FOUND                  = 404
HTTP_405_METHOD_NOT_ALLOWED         = 405
HTTP_429_TOO_MANY_REQUESTS          = 429
HTTP_503_SERVICE_UNAVAILABLE        = 503
HTTP_501_NOT_IMPLEMENTED            = 501
HTTP_500_INTERNAL_SERVER_ERROR      = 500
