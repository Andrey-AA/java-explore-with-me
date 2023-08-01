package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
public class StatsClient extends BaseClient {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }
//    @Autowired
//    public StatsClient(RestTemplateBuilder builder) {
//        super(
//                builder
//                        .uriTemplateHandler(new DefaultUriBuilderFactory("http://localhost:9090"))
//                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
//                        .build()
//        );
//    }

    public ResponseEntity<Object> addStat(EndpointHitDto createStatDto) {
        return post("/hit", createStatDto);
    }

    public List<ViewStatsDto> getStat(LocalDateTime start, LocalDateTime end, Collection<String> uris, Boolean unique) {
        Map<String, Object> parameters = Map.of(
                "start", start.format(formatter),
                "end", end.format(formatter),
                "uris", String.join(",", uris),
                "unique", unique
        );
        String queryString = "?start={start}&end={end}&uris={uris}&unique={unique}";

        ResponseEntity<ViewStatsDto[]> responseEntity = get("/stats" + queryString, null, parameters);

        ViewStatsDto[] stats = responseEntity.getBody();

        if (stats != null && stats.length > 0) {
            return Arrays.asList(stats);
        }
        return new ArrayList<>();
    }
}
