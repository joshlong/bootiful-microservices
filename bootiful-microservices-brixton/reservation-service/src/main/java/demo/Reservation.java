package demo;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Reservation { // RESERVATIONS

    @Id
    @GeneratedValue
    private Long id ;  // ID

    private String reservationName ; // reservation_name

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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Reservation{");
        sb.append("id=").append(id);
        sb.append(", reservationName='").append(reservationName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
