const fdk=require('@fnproject/fdk');

fdk.handle(function(input, ctx){
  
  console.log('\nReturn env vars')
  return ctx.config
})
