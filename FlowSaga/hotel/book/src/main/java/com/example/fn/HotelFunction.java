package com.example.fn;

import com.fnproject.fn.api.FnConfiguration;
import com.fnproject.fn.api.RuntimeContext;
import com.goplaceshotels.api.GoPlacesHotels;


public class HotelFunction {

    private GoPlacesHotels apiClient;

    public static class HotelBookingRequest {
        public String city;
        public String hotel;
    }

    public static class HotelBookingResponse {
        public String confirmation;
    }

    @FnConfiguration
    public void configure(RuntimeContext ctx) {
        String hotelApiUrl = ctx.getConfigurationByKey("HOTEL_API_URL")
                .orElseThrow(() -> new RuntimeException("No URL endpoint was provided."));

        String hotelApiSecret = "Todo";
        //String hotelApiSecret = ctx.getConfigurationByKey("HOTEL_API_SECRET")
        //        .orElseThrow(() -> new RuntimeException("No secret was provided."));

        apiClient = new GoPlacesHotels(hotelApiUrl, hotelApiSecret);

    }

    public HotelBookingResponse book(HotelBookingRequest input) {
        HotelBookingResponse res = new HotelBookingResponse();
        res.confirmation = apiClient.bookHotel(input.city, input.hotel).confirmation;
        return res;

        //FlightBookingResponse res = new FlightBookingResponse();
        //res.confirmation="123456";
    }
}