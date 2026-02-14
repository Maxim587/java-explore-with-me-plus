package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.CommentDtoAdmin;
import ru.practicum.dto.NewCommentDto;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", source = "user")
    @Mapping(target = "event", source = "event")
    Comment mapToComment(NewCommentDto comment, User user, Event event);

    @Mapping(target = "authorName", source = "comment.author.name")
    @Mapping(target = "eventId", source = "comment.event.id")
    CommentDto mapToCommentDto(Comment comment);

    @Mapping(target = "authorId", source = "comment.author.id")
    @Mapping(target = "eventId", source = "comment.event.id")
    CommentDtoAdmin mapToCommentDtoAdmin(Comment comment);
}
