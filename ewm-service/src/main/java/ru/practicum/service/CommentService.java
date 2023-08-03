package ru.practicum.service;


import lombok.AllArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.CommentEntryDto;
import ru.practicum.exception.ObjectNotFoundException;
import ru.practicum.exception.RequestNotValidException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.repository.CommentRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class CommentService {

    private final UserRepository userRepository;
    private final CommentRepository repository;
    private final EventRepository eventRepository;

    public List<CommentDto> getCommentsByUser(Integer userId, Boolean asc, Integer from, Integer size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователь не найден: userId = " + userId));

        Pageable pageable = PageRequest.of(from, size);
        List<Comment> comments = BooleanUtils.isTrue(asc) ? repository.getAllByAuthorIdOrderByCreatedDateAsc(user.getId(), pageable)
                : repository.getAllByAuthorIdOrderByCreatedDateDesc(user.getId(), pageable);

        if (comments.isEmpty()) {
            return Collections.emptyList();
        }

        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    public CommentDto addComment(Integer userId, Integer eventId, CommentEntryDto entryDto) {
        if (!userRepository.existsById(userId)) {
            throw new ObjectNotFoundException("User Not Found!");
        }
        if (!eventRepository.existsById(eventId)) {
            throw new ObjectNotFoundException("Event Not Found!");
        }
        Comment comment = CommentMapper.fromEntryComment(entryDto);
        comment.setCreatedDate(LocalDateTime.now());
        comment.setEvent(new Event().setId(eventId));
        comment.setAuthor(new User().setId(userId));
        return CommentMapper.toCommentDto(repository.save(comment));
    }

    public CommentDto updateComment(Integer userId, Integer commentId, CommentEntryDto entryDto) {
        if (!repository.existsById(commentId) || !userRepository.existsById(userId)) {
            throw new ObjectNotFoundException("Comment or User Not Found!");
        }
        Comment comment = repository.getOne(commentId);
        if (comment.getAuthor().getId().longValue() != userId.longValue()) {
            throw new RequestNotValidException("Only sender can update!");
        }
        if (entryDto.getText() != null) {
            comment.setText(entryDto.getText());
            comment.setUpdatedDate(LocalDateTime.now());
        }
        return CommentMapper.toCommentDto(repository.save(comment));
    }

    public void deleteCommentByOwner(Integer userId, Integer commentId) {
        if (!repository.existsById(commentId) || !userRepository.existsById(userId)) {
            throw new ObjectNotFoundException("Comment or User Not Found!");
        }
        Comment comment = repository.getOne(commentId);
        if (comment.getAuthor().getId().longValue() != userId.longValue()) {
            throw new RequestNotValidException("Only sender or admin can delete it!");
        }
        repository.deleteById(commentId);
    }

    @Transactional(readOnly = true)
    public CommentDto getCommentById(Integer commentId) {
        Comment comment = repository.findById(commentId)
                .orElseThrow(() -> new ObjectNotFoundException("Comment Not Found!"));
        return CommentMapper.toCommentDto(comment);
    }

}
