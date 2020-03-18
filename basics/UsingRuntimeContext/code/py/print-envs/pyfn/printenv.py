import io
import json
import logging

from fdk import response

def handler(ctx, data: io.BytesIO=None):
    try:
        evdict = dict(ctx.Config())

    except (Exception, ValueError) as ex:
        logging.getLogger().info('error parsing env payload: ' + str(ex))

    logging.getLogger().info("print all env vars...")
    return response.Response(
        ctx, response_data=json.dumps(
            evdict, sort_keys=True, indent=4),
        headers={"Content-Type": "application/json"}
    )
