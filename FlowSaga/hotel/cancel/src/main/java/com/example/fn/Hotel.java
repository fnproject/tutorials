package com.example.fn;
import com.fnproject.fn.api.FnConfiguration;
import com.fnproject.fn.api.RuntimeContext;

import com.goplacesairlines.api.GoPlacesHotels;

import java.io.Serializable;

public class Hotel implements Serializable {

    private GoPlacesHotels apiClient;

    public static class HotelBookingRequest implements Serializable {
        public String city;
        public String hotel;
    }

    public static class HotelBookingResponse implements Serializable {
        public HotelBookingResponse(String confirmation) {
            this.confirmation = confirmation;
        }

        public String confirmation;
    }

    @FnConfiguration
    public void configure(RuntimeContext ctx) {
        String hotelApiUrl = ctx.getConfigurationByKey("HOTEL_API_URL")
                .orElse("http://localhost:3000");

        String hotelApiSecret = ctx.getConfigurationByKey("HOTEL_API_SECRET")
                .orElseThrow(() -> new RuntimeException("No credentials provided for hotel API."));

        this.apiClient = new GoPlacesHotels(hotelApiUrl, hotelApiSecret);
    }

    public HotelBookingResponse book(HotelBookingRequest hotelDetails) {
        GoPlacesHotels.BookingResponse apiResponse  = apiClient.bookHotel(hotelDetails.city, hotelDetails.hotel);
        return new HotelBookingResponse(apiResponse.confirmation);
    }

    public HotelBookingResponse cancel(HotelBookingRequest cancellationRequest) {
        GoPlacesHotels.CancellationResponse apiResponse  = apiClient.cancelHotel(cancellationRequest.city, cancellationRequest.hotel);
        return new HotelBookingResponse(apiResponse.confirmation.toString());
    }
}
