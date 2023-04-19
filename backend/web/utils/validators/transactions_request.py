from marshmallow import Schema, fields, validate, EXCLUDE
import datetime


class TransactionsRequestSchema(Schema):

    """

    The schema used to validate JSON body data for a request to list
    transactions in an account.

    Attributes:

        account_id (str):
            The account for which transactions are being requested.

        institution_id (str):
            The financial institution to which the account belongs.

        start_date (:obj:`datetime.date`):
            The earliest date for which data should be returned.

        end_date (:obj:`datetime.date`, default=:func:``datetime.date.today()``):
            The latest date for which data should be returned.

        count (int, optional, default=`100`, min=`1`, max=`500`):
            The number of transactions to fetch.

        offset (int, optional, default=`0`): the number of transactions to skip.

    """

    class Meta:
        unknown = EXCLUDE

    account_id = fields.String(required=True)
    institution_id = fields.String(required=True)

    start_date = fields.Date(
        format="%Y-%m-%d",
        required=True,
        validate=validate.Range(max=datetime.date.today()),
    )

    end_date = fields.Date(
        format="%Y-%m-%d",
        validate=validate.Range(max=datetime.date.today()),
        load_default=datetime.date.today(),
    )

    count = fields.Int(validate=validate.Range(min=1, max=500), load_default=100)

    offset = fields.Int(validate=validate.Range(min=1), load_default=0)
