package ru.itmo.nemat.data;

import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import ru.itmo.nemat.models.Attachment;
import ru.itmo.nemat.models.AttachmentType;
import ru.itmo.nemat.models.Message;
import java.time.LocalDateTime;

public final class MessageSpecifications {

    private MessageSpecifications() {
        throw new UnsupportedOperationException();
    }

    public static Specification<Message> hasText(String text) {
        return (root, query, cb) -> {
            if (text == null || text.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(
                    cb.lower(root.get("text")),
                    "%" + text.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Message> hasAuthor(String author) {
        return (root, query, cb) -> {
            if (author == null || author.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(
                    cb.lower(root.get("author")),
                    author.toLowerCase()
            );
        };
    }

    public static Specification<Message> createdAfter(LocalDateTime from) {
        return (root, query, cb) -> {
            if (from == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("dateTime"), from);
        };
    }

    public static Specification<Message> createdBefore(LocalDateTime to) {
        return (root, query, cb) -> {
            if (to == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("dateTime"), to);
        };
    }

    public static Specification<Message> hasMediaType(String type) {
        return (root, query, cb) -> {
            if (type == null || type.isBlank() || type.equals("ALL")) {
                return cb.conjunction();
            }
            query.distinct(true);
            Join<Message, Attachment> join = root.join("mediaPaths");

            return cb.equal(join.get("type"), AttachmentType.valueOf(type.toUpperCase()));
        };
    }

}