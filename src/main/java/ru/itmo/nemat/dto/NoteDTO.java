package ru.itmo.nemat.dto;

import java.time.OffsetDateTime;

public record NoteDTO(
        Long id,
        String text,
        OffsetDateTime createdAt,
        String author
) {}