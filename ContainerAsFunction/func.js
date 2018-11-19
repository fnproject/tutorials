const fdk = require('@fnproject/fdk');
const fs  = require('fs');
const tmp = require('tmp');
const im  = require('imagemagick');

fdk.handle((buffer, ctx) => {
  return new Promise((resolve, reject) => {
    tmp.tmpName((err, tmpFile) => {
      if (err) throw err;
      fs.writeFile(tmpFile, buffer, (err) => {
        if (err) throw err;
        im.identify(['-format', '{"width": %w, "height": %h}', tmpFile],
          (err, output) => {
            if (err) {
              reject(err);
            } else {
              resolve(JSON.parse(output));
            }
          }
        );
      });
    });
  });
}, { inputMode: 'buffer' });

