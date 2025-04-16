package ru.practicum.comments.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.CommentImportDto;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.mapper.CommentMapper;
import ru.practicum.comments.model.Comment;
import ru.practicum.comments.repository.CommentsRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.additions.Constants.DATE_TIME_FORMATTER;

@Service
@RequiredArgsConstructor
public class CommentsServiceImpl implements CommentsService {

    private final CommentsRepository commentsRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public CommentDto addCommentDto(Long userId, Long eventId, NewCommentDto newCommentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено: " + eventId));

        Comment comment = CommentMapper.toComment(newCommentDto);
        comment.setUser(user);
        comment.setEvent(event);
        comment.setCreated(LocalDateTime.now());

        return CommentMapper.toCommentDto(commentsRepository.save(comment));
    }

    @Override
    public CommentDto updateCommentDto(Long userId, Long commentId, CommentImportDto commentImportDto) {
        Comment updatingComment = commentsRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден: " + commentId));
        validateOwnership(updatingComment.getUser().getId(), userId);
        Optional.ofNullable(commentImportDto.getMessage()).ifPresent(updatingComment::setMessage);
        return CommentMapper.toCommentDto(commentsRepository.save(updatingComment));
    }

    @Override
    public void deleteComment(Long userId, Long commentId) {
        Comment deletedComment = commentsRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден: " + commentId));
        validateOwnership(deletedComment.getUser().getId(), userId);
        commentsRepository.deleteById(commentId);
    }

    @Override
    public void adminDelete(Long commentId) {
        commentsRepository.deleteById(commentsRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден: " + commentId)).getId());
    }

    @Override
    public List<CommentImportDto> getUserCommentsDto(Long userId, String rangeStart, String rangeEnd, Integer from, Integer size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден: " + userId);
        }
        List<Comment> comments = fetchComments(commentsRepository::findByUserIdAndCreatedBetweenOrderByIdAsc,
                commentsRepository::findByUserIdOrderByIdAsc, userId, rangeStart, rangeEnd, from, size);

        return comments.stream()
                .map(CommentMapper::toCommentImportDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentImportDto> getEventCommentsDto(Long eventId, String rangeStart, String rangeEnd, Integer from, Integer size) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Событие не найдено: " + eventId);
        }
        List<Comment> comments = fetchComments(commentsRepository::findByEventIdAndCreatedBetweenOrderByIdAsc,
                commentsRepository::findByEventIdOrderByIdAsc,
                eventId, rangeStart, rangeEnd, from, size);
        return comments.stream()
                .map(CommentMapper::toCommentImportDto)
                .collect(Collectors.toList());
    }

    private void validateOwnership(Long authorId, Long requesterId) {
        if (!authorId.equals(requesterId)) {
            throw new ConflictException(String.format("Попытка несанкционированного доступа от %s", requesterId));
        }
    }

    private List<Comment> fetchComments(FetchWithDateRange fetchWithRange,
                                        FetchWithoutDateRange fetchWithoutRange,
                                        Long id, String rangeStart, String rangeEnd,
                                        int from, int size) {

        LocalDateTime start = parseDate(rangeStart);
        LocalDateTime end = parseDate(rangeEnd);
        Pageable pageable = PageRequest.of(from / size, size);

        if (start != null && end != null) {
            return fetchWithRange.fetch(id, start, end, pageable);
        } else {
            return fetchWithoutRange.fetch(id, pageable);
        }
    }

    @FunctionalInterface
    private interface FetchWithDateRange {
        List<Comment> fetch(Long id, LocalDateTime start, LocalDateTime end, Pageable pageable);
    }

    @FunctionalInterface
    private interface FetchWithoutDateRange {
        List<Comment> fetch(Long id, Pageable pageable);
    }

    private LocalDateTime parseDate(String date) {
        return date == null ? null : LocalDateTime.parse(date, DATE_TIME_FORMATTER);
    }
}