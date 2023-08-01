package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class StatsServer {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(StatsServer.class);

        Map<String, Object> props = new HashMap<>();
        props.put("server.port", "9090");
        props.put("stat-server.url", "http://stats-server:9090");
//        props.put("stats-server.url", "http://localhost:9090");

        app.setDefaultProperties(props);
        app.run(args);
    }
}
