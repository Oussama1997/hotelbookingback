package com.service.hotelbookingback.exceptions;

public class InvalidBookingStateAndDateException extends RuntimeException{

    public InvalidBookingStateAndDateException(String message){
        super(message);
    }
}
