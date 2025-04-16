package ru.practicum.comments.service;

import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.CommentImportDto;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.model.Comment;

import java.util.List;

public interface CommentsService {

    CommentDto addCommentDto(Long userId, Long eventId, NewCommentDto newCommentDto);

    CommentDto updateCommentDto(Long userId, Long commentId, CommentImportDto dto);

    List<CommentImportDto> getUserCommentsDto(Long userId, String rangeStart, String rangeEnd, Integer from, Integer size);

    List<CommentImportDto> getEventCommentsDto(Long eventId, String rangeStart, String rangeEnd, Integer from, Integer size);

    void deleteComment(Long userId, Long commentId);

    void  adminDelete(Long commentId);
}
