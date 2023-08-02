package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.dto.RequestCountDto;
import ru.practicum.model.Request;
import ru.practicum.model.RequestStatus;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Integer> {

    List<Request> findByRequesterId(int userId);

    List<Request> findByIdIn(List<Integer> requestIds);

    @Query("select new ru.practicum.dto.RequestCountDto(r.event.id, count(r.id)) " +
            "from Request as r " +
            "where r.event.id = ?1 " +
            "AND r.status = ?2 " +
            "group by r.event.id " +
            "order by count(r.id) desc")
    RequestCountDto findRequestCountDtoByEventIdAndStatus(int eventId, RequestStatus status);

    @Query("select new ru.practicum.dto.RequestCountDto(r.event.id, count(r.id)) " +
            "from Request as r " +
            "where r.event.id IN ?1 " +
            "AND r.status = ?2 " +
            "group by r.event.id " +
            "order by count(r.id) desc")
    List<RequestCountDto> findRequestCountDtoListByEventId(List<Integer> eventIdList, RequestStatus status);

    List<Request> findByEventId(int eventId);

    List<RequestCountDto> findAllRequestCountDtoByEventIdInAndStatus(List<Integer> eventIdList, RequestStatus confirmed);

}
