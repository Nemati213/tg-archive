package ru.itmo.nemat.services;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import ru.itmo.nemat.TelegramParser;
import ru.itmo.nemat.data.MessageRepository;
import ru.itmo.nemat.models.Message;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

@Service
public class ChatLoaderService {
    @Value("${app.export.root-path}")
    private String rootPath;

    private TelegramParser parser;
    private MessageRepository messageRepository;
    private final EntityManager entityManager;
    public ChatLoaderService(TelegramParser parser, MessageRepository messageRepository, EntityManager entityManager) {
        this.parser = parser;
        this.messageRepository = messageRepository;
        this.entityManager = entityManager;
    }

    public void loadAllMessages() throws IOException {
        if (messageRepository.count() > 0) {
            System.out.println("База уже содержит данные. Пропускаю импорт.");
            return;
        }

        try (Stream<Path> paths = Files.list(Paths.get(rootPath))) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith("message"))
                    .filter(path -> path.getFileName().toString().endsWith(".html"))
                    .sorted(Comparator.comparingInt(path -> {
                        String fileName = path.getFileName().toString();
                        String numberStr = fileName.replaceAll("\\D", "");
                        return Integer.parseInt(numberStr);
                    }))
                    .forEach(path -> {
                        try {
                            List<Message> messages = parser.parseFile(path.toFile());
                            messageRepository.saveAll(messages);
                            System.out.println("Файл " + path.getFileName() + " успешно загружен.");
                        } catch (IOException e) {
                            throw new RuntimeException("Ошибка при чтении файла " + path, e);
                        }
                    });
        } catch (Exception e) {
            System.err.println("Ошибка импорта: " + e.getMessage());
        }
    }

    public void linkReplies() {
        if(messageRepository.countAllByTgReplyIdNotNullAndReplyToIsNull() == 0) return;
        System.out.println(messageRepository.countAllByTgReplyIdNotNullAndReplyToIsNull());
        System.out.println("Начинаем второй проход: связываем ответы...");
        List<Object[]> mappings = messageRepository.findAllIdMappings();
        Map<Long, Long> tgIdToInternalId = new HashMap<>();
        for (Object[] row : mappings) {
            tgIdToInternalId.put((Long) row[0], (Long) row[1]);
        }
        int pageSize = 2000;
        Slice<Message> page;
        do {
            page = messageRepository.findAllByTgReplyIdNotNullAndReplyToIsNull(PageRequest.of(0, pageSize));
            System.out.println(page);
            for (Message msg : page.getContent()) {
                Long parentTgId = msg.getTgReplyId();
                Long parentInternalId = tgIdToInternalId.get(parentTgId);

                if (parentInternalId != null) {
                    Message parentProxy = entityManager.getReference(Message.class, parentInternalId);
                    msg.setReplyTo(parentProxy);
                }
            }

            messageRepository.saveAll(page.getContent());
            messageRepository.flush();
            entityManager.clear();

            System.out.println("Обработана страница");

        } while (page.hasNext());

        System.out.println("Все ответы успешно связаны!");


    }
}
