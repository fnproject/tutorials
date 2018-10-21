package com.example.fn.messages;

import com.fnproject.fn.api.FnConfiguration;
import com.fnproject.fn.api.RuntimeContext;
import com.fnproject.fn.api.flow.Flows;

import java.io.Serializable;

public class EmailReq implements Serializable {
    public String message;

    public static void setFuncEmail(String funcEmail) {
        EmailReq.funcEmail = funcEmail;
    }

    static private String funcEmail;

    public static void sendFailEmail() {
        System.out.println("----> sendFailEmail : " + funcEmail);
        Flows.currentFlow().invokeFunction(funcEmail, composeFailEmail());
    }

    public static void sendSuccessMail(BookingRes flightRes, BookingRes hotelRes, BookingRes carRes) {
        Flows.currentFlow().invokeFunction(funcEmail, composeSuccessEmail(flightRes, hotelRes, carRes));
    }

    public static void sendSuccessMail(BookingRes flightRes, BookingRes carRes) {
        Flows.currentFlow().invokeFunction(funcEmail, composeSuccessEmail(flightRes, carRes));
    }

    public static EmailReq composeSuccessEmail(BookingRes flightResponse,
                                               BookingRes hotelResponse,
                                               BookingRes carResponse) {

        EmailReq result = new EmailReq();
        result.message = "Flight confirmation: " + flightResponse.confirmation + "\n" +
                "Hotel confirmation: " +  hotelResponse.confirmation + "\n" +
                "Car rental confirmation: " + carResponse.confirmation;
        return result;
    }

    public static EmailReq composeSuccessEmail(BookingRes flightResponse,
                                               BookingRes hotelResponse) {
        EmailReq result = new EmailReq();
        result.message = "Flight confirmation: " + flightResponse.confirmation + "\n" +
                "Hotel confirmation: " +  hotelResponse.confirmation ;
        return result;
    }



    public static EmailReq composeFailEmail() {
        EmailReq result = new EmailReq();
        result.message = "We failed to book your trip, sorry.";
        return result;
    }

/*    public static void sendSuccessMail(BookingRes flightRes, BookingRes hotelRes) {
        EmailReq message = composeSuccessEmail(flightRes, hotelRes);
        Flows.currentFlow().invokeFunction(funcEmail, message);
    }*/

}
