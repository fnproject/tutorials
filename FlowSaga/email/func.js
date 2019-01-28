const fdk=require('@fnproject/fdk');
const request = require('request');

function initializePromise() {
    var options = {
        url: process.env.EMAIL_API_URL,
    };
    return new Promise(function(resolve, reject) {
        request.post(options, function(err, resp, body) {
            if (err) {
                reject(err);
            } else {
                resolve(JSON.parse(body));
            }
        })
    })

}


fdk.handle(function(input){

   return initializePromise().then(function(result) {
      return result;
   }, function(err) {
      console.log(err);
      return (JSON.stringify({ "status": "remote email error!"}))
   })

})

