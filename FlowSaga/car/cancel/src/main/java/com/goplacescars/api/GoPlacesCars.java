package com.goplacescars.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.io.Serializable;

public class GoPlacesCars implements Serializable {
    public GoPlacesCars(String carApiUrl, String secret) {
        this.carApiUrl = carApiUrl;
        this.secret = secret;
    }

    private String secret;
    private String carApiUrl;

    public BookingResponse bookCar(String model) {
        ObjectMapper jsonify = new ObjectMapper();
        BookCarInfo request = new BookCarInfo();
        request.model = model;
        request.secret = this.secret;
        try {
            String response = Request.Post(carApiUrl)
                    .bodyByteArray(
                            jsonify.writeValueAsBytes(request),
                            ContentType.APPLICATION_JSON)
                    .execute().returnContent().asString();
            return jsonify.readValue(response, BookingResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CancellationResponse cancelCar(String model) {
        ObjectMapper jsonify = new ObjectMapper();
        try {
            String response = Request.Delete(carApiUrl)
                    .execute().returnContent().asString();
            return jsonify.readValue(response, CancellationResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class CancellationResponse implements Serializable {
        public Boolean confirmation;
    }

    public static class BookCarInfo implements Serializable {
        public String model;
        public String secret;
    }


    public static class BookingResponse implements Serializable {
        public String confirmation;
    }
}
