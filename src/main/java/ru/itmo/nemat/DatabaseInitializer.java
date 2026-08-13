package ru.itmo.nemat;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.itmo.nemat.services.ChatLoaderService;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final ChatLoaderService chatLoaderService;

    public DatabaseInitializer(ChatLoaderService chatLoaderService) {
        this.chatLoaderService = chatLoaderService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> Запуск процесса инициализации базы данных...");

        chatLoaderService.loadAllMessages();
        chatLoaderService.linkReplies();
        System.out.println(">>> База данных готова к работе!");
    }
}