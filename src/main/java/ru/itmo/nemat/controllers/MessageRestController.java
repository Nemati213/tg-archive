package ru.itmo.nemat.controllers;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.nemat.dto.MessageDTO;
import ru.itmo.nemat.models.MessageFilter;
import ru.itmo.nemat.services.MessageService;

import java.time.LocalDate;

@RestController
@RequestMapping(path = "api/v1/messages")
@RequiredArgsConstructor
public class MessageRestController {
    private final MessageService messageService;

    @GetMapping
    public Page<MessageDTO> getMessages(
            @PageableDefault(size = 50, sort = "dateTime", direction = Sort.Direction.ASC)
            Pageable pageable,
            MessageFilter filter
    ) {
        return messageService.searchMessages(pageable, filter);
    }

    @GetMapping("/page-by-date")
    public int getPageNumberByDate(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,

            @RequestParam(defaultValue = "50")
            int pageSize
    ) {
        return messageService.calculatePageNumberByDate(date, pageSize);
    }

    @GetMapping("/page-by-id")
    public int getPageNumberByTgId(
            @RequestParam Long messageId,
            @RequestParam(defaultValue = "50") int pageSize
    ) {
        return messageService.calculatePageNumberByMessageId(messageId, pageSize);
    }

}
