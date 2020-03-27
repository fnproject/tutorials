const fdk=require('@fnproject/fdk');

/* 
    The following is a list of data items available at runtime
    config - Current environment variables.
    headers - HTTP Headers.
    deadline - How soon the function will be ended.
    callID - ID assigned to the request.
    fnID - ID assigned to function.
    appID - ID Assigned to application.
    memory - Amount of memory assigned to this function
    contentType - Incoming request content type
*/


fdk.handle(function(input, ctx){
  
  console.log('\nReturn all vars')
  return {

            "ctx.config": ctx.config,
            "ctx.headers": ctx.headers,
            "ctx.deadline": ctx.deadline,
            "ctx.callID": ctx.callID,
            "ctx.fnID": ctx.fnID,
            "ctx.appID": ctx.appID,
            "ctx.memory": ctx.memory,
            "ctx.contextType": ctx.contentType
            
         }
})




