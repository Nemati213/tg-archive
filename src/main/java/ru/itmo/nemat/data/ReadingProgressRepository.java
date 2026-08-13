package ru.itmo.nemat.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.nemat.models.ReadingProgress;
import ru.itmo.nemat.models.User;
import java.util.Optional;

@Repository
public interface ReadingProgressRepository extends JpaRepository<ReadingProgress, Long> {
    Optional<ReadingProgress> findByUser(User user);
}