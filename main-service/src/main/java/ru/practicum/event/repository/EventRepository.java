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
    from Event e
    where (:text is null or (e.annotation ilike %:text% or e.description ilike %:text%))
    and (:categories is null or e.category.id in :categories)
    and (:paid is null or e.paid = :paid)
    and e.eventDate between :rangeStart and :rangeEnd
    and (:onlyAvailable = false or e.confirmedRequests < e.participantLimit)
    and e.state = 'PUBLISHED'
    """)
    List<Event> findAllEvents(@Param("text") String text,
                              @Param("categories") List<Long> categories,
                              @Param("paid") Boolean paid,
                              @Param("rangeStart") LocalDateTime rangeStart,
                              @Param("rangeEnd") LocalDateTime rangeEnd,
                              @Param("onlyAvailable") Boolean onlyAvailable,
                              Pageable pageable);

    @Query("""
    select e
    from Event e
    where (:users is null or e.initiator.id in :users)
    and (:states is null or e.state in :states)
    and (:categories is null or e.category.id in :categories)
    and (:rangeStart is null or e.eventDate >= :rangeStart)
    and (:rangeEnd is null or e.eventDate <= :rangeEnd)
    order by e.eventDate desc
    """)
    List<Event> findAllEvents(@Param("users") List<Long> users,
                              @Param("states") List<State> states,
                              @Param("categories") List<Long> categories,
                              @Param("rangeStart") LocalDateTime rangeStart,
                              @Param("rangeEnd") LocalDateTime rangeEnd,
                              Pageable pageable);

    boolean existsByCategory(Category category);
}
