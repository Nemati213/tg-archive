package ru.itmo.nemat.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.nemat.models.Message;
import ru.itmo.nemat.models.ReadingProgress;
import ru.itmo.nemat.models.User;
import ru.itmo.nemat.data.MessageRepository;
import ru.itmo.nemat.data.ReadingProgressRepository;
import ru.itmo.nemat.data.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class ReadingProgressService {
    private final ReadingProgressRepository progressRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    @Transactional
    public void saveOrUpdateProgress(Long messageId) {
        User currentUser = getCurrentUser();
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Сообщение не найдено: " + messageId));

        ReadingProgress progress = progressRepository.findByUser(currentUser)
                .orElseGet(() -> {
                    ReadingProgress newProgress = new ReadingProgress();
                    newProgress.setUser(currentUser);
                    return newProgress;
                });

        progress.setLastReadMessage(message);
        progress.setUpdatedAt(OffsetDateTime.now());

        progressRepository.save(progress);
    }

    @Transactional(readOnly = true)
    public Long getLastReadMessageId() {
        User currentUser = getCurrentUser();
        return progressRepository.findByUser(currentUser)
                .map(progress -> progress.getLastReadMessage().getId())
                .orElse(null);
    }
}