const fdk=require('@fnproject/fdk');
const request = require('request');

function initializePromise() {
    var options = {
        uri: process.env.CAR_API_URL
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
      return (JSON.stringify({ "status": "remote car-book error!"}))
   })

})




/*
var request = require('request');
var fs = require('fs');

var api_url = process.env.CAR_API_URL;
var stdin = JSON.parse(fs.readFileSync('/dev/stdin').toString());

request.post(
    api_url,
    { json: stdin },
    function (error, response, body) {
        if (!error && response.statusCode === 200) {
            console.log(JSON.stringify(response.body));
        } else {
            throw new Error();
        }
    }
);

*/