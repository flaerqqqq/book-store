package com.epam.rd.autocode.spring.project.mapper;

import com.epam.rd.autocode.spring.project.conf.GlobalMapperConfig;
import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.BookRequestDto;
import com.epam.rd.autocode.spring.project.dto.BookResponseDto;
import com.epam.rd.autocode.spring.project.model.Book;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class)
public interface BookMapper {

    BookDTO entityToDto(Book entity);

    @Mapping(target = "publicId", ignore = true)
    Book dtoToEntity(BookDTO dto);

    BookDTO requestDtoToDto(BookRequestDto requestDto);

    BookRequestDto dtoToRequestDto(BookDTO dto);

    BookResponseDto dtoToResponseDto(BookDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "publicId", ignore = true)
    void updateBookFromDto(BookDTO dto, @MappingTarget Book book);
}