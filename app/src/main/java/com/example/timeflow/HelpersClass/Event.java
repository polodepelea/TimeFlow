package com.example.timeflow.HelpersClass;

import java.io.Serializable;

public class Event implements Serializable {
    private String eventName;
    private String eventTime;
    private String eventDate;


    public Event() {
    }

    public Event(String eventDate, String eventTime,String eventName) {
        this.eventDate = eventDate;
        this.eventName = eventName;
        this.eventTime = eventTime;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getEventDate() {return eventDate;}

    public void setEventDate(String eventDate) {this.eventDate = eventDate;}
}

