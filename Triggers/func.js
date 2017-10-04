var Jimp = require("jimp");
fs = require('fs');

obj = JSON.parse(fs.readFileSync('/dev/stdin').toString())
if (obj.name != "") {
    name = obj.name
}

console.log("got:", obj)

Jimp.read("lenna.png").then(function (lenna) {
    lenna.resize(256, 256)            // resize 
         .quality(60)                 // set JPEG quality 
         .greyscale()                 // set greyscale 
         .write("lena-small-bw.jpg"); // save 
}).catch(function (err) {
    console.error(err);
});