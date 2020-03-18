import io
import json
import logging

from fdk import response

# Set DB_HOST_URL, DB_USER, and DB_PASSWORD in app or function

def handler(ctx, data: io.BytesIO=None):
    try:
        evdict = dict(ctx.Config())
        mydict = dict()
        mydict["DB_HOST_URL"] = evdict["DB_HOST_URL"]
        mydict["DB_USER"] = evdict["DB_USER"]
        mydict["DB_PASSWD"] = evdict["DB_PASSWD"]

    except (Exception, ValueError) as ex:
        logging.getLogger().info('error parsing json payload: ' + str(ex))

    logging.getLogger().info("print three env vars...")
    return response.Response(
        ctx, response_data=json.dumps(
            mydict, sort_keys=True, indent=4),
        headers={"Content-Type": "application/json"}
    )
