package ru.itmo.nemat.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
public class ReadingProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_read_message_id", nullable = false)
    private Message lastReadMessage;

    private OffsetDateTime updatedAt;
}