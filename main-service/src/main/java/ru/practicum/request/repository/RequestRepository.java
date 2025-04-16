package ru.practicum.request.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.request.model.Request;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByRequesterId(Long id, Sort sort);

    Boolean existsByRequesterIdAndEventId(Long id, Long eventId);

    List<Request> findRequestsByEventId(Long eventId, Sort sort);
}