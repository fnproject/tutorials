package com.example.fn.messages;

import java.io.Serializable;
import java.util.Date;

public class TripReq implements Serializable {
    public FlightReq flight;
    public HotelReq hotel;
    public CarReq carRental;

    public static class FlightReq implements Serializable {
        public Date departureTime;
        public String flightCode;
    }

    public static class HotelReq implements Serializable {
        public String city;
        public String hotel;
    }

    public static class CarReq implements Serializable {
        public String model;
    }
}
