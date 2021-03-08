package com.lockminds.brass_services.model;

public class Lot {
    String lot_no, haulier, driver,destination,escorter,horse,source;

    public Lot() {
    }

    public Lot(String lot_no, String haulier, String driver, String destination, String escorter, String horse, String source) {
        this.lot_no = lot_no;
        this.haulier = haulier;
        this.driver = driver;
        this.destination = destination;
        this.escorter = escorter;
        this.source = source;
        this.horse = horse;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getLot_no() {
        return lot_no;
    }

    public void setLot_no(String lot_no) {
        this.lot_no = lot_no;
    }

    public String getHaulier() {
        return haulier;
    }

    public void setHaulier(String haulier) {
        this.haulier = haulier;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getEscorter() {
        return escorter;
    }

    public void setEscorter(String escorter) {
        this.escorter = escorter;
    }

    public String getHorse() {
        return horse;
    }

    public void setHorse(String horse) {
        this.horse = horse;
    }
}
