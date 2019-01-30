require 'fdk'
require 'jsonclient'

def myfunction(context:, input:)
  payload = { city: input['city'], hotel: input['hotel'], secret: ENV['HOTEL_API_SECRET'] }
  resp = JSONClient.delete(ENV['HOTEL_API_URL'], body: payload)
  return resp.body if resp.status < 300

  raise HTTPClient::BadResponseError, resp.reason
end

FDK.handle(target: :myfunction)