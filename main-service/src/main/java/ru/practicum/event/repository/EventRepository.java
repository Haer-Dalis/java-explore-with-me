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
           SELECT e
           FROM Event AS e
           WHERE (:users IS NULL OR e.initiator.id IN :users)
           AND (:states IS NULL OR e.state IN :states)
           AND (:categories IS NULL OR e.category.id IN :categories)
           AND (:rangeStart IS NULL OR e.eventDate >= :rangeStart)
           AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd)
           ORDER BY e.eventDate DESC
           """)
    List<Event> findAdminEvents(@Param("users") List<Long> users,
                                @Param("states") List<State> states,
                                @Param("categories") List<Long> categories,
                                @Param("rangeStart") LocalDateTime rangeStart,
                                @Param("rangeEnd") LocalDateTime rangeEnd,
                                Pageable pageable);

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
