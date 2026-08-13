package ru.itmo.nemat.dto;

public record CreateNoteRequest(
        Long messageId,
        String text
) {}