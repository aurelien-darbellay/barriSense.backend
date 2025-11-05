package com.barrisense.backend.complaint.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "complaints")

public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private Long id;

    @Column(name = "user_id", nullable = false)
    @Getter
    @Setter
    private UUID userId;

    @Column(name = "hood_id", nullable = false)
    @Getter
    @Setter
    private Long hoodId;

    @Column(name = "hood_name", nullable = false)
    @Getter
    @Setter
    private String hoodName;

    @Column(nullable = false, length = 1000)
    @Getter
    @Setter
    private String content;


}