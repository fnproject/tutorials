const fdk=require('@fnproject/fdk');
const oracledb = require('oracledb');

fdk.handle((input, ctx) => {
  return {'version': oracledb.versionString};
})
