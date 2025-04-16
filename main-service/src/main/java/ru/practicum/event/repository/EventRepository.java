package ru.practicum.event.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.State;
import ru.practicum.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByInitiatorId(Long userId, Pageable pageable);

    @Query("""
            select e
            from Event as e
            where (?1 is null or e.initiator.id in ?1)
            and (?2 is null or e.state in ?2)
            and (?3 is null or e.category.id in ?3)
            and e.eventDate between ?4 and ?5
            order by e.eventDate desc
            """)
    List<Event> findAllEventsByFilterAndPeriod(List<Long> users, List<State> states, List<Long> categories,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable);

    @Query("""
            select e
            from Event as e
            where (?1 is null or e.initiator.id in ?1)
            and (?2 is null or e.state in ?2)
            and (?3 is null or e.category.id in ?3)
            and e.eventDate >= ?4
            order by e.eventDate desc
            """)
    List<Event> findAllEventsByFilterAndRangeStart(List<Long> users, List<State> states, List<Long> categories,
                                                   LocalDateTime rangeStart, Pageable pageable);

    @Query("""
            select e
            from Event as e
            where (?1 is null or e.initiator.id in ?1)
            and (?2 is null or e.state in ?2)
            and (?3 is null or e.category.id in ?3)
            and e.eventDate <= ?4
            order by e.eventDate desc
            """)
    List<Event> findAllEventsByFilterAndRangeEnd(List<Long> users, List<State> states, List<Long> categories,
                                                 LocalDateTime rangeEnd, Pageable pageable);

    @Query("""
            select e
            from Event as e
            where (?1 is null or e.initiator.id in ?1)
            and (?2 is null or e.state in ?2)
            and (?3 is null or e.category.id in ?3)
            order by e.eventDate desc
            """)
    List<Event> findAllByParams(List<Long> users, List<State> states, List<Long> categories, Pageable pageable);

    @Query("SELECT e FROM Event e " +
            "WHERE (:text IS NULL OR (e.annotation ILIKE %:text% OR e.description ILIKE %:text%)) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND e.eventDate BETWEEN :rangeStart AND :rangeEnd " +
            "AND (:onlyAvailable = FALSE OR e.confirmedRequests < e.participantLimit) " +
            "AND e.state = 'PUBLISHED'")
    List<Event> findPublishedEvents(@Param("text") String text,
                                    @Param("categories") List<Long> categories,
                                    @Param("paid") Boolean paid,
                                    @Param("rangeStart") LocalDateTime rangeStart,
                                    @Param("rangeEnd") LocalDateTime rangeEnd,
                                    @Param("onlyAvailable") Boolean onlyAvailable,
                                    Pageable pageable);

    @Query("SELECT e FROM Event e " +
            "WHERE (:text IS NULL OR (e.annotation ILIKE %:text% OR e.description ILIKE %:text%)) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND e.eventDate >= :rangeStart " +
            "AND (:onlyAvailable = FALSE OR e.confirmedRequests < e.participantLimit) " +
            "AND e.state = 'PUBLISHED'")
    List<Event> findPublishedEvents(@Param("text") String text,
                                    @Param("categories") List<Long> categories,
                                    @Param("paid") Boolean paid,
                                    @Param("rangeStart") LocalDateTime rangeStart,
                                    @Param("onlyAvailable") Boolean onlyAvailable,
                                    Pageable pageable);

    boolean existsByCategory(Category category);
}
