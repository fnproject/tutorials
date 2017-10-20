package com.example.fn.messages;

import java.io.Serializable;

public class EmailReq implements Serializable {
    public String message;

    public static EmailReq composeSuccessEmail(BookingRes flightResponse,
                                               BookingRes hotelResponse,
                                               BookingRes carResponse) {
        EmailReq result = new EmailReq();
        result.message = "Flight confirmation: " + flightResponse.confirmation + "\n" +
                "Hotel confirmation: " +  hotelResponse.confirmation + "\n" +
                "Car rental confirmation: " + carResponse.confirmation;
        return result;
    }

    public static EmailReq composeFailEmail() {
        EmailReq result = new EmailReq();
        result.message = "We failed to book your trip, sorry.";
        return result;
    }
}
