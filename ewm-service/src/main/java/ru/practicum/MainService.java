package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class MainService {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(MainService.class);

        Map<String, Object> props = new HashMap<>();
        props.put("stats-server.url", "http://stats-server:9090");
        app.setDefaultProperties(props);
        app.run(args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
