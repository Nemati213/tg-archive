package ru.itmo.nemat.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tgId;
    @Column(columnDefinition = "TEXT")
    private String text;
    private String author;
    private OffsetDateTime dateTime;
    private String forwardedFrom;
    private Long tgReplyId;

    @ManyToOne
    @JoinColumn(name = "reply_to_id")
    private Message replyTo;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "message")
    private List<Attachment> mediaPaths;
    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private List<Note> notes = new ArrayList<>();


    @Override
    public String toString() {
        if(mediaPaths == null || mediaPaths.isEmpty()) {
            return String.format("[%s] %s: %s",
                    dateTime, author, text);
        }
        return String.format("[%s] %s: %s %s",
                        dateTime, author, text, mediaPaths);
    }

}
