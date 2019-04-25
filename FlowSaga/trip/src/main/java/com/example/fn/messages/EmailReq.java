package com.example.fn.messages;

import com.fnproject.fn.api.flow.Flows;

import java.io.Serializable;

public class EmailReq implements Serializable {
    public String message;

    public static EmailReq composeSuccessEmail(BookingRes flightResponse,
                                               BookingRes hotelResponse,
                                               BookingRes carResponse) {

        String msg = flightResponse.confirmation + " " + hotelResponse.confirmation + " " + carResponse.confirmation;

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

    public static void sendFailEmail(String funcIdEmail) {
        Flows.currentFlow().invokeFunction(funcIdEmail, composeFailEmail());
    }

    public static void sendSuccessMail(String funcIdEmail, BookingRes flightRes, BookingRes hotelRes, BookingRes carRes) {
        EmailReq message = composeSuccessEmail(flightRes, hotelRes, carRes);
        Flows.currentFlow().invokeFunction(funcIdEmail, message);
    }
}
