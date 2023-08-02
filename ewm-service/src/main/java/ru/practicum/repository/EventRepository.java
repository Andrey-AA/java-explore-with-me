package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Event;
import ru.practicum.model.State;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Integer> {

    List<Event> findByInitiatorId(int initiatorId, Pageable pageable);

    @Query(" select e from Event e " +
            "WHERE (:users is null or e.initiator.id IN :users) " +
            "and (:state is null or e.state IN :state) " +
            "and (:categories is null or e.category.id IN :categories) " +
            "and (e.eventDate BETWEEN :start and :end) " +
            " ORDER BY e.id DESC")
    List<Event> findByUserStateCategoryStartEndOrderByIdDesc(@Param("users") List<Integer> users, @Param("state") List<State> state, @Param("categories") List<Integer> categories,
                                                             @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    @Query(" select e from Event e " +
            " where (upper(e.annotation) like upper(concat('%', ?1, '%')) " +
            " or upper(e.description) like upper(concat('%', ?1, '%'))) " +
            " and (e.category.id IN (?2)) " +
            " and (e.paid = ?3) " +
            " and e.eventDate BETWEEN ?4 and ?5 " +
            " ORDER BY e.eventDate DESC")
    List<Event> findByTextCategoriesPaidStartEndSortByEventDate(String text, List<Integer> categories, Boolean paid, LocalDateTime start, LocalDateTime end,
                                                                Pageable pageable);

    @Query(" select e from Event e " +
            " where (:text is null or upper(e.annotation) like upper(concat('%', :text, '%')) " +
            " or upper(e.description) like upper(concat('%', :text, '%'))) " +
            " and (:categories is null or e.category.id IN :categories) " +
            " and (:paid is null or e.paid = :paid) " +
            " and e.eventDate >= :now ")
    List<Event> findByTextCategoriesPaidEventDateAfter(@Param("text") String text,
                                                       @Param("categories") List<Integer> categories,
                                                       @Param("paid") Boolean paid,
                                                       @Param("now") LocalDateTime now, Pageable pageable);

    Optional<Event> findByIdAndState(int eventId, State state);

}
