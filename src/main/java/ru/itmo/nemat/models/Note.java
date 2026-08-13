package ru.itmo.nemat.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Entity
@Data
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private Message message;

    private OffsetDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

}
