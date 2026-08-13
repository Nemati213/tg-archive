package ru.itmo.nemat.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;

@Configuration
public class DateTimeConfig {
    @Value("${app.chat.timezone:UTC}")
    private String timezone;
    @Bean
    public ZoneId zoneId(){
        return ZoneId.of(timezone);
    }
}
