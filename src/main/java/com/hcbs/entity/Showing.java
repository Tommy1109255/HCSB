package com.hcbs.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Showing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Film film;

    @ManyToOne
    private Screen screen;

    private LocalDate date;
    private LocalTime startTime;

    private double priceLowerHall;
    private double priceGallery;
    private int remainingSeats;
    @OneToMany(mappedBy = "showing", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Booking> bookings;
}
