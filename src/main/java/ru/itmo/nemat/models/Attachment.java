package ru.itmo.nemat.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String path;
    private AttachmentType type;
    @ManyToOne
    private Message message;

    @Override
    public String toString() {
        return type + ": " + path;
    }
}