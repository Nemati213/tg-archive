package ru.itmo.nemat.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.nemat.models.Note;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
}