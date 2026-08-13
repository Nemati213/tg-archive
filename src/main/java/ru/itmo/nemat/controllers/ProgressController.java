package ru.itmo.nemat.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.nemat.services.ReadingProgressService;

@RestController
@RequestMapping("/api/v1/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ReadingProgressService progressService;

    @PostMapping
    public ResponseEntity<Void> updateProgress(@RequestParam Long messageId) {
        progressService.saveOrUpdateProgress(messageId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Long> getProgress() {
        Long messageId = progressService.getLastReadMessageId();
        return ResponseEntity.ok(messageId);
    }
}