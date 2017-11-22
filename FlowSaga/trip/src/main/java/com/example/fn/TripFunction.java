package com.example.fn;

import com.example.fn.messages.BookingRes;
import com.example.fn.messages.EmailReq;
import com.example.fn.messages.TripReq;
import com.fnproject.fn.api.flow.Flow;
import com.fnproject.fn.api.flow.FlowFuture;
import com.fnproject.fn.api.flow.Flows;

import java.io.Serializable;

public class TripFunction implements Serializable {

    public void book1(TripReq input) {
        Flow f = Flows.currentFlow();

        FlowFuture<BookingRes> flightFuture =
                f.invokeFunction("./flight/book", input.flight, BookingRes.class);

        FlowFuture<BookingRes> hotelFuture =
                f.invokeFunction("./hotel/book", input.hotel, BookingRes.class);

        FlowFuture<BookingRes> carFuture =
                f.invokeFunction("./car/book", input.carRental, BookingRes.class);

        flightFuture.thenCompose(
            (flightRes) -> hotelFuture.thenCompose(
                (hotelRes) -> carFuture.whenComplete(
                    (carRes, e) -> EmailReq.sendSuccessMail(flightRes, hotelRes, carRes)
                )
            )
        );
    }



    public void book2(TripReq input) {
        Flow f = Flows.currentFlow();

        FlowFuture<BookingRes> flightFuture =
                f.invokeFunction("./flight/book", input.flight, BookingRes.class);

        FlowFuture<BookingRes> hotelFuture =
                f.invokeFunction("./hotel/book", input.hotel, BookingRes.class);

        FlowFuture<BookingRes> carFuture =
                f.invokeFunction("./car/book", input.carRental, BookingRes.class);

        flightFuture.thenCompose(
                (flightRes) -> hotelFuture.thenCompose(
                        (hotelRes) -> carFuture.whenComplete(
                                (carRes, e) -> EmailReq.sendSuccessMail(flightRes, hotelRes, carRes)
                        )
                                .exceptionallyCompose( (e) -> cancel("./car/cancel", input.carRental, e) )
                )
                .exceptionallyCompose( (e) -> cancel("./hotel/cancel", input.hotel, e) )
        )
        .exceptionallyCompose( (e) -> cancel("./flight/cancel", input.flight, e) )
        .exceptionally( (err) -> {EmailReq.sendFailEmail(); return null;} );
    }

    private static FlowFuture<BookingRes> cancel(String cancelFn, Object input, Throwable e) {
        Flows.currentFlow().invokeFunction(cancelFn, input, BookingRes.class);
        return Flows.currentFlow().failedFuture(e);
    }



    public void book3(TripReq input) {
        Flow f = Flows.currentFlow();

        FlowFuture<BookingRes> flightFuture =
                f.invokeFunction("./flight/book", input.flight, BookingRes.class);

        FlowFuture<BookingRes> hotelFuture =
                f.invokeFunction("./hotel/book", input.hotel, BookingRes.class);

        FlowFuture<BookingRes> carFuture =
                f.invokeFunction("./car/book", input.carRental, BookingRes.class);

        flightFuture.thenCompose(
                (flightRes) -> hotelFuture.thenCompose(
                        (hotelRes) -> carFuture.whenComplete(
                                (carRes, e) -> EmailReq.sendSuccessMail(flightRes, hotelRes, carRes)
                        )
                        .exceptionallyCompose( (e) -> retryCancel("./car/cancel", input.carRental, e) )
                )
                .exceptionallyCompose( (e) -> retryCancel("./hotel/cancel", input.hotel, e) )
        )
        .exceptionallyCompose( (e) -> retryCancel("./flight/cancel", input.flight, e) )
        .exceptionally( (err) -> {EmailReq.sendFailEmail(); return null;} );
    }

    private static FlowFuture<BookingRes> retryCancel(String cancelFn, Object input, Throwable e) {
        Retry.exponentialWithJitter(
                () -> Flows.currentFlow().invokeFunction(cancelFn, input, BookingRes.class));
        return Flows.currentFlow().failedFuture(e);
    }
}