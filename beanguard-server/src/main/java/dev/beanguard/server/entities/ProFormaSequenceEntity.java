package dev.beanguard.server.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pro_forma_sequence")
public class ProFormaSequenceEntity {

    @Id
    @Column(nullable = false)
    private int year;

    @Column(name = "last_number", nullable = false)
    private int lastNumber;
}
