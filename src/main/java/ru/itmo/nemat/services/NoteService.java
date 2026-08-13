package ru.itmo.nemat.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.nemat.dto.CreateNoteRequest;
import ru.itmo.nemat.dto.NoteDTO;
import ru.itmo.nemat.models.Message;
import ru.itmo.nemat.models.Note;
import ru.itmo.nemat.models.User;
import ru.itmo.nemat.data.MessageRepository;
import ru.itmo.nemat.data.NoteRepository;
import ru.itmo.nemat.data.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class NoteService {
    private final NoteRepository noteRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Текущий пользователь не найден в БД"));
    }

    @Transactional
    public NoteDTO createNote(CreateNoteRequest request) {
        Message message = messageRepository.findById(request.messageId())
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));

        User currentUser = getCurrentUser();

        Note note = new Note();
        note.setText(request.text());
        note.setCreatedAt(OffsetDateTime.now());
        note.setMessage(message);
        note.setUser(currentUser);

        Note savedNote = noteRepository.save(note);

        return new NoteDTO(savedNote.getId(), savedNote.getText(), savedNote.getCreatedAt(), currentUser.getUsername());
    }

    @Transactional
    public void deleteNote(Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new EntityNotFoundException("Note not found"));

        User currentUser = getCurrentUser();

        if (!note.getUser().getId().equals(currentUser.getId()) && !currentUser.getRole().equals("ROLE_ADMIN")) {
            throw new RuntimeException("У вас нет прав для удаления этой заметки");
        }

        noteRepository.deleteById(noteId);
    }
}