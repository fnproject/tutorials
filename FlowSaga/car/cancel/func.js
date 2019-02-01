const fdk=require('@fnproject/fdk');
const request = require('request');


fdk.handle(function(input){

   var options = {
       uri: process.env.CAR_API_URL
   };

   return new Promise(function(resolve, reject) {
       request.delete(options, function(err, resp, body) {
          if (!err && resp.statusCode === 200) {
             resolve(JSON.parse(body));
           } else {
             reject("car-cancel error, unable to cancel car booking!!"); 
           }
       })
   })

})