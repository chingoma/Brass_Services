package com.lockminds.brass_services.reponses;

public class LotResponse {

    String destination, date, destination_status;

    public LotResponse() {
    }

    public LotResponse(String destination, String date, String destination_status) {
        this.destination = destination;
        this.date = date;
        this.destination_status = destination_status;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDestination_status() {
        return destination_status;
    }

    public void setDestination_status(String destination_status) {
        this.destination_status = destination_status;
    }
}

