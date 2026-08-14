package dev.beanguard.server.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_sequence")
public class OrderSequenceEntity {

    @Id
    @Column(nullable = false)
    private int year;

    @Column(name = "last_number", nullable = false)
    private int lastNumber;
}
