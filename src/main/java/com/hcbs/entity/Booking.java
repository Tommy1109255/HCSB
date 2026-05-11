package com.hcbs.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String reference;

    @ManyToOne
    private Showing showing;

    private int numTickets;
    private String seatNumbers;
    private double totalCost;
    private LocalDateTime bookingDate;

    @ManyToOne
    private User bookedBy;

    private boolean cancelled;
    private LocalDateTime cancellationDate;
}
