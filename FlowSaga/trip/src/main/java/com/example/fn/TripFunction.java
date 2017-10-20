package com.example.fn;

import com.example.fn.messages.BookingRes;
import com.example.fn.messages.EmailReq;
import com.example.fn.messages.TripReq;
import com.fnproject.fn.api.flow.Flow;
import com.fnproject.fn.api.flow.Flows;

import java.io.Serializable;

import static com.example.fn.messages.EmailReq.composeSuccessEmail;

public class TripFunction implements Serializable {

    public void book(TripReq input) {

    }
}