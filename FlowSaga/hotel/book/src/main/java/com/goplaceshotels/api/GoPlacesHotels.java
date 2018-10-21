package com.goplaceshotels.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

public class GoPlacesHotels implements Serializable {
    public GoPlacesHotels(String hotelApiUrl, String secret) {
        this.hotelApiUrl = hotelApiUrl;
        this.secret = secret;
    }

    private String secret;
    private String hotelApiUrl;

    public BookingResponse bookHotel(String city, String hotel) {
        ObjectMapper jsonify = new ObjectMapper();
        BookHotelInfo request = new BookHotelInfo();
        request.city = city;
        request.hotel = hotel;
        request.secret = this.secret;
        try {
            String response = Request.Post(hotelApiUrl)
                    .bodyByteArray(
                            jsonify.writeValueAsBytes(request),
                            ContentType.APPLICATION_JSON)
                    .execute().returnContent().asString();
            return jsonify.readValue(response, BookingResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CancellationResponse cancelHotel(String city, String hotel) {
        ObjectMapper jsonify = new ObjectMapper();
        try {
            String response = Request.Delete(hotelApiUrl)
                    .execute().returnContent().asString();
            return jsonify.readValue(response, CancellationResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class CancellationResponse implements Serializable {
        public Boolean confirmation;
    }

    public static class BookHotelInfo implements Serializable {
        public String city;
        public String hotel;
        public String secret;
    }

    public static class BookingResponse implements Serializable {
        public String confirmation;
    }
}
