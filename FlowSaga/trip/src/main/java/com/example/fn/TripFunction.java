package com.example.fn;

import com.example.fn.messages.BookingRes;
import com.example.fn.messages.EmailReq;
import com.example.fn.messages.TripReq;
import com.fnproject.fn.api.FnConfiguration;
import com.fnproject.fn.api.FnFeature;
import com.fnproject.fn.api.RuntimeContext;
import com.fnproject.fn.api.flow.Flow;
import com.fnproject.fn.api.flow.FlowFuture;
import com.fnproject.fn.api.flow.Flows;
import com.fnproject.fn.runtime.flow.FlowFeature;

import java.io.Serializable;


@FnFeature(FlowFeature.class)
public class TripFunction implements Serializable {

    private String funcIdFlightBook;
    private String funcIdFlightCancel;

    private String funcIdCarBook;
    private String funcIdCarCancel;

    private String funcIdHotelBook;
    private String funcIdHotelCancel;

    private String funcIdEmail;


    @FnConfiguration
    public void configure(RuntimeContext ctx) {

        funcIdFlightBook = ctx.getConfigurationByKey("FLIGHT_BOOK_ID")
                .orElseThrow(() -> new RuntimeException("No FLIGHT_BOOK_ID defined!"));

        funcIdFlightCancel = ctx.getConfigurationByKey("FLIGHT_CANCEL_ID")
                .orElseThrow(() -> new RuntimeException("No FLIGHT_CANCEL_ID defined!"));

        funcIdHotelBook = ctx.getConfigurationByKey("HOTEL_BOOK_ID")
                .orElseThrow(() -> new RuntimeException("No HOTEL_BOOK_ID defined!"));

        funcIdHotelCancel = ctx.getConfigurationByKey("HOTEL_CANCEL_ID")
                .orElseThrow(() -> new RuntimeException("No HOTEL_CANCEL_ID defined!"));

        funcIdCarBook = ctx.getConfigurationByKey("CAR_BOOK_ID")
                .orElseThrow(() -> new RuntimeException("No CAR_BOOK_ID defined!"));

        funcIdCarCancel = ctx.getConfigurationByKey("CAR_CANCEL_ID")
                .orElseThrow(() -> new RuntimeException("No CAR_CANCEL_ID defined!"));

        funcIdEmail = ctx.getConfigurationByKey("EMAIL_ID")
                .orElseThrow(() -> new RuntimeException("No FUNC_ID_EMAIL defined!"));

    }

    public void book1(TripReq input) {
        Flow f = Flows.currentFlow();

        FlowFuture<BookingRes> flightFuture =
            f.invokeFunction(funcIdFlightBook, input.flight, BookingRes.class);

        FlowFuture<BookingRes> hotelFuture =
            f.invokeFunction(funcIdHotelBook, input.hotel, BookingRes.class);

        FlowFuture<BookingRes> carFuture =
            f.invokeFunction(funcIdCarBook, input.carRental, BookingRes.class);

        flightFuture.thenCompose(
            (flightRes) -> hotelFuture.thenCompose(
                (hotelRes) -> carFuture.whenComplete(
                    (carRes, e) -> {
                        if (e==null) {// no exception!
                            EmailReq.sendSuccessMail(funcIdEmail, flightRes, hotelRes, carRes);
                        }
                    }
                )
            )
        );
    }


    public void book2(TripReq input) {
        Flow f = Flows.currentFlow();

        FlowFuture<BookingRes> flightFuture =
            f.invokeFunction(funcIdFlightBook, input.flight, BookingRes.class);

        FlowFuture<BookingRes> hotelFuture =
            f.invokeFunction(funcIdHotelBook, input.hotel, BookingRes.class);

        FlowFuture<BookingRes> carFuture =
            f.invokeFunction(funcIdCarBook, input.carRental, BookingRes.class);

        flightFuture.thenCompose(
            (flightRes) -> hotelFuture.thenCompose(
                (hotelRes) -> carFuture.whenComplete(
                    (carRes, e) -> {
                        if (e == null) { // no exception!
                            EmailReq.sendSuccessMail(funcIdEmail, flightRes, hotelRes, carRes);
                        }
                    }
                )
                .exceptionallyCompose( (e) -> cancel(funcIdCarCancel, input.carRental, e) )
            )
            .exceptionallyCompose( (e) -> cancel(funcIdHotelCancel, input.hotel, e) )
        )
        .exceptionallyCompose( (e) -> cancel(funcIdFlightCancel, input.flight, e) )
        .exceptionally( (err) -> {
            EmailReq.sendFailEmail(funcIdEmail);
            return null;
        } );
    }

    private static FlowFuture<BookingRes> cancel(String cancelFnId, Object input, Throwable e) {
        Flows.currentFlow().invokeFunction(cancelFnId, input, BookingRes.class);
        return Flows.currentFlow().failedFuture(e);
    }



    public void book3(TripReq input) {
        Flow f = Flows.currentFlow();

        FlowFuture<BookingRes> flightFuture =
            f.invokeFunction(funcIdFlightBook, input.flight, BookingRes.class);

        FlowFuture<BookingRes> hotelFuture =
            f.invokeFunction(funcIdHotelBook, input.hotel, BookingRes.class);

        FlowFuture<BookingRes> carFuture =
            f.invokeFunction(funcIdCarBook, input.carRental, BookingRes.class);

        flightFuture.thenCompose(
            (flightRes) -> hotelFuture.thenCompose(
                (hotelRes) -> carFuture.whenComplete(
                    (carRes, e) -> {
                        if (e == null) { // no exception!
                            EmailReq.sendSuccessMail(funcIdEmail, flightRes, hotelRes, carRes);
                        }
                    }
                )
                .exceptionallyCompose( (e) -> retryCancel(funcIdCarCancel, input.carRental, e) )
            )
            .exceptionallyCompose( (e) -> retryCancel(funcIdHotelCancel, input.hotel, e) )
        )
        .exceptionallyCompose( (e) -> retryCancel(funcIdFlightCancel, input.flight, e) )
        .exceptionally( (err) -> {
            EmailReq.sendFailEmail(funcIdEmail);
            return null;
        } );
    }

    private static FlowFuture<BookingRes> retryCancel(String cancelFn, Object input, Throwable e) {
        Retry.exponentialWithJitter(
            () -> Flows.currentFlow().invokeFunction(cancelFn, input, BookingRes.class));
        return Flows.currentFlow().failedFuture(e);
    }


}