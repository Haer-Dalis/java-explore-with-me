package ru.practicum.comments.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.comments.model.Comment;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentsRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByEventIdAndCreatedBetweenOrderByIdAsc(Long eventId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Comment> findByUserIdAndCreatedBetweenOrderByIdAsc(Long userId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Comment> findByEventIdOrderByIdAsc(Long eventId, Pageable pageable);
    
    List<Comment> findByUserIdOrderByIdAsc(Long userId, Pageable pageable);
}
