package ru.practicum.comments.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.additions.Constants;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.CommentImportDto;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.service.CommentsService;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentsService commentsService;

    @PostMapping("/user/{userId}/event/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@PathVariable Long userId,
                                 @PathVariable Long eventId,
                                 @Valid @RequestBody NewCommentDto newCommentDto) {
        return commentsService.addCommentDto(userId, eventId, newCommentDto);
    }

    @PatchMapping("/user/{userId}/update/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto updateComment(@PathVariable Long userId,
                                    @PathVariable Long commentId,
                                    @Valid @RequestBody CommentImportDto commentNewDto) {
        return commentsService.updateCommentDto(userId, commentId, commentNewDto);
    }

    @GetMapping("/user/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentImportDto> getUserComments(@PathVariable Long userId,
                                                  @RequestParam(required = false) String rangeStart,
                                                  @RequestParam(required = false) String rangeEnd,
                                                  @RequestParam(defaultValue = Constants.DEFAULT_FROM) Integer from,
                                                  @RequestParam(defaultValue = Constants.DEFAULT_SIZE) Integer size) {
        return commentsService.getUserCommentsDto(userId, rangeStart, rangeEnd, from, size);
    }

    @DeleteMapping("/user/{userId}/delete/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId,
                              @PathVariable Long commentId) {
        commentsService.deleteComment(userId, commentId);
    }

    @GetMapping("/event/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentImportDto> getEventComments(@PathVariable Long eventId,
                                                   @RequestParam(required = false) String rangeStart,
                                                   @RequestParam(required = false) String rangeEnd,
                                                   @RequestParam(defaultValue = Constants.DEFAULT_FROM) Integer from,
                                                   @RequestParam(defaultValue = Constants.DEFAULT_SIZE) Integer size) {
        return commentsService.getEventCommentsDto(eventId, rangeStart, rangeEnd, from, size);
    }
}
