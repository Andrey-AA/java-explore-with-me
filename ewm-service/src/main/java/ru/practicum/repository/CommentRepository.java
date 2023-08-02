package ru.practicum.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> getAllByAutherIdOrderByCreatedDateDesc(Integer id, Pageable pageable);

    List<Comment> getAllByAutherIdOrderByCreatedDateAsc(Integer id, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.event.id = :eventId")
    Page<Comment> findAllByEventId(@Param("eventId") Integer eventId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.event.initiator.id = :userId")
    Page<Comment> findAllByEventInitiatorId(@Param("userId") Integer userId, Pageable pageable);

}
