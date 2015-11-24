package com.example;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by jlong on 11/23/15.
 */
@Entity
public class Reservation {

    @Id
    @GeneratedValue
    private Long id ;

    private String reservationName ;

    @Override
    public String toString() {
        String sb = "Reservation{" + "id=" + id +
                ", reservationName='" + reservationName + '\'' +
                '}';
        return sb;
    }

    Reservation() { // why JPA why??
    }

    public Reservation(String reservationName) {

        this.reservationName = reservationName;
    }

    public Long getId() {

        return id;
    }

    public String getReservationName() {
        return reservationName;
    }
}
