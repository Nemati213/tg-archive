package ru.itmo.nemat.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.itmo.nemat.dto.CreateNoteRequest;
import ru.itmo.nemat.dto.NoteDTO;
import ru.itmo.nemat.services.NoteService;

@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
public class NoteRestController {
    private final NoteService noteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NoteDTO addNote(@RequestBody CreateNoteRequest request) {
        return noteService.createNote(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
    }
}