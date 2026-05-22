package com.hcbs.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Screen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int screenNumber;
    private int capacity;
    private int rows;
    private int columns;

    @ManyToOne
    @JoinColumn(name = "cinema_id")
    private Cinema cinema;
}
