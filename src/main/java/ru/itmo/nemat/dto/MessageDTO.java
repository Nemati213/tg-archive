package ru.itmo.nemat.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record MessageDTO(
        Long id,
        String text,
        String author,
        OffsetDateTime dateTime,
        ReplyDTO replyTo,
        List<AttachmentDTO>mediaPaths,
        String forwardedFrom,
        List<NoteDTO> notes) {
}
