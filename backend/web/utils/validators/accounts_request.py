from marshmallow import Schema, fields, EXCLUDE


class AccountsRequestSchema(Schema):

    """

    The schema used to validate JSON body data for a request to list
    user accounts associated with an institution.

    Attributes:

        institution_id (str):
            The financial institution to which the account belongs.

    """

    class Meta:
        unknown = EXCLUDE

    institution_id = fields.String(required=True)
