const fdk=require('@fnproject/fdk');

fdk.handle(function(input, ctx){
  
  console.log('\nReturn 3 vars')
  return {
            "DB_HOST_URL": ctx.config.DB_HOST_URL,
            "DB_USER": ctx.config.DB_USER,
            "DB_PASSWORD": ctx.config.DB_PASSWORD
         }
})
