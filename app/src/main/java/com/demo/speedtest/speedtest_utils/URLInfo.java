package com.demo.speedtest.speedtest_utils;

public class URLInfo {
    double lat;
    double lon;
    String name;
    String country;
    String cc;
    String sponsor;
    String host;

    public void setLat(double lat) {
        this.lat = lat;
    }

    public Double getLat() {
        return lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public Double getLon() {
        return lon;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    public void setCC(String cc) {
        this.cc = cc;
    }

    public String getCC() {
        return cc;
    }

    public void setSponsor(String sponsor) {
        this.sponsor = sponsor;
    }

    public String getSponsor() {
        return sponsor;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }
}
