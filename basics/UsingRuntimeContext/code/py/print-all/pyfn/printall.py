import io
import json
import logging

from fdk import response

# The following is a list of data items available at runtime
# Config [os._Environ] - Current environment variables.
# Headers [dict] - HTTP Headers.
# AppID [str] - ID Assigned to application.
# FnID [str] - ID assigned to function.
# CallID [str] - ID assigned to the request.
# Format [str] - The function's communication format.
# Deadline [str] - How soon the function will be ended.
# Method [str] HTTP method used for the request.
# Request [str] - URL Used to invoke the function.

def handler(ctx, data: io.BytesIO=None):

    logging.getLogger().info("print all fn vars...")
    return response.Response(
        ctx, response_data=json.dumps(
            {   "ctx.Config": dict(ctx.Config()),
                "ctx.Headers": ctx.Headers(),
                "ctx.AppID": ctx.AppID(),
                "ctx.FnID": ctx.FnID(),
                "ctx.CallID": ctx.CallID(),
                "ctx.Format": ctx.Format(),
                "ctx.Deadline": ctx.Deadline(),
                "ctx.RequestURL": ctx.RequestURL(),
                "ctx.Method": ctx.Method()
            },
            sort_keys=True, indent=4),
        headers={"Content-Type": "application/json"}
    )
