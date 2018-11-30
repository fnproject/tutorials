const fdk=require('@fnproject/fdk');

var api_url = process.env.EMAIL_API_URL;
var request = require('request');

fdk.handle(function(input){
	request(api_url, { json: input}, (err, res, body) => {
	  if (err) { return console.log(err); }
	});
	return {'response': input.subject}
})
