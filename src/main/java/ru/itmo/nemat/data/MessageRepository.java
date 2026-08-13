package ru.itmo.nemat.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import ru.itmo.nemat.models.Message;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long>, JpaSpecificationExecutor<Message> {
    boolean existsByTgId(Long tgId);
    Optional<Message> findByTgId(Long tgId);
    @Query("SELECT m.tgId, m.id FROM Message m")
    List<Object[]> findAllIdMappings();

    Slice<Message> findAllByTgReplyIdNotNullAndReplyToIsNull(Pageable pageable);

    @EntityGraph(attributePaths = {"mediaPaths"})
    Page<Message> findAll(Pageable pageable);

    int countAllByTgReplyIdNotNullAndReplyToIsNull();
    long countByDateTimeBefore(OffsetDateTime dateTime);

    boolean existsByAuthor(String author);

    long countByDateTimeLessThan(OffsetDateTime dateTime);

}
