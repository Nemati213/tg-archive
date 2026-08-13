package ru.itmo.nemat.models;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record MessageFilter(
        String query,
        String author,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime dateFrom,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime dateTo,
        String mediaType
) {}