package ru.itmo.nemat.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.itmo.nemat.data.MessageRepository;
import ru.itmo.nemat.data.MessageSpecifications;
import ru.itmo.nemat.dto.AttachmentDTO;
import ru.itmo.nemat.dto.MessageDTO;
import ru.itmo.nemat.dto.ReplyDTO;
import ru.itmo.nemat.dto.NoteDTO;
import ru.itmo.nemat.models.Attachment;
import ru.itmo.nemat.models.Message;
import ru.itmo.nemat.models.MessageFilter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository repository;
    private final ZoneId targetZone;

    public Page<MessageDTO> getMessages(Pageable pageable) {
        Page<Message> messages = repository.findAll(pageable);
        Page<MessageDTO> messagesDTO = messages.map(this::convertToDTO);
        return messagesDTO;
    }
    private MessageDTO convertToDTO(Message message) {
        ReplyDTO replyDTO = null;
        if (message.getReplyTo() != null) {
            Message r = message.getReplyTo();
            String previewText = r.getText();

            if ((previewText == null || previewText.isEmpty()) && !r.getMediaPaths().isEmpty()) {
                previewText = "[" + r.getMediaPaths().get(0).getType() + "]";
            }
            String excerpt = (previewText.length() > 50)
                    ? previewText.substring(0, 50).trim() + "..."
                    : previewText;

            replyDTO = new ReplyDTO(r.getId(), excerpt, r.getAuthor());
        }

        List<AttachmentDTO> attachments = message.getMediaPaths().stream()
                .map(a -> new AttachmentDTO(a.getId(), a.getPath(), a.getType().toString()))
                .toList();

        OffsetDateTime targetDateTime = message.getDateTime()
                .atZoneSameInstant(targetZone)
                .toOffsetDateTime();

        List<NoteDTO> notes = message.getNotes().stream()
                .map(n -> new NoteDTO(
                        n.getId(),
                        n.getText(),
                        n.getCreatedAt(),
                        n.getUser() != null ? n.getUser().getUsername() : "Аноним"
                ))
                .toList();

        return new MessageDTO(
                message.getId(),
                message.getText(),
                message.getAuthor(),
                targetDateTime,
                replyDTO,
                attachments,
                message.getForwardedFrom(),
                notes
        );
    }
    public Page<MessageDTO> searchMessages(Pageable pageable, MessageFilter filter) {
        Specification<Message> spec = Specification
                .where(MessageSpecifications.hasAuthor(filter.author()))
                .and(MessageSpecifications.hasText(filter.query()))
                .and(MessageSpecifications.createdAfter(filter.dateFrom()))
                .and(MessageSpecifications.createdBefore(filter.dateTo()))
                .and(MessageSpecifications.hasMediaType(filter.mediaType()));


        return repository.findAll(spec, pageable)
                .map(this::convertToDTO);
    }

    public int calculatePageNumberByDate(LocalDate localDate, int pageSize) {
        OffsetDateTime targetDateTime = localDate.atStartOfDay(targetZone).toOffsetDateTime();

        long countBefore = repository.countByDateTimeBefore(targetDateTime);

        return (int) (countBefore / pageSize);
    }

    public int calculatePageNumberByMessageId(Long messageId, int pageSize) {
        Message targetMessage = repository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found with id: " + messageId));

        long messagesBefore = repository.countByDateTimeLessThan(targetMessage.getDateTime());

        return (int) (messagesBefore / pageSize);
    }
}
