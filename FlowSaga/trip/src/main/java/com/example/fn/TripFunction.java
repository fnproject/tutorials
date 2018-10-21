	package com.example.fn;

import com.example.fn.messages.BookingRes;
import com.example.fn.messages.EmailReq;
import com.example.fn.messages.TripReq;
import com.fnproject.fn.api.flow.Flow;
import com.fnproject.fn.api.flow.FlowFuture;
import com.fnproject.fn.api.flow.Flows;
import com.fnproject.fn.api.FnFeature;
import com.fnproject.fn.runtime.flow.FlowFeature;

import com.fnproject.fn.api.FnConfiguration;
import com.fnproject.fn.api.RuntimeContext;

import java.io.Serializable;

@FnFeature(FlowFeature.class)
public class TripFunction implements Serializable {

    String funcFlightBook;
    String funcFlightCancel;
    String funcHotelBook;
    String funcHotelCancel;
    String funcCarBook;
    String funcCarCancel;
    String funcEmail;

    @FnConfiguration
    public void configure(RuntimeContext ctx) {
        funcFlightBook = ctx.getConfigurationByKey("FLIGHT-BOOK-ID")
                .orElseThrow(() -> new RuntimeException("Missing FunctionId"));

        funcFlightCancel = ctx.getConfigurationByKey("FLIGHT-CANCEL-ID")
                .orElseThrow(() -> new RuntimeException("Missing FunctionId"));

        funcHotelBook = ctx.getConfigurationByKey("HOTEL-BOOK-ID")
                .orElseThrow(() -> new RuntimeException("Missing FunctionId"));

        funcHotelCancel = ctx.getConfigurationByKey("HOTEL-CANCEL-ID")
                .orElseThrow(() -> new RuntimeException("Missing FunctionId"));

        funcCarBook = ctx.getConfigurationByKey("CAR-BOOK-ID")
                .orElseThrow(() -> new RuntimeException("Missing FunctionId"));

        funcCarCancel = ctx.getConfigurationByKey("CAR-CANCEL-ID")
                .orElseThrow(() -> new RuntimeException("Missing FunctionId"));

        funcEmail = ctx.getConfigurationByKey("EMAIL-ID")
                .orElseThrow(() -> new RuntimeException("Missing FunctionId"));

        EmailReq.setFuncEmail(funcEmail); // todo remove - set email functionId

    }

    public void bookFlightHotel(TripReq input) {

        Flow f = Flows.currentFlow();

        FlowFuture<BookingRes> flightFuture =
                f.invokeFunction(funcFlightBook, input.flight, BookingRes.class);

        FlowFuture<BookingRes> hotelFuture =
                f.invokeFunction(funcHotelBook, input.hotel, BookingRes.class);

        flightFuture.thenCompose(
                (flightRes) -> hotelFuture.whenComplete(
                        (hotelRes, e) -> EmailReq.sendSuccessMail(flightRes, hotelRes)
                )
        );

    }


    public void bookAll(TripReq input) {

        Flow f = Flows.currentFlow();

        FlowFuture<BookingRes> flightFuture =
                f.invokeFunction(funcFlightBook, input.flight, BookingRes.class);

        FlowFuture<BookingRes> hotelFuture =
                f.invokeFunction(funcHotelBook, input.hotel, BookingRes.class);

        FlowFuture<BookingRes> carFuture =
                f.invokeFunction(funcCarBook, input.carRental, BookingRes.class);


        flightFuture.thenCompose(
                (flightRes) -> hotelFuture.thenCompose(
                        (hotelRes) -> carFuture.whenComplete(
                                (carRes, e) -> EmailReq.sendSuccessMail(flightRes, hotelRes, carRes)
                        ).exceptionallyCompose( (e) -> cancel(funcCarCancel, input.carRental, e) )
                ).exceptionallyCompose( (e) -> cancel(funcHotelCancel, input.hotel, e) )
        ).exceptionallyCompose( (e) -> cancel(funcFlightCancel, input.flight, e) )
        .exceptionally( (err) -> {
                                EmailReq.sendFailEmail();
                                return null;
                    } );
        ;

    }

    public void bookHotelOnly(TripReq input) {

        Flow f = Flows.currentFlow();

        FlowFuture<BookingRes> flightFuture =
                f.invokeFunction(funcFlightBook, input.flight, BookingRes.class);

        flightFuture.whenComplete((bookingRes, throwable) -> {
            if (throwable != null) {
                System.out.println(" throwable : " + throwable.getMessage());
            } else EmailReq.sendSuccessMail(bookingRes, bookingRes);
        });
    }


    private static FlowFuture<BookingRes> cancel(String cancelFn, Object input, Throwable e) {
        Flows.currentFlow().invokeFunction(cancelFn, input, BookingRes.class);
        return Flows.currentFlow().failedFuture(e);
    }

 
    private static FlowFuture<BookingRes> retryCancel(String cancelFn, Object input, Throwable e) {
        Retry.exponentialWithJitter(
            () -> Flows.currentFlow().invokeFunction(cancelFn, input, BookingRes.class));
        return Flows.currentFlow().failedFuture(e);
    }
}